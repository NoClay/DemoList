package noclay.wechat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.jar.Manifest;

import static android.R.id.message;

/**
 * 尝试构件蓝牙连接的服务
 */

public class MyService{
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static MyService sInstance;
    private final String TAG = "MyService";
    private static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private AcceptThread myAcceptThread;
    private ConnectThread myConnectThread;
    private ConnectedThread myConnectedThread;
    private Handler myHandler;
    private static int connectState = 0;


    public static final int STATE_BREAKOFF = 4;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_NONE = 0;

    /**
     * 构造一个Service实例
     * @param paramContext
     * @param paramHandler
     */
    public MyService(Context paramContext, Handler paramHandler)
    {
        this.myHandler = paramHandler;
        Log.i("MyService", "new  MyService:");
    }

    /**
     * 获取MyService的实例
     * @param paramContext
     * @param paramHandler
     * @return
     */
    public static MyService getInstance(Context paramContext, Handler paramHandler)
    {
        if (sInstance == null)
            sInstance = new MyService(paramContext, paramHandler);
        return sInstance;
    }

    /**
     * 获取连接的状态
     * @return
     */
    public static int getMyState() {
        return connectState;
    }

    /**
     * 设置连接的状态
     * @param myState
     */
    public static void setMyState(int myState) {
        MyService.connectState = myState;
    }

    /**
     * 连接的方法
     * @param paramBluetoothDevice
     */
    public void connect(BluetoothDevice paramBluetoothDevice)
    {
        Log.i("MyService", "connect:" + paramBluetoothDevice.getAddress());

        if ((connectState == STATE_CONNECTED) && (this.myConnectThread != null))
        {
            this.myConnectThread.cancel();
            this.myConnectThread = null;
        }
        if (this.myConnectedThread != null)
        {
            this.myConnectedThread.cancel();
            this.myConnectedThread = null;
        }
        this.myConnectThread = new ConnectThread(paramBluetoothDevice);
        this.myConnectThread.start();
        setMyState(2);
        return;
    }

    /**
     * 已经连接成功，调用此方法
     * @param paramBluetoothSocket
     * @param paramBluetoothDevice
     */
    public void connected(BluetoothSocket paramBluetoothSocket, BluetoothDevice paramBluetoothDevice)
    {
        if (this.myConnectThread != null)
        {
            this.myConnectThread.cancel();
            this.myConnectThread = null;
        }
        if (this.myConnectedThread != null)
        {
            this.myConnectedThread.cancel();
            this.myConnectedThread = null;
        }
        if (this.myAcceptThread != null)
        {
            this.myAcceptThread.cancel();
            this.myAcceptThread = null;
        }
        this.myConnectedThread = new ConnectedThread(paramBluetoothSocket);
        this.myConnectedThread.start();
        return;
    }

    /**
     * 启动服务，等待连接
     */
    public void start()
    {
        if (this.myConnectThread != null)
        {
            this.myConnectThread.cancel();
            this.myConnectThread = null;
        }
        if (MyService.getMyState() != 3)
        {
            if (this.myConnectedThread != null)
            {
                this.myConnectedThread.cancel();
                this.myConnectedThread = null;
            }
            if (this.myAcceptThread == null)
            {
                this.myAcceptThread = new AcceptThread();
                this.myAcceptThread.start();
            }
        }
        return;
    }

    /**
     * 停止服务
     */
    public void stop()
    {
        Log.i("MyService", "MyService stop");
        if (this.myConnectThread != null)
        {
            this.myConnectThread.cancel();
            this.myConnectThread = null;
        }
        if (this.myConnectedThread != null)
        {
            this.myConnectedThread.cancel();
            this.myConnectedThread = null;
        }
        if (this.myAcceptThread != null)
        {
            this.myAcceptThread.cancel();
            this.myAcceptThread = null;
        }
        MyService.setMyState(STATE_NONE);
        sInstance = null;
        return;
    }

    /**
     * 写内容
     * @param paramArrayOfByte
     */
    public void write(byte[] paramArrayOfByte)
    {
        if (MyService.getMyState() != 3)
            return;
        ConnectedThread localConnectedThread = this.myConnectedThread;
        localConnectedThread.write(paramArrayOfByte);
        return;
    }



    private class AcceptThread extends Thread{
        private BluetoothServerSocket bluetoothServerSocket;
        private BluetoothSocket bluetoothSocket;

        public AcceptThread() {
            try {
                bluetoothServerSocket = bluetoothAdapter.
                        listenUsingInsecureRfcommWithServiceRecord("test", MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            if(bluetoothServerSocket != null){
                try {
                    bluetoothServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            setName("AcceptThread");
            if(bluetoothServerSocket == null)
                return;
            while (true){
                try {
                    bluetoothSocket = bluetoothServerSocket.accept();
                    Message message1 = new Message();
                    message1.what = MainActivity.MSG_WAIT_CONNECT;
                    myHandler.sendMessage(message1);
                    if(bluetoothSocket != null){
                        //连接成功
                        Message message = new Message();
                        message.what = MainActivity.MSG_CONNECT_SUCCESS;
                        myHandler.sendMessage(message);
                        Log.e(TAG, "run: " + this.getName() + "连接成功" );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class ConnectThread extends Thread{
        private BluetoothDevice bluetoothDevice;
        private BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
            try {
                bluetoothSocket = this.bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            if(bluetoothSocket != null){
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            setName("ConnectThread");
            if(bluetoothSocket == null)
                return;
            while (true){
                try {
                    bluetoothSocket.connect();
                    Message message = new Message();
                    message.what = MainActivity.MSG_START_CONNECT;
                    myHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: " + this.getName() + e);
                }
            }
        }
    }

    private class ConnectedThread extends Thread{
        byte[] buffer = new byte[1024];
        private InputStream inputStream;
        private OutputStream outputStream;
        long[] times = new long[10];
        String[] wordsBuf = new String[10];
        private BluetoothSocket bluetoothSocket;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
            try {
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel(){
            if(bluetoothSocket != null){
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] paramArrayOfByte)
        {
            try {
                outputStream.write(paramArrayOfByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            setName("connectedThread");
            int i = 0;
            if(bluetoothSocket == null)
                return;
            do {
                try {
                    i = inputStream.read(buffer);
                    Log.e(TAG, "run: " + this.getName() + "     \n" + buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }while (i != 0);
        }
    }
}