package com.fonsecakarsten.audiobooky;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Karsten on 6/16/2017.
 * Audiobook template
 */

class AudioBook implements Serializable {
    private String coverImagePath;
    private String title;
    private String subtitle;
    private String author;
    private String description;
    private String publisher;
    private String publishDate;
    private String ISBN;
    private ArrayList<String> pageText = new ArrayList<>();

    void setPageText(String newPage) {
        pageText.add(newPage);
    }

    String getCoverImagePath() {
        return coverImagePath;
    }

    void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    String getTitle() {
        if (subtitle != null) {
            return title + ": " + subtitle;
        }
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    String getAuthor() {
        return author;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    public String getPageText(int page) {
        return pageText.get(page);
    }

//    public int getChapters() {
//        return null
//    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}