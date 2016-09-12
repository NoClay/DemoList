package noclay.treehole3.ActivityCollect;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import noclay.treehole3.ListViewPackage.TreeHoleItemForLove;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/22.
 */
public class AddLoveActivity extends AppCompatActivity {
    private ImageView returnLastActivityButton;
    private EditText addLoveToText;
    private EditText addLoveContentText;
    private TextView addLoveContentListener;
    private EditText addLoveFromText;
    private LinearLayout addLoveSendButton;
    private Context context = AddLoveActivity.this;
    private int times;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_love_activity_layout);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置全屏
        initView();
        //初始化控件
        //时刻检测编辑字数，对其进行限制
        addLoveContentText.addTextChangedListener(new TextWatcher() {//时刻检测编辑字数，对其进行限制
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int i = addLoveContentText.getText().toString().length();
                if(i <= 100){
                    addLoveContentListener.setText("还可以编辑" + (100 - i) + "个字");
                }
                else{
                    addLoveContentListener.setText("超出限制" + (i - 100) + "个字");
                }
            }
        });
        //设置编辑内容的监听器
        returnLastActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        addLoveSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(addLoveContentText.getText().toString())){
                    Toast.makeText(AddLoveActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                }else if(addLoveContentText.getText().toString().length() > 100){
                    Toast.makeText(AddLoveActivity.this, "内容过长", Toast.LENGTH_SHORT).show();
                }else if(!isOpenNetWork()){
                    Toast.makeText(AddLoveActivity.this, "好像断网了呢", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "正在发表", Toast.LENGTH_SHORT).show();
                    uploadLoveMessage();//发表
                }
            }
        });
    }
    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connect.getActiveNetworkInfo() != null){
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
    private void initView() {
        returnLastActivityButton = (ImageView) findViewById(R.id.return_last_activity_button);
        addLoveToText = (EditText) findViewById(R.id.add_love_to_text);
        addLoveContentText = (EditText) findViewById(R.id.add_love_content_text);
        addLoveContentListener = (TextView) findViewById(R.id.add_love_content_text_listener);
        addLoveFromText = (EditText) findViewById(R.id.add_love_from_text);
        addLoveSendButton = (LinearLayout) findViewById(R.id.add_love_send_button);
    }

    public void uploadLoveMessage() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginState",MODE_PRIVATE);
        String id = sharedPreferences.getString("userId", null);
        SignUserBaseClass signUserBaseClass = new SignUserBaseClass();
        signUserBaseClass.setObjectId(id);
        TreeHoleItemForLove tree= new TreeHoleItemForLove();
        tree.setAuthor(signUserBaseClass);
        tree.setContent(addLoveContentText.getText().toString());
        if(TextUtils.isEmpty(addLoveFromText.getText().toString())){
            tree.setFromUserName("匿名");
        }else{
            tree.setFromUserName(addLoveFromText.getText().toString());
        }
        if (TextUtils.isEmpty(addLoveToText.getText().toString())){
            tree.setToUserName("匿名");
        }else{
            tree.setToUserName(addLoveToText.getText().toString());
        }
        tree.setFromMan(sharedPreferences.getBoolean("male", false));
        tree.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    Toast.makeText(context, "发表成功", Toast.LENGTH_SHORT).show();
                    delayTime(2000);
                }else{
                    Toast.makeText(context, "发表失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void delayTime(int timeInMill) {//延时关闭活动
        times = timeInMill / 100;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(times > 0){
                    try {
                        times --;
                        Thread.sleep(100);
                        if (times == 0){
                            finish();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
