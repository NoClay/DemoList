package noclay.demobluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

/**
 *@author Himi
 */
public class MySurfaceView extends SurfaceView implements Callback, Runnable {
	private Thread th;
	private SurfaceHolder sfh;
	private Canvas canvas;
	private Paint paint;
	private boolean flag;
	//���ڴ洢�������������豸
	public static Vector<String> vc_str;
	//δ���������豸
	public static final int NONE = 1;
	//�������������豸
	public static final int CONNTCTING = 2;
	//�����������豸
	public static final int CONNTCTED = 3;
	//��ǰ��������״̬
	public static int gameState = NONE;
	//���������豸���±�����
	public static int deviceIndex;
	public static int myArc_x = 50, myArc_y = 150, other_Arcx = 110, other_Arcy = 150;
	private OutputStream ops;
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		vc_str = new Vector<String>();
		this.setKeepScreenOn(true);
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
		paint.setAntiAlias(true);
		this.setLongClickable(true);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		Log.e("Himi", "surfaceChanged");
	}
	public void surfaceCreated(SurfaceHolder holder) {
		flag = true;
		th = new Thread(this, "himi_Thread_one");
		th.start();
		Log.e("Himi", "surfaceCreated");
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.e("Himi", "surfaceChanged");
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		flag = false;
		Log.e("Himi", "surfaceDestroyed");
	}
	public void myDraw() {
		try {
			canvas = sfh.lockCanvas();
			if (canvas != null) {
				canvas.drawColor(Color.WHITE);
				paint.setColor(Color.RED);
				switch (gameState) {
				case NONE:
					if (vc_str != null) {
						for (int i = 0; i < vc_str.size(); i++) {
							paint.setTextSize(12);
							canvas.drawText(vc_str.elementAt(i), 3, 150 + i * 30, paint);
						}
					}
					break;
				case CONNTCTING:
					paint.setTextSize(20);
					canvas.drawText("���������豸:", 3, 150, paint);
					paint.setTextSize(12);
					canvas.drawText(vc_str.elementAt(deviceIndex), 3, 190, paint);
					break;
				case CONNTCTED:
					paint.setTextSize(20);
					paint.setTextSize(12);
					canvas.drawText("�ѳɹ�����:" + vc_str.elementAt(deviceIndex), 10, 110, paint);
					paint.setColor(Color.RED);
					canvas.drawCircle(myArc_x, myArc_y, 20, paint);
					paint.setColor(Color.BLUE);
					canvas.drawCircle(other_Arcx, other_Arcy, 20, paint);
					paint.setColor(Color.BLACK);
					canvas.drawText("�ҷ�Բ��", myArc_x - 20, myArc_y - 25, paint);
					canvas.drawText("�Է�Բ��", other_Arcx - 20, other_Arcy - 25, paint);
					break;
				}
			}
		} catch (Exception e) {
			Log.v("Himi", "draw is Error!");
			e.printStackTrace();
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gameState == CONNTCTED) {
			try {
				ops = MainActivity.btSocket.getOutputStream();
				byte bx[] = null;
				byte by[] = null;
				myArc_x = (int) event.getX();
				myArc_y = (int) event.getY();
				bx = new String("X=" + myArc_x).getBytes();
				by = new String("X=" + myArc_x).getBytes();
				if (bx != null && by != null) {
					ops.write(bx);
					ops.write(by);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
	private void logic() {
	}
	public void run() {
		while (flag) {
			logic();
			myDraw();
			try {
				Thread.sleep(100);
			} catch (Exception ex) {
			}
		}
	}
}
