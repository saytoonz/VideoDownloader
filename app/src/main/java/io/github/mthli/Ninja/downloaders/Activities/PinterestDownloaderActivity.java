package io.github.mthli.Ninja.downloaders.Activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.downloaders.models.FbVideoDownloader;
import io.github.mthli.Ninja.downloaders.models.InstagramVideoDownloader;
import io.github.mthli.Ninja.downloaders.models.PinterestDownloader;
import io.github.mthli.Ninja.downloaders.models.TiktokVideoDownloader;
import io.github.mthli.Ninja.downloaders.models.TopBuzzDownloader;
import io.github.mthli.Ninja.downloaders.models.TwitterVideoDownloader;


public class PinterestDownloaderActivity extends AppCompatActivity {

    EditText inputURl;
    Button BtnDownload;
    Button BtnPaste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinterest_downloader);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        inputURl = findViewById(R.id.content_url);
        BtnDownload = findViewById(R.id.check_url);
        BtnPaste = findViewById(R.id.paste_url);


        BtnDownload.setOnClickListener(v -> {
            final String URL = inputURl.getText().toString();
            if (!TextUtils.isEmpty(URL)) {
                checkIfLink(URL);
            }
        });


        BtnPaste.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                ClipData clip = Objects.requireNonNull(getSystemService(ClipboardManager.class)).getPrimaryClip();
                if (clip != null) {
                    inputURl.setText(clip.getItemAt(0).getText());
                }
            }
        });
    }


    private void checkIfLink(String url) {
        if (URLUtil.isValidUrl(url)) {

            inputURl.setText("");

            if (itsInstagramLink(url)) {
                InstagramVideoDownloader downloader = new InstagramVideoDownloader(getApplicationContext(), url);
                downloader.DownloadVideo();
                Toast.makeText(this, "Parsing an Instagram Url...", Toast.LENGTH_SHORT).show();
                finish();

            } else if (itsTwitterLink(url)) {
                TwitterVideoDownloader downloader = new TwitterVideoDownloader(getApplicationContext(), url);
                downloader.DownloadVideo();
                Toast.makeText(this, "Parsing a Twitter Url...", Toast.LENGTH_SHORT).show();
                finish();

            } else if (itsTikTokLink(url)) {
                TiktokVideoDownloader downloader = new TiktokVideoDownloader(getApplicationContext(), url);
                downloader.DownloadVideo();
                Toast.makeText(this, "Parsing a Tiktok Url...", Toast.LENGTH_SHORT).show();
                finish();

            } else if (itsFacebookLink(url)) {
                FbVideoDownloader downloader = new FbVideoDownloader(getApplicationContext(), url);
                downloader.DownloadVideo();
                Toast.makeText(this, "Parsing a Facebook Url...", Toast.LENGTH_SHORT).show();
                finish();

            } else if (itsBuzzLink(url)) {
                TopBuzzDownloader downloader = new TopBuzzDownloader(getApplicationContext(), url);
                downloader.DownloadVideo();
                Toast.makeText(this, "Parsing a BuzzVideo Url...", Toast.LENGTH_SHORT).show();
                finish();

            } else if (itsPinterestLink(url)) {
                PinterestDownloader downloader = new PinterestDownloader(getApplicationContext(), url);
                downloader.DownloadVideo();
                Toast.makeText(this, "Parsing a Pinterest Url...", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean itsInstagramLink(String clipboard) {
        return (clipboard.contains("https://www.instagram.com/p/") ||
                clipboard.contains("https://www.instagram.com/tv/"));
    }

    private boolean itsTwitterLink(String clipboard) {
        return clipboard.contains("https://twitter.com/i/status/") ||
                (clipboard.contains("https://twitter.com/") && clipboard.contains("/status/"));
    }

    private boolean itsTikTokLink(String clipboard) {
        return clipboard.contains("https://vm.tiktok.com/") || clipboard.contains("https://m.tiktok.com/") ||
                (clipboard.contains("https://www.tiktok.com/") && clipboard.contains("/video/"));
    }

    private boolean itsFacebookLink(String clipboard) {
        return clipboard.contains("https://www.facebook.com/") ||
                clipboard.contains("https://web.facebook.com/") ||
                clipboard.contains("https://m.facebook.com/");
    }

    private boolean itsBuzzLink(String clipboard) {
        return (clipboard.contains("://va.topbuzz.com/al/") && clipboard.contains("http")) ||
                (clipboard.contains("://www.topbuzz.com/a/") && clipboard.contains("http"));
    }

    private boolean itsPinterestLink(String clipboard) {
        return (clipboard.contains("://pin.it/") && clipboard.contains("http")) ||
                (clipboard.contains("https://www.pinterest.com/") && clipboard.contains("/sent/"));
    }

}
