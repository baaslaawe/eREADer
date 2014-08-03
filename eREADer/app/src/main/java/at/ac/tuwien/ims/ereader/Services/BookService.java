package at.ac.tuwien.ims.ereader.Services;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Chapter;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Entities.Language;
import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.R;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Created by Flo on 16.07.2014.
 */
public class BookService {
    private DatabaseHelper db;
    private String ebook_loading_failed;

    public BookService(Context c) {
        db=new DatabaseHelper(c);
        ebook_loading_failed=c.getString(R.string.ebook_loading_failed);
    }

    public void insertTestBooks() {
        db.resetDatabase();

        Book b1=insertBook("Lel1", "Lelman", Language.DE);
        insertChapter(b1, "Erstes Kapitel", 1,
                "b1CONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\n" +
                        "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTEN\nTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT" +
                        "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT" +
                        "CONTENTCONTENTCONTENTCONCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTTENTCONTENTCONTENT" +
                        "CONTENTCONTENT\nCONTENTCOCONTENTCONTENTCONTENTCONTENTCONT\nENTCONTENTNTENTCONTENTCONTENT" +
                        "CONTENTCONTENTCONTENTCOCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTNTENTCONTENTCONTENT" +
                        "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCO\nNTENTCONTENTCONTENT" +
                        "CONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCO\nNTENTCONTENTCONTENTCONTENTCONTENTCONTENT");
        insertChapter(b1, "Zweites Kapitel", 2,
                "Das ist der erste Test. Das ist der zweite Test. Das ist der dritte Test. Das ist ein Test." +
                        "Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test.");
        insertChapter(b1, "Kapitel 3", 3,
                "Hallo wie geht es dir?");


        Book b2=insertBook("Faust", "Johann Wolfgang von Goethe", Language.EN);
        insertChapter(b2, "Chapter 1", 1,
                "TEST CONTENT 1. TEST CONTENT 1. TEST CONTENT 1.\n"+
                        "TEST CONTENT 2. TEST CONTENT 2. TEST CONTENT 2.");

        insertChapter(b2, "Second Chapter", 2,
                "TEST CONTENT 4. TEST CONTENT 4. TEST CONTENT 4.\n"+
                        "TEST CONTENT 5. TEST CONTENT 5. TEST CONTENT 5.");

        insertChapter(b2, "Chapter 3", 3,
                "Chiefly, enough of incident prepare!\n" +
                        "They come to look, and they prefer to stare.\n" +
                        "TEST CONTENT 6. TEST CONTENT 6. TEST CONTENT 6.\n" +
                        "You do not feel, how such a trade debases;\n" +
                        "How ill it suits the Artist, proud and true!\n" +
                        "TEST CONTENT 7. TEST CONTENT 7. TEST CONTENT 7.");

        updateCurrentPosition(new CurrentPosition(b1.getId(), 1, 1));
        updateCurrentPosition(new CurrentPosition(b2.getId(), 2, 0));
    }

    public void addBookManually(String URI) throws ServiceException {
        EpubReader epr=new EpubReader();
        nl.siegmann.epublib.domain.Book b;
        try {
            b=epr.readEpub(new FileInputStream(URI));
        } catch (IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }

        String author="";
        for (int i=0; i<b.getMetadata().getAuthors().size(); i++) {
            author += b.getMetadata().getAuthors().get(i).getFirstname() + " " + b.getMetadata().getAuthors().get(i).getLastname();
            if (b.getMetadata().getAuthors().size() > 1)
                author+=", ";
        }
        if(author.isEmpty())
            throw new ServiceException(ebook_loading_failed);

        Language lang;
        String l=b.getMetadata().getLanguage();
        if (l.equals("English") || l.equals("Englisch") || l.equals("en")) {
            lang=Language.EN;
        } else if(l.equals("German") || l.equals("Deutsch") || l.equals("de")) {
            lang=Language.DE;
        } else if(l.equals("Spanish") || l.equals("Spanisch") || l.equals("es")) {
            lang=Language.ES;
        } else
            throw new ServiceException(ebook_loading_failed);

        Book bookToSave= new Book(b.getMetadata().getFirstTitle(), author, lang);

        String content="";
        InputStream is;
        BufferedReader r;
        StringBuilder strb=new StringBuilder();
        for (TOCReference tocReference : b.getTableOfContents().getTocReferences()) {
            try {
                is = tocReference.getResource().getInputStream();
                r = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = r.readLine()) != null) {
                    strb.append(line);
                    strb.append("\n");
                }
            } catch(IOException e){
                throw new ServiceException(ebook_loading_failed);
            }
        }
        content=strb.toString();
        if(content.isEmpty())
            throw new ServiceException(ebook_loading_failed);

        List<String> chaps=new ArrayList<String>();


        //Chapter c= new Chapter(book, , );

        //Page p= new Page(c, ,);
    }

    public Book insertBook(String title, String author, Language language) {
        Book b=new Book(title, author, language);
        long id=db.insertBook(b);
        insertCurrentPosition(id, 0, 0);
        return db.getBook(id);
    }

    public Chapter insertChapter(Book bookOfChapter, String heading, int chapter_nr, String content) {
        Chapter c=new Chapter(bookOfChapter, heading, chapter_nr, content);
        long id=db.insertChapter(c);
        return db.getChapter(id);
    }

    private CurrentPosition insertCurrentPosition(long book_id, int currentChapterInBook, int currentSentence) {
        CurrentPosition c=new CurrentPosition(book_id, currentChapterInBook, currentSentence);
        long id=db.insertCurrentPosition(c);
        return db.getCurrentPosition(id);
    }

    public List<Book> getAllBooks() {
        return db.getAllBooks();
    }

    public void deleteBook(long book_id) {
        db.deleteBook(book_id);
    }

    public Book getBook(long book_id) {
        return db.getBook(book_id);
    }

    public List<Chapter> getChaptersOfBook(long book_id) {
        return db.getChaptersByBook(book_id);
    }

    public List<Chapter> getLightweightChaptersOfBook(long book_id) {
        return db.getLightweightChapters(book_id);
    }

    public CurrentPosition getCurrentPosition(long book_id) {
        return db.getCurrentPosition(book_id);
    }

    public void updateCurrentPosition(CurrentPosition c) {
        db.updateCurrentPosition(c);
    }

    public int getNumberOfWords(long chapter_id) {
        Chapter c=db.getChapter(chapter_id);
        String text=c.getContent().trim();
        return text.isEmpty() ? 0 : text.split("\\s+").length;
    }
}