package at.ac.tuwien.ims.ereader.Util;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.ac.tuwien.ims.ereader.R;

/**
 * Created by Flo on 11.07.2014.
 */
public class FragmentSearchbar extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.searchbar, null);
        return view;
    }
}
