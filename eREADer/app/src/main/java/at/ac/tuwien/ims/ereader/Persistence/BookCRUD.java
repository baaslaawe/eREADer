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
import at.ac.tuwien.ims.ereader.Entities.Book;

/**
 * Interface for the CRUD methods for books.
 *
 * @author Florian Schuster
 */
public interface BookCRUD {
    /**
     * Inserts a new book to the DB.
     *
     * @param book book to add
     * @return the generated id of the inserted book
     */
    public long insertBook(Book book);

    /**
     * Gets a book from the DB.
     *
     * @param id id of the book
     * @return the book specified by id
     */
    public Book getBook(long id);

    /**
     * Returns all the books from the DB.
     *
     * @return a list with all books
     */
    public List<Book> getAllBooks();

    /**
     * Returns the number of all books in the DB.
     *
     * @return number of all books
     */
    public int getAllBooksCount();

    /**
     * Updates a book.
     *
     * @param book book to update
     */
    public void updateBook(Book book);

    /**
     * Deletes a book from the DB (including all its chapters and pages).
     *
     * @param book_id id of book to delete
     */
    public void deleteBook(long book_id);
}
