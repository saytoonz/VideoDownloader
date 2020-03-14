package io.github.mthli.Ninja.downloaders.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.github.mthli.Ninja.R;

public class ImageViewer extends AppCompatActivity {

    private String url;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        PhotoView photoView = findViewById(R.id.photo_viewer);
        FloatingActionButton share_image = findViewById(R.id.share_image);
        if (getIntent() == null) {
            Toast.makeText(this, "There was an error...", Toast.LENGTH_SHORT).show();
            finish();
        } else if (getIntent().hasExtra("url")) {
            url = getIntent().getStringExtra("url");
            uri = Uri.parse(url);
        }

        if (!TextUtils.isEmpty(url)) {
            Glide.with(this)
                    .load(uri)
                    .into(photoView);
        } else {
            Toast.makeText(this, "There was an error...", Toast.LENGTH_SHORT).show();
            finish();
        }


        share_image.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            String title = "Share image file";
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, title));
        });

    }
}
