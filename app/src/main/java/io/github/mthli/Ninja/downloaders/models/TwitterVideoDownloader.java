package io.github.mthli.Ninja.downloaders.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONObject;

import java.util.Date;

import io.github.mthli.Ninja.downloaders.databases.DBHelper;
import io.github.mthli.Ninja.downloaders.interfaces.VideoDownloader;

import static io.github.mthli.Ninja.Activity.BrowserActivity.openHistoryFragment;

public class TwitterVideoDownloader implements VideoDownloader {

    private Context context;
    private String VideoURL;
    private String VideoTitle;

    public TwitterVideoDownloader(Context context, String videoURL) {
        this.context = context;
        VideoURL = videoURL;
    }

    @Override
    public String createDirectory() {
        return null;
    }

    @Override
    public String getVideoId(String link) {
        if (link.contains("?")) {
            link = link.substring(link.indexOf("status"));
            link = link.substring(link.indexOf("/") + 1, link.indexOf("?"));
        } else {
            link = link.substring(link.indexOf("status"));
            link = link.substring(link.indexOf("/") + 1);
        }
        return link;
    }

    @Override
    public void DownloadVideo() {
        AndroidNetworking.post("https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php")
                .addBodyParameter("id", getVideoId(VideoURL))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.e("Hello", response.toString());
                        String URL = response.toString();
                        if (URL.contains("url")) {
                            URL = URL.substring(URL.indexOf("url"));
                            URL = URL.substring(ordinalIndexOf(URL, "\"", 1) + 1, ordinalIndexOf(URL, "\"", 2));
                            if (URL.contains("\\")) {
                                URL = URL.replace("\\", "");
                            }
                            //Log.e("HelloURL",URL);
                            if (URLUtil.isValidUrl(URL)) {
                                if (VideoTitle == null || VideoTitle.equals("")) {
                                    VideoTitle = "TwitterVideo" + new Date().toString();
                                } else {
                                    VideoTitle = VideoTitle + ".mp4";
                                }
                                try {
                                    String fileSize = new Date().toString();
                                    DBHelper dbHelper = new DBHelper(context);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                                    ContentValues values = new ContentValues();
                                    values.put("url", VideoURL);
                                    values.put("link", URL);
                                    values.put("media_type", "video");
                                    values.put("title", VideoTitle);
                                    values.put("downloaded", "no");
                                    values.put("local_location", "");
                                    values.put("link_type", "twitter");
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
                                Toast.makeText(context, "No Video Found", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } else {
                            if (Looper.myLooper() == null)
                                Looper.prepare();
                            Toast.makeText(context, "No Video Found", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (Looper.myLooper() == null)
                            Looper.prepare();
                        Toast.makeText(context, "Invalid Video URL", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });
    }

    private static int ordinalIndexOf(String str, String substr, int n) {
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }
}
