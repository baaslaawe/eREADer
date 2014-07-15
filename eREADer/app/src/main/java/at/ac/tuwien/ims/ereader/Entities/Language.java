package at.ac.tuwien.ims.ereader.Entities;

/**
 * Created by Flo on 05.07.2014.
 */
public enum Language {
    DE(0),
    EN(1),
    ES(2);

    private final int code;
    private Language(int c) {
        code = c;
    }
    public int getCode() {
        return code;
    }
}
