package com.xiaokele.huaweiview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainActivity;
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = (LinearLayout) findViewById(R.id.activity_main);
        final HuaWeiView hww = (HuaWeiView) findViewById(R.id.hww);
        hww.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int angle = random.nextInt(300)+1;
                hww.changeAngle(angle);
            }
        });
        hww.setOnAngleColorListener(new HuaWeiView.OnAngleColorListener() {
            @Override
            public void colorListener(int red, int green) {
                mainActivity.setBackgroundColor(new Color().argb(255, red, green, 0));
            }
        });
    }
}
