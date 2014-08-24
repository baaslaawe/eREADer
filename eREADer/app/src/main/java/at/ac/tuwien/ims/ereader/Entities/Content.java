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
package at.ac.tuwien.ims.ereader.Entities;

/**
 * Definition of a content entity.
 *
 * @author Florian Schuster
 */
public class Content {
    private long id;
    private Book book;
    private String heading;
    private String content;
    private int words;

    public Content(long id, Book book, String heading, String content, int words) {
        this.id = id;
        this.book = book;
        this.heading = heading;
        this.content = content;
        this.words = words;
    }

    public Content(long id, Book book, String heading, int words) { //lightweight for displaying in toc
        this.id = id;
        this.book = book;
        this.heading = heading;
        this.words = words;
    }

    public Content(Book book, String heading, String content, int words) {
        this.book = book;
        this.heading = heading;
        this.content = content;
        this.words = words;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String toString() {
        return heading+" ("+book.getTitle()+")";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }
}
