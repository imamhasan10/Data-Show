package com.sharepricereader.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.content.SharedPreferences;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Full mobile browser for the website box: fast, JavaScript on, video (YouTube etc.) plays,
 * search/address bar on top. "Send" takes the selected text (or the whole page text) back to Data Show.
 */
public class BrowserActivity extends Activity {

    /** Result is passed through these fields (an Intent is too small for a whole page). */
    static String resText, resUrl;

    private WebView web;
    private EditText addr;
    private ProgressBar bar;
    private FrameLayout root;
    private View customView;
    private WebChromeClient.CustomViewCallback customCb;
    private LinearLayout page;

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private TextView btn(String label, int size) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor("#1b2333"));
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        return t;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        resText = null;
        resUrl = null;

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets in) {
                v.setPadding(0, in.getSystemWindowInsetTop(), 0, in.getSystemWindowInsetBottom());
                return in;
            }
        });

        // ---- top bar: close, address/search, go
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(Color.parseColor("#f4f6fb"));
        top.setPadding(dp(4), dp(4), dp(4), dp(4));

        TextView close = btn("\u2190", 22);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        top.addView(close);

        addr = new EditText(this);
        addr.setSingleLine(true);
        addr.setHint("Search or type link");
        addr.setTextSize(15);
        addr.setSelectAllOnFocus(true);
        addr.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        addr.setImeOptions(EditorInfo.IME_ACTION_GO);
        addr.setBackgroundColor(Color.WHITE);
        addr.setPadding(dp(10), dp(8), dp(10), dp(8));
        addr.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent e) {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || (e != null && e.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    go();
                    return true;
                }
                return false;
            }
        });
        top.addView(addr, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView goB = btn("\uD83D\uDD0D", 20);
        goB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go();
            }
        });
        top.addView(goB);
        page.addView(top, new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));

        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setVisibility(View.GONE);
        page.addView(bar, new LinearLayout.LayoutParams(-1, dp(3)));

        // ---- web view
        web = new WebView(this);
        page.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));

        // ---- bottom bar: back, forward, reload, send
        LinearLayout bot = new LinearLayout(this);
        bot.setOrientation(LinearLayout.HORIZONTAL);
        bot.setGravity(Gravity.CENTER_VERTICAL);
        bot.setBackgroundColor(Color.parseColor("#f4f6fb"));
        bot.setPadding(dp(4), dp(2), dp(4), dp(2));

        TextView back = btn("\u25C0", 18);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (web.canGoBack()) web.goBack();
            }
        });
        TextView fwd = btn("\u25B6", 18);
        fwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (web.canGoForward()) web.goForward();
            }
        });
        TextView rel = btn("\u27F3", 20);
        rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.reload();
            }
        });
        TextView send = btn("Send text \u27A4", 15);
        send.setTextColor(Color.WHITE);
        send.setBackgroundColor(Color.parseColor("#2563eb"));
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBack();
            }
        });
        bot.addView(back);
        bot.addView(fwd);
        bot.addView(rel);
        View sp = new View(this);
        bot.addView(sp, new LinearLayout.LayoutParams(0, 1, 1f));
        bot.addView(send);
        page.addView(bot, new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));

        setup();

        String u = getIntent().getStringExtra("url");
        if (u == null || u.trim().isEmpty()) u = "https://www.google.com/";
        addr.setText(u);
        web.loadUrl(u);
    }


    /** Smart Selection bridge (called from libs/smartsel.js inside the website). */
    private String selJs;

    private class SelBridge {
        @android.webkit.JavascriptInterface
        public void send(final String t) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (t == null || t.trim().isEmpty()) return;
                    resText = t;
                    resUrl = web.getUrl();
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void share(final String t) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, t);
                    startActivity(Intent.createChooser(i, "Share"));
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void open(final String u) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u)));
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void addHist(String t) {
            try {
                SharedPreferences sp = getSharedPreferences("dssel", MODE_PRIVATE);
                JSONArray old = new JSONArray(sp.getString("hist", "[]"));
                JSONArray a = new JSONArray();
                JSONObject o = new JSONObject();
                o.put("t", t.length() > 6000 ? t.substring(0, 6000) : t);
                o.put("d", System.currentTimeMillis());
                a.put(o);
                for (int i = 0; i < old.length() && a.length() < 50; i++) a.put(old.get(i));
                sp.edit().putString("hist", a.toString()).apply();
            } catch (Exception ignored) {
            }
        }

        @android.webkit.JavascriptInterface
        public String hist() {
            return getSharedPreferences("dssel", MODE_PRIVATE).getString("hist", "[]");
        }
    }

    private String loadSelJs() {
        if (selJs != null) return selJs;
        try {
            InputStream in = getAssets().open("libs/smartsel.js");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            selJs = bo.toString("UTF-8");
        } catch (Exception e) {
            selJs = "";
        }
        return selJs;
    }

    private void injectSel() {
        String js = loadSelJs();
        if (js.isEmpty()) return;
        web.evaluateJavascript(js + "\n;try{window.DSSel.init({send:function(t){DSSelB.send(t)},share:function(t){DSSelB.share(t)},"
                + "openUrl:function(u){DSSelB.open(u)},save:function(t){},addHist:function(t){DSSelB.addHist(t)},"
                + "hist:function(){return DSSelB.hist()}})}catch(e){}", null);
    }

    private void setup() {
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        // normal mobile Chrome user agent (sites like YouTube treat "wv" WebViews worse)
        String ua = s.getUserAgentString();
        if (ua != null) s.setUserAgentString(ua.replace("; wv)", ")"));

        web.addJavascriptInterface(new SelBridge(), "DSSelB");
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, android.webkit.WebResourceRequest r) {
                String sch = r.getUrl().getScheme();
                if ("http".equals(sch) || "https".equals(sch)) return false;
                if ("tel".equals(sch) || "mailto".equals(sch)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, r.getUrl()));
                    } catch (Exception ignored) {
                    }
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView v, String url, Bitmap icon) {
                if (url != null && !addr.hasFocus()) addr.setText(url);
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                bar.setVisibility(View.GONE);
                if (url != null && !addr.hasFocus()) addr.setText(url);
                injectSel();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int p) {
                bar.setProgress(p);
                bar.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
            }

            // full screen video
            @Override
            public void onShowCustomView(View view, CustomViewCallback cb) {
                if (customView != null) {
                    cb.onCustomViewHidden();
                    return;
                }
                customView = view;
                customCb = cb;
                page.setVisibility(View.GONE);
                root.addView(view, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }

            @Override
            public void onHideCustomView() {
                hideCustom();
            }
        });
    }

    private void hideCustom() {
        if (customView == null) return;
        root.removeView(customView);
        customView = null;
        page.setVisibility(View.VISIBLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (customCb != null) {
            try {
                customCb.onCustomViewHidden();
            } catch (Exception ignored) {
            }
            customCb = null;
        }
    }

    /** Address box: a link opens, anything else is searched. */
    private void go() {
        String t = addr.getText().toString().trim();
        if (t.isEmpty()) return;
        String u;
        if (t.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            u = t;
        } else if (t.contains(" ") || !t.contains(".")) {
            u = "https://www.google.com/search?q=" + Uri.encode(t);
        } else {
            u = "https://" + t;
        }
        try {
            InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (im != null) im.hideSoftInputFromWindow(addr.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
        addr.clearFocus();
        web.loadUrl(u);
    }

    /** Selected text, or the whole page text, goes back to Data Show. */
    private void sendBack() {
        final String url = web.getUrl();
        web.evaluateJavascript(
                "(function(){var s=window.getSelection?window.getSelection().toString():'';"
                        + "return (s&&s.trim())?s:(document.body?document.body.innerText:'')})()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String v) {
                        String text = "";
                        try {
                            Object o = new JSONTokener(v == null ? "\"\"" : v).nextValue();
                            text = o == null ? "" : String.valueOf(o);
                        } catch (Exception ignored) {
                        }
                        if (text.trim().isEmpty() || "null".equals(text)) {
                            Toast.makeText(BrowserActivity.this, "Nothing to send", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        resText = text;
                        resUrl = url;
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustom();
        } else if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // keep audio playing is not needed; pause page timers
        if (web != null) web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.stopLoading();
            web.destroy();
        }
        super.onDestroy();
    }
}
