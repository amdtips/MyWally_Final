package com.myhutao.mywally;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvUri;
    private ActivityResultLauncher<String[]> pickVideoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPick = findViewById(R.id.btnPick);
        Button btnSet = findViewById(R.id.btnSetWallpaper);
        tvUri = findViewById(R.id.tvUri);

        // restore saved URI
        String saved = getSharedPreferences("mywally_prefs", Context.MODE_PRIVATE)
                .getString("video_uri", null);
        tvUri.setText(saved != null ? saved : "Belum ada video terpilih");

        pickVideoLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            // persist permission
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            getSharedPreferences("mywally_prefs", Context.MODE_PRIVATE).edit()
                                    .putString("video_uri", uri.toString()).apply();
                            tvUri.setText(uri.toString());
                        }
                    }
                });

        btnPick.setOnClickListener(v -> {
            pickVideoLauncher.launch(new String[]{"video/*"});
        });

        btnSet.setOnClickListener(v -> {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, VideoWallpaperService.class));
            startActivity(intent);
        });
    }
}
