package id.periksa.plugins.usbcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "UsbCamera", permissions = {
        @Permission(strings = {Manifest.permission.CAMERA}, alias = UsbCameraPlugin.PERM_CAMERA),
        @Permission(strings = {Manifest.permission.READ_EXTERNAL_STORAGE}, alias = UsbCameraPlugin.PERM_READ_EXT_STORAGE),
        @Permission(strings = {Manifest.permission.WRITE_EXTERNAL_STORAGE}, alias = UsbCameraPlugin.PERM_WRITE_EXT_STORAGE)
})
public class UsbCameraPlugin extends Plugin {

    static final String PERM_CAMERA = "camera";
    static final String PERM_READ_EXT_STORAGE = "r_ext";
    static final String PERM_WRITE_EXT_STORAGE = "w_ext";

    private static final String TAG = "PluginBridgeDebug";
    private static final String[] REQUIRED_PERMISSION_ALIASES = new String[]{
            PERM_CAMERA, PERM_READ_EXT_STORAGE, PERM_WRITE_EXT_STORAGE
    };

    private final List<String> mMissPermissions = new ArrayList<>();

    @Override
    protected void handleOnStart() {
        super.handleOnStart();
    }

    @PluginMethod
    public void getPhoto(PluginCall call) {
        if (checkAndRequestPermissions(call)) {
            showIntent(call);
        }
    }

    private boolean checkAndRequestPermissions(PluginCall call) {
        mMissPermissions.clear();
        for (String permission : REQUIRED_PERMISSION_ALIASES) {
            boolean permissionGranted = getPermissionState(permission) == PermissionState.GRANTED;
            if (!permissionGranted) {
                mMissPermissions.add(permission);
            }
        }

        if (!mMissPermissions.isEmpty()) {
            requestPermissionForAliases(
                    mMissPermissions.toArray(new String[mMissPermissions.size()]),
                    call,
                    "appPermissionCallback"
            );
            return false;
        }
        return true;
    }

    @PermissionCallback
    private void appPermissionCallback(PluginCall call) {
        if (getPermissionState(PERM_CAMERA) != PermissionState.GRANTED) {
            call.reject("User denied required permissions");
            return;
        } else if (getPermissionState(PERM_WRITE_EXT_STORAGE) != PermissionState.GRANTED) {
            call.reject("User denied required permissions");
            return;
        }
        showIntent(call);
    }

    private void showIntent(PluginCall call) {
        JSObject configObject = call.getData();
        boolean saveToStorage = configObject.getBoolean("saveToStorage", false);

        Intent camIntent = new Intent(getActivity(), USBCameraActivity.class);
        camIntent.putExtra("capture_to_storage", saveToStorage); // TODO: toggle capture_to_storage.
        startActivityForResult(call, camIntent, "imageResult");
    }

    private String loadImageFromUri(Uri photoUri, int compressionRatio) {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);

        // Convert Bitmap to Base64 string.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressionRatio, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.NO_PADDING | Base64.NO_WRAP);

        return encoded;
    }

    @ActivityCallback
    private void imageResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        Bundle bundle = result.getData().getExtras();
        if (bundle != null) {
            String exitCode = (String) bundle.get("exit_code");
            int resultCode = result.getResultCode();

            String resultCodeDesc = (resultCode == -1) ? "OK" : "CANCELED";

            String b64Result = "";
            Uri photoUri = (Uri) bundle.get("img_uri");

            // Result Code is OK.
            if (resultCode == -1) {
                if (photoUri != null) {
                    b64Result = loadImageFromUri(photoUri, 85);
                    b64Result = "data:image/jpeg;base64," + b64Result;
                }
            }

            JSObject plResult = new JSObject();
            plResult.put("status_code", resultCode);
            plResult.put("status_code_s", resultCodeDesc);
            plResult.put("exit_code", exitCode);

            JSObject dataResult = new JSObject();
            if (b64Result.length() > 0) {
                dataResult.put("dataURL", b64Result);
                dataResult.put("fileURI", photoUri);
                plResult.put("data", dataResult);
            }

            call.resolve(plResult);
        }
    }
}
