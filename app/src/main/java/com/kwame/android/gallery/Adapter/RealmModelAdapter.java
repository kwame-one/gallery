package com.kwame.android.gallery.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Kwame on 10/16/2017.
 */

public class RealmModelAdapter<T extends RealmObject> extends RealmBaseAdapter<T> {


    public RealmModelAdapter(Context context, RealmResults<T> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
