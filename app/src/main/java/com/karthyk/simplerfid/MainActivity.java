package com.karthyk.simplerfid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String ACTION_USB_PERMISSION = "com.karthyk.simplerfid.USB_PERMISSION";

  private UsbManager manager;
  private PendingIntent mPermissionIntent;
  private UsbDevice device;

  private TextView tvRfidOut;
  private Button btnCheck;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    injectViews();
    checkInfo();
  }

  private void injectViews() {
    tvRfidOut = (TextView) findViewById(R.id.rfid_out);
    btnCheck = (Button) findViewById(R.id.btn_check);
    //btnCheck.setVisibility(View.GONE);
    btnCheck.setOnClickListener(this);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_check:
        startActivity(new Intent(this, AccessoryActivity.class));
        break;
      default:
    }
  }

  @Override protected void onResume() {
    super.onResume();
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    registerReceiver(mUsbReceiver, filter);
  }

  @Override protected void onPause() {
    Log.d("MainAct", "Paused Activity");
    super.onPause();
  }

  @Override protected void onDestroy() {
    unregisterReceiver(mUsbReceiver);
    Log.d("MainAct", "Destroyed Activity");
    super.onDestroy();
  }

  private void checkInfo() {
    manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        /*
         * this block required if you need to communicate to USB devices it's
         * take permission to device
         * if you want than you can set this to which device you want to communicate
         */
    // ------------------------------------------------------------------
    mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
        ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    registerReceiver(mUsbReceiver, filter);
    // -------------------------------------------------------------------
    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
    String i = "";
    while (deviceIterator.hasNext()) {
      device = deviceIterator.next();
      manager.requestPermission(device, mPermissionIntent);
      i += "\n" + "DeviceID: " + device.getDeviceId() + "\n"
          + "DeviceName: " + device.getDeviceName() + "\n"
          + "DeviceClass: " + device.getDeviceClass() + " - "
          + "DeviceSubClass: " + device.getDeviceSubclass() + "\n"
          + "VendorID: " + device.getVendorId() + "\n"
          + "ProductID: " + device.getProductId() + "\n";
    }
    if(device!=null)
      Log.d("MainAct_Check", device.toString());
  }

  private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbDevice device = (UsbDevice) intent
              .getParcelableExtra(UsbManager.EXTRA_DEVICE);
          if (intent.getBooleanExtra(
              UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (device != null) {
              tvRfidOut.setText("Device Permission Granted");
              new ReadFromUSB().execute();
            }
          } else {
            tvRfidOut.setText("Error" + device);
          }
        }
      }
    }
  };

  private byte[] readFromDevice() {
    byte[] rfidInput = new byte[128];
    UsbInterface usbInterface = device.getInterface(0);
    UsbEndpoint endpoint = usbInterface.getEndpoint(0);
    UsbDeviceConnection connection = manager.openDevice(device);
    connection.claimInterface(usbInterface, true);
    connection.bulkTransfer(endpoint, rfidInput, rfidInput.length, 0);
    //connection.controlTransfer(64, 3, 0X138, 0, rfidInput, rfidInput.length, 10);
    return rfidInput;
  }

  private class ReadFromUSB extends AsyncTask<Void, Void, String> {
    String output = "";
    @Override protected String doInBackground(Void... params) {
      byte[] input = readFromDevice();
      if(input != null) {
        try {
          output = new String(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          Log.d("MainAct", e.toString());
        }
      } else {
        Log.d("MainAct", "Byte Array Null");
      }
      return null;
    }

    @Override protected void onPostExecute(String s) {
      tvRfidOut.setText(output);
      Log.d("MainAct_Receiver", output);
      super.onPostExecute(s);
    }
  }
}
