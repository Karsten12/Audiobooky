package com.fonsecakarsten.audiobooky;

import android.graphics.Bitmap;

/**
 * Created by Karsten on 6/16/2017.
 */

class AudioBook {
    private Bitmap coverImage;
    private String title;
    private String author;

    public void setCoverImage(Bitmap coverImage) {
        this.coverImage = coverImage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Bitmap getCoverImage() {
        return coverImage;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}