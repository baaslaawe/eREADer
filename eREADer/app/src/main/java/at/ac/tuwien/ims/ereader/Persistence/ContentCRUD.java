package at.ac.tuwien.ims.ereader.Persistence;

import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Content;

/**
 * Created by Flo on 14.07.2014.
 */
public interface ContentCRUD {
    /**
     * Inserts a new content chunk to the DB.
     *
     * @param content content chunk to add
     * @return the generated id of the inserted content chunk
     */
    public long insertContent(Content content);

    /**
     * Gets a content chunk from the DB.
     *
     * @param id id of the chapter
     * @return the chapter specified by id
     */
    public Content getContent(long id);

    /**
     * Returns all content chunks without content from a book.
     *
     * @param book_id id of the book
     * @return a list with content chunks
     */
    public List<Content> getLightweightContents(long book_id);

    /**
     * Returns all content chunks from a book.
     *
     * @param book_id id of the book
     * @return a list with content chunks
     */
    public List<Content> getContentsByBook(long book_id);

    /**
     * Deletes a content chunk from the DB.
     *
     * @param content_id id of content chunk to delete
     */
    public void deleteContent(long content_id);
}
