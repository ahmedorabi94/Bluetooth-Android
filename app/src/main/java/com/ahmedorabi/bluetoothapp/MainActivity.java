package com.ahmedorabi.bluetoothapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.ahmedorabi.bluetoothapp.adapter.DeviceAdapter;
import com.ahmedorabi.bluetoothapp.adapter.DeviceCallback;
import com.ahmedorabi.bluetoothapp.data.Admin;
import com.ahmedorabi.bluetoothapp.data.Device;
import com.ahmedorabi.bluetoothapp.data.db.AdminDatabase;
import com.ahmedorabi.bluetoothapp.data.db.UserDao;
import com.ahmedorabi.bluetoothapp.databinding.ActivityMainBinding;
import com.ahmedorabi.bluetoothapp.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private ActivityMainBinding binding;
    private final String TAG = "MainActivity";
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    InputStream tmpIn = null;
    OutputStream tmpOut = null;
    List<Device> deviceList;
    private final File filePath = new File(Environment.getExternalStorageDirectory() + "/Admin.xls");

    private final DeviceCallback callback = new DeviceCallback() {
        @Override
        public void onItemClick(Device device) {

            BluetoothDevice bluetoothDevice = BA.getRemoteDevice(device.getAddress());

            new ClientThread(bluetoothDevice).start();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 200);


        BA = BluetoothAdapter.getDefaultAdapter();

        deviceList = new ArrayList<>();

        binding.turnOnBtn.setOnClickListener(v -> on());
        binding.turnOffBtn.setOnClickListener(v -> off());
        binding.getVisiableBtn.setOnClickListener(v -> visible());
        binding.listDevicesBtn.setOnClickListener(v -> list());

        binding.sendDataBtn.setOnClickListener(v -> {

            FileUtils.createExcelSheet(filePath);

            try {
                if (mmSocket != null && filePath.exists()) {
                    Toast.makeText(MainActivity.this, "Start Sending  File... ", Toast.LENGTH_SHORT).show();
                    sendFile(Uri.fromFile(filePath), mmSocket);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                e.printStackTrace();
            }


        });

        binding.receiveDataBtn.setOnClickListener(v -> {
            try {

                if (filePath.exists()) {
                    List<Admin> adminList = FileUtils.readExcel(this, filePath);
                    UserDao dao = AdminDatabase.getDatabase(getApplicationContext()).userDao();
                    dao.insertAll(adminList);

                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });

        binding.searchBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));


        new ServerThread().start();


    }


    public void on() {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }

    }

    public void off() {
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
    }

    public void visible() {

        // Register for broadcasts when a device is discovered.

        binding.progressbar.setVisibility(View.VISIBLE);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        BA.startDiscovery();

//        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        startActivityForResult(getVisible, 0);
    }

    public void list() {
        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();

        for (BluetoothDevice bt : pairedDevices) {
            deviceList.add(new Device(bt.getName(), bt.getAddress()));
        }

        DeviceAdapter adapter = new DeviceAdapter(callback);
        adapter.submitList(deviceList);
        binding.recyclerView.setAdapter(adapter);
        binding.progressbar.setVisibility(View.GONE);

    }


    public void sendFile(Uri uri, BluetoothSocket bs) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(getContentResolver().openInputStream(uri));
        OutputStream os = bs.getOutputStream();


        try {
            int bufferSize = 16 * 1024;
            byte[] buffer = new byte[bufferSize];

            // we need to know how may bytes were read to write them to the byteBuffer
            int len = 0;
            while ((len = bis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            bis.close();
            os.flush();
//           os.close();
//            bs.close();
        }
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                deviceList.add(new Device(deviceName, deviceHardwareAddress));
                list();
                Log.e("BroadcastReceiver", deviceName + " : " + deviceHardwareAddress);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    // To Accept from another device // Server
    private class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public ServerThread() {
            BluetoothServerSocket tmp = null;
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Waiting for Connection...", Toast.LENGTH_SHORT).show());
            try {
                tmp = BA.listenUsingRfcommWithServiceRecord("Ahmed", myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    String name = socket.getRemoteDevice().getName();

                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected To " + name, Toast.LENGTH_SHORT).show());

                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    new ConnectedThread(socket).start();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    // To Connect to another device // Client
    private class ClientThread extends Thread {

        String name;

        public ClientThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            name = device.getName();
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Waiting for Connection To " + name, Toast.LENGTH_SHORT).show());

            Log.e(TAG, "ConnectThread Start");
            try {
                tmp = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.e(TAG, "ConnectThread Run");

            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                mmSocket.connect();
                Log.e(TAG, "Connected");

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "connected To " + name, Toast.LENGTH_SHORT).show());


            } catch (IOException connectException) {
                Log.e(TAG, connectException.getMessage());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, closeException.getMessage());
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            new ConnectedThread(mmSocket).start();

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {

            Log.e(TAG, "ConnectedThread Start");

            mmSocket = socket;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            OutputStream mmOutStream = tmpOut;
        }

        public void run() {

            Log.e(TAG, "ConnectedThread Run");

            byte[] mmBuffer = new byte[16 * 1024];

            while (true) {
                try {

                    FileOutputStream out = new FileOutputStream(filePath);

                    int count;
                    while ((count = mmInStream.read(mmBuffer)) > 0) {
                        out.write(mmBuffer, 0, count);
                    }

                    out.flush();
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Received", Toast.LENGTH_SHORT).show());

                    //  out.close();
                    //   in.close();
                    //    socket.close();

                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }


        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


}