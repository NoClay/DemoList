package noclay.treehole3.ActivityCollect;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import noclay.treehole3.ListViewPackage.ListViewAdapterForMySpeak;
import noclay.treehole3.ListViewPackage.ListViewAdapterForSpeak;
import noclay.treehole3.ListViewPackage.PullListView;
import noclay.treehole3.ListViewPackage.TreeHoleItemForLove;
import noclay.treehole3.ListViewPackage.TreeHoleItemForSpeak;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 82661 on 2016/8/28.
 */
public class ManagerSpeakActivity extends AppCompatActivity {
    private Context context = ManagerSpeakActivity.this;
    private PullListView listView;
    private ListViewAdapterForMySpeak listViewAdapterForSpeak;
    private List<TreeHoleItemForSpeak> speakList = new ArrayList<>();
    private LinearLayout loadingLayout;
    private AnimationDrawable loadingDrawable;
    private ImageView back;

    private static final int LOAD_OVER = 0;
    private static final int UP_LOAD = 1;
    private static final int DOWN_LOAD = 2;
    private static final int LOAD_LAYOUT = 3;
    private static final int DELETE_ITEM_FOR_SPEAK = 4;

    private boolean isLoadSuccess = false;
    private ImageView returnHomeButton;
    private int times;
    private Message msg1;
    private static final String TAG = "ManagerSpeakActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_speak);
        initView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TreeHoleItemForSpeak treeHoleItemForSpeak = speakList.get(i - 1);
                Intent intent = new Intent(context, SpeakInfoActivity.class);
                intent.putExtra("objectId", treeHoleItemForSpeak.getObjectId());
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TreeHoleItemForSpeak treeHoleItemForSpeak = speakList.get(i - 1);
                msg1 = new Message();
                msg1.arg2 = i - 1;
                Dialog.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case DialogInterface.BUTTON_POSITIVE:{
                                Toast.makeText(context, "正在删除", Toast.LENGTH_SHORT).show();
                                TreeHoleItemForSpeak tree = new TreeHoleItemForSpeak();
                                tree.setObjectId(treeHoleItemForSpeak.getObjectId());
                                tree.delete(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {

                                        msg1.what = DELETE_ITEM_FOR_SPEAK;
                                        msg1.arg1 = ( e == null ? 1 : 0);
                                        handler.sendMessage(msg1);
                                    }
                                });
                                break;
                            }
                            case DialogInterface.BUTTON_NEGATIVE:{
                                break;
                            }
                        }
                    }
                };
                Dialog dialog = new AlertDialog.Builder(context)
                        .setTitle("是否删除？")
                        .setMessage(treeHoleItemForSpeak.getContent())
                        .setPositiveButton("确定",listener)
                        .setNegativeButton("取消",listener)
                        .create();
                dialog.show();
                return true;
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initView() {
        listView = (PullListView) findViewById(R.id.fragment_list_view_for_speak);
        listViewAdapterForSpeak = new ListViewAdapterForMySpeak(context, R.layout.tree_hole_item_for_my_speak, speakList);
        loadingLayout = (LinearLayout) findViewById(R.id.load_layout);
        ImageView iv_loading = (ImageView) findViewById(R.id.iv_loading);
        back = (ImageView) findViewById(R.id.cancel_button);
        returnHomeButton = (ImageView) findViewById(R.id.return_home_button);



        listView.setAdapter(listViewAdapterForSpeak);
        getMore(LOAD_LAYOUT, null);
        loadingDrawable = (AnimationDrawable) iv_loading.getDrawable();
        loadingDrawable.start();

        listView.setOnRefreshListener(new PullListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMore(DOWN_LOAD, null);
            }
        });
        listView.setOnGetMoreListener(new PullListView.OnGetMoreListener() {
            @Override
            public void onGetMore() {
                getMore(UP_LOAD, speakList.get(listViewAdapterForSpeak.getCount() - 1).getCreatedAt());
//                Log.d(TAG, "onGetMore() called with: " + listViewAdapterForSpeak.getCount());
//                Log.d(TAG, "onGetMore() called with: " + speakList.get(listViewAdapterForSpeak.getCount() - 1).getCreatedAt());
            }
        });
        returnHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//返回头部
                listView.smoothScrollToPosition(0);
            }
        });
    }
    private void getMore(final int type, String start) {
        BmobQuery<TreeHoleItemForSpeak> query = new BmobQuery<TreeHoleItemForSpeak>();
        List<BmobQuery<TreeHoleItemForSpeak>> queries = new ArrayList<>();

        SignUserBaseClass user = new SignUserBaseClass();
        user.setObjectId(getSharedPreferences("LoginState",MODE_PRIVATE).getString("userId",null));
        query.addWhereEqualTo("author", new BmobPointer(user));
        queries.add(query);
        if(type == UP_LOAD){
            BmobQuery<TreeHoleItemForSpeak> query1 = new BmobQuery<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date  = null;
            try {
                date = sdf.parse(start);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            query1.addWhereLessThan("createdAt", new BmobDate(date));
            queries.add(query1);
        }
        BmobQuery<TreeHoleItemForSpeak> mainQuery = new BmobQuery<>();
        mainQuery.and(queries);
        mainQuery.order("-createdAt");
        mainQuery.setLimit(10);
        mainQuery.findObjects(new FindListener<TreeHoleItemForSpeak>() {
            @Override
            public void done(List<TreeHoleItemForSpeak> list0, BmobException e) {
                if(e == null){
                    isLoadSuccess = list0.size() == 0 ? false : true;
                    if(type == DOWN_LOAD){
                        speakList.clear();
                    }
                    speakList.addAll(list0);
                    delayTime(1000, type);
                }
            }
        });
    }

    private void delayTime(int timeInMill, final int type) {
        times = timeInMill / 100;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(times > 0){
                    try {
                        times --;
                        Thread.sleep(100);
                        if (times == 0){
                            Message message = new Message();
                            message.what = LOAD_OVER;
                            message.arg1 = type;
                            handler.sendMessage(message);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case DELETE_ITEM_FOR_SPEAK:{
                    if(msg.arg1 == 1){
                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                        speakList.remove(msg.arg2);
                        listViewAdapterForSpeak.notifyDataSetChanged();
                    }else{
                        Toast.makeText(context, "删除失败，数据库错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case LOAD_OVER:{
                    switch (msg.arg1){
                        case UP_LOAD:{
                            if(!isLoadSuccess){
                                Toast.makeText(context, "碰到头了", Toast.LENGTH_SHORT).show();
                            }
                            listViewAdapterForSpeak.notifyDataSetChanged();
                            listView.refreshComplete();
                            listView.getMoreComplete();
                            break;
                        }
                        case DOWN_LOAD:{
//                            Log.d("123", "handleMessage() called with: " + "msg = [" + msg + "]");
                            listViewAdapterForSpeak.notifyDataSetChanged();
                            listView.refreshComplete();
                            listView.getMoreComplete();
                            break;
                        }
                        case LOAD_LAYOUT:{
                            //初始化布局完成，进行首次的加载
                            loadingLayout.setVisibility(View.GONE);
                            listViewAdapterForSpeak.notifyDataSetChanged();
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connect.getActiveNetworkInfo() != null){
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
}
