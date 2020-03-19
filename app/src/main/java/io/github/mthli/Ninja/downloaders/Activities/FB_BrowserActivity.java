package io.github.mthli.Ninja.downloaders.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.downloaders.Constant;
import io.github.mthli.Ninja.downloaders.databases.DBHelper;

public class FB_BrowserActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EXAMPLE";
    @SuppressLint("StaticFieldLeak")
    private WebView webo;
    private static String URL = "https://www.facebook.com/";
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button streamButton, downloadButton, cancelButton;
    private ProgressBar progress;
    ProgressDialog mProgressDialog;
    String fileN = null;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123;
    boolean result;
    String urlString;
    Dialog main_dialog, downloadDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_b__browser);


        if (getIntent() == null){
            finish();
        }else{
            if (getIntent().hasExtra("VideoURL"))
                URL = getIntent().getStringExtra("VideoURL");
        }

        progress = findViewById(R.id.progressBar);
        progress.setVisibility(View.GONE);
        result = checkPermission();
        if (result) {
            checkFolder();
        }
        if (!isConnectingToInternet(this)) {
            Toast.makeText(this, "Please Connect to Internet", Toast.LENGTH_LONG).show();
        }
        gotoFB();

    }

    @JavascriptInterface
    public void processVideo(String str, String str2) {
        Log.e("WEBVIEWJS", "RUN");
        Log.e("WEBVIEWJS", str);
        urlString = str;
        runOnUiThread(() -> {
            // put your code to show dialog here.
            LayoutInflater dialogLayout = LayoutInflater.from(FB_BrowserActivity.this);
            View DialogView = dialogLayout.inflate(R.layout.dialog_download, null);
            main_dialog = new Dialog(FB_BrowserActivity.this, R.style.MyDialogTheme);
            main_dialog.setContentView(DialogView);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(main_dialog.getWindow().getAttributes());
            lp.width = (getResources().getDisplayMetrics().widthPixels);
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.65);
            main_dialog.getWindow().setAttributes(lp);
            streamButton = DialogView.findViewById(R.id.streamButton);
            downloadButton = DialogView.findViewById(R.id.downloadButton);
            cancelButton = DialogView.findViewById(R.id.cancelButton);
            main_dialog.setCancelable(false);
            main_dialog.setCanceledOnTouchOutside(false);
            main_dialog.show();
            streamButton.setOnClickListener(v -> {
                Toast.makeText(FB_BrowserActivity.this, "Streaming", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(FB_BrowserActivity.this, FullscreenVideoPlayer.class);
                intent.putExtra("url", urlString);
                intent.putExtra("title", webo.getTitle());
                startActivity(intent);
            });
            downloadButton.setOnClickListener(v -> {
                Toast.makeText(FB_BrowserActivity.this, "Downloading", Toast.LENGTH_SHORT).show();
                newDownload(urlString);
                main_dialog.dismiss();
            });
            cancelButton.setOnClickListener(v -> main_dialog.dismiss());
        });

    }


    public void setValue(int progress) {
        this.progress.setProgress(progress);
        if (progress >= 100) // code to be added
            this.progress.setVisibility(View.GONE);
    }

    public static boolean isConnectingToInternet(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


    public void gotoFB() {
        webo = findViewById(R.id.webViewFb);
        webo.getSettings().setJavaScriptEnabled(true);
        webo.addJavascriptInterface(this, "FBDownloader");
        webo.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setVisibility(View.VISIBLE);
                FB_BrowserActivity.this.setValue(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });
        webo.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                FB_BrowserActivity.this.webo.loadUrl("javascript:(function() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\");');}}})()");
                Log.e("WEBVIEWFIN", url);
                progress.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                FB_BrowserActivity.this.webo.loadUrl("javascript:(function prepareVideo() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;console.log(i);var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');}}})()");
                FB_BrowserActivity.this.webo.loadUrl("javascript:( window.onload=prepareVideo;)()");
            }
        });
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        cookieManager.setAcceptCookie(true);
        webo.loadUrl(URL);
    }

    public class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String videoUrl="";

        public DownloadTask(Context context) {
            this.context = context;
        }

        private NumberProgressBar bnp;

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                videoUrl = sUrl[0];
                java.net.URL url = new URL(videoUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();

                input = connection.getInputStream();
                String dir = android.os.Environment.getExternalStorageDirectory().getPath() + "/Video Downloader/";
                if (!new File(dir).exists() || !new File(dir).isDirectory()) {
                    new File(dir).mkdirs();
                }
                fileN = "VDownloader" + UUID.randomUUID().toString().substring(0, 10) + ".mp4";
//                File filename = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FBDownloader/", fileN);
//                output = new FileOutputStream(filename);
                output = new FileOutputStream(dir + fileN);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            LayoutInflater dialogLayout = LayoutInflater.from(FB_BrowserActivity.this);
            View DialogView = dialogLayout.inflate(R.layout.progress_dialog, null);
            downloadDialog = new Dialog(FB_BrowserActivity.this, R.style.MyDialogTheme);
            downloadDialog.setContentView(DialogView);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(downloadDialog.getWindow().getAttributes());
            lp.width = (getResources().getDisplayMetrics().widthPixels);
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.65);
            downloadDialog.getWindow().setAttributes(lp);

            final Button cancel = DialogView.findViewById(R.id.cancel_btn);
            cancel.setOnClickListener(v -> {
                cancel(true);
                downloadDialog.dismiss();

            });

            downloadDialog.setCancelable(false);
            downloadDialog.setCanceledOnTouchOutside(false);
            bnp = DialogView.findViewById(R.id.number_progress_bar);
            bnp.setProgress(0);
            bnp.setMax(100);
            downloadDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            bnp.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            downloadDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();

                String fileSize = new Date().toString();
                DBHelper dbHelper = new DBHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("url", webo.getUrl());
                values.put("link", videoUrl);
                values.put("title", webo.getTitle());
                values.put("media_type", "video");
                values.put("downloaded", "yes");
                values.put("local_location", fileN);
                values.put("link_type", "fb");
                values.put("time", fileSize);
                long newRowId = db.insert("link_lists", null, values);
            }
                MediaScannerConnection.scanFile(FB_BrowserActivity.this,
                        new String[]{Environment.getExternalStorageDirectory().getAbsolutePath() + "/FBDownloader/" + fileN}, null,
                        (newpath, newuri) -> {
                            Log.i("ExternalStorage", "Scanned " + newpath + ":");
                            Log.i("ExternalStorage", "-> uri=" + newuri);
                        });

        }
    }

    public void newDownload(String url) {
        final DownloadTask downloadTask = new DownloadTask(FB_BrowserActivity.this);
        downloadTask.execute(url);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(FB_BrowserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(FB_BrowserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(FB_BrowserActivity.this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Write Storage permission is necessary to Download Images and Videos!!!");
                    alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) ->
                            ActivityCompat.requestPermissions(FB_BrowserActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE));
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions(FB_BrowserActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void checkAgain() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(FB_BrowserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(FB_BrowserActivity.this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle("Permission necessary");
            alertBuilder.setMessage("Write Storage permission is necessary to Download Images and Videos!!!");
            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(FB_BrowserActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        } else {
            ActivityCompat.requestPermissions(FB_BrowserActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkFolder();
                } else {
                    //code for deny
                    checkAgain();
                }
                break;
        }
    }

    public void checkFolder() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FBDownloader/";
        File dir = new File(path);
        boolean isDirectoryCreated = dir.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir();
        }
        if (isDirectoryCreated) {
            // do something\
            Log.d("Folder", "Already Created");
        }
    }
}
