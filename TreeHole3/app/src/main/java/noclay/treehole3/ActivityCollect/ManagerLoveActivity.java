package noclay.treehole3.ActivityCollect;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import noclay.treehole3.ListViewPackage.ListViewAdapterForLove;
import noclay.treehole3.ListViewPackage.PullListView;
import noclay.treehole3.ListViewPackage.TreeHoleItemForLove;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 82661 on 2016/8/25.
 */
public class ManagerLoveActivity extends AppCompatActivity {
    private PullListView listViewLoveWall;
    List<TreeHoleItemForLove> list = new ArrayList<>();
    ListViewAdapterForLove listViewAdapterForLove;
    private RelativeLayout searchButton;
    private LinearLayout loadLayout;
    private AnimationDrawable loadingDrawable;
    private ImageButton returnHomeButton;
    private ImageView back;
    private static final int LOAD_OVER = 0;
    private static final int UP_LOAD = 1;
    private static final int DOWN_LOAD = 2;
    private static final int LOAD_LAYOUT = 3;
    private static final int DELETE_ITEM_FOR_LOVE = 4;
    private Context context = ManagerLoveActivity.this;
    private int times;
    private Message msg1;
    private boolean isLoadSuccess = false;
    private static final String TAG = "ManagerLoveActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_love);
        initView();

        listViewLoveWall.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TreeHoleItemForLove treeHoleItemForLove = list.get(i - 1);
                msg1 = new Message();
                msg1.arg2 = i - 1;
                Dialog.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case DialogInterface.BUTTON_POSITIVE:{
                                Toast.makeText(context, "正在删除", Toast.LENGTH_SHORT).show();
                                TreeHoleItemForLove tree = new TreeHoleItemForLove();
                                tree.setObjectId(treeHoleItemForLove.getObjectId());
                                tree.delete(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {

                                        msg1.what = DELETE_ITEM_FOR_LOVE;
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
                        .setMessage(treeHoleItemForLove.getContent())
                        .setPositiveButton("确定",listener)
                        .setNegativeButton("取消",listener)
                        .create();
                dialog.show();
                return false;
            }
        });
    }

    private void initView() {
        listViewLoveWall = (PullListView) findViewById(R.id.fragment_list_view_for_left_love);
        listViewAdapterForLove = new ListViewAdapterForLove(context , R.layout.tree_hole_item_for_love, list,false);
        loadLayout = (LinearLayout)findViewById(R.id.load_layout);
        searchButton = (RelativeLayout) findViewById(R.id.search_go_btn);
        searchButton.setVisibility(View.GONE);//设置不可见
        back = (ImageView) findViewById(R.id.cancel_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context , SearchActivity.class);
//                startActivity(intent);
//            }
//        });
        returnHomeButton = (ImageButton) findViewById(R.id.return_home_button);
        //加载上一次打开的数据
        listViewLoveWall.setAdapter(listViewAdapterForLove);
        getMore(LOAD_LAYOUT,"");


        ImageView iv_loading = (ImageView) findViewById(R.id.iv_loading);
        loadingDrawable = (AnimationDrawable) iv_loading.getDrawable();
        loadingDrawable.start();

        listViewLoveWall.setOnRefreshListener(new PullListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新数据，下拉刷新
                //加载最新的数据，并保存到loadList中，没有则为空
                getMore(DOWN_LOAD,"");
                //用于延时
            }
        });
        listViewLoveWall.setOnGetMoreListener(new PullListView.OnGetMoreListener() {
            @Override
            public void onGetMore() {
                //获取更多数据，上拉加载
                getMore(UP_LOAD,list.get(listViewAdapterForLove.getCount() - 1).getCreatedAt() );
            }
        });

        returnHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//返回头部
                listViewLoveWall.smoothScrollToPosition(0);
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

    private void getMore(final int type, String start) {
        BmobQuery<TreeHoleItemForLove> query = new BmobQuery<TreeHoleItemForLove>();
        List<BmobQuery<TreeHoleItemForLove>> queries = new ArrayList<>();

        SignUserBaseClass user = new SignUserBaseClass();
        user.setObjectId(getSharedPreferences("LoginState",MODE_PRIVATE).getString("userId",null));
        query.addWhereEqualTo("author", new BmobPointer(user));
        queries.add(query);
        if(type == UP_LOAD){
            BmobQuery<TreeHoleItemForLove> query1 = new BmobQuery<>();
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
        BmobQuery<TreeHoleItemForLove> mainQuery = new BmobQuery<>();
        mainQuery.and(queries);
        mainQuery.order("-createdAt");
        mainQuery.setLimit(10);
        mainQuery.findObjects(new FindListener<TreeHoleItemForLove>() {
            @Override
            public void done(List<TreeHoleItemForLove> list0, BmobException e) {
                if(e == null){
                    isLoadSuccess = list0.size() == 0 ? false : true;
                    if(type == DOWN_LOAD){
                        list.clear();
                    }
                    list.addAll(list0);
                    delayTime(1000, type);
                }
            }
        });
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DELETE_ITEM_FOR_LOVE:{
                    if(msg.arg1 == 1){
                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                        list.remove(msg.arg2);
                        listViewAdapterForLove.notifyDataSetChanged();
                    }else{
                        Toast.makeText(context, "删除失败，数据库错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case LOAD_OVER: {//刷新完毕
                    switch(msg.arg1){
                        case UP_LOAD:{
                            if(!isLoadSuccess){
                                Toast.makeText(context , "碰到头了", Toast.LENGTH_SHORT).show();
                            }
                            listViewAdapterForLove.notifyDataSetChanged();
                            listViewLoveWall.refreshComplete();
                            listViewLoveWall.getMoreComplete();
                            break;
                        }
                        case DOWN_LOAD:{
                            listViewAdapterForLove.notifyDataSetChanged();
                            listViewLoveWall.refreshComplete();
                            listViewLoveWall.getMoreComplete();
                            break;
                        }
                        case LOAD_LAYOUT:{
                            loadLayout.setVisibility(View.INVISIBLE);
                            listViewAdapterForLove.notifyDataSetChanged();
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };
}
