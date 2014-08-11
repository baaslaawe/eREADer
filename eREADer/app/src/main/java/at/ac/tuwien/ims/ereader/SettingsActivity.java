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
import java.util.HashMap;

import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;
import at.ac.tuwien.ims.ereader.Util.StaticHelper;

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

    private Typeface face0;
    private Typeface face1;
    private Typeface face2;

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
        int standardTextSize=(int)testtext.getTextSize();

        Integer[] array1;
        if (standardTextSize>7)
            array1=new Integer[]{standardTextSize-5, standardTextSize, standardTextSize+5};
        else
            array1=new Integer[]{standardTextSize, standardTextSize+5};

        SharedPreferences settings = getSharedPreferences("settings", 0);

        fontsize=(Spinner) findViewById(R.id.font_size);
        fontsize.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, array1));
        Integer fonts=settings.getInt("font_size", standardTextSize);
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
        face0 = testtext.getTypeface();
        face1 = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
        face2 = Typeface.createFromAsset(getAssets(), "fonts/LinLibertine_R.ttf");
        String[] array2={getString(R.string.standard), getString(R.string.geosans), getString(R.string.libertine)};

        fonttype.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, array2));
        long fontt=settings.getLong("font_type", 0);
        fonttype.setSelection((int) fontt);
        fonttype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (id==StaticHelper.typeface_1) {
                    testtext.setTypeface(face1);
                } else if (id==StaticHelper.typeface_2) {
                    testtext.setTypeface(face2);
                } else {
                    testtext.setTypeface(face0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        testtext.setTextSize(fonts);
        if (fontt==StaticHelper.typeface_1) {
            testtext.setTypeface(face1);
        } else if (fontt==StaticHelper.typeface_2) {
            testtext.setTypeface(face2);
        } else {
            testtext.setTypeface(face0);
        }
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==saveButton) {
                SharedPreferences settings = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = settings.edit();

                //todo other settings?
                editor.putInt("font_size", (Integer)fontsize.getSelectedItem());
                editor.putLong("font_type", fonttype.getSelectedItemId());
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
