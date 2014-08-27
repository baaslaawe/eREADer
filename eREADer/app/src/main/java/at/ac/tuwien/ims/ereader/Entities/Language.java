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

import java.util.Locale;

/**
 * Enum of available languages for the tts engine in this application.
 *
 * @author Florian Schuster
 */
public enum Language {
    DE(0, new Locale("de", "DE")),
    EN(1, new Locale("en", "US")),
    ES(2, new Locale("es", "ES")),
    FR(3, new Locale("fr", "FR")),
    UNKNOWN(4, new Locale("en", "US"));

    private final int code;
    private final Locale locale;
    private Language(int c, Locale l) {
        code=c;
        locale=l;
    }
    public int getCode() {
        return code;
    }
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the Language from given code of the language.
     *
     * @param lang language code
     * @return a Language object
     */
    public static Language getLanguageFromCode(int lang) {
        if (Language.DE.getCode() == lang)
            return Language.DE;
        else if (Language.EN.getCode() == lang)
            return Language.EN;
        else if (Language.ES.getCode() == lang)
            return Language.ES;
        else if (Language.FR.getCode() == lang)
            return Language.FR;
        else
            return Language.UNKNOWN;
    }

    /**
     * Languages are only saved as String in ebooks, so we try to convert it to our own format.
     *
     * @param l String of the language
     * @return language of the ebook or Language.UNKOWN if language is unknown
     */
    public static Language getLanguageFromString(String l) {
        if (l.equals("English") || l.equals("Englisch") || l.equals("en")) {
            return Language.EN;
        } else if(l.equals("German") || l.equals("Deutsch") || l.equals("de")) {
            return Language.DE;
        } else if(l.equals("Spanish") || l.equals("Spanisch") || l.equals("es")) {
            return Language.ES;
        } else if(l.equals("French") || l.equals("Franzoesisch") || l.equals("fr")) {
            return Language.FR;
        } else
            return Language.UNKNOWN;
    }
}
