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
 * Enum of available languages for the tts engine in this application.
 *
 * @author Florian Schuster
 */
public enum Language {
    DE(0),
    EN(1),
    ES(2),
    UNKNOWN(3);

    private final int code;
    private Language(int c) {
        code = c;
    }
    public int getCode() {
        return code;
    }
}
