package noclay.treehole3.ListViewPackage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import noclay.treehole3.R;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Created by 寒 on 2016/7/19.
 */
public class ListViewAdapterForLove extends ArrayAdapter<TreeHoleItemForLove>{
    private int resource;
    private boolean isSort = false;
    TreeHoleItemForLove treeHoleItemForLove;
    private String phoneNumber;
    private static final String TAG = "ListViewAdapterForLove";

    public ListViewAdapterForLove(Context context, int resource, List<TreeHoleItemForLove> objects, boolean isSort) {
        super(context, resource, objects);
        this.resource = resource;
        this.isSort = isSort;
        phoneNumber = getContext().getSharedPreferences("LoginState",Context.MODE_PRIVATE).getString("userName",null);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        treeHoleItemForLove = getItem(position);
        View view;
        final ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            viewHolder = new ViewHolder();
            viewHolder.fromUserNameTextView = (TextView) view.findViewById(R.id.from_user_name);
            viewHolder.toUserNameTextView = (TextView) view.findViewById(R.id.to_user_name);
            viewHolder.createAt = (TextView) view.findViewById(R.id.from_user_date);
            viewHolder.loveWallContentTextView = (TextView) view.findViewById(R.id.love_wall_content);
            viewHolder.selectBackground = (LinearLayout) view.findViewById(R.id.select_background);
            viewHolder.admireCheckBox = (RadioButton) view.findViewById(R.id.admire_button);

            //设置左侧的绑定控件
            viewHolder.sortViewBackground = (RelativeLayout) view.findViewById(R.id.sort_view_background);
            viewHolder.sortViewText = (TextView) view.findViewById(R.id.sort_view_text);
            viewHolder.love_wall_icon = (ImageView) view.findViewById(R.id.love_wall_icon);

            //设置祝福按钮的点击事件
            final ViewHolder finalViewHolder = viewHolder;
            viewHolder.admireCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    treeHoleItemForLove = (TreeHoleItemForLove) finalViewHolder.admireCheckBox.getTag();
                    if(treeHoleItemForLove.isLiked(phoneNumber)){
                        Toast.makeText(getContext(), "您已经祝福过了", Toast.LENGTH_SHORT).show();
                    }else{
                        treeHoleItemForLove.addLikes(phoneNumber);
                        treeHoleItemForLove.update(treeHoleItemForLove.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e == null){
                                    finalViewHolder.admireCheckBox.setChecked(true);
                                    finalViewHolder.admireCheckBox.setText(treeHoleItemForLove.
                                            getLikesNumber().toString());
                                    Toast.makeText(getContext(),"谢谢您的祝福", Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    Toast.makeText(getContext(),"数据库异常", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
            view.setTag(viewHolder);
            viewHolder.admireCheckBox.setTag(treeHoleItemForLove);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
            viewHolder.admireCheckBox.setTag(treeHoleItemForLove);
        }

//        设置发送人
        viewHolder.fromUserNameTextView.setText("From:" + treeHoleItemForLove.getFromUserName());
//        设置接收人
        viewHolder.toUserNameTextView.setText("To:" + treeHoleItemForLove.getToUserName());
//        设置内容
        viewHolder.loveWallContentTextView.setText(treeHoleItemForLove.getContent());
//        设置发表的日期
        viewHolder.createAt.setText(treeHoleItemForLove.getCreatedAt().toString());
//        设置表白的皮肤
        if(treeHoleItemForLove.getFromMan()){
            viewHolder.selectBackground.setBackgroundDrawable(getContext().getResources().
                    getDrawable(R.drawable.radius_rectangle_for_love_from_man));
        }else{
            viewHolder.selectBackground.setBackgroundDrawable(getContext().getResources().
                    getDrawable(R.drawable.radius_rectangle_for_love_love_from_woman));
        }
        viewHolder.admireCheckBox.setText(treeHoleItemForLove.getLikesNumber().toString());
        viewHolder.admireCheckBox.setChecked(treeHoleItemForLove.isLiked(phoneNumber));


        if(isSort){
            viewHolder.love_wall_icon.setVisibility(View.INVISIBLE);
            switch(position){
                case 0:{viewHolder.sortViewBackground.setBackgroundResource(R.drawable.first);break;}
                case 1:{viewHolder.sortViewBackground.setBackgroundResource(R.drawable.second);break;}
                case 2:{viewHolder.sortViewBackground.setBackgroundResource(R.drawable.third);break;}
                default:{viewHolder.sortViewBackground.setBackgroundResource(R.drawable.next_sort);break;}
                }
            viewHolder.sortViewText.setText(position + 1 + "");
        }else{
            viewHolder.sortViewBackground.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private class ViewHolder {
        LinearLayout selectBackground;
        TextView fromUserNameTextView;
        TextView toUserNameTextView;
        RadioButton admireCheckBox;
        TextView loveWallContentTextView;
        TextView createAt;
        ImageView love_wall_icon;
        RelativeLayout sortViewBackground;
        TextView sortViewText;
    }
}
