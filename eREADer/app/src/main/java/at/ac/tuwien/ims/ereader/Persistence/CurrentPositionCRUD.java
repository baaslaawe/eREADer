/*
    This file is part of the eReader application.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package at.ac.tuwien.ims.ereader.Persistence;

import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;

/**
 * Interface for the CRUD methods for currentpositions.
 *
 * @author Florian Schuster
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
