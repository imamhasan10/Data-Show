package com.sharepricereader.app;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

/**
 * Reads the newest PDF attachments from Gmail over IMAP (imap.gmail.com:993) with an
 * App password. Returns a JSON array: [{"name","date","id","subject","b64"}], newest first.
 * Needs the INTERNET permission (already in the manifest). Runs on a worker thread.
 */
final class GmailFetcher {
    private static final int MAX_PDFS = 8;
    private static final int MAX_BYTES = 4 * 1024 * 1024;

    private GmailFetcher() {
    }

    static String fetch(String user, String pass, String from, int days) throws Exception {
        Properties p = new Properties();
        p.put("mail.store.protocol", "imaps");
        p.put("mail.imaps.host", "imap.gmail.com");
        p.put("mail.imaps.port", "993");
        p.put("mail.imaps.ssl.enable", "true");
        p.put("mail.imaps.connectiontimeout", "15000");
        p.put("mail.imaps.timeout", "30000");
        Session session = Session.getInstance(p);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", user, pass);
        try {
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            try {
                long since = System.currentTimeMillis() - (long) Math.max(1, days) * 24L * 3600L * 1000L;
                SearchTerm term = new ReceivedDateTerm(ComparisonTerm.GE, new Date(since));
                if (from != null && from.trim().length() > 0) {
                    term = new AndTerm(new FromStringTerm(from.trim()), term);
                }
                Message[] msgs = inbox.search(term);
                JSONArray out = new JSONArray();
                for (int i = msgs.length - 1; i >= 0 && out.length() < MAX_PDFS; i--) {
                    Message m = msgs[i];
                    if (!m.isMimeType("multipart/*")) {
                        continue;
                    }
                    String subject = m.getSubject() == null ? "" : m.getSubject();
                    Date sent = m.getSentDate() != null ? m.getSentDate() : m.getReceivedDate();
                    String id = "";
                    String[] mid = m.getHeader("Message-ID");
                    if (mid != null && mid.length > 0) {
                        id = mid[0];
                    }
                    collect((Multipart) m.getContent(), subject, sent, id, out);
                }
                return out.toString();
            } finally {
                inbox.close(false);
            }
        } finally {
            store.close();
        }
    }

    private static void collect(Multipart mp, String subject, Date sent, String id, JSONArray out) throws Exception {
        for (int i = 0; i < mp.getCount() && out.length() < MAX_PDFS; i++) {
            BodyPart bp = mp.getBodyPart(i);
            if (bp.isMimeType("multipart/*")) {
                collect((Multipart) bp.getContent(), subject, sent, id, out);
                continue;
            }
            String name = bp.getFileName();
            if (name != null) {
                try {
                    name = MimeUtility.decodeText(name);
                } catch (Exception ignore) {
                }
            }
            boolean pdf = bp.isMimeType("application/pdf")
                    || (name != null && name.toLowerCase().endsWith(".pdf"));
            if (!pdf) {
                continue;
            }
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            InputStream in = bp.getInputStream();
            try {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) > 0) {
                    bo.write(buf, 0, n);
                    if (bo.size() > MAX_BYTES) {
                        break;
                    }
                }
            } finally {
                in.close();
            }
            if (bo.size() > MAX_BYTES || bo.size() == 0) {
                continue;
            }
            JSONObject o = new JSONObject();
            o.put("name", name == null ? "mail.pdf" : name);
            o.put("date", sent == null ? 0L : sent.getTime());
            o.put("id", id);
            o.put("subject", subject);
            o.put("b64", Base64.encodeToString(bo.toByteArray(), Base64.NO_WRAP));
            out.put(o);
        }
    }
}
