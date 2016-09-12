package noclay.treehole3.ListViewPackage;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import noclay.treehole3.ActivityCollect.CommentActivity;
import noclay.treehole3.R;

/**
 * Created by 82661 on 2016/8/28.
 */
public class ListViewAdapterForMySpeak extends ArrayAdapter<TreeHoleItemForSpeak>{

    private int resourceId;
    public ListViewAdapterForMySpeak(Context context, int resource, List<TreeHoleItemForSpeak> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }
    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        TreeHoleItemForSpeak treeHoleItemForSpeak = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();

            viewHolder.content = (TextView) view.findViewById(R.id.user_content);
            viewHolder.admireShow = (TextView) view.findViewById(R.id.admire_show);
            viewHolder.sharedShow = (TextView) view.findViewById(R.id.shared_show);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        //设置吐槽的内容
        viewHolder.content.setText(treeHoleItemForSpeak.getContent());
        //设置点赞的显示
        viewHolder.admireShow.setText("点赞 " + treeHoleItemForSpeak.getAdmireNumber());
        viewHolder.admireShow.setText("分享 " + treeHoleItemForSpeak.getSharedNumber());
        return view;
    }
    private class ViewHolder {
        TextView content;
        TextView admireShow;//热度展示
        TextView sharedShow;
    }
}
