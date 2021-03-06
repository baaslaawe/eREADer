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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private String ebook_unknown_language;

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
        ebook_unknown_language=c.getString(R.string.ebook_unknown_language);
    }

    /**
     * Adds a book of the epub format to the database.
     *
     * @param URI of the selected file
     * @return the inserted book
     * @throws ServiceException if an error occurs during book insertion
     */
    public Book addBookAsEPUB(String URI) throws ServiceException {
        try {
            nl.siegmann.epublib.domain.Book b=new EpubReader().readEpub(new FileInputStream(URI));
            String author="";
            if(!b.getMetadata().getAuthors().isEmpty()) {
                author = b.getMetadata().getAuthors().get(0).getFirstname() + " " + b.getMetadata().getAuthors().get(0).getLastname();
                author = author.replace(",", "");
            }

            String lan="";
            if(b.getMetadata().getLanguage()!=null)
                lan=b.getMetadata().getLanguage();
            Language lang=Language.getLanguageFromString(lan);

            String title="";
            if(b.getMetadata().getFirstTitle()!=null)
                title=b.getMetadata().getFirstTitle();
            Book bookToSave=insertBook(title, author, lang);

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

            return bookToSave;
        } catch (IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    /**
     * Adds a book of the pdf format to the database.
     *
     * @param URI of the selected file
     * @param language the selected language in AddBookActivity
     * @return the inserted book
     * @throws ServiceException if an error occurs during book insertion
     */
    public Book addBookAsPDF(String URI, String language) throws ServiceException {
        try {
            PdfReader reader = new PdfReader(URI);
            Map<String,String> info=reader.getInfo();
            String title="";
            List<String> titleSearch=new ArrayList<String>();
            titleSearch.add("Title");
            titleSearch.add("title");
            titleSearch.add("Titel");
            titleSearch.add("titel");
            for(String s: titleSearch)
                if(title.equals("")) {
                    if (info.get(s) != null)
                        title = info.get(s);
                } else
                    break;
            String author="";
            List<String> authorSearch=new ArrayList<String>();
            authorSearch.add("Author");
            authorSearch.add("author");
            authorSearch.add("Autor");
            authorSearch.add("autor");
            for(String s: authorSearch)
                if(author.equals("")) {
                    if (info.get(s) != null)
                        author = info.get(s);
                } else
                    break;

            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextExtractionStrategy strategy;
            StringBuilder str = new StringBuilder();
            for (int j = 1; j <= reader.getNumberOfPages(); j++) {
                strategy = parser.processContent(j, new SimpleTextExtractionStrategy());
                String currentContent=strategy.getResultantText();

                for(int i=0; i<currentContent.length(); i++) {
                    if(i>1 && currentContent.charAt(i)=='\n') {
                        char lastChar=currentContent.charAt(i-1);
                        if(lastChar=='\n' || (currentContent.charAt(i-2)=='\n' && lastChar==' ')) {
                            str.append("\n");
                            continue;
                        } else if (lastChar!='.' && lastChar!='!' && lastChar!='?') {
                            str.append(" ");
                            continue;
                        }
                    }
                    str.append(currentContent.charAt(i));
                }
            }
            reader.close();

            Language lang=Language.getLanguageFromString(language);
            Book bookToSave=insertBook(title, author, lang);

            divideAndSafeContentInChunks(bookToSave, str.toString().trim());

            return bookToSave;
        } catch(IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    /**
     * Adds a book of the txt format to the database.
     *
     * @param URI of the selected file
     * @param language the selected language in AddBookActivity
     * @return the inserted book
     * @throws ServiceException if an error occurs during book insertion
     */
    public Book addBookAsTXT(String URI, String language) throws ServiceException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(URI));
            StringBuilder str = new StringBuilder();
            String line;
            String title="";
            String author="";
            while ((line = in.readLine()) != null) {
                if(title.isEmpty() || author.isEmpty())
                    if(line.toLowerCase().contains("title") || line.toLowerCase().contains("titel")) {
                        String[] tokens = line.split(": ");
                        title = tokens[1];
                    } else if(line.toLowerCase().contains("author") || line.toLowerCase().contains("autor")) {
                        String[] tokens = line.split(": ");
                        author = tokens[1];
                    }
                str.append(line);
                if ("".equals(line.trim()))
                    str.append("\n");
            }
            in.close();
            Language lang=Language.getLanguageFromString(language);
            Book bookToSave=insertBook(title, author, lang);

            String content=str.toString().trim();
            divideAndSafeContentInChunks(bookToSave, content);

            return bookToSave;
        } catch(IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    /**
     * Adds a book of the html format to the database.
     *
     * @param URI of the selected file
     * @param language the selected language in AddBookActivity
     * @return the inserted book
     * @throws ServiceException if an error occurs during book insertion
     */
    public Book addBookAsHTML(String URI, String language) throws ServiceException {
        try {
            StringBuilder str = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(URI));
            String line;
            String title="";
            String author="";
            while ((line = in.readLine()) != null) {
                str.append(line);
                if ("".equals(line.trim()))
                    str.append("\n");
            }
            in.close();

            Language lang=Language.getLanguageFromString(language);
            Book bookToSave=insertBook(title, author, lang);

            Document doc = Jsoup.parse(str.toString());
            Elements els = doc.select("style");
            for(Element e: els){
                e.remove();
            }
            String cont=doc.toString();

            String content=Html.fromHtml(cont).toString().replace((char) 65532, (char) 32).replaceAll("\uFFFD", "").trim();
            divideAndSafeContentInChunks(bookToSave, content);

            return bookToSave;
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
                String sentence=content.substring(firstIndex, lastIndex);
                words+=sentence.split("\\s+").length;
                if(words>=5000) {
                    insertContent(bookToSaveTo, Integer.toString(actualContentNumber), content.substring(savedIndex, lastIndex), words);
                    actualContentNumber++;
                    words=0;
                    savedIndex=lastIndex;
                }
            }
        }

        lastIndex=content.length()-1;
        if(words<=1500 && actualContentNumber>1) {
            Content lastContent=db.getContentsByBook(bookToSaveTo.getId()).get(db.getContentsByBook(bookToSaveTo.getId()).size()-1);
            db.deleteContent(lastContent.getId());
            insertContent(bookToSaveTo, Integer.toString(actualContentNumber - 1), lastContent.getContent() + "\n" + content.substring(savedIndex, lastIndex), lastContent.getWords() + words);
        } else {
            insertContent(bookToSaveTo, Integer.toString(actualContentNumber), content.substring(savedIndex, lastIndex), words);
        }
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
        if(title==null||author==null)
            throw new ServiceException(ebook_loading_failed);
        if(title.isEmpty())
            title=no_title;
        if(author.isEmpty())
            author=no_author;
        if(language==Language.UNKNOWN)
            throw new ServiceException(ebook_unknown_language);

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
        if(bookOfContent==null || content==null)
            throw new ServiceException(ebook_loading_failed);
        if(heading.isEmpty() || content.isEmpty() || numberOfWords<0)
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
     * Returns all books alphabetically in the database using the DatabaseHelper.
     *
     * @return list with book entities
     */
    public List<Book> getAllBooksAlphabetically() {
        List<Book> list=db.getAllBooks();
        Collections.sort(list, new Comparator<Book>() {
            @Override
            public int compare(final Book object1, final Book object2) {
                return object1.getTitle().compareTo(object2.getTitle());
            }
        });
        return list;
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