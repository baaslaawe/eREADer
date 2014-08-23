package at.ac.tuwien.ims.ereader.Entities;

import java.util.List;

/**
 * Created by Flo on 14.07.2014.
 */
public class Content {
    private long id;
    private Book book;
    private String heading;
    private String content;
    private int words;

    public Content(long id, Book book, String heading, String content, int words) {
        this.id = id;
        this.book = book;
        this.heading = heading;
        this.content = content;
        this.words = words;
    }

    public Content(long id, Book book, String heading, int words) { //lightweight for displaying in toc
        this.id = id;
        this.book = book;
        this.heading = heading;
        this.words = words;
    }

    public Content(Book book, String heading, String content, int words) {
        this.book = book;
        this.heading = heading;
        this.content = content;
        this.words = words;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String toString() {
        return heading+" ("+book.getTitle()+")";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }
}
