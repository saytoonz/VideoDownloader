package io.github.mthli.Ninja.downloaders.models;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;

import io.github.mthli.Ninja.downloaders.databases.DBHelper;

import static android.content.Context.DOWNLOAD_SERVICE;
import static io.github.mthli.Ninja.Activity.BrowserActivity.openHistoryFragment;
import static io.github.mthli.Ninja.downloaders.Activities.InstagramDownloaderActivity.instagramRunning;
import static io.github.mthli.Ninja.downloaders.Activities.PinterestDownloaderActivity.pinterestRunning;

public class PinterestDownloader {

    private Context context;
    private String FinalURL;
    private String VideoURL;
    private String VideoTitle;
//    String[] strings = new String[]{"736x", "60x60", "474x", "170x", "600x315", "564x", "236x", "136x136", "orig"};
//    String[] videoStrings = new String[]{"V_720P", "V_HLSV4", "V_HLSV3_WEB", "V_HLSV3_MOBILE", "orig"};
    String[] strings = new String[]{"orig"};
    String[] videoStrings = new String[]{"V_720P", "V_HLSV4", "V_HLSV3_WEB", "V_HLSV3_MOBILE", "orig"};

    public PinterestDownloader(Context context, String videoURL) {
        this.context = context;
        VideoURL = videoURL;
    }

    public void DownloadVideo() {
        new Data().execute(getVideoId(VideoURL));
    }

    private String createDirectory() {
        return "";
    }

    private String getVideoId(String link) {
        return link;
    }

    private class Data extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... parm) {
            if (parm[0].length() != 0) {

                try {
                    String tempURL = getRedirectUrl(parm[0]);
                    String realUrl = tempURL.substring(0, tempURL.indexOf("sent"));
                    Document doc = Jsoup.connect(realUrl).get();
                    Element elements = doc.getElementById("initial-state");
                    JSONArray array = new JSONObject(elements.data()).getJSONArray("resourceResponses");
                    JSONObject imagesObj = null;
                    JSONObject videosObj = null;


                    try {
                        imagesObj = array.getJSONObject(0).getJSONObject("response").getJSONObject("data").getJSONObject("images");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        videosObj = array.getJSONObject(0).getJSONObject("response")
                                .getJSONObject("data").getJSONObject("videos").getJSONObject("video_list");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (videosObj != null) {
                        String[] pinterest = getPinterset(videosObj.getJSONObject("V_720P"), "pinterest video");
                        if (pinterest != null) {
                            Log.e("TAG", "doInBackground: " + Arrays.toString(pinterest));
                            if (pinterest[0].contains(".mp4")) {
                                publishProgress(pinterest);
                            } else {
                                Toast.makeText(context, "No good format identified for this video...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    if (imagesObj != null) {
                        for (String string : strings) {
                            String[] pinterest = getPinterset(imagesObj.getJSONObject(string), "pinterest image");
                            publishProgress(pinterest);
                        }
                    }


                    return null;

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            return "No URL";
        }


        @Override
        protected void onProgressUpdate(String... s) {
            super.onProgressUpdate(s);
            FinalURL = s[0];
            if (FinalURL != null || FinalURL != "No URL") {
                    VideoTitle = s[1] + "x" + s[2] + " " + s[4] + " " + s[3];
                try {

                    String fileSize = new Date().toString();
                    DBHelper dbHelper = new DBHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put("url", VideoURL);
                    values.put("link", s[0]);
                    values.put("title", VideoTitle);
                    values.put("media_type", s[s.length - 1]);
                    values.put("downloaded", "no");
                    values.put("local_location", "");
                    values.put("link_type", "pinterest");
                    values.put("time", fileSize);
                    long newRowId = db.insert("link_lists", null, values);

                } catch (Exception e) {
                    if (Looper.myLooper() == null)
                        Looper.prepare();
                    Toast.makeText(context, "Video Can't be downloaded! Try Again", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            } else {
                Toast.makeText(context, "Video Can't be downloaded!", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
                if (pinterestRunning){
                    ((Activity) context).finish();
                    openHistoryFragment();
                }

        }

    }




    private String[] getPinterset(JSONObject value, String type) {
        try {
            String duration = "";
            String url = value.getString("url");
            String width = value.optString("width");
            String height = value.optString("height");
            if (value.has("duration")) {
                duration = value.getString("duration");
            }
            return new String[]{
                    url,
                    width,
                    height,
                    duration,
                    type
            };
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    private String getRedirectUrl(String path) {
        String url = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            url = conn.getHeaderField("Location");
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }
}
