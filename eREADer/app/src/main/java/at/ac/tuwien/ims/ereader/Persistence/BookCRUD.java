package at.ac.tuwien.ims.ereader.Persistence;

import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;

/**
 * Created by Flo on 14.07.2014.
 */
public interface BookCRUD {
    /**
     * Inserts a new book to the DB.
     *
     * @param book book to add
     */
    public void insertBook(Book book);

    /**
     * Gets a book from the DB.
     *
     * @param id id of the book
     * @return the book specified by id
     */
    public Book getBook(int id);

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
     * Deletes a book from the DB (including all its chapters and pages).
     *
     * @param book book to delete
     */
    public void deleteBook(Book book);
}