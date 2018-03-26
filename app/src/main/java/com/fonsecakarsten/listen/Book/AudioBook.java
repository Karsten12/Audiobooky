package com.fonsecakarsten.listen.Book;

import java.io.Serializable;

/**
 * Created by Karsten on 6/16/2017.
 * Audiobook template
 */

class AudioBook implements Serializable {
    private String title;
    private String author;
    private String coverImagePath;

    private String subtitle;
    private String publisher;
    private String absolutePath;
    private String publishDate;
    private String description;
    private int rating;
    private String ISBN;

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getSubtitle() {
        return subtitle;
    }

    void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    String getISBN() {
        return ISBN;
    }

    void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    String getPublishDate() {
        return publishDate;
    }

    void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    String getPublisher() {
        return publisher;
    }

    void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    int getRating() {
        return rating;
    }

    void setRating(int rating) {
        this.rating = rating;
    }
}