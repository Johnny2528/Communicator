package com.joh.communicate;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.api.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVcommunicator extends CordovaPlugin {

    private CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.startExecuting(message, callbackContext);

            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }


    public void startExecuting(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
        if(PermissionHandler.checkPermission(this,PermissionHandler.RECORD_AUDIO)) {

            switch (message) {
                case "start":
                    if(mSpeechManager==null)
                    {
                        SetSpeechListener();
                    }
                    else if(!mSpeechManager.ismIsListening())
                    {
                        mSpeechManager.destroy();
                        SetSpeechListener();
                    }

                    sendResult("Listening");
                    //callbackContext.success("listening");
                    break;
                case "stop":
                    if(mSpeechManager!=null) {
                        mSpeechManager.destroy();
                        mSpeechManager = null;
                    }
                    sendResult("Stopped");
                    //callbackContext.success("Stopped");
                    break;
                case "mute":
                    if(mSpeechManager!=null) {
                        if(mSpeechManager.isInMuteMode()) {
                            mSpeechManager.mute(false);
                        }
                        else
                        {
                            mSpeechManager.mute(true);
                        }
                        sendResult("Muted");
                        //callbackContext.success("Muted");
                    }
                    break;
            }
        }
        else
        {
            PermissionHandler.askForPermission(PermissionHandler.RECORD_AUDIO,this);
        }
    }else{
        callbackContext.error("Expected one non-empty string argument.");
    }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case PermissionHandler.RECORD_AUDIO:
                if(grantResults.length>0) {
                    if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                        startExecuting("start",null);
                    }
                }
                break;

        }
    }


    private void SetSpeechListener()
    {
        mSpeechManager=new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {



                if(results!=null && results.size()>0)
                {

                    if(results.size()==1)
                    {
                        mSpeechManager.destroy();
                        mSpeechManager = null;
                        sendResult(results.get(0));
                    }
                    else {
                      /*   StringBuilder sb = new StringBuilder();
                       if (results.size() > 5) {
                            results = (ArrayList<String>) results.subList(0, 5);
                        }
                        for (String result : results) {
                            sb.append(result).append("\n");
                        }
                        result_tv.setText(sb.toString());
                        */
                        sendResult(results.get(0));
                        //result_tv.setText(results.get(0));
                    }
                }
                else{
                    result_tv.setText(getString(R.string.no_results_found));
                    callback
                }

                    
            }
        });
    }

    private sendResult(_param){
        PluginResult result = new PluginResult(PluginResult.Status.OK, _param);
        result.setKeepCallback(true);
        callback.sendPluginResult(result);
    }

    @Override
    protected void onPause() {
        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }
        super.onPause();
    }
}
