/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */


package org.apache.cordova.geolocation;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;



import javax.security.auth.callback.Callback;

import android.annotation.TargetApi;
import android.provider.Settings;



public class Geolocation extends CordovaPlugin  {

    String TAG = "GeolocationPlugin";
    CallbackContext context;

    String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };

    Boolean doNotShowAnymoreTicked = false;



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        LOG.d(TAG, "--------------------BLUETOOTH IS DISABLED");
                        sendJavascript("events.publish('geoloc.bluetoothDisabled')");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        LOG.d(TAG, "--------------------BLUETOOTH TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        LOG.d(TAG, "--------------------BLUETOOTH IS ENABLED");
                        sendJavascript("events.publish('geoloc.bluetoothEnabled')");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        LOG.d(TAG, "--------------------BLUETOOTH TURNING ON");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                LOG.d(TAG, "--------------------LOCATION STATUS CHANGED");
                boolean gps_enabled;
                boolean network_enabled;
                LocationManager lm = (LocationManager) context.getSystemService(
                        Context.LOCATION_SERVICE);
                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    gps_enabled = false;
                }

                try {
                    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch(Exception ex) {
                    ex.printStackTrace();
                    network_enabled = false;
                }

                if(!gps_enabled && !network_enabled) {
                    sendJavascript("events.publish('geoloc.locationDisabled')");
                }
                else{
                    sendJavascript("events.publish('geoloc.locationEnabled')");
                }
            }
        }
    };

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "We are entering execute");





        context = callbackContext;

        //Context mcontext=this.cordova.getActivity().getApplicationContext();
        Intent intent = null;
        Uri packageUri = Uri.parse("package:" + this.cordova.getActivity().getPackageName());
        if(action.equals("getPermission"))
        {



            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean isEnabled = bluetoothAdapter.isEnabled();
            bluetoothAdapter.enable();
            sendJavascript("events.publish('geoloc.bluetoothEnabled')");


            // Register for broadcasts on BluetoothAdapter state change
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            this.cordova.getActivity().registerReceiver(mReceiver, filter);

            IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
            this.cordova.getActivity().registerReceiver(gpsReceiver, filter2);

            /*

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                LOG.d(TAG, "--------------------NO BLUETOOTH");
                sendJavascript("events.publish('geoloc.bluetoothDisabled')");
            } else {


                if (!mBluetoothAdapter.isEnabled()) {
                    LOG.d(TAG, "--------------------BLUETOOTH IS DISABLED");
                    sendJavascript("events.publish('geoloc.bluetoothDisabled')");
                }
                else{
                    LOG.d(TAG, "--------------------BLUETOOTH IS ENABLED");
                    sendJavascript("events.publish('geoloc.bluetoothEnabled')");
                }
            }
            */




            boolean gps_enabled;
            boolean network_enabled;
            LocationManager lm = (LocationManager) this.cordova.getActivity().getSystemService(
                    Context.LOCATION_SERVICE);
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
                gps_enabled = false;
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {
                ex.printStackTrace();
                network_enabled = false;
            }

            if(!gps_enabled && !network_enabled) {
                sendJavascript("events.publish('geoloc.locationDisabled')");
            }
            else{
                sendJavascript("events.publish('geoloc.locationEnabled')");
            }
            return true;
        }
        else if(action.equals("clearWatch")){
            LOG.d(TAG, "------------------ CLEAR WATCH");
            if(hasPermisssion())
            {
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                context.sendPluginResult(r);
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        PluginResult result;
        //This is important if we're using Cordova without using Cordova, but we have the geolocation plugin installed
        if(context != null) {
            boolean gps_enabled;
            boolean network_enabled;
            LocationManager lm = (LocationManager) this.cordova.getActivity().getSystemService(
                    Context.LOCATION_SERVICE);
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
                gps_enabled = false;
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {
                ex.printStackTrace();
                network_enabled = false;
            }

            if(!gps_enabled && !network_enabled) {
                sendJavascript("events.publish('geoloc.locationDisabled')");
            }
            else{
                sendJavascript("events.publish('geoloc.locationEnabled')");
            }
            
            result = new PluginResult(PluginResult.Status.OK);
            context.sendPluginResult(result);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void sendJavascript(final String javascript) {
        if (javascript != null && javascript.trim().length() > 0 && !javascript.equals("null")) {

            webView.getView().post(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.sendJavascript(javascript);
                    } else {
                        webView.loadUrl("javascript:" + javascript);
                    }
                }
            });
        }
    }

}
