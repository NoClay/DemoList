package noclay.treehole3.FragmentCollect;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/19.
 */
public class LoveWallFragment extends Fragment {
    private TextView leftLove, rightLove;
    private View loveWallFragment;
    private LoveWallRightChildFragment loveWallRightChildFragment;
    private LoveWallLeftChildFragment loveWallLeftChildFragment;
    private FragmentManager fragmentManager;
    private static final String TAG = "LoveWallFragment";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loveWallFragment = inflater.inflate(R.layout.fragment_love_wall_activity_layout, container, false);
        initView();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.left_love:{
                        clearChoosedState();
                        leftLove.setTextColor(getResources().getColor(R.color.skyBlue));
                        leftLove.setBackgroundColor(getResources().getColor(R.color.white));
                        setFragmentSelect(1);
                        break;
                    }
                    case R.id.right_love:{
                        clearChoosedState();
                        rightLove.setTextColor(getResources().getColor(R.color.skyBlue));
                        rightLove.setBackgroundColor(getResources().getColor(R.color.white));
                        setFragmentSelect(2);
                        break;
                    }
                }
            }
        };
        leftLove.setOnClickListener(listener);
        rightLove.setOnClickListener(listener);
        return loveWallFragment;
    }

    private void setFragmentSelect(int i) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
//        Log.d(TAG, "setFragmentSelect() called with: " + "i = [" + i + "]");
        switch (i) {
            case 1: {
                if(loveWallLeftChildFragment == null) {
                    loveWallLeftChildFragment = new LoveWallLeftChildFragment();
                    transaction.add(R.id.love_wall_view_pager, loveWallLeftChildFragment);
                }
                else{
                    transaction.show(loveWallLeftChildFragment);
                }
                break;
            }
            case 2: {
                if (loveWallRightChildFragment == null){
                    loveWallRightChildFragment = new LoveWallRightChildFragment();
                    transaction.add(R.id.love_wall_view_pager, loveWallRightChildFragment);
                }
                else{
                    transaction.show(loveWallRightChildFragment);
                }
                break;
            }
        }
        transaction.commit();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if(loveWallLeftChildFragment != null){
            transaction.hide(loveWallLeftChildFragment);
        }
        if(loveWallRightChildFragment != null){
            transaction.hide(loveWallRightChildFragment);
        }
    }

    private void clearChoosedState() {
        leftLove.setTextColor(getResources().getColor(R.color.lightGray));
        rightLove.setTextColor(getResources().getColor(R.color.lightGray));
        leftLove.setBackgroundColor(getResources().getColor(R.color.lightWhite));
        rightLove.setBackgroundColor(getResources().getColor(R.color.lightWhite));
    }

    private void initView() {
        leftLove = (TextView) loveWallFragment.findViewById(R.id.left_love);
        rightLove = (TextView) loveWallFragment.findViewById(R.id.right_love);
        fragmentManager = getFragmentManager();
        setFragmentSelect(1);
    }
}
