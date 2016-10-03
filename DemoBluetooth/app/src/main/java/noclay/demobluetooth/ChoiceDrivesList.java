package noclay.demobluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 
 * @author Himi
 *
 */
public class ChoiceDrivesList extends Activity {
	//���������豸������
	private String[] names;
	//��ʾ
	private Toast toast;
	//�Ի�����ʾ��ǰ�������������豸
	private AlertDialog.Builder dialog;

	public ChoiceDrivesList() {
		names = new String[MySurfaceView.vc_str.size()];
		for (int i = 0; i < MySurfaceView.vc_str.size(); i++) {
			names[i] = MySurfaceView.vc_str.elementAt(i);
		}
	}
	public void DisplayToast(String str, int type) {
		try {
			toast = null;
			if (type == 0) {
				toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
			} else {
				toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
			}
			toast.setGravity(Gravity.TOP, 0, 220);
			toast.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialog = new AlertDialog.Builder(ChoiceDrivesList.this);
		dialog.setIcon(android.R.drawable.btn_dialog);
		dialog.setSingleChoiceItems(names, 0, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				MySurfaceView.deviceIndex = which;
			}
		}).setIcon(R.drawable.icon).setPositiveButton("����", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				DisplayToast("���������豸��" + MySurfaceView.vc_str.elementAt(MySurfaceView.deviceIndex), 1);
				MySurfaceView.gameState = MySurfaceView.CONNTCTING;
				new ConnectThread().start();
				finish();
			}
		}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				finish();
			}
		}).setTitle("��ѡ�������豸!");
		dialog.show();
	}
}