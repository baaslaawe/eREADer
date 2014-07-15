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
     */
    public void insertChapter(Chapter chapter);

    /**
     * Gets a chapter from the DB.
     *
     * @param id id of the chapter
     * @return the chapter specified by id
     */
    public Chapter getChapter(int id);

    /**
     * Returns all chapters from a book.
     *
     * @param book_id id of the book
     * @return a list with chapters
     */
    public List<Chapter> getChaptersByBook(int book_id);

    /**
     * Deletes a chapter from the DB including its pages.
     *
     * @param chapter chapter to delete
     */
    public void deleteChapter(Chapter chapter);
}
