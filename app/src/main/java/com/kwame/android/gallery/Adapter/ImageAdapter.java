package com.kwame.android.gallery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kwame.android.gallery.Activity.FullScreenActivity;
import com.kwame.android.gallery.Interface.ItemCallback;
import com.kwame.android.gallery.Model.Image;
import com.kwame.android.gallery.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kwame on 10/18/2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>{
    private List<Image> images;
    private Context context;


    public ImageAdapter(Context context, List<Image> images) {
        this.images = images;
        this.context = context;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Image image = images.get(position);

        Glide.with(context)
                .load(new File(image.getImagePath()))
                .into(holder.photo);


    }

    @Override
    public int getItemCount() {
        return images.size();
    }





    public class ImageViewHolder extends RecyclerView.ViewHolder{
        private ImageView photo;


        public ImageViewHolder(View itemView) {
            super(itemView);
            photo = (ImageView)itemView.findViewById(R.id.image);

        }


    }
}
