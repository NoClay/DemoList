package noclay.treehole3.ActivityCollect;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import noclay.treehole3.ListViewPackage.TreeHoleItemForSpeak;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/22.
 */
public class AddSpeakActivity extends AppCompatActivity {
    private ImageView userImageShow;
    private TextView userNameShow;
    private String userId;
    private CheckBox userIsNone;//用户是否选择匿名
    private ImageView postSpeakButton;
    private EditText contentEditText;
    private int times;
    private Context context = AddSpeakActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_speak_activity_layout);
        initView();
        postSpeakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(contentEditText.getText().toString())){
                    Toast.makeText(context, "内容不能为空", Toast.LENGTH_SHORT).show();
                }else if(!isOpenNetWork()){
                    Toast.makeText(context, "您的网络状况不佳", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "正在发布", Toast.LENGTH_SHORT).show();

                    upLoadSpeakMessage();

                }
            }
        });
    }

    private void upLoadSpeakMessage() {
        TreeHoleItemForSpeak tree = new TreeHoleItemForSpeak();
        SignUserBaseClass author = new SignUserBaseClass();
        author.setObjectId(userId);
        tree.setAuthor(author);
        tree.setNoName(userIsNone.isChecked());
        tree.setContent(contentEditText.getText().toString());
        tree.setSharedNumber(0);
        tree.setCommentNumber(0);
        tree.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
                    delayTime(2000);
                }else{
                    Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show();

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
    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connect.getActiveNetworkInfo() != null){
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
    private void initView() {
        userImageShow = (ImageView) findViewById(R.id.user_image_show);
        userNameShow = (TextView) findViewById(R.id.user_name_show);
        userIsNone = (CheckBox) findViewById(R.id.user_is_none);
        postSpeakButton = (ImageView) findViewById(R.id.post_speak_button);
        contentEditText = (EditText) findViewById(R.id.content_edit_text);
        initUserMessage();//设置用户基本信息
    }

    private void initUserMessage() {
        SharedPreferences shared = getSharedPreferences("LoginState", MODE_PRIVATE);
        userNameShow.setText(shared.getString("name",null));
        userId = shared.getString("userId", null);
    }
}
