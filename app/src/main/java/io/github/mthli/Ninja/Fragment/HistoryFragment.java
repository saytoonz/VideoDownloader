package io.github.mthli.Ninja.Fragment;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.downloaders.databases.DBHelper;
import io.github.mthli.Ninja.downloaders.history_files.adapters.HistoryAdapter;
import io.github.mthli.Ninja.downloaders.history_files.models.HistoryModel;

public class HistoryFragment extends Fragment {

    private Handler handler;
    private Runnable runnable;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    DBHelper dbHelper;
    private static HistoryAdapter adapter;
    List<HistoryModel> historyModelList = new ArrayList<>();

    public static void notifyAdapterDataChange(){
        adapter.notifyDataSetChanged();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerView = root.findViewById(R.id.recycler);
        toolbar = root.findViewById(R.id.toolbar);
        dbHelper = new DBHelper(getActivity());
        historyModelList = getDownloadLinks();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new HistoryAdapter(getActivity(), historyModelList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                notifyAdapterDataChange();
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 6000);
        return root;
    }

    private List<HistoryModel> getDownloadLinks() {

        SQLiteDatabase db =dbHelper.getReadableDatabase();
        String[] projection = {"id", "url", "link", "media_type", "title", "downloaded", "local_location","time","link_type"};
        String sortOrder = "id DESC";
        Cursor cursor =
                db.query("link_lists", projection, null, null, null, null, sortOrder);

        List<HistoryModel> list = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String originalUrl = cursor.getString(cursor.getColumnIndexOrThrow("url"));
            String link = cursor.getString(cursor.getColumnIndexOrThrow("link"));
            String media_type = cursor.getString(cursor.getColumnIndexOrThrow("media_type"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String downloaded = cursor.getString(cursor.getColumnIndexOrThrow("downloaded"));
            String local_location = cursor.getString(cursor.getColumnIndexOrThrow("local_location"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            String link_type = cursor.getString(cursor.getColumnIndexOrThrow("link_type"));

            list.add(new HistoryModel(String.valueOf(itemId), originalUrl, link, media_type, link_type, downloaded, title, local_location,time));
        }
        cursor.close();

        return list;

    }


    @Override
    public void onDestroy() {
        dbHelper.close();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }


}
