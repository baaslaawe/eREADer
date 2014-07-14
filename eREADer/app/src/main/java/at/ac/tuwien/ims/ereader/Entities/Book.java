package at.ac.tuwien.ims.ereader.Entities;

import java.util.ArrayList;

/**
 * Created by Flo on 04.07.2014.
 */
public class Book {
    private String title;
    private String author;
    private ArrayList<String> chapters;
    private ArrayList<String> content;
    private Language language;

    public Book(String title, String author, Language language) {
        this.title=title;
        this.author=author;
        this.language=language;
    }

    public Book(String title, String author, ArrayList<String> chapters, ArrayList<String> content, Language language, String releaseDate) {
        this.title=title;
        this.author=author;
        this.chapters=chapters;
        this.content=content;
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

    public ArrayList<String> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<String> chapters) {
        this.chapters = chapters;
    }

    public ArrayList<String> getContent() {
        return content;
    }

    public void setContent(ArrayList<String> content) {
        this.content = content;
    }
}
