package com.stanleyidesis.cordova.plugin;

import java.util.*;

// The native Toast API
import android.widget.Toast;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//LEGIC packages
import com.legic.mobile.sdk.api.LegicMobileSdkManager;
import com.legic.mobile.sdk.api.LegicMobileSdkManagerFactory;
import com.legic.mobile.sdk.api.exception.LegicMobileSdkException;
import com.legic.mobile.sdk.api.listener.LegicMobileSdkEventListener;
import com.legic.mobile.sdk.api.listener.LegicMobileSdkPasswordEventListener;
import com.legic.mobile.sdk.api.listener.LegicMobileSdkRegistrationEventListener;
import com.legic.mobile.sdk.api.listener.LegicMobileSdkSynchronizeEventListener;
import com.legic.mobile.sdk.api.listener.LegicNeonFileEventListener;
import com.legic.mobile.sdk.api.listener.LegicReaderEventListener;
import com.legic.mobile.sdk.api.types.LcMessageMode;
import com.legic.mobile.sdk.api.types.LegicMobileSdkErrorReason;
import com.legic.mobile.sdk.api.types.LegicMobileSdkFileAddressingMode;
import com.legic.mobile.sdk.api.types.LegicMobileSdkStatus;
import com.legic.mobile.sdk.api.types.LegicNeonFile;
import com.legic.mobile.sdk.api.types.RfInterface;
import com.legic.mobile.sdk.api.types.RfInterfaceState;
import com.legic.mobile.sdk.api.types.LcConfirmationMethod;
import com.legic.mobile.sdk.api.types.LegicMobileSdkPushType;

public class ToastyPlugin extends CordovaPlugin implements LegicMobileSdkRegistrationEventListener, LegicMobileSdkSynchronizeEventListener {
  private LegicMobileSdkManager mManager;
  private CallbackContext currentContext;

  private static final String DURATION_LONG = "long";

  public void initialize() {      
      try {
        mManager = LegicMobileSdkManagerFactory.getInstance(cordova.getActivity().getApplicationContext());

        //registerListeners();
  
        if (!mManager.isStarted()) {
            mManager.start(50122131, 
            "MobCloudTraQTechUser", 
            "63dA+Xhc4bJImYlqf/p17BwtcgVe+KCzk8kZczuJ5e0=",
            "https://api.legicconnect.com/public");
        }
  
        mManager.setLcProjectAddressingMode(true);
        
        if (mManager.isRegisteredToBackend()) {
            if (mManager.isRfInterfaceSupported(RfInterface.BLE) && !mManager.isRfInterfaceActive(RfInterface.BLE)) {
                mManager.activateRfInterface(RfInterface.BLE);
            }
        }
        //add listeners
        mManager.registerForSynchronizeEvents(this);
        mManager.registerForRegistrationEvents(this);

        this.showToast("INITIALIZED LEGIC SDK");
        this.currentContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
      } catch (Exception e) {
        this.showToast(e.getMessage());
        this.currentContext.error("Error encountered: " + e.getMessage());
      }
      this.currentContext = null;
  }

  public void initiateRegistration(String deviceId) {
    List<RfInterface> interfaces = new ArrayList<>();
    try {
      boolean ble = mManager.isRfInterfaceSupported(RfInterface.BLE);
      if (ble) {
          interfaces.add(RfInterface.BLE);
      }
    } catch (LegicMobileSdkException e) {
      System.out.println(e.getLocalizedMessage());
    }
    mManager.initiateRegistration(
              deviceId,
              interfaces,
              LcConfirmationMethod.NONE,
              null,
              LegicMobileSdkPushType.GCM);
  }

  public void handleSDKStatus(LegicMobileSdkStatus status) {
    if (status.isSuccess() && this.currentContext != null) {
      this.currentContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
      this.currentContext = null;
    } else {
      this.handleSdkError(status);
    }
  }

  public void showToast(String text) {
    Toast toast = Toast.makeText(cordova.getActivity(), text, Toast.LENGTH_SHORT);
    // Display toast
    toast.show();
  }

  //REGISTRATION EVENT LISTENERS
  public void backendRegistrationStartDoneEvent(LegicMobileSdkStatus status) {
    this.handleSDKStatus(status);
    this.showToast("first registration step complete");
  }
  public void backendRegistrationFinishedDoneEvent(LegicMobileSdkStatus status) {
    this.handleSDKStatus(status);
    this.showToast("backend registration complete!");
  }
  public void backendUnregisterDoneEvent(LegicMobileSdkStatus status) {
    this.handleSDKStatus(status);
  }
  //SYNCHRONIZATION EVENT LISTENERS
  public void backendSynchronizeStartEvent() {
    System.out.println("synchronization started");
  }
  public void backendSynchronizeDoneEvent(LegicMobileSdkStatus status) {
    this.handleSDKStatus(status);
  }

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
    this.currentContext = callbackContext;
    if (action.equals("initialize")) {
      this.initialize();
      return true;
    } else if (action.equals("initiate_registration")) {
      this.showToast("starting registration");
      String deviceId;
      try {
        JSONObject options = args.getJSONObject(0);
        deviceId = options.getString("deviceId");
      } catch (JSONException e) {
        callbackContext.error("Error encountered: " + e.getMessage());
        return false;
      }
      this.initiateRegistration(deviceId);
      return true;
    } else if (action.equals("finish_registration")) {
      String token;
      try {
        JSONObject options = args.getJSONObject(0);
        token = options.getString("token");
      } catch (JSONException e) {
        callbackContext.error("Error encountered: " + e.getMessage());
        return false;
      }
      mManager.register(token);
      return true;
    } else if (action.equals("unregister")) {
      mManager.unregister();
      return true;
    } else if (action.equals("synchronize")) {
      mManager.synchronizeWithBackend();
      return true;
    } else if (action.equals("get_files")) {

      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
      return true;
    } else if (action.equals("get_card")) {
      
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
      return true;
    } else {
      callbackContext.error("\"" + action + "\" is not a recognized action.");
      this.currentContext = null;
      return false;
    }


      // Verify that the user sent a 'show' action
      /*if (!action.equals("show")) {
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
      }
      String message;
      String duration;
      try {
        JSONObject options = args.getJSONObject(0);
        message = options.getString("message");
        duration = options.getString("duration");
      } catch (JSONException e) {
        callbackContext.error("Error encountered: " + e.getMessage());
        return false;
      }
      // Create the toast
      Toast toast = Toast.makeText(cordova.getActivity(), message,
      DURATION_LONG.equals(duration) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
      // Display toast
      toast.show();
      // Send a positive result to the callbackContext
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
      callbackContext.sendPluginResult(pluginResult);
      return true;*/
  }

  public void handleSdkError(LegicMobileSdkStatus status) {
    if (!status.isSuccess() && this.currentContext != null) {
      // LegicMobileSdkErrorReason gives more insight about the cause
      LegicMobileSdkErrorReason reason = status.getReason();
      this.currentContext.error(status.getError().name());
      this.currentContext = null;
      /*log("An action failed with the following error: " + status.getError().name());
      switch(reason.getReasonType()) {
          case SDK_ERROR:
              log("SDK internal error:\n" +
                      "You probably tried actions that are not allowed (unsupported interfaces, " +
                      "activation of non-deployed files, invalid data).");

              log("SDK error code: " + reason.getSdkErrorCode());
              break;
          case BACKEND_ERROR:
              log("Backend error:\n" +
                      "This is usually caused by invalid configuration data (invalid mobileAppId), " +
                      "incorrect requests (wrong state, not registered) or by problems on the backend system.");

              log("Back-end error code (LEGIC Connect): " + reason.getErrorCode());
              break;
          case HTTP_ERROR:
              log("HTTP error:\n" +
                      "This could be caused by connection or authentication problems, please check " +
                      "your configuration and/or your network settings.");

              log("HTTP Error code: " + reason.getErrorCode());
              break;
          default:
              log("Unknown error reason: " + reason.toString());
      }
      log("Full error description:\n"+ reason);*/
    }
  }
}