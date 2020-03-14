package io.github.mthli.Ninja.downloaders.history_files.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.downloaders.Activities.FullscreenVideoPlayer;
import io.github.mthli.Ninja.downloaders.Activities.ImageViewer;
import io.github.mthli.Ninja.downloaders.databases.DBHelper;
import io.github.mthli.Ninja.downloaders.history_files.models.HistoryModel;

import static io.github.mthli.Ninja.Activity.BrowserActivity.openNewUrl;
import static io.github.mthli.Ninja.Fragment.HistoryFragment.notifyAdapterDataChange;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Context context;
    private List<HistoryModel> historyModelList;

    public HistoryAdapter(Context context, List<HistoryModel> historyModelList) {
        this.context = context;
        this.historyModelList = historyModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final HistoryModel item = historyModelList.get(position);
        item.setIndexPosition(position);

        holder.title.setText(item.getTitle());
        holder.time.setText(item.getTime());
        if (item.getDownloaded().equals("no")) {
            holder.download.setImageResource(R.drawable.history_selector);
            holder.download.setOnClickListener(v -> downloadFile(item.getLink(), holder.progress_bar, item));
        } else {
            if (TextUtils.isEmpty(item.getLocal_location())) {
                holder.download.setImageResource(R.drawable.history_selector);
                holder.download.setOnClickListener(v -> downloadFile(item.getLink(), holder.progress_bar, item));
            } else {
                //Check if file exist
                //if it exist show share else show download
                if (fileExists(item.getLocal_location())) {
                    holder.download.setImageResource(R.drawable.ic_share_black_24dp);
                    holder.download.setOnClickListener(v -> {
                        shareFile(item);
                    });

                    holder.itemView.setOnClickListener(v -> {
                        String file = "file://" + Environment.getExternalStorageDirectory().getPath() +
                                "/Video Downloader/" + item.getLocal_location();
                        if (item.getMedia_type().contains("image")) {
                            Intent intent = new Intent(context, ImageViewer.class);
                            intent.putExtra("url", file);
                            intent.putExtra("title", item.getTitle());
                            context.startActivity(intent);
                        } else {
                            Intent intent = new Intent(context, FullscreenVideoPlayer.class);
                            intent.putExtra("url", file);
                            intent.putExtra("title", item.getTitle());
                            context.startActivity(intent);
                            Log.e("TAG", "onBindViewHolder: " + file);
                        }
                    });
                } else {
                    holder.download.setImageResource(R.drawable.history_selector);
                    holder.download.setOnClickListener(v -> downloadFile(item.getLink(), holder.progress_bar, item));
                }
            }
        }

        switch (item.getLink_type()) {
            case "fb":
                Glide.with(context)
                        .load(item.getLink())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_facebook))
                        .into(holder.image);

                break;
            case "twitter":
                Glide.with(context)
                        .load(item.getLink())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_twitter))
                        .into(holder.image);

                break;
            case "instagram":
                Glide.with(context)
                        .load(item.getLink())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_instagram))
                        .into(holder.image);

                break;
            case "tik":
                Glide.with(context)
                        .load(item.getLink())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_tiktok))
                        .into(holder.image);

                break;
            case "buzz":
                Glide.with(context)
                        .load(item.getLink())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_buzz))
                        .into(holder.image);

                break;
            case "pinterest":
                Glide.with(context)
                        .load(item.getLink())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_pinterest))
                        .into(holder.image);

                break;
        }


        holder.menu.setOnClickListener(v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(context, holder.menu);
            //inflating menu from xml resource
            popup.inflate(R.menu.history_menu);
            //adding click listener
            popup.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.repost_on_fb:
                        Toast.makeText(context, "Repost to FB", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.repost_on_ig:
                        Toast.makeText(context, "Repost to IG", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.share:
                        shareFile(item);
                        break;
                    case R.id.view_pic:
                        String file = "file://" + Environment.getExternalStorageDirectory().getPath() +
                                "/Video Downloader/" + item.getLocal_location();
                        Intent intent = new Intent(context, ImageViewer.class);
                        intent.putExtra("url", file);
                        intent.putExtra("title", item.getTitle());
                        context.startActivity(intent);
                        break;
                    case R.id.visit_site:
                        openNewUrl(item.getOriginalUrl());
                        break;
                    case R.id.delete:
                        AlertDialog.Builder alBuilder = new AlertDialog.Builder(context);
                        alBuilder.setMessage("Do You want to Delete "+item.getTitle()+"?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    historyModelList.remove(item.getIndexPosition());
                                    notifyDataSetChanged();
                                    deleteFileAndRow(item);
                                })
                                .setNegativeButton("No",null);
                        alBuilder.setCancelable(true);
                        alBuilder.show();
                        break;
                }
                return false;
            });
            //displaying the popup
            popup.show();
        });
    }

    private void deleteFileAndRow(HistoryModel item) {
        DBHelper dbHelper  = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection =  "id LIKE ?";
        String[] selectionArgs = {item.getId()};
        int deletedRows = db.delete("link_lists", selection, selectionArgs);

        if (!item.getDownloaded().equals("no")) {
            if (!TextUtils.isEmpty(item.getLocal_location())) {
                if (fileExists(item.getLocal_location())) {
                    String url = Environment.getExternalStorageDirectory().getPath() +
                            "/Video Downloader/" + item.getLocal_location();
                    File file = new File(url);
                    if (file.exists())
                        file.delete();
                }
            }
        }


    }

    private void shareFile(HistoryModel item) {
        String file = Environment.getExternalStorageDirectory().getPath() +
                "/Video Downloader/" + item.getLocal_location();
        final Uri uri = Uri.parse(file);
        String title = "Share video file";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("video/*");
        if (item.getMedia_type().contains("image")) {
            title = "Share image file";
            share.setType("image/*");
        }
        share.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(share, title));

    }


    private void downloadFile(String link, ProgressBar progress_bar, HistoryModel item) {
        new DownloadTask(context, progress_bar, item).execute(link);
    }

    private boolean fileExists(String filename) {
        String file = android.os.Environment.getExternalStorageDirectory().getPath() + "/Video Downloader/" + filename;
        File f = new File(file);
        return f != null && f.exists();
    }

    @Override
    public int getItemCount() {
        return historyModelList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private ProgressBar progress_bar;
        private TextView time;
        private ImageView download;
        private ImageView menu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            progress_bar = itemView.findViewById(R.id.progress_bar);
            time = itemView.findViewById(R.id.time);
            download = itemView.findViewById(R.id.download);
            menu = itemView.findViewById(R.id.menu);
        }
    }
}


class DownloadTask extends AsyncTask<String, Integer, String> {
    private Context context;
    private ProgressBar progress_bar;
    private HistoryModel historyModel;
    private String filename;

    DownloadTask(Context context, ProgressBar progress_bar, HistoryModel item) {
        this.context = context;
        this.progress_bar = progress_bar;
        this.historyModel = item;

        if (item.getMedia_type().equals("video"))
            this.filename = System.currentTimeMillis() + ".mp4";
        else
            this.filename = System.currentTimeMillis() + ".png";


    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progress_bar.setVisibility(View.VISIBLE);
    }


    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {

            URL url = new URL(sUrl[0]);

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
            output = new FileOutputStream(dir + filename);

            byte[] data = new byte[4096];
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
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progress_bar.setIndeterminate(false);
        progress_bar.setMax(100);
        progress_bar.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(context, " Error! Couldn't download...", Toast.LENGTH_SHORT).show();
        } else {
            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("local_location", filename);
            values.put("downloaded", "yes");
            values.put("time", new Date().toString());

            String selection = "id" + " LIKE ?";
            String[] selectionArgs = {historyModel.getId()};

            int count = db.update("link_lists", values, selection, selectionArgs);
            progress_bar.setVisibility(View.GONE);
            historyModel.setLocal_location(filename);
            historyModel.setDownloaded("yes");
            notifyAdapterDataChange();
            MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory().toString()},
                    null, (path, uri) -> {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    });

        }
    }
}
