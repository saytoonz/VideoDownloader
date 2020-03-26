package io.github.mthli.Ninja.downloaders.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import io.github.mthli.Ninja.downloaders.databases.DBHelper;
import io.github.mthli.Ninja.downloaders.interfaces.VideoDownloader;

import static io.github.mthli.Ninja.Activity.BrowserActivity.openHistoryFragment;
import static io.github.mthli.Ninja.Activity.BrowserActivity.openNewUrl;


public class FbVideoDownloader implements VideoDownloader {

    private Context context;
    private String VideoURL;
    private String VideoTitle;

    public FbVideoDownloader(Context context, String videoURL) {
        this.context = context;
        VideoURL = videoURL;
    }

    @Override
    public String createDirectory() {
        return "";
    }

    @Override
    public String getVideoId(String link) {
        return link;
    }

    @Override
    public void DownloadVideo() {
        new Data().execute(getVideoId(VideoURL));
    }

    @SuppressLint("StaticFieldLeak")
    private class Data extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection;
            BufferedReader reader;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String buffer = "No URL";
                String Line;
                while ((Line = reader.readLine()) != null) {
                    if (Line.contains("og:video:url")) {
                        Line = Line.substring(Line.indexOf("og:video:url"));
                        if (Line.contains("og:title")) {
                            VideoTitle = Line.substring(Line.indexOf("og:title"));
                            VideoTitle = VideoTitle.substring(ordinalIndexOf(VideoTitle, "\"", 1) + 1, ordinalIndexOf(VideoTitle, "\"", 2));
                        }
                        Line = Line.substring(ordinalIndexOf(Line, "\"", 1) + 1, ordinalIndexOf(Line, "\"", 2));
                        if (Line.contains("amp;")) {
                            Line = Line.replace("amp;", "");
                        }
                        if (!Line.contains("https")) {
                            Line = Line.replace("http", "https");
                        }
                        buffer = Line;
                        break;
                    } else {
                        buffer = "No URL";
                    }
                }
                return buffer;
            } catch (IOException e) {
                return "No URL";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (!s.contains("No URL")) {
                if (VideoTitle == null || VideoTitle.equals("")) {
                    VideoTitle = "fbVideo" + new Date().toString();
                } else {
                    VideoTitle = VideoTitle + ".mp4";
                }

                String fileSize = new Date().toString();
                DBHelper dbHelper = new DBHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("url", VideoURL);
                values.put("link", s);
                values.put("title", VideoTitle);
                values.put("media_type", "video");
                values.put("downloaded", "no");
                values.put("local_location", "");
                values.put("link_type", "fb");
                values.put("time", fileSize);
                long newRowId = db.insert("link_lists", null, values);
                openHistoryFragment();

            } else {
                Toast.makeText(context, "Opening url in FB browser....", Toast.LENGTH_SHORT).show();
                openNewUrl(VideoURL);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (Looper.myLooper() == null)
                Looper.prepare();
            Toast.makeText(context, "Video Can't be downloaded! Try Again", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    private static int ordinalIndexOf(String str, String substr, int n) {
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }
}
