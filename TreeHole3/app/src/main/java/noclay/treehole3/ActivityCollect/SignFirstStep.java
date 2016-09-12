package noclay.treehole3.ActivityCollect;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;
import noclay.treehole3.MainActivity;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/24.
 */
public class SignFirstStep extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SignFirstStep";
    private ImageView cancelButton;
    private EditText signUserName;
    private RadioButton manChecked, womanChecked;
    private EditText signUserPassWord, signUserPassWordAgain;
    private EditText signUserPhoneNumber;
    private Button sendMessage;
    private EditText checkNumberForMessage;
    private TextView accessText;
    private Button completeSignButton;
    private Context context = SignFirstStep.this;
    private int cur = 0;
    private int i = 30;
    private static final int MSG_WHAT_FOR_THREAD = 0;
    private static final int MSG_WHAT_FOR_THREAD_DEATH = 2;
    private static final int MSG_WHAT_FOT_SHORT_MESSAGE = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_activity_layout);
        initView();
        SMSSDK.initSDK(SignFirstStep.this, "1559e5fc73570", "8a88fdb37b3887daa07b4074a1b9b66b");
        EventHandler eh = new EventHandler() {
            @Override
            public void afterEvent(int i, int i1, Object o) {
                Message msg = new Message();
                msg.arg1 = i;
                msg.arg2 = i1;
                msg.what = MSG_WHAT_FOT_SHORT_MESSAGE;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);
        //设置电话的验证
        signUserPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isMobileNum(signUserPhoneNumber.getText().toString())) {
                    //发送短信
                    sendMessage.setClickable(true);
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_def));
                } else {
                    sendMessage.setClickable(false);
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_2));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    public static boolean isMobileNum(String mobiles) {
        String regex = "1[3|5|7|8|][0-9]{9}";
        return mobiles.matches(regex);
    }

    private void initView() {
        cancelButton = (ImageView) findViewById(R.id.cancel_button);
        signUserName = (EditText) findViewById(R.id.name);
        manChecked = (RadioButton) findViewById(R.id.man_check);
        womanChecked = (RadioButton) findViewById(R.id.woman_check);
        signUserPassWord = (EditText) findViewById(R.id.loadPassWord);
        signUserPassWordAgain = (EditText) findViewById(R.id.loadPassWordAgain);
        signUserPhoneNumber = (EditText) findViewById(R.id.loadPhoneNumber);
        sendMessage = (Button) findViewById(R.id.send_message);
        checkNumberForMessage = (EditText) findViewById(R.id.checkNumber);
        accessText = (TextView) findViewById(R.id.accessText);
        completeSignButton = (Button) findViewById(R.id.completeSign);
        cancelButton.setOnClickListener(this);
        manChecked.setOnClickListener(this);
        womanChecked.setOnClickListener(this);
        sendMessage.setOnClickListener(this);
        accessText.setOnClickListener(this);
        completeSignButton.setOnClickListener(this);
        signUserName.requestFocus();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_button: {
                setResultBack(false);
                break;
            }
            case R.id.man_check: {
                cur = 1;
                break;//是男的则为1
            }
            case R.id.woman_check: {
                cur = -1;
                break;
            }
            case R.id.send_message: {
                Bmob.initialize(this, "e7a1bf15265fddb02517d7d9181fe6a6");
                BmobQuery<SignUserBaseClass> query = new BmobQuery<>();
                query.addWhereEqualTo("phoneNumber", signUserPhoneNumber.getText().toString());
                query.findObjects(new FindListener<SignUserBaseClass>() {
                    @Override
                    public void done(List<SignUserBaseClass> list, BmobException e) {
                        if (!list.isEmpty()) {
                            Toast.makeText(SignFirstStep.this, "该手机已注册", Toast.LENGTH_SHORT).show();
                        } else{//未查询到用户
                            SMSSDK.getVerificationCode("86", signUserPhoneNumber.getText().
                                    toString(), new OnSendMessageHandler() {
                                @Override
                                public boolean onSendMessage(String s, String s1) {
                                    return false;
                                }
                            });
                        }
                    }
                });

                break;
            }
            case R.id.accessText: {
                Intent intent = new Intent(context, HelpText.class);
                startActivity(intent);
                break;
            }
            case R.id.completeSign: {
                SignUserBaseClass signOne = new SignUserBaseClass();
                if (signUserName.getText().toString().length() > 20 || signUserName.getText().toString().length() <= 0) {
                    Toast.makeText(context, "昵称过长或过短", Toast.LENGTH_SHORT).show();
                } else if (cur == 0) {
                    Toast.makeText(context, "未选择性别", Toast.LENGTH_SHORT).show();
                } else if (signUserPassWord.getText().toString().length() > 16 || signUserPassWord.
                        getText().toString().length() < 6) {
                    Toast.makeText(context, "密码过长或过短", Toast.LENGTH_SHORT).show();
                } else if (!signUserPassWord.getText().toString().equals(signUserPassWordAgain.getText().toString())) {
                    Toast.makeText(context, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(checkNumberForMessage.getText().toString())) {
                    Toast.makeText(context, "验证码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    SMSSDK.submitVerificationCode("86", signUserPhoneNumber.getText().toString(), checkNumberForMessage.getText().toString());
                }
                break;
            }
        }
    }

    private void setResultBack(boolean b) {
        Intent intent = new Intent();
        intent.putExtra("userName", signUserPhoneNumber.getText().toString());
        if (b) {
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_FOR_THREAD: {
                    sendMessage.setClickable(false);
                    sendMessage.setText(msg.arg1 + "秒后可获取验证码");
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_2));
                    break;
                }
                case MSG_WHAT_FOR_THREAD_DEATH: {
//                    Log.d(TAG, "handleMessage() called with: " + "线程死亡");
                    sendMessage.setClickable(true);
                    sendMessage.setText("获取验证码");
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_def));
                    break;
                }
                case MSG_WHAT_FOT_SHORT_MESSAGE: {
                    int event = msg.arg1;
                    int result = msg.arg2;
                    switch (event) {
                        case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE: {
                            if (result == SMSSDK.RESULT_COMPLETE) {
                                signUser();//将注册信息上传到服务器端
                                //在这里注册吧
                                //验证成功
                            } else {
                                Toast.makeText(context, "验证码错误", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case SMSSDK.EVENT_GET_VERIFICATION_CODE: {
                            if (result == SMSSDK.RESULT_COMPLETE) {
                                Toast.makeText(SignFirstStep.this, "验证码发送成功，请等待", Toast.LENGTH_SHORT).show();
                                i = 30;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (i > 0) {
                                            i--;
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            Message message = new Message();
                                            if (i == 0) {
                                                message.what = SignFirstStep.MSG_WHAT_FOR_THREAD_DEATH;
                                            } else {
                                                message.what = SignFirstStep.MSG_WHAT_FOR_THREAD;
                                            }
                                            message.arg1 = i;
                                            handler.sendMessage(message);
//                                            Log.d(TAG, "run() called with: " + i);
                                        }
                                    }
                                }).start();
//                                Log.d(TAG, "afterEvent() called with: 验证码发送成功");
                                //验证码已发送
                            } else {
                                Toast.makeText(SignFirstStep.this, "验证码发送失败", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "afterEvent() called with: 验证码发送失败");
                                //获取验证码失败
                            }
                        }
                        break;
                    }
                }
            }
        }
    };

    private void signUser() {
        //设置注册按钮不可点击
        completeSignButton.setClickable(false);
        SignUserBaseClass sign = new SignUserBaseClass();
        sign.setPhoneNumber(signUserPhoneNumber.getText().toString());//设置用户名
        sign.setPassWord(signUserPassWord.getText().toString());//设置密码
        sign.setName(signUserName.getText().toString());//设置昵称
        if (cur == 1) {//设置性别
            sign.setMan(true);
        } else if (cur == -1) {
            sign.setMan(false);
        }
        sign.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    Toast.makeText(context, "注册成功，即将跳转到登录界面", Toast.LENGTH_SHORT).show();
                    setResultBack(true);
                } else {

                    Toast.makeText(context, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    setResultBack(false);

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResultBack(false);
        super.onBackPressed();
    }
}
