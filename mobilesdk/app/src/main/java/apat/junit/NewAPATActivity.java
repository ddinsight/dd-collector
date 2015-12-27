package apat.junit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.ddinsight.AirplugAnalyticTracker;

public class NewAPATActivity extends Activity{
    public final static String TAG = "NewAPATActivity";

    private Button btnTrack;
    private Button btnDispatch;
    private Button btnDeleteAll;

	public static AirplugAnalyticTracker tracker;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // init component
        initComponent();

		// get tracker singleton instance
		tracker = AirplugAnalyticTracker.getInstance();

		// start
        tracker.start("apman@airplug.com", "1001", -1, false, this, "logserver.example.com", 80, false);
    }

    void initComponent(){
        btnTrack = (Button)findViewById(R.id.btnTrack);
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // track anyting(Java Object) you want
                AATLog aatLog = new AATLog("StartWars", 120);
                tracker.trackEvent("AT", aatLog);
            }
        });

        btnDispatch = (Button)findViewById(R.id.btnDispatch);
        btnDispatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dispatch tacked log
                tracker.dispatch();
            }
        });

        btnDeleteAll = (Button)findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.deleteLogAll();
            }
        });

    }
}