package com.kwame.android.gallery.Adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kwame.android.gallery.Activity.AlbumImagesActivity;
import com.kwame.android.gallery.R;
import com.kwame.android.gallery.Realm.Albums;
import com.kwame.android.gallery.Realm.RealmController;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Kwame on 10/14/2017.
 */
public class AlbumAdapter extends RealmRecyclerViewAdapter<Albums> {

    private Realm realm;
    private Context mContext;
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String POSITION = "position";
    private static final String EXTRAS = "extras";

    public AlbumAdapter(Context context) {
        mContext = context;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        realm = RealmController.getInstance().getRealm();

        final Albums albums = getItem(position);
        final AlbumViewHolder holder = (AlbumViewHolder)viewHolder;

        holder.name.setText(albums.getName());
        System.out.println("path image"+albums.getPath());
        if(albums.getPath().equals("")) {
            holder.image.setImageResource(R.drawable.placeholder);
        }else {
            holder.image.setImageURI(Uri.parse(albums.getPath()));
        }

       //item click
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString(ID, String.valueOf(albums.getId()));
                args.putString(NAME, albums.getName());
                args.putString(POSITION, String.valueOf(position));
                Intent intent = new Intent(mContext,AlbumImagesActivity.class);
                intent.putExtra(EXTRAS, args);
                mContext.startActivity(intent);
            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopPopupMenu(holder.overflow, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(getRealmAdapter() != null)
            return getRealmAdapter().getCount();
        return 0;
    }

    private void shopPopupMenu(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.album_option_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                        alertDialog.setTitle("Are you sure?");
                        alertDialog.setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        RealmResults<Albums> results = realm.where(Albums.class).findAll();
                                        realm.beginTransaction();
                                        results.remove(position);
                                        realm.commitTransaction();
                                        notifyDataSetChanged();
                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).create().show();
                        return true;

                    case R.id.rename:
                        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                        View prompt = layoutInflater.inflate(R.layout.prompt, null);
                        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        alert.setView(prompt);
                        alert.setTitle("Rename");

                        Albums albums = getItem(position);
                        final EditText name = (EditText)prompt.findViewById(R.id.album_name);
                        name.setText(albums.getName());

                        alert.setCancelable(false)
                                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (name.getText().toString().equals(" ") || name.getText().toString().equals(null) || name.getText().toString().equals("")) {
                                            Toast.makeText(mContext, "Please  Enter a name", Toast.LENGTH_SHORT).show();
                                        }else {

                                            RealmResults<Albums> results = realm.where(Albums.class).findAll(); //query all albums in db
                                            realm.beginTransaction(); //begin transaction
                                            results.get(position).setName(name.getText().toString()); //get new album name and store in db
                                            realm.commitTransaction();

                                            notifyDataSetChanged();

                                        }
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).create().show();
                        return true;
                    case R.id.share :
                        Albums albumItem =  getItem(position);

                        JSONParser parser = new JSONParser();
                        File txt = new File(Environment.getExternalStorageDirectory()+"/Albums/"+albumItem.getId(), albumItem.getName()+albumItem.getId()+".txt");
                        String lat = null, lng = null;

                        try {
                            Object object = parser.parse(new FileReader(txt));

                            JSONObject jsonObject = (JSONObject)object;
                            lat = (String)jsonObject.get("lat");
                            lng = (String)jsonObject.get("lng");

                        } catch (IOException e) {

                            e.printStackTrace();

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        ArrayList<Uri> images = new ArrayList<>();
                        File[] image = new File(Environment.getExternalStorageDirectory()+"/Albums/"+albumItem.getId()).listFiles();

                        for(int i=0; i<image.length; i++) {
                            if(image != null) {
                                if(String.valueOf(image[i]).endsWith(".txt")) {
                                    continue;
                                }else {
                                    images.add(Uri.parse(image[i].getAbsolutePath()));
                                }
                            }
                        }

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        shareIntent.setType("image/*");

                        shareIntent.putExtra(Intent.EXTRA_TEXT, "latitude: "+lat+"\nlongitude: "+lng);
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, images);
                        mContext.startActivity(Intent.createChooser(shareIntent, "Share via"));

                        return true;
                    default:

                }
                return false;
            }
        });

        popupMenu.show();

    }


    private class AlbumViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView name;
        private CardView cardView;
        private ImageButton overflow;

        AlbumViewHolder(View itemView){
            super(itemView);
            image = (ImageView)itemView.findViewById(R.id.image);
            name = (TextView)itemView.findViewById(R.id.name);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            overflow = (ImageButton)itemView.findViewById(R.id.show_options);

        }
    }
}
