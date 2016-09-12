package noclay.treehole3.ActivityCollect;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import noclay.treehole3.ListViewPackage.ListViewAdapterForLove;
import noclay.treehole3.ListViewPackage.PullListView;
import noclay.treehole3.ListViewPackage.TreeHoleItemForLove;
import noclay.treehole3.R;

/**
 * Created by 82661 on 2016/8/5.
 */
public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private PullListView searchResultShow;
    private ImageView cancelButton;
    private AutoCompleteTextView searchKeyEditText;
    private List<TreeHoleItemForLove> treeList = new ArrayList<>();
    private TextView searchStartButton;
    private CheckBox fromChoosed;
    private CheckBox toChoosed;
    private CheckBox contentChoosed;
    private ListViewAdapterForLove listViewAdapterForLove;
    private TextView searchResultShowInNumber;
    private ImageButton returnHomeButton;
    private LinearLayout mainSearchShow;
    private RelativeLayout mainResultShow;
    private boolean isFirst;
    private static final int LOAD_OVER = 0;
    private static final int UP_LOAD = 1;
    private static final int DOWN_LOAD = 2;
    private static final int LOAD_LAYOUT = 3;
    private int cur = 0;
    private AnimationDrawable loadingDrawable;
    private static final int SEARCH = 0;
    private static final int RESULT = 1;
    private boolean isLoadSuccess = false;
    private LinearLayout loadLayout;
    private int times;
    private int skip;
    private BmobQuery<TreeHoleItemForLove> query;

    private Context context = SearchActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_layout);
        initView();
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBackMode(cur, true);
            }
        });
        returnHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBackMode(cur, false);
            }
        });
        searchStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOpenNetWork()) {
                    Toast.makeText(context, "您的网络状态不佳", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(searchKeyEditText.getText().toString())) {
                    Toast.makeText(context, "关键词不能为空", Toast.LENGTH_SHORT).show();
                } else if (!(fromChoosed.isChecked() || toChoosed.isChecked()
                        || contentChoosed.isChecked())) {
                    Toast.makeText(context, "请选择来源", Toast.LENGTH_SHORT).show();
                } else {
                    isFirst = true;
                    getMore(DOWN_LOAD);
                }
            }
        });
        searchResultShow.setOnRefreshListener(new PullListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新数据，下拉刷新
                //加载最新的数据，并保存到loadList中，没有则为空
                isFirst = false;
                getMore(DOWN_LOAD);
                //用于延时
                delayTime(2000, DOWN_LOAD);
            }
        });
        searchResultShow.setOnGetMoreListener(new PullListView.OnGetMoreListener() {
            @Override
            public void onGetMore() {
                skip++;
                isLoadSuccess = true;
                isFirst = false;
                getMore(UP_LOAD);
                delayTime(2000, UP_LOAD);
            }
        });
    }

    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connect.getActiveNetworkInfo() != null) {
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        setBackMode(cur, false);
    }

    private void setBackMode(int type, boolean isCancel) {
        if (isCancel) {
            finish();
        } else if (type == SEARCH) {
            finish();
        } else {
            cur = SEARCH;
            mainSearchShow.setVisibility(View.VISIBLE);
            mainResultShow.setVisibility(View.GONE);
        }
    }

    private void delayTime(int timeInMill, final int type) {
        times = timeInMill / 100;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (times > 0) {
                    try {
                        times--;
                        Thread.sleep(100);
                        if (times == 0) {
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

    private void getMore(int type) {
        String key = searchKeyEditText.getText().toString();
        BmobQuery<TreeHoleItemForLove> query1 = new BmobQuery<TreeHoleItemForLove>();
        BmobQuery<TreeHoleItemForLove> query2 = new BmobQuery<TreeHoleItemForLove>();
        BmobQuery<TreeHoleItemForLove> query3 = new BmobQuery<TreeHoleItemForLove>();
        query1.addWhereContains("fromUserName", key);
        query2.addWhereContains("toUserName", key);
        query3.addWhereContains("content", key);
        List<BmobQuery<TreeHoleItemForLove>> andQuerys = new ArrayList<BmobQuery<TreeHoleItemForLove>>();
        if (fromChoosed.isChecked()) {
            andQuerys.add(query1);
        }
        if (toChoosed.isChecked()) {
            andQuerys.add(query2);
        }
        if (contentChoosed.isChecked()) {
            andQuerys.add(query3);
        }
        query = new BmobQuery<TreeHoleItemForLove>();
        query.or(andQuerys);//或逻辑
        query.order("-createdAt");//从新到旧排序
        if (type == DOWN_LOAD) {
            query.count(TreeHoleItemForLove.class, new CountListener() {
                @Override
                public void done(Integer integer, BmobException e) {
                    if (e != null) {
                        Toast.makeText(context, "数据库异常", Toast.LENGTH_SHORT).show();
                    } else {
                        //设置页面可视化
                        cur = RESULT;
                        mainResultShow.setVisibility(View.VISIBLE);
                        mainSearchShow.setVisibility(View.GONE);

                        treeList.clear();
                        listViewAdapterForLove.clear();
                        searchResultShowInNumber.setText("共有" + integer + "条表白信息");
                        if (integer == 0) {//0条信息显示无法加载更多
                            loadLayout.setVisibility(View.GONE);
                            searchResultShow.setNoMore();
                        } else {
//                            Log.d(TAG, "done() called with: " + "integer = [" + integer + "], e = [" + isFirst + "]");
                            //加载的动画
                            searchResultShow.setHasMore();
                            if (isFirst) {
                                loadLayout.setVisibility(View.VISIBLE);
                                ImageView iv_loading = (ImageView) findViewById(R.id.iv_loading);
                                loadingDrawable = (AnimationDrawable) iv_loading.getDrawable();
                                loadingDrawable.start();
                            } else {
                                loadLayout.setVisibility(View.GONE);
                            }
                            skip = 0;
                            query.setLimit(15);
                            query.setSkip(skip * 15);
                            query.findObjects(new FindListener<TreeHoleItemForLove>() {
                                @Override
                                public void done(List<TreeHoleItemForLove> list, BmobException e) {
//                                    Log.d(TAG, "在进行搜索");
                                    skip = 1;
                                    if (e == null) {
                                        treeList.addAll(list);
                                        delayTime(2000, DOWN_LOAD);
                                    } else {
                                        Toast.makeText(context, "数据库异常", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            if (integer <= 15){
                                searchResultShow.setNoMore();
                            }
                        }
                    }

                }
            });
        }else{
            query.setLimit(15);
            query.setSkip(skip);
            query.findObjects(new FindListener<TreeHoleItemForLove>() {
                @Override
                public void done(List<TreeHoleItemForLove> list, BmobException e) {

                }
            });
        }
    }


    private void initView() {
        loadLayout = (LinearLayout) findViewById(R.id.load_layout);
        searchResultShow = (PullListView) findViewById(R.id.search_result_show);
        cancelButton = (ImageView) findViewById(R.id.cancel_button);
        searchKeyEditText = (AutoCompleteTextView) findViewById(R.id.search_key_edit_text);
        searchStartButton = (TextView) findViewById(R.id.search_start_button);
        fromChoosed = (CheckBox) findViewById(R.id.checkbox2);
        toChoosed = (CheckBox) findViewById(R.id.checkbox1);
        contentChoosed = (CheckBox) findViewById(R.id.checkbox3);
        contentChoosed.setChecked(true);//默认设置搜索内容
        searchResultShowInNumber = (TextView) findViewById(R.id.search_result_show_in_number);
        returnHomeButton = (ImageButton) findViewById(R.id.return_home_button);
        mainResultShow = (RelativeLayout) findViewById(R.id.main_result_show);
        mainSearchShow = (LinearLayout) findViewById(R.id.main_search_layout);
        mainResultShow.setVisibility(View.GONE);
        cur = SEARCH;
        listViewAdapterForLove = new
                ListViewAdapterForLove(context, R.layout.tree_hole_item_for_love, treeList, false);
        searchResultShow.setAdapter(listViewAdapterForLove);

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_OVER: {//刷新完毕
                    switch (msg.arg1) {
                        case UP_LOAD: {
                            if (!isLoadSuccess) {
                                Toast.makeText(context, "碰到头了", Toast.LENGTH_SHORT).show();
                            }
                            listViewAdapterForLove.notifyDataSetChanged();
                            searchResultShow.refreshComplete();
                            searchResultShow.getMoreComplete();
                            break;
                        }
                        case DOWN_LOAD: {
//                            Log.d("123", "handleMessage() called with: " + "msg = [" + msg + "]");
                            listViewAdapterForLove.notifyDataSetChanged();
                            searchResultShow.refreshComplete();
                            searchResultShow.getMoreComplete();
                            loadLayout.setVisibility(View.GONE);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };

}
