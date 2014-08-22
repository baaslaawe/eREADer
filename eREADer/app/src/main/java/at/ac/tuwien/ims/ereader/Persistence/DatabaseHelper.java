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
import at.ac.tuwien.ims.ereader.Entities.Content;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Entities.Language;

/**
 * Created by Flo on 14.07.2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BookCRUD, ContentCRUD, CurrentPositionCRUD {
    private static final String DATABASE_NAME = "eREADerDB";
    private static final int DATABASE_VERSION = 1;

    //---------------------------------------------------------------------
    //BOOKS
    private static final String TABLE_BOOKS = "books";

    private static final String BOOK_KEY_ID = "id";
    private static final String BOOK_KEY_TITLE = "title";
    private static final String BOOK_KEY_AUTHOR = "author";
    private static final String BOOK_KEY_LANGUAGE = "lang";

    private static final String CREATE_BOOK_TABLE =
            "CREATE TABLE " + TABLE_BOOKS + "("
                    + BOOK_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BOOK_KEY_TITLE + " TEXT NOT NULL,"
                    + BOOK_KEY_AUTHOR + " TEXT NOT NULL,"
                    + BOOK_KEY_LANGUAGE + " INTEGER NOT NULL);";

    private static final String DROP_BOOK_TABLE = "DROP TABLE IF EXISTS " + TABLE_BOOKS;

    //---------------------------------------------------------------------
    //CONTENTS
    private static final String TABLE_CONTENTS = "contents";

    private static final String CONTENTS_KEY_ID = "id";
    private static final String CONTENTS_KEY_BOOK_ID = "book_id";
    private static final String CONTENTS_KEY_HEADING = "heading";
    private static final String CONTENTS_KEY_CONTENT = "content";

    private static final String CREATE_CONTENTS_TABLE =
            "CREATE TABLE " + TABLE_CONTENTS + "("
                    + CONTENTS_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + CONTENTS_KEY_BOOK_ID + " INTEGER NOT NULL,"
                    + CONTENTS_KEY_HEADING + " TEXT NOT NULL,"
                    + CONTENTS_KEY_CONTENT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + CONTENTS_KEY_BOOK_ID + ") REFERENCES "
                    + TABLE_BOOKS +"(" + BOOK_KEY_ID+ "));";

    private static final String DROP_CONTENTS_TABLE = "DROP TABLE IF EXISTS " + TABLE_CONTENTS;

    //---------------------------------------------------------------------
    //CURRENT POSITION
    private static final String TABLE_CURR = "curr";

    private static final String CURR_KEY_BOOK_ID = "book_id";
    private static final String CURR_KEY_CHAPTER = "current_chapter";
    private static final String CURR_KEY_SENTENCE = "current_sentence";

    private static final String CREATE_CURR_TABLE =
            "CREATE TABLE " + TABLE_CURR + "("
                    + CURR_KEY_BOOK_ID + " INTEGER PRIMARY KEY,"
                    + CURR_KEY_CHAPTER + " INTEGER NOT NULL,"
                    + CURR_KEY_SENTENCE + " INTEGER NOT NULL);";

    private static final String DROP_CURR_TABLE = "DROP TABLE IF EXISTS " + TABLE_CURR;

    //---------------------------------------------------------------------

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_TABLE);
        db.execSQL(CREATE_CONTENTS_TABLE);
        db.execSQL(CREATE_CURR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DatabaseHelper.class.getName(), "Upgrading Database "+TABLE_BOOKS+" from Version "
                + oldVersion + "to " + newVersion);
        db.execSQL(DROP_BOOK_TABLE);

        Log.d(DatabaseHelper.class.getName(), "Upgrading Database "+ TABLE_CONTENTS +" from Version "
                + oldVersion + "to " + newVersion);
        db.execSQL(DROP_CONTENTS_TABLE);

        Log.d(DatabaseHelper.class.getName(), "Upgrading Database "+TABLE_CURR+" from Version "
                + oldVersion + "to " + newVersion);
        db.execSQL(DROP_CURR_TABLE);

        onCreate(db);
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DROP_BOOK_TABLE);
        db.execSQL(DROP_CONTENTS_TABLE);
        db.execSQL(DROP_CURR_TABLE);

        db.execSQL(CREATE_BOOK_TABLE);
        db.execSQL(CREATE_CONTENTS_TABLE);
        db.execSQL(CREATE_CURR_TABLE);

        db.close();
        Log.d(DatabaseHelper.class.getName(), "DB was reset");
    }

    //---------------------------------------------------------------------
    //BOOKS

    public long insertBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BOOK_KEY_TITLE, book.getTitle());
        values.put(BOOK_KEY_AUTHOR, book.getAuthor());
        values.put(BOOK_KEY_LANGUAGE, book.getLanguage().getCode());

        long id=db.insert(TABLE_BOOKS, null, values);
        db.close();
        Log.d(DatabaseHelper.class.getName(), book.toString()+" added to DB");
        return id;
    }

    public Book getBook(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKS, new String[] {BOOK_KEY_ID, BOOK_KEY_TITLE,
                        BOOK_KEY_AUTHOR, BOOK_KEY_LANGUAGE},
                BOOK_KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Language langu=null;
        int lang=Integer.parseInt(cursor.getString(3));
        if (Language.DE.getCode() == lang)
            langu=Language.DE;
        else if (Language.EN.getCode() == lang)
            langu=Language.EN;
        else if (Language.ES.getCode() == lang)
            langu=Language.ES;
        Book book = new Book(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                langu);
        Log.d(DatabaseHelper.class.getName(), book.toString() + " read from DB");
        return book;
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<Book>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + BOOK_KEY_ID;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Language langu=null;
                int lang=Integer.parseInt(cursor.getString(3));
                if (Language.DE.getCode() == lang)
                    langu=Language.DE;
                else if (Language.EN.getCode() == lang)
                    langu=Language.EN;
                else if (Language.ES.getCode() == lang)
                    langu=Language.ES;
                Book book = new Book(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                        langu);
                bookList.add(book);
                Log.d(DatabaseHelper.class.getName(), "book got from db: "+book.getId());
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

    public void deleteBook(long book_id) {
        Book book=getBook(book_id);
        for(Content content : getContentsByBook(book_id)) {
            deleteContent(content.getId());
        }
        if(book==null)
            return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKS, BOOK_KEY_ID + " = ?", new String[] { String.valueOf(book.getId()) });
        db.close();
        Log.d(DatabaseHelper.class.getName(), book.toString()+" deleted from DB");
    }

    //---------------------------------------------------------------------
    //CHAPTERS

    public long insertContent(Content content) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTENTS_KEY_BOOK_ID, content.getBook().getId());
        values.put(CONTENTS_KEY_HEADING, content.getHeading());
        values.put(CONTENTS_KEY_CONTENT, content.getContent());

        long id=db.insert(TABLE_CONTENTS, null, values);
        db.close();
        Log.d(DatabaseHelper.class.getName(), content.toString()+" added to DB");
        return id;
    }

    public Content getContent(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTENTS, new String[] {CONTENTS_KEY_ID, CONTENTS_KEY_BOOK_ID,
                        CONTENTS_KEY_HEADING, CONTENTS_KEY_CONTENT},
                CONTENTS_KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Content content = new Content(Integer.parseInt(cursor.getString(0)),
                getBook(Integer.parseInt(cursor.getString(1))),
                cursor.getString(2),
                cursor.getString(3));
        Log.d(DatabaseHelper.class.getName(), content.toString() + " read from DB");
        return content;
    }

    public List<Content> getLightweightContents(long book_id) {
        List<Content> contentList = new ArrayList<Content>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTENTS +
                " WHERE " + CONTENTS_KEY_BOOK_ID + "=" + book_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Content content = new Content(Integer.parseInt(cursor.getString(0)),
                        getBook(Integer.parseInt(cursor.getString(1))),
                        cursor.getString(2));
                contentList.add(content);
            } while (cursor.moveToNext());
        }

        Log.d(DatabaseHelper.class.getName(), "All chapters without content from book " + String.valueOf(book_id) + " read from DB");
        return contentList;
    }

    public List<Content> getContentsByBook(long book_id) {
        List<Content> contentList = new ArrayList<Content>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTENTS +
                " WHERE " + CONTENTS_KEY_BOOK_ID + "=" + book_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Content content = new Content(Integer.parseInt(cursor.getString(0)), getBook(Integer.parseInt(cursor.getString(1))),
                        cursor.getString(2), cursor.getString(3));
                contentList.add(content);
            } while (cursor.moveToNext());
        }

        Log.d(DatabaseHelper.class.getName(), "All chapters from book " + String.valueOf(book_id) + " read from DB");
        return contentList;
    }

    public void deleteContent(long chapter_id) {
        Content content=getContent(chapter_id);
        if(content==null)
            return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTENTS, CONTENTS_KEY_ID + " = ?", new String[] { String.valueOf(content.getId()) });
        db.close();
        Log.d(DatabaseHelper.class.getName(), content.toString()+" deleted from DB");
    }

    //---------------------------------------------------------------------
    //CURRENT POSITION

    public long insertCurrentPosition(CurrentPosition curr) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CURR_KEY_BOOK_ID, curr.getBook_id());
        values.put(CURR_KEY_CHAPTER, curr.getCurrentContent());
        values.put(CURR_KEY_SENTENCE, curr.getCurrentSentence());

        long id=db.insert(TABLE_CURR, null, values);
        db.close();
        Log.d(DatabaseHelper.class.getName(), curr.toString()+" added to DB");
        return id;
    }

    public void updateCurrentPosition(CurrentPosition curr) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CURR_KEY_CHAPTER, curr.getCurrentContent());
        values.put(CURR_KEY_SENTENCE, curr.getCurrentSentence());

        db.update(TABLE_CURR, values, CURR_KEY_BOOK_ID + " = ?", new String[]{String.valueOf(curr.getBook_id())});
        Log.d(DatabaseHelper.class.getName(), curr.toString() + " updated in DB");
    }

    public CurrentPosition getCurrentPosition(long book_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CURR, new String[] {CURR_KEY_BOOK_ID, CURR_KEY_CHAPTER, CURR_KEY_SENTENCE},
                CURR_KEY_BOOK_ID + "=?", new String[] { String.valueOf(book_id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        CurrentPosition c=new CurrentPosition(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)),
                Integer.parseInt(cursor.getString(2)));
        Log.d(DatabaseHelper.class.getName(), c.toString() + " read from DB");
        return c;
    }

    public void deleteCurrentPosition(long book_id) {
        CurrentPosition c = getCurrentPosition(book_id);
        if(c==null)
            return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CURR, CURR_KEY_BOOK_ID + " = ?", new String[] { String.valueOf(c.getBook_id()) });
        db.close();
        Log.d(DatabaseHelper.class.getName(), c.toString()+" deleted from DB");
    }
}
