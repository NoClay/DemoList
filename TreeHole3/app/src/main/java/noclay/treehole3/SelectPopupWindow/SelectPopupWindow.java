package noclay.treehole3.SelectPopupWindow;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.w3c.dom.Text;

import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/22.
 */
public class SelectPopupWindow extends PopupWindow {
    private View mMenuView;
    private TextView addLoveButton, addSpeakButton;
    private ImageView closeSelectMenuButton;

    public SelectPopupWindow(Context context, View.OnClickListener itemOnClick) {
        super(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = layoutInflater.inflate(R.layout.select_window_layout, null);
        this.setContentView(mMenuView);


        addLoveButton = (TextView) mMenuView.findViewById(R.id.add_love_button);
        addSpeakButton = (TextView) mMenuView.findViewById(R.id.add_speak_button);
        closeSelectMenuButton = (ImageView) mMenuView.findViewById(R.id.close_select_menu_button);

        addSpeakButton.setOnClickListener(itemOnClick);
        addLoveButton.setOnClickListener(itemOnClick);
        closeSelectMenuButton.setOnClickListener(itemOnClick);


        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dialog_window_background));
//        this.getBackground().setAlpha(200);
//        ColorDrawable dw = new ColorDrawable(0xc8ffffff);
//        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.PopupAnimation);

    }


}
