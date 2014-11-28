/*
 * Copyright (C) 2010 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.scanner.camera;

import java.util.Collection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.scanner.data.Preferences;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
public final class CameraConfigurationManager {

    //private static final String TAG = "CameraConfiguration";
    private static final int MIN_PREVIEW_PIXELS = 320 * 240; // small screen
    private static final int MAX_PREVIEW_PIXELS = 800 * 480; // large/HD screen

    private final Context context;
    private Point screenResolution;
    private Point cameraResolution;

    public CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenResolution = getScreenSize(manager);
        //Log.i(TAG, "Screen resolution: " + screenResolution);
        
        // see: http://stackoverflow.com/a/16252917/1993220 but add this too to cope with portrait preview size
        // other changes are in CameraManager and DecodeHandler
        boolean isPortrait = screenResolution.x < screenResolution.y;
        cameraResolution = findBestPreviewSizeValue(parameters, screenResolution, isPortrait);
        if (isPortrait) {
            int tmp = cameraResolution.x;
            cameraResolution.x = cameraResolution.y;
            cameraResolution.y = tmp;
        }
        //Log.i(TAG, "Camera resolution: " + cameraResolution);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void setDesiredCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            //Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        initializeTorch(parameters, prefs);
        String focusMode = findSettableValue(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_MACRO);
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        camera.setParameters(parameters);
        
        int cameraOrientation = 90; // default before v9
        boolean cameraFrontFacing = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);
            cameraOrientation = cameraInfo.orientation;
            cameraFrontFacing = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        int screenRotation = getScreenRotationDegrees((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        int displayOrientation = getPreviewOrientationDegrees(screenRotation, cameraOrientation, cameraFrontFacing);
        camera.setDisplayOrientation(displayOrientation);
        //Log.i(TAG, "Camera orientation: " + displayOrientation);
    }

    public Point getCameraResolution() {
        return cameraResolution;
    }

    public Point getScreenResolution() {
        return screenResolution;
    }

    void setTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        doSetTorch(parameters, newSetting);
        camera.setParameters(parameters);
    }

    private static void initializeTorch(Camera.Parameters parameters, SharedPreferences prefs) {
        doSetTorch(parameters, Preferences.KEY_FRONT_LIGHT);
    }

    private static void doSetTorch(Camera.Parameters parameters, boolean newSetting) {
        String flashMode;
        if (newSetting) {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON);
        } else {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_OFF);
        }
        if (flashMode != null) {
            parameters.setFlashMode(flashMode);
        }
    }
    
    /**
     * Set the SurfaceHolder's type to SURFACE_TYPE_PUSH_BUFFERS, but only in API < 11 (after this it is set
     * automatically by the system when needed)
     * 
     * @param holder
     */
    @SuppressWarnings("deprecation")
    public static void setPushBuffers(SurfaceHolder holder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setScreenOrientationFixed(Activity activity, boolean orientationFixed) {
        if (orientationFixed) {
            WindowManager windowManager = activity.getWindowManager();
            boolean naturallyPortrait = getNaturalScreenOrientation(windowManager) == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            int reversePortrait = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            int reverseLandscape = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                reversePortrait = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                reverseLandscape = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
            switch (windowManager.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    activity.setRequestedOrientation(naturallyPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case Surface.ROTATION_90:
                    activity.setRequestedOrientation(naturallyPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            : reversePortrait);
                    break;
                case Surface.ROTATION_180:
                    activity.setRequestedOrientation(naturallyPortrait ? reversePortrait : reverseLandscape);
                    break;
                case Surface.ROTATION_270:
                    activity.setRequestedOrientation(naturallyPortrait ? reverseLandscape
                            : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
            }
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }
    
    /**
     * Get the "natural" screen orientation - i.e. the orientation in which this device is designed to be used most
     * often.
     * 
     * @param windowManager
     * @return either ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE or ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
     */
    public static int getNaturalScreenOrientation(WindowManager windowManager) {
        Display display = windowManager.getDefaultDisplay();
        Point screenSize = getScreenSize(windowManager);
        int width = 0;
        int height = 0;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                width = screenSize.x;
                height = screenSize.y;
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                width = screenSize.y;
                height = screenSize.x;
                break;
            default:
                break;
        }

        if (width > height) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }
    
    /**
     * Get the current rotation of the screen, either 0, 90, 180 or 270 degrees
     * 
     * @param windowManager
     * @return
     */
    private static int getScreenRotationDegrees(WindowManager windowManager) {
        int degrees = 0;
        switch (windowManager.getDefaultDisplay().getRotation()) {
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
        return degrees;
    }
    
    // see: http://developer.android.com/reference/android/hardware/Camera.Parameters.html#setRotation(int)
    private static int getPreviewOrientationDegrees(int screenOrientationDegrees, int cameraOrientationDegrees,
            boolean usingFrontCamera) {
        int previewOrientationDegrees;
        if (usingFrontCamera) { // compensate for the mirror of the front camera
            previewOrientationDegrees = (cameraOrientationDegrees + screenOrientationDegrees) % 360;
            previewOrientationDegrees = (360 - previewOrientationDegrees) % 360;
        } else { // back-facing
            previewOrientationDegrees = (cameraOrientationDegrees - screenOrientationDegrees + 360) % 360;
        }
        return previewOrientationDegrees;
    }
    
    @SuppressWarnings("deprecation")
    private static Point getScreenSize(WindowManager windowManager) {
        Point screenSize = new Point();
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            getPointScreenSize(display, screenSize);
        } else {
            screenSize.set(display.getWidth(), display.getHeight());
        }
        return screenSize;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private static void getPointScreenSize(Display display, Point screenSize) {
        display.getSize(screenSize);
    }

    private static Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution, boolean portrait) {
        Point bestSize = null;
        int diff = Integer.MAX_VALUE;
        for (Camera.Size supportedPreviewSize : parameters.getSupportedPreviewSizes()) {
            int pixels = supportedPreviewSize.height * supportedPreviewSize.width;
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                continue;
            }
            int supportedWidth = portrait ? supportedPreviewSize.height : supportedPreviewSize.width;
            int supportedHeight = portrait ? supportedPreviewSize.width : supportedPreviewSize.height;
            int newDiff = Math.abs(screenResolution.x * supportedHeight - supportedWidth * screenResolution.y);
            if (newDiff == 0) {
                bestSize = new Point(supportedWidth, supportedHeight);
                break;
            }
            if (newDiff < diff) {
                bestSize = new Point(supportedWidth, supportedHeight);
                diff = newDiff;
            }
        }
        if (bestSize == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            bestSize = new Point(defaultSize.width, defaultSize.height);
        }
        return bestSize;
    }

    private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
        //Log.i(TAG, "Supported values: " + supportedValues);
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        //Log.i(TAG, "Settable value: " + result);
        return result;
    }

}
