package noclay.ble_qq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 服务端
 * 
 * @author LGL
 *
 */
public class ServiceActivity extends Activity {

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

	// 蓝牙设备Socket服务端
	private BluetoothServerSocket serviceSocket = null;

	// 蓝牙设备Socket客户端
	private BluetoothSocket socket = null;

	// 设备名称‘
	private static final String NAME = "LGL";

	private boolean isReceiver = true;

	// 输入输出流
	private PrintStream out;
	private BufferedReader in;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle("服务端");
		setContentView(R.layout.activity_service);

		initView();
		// 创建蓝牙服务端的Socket
		initService();
	}

	private void initView() {
		// 初始化
		tv_content = (TextView) findViewById(R.id.tv_content);
		et_info = (EditText) findViewById(R.id.et_info);
		btn_send = (Button) findViewById(R.id.btn_send);

	}

	private void initService() {
		tv_content.setText("服务器已经启动，正在等待设备连接...\n");
		// 开启线程操作
		new Thread(new Runnable() {

			@Override
			public void run() {
				// 得到本地适配器
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				// 创建蓝牙Socket服务端
				try {
					// 服务端地址
					serviceSocket = mBluetoothAdapter
							.listenUsingInsecureRfcommWithServiceRecord(
									NAME,
									UUID.fromString("00000000-2527-eef3-ffff-ffffe3160865"));
					// 阻塞线程等待连接
					socket = serviceSocket.accept();
					if (socket != null) {
						// I/O流
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
