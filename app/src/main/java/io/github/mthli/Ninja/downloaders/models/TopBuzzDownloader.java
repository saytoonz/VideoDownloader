package io.github.mthli.Ninja.downloaders.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import io.github.mthli.Ninja.downloaders.databases.DBHelper;

import static io.github.mthli.Ninja.Activity.BrowserActivity.openHistoryFragment;

public class TopBuzzDownloader {

    private Context context;
    private String FinalURL;
    private String VideoURL;
    private String VideoTitle;

    public TopBuzzDownloader(Context context, String videoURL) {
        this.context = context;
        VideoURL = videoURL;
    }

    public void DownloadVideo() {
        new Data().execute(getVideoId(VideoURL));
    }

    private String getVideoId(String link) {
        return link;
    }

    private class Data extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... parm) {
            BufferedReader reader = null;
            try {
                URL url = new URL(parm[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String Line = "";
                while ((Line = reader.readLine()) != null) {
                    if (Line.contains("INITIAL_STATE")) {
                        if (Line.contains("title")) {
                            Line = Line.substring(Line.indexOf("videoTitle"));
                            VideoTitle = Line.substring(ordinalIndexOf(Line, "\"", 1) + 1, ordinalIndexOf(Line, "\\", 2));
                            if (Line.contains("480p")) {
                                Line = Line.substring(Line.indexOf("480p"));
                                Line = Line.substring(Line.indexOf("main_url"));
                                Line = Line.substring(ordinalIndexOf(Line, "\"", 1) + 1, ordinalIndexOf(Line, "\"", 2));
                                Line = Line.replace("u002F", "");
                                if (Line.contains("http")) {
                                    Line = Line.replace("http", "https");
                                }
                                buffer.append(Line);
                            } else {
                                buffer.append("No URL");
                            }
                        }
                    }
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            FinalURL = o;
            if (FinalURL != null || FinalURL != "No URL") {
                String fileSize = new Date().toString();
                FinalURL = FinalURL.replaceAll("[^-a-zA-Z0-9:?=&.%\\s]", "/");
                DBHelper dbHelper = new DBHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("url", VideoURL);
                values.put("link", FinalURL);
                values.put("media_type", "video");
                values.put("title", VideoTitle);
                values.put("downloaded", "no");
                values.put("local_location", "");
                values.put("link_type", "buzz");
                values.put("time", fileSize);
                long newRowId = db.insert("link_lists", null, values);

                openHistoryFragment();


            } else {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                Toast.makeText(context, "Video Can't be downloaded!", Toast.LENGTH_SHORT).show();
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
