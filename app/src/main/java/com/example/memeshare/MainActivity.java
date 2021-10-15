package com.example.memeshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    String s;

    /* access modifiers changed from: protected */
    @Override // androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nextImage();
    }

    public void nextImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.memeImage);
        final ProgressBar progressB = (ProgressBar) findViewById(R.id.progress);
        Volley.newRequestQueue(this).add(new JsonObjectRequest(0, "https://meme-api.herokuapp.com/gimme", null, new Response.Listener<JSONObject>() {

            public void onResponse(JSONObject response) {
                progressB.setVisibility(View.GONE);
                try {
                    Picasso.get().load(response.getString("url")).into(imageView);
                    MainActivity.this.s = response.getString("url");
                    Log.d("url", MainActivity.this.s);
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
        Intent sendIntent = new Intent();
        sendIntent.setAction("android.intent.action.SEND");
        sendIntent.putExtra("android.intent.extra.TEXT", this.s);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }
}