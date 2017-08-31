package com.hl.reddotmove;

import android.graphics.PointF;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView point = (TextView) findViewById(R.id.point);
        point.setText("10");
        point.setTag(10);
        GooViewListener listener = new GooViewListener(this,point){
            @Override
            public void onDisappear(PointF mDragCenter) {
                super.onDisappear(mDragCenter);
                Toast.makeText(MainActivity.this, "消失了", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReset(boolean isOutOfRange) {
                super.onReset(isOutOfRange);
                Toast.makeText(MainActivity.this, "重置了", Toast.LENGTH_SHORT).show();
            }

        };
        point.setOnTouchListener(listener);

    }
}
