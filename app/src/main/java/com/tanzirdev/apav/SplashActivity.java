package com.tanzirdev.apav;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash);
        View logo = findViewById(R.id.logo);
        logo.setScaleX(0.5f); logo.setScaleY(0.5f); logo.setAlpha(0f);
        logo.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(800).setInterpolator(new OvershootInterpolator()).start();
        for (int id : new int[]{R.id.name, R.id.tag}) {
            View v = findViewById(id); v.setAlpha(0f); v.setTranslationY(30f);
            v.animate().alpha(1f).translationY(0f).setStartDelay(500).setDuration(600).start();
        }
        logo.postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 1900);
    }
}
