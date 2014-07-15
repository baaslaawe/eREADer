package at.ac.tuwien.ims.ereader.Entities;

import java.util.List;

/**
 * Created by Flo on 04.07.2014.
 */
public class Book {
    private int id;
    private String title;
    private String author;
    private Language language;
    private List<Chapter> chapters;

    public Book() {}

    public Book(String title, String author, Language language) {
        this.title=title;
        this.author=author;
        this.language=language;
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

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent(int chap, int page) {
        return getChapters().get(chap).getPages().get(page).getContent();
    }

    public String getChapterHeading(int chap) {
        return getChapters().get(chap).getHeading();
    }

    public String toString() {
        return title+" ("+author+")";
    }
}
