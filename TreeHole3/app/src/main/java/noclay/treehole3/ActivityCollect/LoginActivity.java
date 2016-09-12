package noclay.treehole3.ActivityCollect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import noclay.treehole3.MainActivity;
import noclay.treehole3.OtherPackage.ActivityCollector;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/5/24.
 */
public class LoginActivity extends Activity {

    private String loginUserPhoneNumber;//用户输入的手机号
    private String loginUserPassWord;//用户自己输入的密码

    private AutoCompleteTextView user_name;
    private EditText user_password;
    private TextView signUser;
    private TextView forgetPassWord;
    private TextView back;
    private Button login;
    private CheckBox rememberLoginStateButton;
    private static final int SIGN= 0;
    private static final int CHANGE_PASSWORD = 1;
    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ActivityCollector.addActivity(this);
        if(!isOpenNetWork()){//网络不可用
            Toast.makeText(LoginActivity.this,"你的网络状态不佳", Toast.LENGTH_SHORT).show();
        }

        initView();
        initLoginOnClickListener();
        initSignOnClickListener();
        initChangeOnClickListener();
        forgetPassWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ChangePassWord.class);
                intent.putExtra("isLogin", true);
                startActivityForResult(intent, CHANGE_PASSWORD);
            }
        });
    }

    private void initChangeOnClickListener() {
        forgetPassWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ChangePassWord.class);
                startActivityForResult(intent, CHANGE_PASSWORD);
            }
        });
    }

    private void initSignOnClickListener() {//注册按钮
        signUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignFirstStep.class);
                startActivityForResult(intent,SIGN);
            }
        });
    }

    private void initLoginOnClickListener() {//点击登陆按钮后的事情
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUserPhoneNumber = user_name.getText().toString();//用户自己输入的帐号
                loginUserPassWord = user_password.getText().toString(); //用户输入的密码
                if (TextUtils.isEmpty(loginUserPhoneNumber) || TextUtils.isEmpty(loginUserPassWord)) {
                    Toast.makeText(LoginActivity.this,"用户名或密码不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    BmobQuery<SignUserBaseClass> query = new BmobQuery<SignUserBaseClass>();
                    query.addWhereEqualTo("phoneNumber", loginUserPhoneNumber);
                    query.setLimit(1);
                    query.findObjects(new FindListener<SignUserBaseClass>() {
                        @Override
                        public void done(List<SignUserBaseClass> list, BmobException e) {
                            if(e == null){
                                if(!list.isEmpty())
                                {
                                    SignUserBaseClass one = list.get(0);
                                    if(loginUserPassWord.equals(one.getPassWord())){
                                        Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                                        editLoginState(one.getObjectId(), one.getName(), one.getPhoneNumber(), one.getMan());
//                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                        startActivity(intent);
                                        //设置返回为ok，即登录成功
                                        Intent intent = new Intent("LOGIN_SUCCESS");
                                        LocalBroadcastManager localBroadcastManager =
                                                LocalBroadcastManager.getInstance(LoginActivity.this);
                                        localBroadcastManager.sendBroadcast(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Toast.makeText(LoginActivity.this, "用户未注册", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(LoginActivity.this, "数据库异常", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void editLoginState(String objectId, String name, String phoneNumber, boolean isMan) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginState",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", objectId);
        editor.putString("userName", phoneNumber);
        editor.putString("name",name);
        editor.putBoolean("male", isMan);
        if(rememberLoginStateButton.isChecked()){
            editor.putBoolean("loginRememberState", true);
        }else{
            editor.putBoolean("loginRememberState", false);
        }
        editor.commit();
    }


    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connect.getActiveNetworkInfo() != null){
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    private void initView() {
        user_name = (AutoCompleteTextView) findViewById(R.id.userName);
        user_password = (EditText) findViewById(R.id.userPassWord);
        signUser = (TextView) findViewById(R.id.signUser);
        forgetPassWord = (TextView) findViewById(R.id.forgetPassWord);
        login = (Button) findViewById(R.id.login);
        rememberLoginStateButton = (CheckBox) findViewById(R.id.rememberLoginState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case CHANGE_PASSWORD:{
                if(resultCode == RESULT_OK){//成功修改密码
                    user_name.setText(data.getStringExtra("userName"));
                }
                break;
            }
            case SIGN:{
                if(resultCode == RESULT_OK){//成功注册
                    user_name.setText(data.getStringExtra("userName"));
                }
                break;
            }
        }
    }
}
