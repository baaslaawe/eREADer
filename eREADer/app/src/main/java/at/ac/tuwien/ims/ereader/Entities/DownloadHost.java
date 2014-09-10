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
 * Definition of a downloadhoast entity.
 *
 * @author Florian Schuster
 */
public class DownloadHost {
    private String URL;
    private String how_to_string;
    private String site_name;
    private String language;

    public DownloadHost(String site_name, String URL, String how_to_string, String language) {
        this.URL = URL;
        this.how_to_string = how_to_string;
        this.site_name = site_name;
        this.language=language;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getHow_to_string() {
        return how_to_string;
    }

    public void setHow_to_string(String how_to_string) {
        this.how_to_string = how_to_string;
    }

    public String getSite_name() {
        return site_name;
    }

    public void setSite_name(String site_name) {
        this.site_name = site_name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
