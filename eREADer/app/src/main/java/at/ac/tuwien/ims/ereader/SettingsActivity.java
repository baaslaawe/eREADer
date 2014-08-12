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

import java.util.Arrays;

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

    private int size_small;
    private int size_medium;
    private int size_large;

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

        SharedPreferences settings = getSharedPreferences("settings", 0);

        fontsize=(Spinner) findViewById(R.id.font_size);
        String[] array1;
        if (standardTextSize>StaticHelper.typesize_range) {
            size_small = standardTextSize - StaticHelper.typesize_range;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range;
        } else {
            size_small = standardTextSize;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range;
        }
        array1 = new String[]{getString(R.string.small), getString(R.string.medium), getString(R.string.large)};

        fontsize.setAdapter(new ArrayAdapter(this, R.layout.spinner_item, array1));
        long fonts=settings.getLong("font_size", 1);
        fontsize.setSelection((int)fonts);
        fontsize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (id==StaticHelper.typesize_small) {
                    testtext.setTextSize(size_small);
                } else if (id==StaticHelper.typesize_large) {
                    testtext.setTextSize(size_large);
                } else {
                    testtext.setTextSize(size_medium);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        fonttype=(Spinner) findViewById(R.id.font_type);
        face0 = testtext.getTypeface();
        face1 = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
        face2 = Typeface.createFromAsset(getAssets(), "fonts/LinLibertine_R.ttf");
        String[] array2={getString(R.string.standard), getString(R.string.geosans), getString(R.string.libertine)};

        fonttype.setAdapter(new ArrayAdapter(this, R.layout.spinner_item, array2));
        long fontt=settings.getLong("font_type", 0);
        fonttype.setSelection((int) fontt);
        fonttype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (id==StaticHelper.typeface_GeoSans) {
                    testtext.setTypeface(face1);
                } else if (id==StaticHelper.typeface_Libertine) {
                    testtext.setTypeface(face2);
                } else {
                    testtext.setTypeface(face0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        if (fonts==StaticHelper.typesize_small) {
            testtext.setTextSize(size_small);
        } else if (fonts==StaticHelper.typesize_large) {
            testtext.setTextSize(size_large);
        } else {
            testtext.setTextSize(size_medium);
        }

        if (fontt==StaticHelper.typeface_GeoSans) {
            testtext.setTypeface(face1);
        } else if (fontt==StaticHelper.typeface_Libertine) {
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
                editor.putLong("font_size", fontsize.getSelectedItemId());
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
