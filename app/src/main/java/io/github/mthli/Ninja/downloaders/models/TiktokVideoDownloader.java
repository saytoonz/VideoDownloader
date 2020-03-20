package io.github.mthli.Ninja.downloaders.models;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Looper;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import io.github.mthli.Ninja.downloaders.databases.DBHelper;
import io.github.mthli.Ninja.downloaders.interfaces.VideoDownloader;

import static io.github.mthli.Ninja.Activity.BrowserActivity.openHistoryFragment;

public class TiktokVideoDownloader implements VideoDownloader {

    private Context context;
    private String VideoURL;
    private String VideoTitle;

    public TiktokVideoDownloader(Context context, String videoURL) {
        this.context = context;
        VideoURL = videoURL;
    }

    @Override
    public String createDirectory() {
        return null;
    }

    @Override
    public String getVideoId(String link) {
        if (!link.contains("https")) {
            link = link.replace("http", "https");
        }
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

                StringBuilder buffer = new StringBuilder();
                String Line;
                while ((Line = reader.readLine()) != null) {
                    //Log.e("Hello", Line);
                    if (Line.contains("videoData")) {
                        Line = Line.substring(Line.indexOf("videoData"));
                        //Log.e("Hello",Line);
                        Line = Line.substring(Line.indexOf("urls"));
                        //Log.e("Hello",Line);
                        VideoTitle = Line.substring(Line.indexOf("text"));
                        if (VideoTitle.contains("#")) {
                            VideoTitle = VideoTitle.substring(ordinalIndexOf(VideoTitle, "\"", 1) + 1, ordinalIndexOf(VideoTitle, "#", 0));
                        } else {
                            VideoTitle = VideoTitle.substring(ordinalIndexOf(VideoTitle, "\"", 1) + 1, ordinalIndexOf(VideoTitle, "\"", 2));
                        }
                        //Log.e("HelloTitle",VideoTitle);
                        Line = Line.substring(ordinalIndexOf(Line, "\"", 1) + 1, ordinalIndexOf(Line, "\"", 2));
                        //Log.e("HelloURL",Line);
                        if (!Line.contains("https")) {
                            Line = Line.replace("http", "https");
                        }
                        buffer.append(Line);
                        break;
                    }
                }
                return buffer.toString();
            } catch (IOException e) {
                return "Invalid Video URL or Check Internet Connection";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (URLUtil.isValidUrl(s)) {
                if (VideoTitle == null || VideoTitle.equals("")) {
                    VideoTitle = "TiktokVideo" + new Date().toString() + ".mp4";
                } else {
                    VideoTitle = VideoTitle + ".mp4";
                }

                try {

                    String fileSize = new Date().toString();
//                    try {
//                        URLConnection connection = new URL(s).openConnection();
//                        final int length = connection.getContentLength();
//                        if (length > 0)
//                            fileSize = String.valueOf(length);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    DBHelper dbHelper = new DBHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put("url", VideoURL);
                    values.put("link", s);
                    values.put("title", VideoTitle);
                    values.put("media_type", "video");
                    values.put("downloaded", "no");
                    values.put("local_location", "");
                    values.put("link_type", "tik");
                    values.put("time", fileSize);
                    long newRowId = db.insert("link_lists", null, values);

                    openHistoryFragment();

                } catch (Exception e) {
                    if (Looper.myLooper() == null)
                        Looper.prepare();
                    Toast.makeText(context, "Video Can't be downloaded! Try Again", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            } else {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
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
