package com.example.testapp.btconnection;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.testapp.testapp.MainActivityServer;

public class BTServer
{
    private final static String TAG = "93367";
    private final static String ENCODING = "UTF-8";
    private final static UUID uuid0 = UUID
            .fromString("a60f35f0-b93a-11de-8a39-08002009c666");
    private final static UUID uuid1 = UUID
            .fromString("a60f35f1-b93a-11de-8a39-08002009c666");
    private final static UUID uuid2 = UUID
            .fromString("a60f35f2-b93a-11de-8a39-08002009c666");
    private final static String NAME = "MultiDeviceServer";
    private static boolean needStop = false;

    private static Context context;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BTHandlerThread mHandlerThread;

    // init BT adapter
    static
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
    }

    /**
     * Start BT server
     * 
     * @param context
     */
    public static void startBTServer(Context context)
    {
        BTServer.context = context;
        AcceptConnectThread act0 = new AcceptConnectThread(0, uuid0);
        AcceptConnectThread act1 = new AcceptConnectThread(1, uuid1);
        act0.start();
        act1.start();
    }

    /**
     * Shutdown BT server
     * 
     * @param context
     */
    public static void shutdownBTServer(Context context)
    {
        needStop = true;
    }

    private static class AcceptConnectThread extends Thread
    {
        private int id;
        private UUID uuid;
        private BluetoothServerSocket serverSocket;

        public AcceptConnectThread(int id, UUID uuid)
        {
            this.uuid = uuid;
            this.id = id;
        }

        public void run()
        {
            try
            {
                serverSocket = mBluetoothAdapter
                        .listenUsingInsecureRfcommWithServiceRecord(NAME, uuid);
                Log.i(TAG, "Started Bluetooth Server");
                BluetoothSocket socket = null;
                while (true)
                {
                    socket = serverSocket.accept();
                    if (needStop)
                        break;
                    if (socket == null)
                        continue;
                    mHandlerThread = new BTHandlerThread(id, uuid, socket);
                    mHandlerThread.start();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "Failed to start BT server");
            }
        }
    }

    public static void send(final String msg)
    {
        new Thread()
        {
            public void run()
            {
                if (mHandlerThread != null)
                {
                    mHandlerThread.write(msg);
                    //mClientThread.write(RSAUtils.encryptString(msg));
                }
            }
        }.start();
    }

    private static class BTHandlerThread extends Thread
    {
        private int id;
        private UUID uuid;
        private BluetoothSocket socket;
        private InputStream is;
        private OutputStream os;

        public BTHandlerThread(int id, UUID uuid, BluetoothSocket socket)
        {
            this.id = id;
            this.uuid = uuid;
            this.socket = socket;
        }

        public void run()
        {
            try
            {
                is = socket.getInputStream();
                os = socket.getOutputStream();
                byte[] buffer = new byte[2048];
                int number = 0;
                while ((number = is.read(buffer)) > 0)
                {
                    if (needStop)
                        break;
                    String message = new String(buffer, 0, number, ENCODING);
                    Log.i(TAG, "Message: " + message);

                    Intent intent = new Intent(
                            MainActivityServer.ACTION_DISPLAY);
                    intent.putExtra(MainActivityServer.KEY_MSG, message);
                            //+ " [From Channel" + id + "]");
                    context.sendBroadcast(intent);

                    //write("received");

                    // os.write((message + " too :" + id).getBytes(ENCODING));
                    // os.flush();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "Failed in handling commands");
            }
        }

        public void write(String message)
        {
            if (socket == null || os == null)
                return;
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
