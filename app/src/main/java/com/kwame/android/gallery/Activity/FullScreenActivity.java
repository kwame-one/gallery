package com.kwame.android.gallery.Activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kwame.android.gallery.R;

import java.io.File;

/**
 * Created by Kwame on 11/6/2017.
 */

public class FullScreenActivity extends AppCompatActivity {

    private static final String IMAGE_PATH = "image";
    private static final String IMG_EXTRAS = "extras";
    private ImageView mImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_fullscreen);
        mImageView = (ImageView)findViewById(R.id.image);

        Bundle bundle = getIntent().getBundleExtra(IMG_EXTRAS);
        System.out.println("Image path "+bundle.getString(IMAGE_PATH));

        Glide.with(this)
                .load(new File(bundle.getString(IMAGE_PATH)))
                .into(mImageView);
    }
}
