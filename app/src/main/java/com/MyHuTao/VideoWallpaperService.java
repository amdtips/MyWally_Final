package com.myhutao.mywally;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.FileDescriptor;

public class VideoWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine(this);
    }

    private static class VideoEngine extends Engine implements SurfaceHolder.Callback,
            MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

        private final Context ctx;
        private MediaPlayer mediaPlayer;
        private SurfaceHolder holder;
        private final SharedPreferences prefs;

        VideoEngine(Context context) {
            super();
            this.ctx = context;
            prefs = ctx.getSharedPreferences("mywally_prefs", Context.MODE_PRIVATE);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.holder = surfaceHolder;
            surfaceHolder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            preparePlayer();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releasePlayer();
        }

        private void preparePlayer() {
            String uriStr = prefs.getString("video_uri", null);
            if (uriStr == null) return;
            try {
                Uri uri = Uri.parse(uriStr);
                releasePlayer();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setLooping(true);
                mediaPlayer.setSurface(holder.getSurface());
                # open asset file descriptor safely
                try (android.content.res.AssetFileDescriptor afd = ctx.getContentResolver().openAssetFileDescriptor(uri, "r")) {
                    if (afd != null) {
                        FileDescriptor fd = afd.getFileDescriptor();
                        mediaPlayer.setDataSource(fd, afd.getStartOffset(), afd.getLength());
                        afd.close();
                        mediaPlayer.prepareAsync();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) mediaPlayer.start();
            } else {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.seekTo(0);
            mp.start();
        }

        private void releasePlayer() {
            try { if (mediaPlayer != null) mediaPlayer.stop(); } catch (Exception ignored) {}
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            releasePlayer();
        }
    }
}
