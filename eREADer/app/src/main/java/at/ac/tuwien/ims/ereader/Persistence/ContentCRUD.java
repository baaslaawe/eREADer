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

import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Content;

/**
 * Interface for the CRUD methods for content.
 *
 * @author Florian Schuster
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
