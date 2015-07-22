package com.jeremy.cameragmail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.Message;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SenderIntentService extends IntentService
{
    private File mStartFolder;
    final String mTag = "CameraTest:SenderIntentService";
    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public SenderIntentService() {
        super("SenderIntentService");
        Log.d(mTag, "Create service " + mStartFolder);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStartFolder = getFilesDir();
        Log.d(mTag, "start here " + mStartFolder.getAbsolutePath());
        //new File("/mnt/sdcard/Pictures/MyCamera");

        if (!mStartFolder.exists())
            if (!mStartFolder.mkdir())
                Log.e(mTag, "create startFolder fail");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns,
     * IntentService stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        int retry_max = 3;
        NativeLib lib = new NativeLib();

        Log.d(mTag, "handle new inten!t");
        for (int i = 0; i < retry_max; i++) {
            File[] files = mStartFolder.listFiles();
            MailInbox inbox = null;
            
            inbox = new MailInbox();
            
            if (files == null)
                break;
            for (File f : files) {
                if (f.isFile()) {
                    String fname = f.getPath();
                    Log.d(mTag, "Start sending " + fname);
                    try {
                        if (fname.endsWith(".jpg")) {
                            /*
                            byte[] content = getBytesFromFile(f);
                            byte[] seed = new byte[] { -43, 76, -116, -11, 41,
                                    14, -33, -13, -82, -103, 35, -96, -63, -11,
                                    -22, -128 };
                                    
                            byte[] seed = lib.getRawKey();
                            byte[] sentContent = SimpleCrypto.encrypt(seed,
                                    content);
                            f.delete();
                            fname = fname.replaceAll("jpg$", "JPG");
                            Log.d(mTag, "Saving to " + fname);
                            writeBytesToFile(fname, sentContent);
                            */
                        }
                        // save to inbox's draft // show file name in title
                        String mail = lib.email();
                        Message msg = null;
                        msg = inbox.newMessage(f.getName(), "this is body",
                                mail, // "security.testtest@gmail.com",
                                mail, fname);
                        // "security.testtest@gmail.com", fname);
                        inbox.saveDraft(msg);
                        Log.d(mTag, "Finish uploading draft");
                        new File(fname).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                        inbox = new MailInbox();
                    }
                }
            }
        }
        Log.d(mTag, "finish handle intent");
    }

    public static void writeBytesToFile(String filePath, byte[] content)
            throws IOException {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filePath));
            bos.write(content);
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }

    public byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = null;
        byte[] bytes = null;

        try {
            is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                try {
                    throw new IOException("Could not completely read file "
                            + file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return bytes;
    }
}
