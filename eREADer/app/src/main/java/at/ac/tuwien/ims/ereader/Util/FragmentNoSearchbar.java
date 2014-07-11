package at.ac.tuwien.ims.ereader.Util;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import at.ac.tuwien.ims.ereader.R;


/**
 * Created by Flo on 11.07.2014.
 */
public class FragmentNoSearchbar extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.no_searchbar, null);
        return view;
    }
}
