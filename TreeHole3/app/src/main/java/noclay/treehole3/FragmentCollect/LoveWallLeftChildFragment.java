package noclay.treehole3.FragmentCollect;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import noclay.treehole3.ActivityCollect.SearchActivity;
import noclay.treehole3.ListViewPackage.ListViewAdapterForLove;
import noclay.treehole3.ListViewPackage.PullListView;
import noclay.treehole3.ListViewPackage.TreeHoleItemForLove;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/20.
 */
public class LoveWallLeftChildFragment extends Fragment{
    private PullListView listViewLoveWall;
    List<TreeHoleItemForLove> list = new ArrayList<>();
    ListViewAdapterForLove listViewAdapterForLove;
    private RelativeLayout searchButton;
    private View loveWallLeftChildFragment;
    private LinearLayout loadLayout;
    private AnimationDrawable loadingDrawable;
    private ImageButton returnHomeButton;
    private static final int LOAD_OVER = 0;
    private static final int UP_LOAD = 1;
    private static final int DOWN_LOAD = 2;
    private static final int LOAD_LAYOUT = 3;

    private int times;
    private boolean isLoadSuccess = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loveWallLeftChildFragment = inflater.inflate(R.layout.fragment_love_wall_left_child_layout, container, false);
        initView();
        return loveWallLeftChildFragment;
    }

    private void initView() {
        listViewLoveWall = (PullListView) loveWallLeftChildFragment.findViewById(R.id.fragment_list_view_for_left_love);
        listViewAdapterForLove = new ListViewAdapterForLove(getActivity(), R.layout.tree_hole_item_for_love, list,false);
        loadLayout = (LinearLayout) loveWallLeftChildFragment.findViewById(R.id.load_layout);
        searchButton = (RelativeLayout) loveWallLeftChildFragment.findViewById(R.id.search_go_btn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                getActivity().startActivity(intent);
            }
        });
        returnHomeButton = (ImageButton) loveWallLeftChildFragment.findViewById(R.id.return_home_button);
        //加载上一次打开的数据
        listViewLoveWall.setAdapter(listViewAdapterForLove);
        getMore(LOAD_LAYOUT,"");


        ImageView iv_loading = (ImageView) loveWallLeftChildFragment.findViewById(R.id.iv_loading);
        loadingDrawable = (AnimationDrawable) iv_loading.getDrawable();
        loadingDrawable.start();

//        Log.d("123", "initView() called with: " + list.size());
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
        query.order("-createdAt");
        if(type == UP_LOAD){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date  = null;
            try {
                date = sdf.parse(start);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            query.addWhereLessThan("createdAt", new BmobDate(date));
        }
        query.setLimit(10);
        query.findObjects(new FindListener<TreeHoleItemForLove>() {
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
                case LOAD_OVER: {//刷新完毕
                    switch(msg.arg1){
                        case UP_LOAD:{
                            if(!isLoadSuccess){
                                Toast.makeText(getActivity().getApplicationContext(), "碰到头了", Toast.LENGTH_SHORT).show();
                            }
                            listViewAdapterForLove.notifyDataSetChanged();
                            listViewLoveWall.refreshComplete();
                            listViewLoveWall.getMoreComplete();
                            break;
                        }
                        case DOWN_LOAD:{
//                            Log.d("123", "handleMessage() called with: " + "msg = [" + msg + "]");
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
