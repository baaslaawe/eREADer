package at.ac.tuwien.ims.ereader.Persistence;

import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Chapter;

/**
 * Created by Flo on 14.07.2014.
 */
public interface ChapterCRUD {
    /**
     * Inserts a new chapter to the DB.
     *
     * @param chapter chapter to add
     * @return the generated id of the inserted chapter
     */
    public long insertChapter(Chapter chapter);

    /**
     * Gets a chapter from the DB.
     *
     * @param id id of the chapter
     * @return the chapter specified by id
     */
    public Chapter getChapter(long id);

    /**
     * Returns all chapters without content from a book.
     *
     * @param book_id id of the book
     * @return a list with chapters
     */
    public List<Chapter> getLightweightChapters(long book_id);

    /**
     * Returns all chapters from a book.
     *
     * @param book_id id of the book
     * @return a list with chapters
     */
    public List<Chapter> getChaptersByBook(long book_id);

    /**
     * Deletes a chapter from the DB (including all its pages).
     *
     * @param chapter_id id of chapter to delete
     */
    public void deleteChapter(long chapter_id);
}
