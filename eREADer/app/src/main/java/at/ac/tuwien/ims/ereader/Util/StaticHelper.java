package at.ac.tuwien.ims.ereader.Util;

/**
 * Created by Flo on 11.08.2014.
 */
public class StaticHelper {
    public static final String COLOR_Blue="#12c2b9";
    public static final String COLOR_White="#ffd4d4d4";

    public static final long typeface_Standard=0;
    public static final long typeface_GeoSans=1;
    public static final long typeface_Libertine=2;

    public static final long typesize_small=0;
    public static final long typesize_medium=1;
    public static final long typesize_large=2;
    public static final int typesize_range_down=10;
    public static final int typesize_range_up=7;

    public static final String BROADCAST_ACTION = "at.ac.tuwien.ims.ereader.Services.BROADCAST";
    public static final String ACTION_PLAY="at.ac.tuwien.ims.ereader.Services.ACTION_PLAY";
    public static final String ACTION_PAUSE="at.ac.tuwien.ims.ereader.Services.ACTION_PAUSE";
    public static final String ACTION_CLOSE="at.ac.tuwien.ims.ereader.Services.ACTION_CLOSE";

    public static final int NOTIFICATION_ID=1;

    public static final float normal_Speechrate=1.f;

    public static float seekbarToRate(int progress) {
        if(progress>=0&&progress<=5)
            return 0.5f;
        else if(progress>5&&progress<=15)
            return 0.6f;
        else if(progress>15&&progress<=25)
            return 0.7f;
        else if(progress>25&&progress<=35)
            return 0.8f;
        else if(progress>35&&progress<=45)
            return 0.9f;
        else if(progress>45&&progress<=55)
            return 1.0f;
        else if(progress>55&&progress<=65)
            return 1.2f;
        else if(progress>65&&progress<=75)
            return 1.4f;
        else if(progress>75&&progress<=85)
            return 1.6f;
        else if(progress>85&&progress<=95)
            return 1.8f;
        else
            return 2.f;
    }

    public static int rateToSeekbar(float rate) {
        if(rate==0.5f)
            return 3;
        else if(rate==0.6f)
            return 10;
        else if(rate==0.7f)
            return 20;
        else if(rate==0.8f)
            return 30;
        else if(rate==0.9f)
            return 40;
        else if(rate==1.f)
            return 50;
        else if(rate==1.2f)
            return 60;
        else if(rate==1.4f)
            return 70;
        else if(rate==1.6f)
            return 80;
        else if(rate==1.8f)
            return 90;
        else
            return 100;
    }
}
