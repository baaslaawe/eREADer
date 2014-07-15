package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Flo on 13.07.2014.
 */
public class Settings extends Activity {
    private ImageButton saveButton;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getActionBar() != null)
            getActionBar().hide();

        saveButton=(ImageButton)findViewById(R.id.savebtn_settings);
        saveButton.setOnClickListener(btnListener);

        ArrayList<String> spinnerArray=new ArrayList<String>();
        spinnerArray.add(getString(R.string.ger));
        spinnerArray.add(getString(R.string.eng));
        spinnerArray.add(getString(R.string.esp));

        spinner = (Spinner) findViewById(R.id.langSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item,
                spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);

        SharedPreferences pref = getSharedPreferences("settings", 0);
        int savedLang=pref.getInt("language", 0);
        spinner.setSelection(savedLang, false);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==saveButton) {
                SharedPreferences pref = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = pref.edit();

                //language todo do we need this?
                int item=spinner.getSelectedItemPosition();
                editor.putInt("language", item);
                editor.apply();

                //todo other settings

                finish();
            }
        }
    };
}
