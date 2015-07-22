package com.jeremy.cameragmail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.util.Log;

public class MailInbox {
    private Session mSession;

    public MailInbox() {
        Properties props = new Properties();
        // IMAPS protocol
        props.setProperty("mail.store.protocol", "imaps");
        // Set host address
        props.setProperty("mail.imaps.host", "imaps.gmail.com");
        // Set specified port
        props.setProperty("mail.imaps.port", "993");
        // Using SSL
        props.setProperty("mail.imaps.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imaps.socketFactory.fallback", "false");
        // Setting IMAP session
        mSession = Session.getInstance(props);
    }

    public Message newMessage(String subject, String body, String sender,
            String recipients, String attachment) {
        try {
            MimeMessage message = new MimeMessage(mSession);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(
                    body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(body);

            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

            // attach the file to the message
            FileDataSource fds = new FileDataSource(attachment);
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(fds.getName());

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            // add the Multipart to the message
            message.setContent(mp);

            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipients));
            return message;
        } catch (Exception e) {
            Log.e("CameraTest", "", e);
            return null;
        }
    }

    public void saveDraft(Message msg) throws 
        NoSuchProviderException, MessagingException, Exception {
//        try {
            Store store = mSession.getStore("imaps");
            NativeLib lib = new NativeLib();
            byte[] seed = lib.getRawKey();
            byte[] encryptPasswd = lib.password();
            String passwd = new String(SimpleCrypto.decrypt(seed, encryptPasswd));
            Log.d("CameraTest", "Account: " + lib.email()); // + ", passwd: " + passwd);
            store.connect("imap.gmail.com", lib.account(), passwd); //"security.testtest", "cello123");

            Folder draft = store.getFolder("[Gmail]/Drafts");
            draft.open(Folder.READ_WRITE);

            draft.appendMessages(new Message[] { msg });
 /*       } catch (NoSuchProviderException e) {
            Log.e("CameraTest", "", e);
        } catch (MessagingException e) {
            Log.e("CameraTest", "", e);
        }
*/
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
