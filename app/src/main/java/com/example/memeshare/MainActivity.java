package com.example.memeshare;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net
        .Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net
        .HttpURLConnection;
import java.net
        .MalformedURLException;
import java.net
        .URL;

public class MainActivity extends AppCompatActivity {
    String img_url;
    public static final int PERMISSION_WRITE = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        nextImage();
    }

    public void nextImage() {
        final ImageView imageView = findViewById(R.id.memeImage);
        final ProgressBar progressB = findViewById(R.id.progress);
        Volley.newRequestQueue(this).add(new JsonObjectRequest(0, "https://meme-api.herokuapp.com/gimme ", null, new Response.Listener<JSONObject>() {

        public void onResponse(JSONObject response) {
            progressB.setVisibility(View.GONE);
            try {
                Glide.with(MainActivity.this).load(response.getString("url"))
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background))
                        .into(imageView);

                img_url = response.getString("url");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d("meme4u", "Something went wrong");
        }
    }));
}

    public void nextMeme(View view) {
        ((ProgressBar) findViewById(R.id.progress)).setVisibility(View.VISIBLE);
        nextImage();
    }

    public void shareMeme(View view) {
        if (checkPermission()) {
            Bitmap bitmap =getBitmapFromURL(img_url);
            shareImage(bitmap);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void shareImage(Bitmap bitmap) {
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        ContentValues newImageDetails = new ContentValues();
        String displayName = "Image." + System.currentTimeMillis() + ".gif";
        newImageDetails.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        Uri imageContentUri = contentResolver.insert(contentUri, newImageDetails);

        try (ParcelFileDescriptor fileDescriptor = contentResolver.openFileDescriptor(imageContentUri, "w", null)) {
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            OutputStream outputStream = new FileOutputStream(fd);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            Log.e("TAG", "Error saving bitmap", e);
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, imageContentUri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "MemeShare App created by Shubham Singh " + img_url);
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("image/*");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share with");
        startActivity(shareIntent);
    }

    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if ((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }
    }
}
