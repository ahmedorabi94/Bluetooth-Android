package com.ahmedorabi.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.ahmedorabi.bluetoothapp.adapter.DeviceAdapter;
import com.ahmedorabi.bluetoothapp.adapter.DeviceCallback;
import com.ahmedorabi.bluetoothapp.data.Admin;
import com.ahmedorabi.bluetoothapp.data.Device;
import com.ahmedorabi.bluetoothapp.data.db.AdminDatabase;
import com.ahmedorabi.bluetoothapp.data.db.UserDao;
import com.ahmedorabi.bluetoothapp.databinding.ActivityMainBinding;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
    private Set<BluetoothDevice> pairedDevices;
    private ActivityMainBinding binding;

    private final String TAG = "MainActivity";
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    private InputStream mmInStream;
    private OutputStream mmOutStream;
    InputStream tmpIn = null;
    OutputStream tmpOut = null;

    private final File filePath = new File(Environment.getExternalStorageDirectory() + "/Demo4.xls");

    private final DeviceCallback callback = new DeviceCallback() {
        @Override
        public void onItemClick(Device device) {

            Log.e("onItemClick", device.toString());

            //  BA = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bluetoothDevice = BA.getRemoteDevice(device.getAddress());

            new ConnectThread(bluetoothDevice).start();
            //   new AcceptThread().start();


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        BA = BluetoothAdapter.getDefaultAdapter();


        binding.turnOnBtn.setOnClickListener(v -> on());
        binding.turnOffBtn.setOnClickListener(v -> off());
        binding.getVisiableBtn.setOnClickListener(v -> visible());
        binding.listDevicesBtn.setOnClickListener(v -> list());

        binding.sendDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    sendFile(Uri.fromFile(filePath), mmSocket);
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);
                    e.printStackTrace();
                }


//                try {
//                    mmOutStream.write("Test Bluetooth".getBytes());
//
//                    // Share the sent message with the UI activity.
////                    Message writtenMsg = handler.obtainMessage(
////                            MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
////                    writtenMsg.sendToTarget();
//                } catch (IOException e) {
//                    Log.e(TAG, "Error occurred when sending data", e);
//
//                    // Send a failure message back to the activity.
////                    Message writeErrorMsg =
////                            handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
////                    Bundle bundle = new Bundle();
////                    bundle.putString("toast",
////                            "Couldn't send data to the other device");
////                    writeErrorMsg.setData(bundle);
////                    handler.sendMessage(writeErrorMsg);
//                }

            }
        });


        binding.receiveDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  createExcelSheet();
                try {
                    readExcel(filePath);
                } catch (Exception e) {
                    //  e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        });


        new AcceptThread().start();

    }


    public void on() {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }

//        try {
//          //  BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
//            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(BA, null);
//
//            if(uuids != null) {
//                for (ParcelUuid uuid : uuids) {
//                    Log.d(TAG, "UUID: " + uuid.getUuid().toString());
//                }
//            }else{
//                Log.d(TAG, "Uuids not found, be sure to enable Bluetooth!");
//            }
//
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
    }

    public void off() {
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
    }


    public void visible() {

        // Register for broadcasts when a device is discovered.
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(receiver, filter);

        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void list() {
        pairedDevices = BA.getBondedDevices();

        List<Device> deviceList = new ArrayList<>();

        for (BluetoothDevice bt : pairedDevices) {
            deviceList.add(new Device(bt.getName(), bt.getAddress()));

        }

        DeviceAdapter adapter = new DeviceAdapter(callback);
        adapter.submitList(deviceList);
        binding.recyclerView.setAdapter(adapter);


    }


    public void createExcelSheet() {


        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet sheet = hssfWorkbook.createSheet("Custom Sheet");

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.createCell(0).setCellValue("Name");
        rowhead.createCell(1).setCellValue("Mobile Number");
        rowhead.createCell(2).setCellValue("Date Of Birth");
        rowhead.createCell(3).setCellValue("working Company");

        List<Admin> admins = GenerateAdmins.getAdmins();

        for (int i = 1; i <= 5; i++) {
            HSSFRow row = sheet.createRow((short) i);
            Admin admin = admins.get(i - 1);
            row.createCell(0).setCellValue(admin.getName());
            row.createCell(1).setCellValue(admin.getMobile());
            row.createCell(2).setCellValue(admin.getDateOfBirth());
            row.createCell(3).setCellValue(admin.getCompany());
        }


        try {
            if (!filePath.exists()) {
                filePath.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            hssfWorkbook.write(fileOutputStream);

            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void readExcel(File file) {
        if (file == null) {
            Log.e("NullFile", "read Excel Error, file is empty");
            return;
        }

        try {
            InputStream stream = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(stream);
            HSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            String name = "", mobile = "", dateOfBirth = "", company = "";
            List<Admin> adminList = new ArrayList<>();

            for (int r = 1; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                //Read one line at a time


                for (int c = 0; c < cellsCount; c++) {
                    //Convert the contents of each grid to a string
                    String value = getCellAsString(row, c, formulaEvaluator);
                    String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;

                    switch (c) {
                        case 0:
                            name = value;
                            break;
                        case 1:
                            mobile = value;
                            break;
                        case 2:
                            dateOfBirth = value;
                            break;
                        case 3:
                            company = value;
                            break;
                    }

                    //    LogUtils.d(cellInfo);
                    Log.e(TAG, cellInfo);
                }

                adminList.add(new Admin(name, mobile, dateOfBirth, company));

            }

            Log.e(TAG, adminList.toString());

            UserDao dao = AdminDatabase.getDatabase(getApplicationContext()).userDao();
            dao.insertAll(adminList);

            Admin admin = dao.getAllUsersByMobile("01011135949");

            Log.e(TAG,admin.toString());


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            /* proper exception handling to be here */
            //   LogUtils.e(e.toString());
        }

    }


    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);

            value = "" + cellValue.getStringValue();
//            switch (cellValue.getCellType()) {
//                case CellType.BOOLEAN:
//                    value = "" + cellValue.getBooleanValue();
//                    break;
//                case CellType.NUMERIC:
//                    double numericValue = cellValue.getNumberValue();
//                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
//                        double date = cellValue.getNumberValue();
//                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
//                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
//                    } else {
//                        value = "" + numericValue;
//                    }
//                    break;
//                case CellType.STRING:
//                    value = "" + cellValue.getStringValue();
//                    break;
//                default:
//                    break;
//            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
            Log.e(TAG, e.getMessage());
        }
        return value;
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
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Log.e("BroadcastReceiver", deviceName + " : " + deviceHardwareAddress);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //   unregisterReceiver(receiver);
    }

    // To Accept from another device
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = BA.listenUsingRfcommWithServiceRecord("Ahmed", myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    //  manageMyConnectedSocket(socket);
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

    // To Connect to another device
    private class ConnectThread extends Thread {

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            Log.e(TAG, "ConnectThread Start");
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            Log.e(TAG, "ConnectThread Run");

            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e(TAG, "Connected");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, connectException.getMessage());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, closeException.getMessage());
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            // manageMyConnectedSocket(mmSocket);

            new ConnectedThread(mmSocket).start();

        }

        // Closes the client socket and causes the thread to finish.
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

        private byte[] mmBuffer; // mmBuffer store for the stream

        //  private static final String TAG = "MY_APP_DEBUG_TAG";
        private Handler handler; // handler that gets info from Bluetooth service


        public ConnectedThread(BluetoothSocket socket) {

            Log.e(TAG, "ConnectedThread Start");

            mmSocket = socket;


            // Get the input and output streams; using temp objects because
            // member streams are final.
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
            mmOutStream = tmpOut;
        }

        public void run() {

            Log.e(TAG, "ConnectedThread Run");


            mmBuffer = new byte[16 * 1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
//                    numBytes = mmInStream.read(mmBuffer);
//
//                    String message = Arrays.toString(mmBuffer);
//
//                    String s = new String(mmBuffer, 0, numBytes);
//
//                    //  byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
//                    Log.e(TAG, s);

//                    long length = filePath.length();
                    FileOutputStream out = new FileOutputStream(filePath);
//                    OutputStream out = mmOutStream;
//
                    int count;
                    while ((count = mmInStream.read(mmBuffer)) > 0) {
                        out.write(mmBuffer, 0, count);
                    }

                    out.flush();
                    //  out.close();
                    //   in.close();
                    //    socket.close();

//                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
//                    bos.write(s.getBytes());
//                    bos.flush();
//                    bos.close();


//                    FileOutputStream fos = new FileOutputStream(filePath);
//                    fos.write(mmBuffer);
//                    fos.close();

                    // Send the obtained bytes to the UI activity.
//                    Message readMsg = handler.obtainMessage(
//                            MessageConstants.MESSAGE_READ, numBytes, -1,
//                            mmBuffer);
//                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
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