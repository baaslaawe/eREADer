package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Chapter;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Services.ReadingService;

/**
 * Created by Flo on 09.07.2014.
 */
public class BookViewerActivity extends Activity {
    private ImageButton optButton;
    private ImageButton ffButton;
    private ImageButton fbButton;
    private ImageButton playButton;
    private ImageButton libbtn;
    private ImageButton volumeButton;

    private TextView content;
    private TextView chap_txt;

    private Book book;
    private ReadingService readingService;
    private boolean serviceBound;

    //todo let user be able to pick a sentence to read
    //todo scroll textview automatically on longer contents

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
        libbtn=(ImageButton)findViewById(R.id.libbtn);
        libbtn.setOnClickListener(btnListener);
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
        //todo try with webview
        content.setText(cont);
        chap_txt.setText(chapt);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==optButton) {
                Intent myIntent = new Intent(BookViewerActivity.this, SettingsActivity.class);
                startActivity(myIntent);
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
            } else if (v==libbtn) {
                Intent myIntent = new Intent(BookViewerActivity.this, MyLibraryActivity.class);
                startActivity(myIntent);
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

    public void updateContent() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Spannable spanText = Spannable.Factory.getInstance().newSpannable(readingService.getCurrentContent());
                int i[] = readingService.getIndicesOfCurrentSentence();
                spanText.setSpan(new BackgroundColorSpan(Color.parseColor("#0FC1B8")), i[0], i[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                content.setText(spanText);
            }
        });
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
                    updateContent();
                    updateChapter();
                    updateButtons();
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
}
