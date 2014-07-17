package at.ac.tuwien.ims.ereader.Entities;

/**
 * Created by Flo on 16.07.2014.
 */
public class CurrentPosition {
    private long book_id;
    private int currentChapterInBook;
    private int currentPageInChapter;
    private int currentSentence;

    public CurrentPosition(long book_id, int currentChapterInBook, int currentPageInChapter, int currentSentence) {
        this.book_id = book_id;
        this.currentChapterInBook = currentChapterInBook;
        this.currentPageInChapter = currentPageInChapter;
        this.currentSentence = currentSentence;
    }

    public long getBook_id() {
        return book_id;
    }

    public void setBook_id(long book_id) {
        this.book_id = book_id;
    }

    public int getCurrentChapter() {
        return currentChapterInBook;
    }

    public void setCurrentChapter(int currentChapterInBook) {
        this.currentChapterInBook = currentChapterInBook;
    }

    public int getCurrentPage() {
        return currentPageInChapter;
    }

    public void setCurrentPage(int currentPageInChapter) {
        this.currentPageInChapter = currentPageInChapter;
    }

    public int getCurrentSentence() {
        return currentSentence;
    }

    public void setCurrentSentence(int currentSentence) {
        this.currentSentence = currentSentence;
    }

    @Override
    public String toString() {
        return "CurrentPosition{" +
                "book_id=" + book_id +
                ", currentChapterInBook=" + currentChapterInBook +
                ", currentPageInChapter=" + currentPageInChapter +
                ", currentSentence=" + currentSentence +
                '}';
    }
}
