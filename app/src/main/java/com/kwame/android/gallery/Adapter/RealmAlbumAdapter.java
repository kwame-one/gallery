package com.kwame.android.gallery.Adapter;

import android.content.Context;

import com.kwame.android.gallery.Realm.Albums;

import io.realm.RealmResults;

/**
 * Created by Kwame on 10/16/2017.
 */

public class RealmAlbumAdapter extends RealmModelAdapter<Albums> {

    public RealmAlbumAdapter(Context context, RealmResults<Albums> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }
}
