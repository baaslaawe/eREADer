package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Locale;

import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;

/**
 * Created by Flo on 13.07.2014.
 */
public class Settings extends Activity {
    private ImageButton saveButton;
    private Button resetbtn;
    private Spinner spinner;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getActionBar() != null)
            getActionBar().hide();
        db=new DatabaseHelper(getApplicationContext());

        saveButton=(ImageButton)findViewById(R.id.savebtn_settings);
        saveButton.setOnClickListener(btnListener);

        resetbtn=(Button)findViewById(R.id.resetbtn);
        resetbtn.setOnClickListener(btnListener);

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
        int savedLang=pref.getInt("language", 1);
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
            } else if (v==resetbtn) {
                AlertDialog.Builder ab = new AlertDialog.Builder(Settings.this);
                ab.setMessage(getString(R.string.sure)).setPositiveButton(getString(R.string.positive), dialogClickListener)
                        .setNegativeButton(getString(R.string.negative), dialogClickListener).show();
            }
        }
    };

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    SharedPreferences pref = getSharedPreferences("settings", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.clear();
                    editor.apply();
                    db.resetDatabase();
                    Intent myIntent = new Intent(Settings.this, MyLibrary.class);
                    finish();
                    startActivity(myIntent);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //do nothing
                    break;
            }
        }
    };
}
