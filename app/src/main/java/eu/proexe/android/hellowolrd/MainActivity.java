package eu.proexe.android.hellowolrd;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import eu.proexe.android.hellowolrd.ff.RefreshLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RefreshLayout refresh = (RefreshLayout)findViewById(R.id.refresh);
        (refresh).setOnStartRefreshingListener(() -> Log.d("XDDD","XD~~~~"));

        findViewById(R.id.buttonUp).setOnClickListener(v -> {
            refresh.startRefreshing();
        });
        findViewById(R.id.buttonDown).setOnClickListener(v -> {
            refresh.stopRefreshing();
        });
        findViewById(R.id.buttonDownIm).setOnClickListener(v -> {
            refresh.stopRefreshingImmediately();
        });

        new Handler().postDelayed((Runnable) () -> {
            refresh.startRefreshing();
        }, 1100);

        new Handler().postDelayed((Runnable) () -> {
            refresh.stopRefreshing();
        }, 1400);


    }
}