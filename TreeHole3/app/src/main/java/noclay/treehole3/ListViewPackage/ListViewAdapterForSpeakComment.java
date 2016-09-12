package noclay.treehole3.ListViewPackage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/22.
 */
public class ListViewAdapterForSpeakComment extends ArrayAdapter<TreeHoleItemComment>{
    private int resource;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TreeHoleItemComment treeHoleItemComment = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            viewHolder = new ViewHolder();

            viewHolder.commentAuthorName = (TextView) view.findViewById(R.id.comment_author_name);
            viewHolder.commentAuthorOrder = (TextView) view.findViewById(R.id.comment_author_order);
            viewHolder.commentContent = (TextView) view.findViewById(R.id.comment_content);



            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.commentAuthorName.setText(treeHoleItemComment.getAuthorName());
        viewHolder.commentContent.setText(treeHoleItemComment.getContent());
        viewHolder.commentAuthorOrder.setText( (position + 1) + "楼");
        return view;
    }

    public ListViewAdapterForSpeakComment(Context context, int resource, List<TreeHoleItemComment> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    private class ViewHolder {
        TextView commentAuthorName;
        TextView commentAuthorOrder;
        TextView commentContent;
    }
}
