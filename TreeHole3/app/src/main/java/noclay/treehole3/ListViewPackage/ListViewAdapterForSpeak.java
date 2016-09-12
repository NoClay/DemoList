package noclay.treehole3.ListViewPackage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;


import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import noclay.treehole3.ActivityCollect.CommentActivity;
import noclay.treehole3.MainActivity;
import noclay.treehole3.OtherPackage.MyCircleImageView;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/20.
 */
public class ListViewAdapterForSpeak extends ArrayAdapter<TreeHoleItemForSpeak>{
    private TreeHoleItemForSpeak treeHoleItemForSpeak;
    private String phoneNumber;
    private int resourceId;
    private ViewHolder viewHolder;
    private PullListView listView;
    private String imageUri = null;

    public ListViewAdapterForSpeak(Context context, int resource, List<TreeHoleItemForSpeak> objects, PullListView listView) {
        super(context, resource, objects);
        this.resourceId = resource;
        this.listView = listView;
        phoneNumber = getContext().getSharedPreferences("LoginState",Context.MODE_PRIVATE).getString("userName",null);
    }
    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        treeHoleItemForSpeak = getItem(position);
        View view;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            //控件绑定
            viewHolder.startCommentActivityButton = (ImageView) view.
                    findViewById(R.id.speak_comment_button);
            viewHolder.userHeadImage = (MyCircleImageView) view.findViewById(R.id.user_head_image);
            viewHolder.userName = (TextView) view.findViewById(R.id.user_name);
            viewHolder.content = (TextView) view.findViewById(R.id.user_content);
            viewHolder.admireShow = (TextView) view.findViewById(R.id.admire_show);
            viewHolder.sharedShow = (TextView) view.findViewById(R.id.shared_show);
            viewHolder.likeButton = (RadioButton) view.findViewById(R.id.like_button);
            viewHolder.dislikeButton = (RadioButton) view.findViewById(R.id.dislike_button);
            viewHolder.commentButton = (ImageButton) view.findViewById(R.id.speak_comment_button);
            viewHolder.shareButton = (ImageButton) view.findViewById(R.id.share_button);
            viewHolder.radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
            //数据错乱解决区
            final ViewHolder finalViewHolder = viewHolder;
            //解决分享的问题
            finalViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isOpenNetWork()){
                        Toast.makeText(getContext(), "请检查您的网络状态", Toast.LENGTH_SHORT).show();
                    }else{
                        treeHoleItemForSpeak = (TreeHoleItemForSpeak) finalViewHolder.shareButton.getTag();
                        shareMsg("西邮树洞", "分享", treeHoleItemForSpeak.getContent(), null);
                        treeHoleItemForSpeak.increment("sharedNumber");
//                        Log.d("logSort", "onClick() called with: ");
                        treeHoleItemForSpeak.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e != null){
                                    Toast.makeText(getContext(), "数据库异常，分享失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        BmobQuery<TreeHoleItemForSpeak> tree2 = new BmobQuery<TreeHoleItemForSpeak>();
                        tree2.getObject(treeHoleItemForSpeak.getObjectId(), new QueryListener<TreeHoleItemForSpeak>() {
                            @Override
                            public void done(TreeHoleItemForSpeak treeHoleItemForSpeak, BmobException e) {
                                if(e == null){
                                    finalViewHolder.sharedShow.setText("分享 " +
                                            treeHoleItemForSpeak.getSharedNumber());
                                }
                            }
                        });
                    }

                }
            });


            finalViewHolder.startCommentActivityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    treeHoleItemForSpeak = (TreeHoleItemForSpeak) finalViewHolder.
                            startCommentActivityButton.getTag();
                    Intent intent = new Intent(getContext(), CommentActivity.class);
                    intent.putExtra("objectId",treeHoleItemForSpeak.getObjectId());

                    getContext().startActivity(intent);
                }
            });
            view.setTag(viewHolder);
            //数据错乱设置tag
            viewHolder.shareButton.setTag(treeHoleItemForSpeak);
            viewHolder.startCommentActivityButton.setTag(treeHoleItemForSpeak);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
            viewHolder.shareButton.setTag(treeHoleItemForSpeak);
            viewHolder.startCommentActivityButton.setTag(treeHoleItemForSpeak);

        }



        viewHolder.sharedShow.setText("分享 " + treeHoleItemForSpeak.getSharedNumber());
        //设置评论的入口
        //设置吐槽的内容
        viewHolder.content.setText(treeHoleItemForSpeak.getContent());
        //设置点赞的显示
        viewHolder.admireShow.setText("点赞 " + treeHoleItemForSpeak.getAdmireNumber());
        //设置头像部分作为空缺
        if(treeHoleItemForSpeak.getNoName()){//设置为匿名
            viewHolder.userName.setText("匿名");
        }else{
            viewHolder.userName.setText(treeHoleItemForSpeak.getAuthor().getName());
        }
        //设置用户头像
        if(treeHoleItemForSpeak.getNoName()){//设置显示匿名的头像
            if(treeHoleItemForSpeak.getAuthor().getMan()){
                viewHolder.userHeadImage.setImageDrawable(getContext().
                        getResources().getDrawable(R.drawable.man));
            }else{
                viewHolder.userHeadImage.setImageDrawable(getContext().
                        getResources().getDrawable(R.drawable.woman));
            }
        }else{
            BmobFile bmobFile = treeHoleItemForSpeak.getAuthor().getUserImage();
            imageUri = bmobFile.getFileUrl();
            viewHolder.userHeadImage.setTag(imageUri);
//            viewHolder.userHeadImage.setImageURI(Uri.parse(imageUri));
//            Log.d("adapter", "position = [" + position + "], user = [" +
//                    treeHoleItemForSpeak.getAuthor().getPhoneNumber()
//                    + "], bmobFile = [" + (bmobFile == null) + "]");
            if(bmobFile != null){
                File image = new File(Environment.getExternalStorageDirectory()
                        + "/XiYouTreeHole/ImageData/userImage/"
                        + treeHoleItemForSpeak.getAuthor().getPhoneNumber()
                        + "userImage.jpg");
                if(image.exists() && image.isFile()){
                    setUserImageView(viewHolder.userHeadImage, treeHoleItemForSpeak.getAuthor().
                            getPhoneNumber(), true, treeHoleItemForSpeak.getAuthor().getMan());
                } else{
                    bmobFile.download(image, new DownloadFileListener() {
                        @Override
                        public void done(String s, BmobException e) {
                            Message message = new Message();
                            message.what = (e == null ? 1 : 0);
                            message.obj = treeHoleItemForSpeak.getAuthor().getPhoneNumber();
                            handler.sendMessage(message);
//                            Log.e("adapter", "下载头像：", e);
                        }

                        @Override
                        public void onProgress(Integer integer, long l) {

                        }
                    });
//                    setUserImageView(viewHolder.userHeadImage, treeHoleItemForSpeak.getAuthor().
//                            getPhoneNumber(), true, treeHoleItemForSpeak.getAuthor().getMan());
                }
            }
        }

        //解决喜欢和不喜欢单选按钮的冲突问题
        viewHolder.radioGroup.setId(position);
        viewHolder.radioGroup.setOnCheckedChangeListener(null);
        //选中状态的恢复
        if(treeHoleItemForSpeak.isLike(phoneNumber)){
            viewHolder.radioGroup.check(R.id.like_button);
        }else if(treeHoleItemForSpeak.isDisLike(phoneNumber)){
            viewHolder.radioGroup.check(R.id.dislike_button);
        }else{
            viewHolder.radioGroup.clearCheck();
        }
        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                treeHoleItemForSpeak = getItem(radioGroup.getId());
                switch (i){
                    case R.id.like_button:{
                        if(!treeHoleItemForSpeak.isLike(phoneNumber)){
                            treeHoleItemForSpeak.addLike(phoneNumber);
                            final int number = treeHoleItemForSpeak.getAdmireNumber();
                            treeHoleItemForSpeak.update(treeHoleItemForSpeak.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e == null){
                                        finalViewHolder.likeButton.setChecked(true);
                                        finalViewHolder.admireShow.setText("点赞" + number);
                                    }else {
                                        Toast.makeText(getContext(), "数据库异常", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        break;
                    }
                    case R.id.dislike_button:{
                        if(!treeHoleItemForSpeak.isDisLike(phoneNumber)){
                            treeHoleItemForSpeak.addDisLike(phoneNumber);
                            final int number = treeHoleItemForSpeak.getAdmireNumber();
                            treeHoleItemForSpeak.update(treeHoleItemForSpeak.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e == null){
                                        finalViewHolder.dislikeButton.setChecked(true);
                                        finalViewHolder.admireShow.setText("点赞" + number);
                                    }else {
                                        Toast.makeText(getContext(), "数据库异常", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        break;
                    }
                }
            }
        });

        return view;
        
    }


    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connect.getActiveNetworkInfo() != null){
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    private void setUserImageView(ImageView userImage, String phoneNumber, boolean isSuccess, boolean isMan){
        if(isSuccess){
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().
                        getContentResolver(), Uri.fromFile(new File(Environment.
                        getExternalStorageDirectory()
                        + "/XiYouTreeHole/ImageData/userImage/"
                        + phoneNumber + "userImage.jpg")));
                userImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(isMan){
            userImage.setImageDrawable(getContext().
                    getResources().getDrawable(R.drawable.man));
        }else{
            userImage.setImageDrawable(getContext().
                    getResources().getDrawable(R.drawable.woman));
        }

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Log.d("adapter", "handleMessage() called with: " + "msg = [" + msg.what + "]");
            ImageView imageView = (ImageView) listView.findViewWithTag(imageUri);
            setUserImageView(imageView, (String) msg.obj, true, treeHoleItemForSpeak.getAuthor().getMan());
        }
    };
    public void shareMsg(String activityTitle, String msgTitle, String msgText,
                         String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(Intent.createChooser(intent, activityTitle));
    }

    private class ViewHolder {
        ImageView startCommentActivityButton;
        MyCircleImageView userHeadImage;
        TextView userName;
        TextView content;
        TextView admireShow;//热度展示
        TextView sharedShow;
        RadioButton likeButton;
        RadioButton dislikeButton;
        ImageButton commentButton;
        ImageButton shareButton;
        RadioGroup radioGroup;
    }

}
