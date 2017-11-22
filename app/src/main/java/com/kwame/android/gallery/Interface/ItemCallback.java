package com.kwame.android.gallery.Interface;

import android.view.View;

/**
 * Created by Kwame on 11/6/2017.
 */

public interface ItemCallback {
    void onItemClick(View view, int p);

    void onLongItemClick(View view, int p);
}
