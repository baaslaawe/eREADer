/*
    This file is part of the eReader application.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.HashMap;

import at.ac.tuwien.ims.ereader.Persistence.DatabaseHelper;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;
import at.ac.tuwien.ims.ereader.Util.StaticHelper;

/**
 * Activity to change settings .
 *
 * @author Florian Schuster
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
    private TextView longertesttext;
    private SeekBar speechRateBar;
    private ImageButton playButtonRate;

    private Typeface face0;
    private Typeface face1;
    private Typeface face2;

    private int size_small;
    private int size_medium;
    private int size_large;

    private TextToSpeech ttsService;
    private boolean ttsInitDone=false;
    private float speechrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SuperActivityToast.onRestoreState(savedInstanceState, SettingsActivity.this);
        setContentView(R.layout.activity_settings);
        if (getActionBar() != null)
            getActionBar().hide();
        db=new DatabaseHelper(getApplicationContext());

        saveButton=(ImageButton)findViewById(R.id.savebtn_settings);
        saveButton.setOnTouchListener(btnListener);
        resetbtn=(Button)findViewById(R.id.resetbtn);
        resetbtn.setOnTouchListener(btnListener);
        menuBtn=(ImageButton)findViewById(R.id.optnbtn_settings);
        menuBtn.setOnTouchListener(btnListener);
        sbMenu=new SidebarMenu(this, false, true, false, false);

        ttsService=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    ttsService.setLanguage(getResources().getConfiguration().locale);
                    ttsInitDone=true;
                }
            }
        });

        testtext=(TextView) findViewById(R.id.test_sentence);
        int standardTextSize=(int)testtext.getTextSize();

        SharedPreferences settings = getSharedPreferences("settings", 0);

        fontsize=(Spinner) findViewById(R.id.font_size);
        String[] array1;
        if (standardTextSize>StaticHelper.typesize_range_down) {
            size_small = standardTextSize - StaticHelper.typesize_range_down;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range_up;
        } else {
            size_small = standardTextSize;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range_up;
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

        longertesttext=(TextView) findViewById(R.id.longer_test_sentence);
        speechRateBar=(SeekBar)findViewById(R.id.seekBar_settings);
        speechRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speechrate=StaticHelper.seekbarToRate(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        speechrate=settings.getFloat("tts_rate", StaticHelper.normal_Speechrate);
        speechRateBar.setProgress(StaticHelper.rateToSeekbar(speechrate));


        playButtonRate=(ImageButton)findViewById(R.id.play_settings);
        playButtonRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ttsInitDone &&!ttsService.isSpeaking()) {
                    ttsService.setLanguage(getResources().getConfiguration().locale);
                    ttsService.setSpeechRate(speechrate);

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                    map.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true");
                    ttsService.speak(longertesttext.getText().toString(), TextToSpeech.QUEUE_FLUSH, map);
                } else {
                    ttsService.stop();
                }
            }
        });
    }

    private View.OnTouchListener btnListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent m) {
            if (v==saveButton) {
                if(m.getAction()== MotionEvent.ACTION_DOWN)
                    ((ImageButton)v).setImageResource(R.drawable.savebtn_pressed);
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    SharedPreferences settings = getSharedPreferences("settings", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putLong("font_size", fontsize.getSelectedItemId());
                    editor.putLong("font_type", fonttype.getSelectedItemId());
                    editor.putFloat("tts_rate", speechrate);
                    editor.apply();
                    showMessage(getString(R.string.settings_saved));
                    ((ImageButton)v).setImageResource(R.drawable.savebtn);
                }
            } else if (v==resetbtn) {
                if(m.getAction()== MotionEvent.ACTION_DOWN)
                    v.setBackgroundColor(Color.parseColor(StaticHelper.COLOR_Blue));
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(SettingsActivity.this);
                    ab.setMessage(getString(R.string.sure)).setPositiveButton(getString(R.string.positive), dialogClickListener)
                            .setNegativeButton(getString(R.string.negative), dialogClickListener).show();
                    v.setBackgroundColor(Color.parseColor(StaticHelper.COLOR_Grey));
                }
            } else if(v==menuBtn) {
                if(m.getAction()==MotionEvent.ACTION_UP)
                    if(sbMenu.getMenuDrawer().isMenuVisible())
                        sbMenu.getMenuDrawer().closeMenu();
                    else
                        sbMenu.getMenuDrawer().openMenu();
            }
            return true;
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
        SuperToast toast=new SuperToast(this);
        toast.setText(message);
        toast.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SuperActivityToast.onSaveState(outState);
    }
}
