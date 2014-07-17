package at.ac.tuwien.ims.ereader.Services;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Chapter;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Entities.Language;
import at.ac.tuwien.ims.ereader.Entities.Page;
import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.R;

/**
 * Created by Flo on 16.07.2014.
 */
public class BookService {
    private DatabaseHelper db;

    public BookService(Context c) {
        db=new DatabaseHelper(c);
    }

    public void insertTestBooks() {
        db.resetDatabase();

        Book b1=insertBook("Lel1", "Lelman", Language.DE);

        Chapter ch1=insertChapter(b1, "lelchap", 1);
        insertPage(ch1, 1, "b1CONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\n" +
                "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTEN\nTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTTENTCONTENTCONTENT" +
                "CONTENTCONTENT\nCONTENTCOCONTENTCONTENTCONTENTCONTENTCONT\nENTCONTENTNTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCOCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTNTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCO\nNTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCO\nNTENTCONTENTCONTENTCONTENTCONTENTCONTENT");
        insertPage(ch1, 2, "cont2b2");
        insertPage(ch1, 3, "cont3b2");

        Chapter ch2=insertChapter(b1, "lelchapchapb1", 2);
        Page p4=insertPage(ch2, 4, "0 This is a Test. 1 This is a Test. 2 This is a Test. 3 This is a Test. 4 This is a Test. 5 This is a Test.");
        insertPage(ch2, 5, "cont3b2");

        Chapter ch3=insertChapter(b1, "lelchap3b1", 3);
        insertPage(ch3, 6, "cont1b2");



        Book b2=insertBook("Lelbook2", "Lelman jr.", Language.EN);

        Chapter ch4=insertChapter(b2, "lelchap", 1);
        insertPage(ch4, 1, "b2CONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\n" +
                "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTEN\nTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTTENTCONTENTCONTENT" +
                "CONTENTCONTENT\nCONTENTCOCONTENTCONTENTCONTENTCONTENTCONT\nENTCONTENTNTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCOCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTNTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCO\nNTENTCONTENTCONTENT" +
                "CONTENTCONTENTCONTENTCONTENT\nCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCONTENTCO\nNTENTCONTENTCONTENTCONTENTCONTENTCONTENT");
        insertPage(ch4, 2, "cont2");
        insertPage(ch4, 3, "cont3");

        Chapter ch5=insertChapter(b2, "lelchapchapb2", 2);
        insertPage(ch5, 4, "cont2b2");
        insertPage(ch5, 5, "cont3b2");

        Chapter ch6=insertChapter(b2, "lelchap3b2", 3);
        Page p6=insertPage(ch6, 6, "0 This is a Test. 1 This is a Test. 2 This is a Test. 3 This is a Test. 4 This is a Test. 5 This is a Test.");

        updateCurrentPosition(new CurrentPosition(b1.getId(), 1, 0, 3));
        updateCurrentPosition(new CurrentPosition(b2.getId(), 2, 0, 2));
    }

    public Book insertBook(String title, String author, Language language) {
        Book b=new Book(title, author, language);
        long id=db.insertBook(b);
        insertCurrentPosition(id, 0, 0, 0);
        return db.getBook(id);
    }

    public Chapter insertChapter(Book bookOfChapter, String heading, int chapter_nr) {
        Chapter c=new Chapter(bookOfChapter, heading, chapter_nr);
        long id=db.insertChapter(c);
        return db.getChapter(id);
    }

    public Page insertPage(Chapter chapterOfPage, int page_nr, String content) {
        Page p=new Page(chapterOfPage, page_nr, content);
        long id=db.insertPage(p);
        return db.getPage(id);
    }

    private CurrentPosition insertCurrentPosition(long book_id, int currentChapterInBook, int currentPageInChapter, int currentSentence) {
        CurrentPosition c=new CurrentPosition(book_id, currentChapterInBook, currentPageInChapter, currentSentence);
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

    public HashMap<Integer, List<Page>> getPagesOfChapters(List<Chapter> chapters) {
        HashMap pages=new HashMap<Integer, List<Page>>();
        int i=0;
        for (Chapter c : chapters) {
            List<Page> tempList=new ArrayList<Page>();
            for (Page p : db.getPagesByChapter(c.getId()))
                tempList.add(p);
            pages.put(i, tempList);
            i++;
        }
        return pages;
    }

    public CurrentPosition getCurrentPosition(long book_id) {
        return db.getCurrentPosition(book_id);
    }

    public void updateCurrentPosition(CurrentPosition c) {
        db.updateCurrentPosition(c);
    }

    public int getMinPage(Chapter chapter) {
        return db.getPagesByChapter(chapter.getId()).get(0).getPage_nr();
    }

    public int getMaxPage(Chapter chapter) {
        List<Page> pages=db.getPagesByChapter(chapter.getId());
        return pages.get(pages.size() - 1).getPage_nr();
    }
}
