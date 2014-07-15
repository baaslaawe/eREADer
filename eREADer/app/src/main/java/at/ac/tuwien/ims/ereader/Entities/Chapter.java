package at.ac.tuwien.ims.ereader.Entities;

import java.util.List;

/**
 * Created by Flo on 14.07.2014.
 */
public class Chapter {
    private int id;
    private Book book;
    private int chapter_nr;
    private String heading;
    private List<Page> pages;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book_id) {
        this.book = book;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChapter_nr() {
        return chapter_nr;
    }

    public void setChapter_nr(int chapter_nr) {
        this.chapter_nr = chapter_nr;
    }

    public String toString() {
        return heading+" ("+book.getTitle()+")";
    }
}
