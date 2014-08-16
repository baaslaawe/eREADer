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
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Chapter;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Services.ReadingService;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;
import at.ac.tuwien.ims.ereader.Util.StaticHelper;

/**
 * Created by Flo on 09.07.2014.
 */
public class BookViewerActivity extends Activity {
    private ImageButton optButton;
    private ImageButton ffButton;
    private ImageButton fbButton;
    private ImageButton playButton;
    private ImageButton volumeButton;

    private TextView content;
    private TextView chap_txt;

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
        optButton.setOnClickListener(btnListener);
        playButton=(ImageButton)findViewById(R.id.playbtn);
        playButton.setOnClickListener(btnListener);
        ffButton=(ImageButton)findViewById(R.id.ffbtn);
        ffButton.setOnClickListener(btnListener);
        fbButton=(ImageButton)findViewById(R.id.fbbtn);
        fbButton.setOnClickListener(btnListener);
        volumeButton =(ImageButton)findViewById(R.id.volume_btn);
        volumeButton.setOnClickListener(btnListener);

        TextView title=(TextView)findViewById(R.id.bktitletxt);
        title.setText(book.getTitle());
        chap_txt=(TextView)findViewById(R.id.chap_txt);
        content=(TextView)findViewById(R.id.book_text);

        int cha=getIntent().getExtras().getInt("chapter");
        String chapt;
        String cont;
        List<Chapter> chapters=bookService.getChaptersOfBook(book.getId());

        if (cha == -1) {
            CurrentPosition c=bookService.getCurrentPosition(book.getId());
            chapt=chapters.get(c.getCurrentChapter()).getHeading();
            cont=chapters.get(c.getCurrentChapter()).getContent();
        } else {
            bookService.updateCurrentPosition(new CurrentPosition(book.getId(), cha, 0));
            chapt=chapters.get(cha).getHeading();
            cont=chapters.get(cha).getContent();
        }

        content.setText(cont);
        chap_txt.setText(chapt);
        sbMenu=new SidebarMenu(this, false, false, false);

        int standardTextSize=(int)content.getTextSize();
        if (standardTextSize>StaticHelper.typesize_range) {
            size_small = standardTextSize - StaticHelper.typesize_range;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range;
        } else {
            size_small = standardTextSize;
            size_medium = standardTextSize;
            size_large = standardTextSize + StaticHelper.typesize_range;
        }
        face0 = content.getTypeface();
        face1 = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
        face2 = Typeface.createFromAsset(getAssets(), "fonts/LinLibertine_R.ttf");
        updateTextSettings();

        content.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    clicktime=event.getEventTime();
                    return true;
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(clicktime!=0 && (event.getEventTime()-clicktime)>=800) {
                        final Layout layout = ((TextView) v).getLayout();
                        if (layout!=null) {
                            final int x=(int) event.getX();
                            final int y=(int) event.getY();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(readingService.getCurrentContent());
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
                return false;
            }
        });
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

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==optButton) {
                if(sbMenu.getMenuDrawer().isMenuVisible())
                    sbMenu.getMenuDrawer().closeMenu();
                else
                    sbMenu.getMenuDrawer().openMenu();
            } else if (v==playButton) {
                if (readingService.getPlaying()) {
                    readingService.stopReading();
                } else {
                    readingService.startReading();
                }
            } else if (v==ffButton) {
                readingService.next();
            } else if(v==fbButton) {
                readingService.last();
            } else if (v== volumeButton) {
                if (readingService.getMuted()) {
                    readingService.setMuted(false);
                    volumeButton.setImageDrawable(getResources().getDrawable(R.drawable.notmuted));
                } else {
                    readingService.setMuted(true);
                    volumeButton.setImageDrawable(getResources().getDrawable(R.drawable.muted));
                }
            }
            updateButtons();
        }
    };

    //todo why does the text disappear after scroll, make scrolling only possible when not reading
    private final void updateFocus() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Layout layout=content.getLayout();
                if (layout!=null) {
                    int line = layout.getLineForOffset((readingService.getIndicesOfCurrentSentence()[0] - (layout.getLineCount() / 2)));
                    content.scrollTo(0, layout.getLineTop(line));
                }
            }
        });
    }

    private void updateContent() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Spannable spanText = Spannable.Factory.getInstance().newSpannable(readingService.getCurrentContent());
                int i[] = readingService.getIndicesOfCurrentSentence();
                if (i!=null){
                    spanText.setSpan(new BackgroundColorSpan(Color.parseColor("#0FC1B8")), i[0], i[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    content.setText(spanText);
                }
            }
        });
    }

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

    private void updateChapter() {
        chap_txt.setText(readingService.getCurrChapterHeading());
    }

    private void updateButtons() {
        if (readingService.getCurrentChapter()==0) {
            fbButton.setAlpha(0.2f);
            fbButton.setEnabled(false);
        }
        if (readingService.getCurrentChapter()==readingService.getNumberOfChaptersInCurrentBook()-1) {
            ffButton.setAlpha(0.2f);
            ffButton.setEnabled(false);
        }
        if (readingService.getCurrentChapter()>0) {
            fbButton.setAlpha(1.f);
            fbButton.setEnabled(true);
        }
        if (readingService.getCurrentChapter()<readingService.getNumberOfChaptersInCurrentBook()-1) {
            ffButton.setAlpha(1.f);
            ffButton.setEnabled(true);
        }

        if(readingService.getPlaying())
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.pausebtn));
        else
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.playbtn));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, ReadingService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        if (serviceBound)
            readingService.updateBook(book);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            Log.d(BookViewerActivity.class.getName(), "Service is disconnected");
            serviceBound = false;
            readingService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(BookViewerActivity.class.getName(), "Service is connected");
            serviceBound = true;
            ReadingService.ReadingServiceBinder mLocalBinder = (ReadingService.ReadingServiceBinder)service;
            readingService = mLocalBinder.getService();

            readingService.updateBook(book);
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {String action = intent.getAction();
            if(action.equalsIgnoreCase(ReadingService.BROADCAST_ACTION)) {
                Bundle extra = intent.getExtras();
                if (extra.getBoolean("update")) {
                    updateTextSettings();
                    updateContent();
                    updateChapter();
                    updateButtons();
                    updateFocus();
                }

                if(extra.getBoolean("ttsStart")) {
                    ttsDoneToast = new SuperActivityToast(BookViewerActivity.this, SuperToast.Type.PROGRESS);
                    ttsDoneToast.setText(getString(R.string.ttsDone_str));
                    ttsDoneToast.setIndeterminate(true);
                    ttsDoneToast.setProgressIndeterminate(true);
                    ttsDoneToast.show();
                } else if (extra.getBoolean("ttsDone")) {
                    ttsDoneToast.dismiss();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(ReadingService.BROADCAST_ACTION));

        if (serviceBound)
            readingService.updateBook(book);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(serviceBound) {
            unbindService(mConnection);
            serviceBound = false;
        }

    }

    private void showMessage(String message) {
        Toast.makeText(BookViewerActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
