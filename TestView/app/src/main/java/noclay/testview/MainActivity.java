package noclay.testview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.list;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class MainActivity extends AppCompatActivity{

    private ListView listView;
    ArrayList<Map<String,String>> data ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initView();
    }

//    private void initView() {
//        listView = (ListView) findViewById(R.id.list_view);
//        data = new ArrayList<>();
//        Map<String, String> item;
//        for(int i = 0; i < 50; i ++){
//            item = new HashMap<>();
//            item.put("测试", "测试" + i);
//            data.add(item);
//        }
//
//        listView.setAdapter(new SimpleAdapter(MainActivity.this,
//                data,
//                android.R.layout.simple_list_item_1,
//                new String[] {"测试"},
//                new int[] {android.R.id.text1}
//                ));
//    }

}
