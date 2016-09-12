package noclay.treehole3;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import noclay.treehole3.ActivityCollect.AddLoveActivity;
import noclay.treehole3.ActivityCollect.AddSpeakActivity;
import noclay.treehole3.ActivityCollect.ChangePassWord;
import noclay.treehole3.ActivityCollect.LoginActivity;
import noclay.treehole3.ActivityCollect.ManagerLoveActivity;
import noclay.treehole3.ActivityCollect.ManagerSpeakActivity;
import noclay.treehole3.FragmentCollect.LoveWallFragment;
import noclay.treehole3.FragmentCollect.SpeakFragment;
import noclay.treehole3.Menu.SlidingMenu;
import noclay.treehole3.OtherPackage.MyCircleImageView;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.SelectPopupWindow.ChooseImageDialog;
import noclay.treehole3.SelectPopupWindow.SelectPopupWindow;

public class MainActivity extends AppCompatActivity {

    private MyCircleImageView nowUserImage;
    private TextView nowUserName;
    private RelativeLayout toggleTheme, toggleAccount, changePassWord, myTreeHoleForLove, myTreeHoleForSpeak;
    private LinearLayout loveButton, speakButton;
    private ImageView loveButtonIcon, speakButtonIcon;
    private TextView loveButtonTitle, speakButtonTitle;
    private ImageView toggleAddMenuButton;
    private android.app.FragmentManager fragmentManager;
    private LoveWallFragment loveWallFragment;
    private SpeakFragment speakFragment;
    private SlidingMenu slidingMenu;
    private MyCircleImageView toggleMenu;
    private SelectPopupWindow mMenuView;
    private ChooseImageDialog chooseUserImageDialog;
    private long mExitTime;//退出的时间
    private Uri userImageUri;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private boolean isAddMenuOpen = false;
    private Context context = MainActivity.this;
    private String userImagePath;
    private static final int REQUEST_CODE_PICK_IMAGE = 0;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1;
    private static final int REQUEST_RESIZE_REQUEST_CODE = 2;
    private static final int REQUEST_LOGIN = 3;
    private static final int MESSAGE_FROM_USER_IMAGE = 0;
    private static final int MESSAGE_FROM_UP_USER_IAMGE = 2;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bmob.initialize(MainActivity.this, "e7a1bf15265fddb02517d7d9181fe6a6");
        //如果有登录的状态，则不进行登录
        initView();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.toggle_menu:
                        slidingMenu.toggle();
                        break;
                    case R.id.toggle_theme:
                        setToggleTheme(true);
                        break;
                    case R.id.love_button_layout:
                        setFooterTheme(1);
                        break;
                    case R.id.add_button: {
                        ObjectAnimator objectAnimator = new ObjectAnimator()
                                .ofFloat(toggleAddMenuButton, View.ROTATION, 0, 45);
                        objectAnimator.setDuration(500);
                        objectAnimator.start();
                        toggleButtonMenu();
                        mMenuView.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                ObjectAnimator objectAnimator = new ObjectAnimator()
                                        .ofFloat(toggleAddMenuButton, View.ROTATION, 45, 0);
                                objectAnimator.setDuration(500);
                                objectAnimator.start();
                            }
                        });
                        break;
                    }
                    case R.id.speak_button:
                        setFooterTheme(3);
                        break;
                    case R.id.my_tree_hole: {
                        Intent intent = new Intent(context, ManagerSpeakActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.my_love_wall: {
                        Intent intent = new Intent(context, ManagerLoveActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.change_passWord: {
//                        Log.d(TAG, "onClick() called with: " + "view = [修改密码]");
                        Intent intent = new Intent(MainActivity.this, ChangePassWord.class);
                        intent.putExtra("isLogin", false);
                        startActivity(intent);
                        break;
                    }
                    case R.id.toggle_account: {
//                        Log.d(TAG, "onClick() called with: " + "view = [切换用户]");
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.userImage: {//添加头像
                        chooseUserImage();
                        break;
                    }
                }
            }
        };
        nowUserImage.setOnClickListener(listener);
        changePassWord.setOnClickListener(listener);
        toggleAccount.setOnClickListener(listener);
        myTreeHoleForLove.setOnClickListener(listener);
        toggleAddMenuButton.setOnClickListener(listener);
        myTreeHoleForSpeak.setOnClickListener(listener);
        toggleTheme.setOnClickListener(listener);
        loveButton.setOnClickListener(listener);
        speakButton.setOnClickListener(listener);
        toggleMenu.setOnClickListener(listener);
    }

    private void chooseUserImage() {//选择照片
        chooseUserImageDialog = new ChooseImageDialog(context, new
                View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chooseUserImageDialog.dismiss();
                        switch (v.getId()) {
                            case R.id.takePhotoBtn: {
                                String state = Environment.getExternalStorageState();
                                if (state.equals(Environment.MEDIA_MOUNTED)) {
                                    Intent getImageByCamera = new
                                            Intent("android.media.action.IMAGE_CAPTURE");
                                    startActivityForResult(getImageByCamera,
                                            REQUEST_CODE_CAPTURE_CAMEIA);
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
                                }
                                break;
                            }
                            case R.id.pickPhotoBtn:
//                                Intent intent = new Intent(Intent.ACTION_PICK);//从相册中选取图片
                                Intent intent = new Intent("android.intent.action.GET_CONTENT");//从相册/文件管理中选取图片
                                intent.setType("image/*");//相片类型
                                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                                break;
                            case R.id.cancelBtn: {
                                break;
                            }
                        }
                    }
                });
        chooseUserImageDialog.showAtLocation(findViewById(R.id.mainLayout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connect.getActiveNetworkInfo() != null) {
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    private void checkIsLogined() {
        SharedPreferences shared = getSharedPreferences("LoginState", MODE_PRIVATE);
        boolean isLogined = shared.getBoolean("loginRememberState", false);
//        Log.d(TAG, "checkIsLogined() called with: " + isLogined);
        if (!isLogined) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }else{
            setNowUser();
        }
    }
    private void setNowUser() {//设置用户的状态
        SharedPreferences shared = getSharedPreferences("LoginState", MODE_PRIVATE);
        nowUserName.setText(shared.getString("name", "您还没有登录"));
        final String phoneNumber = shared.getString("userName", "");
        userImagePath = Environment.getExternalStorageDirectory() +
                "/XiYouTreeHole/ImageData/userImage/" + phoneNumber + "userImage.jpg";
//        Log.d(TAG, "initUserImage() called with: " + "头像初始化");
        final File userImage = new File(userImagePath);
        if (userImage.exists()) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.
                        getContentResolver(),Uri.fromFile(userImage));
                nowUserImage.setImageBitmap(bitmap);
                toggleMenu.setImageBitmap(bitmap);
//                Log.d(TAG, "initUserImage() called with: " + "头像初始化完成");
            } catch (IOException e) {
                e.printStackTrace();
//                Log.e(TAG, "initUserImage: ", e);
            }
        } else if (isOpenNetWork()) {
            BmobQuery<SignUserBaseClass> user = new BmobQuery<>();
            user.getObject(shared.getString("userId", ""), new QueryListener<SignUserBaseClass>() {
                @Override
                public void done(SignUserBaseClass signUserBaseClass, BmobException e) {
                    if (e == null) {
                        BmobFile bmobFile = signUserBaseClass.getUserImage();
                        if (bmobFile != null) {
                            bmobFile.download(userImage, new DownloadFileListener() {
                                @Override
                                public void done(String s, BmobException e) {
                                    Message message = new Message();
                                    message.what = MESSAGE_FROM_USER_IMAGE;
                                    message.arg1 = 1;
                                    message.obj = s;
                                    handler.sendMessage(message);
                                }
                                @Override
                                public void onProgress(Integer integer, long l) {

                                }
                            });
                        }else{
                            Message message = new Message();
                            message.what = MESSAGE_FROM_USER_IMAGE;
                            message.arg1 = 0;
                            handler.sendMessage(message);
                        }
                    } else {
                        Toast.makeText(context, "数据库异常", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
            Toast.makeText(context, "你的网络状况不佳", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleButtonMenu() {
        mMenuView = new SelectPopupWindow(context, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.add_love_button: {
                        Intent intent = new Intent(context, AddLoveActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.add_speak_button: {
                        Intent intent = new Intent(context, AddSpeakActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.close_select_menu_button: {
                        break;
                    }
                }
                mMenuView.dismiss();
            }
        });
        mMenuView.showAtLocation(findViewById(R.id.mainLayout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void setFooterTheme(int curNumber) {
        clearFooterTheme();
        switch (curNumber) {
            case 1: {
                loveButtonIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.love_up));
                loveButtonTitle.setTextColor(getResources().getColor(R.color.orangeRed));
                setSelectFragment(1);
                break;
            }
            case 3: {
                speakButtonIcon.setImageDrawable(getResources().getDrawable(R.drawable.speak1));
                speakButtonTitle.setTextColor(getResources().getColor(R.color.orangeRed));
                setSelectFragment(2);
                break;
            }
        }
    }

    private void setSelectFragment(int i) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (loveWallFragment != null) {
            transaction.hide(loveWallFragment);
        }
        if (speakFragment != null) {
            transaction.hide(speakFragment);
        }
        if (i == 1) {
            if (loveWallFragment == null) {
                loveWallFragment = new LoveWallFragment();
                transaction.add(R.id.main_view_pager, loveWallFragment);
            } else {
                transaction.show(loveWallFragment);
            }
        } else if (i == 2) {
            if (speakFragment == null) {
                speakFragment = new SpeakFragment();
                transaction.add(R.id.main_view_pager, speakFragment);
            } else {
                transaction.show(speakFragment);
            }
        }
        transaction.commit();
    }


    private void clearFooterTheme() {
        loveButtonIcon.setImageDrawable(getResources().getDrawable(R.drawable.love));
        speakButtonIcon.setImageDrawable(getResources().getDrawable(R.drawable.speak));
        loveButtonTitle.setTextColor(getResources().getColor(R.color.lightGray));
        speakButtonTitle.setTextColor(getResources().getColor(R.color.lightGray));
    }

    private void initView() {
        slidingMenu = (SlidingMenu) findViewById(R.id.main_menu);
        toggleTheme = (RelativeLayout) findViewById(R.id.toggle_theme);
        loveButton = (LinearLayout) findViewById(R.id.love_button_layout);
        loveButtonIcon = (ImageView) findViewById(R.id.love_button_icon);
        loveButtonTitle = (TextView) findViewById(R.id.love_button_title);
        speakButton = (LinearLayout) findViewById(R.id.speak_button);
        speakButtonIcon = (ImageView) findViewById(R.id.speak_button_icon);
        speakButtonTitle = (TextView) findViewById(R.id.speak_button_title);
        toggleAddMenuButton = (ImageView) findViewById(R.id.add_button);
        toggleMenu = (MyCircleImageView) findViewById(R.id.toggle_menu);
        toggleAccount = (RelativeLayout) findViewById(R.id.toggle_account);
        changePassWord = (RelativeLayout) findViewById(R.id.change_passWord);
        myTreeHoleForSpeak = (RelativeLayout) findViewById(R.id.my_tree_hole);
        myTreeHoleForLove = (RelativeLayout) findViewById(R.id.my_love_wall);
        nowUserImage = (MyCircleImageView) findViewById(R.id.userImage);
        nowUserName = (TextView) findViewById(R.id.userName);
        setToggleTheme(false);
        fragmentManager = getFragmentManager();
        setFooterTheme(1);
        //存储头像所在的目录
        File fileDir = new File(Environment.getExternalStorageDirectory() + "/XiYouTreeHole/ImageData/userImage");
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        //初始化广播
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LOGIN_SUCCESS");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    private void setToggleTheme(boolean type) {
        SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        int nowThemeCur = sharedPreferences.getInt("nowThemeCur", 1);
        int themeCount = 5;
        if (type) {
            nowThemeCur = nowThemeCur % themeCount + 1;
        }
        switch (nowThemeCur) {
            case 1: {
                slidingMenu.setBackgroundResource(R.drawable.background1);
                break;
            }
            case 2: {
                slidingMenu.setBackgroundResource(R.drawable.background2);
                break;
            }
            case 3: {
                slidingMenu.setBackgroundResource(R.drawable.background3);
                break;
            }
            case 4: {
                slidingMenu.setBackgroundResource(R.drawable.background4);
                break;
            }
            case 5: {
                slidingMenu.setBackgroundResource(R.drawable.background4);
                break;
            }
        }
//        Log.d(TAG, "onClick() called with: " + "now= [" + nowThemeCur + "]");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("nowThemeCur", nowThemeCur);
        editor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isAddMenuOpen) {//如果菜单在打开的状态
                isAddMenuOpen = false;
            } else if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(context, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_FROM_USER_IMAGE: {
                    if(msg.arg1 == 1){
                        String imagePath = (String) msg.obj;
                        nowUserImage.setImageURI(Uri.parse(imagePath));
                        toggleMenu.setImageURI(Uri.parse(imagePath));
                    }else{
                        nowUserImage.setImageDrawable(getResources().getDrawable(R.drawable.man));
                        toggleMenu.setImageDrawable(getResources().getDrawable(R.drawable.man));
                    }
                    break;
                }
                case MESSAGE_FROM_UP_USER_IAMGE:{
                    String phoneNumber = getSharedPreferences("LoginState", MODE_PRIVATE).
                            getString("userName", null);
                    String path = Environment.getExternalStorageDirectory() +
                            "/XiYouTreeHole/ImageData/userImage/";
                    if(msg.arg1 == 1){
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.
                                    getContentResolver(), Uri.fromFile(new File(path +
                                    phoneNumber + "userImage.jpg")));
                            nowUserImage.setImageBitmap(bitmap);
                            toggleMenu.setImageBitmap(bitmap);
                            Toast.makeText(context, "头像上传成功", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
//                            Log.e(TAG, "onActivityResult: " , e);
                        }
                    }else{
                        //头像上传失败
                        boolean flag1, flag2;
                        File oldFile = new File(path + phoneNumber + "userImage_copy.jpg");
                        flag1 = oldFile.renameTo(new File(path + phoneNumber +"userImage.jpg"));
//                        Log.d(TAG, "设置副本:" + oldFile.getAbsolutePath() + flag1);
                        Toast.makeText(context, "头像上传失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri imageUri;
        if (resultCode == RESULT_CANCELED) {
        } else if (resultCode == RESULT_OK) {//选取成功后进行裁剪
            switch (requestCode) {
                case REQUEST_CODE_PICK_IMAGE: {
                    //从图库中选择图片作为头像
                    imageUri = data.getData();
                    reSizeImage(imageUri);
                    break;
                }
                case REQUEST_CODE_CAPTURE_CAMEIA: {
                    //使用相机获取头像
                    imageUri = data.getData();
//                    Log.d(TAG, "onActivityResult: " + imageUri);
                    if (imageUri == null) {
                        //use bundle to get data
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Bitmap bitMap = (Bitmap) bundle.get("data"); //get bitmap
                            imageUri = Uri.parse(MediaStore.Images.Media.
                                    insertImage(getContentResolver(), bitMap, null, null));
//                            Log.d(TAG, "onActivityResult: bndle != null" + imageUri);
                            reSizeImage(imageUri);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                case REQUEST_RESIZE_REQUEST_CODE: {
                    //剪切图片返回
//                    Log.d(TAG, "剪切完毕：" + userImageUri);
                    if (userImageUri == null) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    } else {//截取图片完成
                        String phoneNumber = getSharedPreferences("LoginState", MODE_PRIVATE).
                                getString("userName", null);

                        //新旧文件的替换

                        String path = Environment.getExternalStorageDirectory() +
                                "/XiYouTreeHole/ImageData/userImage/";
                        File oldFile = new File(path + phoneNumber + "userImage.jpg");
                        oldFile.renameTo(new File(path + phoneNumber +"userImage_copy.jpg"));
                        File newFile = new File(path + "crop.jpg");
                        newFile.renameTo(new File(path + phoneNumber + "userImage.jpg"));

                        final BmobFile bmobFile = new BmobFile(new File(path + phoneNumber + "userImage.jpg"));
                        bmobFile.upload(new UploadFileListener() {//尝试上传头像
                            @Override
                            public void done(BmobException e) {
                                if(e == null){
                                    SignUserBaseClass user = new SignUserBaseClass();
                                    user.setObjectId(getSharedPreferences("LoginState", MODE_PRIVATE).
                                            getString("userId", null));
                                    user.setUserImage(bmobFile);
                                    user.update(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            Message message = new Message();
                                            message.what = MESSAGE_FROM_UP_USER_IAMGE;
                                            message.arg1 = (e == null ? 1: 0);
                                            handler.sendMessage(message);
                                        }
                                    });
                                }else{
                                    Message message = new Message();
                                    message.what = MESSAGE_FROM_UP_USER_IAMGE;
                                    message.arg1 = 0;
                                    handler.sendMessage(message);
                                }
                            }
                        });
                        //尝试上传更新头像

                    }
                    break;
                }
            }
        }
    }

    private void reSizeImage(Uri uri) {//重新剪裁图片的大小
//        Log.d(TAG, "尝试剪切的文件输出" + "uri = [" + uri + "]");
        File outputImage = new File(Environment.getExternalStorageDirectory() + "/XiYouTreeHole/ImageData/userImage/crop.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.getAbsoluteFile().delete();
//                Log.d(TAG, "删除 " + "uri = [" + outputImage.getAbsolutePath() + "]");
            }
            outputImage.createNewFile();
//            Log.d(TAG, "创建 " + "uri = [" + outputImage.getAbsolutePath() + "]");

        } catch (Exception e) {
            e.printStackTrace();
//            Log.d(TAG, "reSizeImage() called with: " + "uri = [" + e.toString() + "]");
        }
        userImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);//输出是X方向的比例
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高，切忌不要再改动下列数字，会卡死
        intent.putExtra("outputX", 500);//输出X方向的像素
        intent.putExtra("outputY", 500);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);//设置为不返回数据
        /**
         * 此方法返回的图片只能是小图片（测试为高宽160px的图片）
         * 故将图片保存在Uri中，调用时将Uri转换为Bitmap，此方法还可解决miui系统不能return data的问题
         */
//        intent.putExtra("return-data", true);
//        intent.putExtra("output", Uri.fromFile(new File("/mnt/sdcard/temp")));//保存路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImageUri);
//        Log.d(TAG, "reSizeImage() called with: " + "uri = [" + userImageUri + "]");
        startActivityForResult(intent, REQUEST_RESIZE_REQUEST_CODE);
    }
    class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            setNowUser();
        }
    }

}
