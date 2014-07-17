package at.ac.tuwien.ims.ereader.Entities;

import java.util.List;

/**
 * Created by Flo on 14.07.2014.
 */
public class Chapter {
    private long id;
    private Book book;
    private String heading;
    private int chapter_nr;

    public Chapter(long id, Book book, String heading, int chapter_nr) {
        this.id = id;
        this.book = book;
        this.heading = heading;
        this.chapter_nr=chapter_nr;
    }

    public Chapter(Book book, String heading, int chapter_nr) {
        this.book = book;
        this.heading = heading;
        this.chapter_nr=chapter_nr;
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

    public int getChapter_nr() {
        return chapter_nr;
    }

    public void setChapter_nr(int chapter_nr) {
        this.chapter_nr = chapter_nr;
    }
}
