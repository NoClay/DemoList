package noclay.treehole3.ListViewPackage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import noclay.treehole3.OtherPackage.TitleAndContent;
import noclay.treehole3.R;


/**
 * Created by 寒 on 2016/5/28.
 */
public class ListViewInScrollViewAdapter extends ArrayAdapter<TitleAndContent> {
    int resource;

    public ListViewInScrollViewAdapter(Context context, int resource, List<TitleAndContent> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TitleAndContent titleAndContent = getItem(position);
        ViewHolder viewHolder;
        View view;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resource,null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            viewHolder.content = (TextView) view.findViewById(R.id.content);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.title.setText(titleAndContent.getTitle());
        if(titleAndContent.getTitle().isEmpty()){
            viewHolder.title.setVisibility(View.GONE);
        }
        viewHolder.content.setText(titleAndContent.getContent());
        return view;
    }

    private class ViewHolder {
        TextView title;
        TextView content;
    }
}
