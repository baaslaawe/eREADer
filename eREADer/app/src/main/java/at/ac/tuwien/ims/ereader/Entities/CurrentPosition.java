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
 * Definition of a currentposition entity.
 *
 * @author Florian Schuster
 */
public class CurrentPosition {
    private long book_id;
    private int currentContent;
    private int currentSentence;

    public CurrentPosition(long book_id, int currentContent, int currentSentence) {
        this.book_id = book_id;
        this.currentContent = currentContent;
        this.currentSentence = currentSentence;
    }

    public long getBook_id() {
        return book_id;
    }

    public void setBook_id(long book_id) {
        this.book_id = book_id;
    }

    public int getCurrentContent() {
        return currentContent;
    }

    public void setCurrentContent(int currentChapterInBook) {
        this.currentContent = currentChapterInBook;
    }

    public int getCurrentSentence() {
        return currentSentence;
    }

    public void setCurrentSentence(int currentSentence) {
        this.currentSentence = currentSentence;
    }

    @Override
    public String toString() {
        return "CurrentPosition{" +
                "book_id=" + book_id +
                ", currentContent=" + currentContent +
                ", currentSentence=" + currentSentence +
                '}';
    }
}
