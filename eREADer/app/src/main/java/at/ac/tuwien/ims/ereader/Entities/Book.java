package at.ac.tuwien.ims.ereader.Entities;

import java.util.List;

/**
 * Created by Flo on 04.07.2014.
 */
public class Book {
    private long id;
    private String title;
    private String author;
    private Language language;

    public Book(long id, String title, String author, Language language) {
        this.language = language;
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public Book(String title, String author, Language language) {
        this.language = language;
        this.title = title;
        this.author = author;
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

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String toString() {
        return title+" ("+author+")";
    }
}
