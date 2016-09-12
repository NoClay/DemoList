package noclay.treehole3.FragmentCollect;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import noclay.treehole3.ActivityCollect.CommentActivity;
import noclay.treehole3.ListViewPackage.ListViewAdapterForSpeak;
import noclay.treehole3.ListViewPackage.PullListView;
import noclay.treehole3.ListViewPackage.TreeHoleItemForLove;
import noclay.treehole3.ListViewPackage.TreeHoleItemForSpeak;
import noclay.treehole3.MainActivity;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/19.
 */
public class SpeakFragment extends Fragment{
    private PullListView listView;
    private ListViewAdapterForSpeak listViewAdapterForSpeak;
    private List<TreeHoleItemForSpeak> speakList = new ArrayList<>();
    private LinearLayout loadingLayout;
    private AnimationDrawable loadingDrawable;
    private View speakFragment;
    private static final int LOAD_OVER = 0;
    private static final int UP_LOAD = 1;
    private static final int DOWN_LOAD = 2;
    private static final int LOAD_LAYOUT = 3;
    private boolean isLoadSuccess = false;
    private ImageView returnHomeButton;
    private int times;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        speakFragment = inflater.inflate(R.layout.fragment_speak_activity_layout,container, false);
        initView();
        return speakFragment;
    }

    private void initView() {
        listView = (PullListView) speakFragment.findViewById(R.id.fragment_list_view_for_speak);
        listViewAdapterForSpeak = new ListViewAdapterForSpeak(getActivity(), R.layout.tree_hole_item_for_speak, speakList, listView);
        loadingLayout = (LinearLayout) speakFragment.findViewById(R.id.load_layout);
        listView.setAdapter(listViewAdapterForSpeak);
        returnHomeButton = (ImageView) speakFragment.findViewById(R.id.return_home_button);
        getMore(LOAD_LAYOUT, null);
        ImageView iv_loading = (ImageView) speakFragment.findViewById(R.id.iv_loading);
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
        BmobQuery<TreeHoleItemForSpeak> query = new BmobQuery<>();
        query.order("-createdAt");
        query.include("author");
        query.setLimit(20);
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
        query.findObjects(new FindListener<TreeHoleItemForSpeak>() {
            @Override
            public void done(List<TreeHoleItemForSpeak> list0, BmobException e) {
                if(e == null){
                    isLoadSuccess = list0.size() == 0 ? false : true;
                    if(type == DOWN_LOAD){
                        speakList.clear();
                    }
                    speakList.addAll(list0);
                    delayTime(2000, type);
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
                case LOAD_OVER:{
                    switch (msg.arg1){
                        case UP_LOAD:{
                            if(!isLoadSuccess){
                                Toast.makeText(getActivity().getApplicationContext(), "碰到头了", Toast.LENGTH_SHORT).show();
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
}
