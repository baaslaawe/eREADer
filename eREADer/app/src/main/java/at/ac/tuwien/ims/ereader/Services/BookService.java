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
        Page p4=insertPage(ch2, 4, "Das ist der erste Test. Das ist der zweite Test. Das ist der dritte Test. Das ist ein Test." +
                "Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test. Das ist ein Test.");
        insertPage(ch2, 5, "cont3b2");

        Chapter ch3=insertChapter(b1, "lelchap3b1", 3);
        insertPage(ch3, 6, "cont1b2");



        Book b2=insertBook("Faust", "Johann Wolfgang von Goethe", Language.EN);

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
        insertPage(ch5, 4, "CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT ");
        insertPage(ch5, 5, "CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT ");

        Chapter ch6=insertChapter(b2, "TestChapter", 3);
        insertPage(ch6, 6, "Chiefly, enough of incident prepare!\n" +
                "They come to look, and they prefer to stare.\n" +
                "Reel off a host of threads before their faces,\n" +
                "So that they gape in stupid wonder: then\n" +
                "By sheer diffuseness you have won their graces,\n" +
                "And are, at once, most popular of men.\n" +
                "Only by mass you touch the mass; for any\n" +
                "Will finally, himself, his bit select:\n" +
                "Who offers much, brings something unto many,\n" +
                "And each goes home content with the effect,\n" +
                "If you've a piece, why, just in pieces give it:\n" +
                "A hash, a stew, will bring success, believe it!\n" +
                "'Tis easily displayed, and easy to invent.\n" +
                "What use, a Whole compactly to present?\n" +
                "Your hearers pick and pluck, as soon as they receive it!");

        insertPage(ch6, 7, "You do not feel, how such a trade debases;\n" +
                "How ill it suits the Artist, proud and true!\n" +
                "The botching work each fine pretender traces\n" +
                "Is, I perceive, a principle with you.");

        insertPage(ch6, 8, "Such a reproach not in the least offends;\n" +
                "A man who some result intends\n" +
                "Must use the tools that best are fitting.\n" +
                "Reflect, soft wood is given to you for splitting,\n" +
                "And then, observe for whom you write!\n" +
                "If one comes bored, exhausted quite,\n" +
                "Another, satiate, leaves the banquet's tapers,\n" +
                "And, worst of all, full many a wight\n" +
                "Is fresh from reading of the daily papers.\n" +
                "Idly to us they come, as to a masquerade,\n" +
                "Mere curiosity their spirits warming:\n" +
                "The ladies with themselves, and with their finery, aid,\n" +
                "Without a salary their parts performing.\n" +
                "What dreams are yours in high poetic places?\n" +
                "You're pleased, forsooth, full houses to behold?\n" +
                "Draw near, and view your patrons' faces!\n" +
                "The half are coarse, the half are cold.\n" +
                "One, when the play is out, goes home to cards;\n" +
                "A wild night on a wench's breast another chooses:\n" +
                "Why should you rack, poor, foolish bards,\n" +
                "For ends like these, the gracious Muses?\n" +
                "I tell you, give but more—more, ever more, they ask:\n" +
                "Thus shall you hit the mark of gain and glory.\n" +
                "Seek to confound your auditory!\n" +
                "To satisfy them is a task.—\n" +
                "What ails you now? Is't suffering, or pleasure?");

        updateCurrentPosition(new CurrentPosition(b1.getId(), 1, 0, 1));
        updateCurrentPosition(new CurrentPosition(b2.getId(), 2, 0, 0));
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
