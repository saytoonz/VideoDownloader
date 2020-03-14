package io.github.mthli.Ninja.downloaders.models;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import io.github.mthli.Ninja.downloaders.databases.DBHelper;
import io.github.mthli.Ninja.downloaders.interfaces.VideoDownloader;

import static android.content.Context.DOWNLOAD_SERVICE;
import static io.github.mthli.Ninja.Activity.BrowserActivity.openHistoryFragment;
import static io.github.mthli.Ninja.downloaders.Activities.InstagramDownloaderActivity.instagramRunning;

public class InstagramVideoDownloader implements VideoDownloader {

    private Context context;
    private String VideoURL;

    public InstagramVideoDownloader(Context context, String videoURL) {
        this.context = context;
        VideoURL = videoURL;
    }

    @Override
    public String createDirectory() {
        return null;
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
            String full_url = strings[0];
            if (full_url != null) {

                int index = full_url.lastIndexOf("/");
                if (index > 0) {
                    full_url = full_url.substring(0, index + 1);
                }


                String URL = full_url + "?__a=1";
                if (URL.contains("://www.instagram.com/")) {
                    HttpHandler sh = new HttpHandler();
                    String jsonStr = sh.makeServiceCall(URL);

                    Log.e("InstagramVideo", "Response from url: " + jsonStr);

                    if (jsonStr != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(jsonStr);

                            String caption = "";
                            String video_url = "";

                            JSONObject graphql = jsonObj.getJSONObject("graphql");
                            JSONObject shortcode_media = graphql.getJSONObject("shortcode_media");
                            String display_url = shortcode_media.getString("display_url");
                            String is_video = shortcode_media.getString("is_video");

                            if (is_video.equals("true")) {
                                video_url = shortcode_media.getString("video_url");
                            }

                            JSONObject edge_media_to_caption = shortcode_media.getJSONObject("edge_media_to_caption");
                            JSONArray edges_caption = edge_media_to_caption.getJSONArray("edges");
                            if (edges_caption != null && edges_caption.length() > 0) {
                                caption = edges_caption
                                        .getJSONObject(0)
                                        .getJSONObject("node")
                                        .getString("text");
                            }

                            JSONObject edge_web_media_to_related_media = shortcode_media.getJSONObject("edge_web_media_to_related_media");
                            JSONArray edges = edge_web_media_to_related_media.getJSONArray("edges");

                            JSONArray edges1 = null;
                            try {
                                JSONObject edge_sidecar_to_children = shortcode_media.getJSONObject("edge_sidecar_to_children");
                                edges1 = edge_sidecar_to_children.getJSONArray("edges");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //Checks for multiple posts
                            if (edges != null && edges.length() > 0) {

                                for (int i = 0; i < edges.length(); i++) {

                                    JSONObject node = edges.getJSONObject(i).getJSONObject("node");

                                    String edge_display_url = node.getString("display_url");
                                    String edge_is_video = node.getString("is_video");

                                    String edge_video_url = "";
                                    if (edge_is_video.equals("true")) {
                                        edge_video_url = node.getString("video_url");
                                        publishProgress(edge_video_url, "video", caption);
                                    } else {
                                        publishProgress(edge_display_url, "image", caption);
                                    }

                                }

                            } else if (edges1 != null && edges1.length() > 0) {

                                for (int i = 0; i < edges1.length(); i++) {

                                    JSONObject node = edges1.getJSONObject(i).getJSONObject("node");

                                    String edge_display_url = node.getString("display_url");
                                    String edge_is_video = node.getString("is_video");

                                    String edge_video_url = "";
                                    if (edge_is_video.equals("true")) {
                                        edge_video_url = node.getString("video_url");
                                        publishProgress(edge_video_url, "video", caption);
                                    } else {
                                        publishProgress(edge_display_url, "image", caption);
                                    }
                                }

                            } else {
                                if (is_video.equals("true")) {
                                    publishProgress(video_url, "video", caption);
                                } else {
                                    publishProgress(display_url, "image", caption);
                                }
                            }


                            return "Done";

                        } catch (final JSONException e) {
                            Log.e("InstagramVideo", "Json parsing error: " + e.getMessage());
                            return "No URL";
                        }
                    } else {
                        Log.e("InstagramVideo", "Couldn't get json from server.");
                        return "No URL";
                    }

                } else {
                    return "No URL";
                }
            } else {
                return "No URL";
            }

        }


        @Override
        public void onProgressUpdate(String... s) {
            super.onProgressUpdate(s);
            String VideoTitle = "";
            String fileSize = new Date().toString();
            if (!s[0].contains("No URL")) {
                if (s[2] == null || s[2].equals("")) {
                    VideoTitle = "InstaVideo" + new Date().toString() + ".mp4";
                } else {
                    VideoTitle = s[2];
                }
                try {
                    try {
                        final URLConnection connection = new URL(s[0]).openConnection();
                        final int length = connection.getContentLength();
                        if (length > 0)
                            fileSize = String.valueOf(length);
                    } catch (Exception ignored) {
                    }

                    DBHelper dbHelper = new DBHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put("url", VideoURL);
                    values.put("link", s[0]);
                    values.put("title", VideoTitle);
                    values.put("media_type", s[1]);
                    values.put("downloaded", "no");
                    values.put("local_location", "");
                    values.put("link_type", "instagram");
                    values.put("time", fileSize);
                    long newRowId = db.insert("link_lists", null, values);

                } catch (Exception e) {
                    if (Looper.myLooper() == null)
                        Looper.prepare();
                    Toast.makeText(context, "Video Can't be downloaded! Try Again", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            } else {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                Toast.makeText(context, "Wrong Video URL or Private Video Can't be downloaded", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.contains("No URL")) {
                if (instagramRunning) {
                    ((Activity) context).finish();
                    openHistoryFragment();
                }
            } else {
                Toast.makeText(context, "Unknow Error....", Toast.LENGTH_SHORT).show();
            }

        }
    }

}


class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();

    HttpHandler() {
    }

    String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

