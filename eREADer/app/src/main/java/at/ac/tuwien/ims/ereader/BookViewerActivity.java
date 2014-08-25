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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Content;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Services.ReadingService;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;
import at.ac.tuwien.ims.ereader.Util.StaticHelper;

/**
 * Activity view and read/play a specific ebook.
 *
 * @author Florian Schuster
 */
public class BookViewerActivity extends Activity {
    private ImageButton optButton;
    private ImageButton ffButton;
    private ImageButton fbButton;
    private ImageButton playButton;
    private ImageButton volumeButton;

    private TextView content;
    private ScrollView contentScrollView;
    private TextView cont_heading;

    private Book book;
    private ReadingService readingService;
    private boolean serviceBound;

    private SuperActivityToast ttsDoneToast;
    private SidebarMenu sbMenu;

    private Typeface face0;
    private Typeface face1;
    private Typeface face2;

    private int size_small;
    private int size_medium;
    private int size_large;

    private long clicktime=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        if (getActionBar() != null)
            getActionBar().hide();

        BookService bookService=new BookService(this);
        book=bookService.getBook(getIntent().getExtras().getInt("book_id"));

        optButton=(ImageButton)findViewById(R.id.optnbtn_book);
        optButton.setOnTouchListener(btnListener);
        playButton=(ImageButton)findViewById(R.id.playbtn);
        playButton.setOnTouchListener(btnListener);
        ffButton=(ImageButton)findViewById(R.id.ffbtn);
        ffButton.setOnTouchListener(btnListener);
        fbButton=(ImageButton)findViewById(R.id.fbbtn);
        fbButton.setOnTouchListener(btnListener);
        volumeButton =(ImageButton)findViewById(R.id.volume_btn);
        volumeButton.setOnTouchListener(btnListener);

        TextView title=(TextView)findViewById(R.id.bktitletxt);
        title.setText(book.getTitle());
        cont_heading =(TextView)findViewById(R.id.chap_txt);
        content=(TextView)findViewById(R.id.book_text);
        contentScrollView =(ScrollView)findViewById(R.id.scrollv_for_text);

        int cha=getIntent().getExtras().getInt("chapter");
        String chapt;
        List<Content> contents =bookService.getContentsOfBook(book.getId());

        if (cha<0) {
            CurrentPosition c=bookService.getCurrentPosition(book.getId());
            chapt= contents.get(c.getCurrentContent()).getHeading();
        } else {
            int currSent=0;
            CurrentPosition c=bookService.getCurrentPosition(book.getId());
            if(cha==c.getCurrentContent()&&c.getCurrentSentence()!=0)
                currSent=bookService.getCurrentPosition(book.getId()).getCurrentSentence();
            bookService.updateCurrentPosition(new CurrentPosition(book.getId(), cha, currSent));
            chapt= contents.get(cha).getHeading();
        }

        content.setText(getString(R.string.loading));
        cont_heading.setText(chapt);
        sbMenu=new SidebarMenu(this, false, false, false);

        int standardTextSize=(int)content.getTextSize();
        if (standardTextSize>StaticHelper.typesize_range_down) {
            size_small = standardTextSize - StaticHelper.typesize_range_down;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range_up;
        } else {
            size_small = standardTextSize;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range_up;
        }
        face0 = content.getTypeface();
        face1 = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
        face2 = Typeface.createFromAsset(getAssets(), "fonts/LinLibertine_R.ttf");
        updateTextSettings();

        content.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (serviceBound && !readingService.isPlaying()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        clicktime = event.getEventTime();
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (clicktime != 0 && (event.getEventTime() - clicktime) >= 800) {
                            final Layout layout = ((TextView) v).getLayout();
                            if (layout != null) {
                                final int x = (int) event.getX();
                                final int y = (int) event.getY();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Spannable spanText = Spannable.Factory.getInstance().newSpannable(readingService.getCurrentContentString());
                                        int i[] = readingService.getIndicesOfCurrentSentence();
                                        int j[] = readingService.getIndicesOfClickedSentence(layout, x, y);
                                        if (i != null && j != null) {
                                            spanText.setSpan(new BackgroundColorSpan(Color.parseColor("#0FC1B8")), i[0], i[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            spanText.setSpan(new BackgroundColorSpan(Color.parseColor("#0FC1B8")), j[0], j[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            content.setText(spanText);
                                        }
                                    }
                                });

                                CharSequence[] items = {getString(R.string.start_from_here), getString(R.string.cancel)};
                                AlertDialog.Builder builder = new AlertDialog.Builder(BookViewerActivity.this);
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        if (item == 0) {
                                            readingService.setCurrentSentence(readingService.getSentenceNumberByClick(layout, x, y));
                                        }
                                        updateContent();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;
                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        updateContent();
                                    }
                                });
                                dialog.show();
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void updateScroll() {
        if(serviceBound)
            if(readingService.isPlaying()) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Layout layout=content.getLayout();
                        int height=contentScrollView.getHeight();
                        int scrollY=contentScrollView.getScrollY();
                        int firstVisibleLineNumber=layout.getLineForVertical(scrollY);
                        int lastVisibleLineNumber=layout.getLineForVertical(scrollY+height);
                        int halfOfLayoutLines=(lastVisibleLineNumber-firstVisibleLineNumber)/2;
                        int i=readingService.getIndicesOfCurrentSentence()[0]+(readingService.getIndicesOfCurrentSentence()[1]-readingService.getIndicesOfCurrentSentence()[0]);
                        int line=layout.getLineForOffset(i)-halfOfLayoutLines;
                        if(line>=0)
                            contentScrollView.smoothScrollTo(0, layout.getLineTop(line));
                    }
                });

                contentScrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            } else {
                contentScrollView.setOnTouchListener(null);
            }
    }

    /**
     * Method that updates the current content and applies a span to it that displays the current-
     * sentence that is read.
     *
     */
    private void updateContent() {
        if(serviceBound)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(readingService.getCurrentContentString());
                    int i[] = readingService.getIndicesOfCurrentSentence();
                    if (i!=null){
                        spanText.setSpan(new BackgroundColorSpan(Color.parseColor("#0FC1B8")), i[0], i[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        content.setText(spanText);
                    }
                }
            });
    }

    /**
     * Method that updates font-size and font-type of the content TextView.
     *
     */
    private void updateTextSettings() {
        SharedPreferences settings = getSharedPreferences("settings", 0);

        long fonts=settings.getLong("font_size", StaticHelper.typesize_medium);
        if (fonts==StaticHelper.typesize_small) {
            content.setTextSize(size_small);
        } else if (fonts==StaticHelper.typesize_large) {
            content.setTextSize(size_large);
        } else {
            content.setTextSize(size_medium);
        }

        long id=settings.getLong("font_type", StaticHelper.typeface_Standard);
        if (id==StaticHelper.typeface_GeoSans) {
            content.setTypeface(face1);
        } else if (id==StaticHelper.typeface_Libertine) {
            content.setTypeface(face2);
        } else {
            content.setTypeface(face0);
        }
    }

    /**
     * Method that updates the current content heading TextView.
     *
     */
    private void updateContentHeader() {
        if(serviceBound)
            cont_heading.setText(readingService.getCurrContentHeading());
    }

    /**
     * Updates the state of fast-forward, fast-backward and volume buttons.
     *
     */
    private void updateButtons() {
        if(serviceBound) {
            if (readingService.getCurrentContent() == 0) {
                fbButton.setAlpha(0.2f);
                fbButton.setEnabled(false);
            }
            if (readingService.getCurrentContent() == readingService.getNumberOfContentsInCurrentBook() - 1) {
                ffButton.setAlpha(0.2f);
                ffButton.setEnabled(false);
            }
            if (readingService.getCurrentContent() > 0) {
                fbButton.setAlpha(1.f);
                fbButton.setEnabled(true);
            }
            if (readingService.getCurrentContent() < readingService.getNumberOfContentsInCurrentBook() - 1) {
                ffButton.setAlpha(1.f);
                ffButton.setEnabled(true);
            }

            if (readingService.getMuted()) {
                volumeButton.setImageDrawable(getResources().getDrawable(R.drawable.muted));
            } else {
                volumeButton.setImageDrawable(getResources().getDrawable(R.drawable.notmuted));
            }
        }
    }

    @Override
    public void onBackPressed() {
        final int drawerState = sbMenu.getMenuDrawer().getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            sbMenu.getMenuDrawer().closeMenu();
            return;
        }
        super.onBackPressed();
    }

    private View.OnTouchListener btnListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent m) {
            if (v==optButton) {
                if(m.getAction()==MotionEvent.ACTION_UP)
                    if(sbMenu.getMenuDrawer().isMenuVisible())
                        sbMenu.getMenuDrawer().closeMenu();
                    else
                        sbMenu.getMenuDrawer().openMenu();
            } else if (v==playButton && serviceBound) {
                if(m.getAction()== MotionEvent.ACTION_DOWN) {
                    if (readingService.isPlaying())
                        playButton.setImageResource(R.drawable.pausebtn_pressed);
                    else
                        playButton.setImageResource(R.drawable.playbtn_pressed);
                } else if(m.getAction()==MotionEvent.ACTION_UP) {
                    if (readingService.isPlaying()) {
                        playButton.setImageResource(R.drawable.playbtn);
                        readingService.pause();
                    } else {
                        playButton.setImageResource(R.drawable.pausebtn);
                        readingService.play();
                    }
                }
            } else if (v==ffButton && serviceBound) {
                if(m.getAction()== MotionEvent.ACTION_DOWN)
                    ffButton.setImageResource(R.drawable.ffbtn_pressed);
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    ffButton.setImageResource(R.drawable.ffbtn);
                    readingService.next();
                }
            } else if(v==fbButton && serviceBound) {
                if(m.getAction()== MotionEvent.ACTION_DOWN)
                    fbButton.setImageResource(R.drawable.fbbtn_pressed);
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    fbButton.setImageResource(R.drawable.fbbtn);
                    readingService.last();
                }
            } else if (v== volumeButton && serviceBound) {
                if(m.getAction()==MotionEvent.ACTION_UP)
                    if (readingService.getMuted()) {
                        readingService.setMuted(false);
                        volumeButton.setImageDrawable(getResources().getDrawable(R.drawable.notmuted));
                    } else {
                        readingService.setMuted(true);
                        volumeButton.setImageDrawable(getResources().getDrawable(R.drawable.muted));
                    }
            }
            updateButtons();
            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, ReadingService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        if (serviceBound) {
            readingService.updateBook(book);
            if(readingService.isPlaying())
                playButton.setImageResource(R.drawable.pausebtn);
            else
                playButton.setImageResource(R.drawable.playbtn);
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            readingService = null;
            Log.d(BookViewerActivity.class.getName(), "Service is disconnected");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBound = true;
            ReadingService.ReadingServiceBinder mLocalBinder = (ReadingService.ReadingServiceBinder)service;
            readingService = mLocalBinder.getService();
            readingService.updateBook(book);
            if(readingService.isPlaying())
                playButton.setImageResource(R.drawable.pausebtn);
            else
                playButton.setImageResource(R.drawable.playbtn);
            Log.d(BookViewerActivity.class.getName(), "Service is connected");
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(StaticHelper.BROADCAST_ACTION));

        if (serviceBound) {
            readingService.updateBook(book);
            if(readingService.isPlaying())
                playButton.setImageResource(R.drawable.pausebtn);
            else
                playButton.setImageResource(R.drawable.playbtn);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceBound) {
            unbindService(mConnection);
            serviceBound = false;
        }
    }

    /**
     * Broadcast receiver that receives broadcasts from the ReadingService.
     * Handles content updates and the TTS toast informer.
     *
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(StaticHelper.BROADCAST_ACTION)) {
                Bundle extra = intent.getExtras();
                if (extra.getBoolean("update")) {
                    updateContent();
                    updateContentHeader();
                    updateButtons();
                    updateScroll();
                }

                if(extra.getBoolean("ttsStart")) {
                    ttsDoneToast = new SuperActivityToast(BookViewerActivity.this, SuperToast.Type.PROGRESS);
                    ttsDoneToast.setText(getString(R.string.ttsDone_str));
                    ttsDoneToast.setIndeterminate(true);
                    ttsDoneToast.setProgressIndeterminate(true);
                    ttsDoneToast.show();
                    playButton.setEnabled(false);
                    contentScrollView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });
                } else if (extra.getBoolean("ttsDone")) {
                    ttsDoneToast.dismiss();
                    playButton.setEnabled(true);
                    contentScrollView.setOnTouchListener(null);
                }
            }
        }
    };

    private void showMessage(String message) {
        SuperToast toast=new SuperToast(this);
        toast.setText(message);
        toast.show();
    }
}
