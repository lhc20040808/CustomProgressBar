package com.lhc.customprogressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private CircleProgressView mCircleProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleProgress = findViewById(R.id.view_progress);
        mCircleProgress.addNode(new CircleProgressView.Node("低风险"));
        mCircleProgress.addNode(new CircleProgressView.Node("1风险"));
        mCircleProgress.addNode(new CircleProgressView.Node("2风险"));
        mCircleProgress.addNode(new CircleProgressView.Node("3风险"));
        mCircleProgress.addNode(new CircleProgressView.Node("4风险"));
        mCircleProgress.addNode(new CircleProgressView.Node("5风险"));
        mCircleProgress.addNode(new CircleProgressView.Node("中高风险"));
        mCircleProgress.draw();
    }
}
