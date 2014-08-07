package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;

import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;

/**
 * Created by Flo on 13.07.2014.
 */
public class SettingsActivity extends Activity {
    private ImageButton saveButton;
    private ImageButton menuBtn;
    private Button resetbtn;
    private Spinner spinner;
    private DatabaseHelper db;
    private SidebarMenu sbMenu;

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

        menuBtn=(ImageButton)findViewById(R.id.optnbtn_settings);
        menuBtn.setOnClickListener(btnListener);

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


        sbMenu=new SidebarMenu(this, false, true, false);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==saveButton) {
                SharedPreferences pref = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = pref.edit();

                //language todo do we need this? ->no
                int item=spinner.getSelectedItemPosition();
                editor.putInt("language", item);
                editor.apply();

                //todo folder for downloaded books
                //todo voice rate and maybe different voices
                showMessage(getString(R.string.settings_saved));
            } else if (v==resetbtn) {
                AlertDialog.Builder ab = new AlertDialog.Builder(SettingsActivity.this);
                ab.setMessage(getString(R.string.sure)).setPositiveButton(getString(R.string.positive), dialogClickListener)
                        .setNegativeButton(getString(R.string.negative), dialogClickListener).show();
            } else if(v==menuBtn) {
                if(sbMenu.getMenuDrawer().isMenuVisible())
                    sbMenu.getMenuDrawer().closeMenu();
                else
                    sbMenu.getMenuDrawer().openMenu();
            }
        }
    };

    @Override
    public void onBackPressed() {
        final int drawerState = sbMenu.getMenuDrawer().getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            sbMenu.getMenuDrawer().closeMenu();
            return;
        }
        super.onBackPressed();
    }

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
                    Intent myIntent = new Intent(SettingsActivity.this, MyLibraryActivity.class);
                    finish();
                    startActivity(myIntent);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //do nothing
                    break;
            }
        }
    };

    private void showMessage(String message) {
        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
