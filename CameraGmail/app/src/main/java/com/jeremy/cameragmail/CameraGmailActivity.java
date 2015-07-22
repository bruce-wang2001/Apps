package com.jeremy.cameragmail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CameraGmailActivity extends Activity implements
        Camera.PictureCallback, View.OnClickListener, Camera.AutoFocusCallback {

    private static final int MEDIA_TYPE_IMAGE = 0;
    public static String TAG = "MyCameraGmail";
    
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private Button mCaptureButton;
    private Button mSettingButton;
    private TextView mResolutionText;
    private boolean mTakingPic = false;

    private final int EXPIRE_MINUTES = 120;
    private final String RESOLUTION_WIDTH_KEY = "resolution_width";
    private final String RESOLUTION_HEIGHT_KEY = "resolution_height";

    private static class SimpleListFragment extends ListFragment {

        List<Camera.Size> mSizes;
        CameraGmailActivity mCallback;
        
        public SimpleListFragment(List<Camera.Size> sizes, CameraGmailActivity callback) {
            mSizes = sizes;
            mCallback = callback;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Log.d(TAG, "select " + mSizes.get((int)id));
            mCallback.onResolutionSelected(mSizes.get((int)id));
            getActivity().getFragmentManager().beginTransaction().remove(this).commit();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            ArrayList<String> resolutions = new ArrayList<String>();
            for (Camera.Size size : mSizes) {
                Log.d(TAG, "Available resolution: " + size.width + " " + size.height);
                String str = size.width + "x" + size.height;
                resolutions.add(str);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    inflater.getContext(), android.R.layout.simple_list_item_1,
                    resolutions);
            setListAdapter(adapter);
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    void onResolutionSelected(Camera.Size size) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(RESOLUTION_WIDTH_KEY, size.width);
        editor.putInt(RESOLUTION_HEIGHT_KEY, size.height);
        editor.commit();
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            setCameraResolution(params);
        }
    }

    private void setCameraResolution(Camera.Parameters params) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int width = prefs.getInt(RESOLUTION_WIDTH_KEY, -1);
        int height = prefs.getInt(RESOLUTION_HEIGHT_KEY, -1);
        String resolutionText = "";
        if (width != -1 && height != -1) {
        } else {
            // set to max resolution
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            int maxResolutionX = 0, maxResolutionY = 0;
            for (Camera.Size size : sizes) {
                if (size.width * size.height > maxResolutionX * maxResolutionY) {
                    maxResolutionX = size.width;
                    maxResolutionY = size.height;
                }
            }
            width = maxResolutionX;
            height = maxResolutionY;
        }
        Log.d(TAG, "use resolution " + width + "x" + height);
        resolutionText = width + "x" + height;
        mResolutionText.setText(resolutionText);
        params.setPictureSize(width, height);
        //mCamera.setParameters(params);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            // attempt to get a Camera instance
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            // get Camera parameters
            Camera.Parameters params = c.getParameters();
            // set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            if (params.getMaxNumMeteringAreas() > 0) { // check that metering
                                                       // areas are supported
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

                // specify an area in center of image
                Rect areaRect1 = new Rect(-100, -100, 100, 100);
                // set weight to 60%
                meteringAreas.add(new Camera.Area(areaRect1, 600));
                // specify an area in upper right of image
                Rect areaRect2 = new Rect(800, -1000, 1000, -800);
                // set weight to 40%
                meteringAreas.add(new Camera.Area(areaRect2, 400));
                params.setMeteringAreas(meteringAreas);
                Log.d(TAG,
                        "max metering areas=" + params.getMaxNumMeteringAreas());
            }
            setCameraResolution(params);
            // set Camera parameters
            c.setParameters(params);

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    // Camera.PictureCallback
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        NativeLib lib = new NativeLib();
        byte[] seed = lib.getRawKey();
        // File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        FileOutputStream fos = getOutputMediaFileStream(MEDIA_TYPE_IMAGE);
        if (fos == null) {
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            // FileOutputStream fos = new FileOutputStream(pictureFile);
            byte[] content = SimpleCrypto.encrypt(seed, data);
            fos.write(content);
            fos.close();
            Toast toast = Toast.makeText(this, getString(R.string.save_pic_done), Toast.LENGTH_SHORT);
            toast.show();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Log.d(TAG, "send intent");
            Intent intent = new Intent(this, SenderIntentService.class);
            startService(intent);
        } catch (Exception e) {
            Log.e("CameraTest", "", e);
        }

        // start to taking other picture
        camera.startPreview();
    }

    @Override
    public void onClick(View v) {
        if (v == mCaptureButton) {
            Log.d(TAG, "capture button click");
            // autofocus
            mCaptureButton.setClickable(false);
            mTakingPic = true;
            mCamera.autoFocus(this);
        } else if (v == mSettingButton) {
            Camera.Parameters params = mCamera.getParameters();
            final List<Camera.Size> sizes = params.getSupportedPictureSizes();
            FragmentManager fm = getFragmentManager();

            if (fm.findFragmentById(android.R.id.content) == null) {
                SimpleListFragment list = new SimpleListFragment(sizes, this);
                fm.beginTransaction().add(android.R.id.content, list).commit();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d(TAG, "Start application");

        Intent intent = new Intent(this, SenderIntentService.class);
        startService(intent);

        // Add a listener to the Capture button
        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(this);
        mSettingButton = (Button)findViewById(R.id.button_setting);
        mSettingButton.setOnClickListener(this);
        mResolutionText = (TextView)findViewById(R.id.resolution_text);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        CameraGmailActivity.setCameraDisplayOrientation(this,
                Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                TAG);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        Log.d(TAG, "File: " + mediaFile.getPath());
        return mediaFile;
    }

    FileOutputStream getOutputMediaFileStream(int type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";

        if (type == MEDIA_TYPE_IMAGE) {
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                File f = getFilesDir();
                Log.d(TAG, "save to " + f.getAbsolutePath() + "/" + fileName);
                return fos;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCameraInstance();
            mPreview.resetCamera(mCamera);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder(); // if you are using MediaRecorder, release it
                                // first
        releaseCamera(); // release the camera immediately on pause event
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset(); // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break;
        case Surface.ROTATION_90:
            degrees = 90;
            break;
        case Surface.ROTATION_180:
            degrees = 180;
            break;
        case Surface.ROTATION_270:
            degrees = 270;
            break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void touchFocus(Rect touchRect) {
        // tell camera to autofucs
        mCamera.autoFocus(this);
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        // get an image from the camera
        if (mTakingPic) {
            mCamera.takePicture(null, null, this);
            mCaptureButton.setClickable(true);
            mTakingPic = false;
        }
    }

}