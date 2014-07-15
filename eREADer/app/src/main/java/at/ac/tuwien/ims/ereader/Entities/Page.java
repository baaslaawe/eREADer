package at.ac.tuwien.ims.ereader.Entities;

/**
 * Created by Flo on 14.07.2014.
 */
public class Page {
    private int id;
    private Chapter chapter;
    private int page_nr;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter_id) {
        this.chapter = chapter;
    }

    public int getPage_nr() {
        return page_nr;
    }

    public void setPage_nr(int page_nr) {
        this.page_nr = page_nr;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return "Page "+page_nr+" ("+chapter.getHeading()+")";
    }
}
