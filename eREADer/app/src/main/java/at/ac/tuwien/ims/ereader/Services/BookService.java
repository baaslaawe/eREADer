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
package at.ac.tuwien.ims.ereader.Services;

import android.content.Context;
import android.text.Html;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Content;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Entities.Language;
import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.R;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * A service class that handles inserting and deleting books, contents and currentpositions.
 *
 * @author Florian Schuster
 */
public class BookService {
    private DatabaseHelper db;
    private String ebook_loading_failed;
    private String no_title;
    private String no_author;

    /**
     * Constructor for the service class. Uses context of caller class to instantiate DatabaseHelper
     * and to get some Strings from the resources.
     *
     * @param c Context
     */
    public BookService(Context c) {
        db=new DatabaseHelper(c);
        ebook_loading_failed=c.getString(R.string.ebook_loading_failed);
        no_title=c.getString(R.string.no_title);
        no_author=c.getString(R.string.no_author);
    }

    /**
     * Adds a book of the epub format to the database.
     *
     * @param URI of the selected file
     * @throws ServiceException if an error occurs during book insertion
     */
    public void addBookAsEPUB(String URI) throws ServiceException {
        try {
            nl.siegmann.epublib.domain.Book b=new EpubReader().readEpub(new FileInputStream(URI));
            String author=b.getMetadata().getAuthors().get(0).getFirstname() + " " + b.getMetadata().getAuthors().get(0).getLastname();
            author=author.replace(",", "");

            Language lang=getLanguageFromString(b.getMetadata().getLanguage());
            Book bookToSave=insertBook(b.getMetadata().getFirstTitle(), author, lang);

            List<Resource> list=b.getContents();
            if(list.isEmpty()) {
                deleteBook(bookToSave.getId());
                throw new ServiceException(ebook_loading_failed);
            }

            int actualContentNumber=1;
            for(int i=0, s=list.size(); i<s; i++) {
                String cont=new String(list.get(i).getData());
                if (i==0&&b.getCoverImage()!=null || cont.isEmpty())
                    continue;
                cont=Html.fromHtml(cont).toString().replace((char) 65532, (char) 32).trim();
                int words=cont.split("\\s+").length;
                if(words<20)
                    continue;
                insertContent(bookToSave, "" + actualContentNumber, cont, words);
                actualContentNumber++;
            }
        } catch (IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    /**
     * Adds a book of the pdf format to the database.
     *
     * @param URI of the selected file
     * @throws ServiceException if an error occurs during book insertion
     */
    public void addBookAsPDF(String URI) throws ServiceException {
        try {
            PdfReader reader = new PdfReader(URI);
            StringBuilder str = new StringBuilder();
            String title="";
            String author="";
            String l="";
            for(Map.Entry<String,String> s : reader.getInfo().entrySet()) {
                if(title.isEmpty() || author.isEmpty() || l.isEmpty())
                    if(s.getKey().toLowerCase().contains("title") || s.getKey().toLowerCase().contains("titel")) {
                        title = s.getValue();
                    } else if(s.getKey().toLowerCase().contains("author") || s.getKey().toLowerCase().contains("autor")) {
                        author = s.getValue();
                    } else if(s.getKey().toLowerCase().contains("language") || s.getKey().toLowerCase().contains("sprache")) {
                        l = s.getValue();
                    }
            }
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextExtractionStrategy strategy;
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                str.append(strategy.getResultantText());
            }
            Language lang=getLanguageFromString(l);
            Book bookToSave=insertBook(title, author, lang);

            String content=str.toString().trim();
            divideAndSafeContentInChunks(bookToSave, content);

        } catch(IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    /**
     * Adds a book of the txt format to the database.
     *
     * @param URI of the selected file
     * @throws ServiceException if an error occurs during book insertion
     */
    public void addBookAsTXT(String URI) throws ServiceException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(URI));
            StringBuilder str = new StringBuilder();
            String line;
            String title="";
            String author="";
            String l="";
            while ((line = in.readLine()) != null) {
                if(title.isEmpty() || author.isEmpty() || l.isEmpty())
                    if(line.toLowerCase().contains("title") || line.toLowerCase().contains("titel")) {
                        String[] tokens = line.split(": ");
                        title = tokens[1];
                    } else if(line.toLowerCase().contains("author") || line.toLowerCase().contains("autor")) {
                        String[] tokens = line.split(": ");
                        author = tokens[1];
                    } else if(line.toLowerCase().contains("language") || line.toLowerCase().contains("sprache")) {
                        String[] tokens = line.split(": ");
                        l = tokens[1];
                    }
                str.append(line);
                str.append("\n");
            }
            in.close();

            Language lang=getLanguageFromString(l);
            Book bookToSave=insertBook(title, author, lang);

            String content=str.toString().trim();
            divideAndSafeContentInChunks(bookToSave, content);

        } catch(IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    /**
     * Divides a whole content string from an ebook into content chunks to better save and display
     * it.
     *
     * @param bookToSaveTo book that the content should be saved to
     * @param content the string of the whole content
     * @throws ServiceException when content is empty
     */
    public void divideAndSafeContentInChunks(Book bookToSaveTo, String content) throws ServiceException {
        if(content.isEmpty() || bookToSaveTo==null) {
            if(bookToSaveTo!=null)
                deleteBook(bookToSaveTo.getId());
            throw new ServiceException(ebook_loading_failed);
        }

        if(bookToSaveTo.getLanguage()==Language.UNKNOWN) { //todo
            deleteBook(bookToSaveTo.getId());
            throw new ServiceException(ebook_loading_failed);
        }

        BreakIterator it = BreakIterator.getSentenceInstance(bookToSaveTo.getLanguage().getLocale());
        it.setText(content);
        int words=0;
        int actualContentNumber=1;
        int lastIndex = it.first();

        int savedIndex=0;
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex != BreakIterator.DONE) {
                words+=content.substring(firstIndex, lastIndex).split("\\s+").length;

                if(words>=5000) {
                    insertContent(bookToSaveTo, "" + actualContentNumber, content.substring(savedIndex, lastIndex), words);
                    actualContentNumber++;
                    words=0;
                    savedIndex=lastIndex;
                }
            }
        }
    }

    /**
     * Languages are only saved as String in ebooks, so we try to convert it to our own format.
     *
     * @param l String of the language
     * @return language of the ebook or Language.UNKOWN if language is unknown
     */
    private Language getLanguageFromString(String l) {
        if (l.equals("English") || l.equals("Englisch") || l.equals("en")) {
            return Language.EN;
        } else if(l.equals("German") || l.equals("Deutsch") || l.equals("de")) {
            return Language.DE;
        } else if(l.equals("Spanish") || l.equals("Spanisch") || l.equals("es")) {
            return Language.ES;
        } else
            return Language.UNKNOWN;
    }

    /**
     * Inserts a book using the DatabaseHelper. Also inserts new currentposition at the start
     * of the book.
     *
     * @param title of the book
     * @param author of the book
     * @param language of the book
     * @return the inserted book with its given id
     * @throws ServiceException if an error occurs while inserting currentposition
     */
    private Book insertBook(String title, String author, Language language) throws ServiceException {
        if(title.isEmpty())
            title=no_title;

        if(author.isEmpty())
            author=no_author;

        Book b=new Book(title, author, language);
        long id=db.insertBook(b);
        insertCurrentPosition(id, 0, 0);
        return db.getBook(id);
    }

    /**
     * Inserts a content using the DatabaseHelper.
     *
     * @param bookOfContent the book that the content belongs to
     * @param heading of the content
     * @param content of the content
     * @param numberOfWords of the content
     * @return the inserted content with its given id
     * @throws ServiceException if book is null, content is empty or words are negative
     */
    private Content insertContent(Book bookOfContent, String heading, String content, int numberOfWords) throws ServiceException {
        if(bookOfContent==null || content.isEmpty() || numberOfWords<0)
            throw new ServiceException(ebook_loading_failed);

        Content c=new Content(bookOfContent, heading, content, numberOfWords);
        long id=db.insertContent(c);
        return db.getContent(id);
    }

    /**
     * Inserts a current position of a book using the DatabaseHelper.
     *
     * @param book_id id of book that the currentposition belongs to
     * @param currentContentInBook content number in current book
     * @param currentSentence number of sentence in content
     * @return the inserted current position with its given id
     * @throws ServiceException if any of the numbers is negative
     */
    private CurrentPosition insertCurrentPosition(long book_id, int currentContentInBook, int currentSentence) throws ServiceException {
        if(book_id<0 || currentContentInBook<0 || currentSentence<0)
            throw new ServiceException(ebook_loading_failed);

        CurrentPosition c=new CurrentPosition(book_id, currentContentInBook, currentSentence);
        long id=db.insertCurrentPosition(c);
        return db.getCurrentPosition(id);
    }

    /**
     * Returns all books in the database using the DatabaseHelper.
     *
     * @return list with book entities
     */
    public List<Book> getAllBooks() {
        return db.getAllBooks();
    }

    /**
     * Deletes a book using the DatabaseHelper.
     *
     * @param book_id the id of the book to delete
     */
    public void deleteBook(long book_id) {
        if(book_id<0)
            return;
        db.deleteBook(book_id);
    }

    /**
     * Gets a book from the database using DatabaseHelper.
     *
     * @param book_id id of the book to get
     * @return the book with the given id
     */
    public Book getBook(long book_id) {
        return db.getBook(book_id);
    }

    /**
     * Updates a book using DatabaseHelper.
     *
     * @param book the book to update
     */
    public void updateBook(Book book) {
        db.updateBook(book);
    }

    /**
     * Returns all contents of a book in the database using the DatabaseHelper.
     *
     * @param book_id id of the book
     * @return a list with content entities
     */
    public List<Content> getContentsOfBook(long book_id) {
        return db.getContentsByBook(book_id);
    }

    /**
     * Returns all contents without the Text of a book in the database using the DatabaseHelper.
     * This is useful when you only want the metadata of a content entity to display a
     * table of contents.
     *
     * @param book_id id of the book
     * @return a list with content entities
     */
    public List<Content> getLightweightContentsOfBook(long book_id) {
        return db.getLightweightContents(book_id);
    }

    /**
     * Gets a current position of a specific book from the database using DatabaseHelper.
     *
     * @param book_id id of the book
     * @return a current position entity
     */
    public CurrentPosition getCurrentPosition(long book_id) {
        return db.getCurrentPosition(book_id);
    }

    /**
     * Updates a current position using DatabaseHelper.
     *
     * @param c the current position to update
     */
    public void updateCurrentPosition(CurrentPosition c) {
        db.updateCurrentPosition(c);
    }

    /**
     * Returns the number of words of a specific content chunk.
     *
     * @param content_id id of the content
     * @return number of words
     */
    public int getNumberOfWords(long content_id) {
        return db.getContent(content_id).getWords();
    }
}