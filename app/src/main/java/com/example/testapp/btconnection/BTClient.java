package com.example.testapp.btconnection;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.testapp.testapp.MainActivityServer;

public class BTClient
{
    private final static String TAG = "93367";
    private static String distServerName = "bbxjf";
    private final static String ENCODING = "UTF-8";
    private final static UUID uuid0 = UUID
            .fromString("a60f35f0-b93a-11de-8a39-08002009c666");
    private final static UUID uuid1 = UUID
            .fromString("a60f35f1-b93a-11de-8a39-08002009c666");
    private final static UUID uuid2 = UUID
            .fromString("a60f35f2-b93a-11de-8a39-08002009c666");
    private static boolean needStop = false;

    private static Context context;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BTClientThread mClientThread;

    /**
     * init BT adapter
     */
    static
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
    }

    public static void setDistServerName(String serverName)
    {
        distServerName = serverName;
    }

    /**
     * start connect<br>
     * It will check bonded devices whether match selected server name
     */
    public static void startConnect(Context context, int id)
    {
        BTClient.context = context;
        // String distServerName = "HTC D816w";
        //String distServerName = "ALCATEL";
        Set<BluetoothDevice> set = mBluetoothAdapter.getBondedDevices();
        Iterator<BluetoothDevice> iterator = set.iterator();
        BluetoothDevice bluetoothDevice = null;
        while (iterator.hasNext())
        {
            BluetoothDevice device = iterator.next();
            String name = device.getName();
            Log.i(TAG, "Bonded device: " + name);
            if (distServerName.equals(name))
            {
                bluetoothDevice = device;
                break;
            }
        }
        if (bluetoothDevice == null)
        {
            Log.i(TAG, "Server Not Found");
            return;
        }
        mClientThread = new BTClientThread(bluetoothDevice, id);
        mClientThread.start();
    }

    public static void send(final String msg)
    {
        new Thread()
        {
            public void run()
            {
                if (mClientThread != null)
                {
                    mClientThread.write(msg);
                    //mClientThread.write(RSAUtils.encryptString(msg));
                }
            }
        }.start();
    }

    private static class BTClientThread extends Thread
    {
        private int id;
        private BluetoothDevice bluetoothDevice;
        private BluetoothSocket bluetoothSocket;
        private OutputStream os = null;
        private InputStream is = null;

        public BTClientThread(BluetoothDevice bluetoothDevice, int id)
        {
            this.id = id;
            this.bluetoothDevice = bluetoothDevice;
        }

        @Override
        public void run()
        {
            try
            {
                UUID uuid = null;
                if (id == 0)
                    uuid = uuid0;
                else if (id == 1)
                    uuid = uuid1;
                else if (id == 2)
                    uuid = uuid2;

                bluetoothSocket = bluetoothDevice
                        .createInsecureRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                is = bluetoothSocket.getInputStream();
                os = bluetoothSocket.getOutputStream();
                Log.i(TAG, "Connected to service");
                os.write(("Hello World" + id).getBytes());
                os.flush();
                byte[] buffer = new byte[2048];
                int number = 0;
                while ((number = is.read(buffer)) > 0)
                {
                    if (needStop)
                        break;
                    String message = new String(buffer, 0, number, ENCODING);
                    Intent intent = new Intent(
                            MainActivityServer.ACTION_DISPLAY);
                    intent.putExtra(MainActivityServer.KEY_MSG, message);
                    context.sendBroadcast(intent);
                    Log.i(TAG, "Message: " + message);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "Failed during connecting to server");
            }
        }

        public void write(String message)
        {
            if (bluetoothSocket == null || os == null)
                return;
            Log.i(TAG, "Sending: " + message);
            try
            {
                byte[] bytes = message.getBytes(ENCODING);
                os.write(bytes);
                os.flush();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "Failed to write data");
            }
        }
    }
}
