package com.example.paint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {
    CostomView game_view;
    TextView levelView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setContentView(R.layout.activity_main);
        game_view = findViewById(R.id.drawing);
        game_view.init();
        levelView = findViewById(R.id.levelView);
    }

    public void onClick(View view) {
        levelView.setText(getString(R.string.level_text, game_view.level));
        game_view.wait_new_game = true;
    }

}
