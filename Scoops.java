package com.scoopsoup.model;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class Scoops {
    @Id
    public ObjectId _id;



    public Integer id;
    public String title;
    public String author;
    public String text;
    public String label;
    public String news_poster;



    // Constructors
    public Scoops() {}

    public Scoops(ObjectId _id, Integer id, String title, String author, String text, String label, String news_poster) {
        this._id = _id;
        this.id = id;
        this.title = title;
        this.author = author;
        this.text = text;
        this.label = label;
        this.news_poster = news_poster;
    }

    // ObjectId needs to be converted to string
    public String get_id() { return _id.toHexString(); }
    public void set_id(ObjectId _id) { this._id = _id; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNews_poster() {
        return news_poster;
    }

    public void setNews_poster(String news_poster) {
        this.news_poster = news_poster;
    }
}
