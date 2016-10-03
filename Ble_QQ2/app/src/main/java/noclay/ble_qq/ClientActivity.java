package noclay.ble_qq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.SocketOptions;
import java.util.UUID;

import com.example.ble_qq.ServiceActivity.ReceiverInfoThread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 客户端
 * 
 * @author LGL
 *
 */
public class ClientActivity extends Activity {

	// 连接成功
	private static final int CONN_SUCCESS = 0x1;
	// 连接失败
	private static final int CONN_FAIL = 0x2;
	private static final int RECEIVER_INFO = 0x3;
	// 设置文本框为空
	private static final int SET_EDITTEXT_NULL = 0x4;
	// 接收到的消息
	private TextView tv_content;
	// 输入框
	private EditText et_info;

	// 发送按钮
	private Button btn_send;

	// 本地蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter = null;

	// 远程设备
	private BluetoothDevice device = null;

	// 蓝牙设备Socket客户端
	private BluetoothSocket socket = null;

	private boolean isReceiver = true;

	// 设备名称
	private static final String NAME = "LGL";

	// 输入输出流
	private PrintStream out;
	private BufferedReader in;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle("客户端");
		setContentView(R.layout.activity_client);

		initView();
		// 初始化Socket客户端连接
		init();

	}

	private void initView() {
		// 初始化
		tv_content = (TextView) findViewById(R.id.tv_content);
		et_info = (EditText) findViewById(R.id.et_info);
		btn_send = (Button) findViewById(R.id.btn_send);
	}

	private void init() {
		tv_content.setText("客户端已经启动，正在与服务端连接...\n");
		// 开始连接
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					// 得到本地蓝牙适配器
					mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					// 通过本地适配器得到地址,这个地址可以公共扫描来获取，就是getAddress()返回的地址
					device = mBluetoothAdapter
							.getRemoteDevice("98:6C:F5:CE:0E:81");
					// 根据UUID返回一个socket,要与服务器的UUID一致
					socket = device.createRfcommSocketToServiceRecord(UUID
							.fromString("00000000-2527-eef3-ffff-ffffe3160865"));
					if (socket != null) {
						// 连接
						socket.connect();
						// 处理流
						out = new PrintStream(socket.getOutputStream());

						in = new BufferedReader(new InputStreamReader(socket
								.getInputStream()));
					}
					// 连接成功发送handler
					handler.sendEmptyMessage(CONN_SUCCESS);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Message mes = handler.obtainMessage(CONN_FAIL,
							e.getLocalizedMessage());
					handler.sendMessage(mes);
				}

			}
		}).start();
	}

	/**
	 * Handler接收消息
	 * 
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONN_SUCCESS:
				setInfo("连接成功! \n");
				btn_send.setEnabled(true);
				Log.i("设备的名称", device.getName());
				Log.i("设备的UUID", device.getUuids() + "");
				Log.i("设备的地址", device.getAddress());
				// 开始接收信息
				new Thread(new ReceiverInfoThread()).start();
				break;
			case CONN_FAIL:
				setInfo("连接失败! \n");
				setInfo(msg.obj.toString() + "\n");
				break;
			case RECEIVER_INFO:
				setInfo(msg.obj.toString() + "\n");
				break;
			case SET_EDITTEXT_NULL:
				et_info.setText("");
				break;

			}
		}
	};

	/**
	 * 接收消息的线程
	 */
	class ReceiverInfoThread implements Runnable {

		@Override
		public void run() {
			String info = null;
			while (isReceiver) {
				try {
					info = in.readLine();
					Message msg = handler.obtainMessage(RECEIVER_INFO);
					handler.sendMessage(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 发送消息
	 */
	public void SendText(View v) {
		final String text = et_info.getText().toString();
		// 不能为空
		if (!TextUtils.isEmpty(text)) {
			Toast.makeText(this, "不能为空", Toast.LENGTH_SHORT).show();
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// 输出
				out.println(text);
				out.flush();
				// 把文本框设置为空
				handler.sendEmptyMessage(SET_EDITTEXT_NULL);
			}
		}).start();
	}

	/**
	 * 拼接文本信息
	 */
	private void setInfo(String info) {
		StringBuffer sb = new StringBuffer();
		sb.append(tv_content.getText());
		sb.append(info);
		tv_content.setText(sb);
	}
}
