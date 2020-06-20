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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

@SuppressWarnings("deprecation")
public class Display extends CordovaPlugin {
    public static boolean debug = true;    
    private float forcedscale = -1;
    private int checkCount = 0;
    private Activity activity = null;
    public CameraPreview arView;
     
    public Display(){
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        //get convenience reference to activity
        activity = cordova.getActivity();
        
        // activity.runOnUiThread(new Runnable() {
        //     public void run() {
        //         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //             try {
        //                 Method m = WebView.class.getMethod("setWebContentsDebuggingEnabled", boolean.class);
        //                 m.invoke(WebView.class, true);
        //             } catch (Exception e) {
        //                 // TODO Auto-generated catch block
        //                 e.printStackTrace();
        //             }
        //         }
        //     }
        // });
        
        
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("")) {
//            JSONObject r = new JSONObject();
//            r.put("cookies", new JSONObject(getCookies()));
//            r.put("mediacache", new JSONArray(getMediaCache()));
//            r.put("mediacache_dir", cachedMediaDirectory.getAbsolutePath());
//            callbackContext.success(r);
        }
        else if (action.equals("startAR")) {
            startAR();
        }
        else if (action.equals("stopAR")) {
            stopAR();
        }
        else {
            return false;
        }

        return true;
    }

    
    
    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------      
    
    //defer
    @JavascriptInterface
    public void startAR() {     
        arView = CameraPreview.newInstance(activity.getApplicationContext());
        arView.setVisibility(View.INVISIBLE);
        
        arView.height = 100;
        arView.width = 100;
        
        //no way to get current background color?
        arView.setBackgroundColor(Color.TRANSPARENT);
        
        SurfaceHolder sfhTrackHolder = arView.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
        sfhTrackHolder.setKeepScreenOn(true);
        sfhTrackHolder.setFixedSize(100, 100);
        
        activity.runOnUiThread(new Runnable() {
            public void run() {     
                
                //activity.addContentView(arView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 0.0F));
                //activity.addContentView(arView, new LinearLayout.LayoutParams(400, 500, 0.0F));
                ViewGroup rootView = (ViewGroup)webView.getParent().getParent();
                rootView.addView(arView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
                rootView.bringChildToFront((View)webView.getParent());
                
                arView.setVisibility(View.VISIBLE);
                arView.showCamera();
                webView.bringToFront();
                webView.setBackgroundColor(0x00000000);
                LinearLayout ll = (LinearLayout)webView.getParent();
                ll.setBackgroundColor(0x00000000);

                View activityView = activity.getCurrentFocus();
            }
        });
    }
    
    @JavascriptInterface
    public void stopAR() {
        activity.runOnUiThread(new Runnable() {
            public void run() {     
                arView.setVisibility(View.INVISIBLE);
                arView.hideCamera();
           }
        });
    }
    
    //start hacks for HTC Incredible
//    @JavascriptInterface
//    public void forceScale(final float scale) {
//        checkCount = 3;//check 3 times then bail (this is for the picture listener so it doesnt run forever)
//        forceScaleInternal(scale);
//        checkScale();
//        //webview.addPictureListener();
//    }
//    
//    Method setScaleWithoutCheck, setNewZoomScale/*Gingerbread*/, setScaleTo/*Froyo*/;
//    private void forceScaleInternal(/*final*/ float scale) {
//        forcedscale = scale;
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                webview.setInitialScale(Math.round((forcedscale*100)));
//            }
//        });
//        
//        try {
//            Method fnGetMinZoomScale = activity.remoteWebView.getClass().getMethod("fnGetMinZoomScale");
//            if (fnGetMinZoomScale != null) {
//                Float minz = (Float) fnGetMinZoomScale.invoke(activity.remoteWebView, (Object[]) null);
//                if(scale < minz) {
//                    scale = minz.floatValue();
//                }
//            }
//        } catch (NoSuchMethodException e) {
//        } catch (IllegalAccessException e) {
//        } catch (InvocationTargetException e) {
//        }       
//        
//        try {
//            if(setScaleWithoutCheck == null) setScaleWithoutCheck = webview.getClass().getMethod("setScaleWithoutCheck", boolean.class);
//            if (setScaleWithoutCheck != null) {
//                setScaleWithoutCheck.invoke(webview, true);
//            }
//        } catch (NoSuchMethodException e) {
//        } catch (IllegalAccessException e) {
//        } catch (InvocationTargetException e) {
//        }
//        try {
//            if(setScaleTo == null) setScaleTo = webview.getClass().getMethod("setScaleTo", float.class, boolean.class, boolean.class);
//            if (setScaleTo != null) {
//                setScaleTo.invoke(webview, scale, true, true);
//                if(Debug.isDebuggerConnected()) Log.i("[appMobi2]", "setScaleTo called with " + scale);
//                //if forceScale succeeded, hold onto scale
//                AppMobiDisplay.this.forcedscale = scale;
//            } else{
//                if(Debug.isDebuggerConnected()) Log.i("[appMobi2]", "setScaleTo not called");
//            }
//        } catch (NoSuchMethodException e) {
//        } catch (IllegalAccessException e) {
//        } catch (InvocationTargetException e) {
//        }
//        try {
//            if(setNewZoomScale == null) setNewZoomScale = webview.getClass().getMethod("setNewZoomScale", float.class, boolean.class, boolean.class);
//            if (setNewZoomScale != null) {
//                setNewZoomScale.invoke(webview, scale, true, true);
//                if(Debug.isDebuggerConnected()) Log.i("[appMobi2]", "setNewZoomScale called with " + scale);
//                //if forceScale succeeded, hold onto scale
//                AppMobiDisplay.this.forcedscale = scale;
//            } else{
//                if(Debug.isDebuggerConnected()) Log.i("[appMobi2]", "setNewZoomScale not called");
//            }
//        } catch (NoSuchMethodException e) {
//        } catch (IllegalAccessException e) {
//        } catch (InvocationTargetException e) {
//        }
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                webview.invalidate();
//            }
//        });
//    }
//
//    public void checkScale() {
//        checkCount--;
//        
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                }
//                
//                float newScale = activity.appView.getScale();
//                if(Debug.isDebuggerConnected()) Log.i("[appMobi2]", "checkScale called with " + newScale +", requestedScale is " + forcedscale);
//                if(forcedscale!=-1 && newScale!=forcedscale) {
//                    forceScaleInternal(forcedscale);
//                    if(Debug.isDebuggerConnected()) Log.i("[appMobi2]", "checkScale calling forceScaleInternal with " + forcedscale);
//                } else if(newScale==forcedscale){
//                    checkCount = 0;
//                    webview.removePictureListener();
//                }
//                
//                if(Debug.isDebuggerConnected()) Log.i("[appMobi2]", "checkCount:"+checkCount);
//                if(checkCount<1) {
//                    webview.removePictureListener();
//                }
//            }
//        });     
//    }
    //end hacks for HTC Incredible
}
