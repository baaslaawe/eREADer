package at.ac.tuwien.ims.ereader.Persistence;

import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;

/**
 * Created by Flo on 16.07.2014.
 */
public interface CurrentPositionCRUD {

    /**
     * Inserts a new current position to the DB.
     *
     * @param curr current position to insert
     * @return id of the inserted current position
     */
    public long insertCurrentPosition(CurrentPosition curr);

    /**
     * Updates a current position in the DB.
     *
     * @param curr current position to update
     */
    public void updateCurrentPosition(CurrentPosition curr);

    /**
     * Gets a current position from the DB.
     *
     * @param book_id id of the book of current position
     * @return current position
     */
    public CurrentPosition getCurrentPosition(long book_id);

    /**
     * Deletes a current position from the DB.
     *
     * @param book_id id of the book of current position to delete
     */
    public void deleteCurrentPosition(long book_id);
}
