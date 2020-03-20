package io.github.mthli.Ninja.downloaders.services;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Objects;

import io.github.mthli.Ninja.downloaders.Activities.InstagramDownloaderActivity;
import io.github.mthli.Ninja.downloaders.Activities.TwitterDownloaderActivity;
import io.github.mthli.Ninja.downloaders.models.FbVideoDownloader;
import io.github.mthli.Ninja.downloaders.models.InstagramVideoDownloader;
import io.github.mthli.Ninja.downloaders.models.PinterestDownloader;
import io.github.mthli.Ninja.downloaders.models.TiktokVideoDownloader;
import io.github.mthli.Ninja.downloaders.models.TopBuzzDownloader;
import io.github.mthli.Ninja.downloaders.models.TwitterVideoDownloader;

import static io.github.mthli.Ninja.Activity.BrowserActivity.openBrowserShowHistory;

public class BackgroundService extends Service {
    private Handler handler;
    private Runnable runnable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleStart(intent, startId);
        return START_STICKY;
    }


    private void handleStart(Intent intent, int startId) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    ClipData clip = Objects.requireNonNull(getSystemService(ClipboardManager.class)).getPrimaryClip();
                    if (clip != null) {
                        String clipboard = clip.getItemAt(0).getText().toString();
                        checkIfLink(clipboard);
                    }
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 6000);
    }

    private void checkIfLink(String clipboard) {
        if (URLUtil.isValidUrl(clipboard)) {
            if (itsInstagramLink(clipboard)) {
                InstagramVideoDownloader downloader = new InstagramVideoDownloader(getApplicationContext(), clipboard);
                downloader.DownloadVideo();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("a", clipboard).apply();
                openHistory();
                Toast.makeText(getApplicationContext(), "Parsing an Instagram Url...", Toast.LENGTH_SHORT).show();

            } else if (itsTwitterLink(clipboard)) {
                TwitterVideoDownloader downloader = new TwitterVideoDownloader(getApplicationContext(), clipboard);
                downloader.DownloadVideo();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("a", clipboard).apply();
                openHistory();
                Toast.makeText(getApplicationContext(), "Parsing a Twitter Url...", Toast.LENGTH_SHORT).show();

            } else if (itsTikTokLink(clipboard)) {
                TiktokVideoDownloader downloader = new TiktokVideoDownloader(getApplicationContext(), clipboard);
                downloader.DownloadVideo();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("a", clipboard).apply();
                openHistory();
                Toast.makeText(getApplicationContext(), "Parsing a TikTok Url...", Toast.LENGTH_SHORT).show();

            } else if (itsFacebookLink(clipboard)) {
                FbVideoDownloader downloader = new FbVideoDownloader(getApplicationContext(), clipboard);
                downloader.DownloadVideo();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("a", clipboard).apply();
                openHistory();
                Toast.makeText(getApplicationContext(), "Parsing a Facebook Url...", Toast.LENGTH_SHORT).show();

            } else if (itsBuzzLink(clipboard)) {
                TopBuzzDownloader downloader = new TopBuzzDownloader(getApplicationContext(), clipboard);
                downloader.DownloadVideo();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("a", clipboard).apply();
                openHistory();
                Toast.makeText(getApplicationContext(), "Parsing an BuzzVideo Url...", Toast.LENGTH_SHORT).show();

            } else if (itsPinterestLink(clipboard)) {
                PinterestDownloader downloader = new PinterestDownloader(getApplicationContext(), clipboard);
                downloader.DownloadVideo();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("a", clipboard).apply();
                openHistory();
                Toast.makeText(getApplicationContext(), "Parsing an Pinterest Url...", Toast.LENGTH_SHORT).show();

            }
            //  Log.e("TAG", "checkIfLink: " + clipboard);
        }
    }

    private boolean itsInstagramLink(String clipboard) {
        if (clipboard.contains("https://www.instagram.com/p/") ||
                clipboard.contains("https://www.instagram.com/tv/"))
            return !clipboard.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("a", "null"));

        return false;
    }

    private boolean itsTwitterLink(String clipboard) {
        if (clipboard.contains("https://twitter.com/i/status/") ||
                (clipboard.contains("https://twitter.com/") && clipboard.contains("/status/")))
            return !clipboard.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("a", "null"));
        return false;
    }

    private boolean itsTikTokLink(String clipboard) {
        if (clipboard.contains("https://vm.tiktok.com/") || clipboard.contains("https://m.tiktok.com/") ||
                (clipboard.contains("https://www.tiktok.com/") && clipboard.contains("/video/")))
            return !clipboard.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("a", "null"));
        return false;
    }

    private boolean itsFacebookLink(String clipboard) {
        if (clipboard.contains("https://www.facebook.com/") || clipboard.contains("https://m.facebook.com/") ||
                clipboard.contains("https://web.facebook.com/"))
            return !clipboard.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("a", "null"));
        return false;//https://m.facebook.com/182243201819458/posts/2678074388902981/?app=fbl
    }

    private boolean itsBuzzLink(String clipboard) {
        if ((clipboard.contains("://va.topbuzz.com/al/") && clipboard.contains("http")) ||
                (clipboard.contains("://www.topbuzz.com/a/") && clipboard.contains("http")))
            return !clipboard.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("a", "null"));
        return false;
    }

    private boolean itsPinterestLink(String clipboard) {
        if ((clipboard.contains("://pin.it/") && clipboard.contains("http")) ||
                (clipboard.contains("https://www.pinterest.com/") && clipboard.contains("/sent/")))
            return !clipboard.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("a", "null"));
        return false;
    }


    private void openHistory() {
        openBrowserShowHistory(getApplicationContext());
    }


}
