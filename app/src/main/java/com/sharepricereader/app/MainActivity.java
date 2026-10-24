package com.sharepricereader.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private static final int REQ_FILE = 1;
    private static final int REQ_PERM = 2;
    private static final int REQ_UPDATE = 3;
    private static final int REQ_CAM = 4;
    private static final int REQ_BROWSER = 5;
    private static final int REQ_WEBCAM = 6;
    private android.webkit.PermissionRequest pendingCam;
    private Uri camUri;
    private File camFile;
    private static final String HOME = "file:///android_asset/index.html";

    private WebView web;
    private ValueCallback<Uri[]> chooser;

    // পেজ ঠিকমতো চালু হলো কিনা দেখার জন্য (ভুল আপডেটের পর উদ্ধার)
    private volatile boolean pageReady;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable readyCheck = new Runnable() {
        @Override
        public void run() {
            if (!pageReady && !isFinishing()) showRecovery();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // নতুন APK install হলে পুরনো ইন-অ্যাপ আপডেট মুছে যায়
        Updater.resetIfApkChanged(this);

        // Android 6-9: Downloads-এ লেখার অনুমতি চাওয়া
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERM);
        }

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);

        web.addJavascriptInterface(new Bridge(), "Android");

        // বদলানো ফাইল থাকলে সেটা দেখানো, না থাকলে মূল asset
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                WebResourceResponse r = Updater.intercept(MainActivity.this, req.getUrl());
                return r != null ? r : super.shouldInterceptRequest(view, req);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                super.onPageStarted(view, url, icon);
                pageReady = false;
                handler.removeCallbacks(readyCheck);
                if (Updater.hasUpdate(MainActivity.this)) handler.postDelayed(readyCheck, 8000);
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final android.webkit.PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean wantsVideo = false;
                        for (String r : request.getResources()) {
                            if (android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) wantsVideo = true;
                        }
                        if (!wantsVideo) {
                            request.deny();
                            return;
                        }
                        if (Build.VERSION.SDK_INT >= 23
                                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            pendingCam = request;
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_WEBCAM);
                        } else {
                            request.grant(new String[]{android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                        }
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (chooser != null) {
                    chooser.onReceiveValue(null);
                }
                chooser = callback;
                if (params.isCaptureEnabled()) {
                    try {
                        camFile = new File(ShareProvider.dir(MainActivity.this), "cap_" + System.currentTimeMillis() + ".jpg");
                        camFile.createNewFile();
                        camUri = ShareProvider.uriFor(camFile);
                        Intent ci = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        ci.putExtra(MediaStore.EXTRA_OUTPUT, camUri);
                        ci.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(ci, REQ_CAM);
                        return true;
                    } catch (Exception e) {
                        // camera না পেলে সাধারণ file বাছাই
                    }
                }
                try {
                    startActivityForResult(params.createIntent(), REQ_FILE);
                } catch (Exception e) {
                    chooser = null;
                    toast("File বাছাই খোলা যায়নি");
                    return false;
                }
                return true;
            }
        });

        loadHome();
    }

    // ------------------------------------------------------------ Update menu

    private void showUpdateMenu() {
        String[] items = {
                "Update from file",
                "Paste update code",
                "Undo last update",
                "Save updated files",
                "Reset to original"
        };
        new AlertDialog.Builder(this)
                .setTitle("App update")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        switch (which) {
                            case 0: pickUpdateFile(); break;
                            case 1: showPasteDialog(); break;
                            case 2: confirmUndo(); break;
                            case 3: exportUpdates(); break;
                            case 4: confirmReset(); break;
                        }
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /** আপডেট ভেঙে গেলে (পেজ চালু হয়নি) উদ্ধারের ডায়ালগ। */
    private void showRecovery() {
        new AlertDialog.Builder(this)
                .setTitle("App did not load")
                .setMessage("The last update may be broken.")
                .setPositiveButton("Undo update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        Updater.undo(MainActivity.this);
                        reloadApp();
                    }
                })
                .setNeutralButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        Updater.reset(MainActivity.this);
                        reloadApp();
                    }
                })
                .setNegativeButton("Ignore", null)
                .show();
    }

    private void pickUpdateFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            startActivityForResult(i, REQ_UPDATE);
        } catch (Exception e) {
            toast("Cannot open file picker");
        }
    }

    private void showPasteDialog() {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        et.setTypeface(Typeface.MONOSPACE);
        et.setTextSize(12);
        et.setGravity(Gravity.TOP | Gravity.START);
        et.setMinLines(8);
        et.setMaxLines(14);
        et.setHorizontallyScrolling(true);
        et.setHint("Paste code starting with #SRUPDATE");

        // ক্লিপবোর্ডে আপডেট কোড থাকলে নিজে থেকেই বসিয়ে দেওয়া
        String clip = readClipboard();
        if (clip != null && clip.trim().startsWith("#SRUPDATE")) et.setText(clip);

        new AlertDialog.Builder(this)
                .setTitle("Paste update code")
                .setView(et)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        String t = et.getText().toString();
                        if (t.trim().isEmpty()) { toast("Nothing pasted"); return; }
                        runUpdate(t.getBytes(StandardCharsets.UTF_8));
                    }
                })
                .setNeutralButton("Clipboard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        String c = readClipboard();
                        if (c == null || c.trim().isEmpty()) { toast("Clipboard is empty"); return; }
                        runUpdate(c.getBytes(StandardCharsets.UTF_8));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String readClipboard() {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null || !cm.hasPrimaryClip()) return null;
            ClipData cd = cm.getPrimaryClip();
            if (cd == null || cd.getItemCount() == 0) return null;
            CharSequence cs = cd.getItemAt(0).coerceToText(this);
            return cs == null ? null : cs.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void runUpdate(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final PatchEngine.Result r = Updater.apply(MainActivity.this, data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reloadApp();
                            showMessage("✅ Updated", r.summary());
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String m = e.getMessage() != null ? e.getMessage() : e.toString();
                            showMessage("❌ Failed (nothing changed)", m);
                        }
                    });
                }
            }
        }).start();
    }

    private void confirmUndo() {
        if (!Updater.hasUpdate(this)) { toast("Nothing to undo"); return; }
        new AlertDialog.Builder(this)
                .setTitle("Undo last update?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        Updater.undo(MainActivity.this);
                        reloadApp();
                        toast("Undone");
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmReset() {
        if (!Updater.hasUpdate(this)) { toast("Already original"); return; }
        new AlertDialog.Builder(this)
                .setTitle("Reset to original?")
                .setMessage("All updates are removed. Your saved data stays.")
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        Updater.reset(MainActivity.this);
                        reloadApp();
                        toast("Reset done");
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void exportUpdates() {
        try {
            byte[] z = Updater.exportZip(this);
            if (z == null) { toast("No updated files"); return; }
            saveToDownloads("DataShow-updated-files.zip", "application/zip", z, "Saved to Downloads: ");
        } catch (Exception e) {
            toast("Failed: " + e.getMessage());
        }
    }

    private void reloadApp() {
        web.clearCache(true);
        loadHome();
    }

    /**
     * Main page load. Updated index.html is given to the WebView directly
     * (base URL stays file:///android_asset/index.html, so saved data / localStorage is the same).
     * shouldInterceptRequest is not reliable for the main file:// page on many phones,
     * that is why the update showed in the dialog but the old page kept loading.
     */
    private void loadHome() {
        try {
            File f = new File(Updater.dir(this), "index.html");
            if (Updater.hasUpdate(this) && f.isFile() && f.length() > 0) {
                String html;
                try (InputStream in = new java.io.FileInputStream(f)) {
                    html = new String(PatchEngine.readAll(in), StandardCharsets.UTF_8);
                }
                web.loadDataWithBaseURL(HOME, html, "text/html", "UTF-8", HOME);
                return;
            }
        } catch (Exception ignored) {
            // fall back to the original page below
        }
        web.loadUrl(HOME);
    }

    private void showMessage(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    // ------------------------------------------------------------ সাধারণ অংশ

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_WEBCAM && pendingCam != null) {
            android.webkit.PermissionRequest r = pendingCam;
            pendingCam = null;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                r.grant(new String[]{android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE});
            } else {
                r.deny();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAM && chooser != null) {
            Uri[] r = (resultCode == RESULT_OK && camFile != null && camFile.length() > 0)
                    ? new Uri[]{camUri} : null;
            chooser.onReceiveValue(r);
            chooser = null;
        } else if (requestCode == REQ_FILE && chooser != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            chooser.onReceiveValue(result);
            chooser = null;
        } else if (requestCode == REQ_BROWSER) {
            String t = BrowserActivity.resText, u = BrowserActivity.resUrl;
            BrowserActivity.resText = null;
            BrowserActivity.resUrl = null;
            if (resultCode == RESULT_OK && t != null && !t.isEmpty()) {
                web.evaluateJavascript("window.__browserSend&&window.__browserSend("
                        + JSONObject.quote(u == null ? "" : u) + "," + JSONObject.quote(t) + ")", null);
            }
        } else if (requestCode == REQ_UPDATE && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            try (InputStream in = getContentResolver().openInputStream(data.getData())) {
                if (in == null) throw new Exception("Cannot open file");
                byte[] bytes = PatchEngine.readAll(in);
                if (bytes.length > 30 * 1024 * 1024) throw new Exception("File too large (over 30 MB)");
                runUpdate(bytes);
            } catch (Exception e) {
                showMessage("❌ Cannot read file", String.valueOf(e.getMessage()));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(readyCheck);
        super.onDestroy();
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveToDownloads(String name, String mime, byte[] data, String okMsg) throws Exception {
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues v = new ContentValues();
            v.put(MediaStore.Downloads.DISPLAY_NAME, name);
            v.put(MediaStore.Downloads.MIME_TYPE, mime);
            v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
            if (uri == null) throw new Exception("file বানানো যায়নি");
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                out.write(data);
            }
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            dir.mkdirs();
            try (FileOutputStream out = new FileOutputStream(new File(dir, name))) {
                out.write(data);
            }
        }
        toast(okMsg + name);
    }

    private void jsRes(String id, boolean ok, byte[] data) {
        final String js = "window.__urlRes('" + id + "'," + ok + ",'" + Base64.encodeToString(data, Base64.NO_WRAP) + "')";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                web.evaluateJavascript(js, null);
            }
        });
    }

    /** Gmail import result: window.__gmRes(id, ok, base64 of text) */
    private void jsGm(String id, boolean ok, String text) {
        final String js = "window.__gmRes&&window.__gmRes('" + id + "'," + ok + ",'"
                + Base64.encodeToString(text.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP) + "')";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                web.evaluateJavascript(js, null);
            }
        });
    }

    /** JavaScript থেকে ডাকা হয়: window.Android.save(filename, mime, base64) */
    private class Bridge {
        @JavascriptInterface
        public void save(String name, String mime, String base64) {
            try {
                saveToDownloads(name, mime, Base64.decode(base64, Base64.DEFAULT),
                        "Downloads ফোল্ডারে সেভ হয়েছে: ");
            } catch (Exception e) {
                toast("সেভ হয়নি: " + e.getMessage());
            }
        }

        /** Customize > App > Update app বোতাম থেকে ডাকা হয় */
        @JavascriptInterface
        public void openUpdater() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showUpdateMenu();
                }
            });
        }

        /** Website box: open the full mobile browser (fast, video works, search bar on top) */
        @JavascriptInterface
        public void openBrowser(final String url) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(MainActivity.this, BrowserActivity.class);
                    i.putExtra("url", url);
                    startActivityForResult(i, REQ_BROWSER);
                }
            });
        }

        /** Share sheet: text */
        @JavascriptInterface
        public void shareText(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(i, "Share"));
                }
            });
        }

        /** Share sheet: file (PDF, Excel, photo...) */
        @JavascriptInterface
        public void shareFile(String name, final String mime, String base64) {
            try {
                String safe = name.replaceAll("[^A-Za-z0-9._ -]", "_");
                File f = new File(ShareProvider.dir(MainActivity.this), safe);
                try (FileOutputStream o = new FileOutputStream(f)) {
                    o.write(Base64.decode(base64, Base64.DEFAULT));
                }
                final Uri u = ShareProvider.uriFor(f);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType(mime == null || mime.isEmpty() ? "*/*" : mime);
                        i.putExtra(Intent.EXTRA_STREAM, u);
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(i, "Share"));
                    }
                });
            } catch (Exception e) {
                toast("Share failed: " + e.getMessage());
            }
        }

        /** Website page download (preview box): result comes back via window.__urlRes */
        @JavascriptInterface
        public void fetchUrl(final String url, final String id) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String cur = url;
                        HttpURLConnection c = null;
                        for (int hop = 0; hop < 6; hop++) {
                            c = (HttpURLConnection) new URL(cur).openConnection();
                            String hh = new URL(cur).getHost().toLowerCase();
                            if (c instanceof javax.net.ssl.HttpsURLConnection && (hh.equals("dsebd.org") || hh.endsWith(".dsebd.org"))) {
                                // DSE's certificate chain is not trusted on some phones (public price page only, no login/secrets)
                                javax.net.ssl.TrustManager[] tm = new javax.net.ssl.TrustManager[]{new javax.net.ssl.X509TrustManager() {
                                    public void checkClientTrusted(java.security.cert.X509Certificate[] x, String a) {}
                                    public void checkServerTrusted(java.security.cert.X509Certificate[] x, String a) {}
                                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                                }};
                                javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");
                                sc.init(null, tm, new java.security.SecureRandom());
                                ((javax.net.ssl.HttpsURLConnection) c).setSSLSocketFactory(sc.getSocketFactory());
                                ((javax.net.ssl.HttpsURLConnection) c).setHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
                                    public boolean verify(String h, javax.net.ssl.SSLSession ss) { String l = h.toLowerCase(); return l.equals("dsebd.org") || l.endsWith(".dsebd.org"); }
                                });
                            }
                            c.setInstanceFollowRedirects(false);
                            c.setConnectTimeout(15000);
                            c.setReadTimeout(20000);
                            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36");
                            c.setRequestProperty("Accept-Language", "en,bn;q=0.8");
                            c.setRequestProperty("Accept", "text/html,application/xhtml+xml,*/*;q=0.8");
                            c.setRequestProperty("Referer", "https://www.dsebd.org/");
                            int code = c.getResponseCode();
                            if (code >= 300 && code < 400 && c.getHeaderField("Location") != null) {
                                cur = new URL(new URL(cur), c.getHeaderField("Location")).toString();
                                continue;
                            }
                            break;
                        }
                        InputStream in = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream();
                        ByteArrayOutputStream bo = new ByteArrayOutputStream();
                        byte[] buf = new byte[16384];
                        int n;
                        while (in != null && (n = in.read(buf)) > 0 && bo.size() < 3 * 1024 * 1024) bo.write(buf, 0, n);
                        jsRes(id, c.getResponseCode() < 400, c.getResponseCode() < 400 ? bo.toByteArray()
                                : ("HTTP " + c.getResponseCode()).getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        jsRes(id, false, String.valueOf(e.getMessage()).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }).start();
        }

        /** Table > Import from Gmail: newest PDF attachments via IMAP (app password). Result -> window.__gmRes */
        @JavascriptInterface
        public void gmailFetch(final String user, final String pass, final String from, final int days, final String id) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        jsGm(id, true, GmailFetcher.fetch(user, pass, from, days));
                    } catch (javax.mail.AuthenticationFailedException e) {
                        jsGm(id, false, "Gmail login failed. Check the address and the App password (not the normal password).");
                    } catch (Throwable e) {
                        jsGm(id, false, "Gmail: " + e.getClass().getSimpleName() + (e.getMessage() == null ? "" : " - " + e.getMessage()));
                    }
                }
            }).start();
        }

        /** পেজের শেষে ডাকা হয়: পেজ ঠিকমতো চালু হয়েছে */
        @JavascriptInterface
        public void ready() {
            pageReady = true;
        }
    }
}
