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
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Content;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Entities.Language;
import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.R;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * todo
 *
 * @author Florian Schuster
 */
public class BookService {
    private DatabaseHelper db;
    private String ebook_loading_failed;
    private String format_not_supported;
    private String could_not_download;
    private String no_title;
    private String no_author;

    public BookService(Context c) {
        db=new DatabaseHelper(c);
        ebook_loading_failed=c.getString(R.string.ebook_loading_failed);
        format_not_supported=c.getString(R.string.format_not_supported);
        could_not_download=c.getString(R.string.could_not_download);
        no_title=c.getString(R.string.no_title);
        no_author=c.getString(R.string.no_author);
    }

    public void addBookManually(String URI) throws ServiceException {
        if(URI.endsWith(".epub"))
            this.addBookAsEPUB(URI);
        else if(URI.endsWith(".pdf"))
            this.addBookAsPDF(URI);
        else if(URI.endsWith(".txt"))
            this.addBookAsTXT(URI);
        else
            throw new ServiceException(format_not_supported);
    }

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
                insertChapter(bookToSave, "" + actualContentNumber, cont, words);
                actualContentNumber++;
            }

        } catch (IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    private void addBookAsHTML(String URI) throws ServiceException {
        //todo download from web?
    }

    private void addBookAsPDF(String URI) throws ServiceException {
        try {
            PdfReader reader = new PdfReader(URI);
            StringBuilder str = new StringBuilder();
            String title="";
            String author="";
            String l="";
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextExtractionStrategy strategy;
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                str.append(strategy.getResultantText());
            }
            String content=str.toString();
            //todo
            //Book bookToSave=insertBook(title, author, lang);
            //insertChapter(bookToSave, "", content, content.split("\\s+").length);

        } catch(IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    private void addBookAsTXT(String URI) throws ServiceException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(URI));
            StringBuilder str = new StringBuilder();
            String line;
            String title="";
            String author="";
            String l="";
            while ((line = in.readLine()) != null) {
                if(title.isEmpty() || author.isEmpty() || l.isEmpty())
                    if(line.contains("Title: ") || line.contains("Titel: ")) {
                        String[] tokens = line.split(": ");
                        title = tokens[1];
                    } else if(line.contains("Author: ") || line.contains("Autor: ")) {
                        String[] tokens = line.split(": ");
                        author = tokens[1];
                    } else if(line.contains("Language: ") || line.contains("Sprache: ")) {
                        String[] tokens = line.split(": ");
                        l = tokens[1];
                    }
                str.append(line);
            }
            in.close();
            String content=str.toString().trim();

            if(content.isEmpty())
                throw new ServiceException(ebook_loading_failed);

            Language lang=getLanguageFromString(l);
            //Book bookToSave=insertBook(title, author, lang);

            //todo split in contents
            //insertChapter(bookToSave, "", content, content.split("\\s+").length);

        } catch(IOException e) {
            throw new ServiceException(ebook_loading_failed);
        }
    }

    private Language getLanguageFromString(String l) throws ServiceException {
        if (l.equals("English") || l.equals("Englisch") || l.equals("en")) {
            return Language.EN;
        } else if(l.equals("German") || l.equals("Deutsch") || l.equals("de")) {
            return Language.DE;
        } else if(l.equals("Spanish") || l.equals("Spanisch") || l.equals("es")) {
            return Language.ES;
        } else
            return Language.UNKNOWN;
    }

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

    private Content insertChapter(Book bookOfChapter, String heading, String content, int words) throws ServiceException {
        if(bookOfChapter==null || content.isEmpty() || words<0)
            throw new ServiceException(ebook_loading_failed);

        Content c=new Content(bookOfChapter, heading, content, words);
        long id=db.insertContent(c);
        return db.getContent(id);
    }

    private CurrentPosition insertCurrentPosition(long book_id, int currentChapterInBook, int currentSentence) throws ServiceException {
        if(book_id<0 || currentChapterInBook<0 || currentSentence<0)
            throw new ServiceException(ebook_loading_failed);

        CurrentPosition c=new CurrentPosition(book_id, currentChapterInBook, currentSentence);
        long id=db.insertCurrentPosition(c);
        return db.getCurrentPosition(id);
    }

    public List<Book> getAllBooks() {
        return db.getAllBooks();
    }

    public void deleteBook(long book_id) {
        if(book_id<0)
            return;
        db.deleteBook(book_id);
    }

    public Book getBook(long book_id) {
        return db.getBook(book_id);
    }

    public void updateBook(Book book) {
        db.updateBook(book);
    }

    public List<Content> getChaptersOfBook(long book_id) {
        return db.getContentsByBook(book_id);
    }

    public List<Content> getLightweightChaptersOfBook(long book_id) {
        return db.getLightweightContents(book_id);
    }

    public CurrentPosition getCurrentPosition(long book_id) {
        return db.getCurrentPosition(book_id);
    }

    public void updateCurrentPosition(CurrentPosition c) {
        db.updateCurrentPosition(c);
    }

    public int getNumberOfWords(long content_id) {
        return db.getContent(content_id).getWords();
    }
}