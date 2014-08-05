package at.ac.tuwien.ims.ereader.Entities;

/**
 * Created by Flo on 05.08.2014.
 */
public class DownloadHost {
    private String URL;
    private String how_to_string;
    private String site_name;

    public DownloadHost(String site_name, String URL, String how_to_string) {
        this.URL = URL;
        this.how_to_string = how_to_string;
        this.site_name = site_name;
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
}
