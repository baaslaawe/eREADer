package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.Arrays;

import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;

/**
 * Created by Flo on 13.07.2014.
 */
public class SettingsActivity extends Activity {
    private ImageButton saveButton;
    private ImageButton menuBtn;
    private Button resetbtn;
    private DatabaseHelper db;
    private SidebarMenu sbMenu;
    private Spinner fonttype;
    private Spinner fontsize;
    private TextView testtext;

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

        sbMenu=new SidebarMenu(this, false, true, false);

        testtext=(TextView) findViewById(R.id.test_sentence);
        int sizenow=(int)testtext.getTextSize();
        Integer[] array1;
        if (sizenow>6)
            array1=new Integer[]{sizenow-6, sizenow-4,sizenow-2,sizenow,sizenow+2,sizenow+4, sizenow+6};
        else
            array1=new Integer[]{sizenow,sizenow+2,sizenow+4, sizenow+6};

        SharedPreferences settings = getSharedPreferences("settings", 0);

        fontsize=(Spinner) findViewById(R.id.font_size);
        fontsize.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, array1));
        Integer fonts=settings.getInt("font_size", sizenow);
        fontsize.setSelection(Arrays.asList(array1).indexOf(fonts));
        fontsize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                testtext.setTextSize((Integer)fontsize.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        fonttype=(Spinner) findViewById(R.id.font_type);
        String[] array2={"Arial","Times New Roman","Standard"};
        fonttype.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, array2));
        String fontt=settings.getString("font_type", "Times New Roman");
        fonttype.setSelection(Arrays.asList(array2).indexOf(fontt));
        fonttype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //Typeface face = Typeface.createFromAsset(getAssets(), "font/Cooper.otf"); //todo which typefaces?
                //testtext.setTypeface();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        testtext.setTextSize(fonts);
        //Typeface face = Typeface.createFromAsset(getAssets(), "font/Cooper.otf");
        //testtext.setTypeface();
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==saveButton) {
                SharedPreferences settings = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = settings.edit();

                //todo other settings?
                editor.putInt("font_size", (Integer)fontsize.getSelectedItem());
                editor.putString("font_type", (String)fonttype.getSelectedItem());
                editor.apply();
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
