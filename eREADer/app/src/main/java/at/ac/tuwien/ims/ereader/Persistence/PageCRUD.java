package at.ac.tuwien.ims.ereader.Persistence;

import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Page;

/**
 * Created by Flo on 14.07.2014.
 */
public interface PageCRUD {
    /**
     * Inserts a new page to the DB.
     *
     * @param page page to add
     * @return the generated id of the inserted page
     */
    public long insertPage(Page page);

    /**
     * Gets a page from the DB.
     *
     * @param id id of the page
     * @return the page specified by id
     */
    public Page getPage(long id);

    /**
     * Returns all the pages from a certain chapter.
     *
     * @param chapter_id id of the chapter
     * @return a list with pages
     */
    public List<Page> getPagesByChapter(long chapter_id);

    /**
     * Deletes a page from the DB.
     *
     * @param page_id id of page to delete
     */
    public void deletePage(long page_id);
}
