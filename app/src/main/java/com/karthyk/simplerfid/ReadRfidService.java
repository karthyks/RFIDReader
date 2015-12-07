package com.karthyk.simplerfid;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ReadRfidService extends Service {

  private static final String TAG = ReadRfidService.class.getSimpleName();

  UsbDevice mDevice;
  UsbManager mUsbManager;

  @Override public void onCreate() {
    mUsbManager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);
    Log.d(TAG, "Created");
    super.onCreate();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    mDevice = intent.getParcelableExtra("Device");
    if(mDevice != null) {
      new ReadFromUSB().execute();
    }
    Log.d(TAG, "Running Service");
    return START_NOT_STICKY;
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public boolean onUnbind(Intent intent) {
    return super.onUnbind(intent);
  }

  @Override public void onRebind(Intent intent) {
    super.onRebind(intent);
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  private byte[] readFromDevice() {
    byte[] rfidInput = new byte[128];
    UsbInterface usbInterface = mDevice.getInterface(0);
    UsbEndpoint endpoint = usbInterface.getEndpoint(0);
    UsbDeviceConnection connection = mUsbManager.openDevice(mDevice);
    connection.claimInterface(usbInterface, true);
    connection.bulkTransfer(endpoint, rfidInput, rfidInput.length, 0);
    connection.controlTransfer(64, 3, 0X138, 0, rfidInput, rfidInput.length, 10);
    return rfidInput;
  }

  public void broadcastResult(String output) {
    Intent resultIntent = new Intent("ReadRfid");
    resultIntent.putExtra("output", output);
    LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    Log.d(TAG, "BroadCasted");
  }

  private class ReadFromUSB extends AsyncTask<Void, Void, String> {
    String output;
    @Override protected String doInBackground(Void... params) {
      byte[] input = readFromDevice();
      if(input != null) {
        try {
          output = input.toString();
        } catch (Exception e) {
          output = e.toString();
        }
      } else {
        Log.d(TAG, "Byte Array Null");
      }
      return null;
    }

    @Override protected void onPostExecute(String s) {
      broadcastResult(output);
      Log.d(TAG, output);
      super.onPostExecute(s);
    }
  }
}
