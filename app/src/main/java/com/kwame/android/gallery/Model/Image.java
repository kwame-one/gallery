package com.kwame.android.gallery.Model;

/**
 * Created by Kwame on 10/18/2017.
 */

public class Image {
    private String imagePath;
    private int image;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public Image(String imagePath) {
        this.imagePath = imagePath;

    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}
