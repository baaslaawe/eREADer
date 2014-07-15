package at.ac.tuwien.ims.ereader.Persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Chapter;
import at.ac.tuwien.ims.ereader.Entities.Language;
import at.ac.tuwien.ims.ereader.Entities.Page;

/**
 * Created by Flo on 14.07.2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BookCRUD, ChapterCRUD, PageCRUD {
    private static final String DATABASE_NAME = "eREADerDB";
    private static final int DATABASE_VERSION = 1;

    //---------------------------------------------------------------------
    //BOOKS
    private static final String TABLE_BOOKS = "books";

    private static final String BOOK_KEY_ID = "id";
    private static final String BOOK_KEY_TITLE = "title";
    private static final String BOOK_KEY_AUTHOR = "author";
    private static final String BOOK_KEY_LANGUAGE = "lang";

    //---------------------------------------------------------------------
    //CHAPTERS
    private static final String TABLE_CHAPTERS = "chapters";

    private static final String CHAPTER_KEY_ID = "id";
    private static final String CHAPTER_KEY_BOOK_ID = "book_id";
    private static final String CHAPTER_KEY_HEADING = "heading";
    private static final String CHAPTER_KEY_CHAPTER_NR = "chapter_nr";

    //---------------------------------------------------------------------
    //PAGES
    private static final String TABLE_PAGES = "pages";

    private static final String PAGE_KEY_ID = "id";
    private static final String PAGE_KEY_CHAPTER_ID = "chapter_id";
    private static final String PAGE_KEY_PAGE_NR = "page_nr";
    private static final String PAGE_KEY_CONTENT = "content";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOK_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + BOOK_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + BOOK_KEY_TITLE + " TEXT NOT NULL,"
                + BOOK_KEY_AUTHOR + " TEXT NOT NULL," + BOOK_KEY_LANGUAGE + " INTEGER NOT NULL";
        db.execSQL(CREATE_BOOK_TABLE);

        String CREATE_CHAPTER_TABLE = "CREATE TABLE " + TABLE_CHAPTERS + "("
                + CHAPTER_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + CHAPTER_KEY_BOOK_ID + " INTEGER NOT NULL,"
                + CHAPTER_KEY_HEADING + " TEXT NOT NULL," + CHAPTER_KEY_CHAPTER_NR + " INTEGER NOT NULL"
                + "FOREIGN KEY(" + CHAPTER_KEY_BOOK_ID + ") REFERENCES "+ TABLE_BOOKS
                +"(" + BOOK_KEY_ID+ "))";
        db.execSQL(CREATE_CHAPTER_TABLE);

        String CREATE_PAGE_TABLE = "CREATE TABLE " + TABLE_PAGES + "("
                + PAGE_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + PAGE_KEY_CHAPTER_ID + " INTEGER NOT NULL,"
                + PAGE_KEY_PAGE_NR + " INTEGER NOT NULL," + PAGE_KEY_CONTENT + " TEXT NOT NULL,"
                + "FOREIGN KEY(" + PAGE_KEY_CHAPTER_ID + ") REFERENCES "+ TABLE_CHAPTERS
                +"("+CHAPTER_KEY_ID+"))";
        db.execSQL(CREATE_PAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DatabaseHelper.class.getName(), "Upgrading Database "+TABLE_BOOKS+" from Version "
                + oldVersion + "to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);

        Log.d(DatabaseHelper.class.getName(), "Upgrading Database "+TABLE_CHAPTERS+" from Version "
                + oldVersion + "to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAPTERS);

        Log.d(DatabaseHelper.class.getName(), "Upgrading Database "+TABLE_PAGES+" from Version "
                + oldVersion + "to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGES);

        onCreate(db);
    }

    //---------------------------------------------------------------------
    //BOOKS

    public void insertBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BOOK_KEY_TITLE, book.getTitle());
        values.put(BOOK_KEY_AUTHOR, book.getAuthor());
        values.put(BOOK_KEY_LANGUAGE, book.getLanguage().getCode());

        db.insert(TABLE_BOOKS, null, values);
        db.close();
        Log.d(DatabaseHelper.class.getName(), book.toString()+" added to DB");
    }

    public Book getBook(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKS, new String[] {BOOK_KEY_ID, BOOK_KEY_TITLE,
                        BOOK_KEY_AUTHOR, BOOK_KEY_LANGUAGE},
                BOOK_KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Book book = new Book();
        book.setId(Integer.parseInt(cursor.getString(0)));
        book.setTitle(cursor.getString(1));
        book.setAuthor(cursor.getString(2));
        int lang=Integer.parseInt(cursor.getString(3));
        if (Language.DE.getCode() == lang)
            book.setLanguage(Language.DE);
        else if (Language.EN.getCode() == lang)
            book.setLanguage(Language.EN);
        else if (Language.ES.getCode() == lang)
            book.setLanguage(Language.ES);
        book.setChapters(getChaptersByBook(book.getId()));

        Log.d(DatabaseHelper.class.getName(), book.toString() + " read from DB");
        return book;
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<Book>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Book book = new Book();
                book.setId(Integer.parseInt(cursor.getString(0)));
                book.setTitle(cursor.getString(1));
                book.setAuthor(cursor.getString(2));
                int lang=Integer.parseInt(cursor.getString(3));
                if (Language.DE.getCode() == lang)
                    book.setLanguage(Language.DE);
                else if (Language.EN.getCode() == lang)
                    book.setLanguage(Language.EN);
                else if (Language.ES.getCode() == lang)
                    book.setLanguage(Language.ES);
                book.setChapters(getChaptersByBook(book.getId()));

                bookList.add(book);
            } while (cursor.moveToNext());
        }

        Log.d(DatabaseHelper.class.getName(), "All books read from DB");
        return bookList;
    }

    public int getAllBooksCount() {
        String countQuery = "SELECT  * FROM " + TABLE_BOOKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        Log.d(DatabaseHelper.class.getName(), "Number of all books read from DB");
        return cursor.getCount();
    }

    public void deleteBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKS, BOOK_KEY_ID + " = ?", new String[] { String.valueOf(book.getId()) });
        for(Chapter chapter : book.getChapters()) {
            deleteChapter(chapter);
        }
        db.close();
        Log.d(DatabaseHelper.class.getName(), book.toString()+" deleted from DB");
    }

    //---------------------------------------------------------------------
    //CHAPTERS

    public void insertChapter(Chapter chapter) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CHAPTER_KEY_BOOK_ID, chapter.getBook().getId());
        values.put(CHAPTER_KEY_HEADING, chapter.getHeading());
        values.put(CHAPTER_KEY_CHAPTER_NR, chapter.getChapter_nr());

        db.insert(TABLE_CHAPTERS, null, values);
        db.close();
        Log.d(DatabaseHelper.class.getName(), chapter.toString()+" added to DB");
    }

    public Chapter getChapter(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CHAPTERS, new String[] {CHAPTER_KEY_ID, CHAPTER_KEY_BOOK_ID,
                        CHAPTER_KEY_HEADING, CHAPTER_KEY_CHAPTER_NR},
                CHAPTER_KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Chapter chapter = new Chapter();
        chapter.setId(Integer.parseInt(cursor.getString(0)));
        chapter.setBook(getBook(Integer.parseInt(cursor.getString(1))));
        chapter.setHeading(cursor.getString(2));
        chapter.setChapter_nr(Integer.parseInt(cursor.getString(3)));
        chapter.setPages(getPagesByChapter(chapter.getId()));

        Log.d(DatabaseHelper.class.getName(), chapter.toString() + " read from DB");
        return chapter;
    }

    public List<Chapter> getChaptersByBook(int book_id) {
        List<Chapter> chapterList = new ArrayList<Chapter>();
        String selectQuery = "SELECT * FROM " + TABLE_CHAPTERS +
                " WHERE " + CHAPTER_KEY_BOOK_ID + "=" + book_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Chapter chapter = new Chapter();
                chapter.setId(Integer.parseInt(cursor.getString(0)));
                chapter.setBook(getBook(book_id));
                chapter.setHeading(cursor.getString(2));
                chapter.setChapter_nr(Integer.parseInt(cursor.getString(3)));
                chapter.setPages(getPagesByChapter(chapter.getId()));

                chapterList.add(chapter);
            } while (cursor.moveToNext());
        }

        Log.d(DatabaseHelper.class.getName(), "All chapters from book " + String.valueOf(book_id) + " read from DB");
        return chapterList;
    }

    public void deleteChapter(Chapter chapter) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHAPTERS, CHAPTER_KEY_ID + " = ?", new String[] { String.valueOf(chapter.getId()) });
        for(Page page : chapter.getPages()) {
            deletePage(page);
        }
        db.close();
        Log.d(DatabaseHelper.class.getName(), chapter.toString()+" deleted from DB");
    }

    //---------------------------------------------------------------------
    //PAGES

    public void insertPage(Page page) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PAGE_KEY_CHAPTER_ID, page.getChapter().getId());
        values.put(PAGE_KEY_PAGE_NR, page.getPage_nr());
        values.put(PAGE_KEY_CONTENT, page.getContent());

        db.insert(TABLE_PAGES, null, values);
        db.close();
        Log.d(DatabaseHelper.class.getName(), page.toString()+" added to DB");
    }

    public Page getPage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PAGES, new String[] {PAGE_KEY_ID, PAGE_KEY_CHAPTER_ID,
                        PAGE_KEY_PAGE_NR, PAGE_KEY_CONTENT},
                PAGE_KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Page page = new Page();
        page.setId(Integer.parseInt(cursor.getString(0)));
        page.setChapter(getChapter(Integer.parseInt(cursor.getString(1))));
        page.setPage_nr(Integer.parseInt(cursor.getString(2)));
        page.setContent(cursor.getString(3));
        Log.d(DatabaseHelper.class.getName(), page.toString() + " read from DB");
        return page;
    }

    public List<Page> getPagesByChapter(int chapter_id) {
        List<Page> pageList = new ArrayList<Page>();
        String selectQuery = "SELECT * FROM " + TABLE_PAGES +
                " WHERE " + PAGE_KEY_CHAPTER_ID + "=" + chapter_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Page page = new Page();
                page.setId(Integer.parseInt(cursor.getString(0)));
                page.setChapter(getChapter(Integer.parseInt(cursor.getString(1))));
                page.setPage_nr(Integer.parseInt(cursor.getString(2)));
                page.setContent(cursor.getString(3));

                pageList.add(page);
            } while (cursor.moveToNext());
        }

        Log.d(DatabaseHelper.class.getName(), "All pages from chapter " + String.valueOf(chapter_id) + " read from DB");
        return pageList;
    }

    public void deletePage(Page page) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PAGES, PAGE_KEY_ID + " = ?", new String[] { String.valueOf(page.getId()) });
        db.close();
        Log.d(DatabaseHelper.class.getName(), page.toString()+" deleted from DB");
    }
}
