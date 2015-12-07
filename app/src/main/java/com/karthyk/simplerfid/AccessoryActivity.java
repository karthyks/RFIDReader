package com.karthyk.simplerfid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class AccessoryActivity extends AppCompatActivity {

  private static final String ACTION_USB_PERMISSION = "com.karthyk.simplerfid" +
      ".USB_ACCESSORY_PERMISSION";
  public static final String TAG = AccessoryActivity.class.getSimpleName();

  UsbManager mUsbManager;
  UsbAccessory mUsbAccessory;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_accessory);
    injectViews();
    checkAccessory();
  }

  private void injectViews() {

  }

  @Override protected void onPause() {
    unregisterReceiver(mUsbReceiver);
    super.onPause();
  }

  @Override protected void onResume() {
    super.onResume();
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    registerReceiver(mUsbReceiver, filter);
  }

  private void checkAccessory() {
    mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
        ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    registerReceiver(mUsbReceiver, filter);

    UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
    if(accessoryList != null) {
      int i = 0;
      String accessory = "";
      while(i < accessoryList.length) {
        mUsbManager.requestPermission(mUsbAccessory, mPermissionIntent);
        accessory += mUsbAccessory.getManufacturer();
        accessory += mUsbAccessory.getModel();
        accessory += mUsbAccessory.getSerial();
      }
      Log.d(TAG, "Accessory : " + accessory);
    } else {
      Log.d(TAG, "Accessory null");
    }
  }

  private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbAccessory accessory = (UsbAccessory) intent
              .getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if(accessory != null){
              //call method to set up accessory communication
              new ReadFromAccessory().execute();
              Log.d(TAG, accessory.toString() + "Accessory attached");
            } else {
              Log.d(TAG, "Accessory in Receiver null");
            }
          }
          else {
            Log.d(TAG, "permission denied for accessory " + accessory);
          }
        }
      }
      if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
        UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
        if (accessory != null) {

        }
      }


    }
  };

  private void readFromAccessory() {
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    Log.d(TAG, "openAccessory: " + mUsbAccessory);
    mFileDescriptor = mUsbManager.openAccessory(mUsbAccessory);
    if (mFileDescriptor != null) {
      FileDescriptor fd = mFileDescriptor.getFileDescriptor();
      mInputStream = new FileInputStream(fd);
      mOutputStream = new FileOutputStream(fd);
      Log.d(TAG, fd.toString());
    }
  }

  private class ReadFromAccessory extends AsyncTask<Void, Void, String> {
    String output;
    @Override protected String doInBackground(Void... params) {
      readFromAccessory();
      return null;
    }

    @Override protected void onPostExecute(String s) {
      super.onPostExecute(s);
    }
  }
}
