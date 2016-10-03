package noclay.ble_qq;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 蓝牙即时通讯
 * 
 * @author LGL
 *
 */
public class MainActivity extends Activity {

	// 本地蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter;
	private TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView) findViewById(R.id.tv);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 判断手机是否支持蓝牙
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
			finish();
		}
		tv.setText(mBluetoothAdapter.getName()+"\n"+mBluetoothAdapter.getAddress());
	}

	/**
	 * 打开蓝牙并且搜索
	 */
	public void openBluetooth(View v) {
		// 开启搜索
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		// 设置可见性300s
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);

		// 强制打开
		// mBluetoothAdapter.enable();
	}

	/**
	 * 关闭蓝牙
	 */
	public void closeBluetooth(View v) {
		mBluetoothAdapter.disable();
	}

	/**
	 * 打开客户端
	 */
	public void Client(View v) {
		startActivity(new Intent(this, ClientActivity.class));
	}

	/**
	 * 打开服务端
	 */
	public void Service(View v) {
		startActivity(new Intent(this, ServiceActivity.class));
	}
}
