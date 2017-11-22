package com.kwame.android.gallery.Activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kwame.android.gallery.Adapter.AlbumAdapter;
import com.kwame.android.gallery.Adapter.RealmAlbumAdapter;
import com.kwame.android.gallery.R;
import com.kwame.android.gallery.Realm.Albums;
import com.kwame.android.gallery.Realm.RealmController;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity{
    private Realm realm;
    private RecyclerView mRecyclerView;
    private AlbumAdapter albumAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Gallery");
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        this.realm = RealmController.with(this).getRealm();

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);

        albumAdapter = new AlbumAdapter(this);
        mRecyclerView.setAdapter(albumAdapter);

        RealmController.with(this).refresh();

        setRealmAdapter(RealmController.with(this).getAlbums());

    }

    public void setRealmAdapter(RealmResults<Albums> albums) {
        RealmAlbumAdapter realmAdapter = new RealmAlbumAdapter(this.getApplicationContext(), albums, true);
        // Set the data and tell the RecyclerView to draw
        albumAdapter.setRealmAdapter(realmAdapter);
        albumAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
       super.onResume();
       albumAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_album_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_album:
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View promptView = layoutInflater.inflate(R.layout.prompt, null);

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setView(promptView);
                alert.setTitle("Add Album");

                final EditText albumName = (EditText)promptView.findViewById(R.id.album_name);
                alert.setCancelable(false)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Albums album = new Albums();
                                album.setId((int) (RealmController.getInstance().getAlbums().size() + System.currentTimeMillis()));
                                album.setName(albumName.getText().toString());
                                //album.setPath(null);

                               if (albumName.getText().toString().equals(" ") || albumName.getText().toString().equals(null) || albumName.getText().toString().equals("")) {
                                   Toast.makeText(MainActivity.this, "Album Name Cannot be Empty", Toast.LENGTH_SHORT).show();
                               }else {
                                   realm.beginTransaction();
                                   realm.copyToRealm(album);
                                   realm.commitTransaction();

                                   albumAdapter.notifyDataSetChanged();
                               }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
