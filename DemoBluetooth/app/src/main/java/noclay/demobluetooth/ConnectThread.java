/**
 * 
 */
package noclay.demobluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Himi
 *
 */
public class ConnectThread extends Thread {
	private BlueToothServer bts;
	@Override
	public void run() {
		//ȡ���ɼ�
		MainActivity.btAda.cancelDiscovery();
		String str = MySurfaceView.vc_str.elementAt(MySurfaceView.deviceIndex);
		String[] values = str.split("\\*");//����split�Ĳ����ǲ���������ʽ����
		//split�˷������ַ����ָ�ɶ���ַ���,��������ֳ��������ַ���
		// *�� ֮ǰһ����*�� ֮�� һ��
		String address = values[1];//�������ȡ�������豸��mac��ַ
		UUID uuid = UUID.fromString(MainActivity.SPP_UUID);//�����ձ�֧��SPPЭ��
		//ʵ�������豸
		BluetoothDevice btDevice = MainActivity.btAda.getRemoteDevice(address);
		try {
			MainActivity.btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
			MainActivity.btSocket.connect();
		} catch (IOException e) {
			Log.e("Himi", "Connected Error!");
			Toast.makeText(MainActivity.ma, "�޷����Ӵ��豸!", 1000).show();
			e.printStackTrace();
			return;
		}
		//����������������������豸�������̣߳�һֱ������������
		MySurfaceView.gameState = MySurfaceView.CONNTCTED;
		bts = new BlueToothServer();
		bts.flag = true;
		bts.start();
	}
}
class BlueToothServer extends Thread {
	private InputStream ips;
	boolean flag;
	@Override
	public void run() {
		while (flag) {
			if (MySurfaceView.gameState == MySurfaceView.CONNTCTED) {
				try {
					//û�н��յ����ݣ�����һֱ��������״̬
					ips = MainActivity.btSocket.getInputStream();
					byte[] buffer = new byte[1024];
					if (ips.read(buffer) != -1) {
						String str = new String(buffer, 0, 1);
						if (str.equals("w")) {//��
							MySurfaceView.other_Arcy -= 5;
						} else if (str.equals("s")) {//��
							MySurfaceView.other_Arcy += 5;
						} else if (str.equals("a")) {//��
							MySurfaceView.other_Arcx -= 5;
						} else if (str.equals("d")) {//��
							MySurfaceView.other_Arcx += 5;
						}
					}
				} catch (IOException e) {
					Log.e("Himi", "inPutStream is Error!!");
					e.printStackTrace();
				}
			}
		}
	}
}
