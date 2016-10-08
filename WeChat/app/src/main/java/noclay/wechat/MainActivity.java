package noclay.wechat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ListMenuPresenter;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.R.id.message;
import static noclay.wechat.R.drawable.bluetooth;

public class MainActivity extends AppCompatActivity {
    private TextView topButton;
    private ListView messageListView;
    private List<MessageForChat> messageList;
    private EditText editText;
    private RelativeLayout sendButton;
    private List<BluetoothDeviceMessage> bluetoothDeviceList;
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;
    private ListViewAdapter adapter;
    ConnectedThread connectedThread;
    SelectBluetoothDevice selectWindow;

    //蓝牙设备的变量
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket, bluetoothSocket1;
    BluetoothDevice bluetoothDevice;
    BluetoothServerSocket bluetoothServerSocket;
    private UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    //请求
    static final int REQUEST_OPEN_BLUETOOTH = 0;
    static final int MSG_WAIT_CONNECT = 0;
    static final int MSG_CONNECT_SUCCESS = 1;
    static final int MSG_START_CONNECT = 2;
    static final int MSG_READ_STRING = 3;
    static final int MSG_CONNECT_FAILED = 4;
    static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        initBluetooth();
        initTopButton();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectedThread == null){
                    toToast("还没有连接");
                }else{
                    connectedThread.write(editText.getText().toString());
                }
            }
        });
    }

    private void initBluetooth() {
        //判断蓝牙的打开状态
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            toToast("本机不支持蓝牙设备");
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_OPEN_BLUETOOTH);

        }
        /**
         * 启动服务器端
         */

        ServerThread serverThread = new ServerThread();
        serverThread.start();
    }

    /**
     * 请求获取用户粗略定位的权限
     * android 6.0及其以上使用
     */
    private void mayRequestLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            int check = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (check != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 0);
            }
        }
    }

    private void toToast(String content) {
        Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
    }


    /**
     * 点击的时候，进行搜索，搜索附近的蓝牙设备，
     * 需要开启设备可见性，方便双方互相的扫描
     */
    private void initTopButton() {
        topButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: startDiscovery");
                bluetoothDeviceList = new ArrayList<BluetoothDeviceMessage>();
                mayRequestLocation();
                openBluetoothDiscoverable();
                searchBluetoothDevice();
                bluetoothDeviceList.clear();
                bluetoothDeviceAdapter = new BluetoothDeviceAdapter(MainActivity.this,
                        R.layout.show_bluetooth_item, bluetoothDeviceList);
                selectWindow = new SelectBluetoothDevice(MainActivity.this,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                BluetoothDeviceMessage message = bluetoothDeviceList.get(i);
                                toToast("正在连接" + message.getName() + " 地址：" + message.getAddress());

                                /**
                                 * 主动连接
                                 */
                                connectBluetoothDevice(message.getName(), message.getAddress());
                            }
                        }, bluetoothDeviceAdapter);
                selectWindow.showAtLocation(findViewById(R.id.main_layout),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });
    }

    /**
     * 和指定的设备进行配对的方法
     *
     * @param device
     */
    private void pairBluetoothDevice(BluetoothDevice device) {
//        配对方法
        Method creMethod = null;
        try {
            creMethod = BluetoothDevice.class
                    .getMethod("createBond");
            try {
                creMethod.invoke(device);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    class ClientThread extends Thread {

        public ClientThread() {
//            Method method;
//            try {
//                Log.d(TAG, "ClientThread: 构建客户端");
//                method = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//                bluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice, 1);
//            } catch (NoSuchMethodException e) {
//                Log.e(TAG, "ClientThread: ", e);
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                Log.e(TAG, "ClientThread: ", e);
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                Log.e(TAG, "ClientThread: ", e);
//                e.printStackTrace();
//            }
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "ClientThread: ", e);
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(bluetoothSocket == null){
                        return;
                    }else{
                        try {
                            Log.d(TAG, "run: isConnected" + bluetoothSocket.isConnected());

                            bluetoothSocket.connect();
                            Log.d(TAG, "run: isConnected" + bluetoothSocket.isConnected());
                            if(bluetoothSocket.isConnected()){
                                Message message = new Message();
                                message.what = MSG_CONNECT_SUCCESS;
                                message.obj = bluetoothSocket.getRemoteDevice();
                                handler.sendMessage(message);
                                connectedThread = new ConnectedThread(bluetoothSocket);
                                connectedThread.start();
                            }else{
                                Message message = new Message();
                                message.what = MSG_CONNECT_FAILED;
                                handler.sendMessage(message);
                            }


                        } catch (IOException e) {
                            Log.e(TAG, "run: ", e);
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * 连接到指定地址的蓝牙
     *
     * @param address
     */
    private void connectBluetoothDevice(String name, String address) {
        Log.d(TAG, "connectBluetoothDevice() called with: " + "name = [" + name + "], address = [" + address + "]");
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        //进行配对方法
        Log.d(TAG, "getBondState() " + bluetoothDevice.getBondState());
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            pairBluetoothDevice(bluetoothDevice);
            Log.d(TAG, "connectBluetoothDevice: bluetoothDevice need paired");
        }
        Log.d(TAG, "connectBluetoothDevice: start");
        if (bluetoothDevice == null) {
            Log.d(TAG, "connectBluetoothDevice: bluetoothDevice is null");
        } else {

            ClientThread clientThread = new ClientThread();
            clientThread.start();
//            try {
//                bluetoothSocket = (BluetoothSocket) bluetoothDevice.getClass()
//                        .getMethod("createRfcommSocket", new Class[] {int.class}).
//                                invoke(bluetoothDevice,1);
//            } catch (IllegalAccessException e) {
//                Log.e(TAG, "connectBluetoothDevice: ", e);
//
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                Log.e(TAG, "connectBluetoothDevice: ", e);
//
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                Log.e(TAG, "connectBluetoothDevice: ", e);
//
//                e.printStackTrace();
//            }
//            /**
//             * break1
//             * 使用bluetoothDevice的两种创建方式，在Api 17创建失败，
//             * 暂时跳过该问题
//             */
//            try {
//
//                bluetoothAdapter.cancelDiscovery();
//                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
//
////                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//            } catch (IOException e) {
//                Log.e(TAG, "connectBluetoothDevice: first", e );
//                e.printStackTrace();
//            }
//

        }
    }

    private class ServerThread extends Thread {
        public ServerThread() {
            Log.d(TAG, "ServerThread: 构件服务端");
            try {
                bluetoothServerSocket = BluetoothAdapter.getDefaultAdapter().
                        listenUsingRfcommWithServiceRecord("test", uuid);
            } catch (IOException e) {
                Log.e(TAG, "ServerThread: construct", e);
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (bluetoothServerSocket != null) {
                try {
                    Log.e(TAG, "run: serverThread  等待连接");
                    bluetoothSocket1 = bluetoothServerSocket.accept();
                    Message message = new Message();
                    message.what = MSG_WAIT_CONNECT;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    Log.e(TAG, "run: ServerThread", e);
                    e.printStackTrace();
                }
                if (bluetoothSocket1 != null) {
                    Log.d(TAG, "run: connect success");
                    connectedThread = new ConnectedThread(bluetoothSocket1);
                    connectedThread.start();
                    Message message = new Message();
                    message.what = MSG_CONNECT_SUCCESS;
                    message.obj = bluetoothSocket1.getRemoteDevice();
                    handler.sendMessage(message);
                }else{
                    Message message = new Message();
                    message.what = MSG_CONNECT_FAILED;
                    handler.sendMessage(message);
                }
            }
        }
    }

    private class ConnectedThread extends Thread{
        private BluetoothSocket bluetoothSocket;
        private OutputStream outputStream;
        private InputStream inputStream;
        byte [] bytes = new byte[1024];


        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
            try {
                outputStream = this.bluetoothSocket.getOutputStream();
                inputStream = this.bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            int i = 0;
            if(inputStream == null)
                return;
            do {
                try {
                    i = inputStream.read(bytes);
                    Message message = new Message();
                    message.what = MSG_READ_STRING;
                    String string = new String(bytes);
                    message.obj = string;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }while (i != 0);
        }

        public void write(String string){
            byte [] bytes;
            bytes = string.getBytes();
            try {
                outputStream.write(bytes);
                MessageForChat msg = new MessageForChat(true, string);
                messageList.add(msg);
                adapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 搜索蓝牙设备，添加到显示页面
     */
    private void searchBluetoothDevice() {
        bluetoothAdapter.startDiscovery();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {

                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                        Log.d(TAG, "onReceive: 结束查找设备");
                        break;
                    }
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED: {
                        Log.d(TAG, "onReceive: 开始查找设备");
                        break;
                    }
                    case BluetoothDevice.ACTION_FOUND: {
                                          /* 从intent中取得搜索结果数据 */
                        Log.d(TAG, "onReceive: 查找到设备");
                        BluetoothDevice device = intent
                                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Iterator<BluetoothDeviceMessage> it = bluetoothDeviceList.iterator();
                        boolean flag = false;
                        while (it.hasNext()) {
                            if (TextUtils.equals(it.next().getAddress(), device.getAddress())) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            BluetoothDeviceMessage message = new BluetoothDeviceMessage(device.getName(),
                                    device.getAddress());
                            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                                message.setPaired(false);
                            } else {
                                message.setPaired(true);
                            }
                            bluetoothDeviceList.add(message);
                            bluetoothDeviceAdapter.notifyDataSetChanged();
                        }
                        Log.d(TAG, "设备：" + device.getName() + " address: " + device.getAddress());
                        break;
                    }
                }

            }
        };
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        if (bluetoothAdapter.isDiscovering()) {//正在查找
            Log.d(TAG, "onClick: 正在查找设备");
        } else {
            bluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 打开蓝牙的可见性，在我测试的小米6.0上，无法计时关闭可见性
     */
    private void openBluetoothDiscoverable() {
        //开启蓝牙可见性
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(intent);
        }
    }

    /**
     * 强制关闭蓝牙的可见性，亲测可用
     */
    private void closeBluetoothDiscoverable() {
        //尝试关闭蓝牙可见性
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(bluetoothAdapter, 1);
            setScanMode.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        topButton = (TextView) findViewById(R.id.topButton);
        messageListView = (ListView) findViewById(R.id.msgListView);
        editText = (EditText) findViewById(R.id.editText);
        sendButton = (RelativeLayout) findViewById(R.id.send);
        messageList = new ArrayList<>();
        initMessage();
        adapter = new ListViewAdapter(MainActivity.this, R.layout.item, messageList);
        messageListView.setAdapter(adapter);
    }

    private void initMessage() {
        for (int i = 0; i < 2; i++) {
            if (i % 2 == 0) {
                MessageForChat message = new MessageForChat(false, "你好");
                messageList.add(message);
            } else {
                MessageForChat message = new MessageForChat(true, "你好");
                messageList.add(message);
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT_SUCCESS:{
                    if(selectWindow != null){
                        selectWindow.dismiss();
                        selectWindow = null;
                    }
                    toToast("连接成功");
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    topButton.setText(device.getName() + " " + device.getAddress());
                    break;
                }
                case MSG_START_CONNECT:{
                    toToast("尝试连接");
                    break;
                }
                case MSG_WAIT_CONNECT:{
                    toToast("等待连接");
                    break;
                }
                case MSG_CONNECT_FAILED:{
                    selectWindow.dismiss();
                    toToast("连接失败，请重试");
                    topButton.setText("Hello World!");
                }
                case MSG_READ_STRING:{

                    MessageForChat msg1 = new MessageForChat(false,(String) msg.obj);
                    messageList.add(msg1);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_OPEN_BLUETOOTH: {
                if (resultCode == RESULT_OK) {
                    toToast("蓝牙已打开");
                } else {
                    toToast("蓝牙未打开，请确认后重新启动本应用");
                    finish();
                }
                break;
            }
        }
    }
}
