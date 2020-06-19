/*
Copyright 2015 Intel Corporation
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file 
except in compliance with the License. You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the 
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific language governing permissions 
and limitations under the License
*/

package com.intel.xdk.display;

import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ZoomButtonsController;

public abstract class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    
    SurfaceHolder mHolder;
    Camera mCamera;
    protected boolean wasCameraShown, wasSurfaceCreated, wasZoomEnabled;
    protected int width, height;
    int orientation;
    Context context;    
    ZoomButtonsController zoom = null;
    boolean debug = true;
    
    protected CameraPreview(Context context) {
        super(context);
        this.context = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        orientation = (Configuration.ORIENTATION_LANDSCAPE==context.getResources().getConfiguration().orientation)?0:90;
    }

    public static CameraPreview newInstance(Context context) {
        final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
        CameraPreview cameraPreview = new FroyoCameraPreview(context);
        return cameraPreview;
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
        if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::surfaceCreated");
        wasSurfaceCreated = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::surfaceDestroyed");
        try {
            if(mCamera!=null) {
                // Surface will be destroyed when we return, so stop the preview.
                // Because the CameraDevice object is not a shared resource, it's very
                // important to release it when the activity is paused.
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                wasSurfaceCreated = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::surfaceChanged");
        width = w;
        height = h;
        if(wasCameraShown) {
            openCamera();
            if (wasZoomEnabled) {
                enableZoom();
            }
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        System.out.println("onConfigurationChanged");
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            System.out.println("onTouchEvent: DOWN, zoom = " + (zoom != null?"ON":"OFF"));
            if (zoom != null) {
                zoom.setVisible(true);
                return true;
            }
        }
        return false;
    }
    //  Note that the camera can change if the surface changes.
    public Camera getCamera() {
        return mCamera;
    }
    public void showCamera() {
        if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::showCamera");
        wasCameraShown = true;
        openCamera();
    }

    public void enableZoom() {
        //not available pre-froyo
        return;
    }
    
    public void hideCamera() {
        if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::hideCamera");
        try {
            if(mCamera!=null){
                if (zoom != null) {
                    zoom.setVisible(false);
                    zoom = null;
                }
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                wasCameraShown = wasZoomEnabled = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void openCamera() {
        if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::openCamera");
        if(wasCameraShown && wasSurfaceCreated && mCamera==null) {
            // The Surface has been created, acquire the camera and tell it where
            // to draw.
            try {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int tempVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
                am.setStreamVolume(AudioManager.STREAM_SYSTEM,0,0);
                mCamera = Camera.open();
                am.setStreamVolume(AudioManager.STREAM_SYSTEM,tempVolume,0);
                try {
                    mCamera.setPreviewDisplay(mHolder);
                    Camera.Parameters parameters = mCamera.getParameters();

                    List<Size> sizes = parameters.getSupportedPreviewSizes();
                    Size optimalSize = getOptimalPreviewSize(sizes, width, height);
                    parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                } catch (Exception e) {
                    mCamera.release();
                    mCamera = null;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
            
    }
    
    private static class FroyoCameraPreview extends CameraPreview {
        
        public FroyoCameraPreview(Context context) {
            super(context);
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            // TODO Auto-generated method stub
            super.onConfigurationChanged(newConfig);

            System.out.println("onConfigurationChanged");

            //there is an issue when keyboard is hidden/shown - sometimes camera preview gets lost
            if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::onConfigurationChanged");
            try {
                //hide sets wasCameraShown to false, so get a copy to set it back with
                boolean wasCameraShown = this.wasCameraShown, wasZoomEnabled = this.wasZoomEnabled;
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    hideCamera();
                    this.wasCameraShown = wasCameraShown;
                    orientation = 0;
                    //openCamera();
                } else {
                    hideCamera();
                    this.wasCameraShown = wasCameraShown;
                    orientation = 90;
                    //openCamera();
                }
                this.wasZoomEnabled = wasZoomEnabled;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void openCamera() {
            if(debug) Log.i("[appMobi]", "AppMobiVersionedCameraPreview::openCamera");
            if(wasCameraShown && wasSurfaceCreated && mCamera==null) {
                // The Surface has been created, acquire the camera and tell it where
                // to draw.
                try {
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    int tempVolume = am.getStreamVolume(7);
                    am.setStreamVolume(7,0,0);
                    mCamera = Camera.open();
                    am.setStreamVolume(7,tempVolume,0);
                    try {
                        mCamera.setPreviewDisplay(mHolder);
                        Camera.Parameters parameters = mCamera.getParameters();

                        List<Size> sizes = parameters.getSupportedPreviewSizes();
                        Size optimalSize = getOptimalPreviewSize(sizes, width, height);
                        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

                        mCamera.setParameters(parameters);
                        mCamera.setDisplayOrientation(orientation);

                        mCamera.startPreview();
                    } catch (Exception e) {
                        mCamera.release();
                        mCamera = null;
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void enableZoom() {                      
            Camera.Parameters parameters = mCamera.getParameters();
            if (!parameters.isZoomSupported())
                return;
            wasZoomEnabled = true;
            zoom = new ZoomButtonsController(this);
            zoom.setAutoDismissed(true);
            zoom.setOnZoomListener(new ZoomButtonsController.OnZoomListener() {
                public void onZoom(boolean zoomin) {
                    Camera.Parameters params = mCamera.getParameters();
                    int zoomValue = params.getZoom();
                    int newValue = zoomValue;
                    final int delta = 10;
                    if (zoomin) {
                        newValue = Math.min(params.getMaxZoom(), newValue + delta);
                    } else {
                        newValue = Math.max(0, newValue - delta);
                    }
                    if (newValue != zoomValue) {
                        params.setZoom(newValue);
                        mCamera.setParameters(params);
                    }
                }
                public void onVisibilityChanged(boolean visible) {
                } 
            });
            zoom.setVisible(true);
        }
        
        
    }
}
