package id.periksa.plugins.usbcamera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.serenegiant.usb_libuvccamera.CameraDialog;
import com.serenegiant.usb_libuvccamera.LibUVCCameraUSBMonitor;
import com.serenegiant.usb_libuvccamera.LibUVCCameraUSBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb_libuvccamera.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static com.serenegiant.utils.FileUtils.getDateTimeString;


public class USBCameraActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent {
    private static final String TAG = "CamActivityDebug";
    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 640;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 480;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */
    private static final int PREVIEW_MODE = 1;

    private static final String TEMP_FILE_NAME = "camera_capture_result";


    private LibUVCCameraUSBMonitor mUSBMonitor;
    private UVCCameraHandler mCameraHandler;
    private CameraViewInterface mUVCCameraView;
    private ImageButton mBtnCapture;
    private TextView mBtnCancel;
    private AlertDialog mDialog;

    private Intent intentResult;

    private boolean isCaptureToStorage;

    // Lifecycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbcamera);

        final View view = findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface) view;

        mBtnCapture = findViewById(R.id.btn_capture);
        mBtnCapture.setOnClickListener(mOnCaptureClickListener);
        mBtnCapture.setVisibility(View.INVISIBLE);

        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(mOnCancelClickListener);
        mBtnCancel.setVisibility(View.INVISIBLE);

        mUSBMonitor = new LibUVCCameraUSBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView,
                1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);

        isCaptureToStorage = getIntent().getExtras().getBoolean("capture_to_storage", false);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        checkPermissionCamera();
        mUSBMonitor.register();
        if (mUVCCameraView != null)
            mUVCCameraView.onResume();

        intentResult = new Intent();

        Log.d(TAG, "***** Device count: " + mUSBMonitor.getDeviceCount());

        if (mUSBMonitor.getDeviceCount() == 0) {
            intentResult.putExtra("exit_code", "exit_no_device");
            setResult(RESULT_CANCELED, intentResult);
            finish();
        }
    }

    @Override
    protected void onStop() {
        mCameraHandler.close();
        if (mUVCCameraView != null)
            mUVCCameraView.onPause();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
        super.onDestroy();
    }

//    Device Connection Methods

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            Log.d(TAG, "Device Attached");
            List<UsbDevice> devices = mUSBMonitor.getDeviceList();
            if (devices.size() == 1) {
                UsbDevice item = devices.get(0);
                mUSBMonitor.requestPermission(item);
            } else {
                CameraDialog.showDialog(USBCameraActivity.this);
            }
        }

        @Override
        public void onDettach(UsbDevice device) {
            Log.d(TAG, "Device Detached");
            mCameraHandler.close();
        }

        @Override
        public void onConnect(UsbDevice device, LibUVCCameraUSBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            Log.d(TAG, "Connecting to Device");
            showToast("Connecting to Device", Toast.LENGTH_SHORT);
            mCameraHandler.open(ctrlBlock);
            startPreview();
        }

        @Override
        public void onDisconnect(UsbDevice device, LibUVCCameraUSBMonitor.UsbControlBlock ctrlBlock) {
            Log.d(TAG, "Disconnecting device");
            exitCancelWithCode("device_disconnected");
        }

        @Override
        public void onCancel(UsbDevice device) {
            Log.d(TAG, "onCancel called");
            exitCancelWithCode("user_canceled");
        }
    };

    @Override
    public LibUVCCameraUSBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        Log.d(TAG, "onDialogResult canceled: " + canceled);
        if (canceled) {
            exitCancelWithCode("user_canceled");
        }
    }


    // Utility and other methods

    private void showToast(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }

    private void exitCancelWithCode(String code) {
        intentResult.putExtra("exit_code", code);
        setResult(RESULT_CANCELED, intentResult);
        finish();
    }

    private void startPreview() {
        final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
        mCameraHandler.startPreview(new Surface(st));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnCapture.setVisibility(View.VISIBLE);
                mBtnCancel.setVisibility(View.VISIBLE);
            }
        });
    }

    private File saveImgToCache(Bitmap bitmap, String fileName) {
       File dir = new File(getCacheDir(), "USBCamera");
       dir.mkdirs();

       try {
           if (dir.canWrite()) {
               File cacheFile = new File(dir, fileName + ".png");
               BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));
               try {
                   bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
                   bos.flush();
               } finally {
                   bos.close();
               }
               return cacheFile;
           }
           return null;
       } catch (IOException ex) {
           ex.printStackTrace();
       }
       return null;
    }

    private File captureCameraImage(boolean saveToStorage) {
        TextureView cameraTextureView = (TextureView) mUVCCameraView;
        Bitmap bitmap = cameraTextureView.getBitmap();
        String fileName = getDateTimeString() + ".png";

        File cacheFile = saveImgToCache(bitmap, TEMP_FILE_NAME);

        if (!saveToStorage) {
            return cacheFile;
        }

        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/ExternalCamera");

        final ContentResolver resolver = getContentResolver();
        Uri uri = null;

        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);

            if (uri == null)
                throw new IOException("Failed to create new MediaStore record.");

            try (final OutputStream stream = resolver.openOutputStream(uri)) {
                if (stream == null)
                    throw new IOException("Failed to open output stream.");

                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
                    throw new IOException("Failed to save bitmap.");
            }
        } catch (IOException e) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }
        }
        return cacheFile;
    }

    private final View.OnClickListener mOnCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCameraHandler.close();
            exitCancelWithCode("user_canceled");
        }
    };

    private final View.OnClickListener mOnCaptureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCameraHandler.isOpened()) {
                File imgResult = captureCameraImage(isCaptureToStorage);
                Uri fileUri = Uri.fromFile(imgResult);

                mCameraHandler.close();

                intentResult.putExtra("exit_code", "success");
                intentResult.putExtra("img_file", imgResult);
                intentResult.putExtra("img_uri", fileUri);
                setResult(RESULT_OK, intentResult);
                finish();
            }
        }
    };
}