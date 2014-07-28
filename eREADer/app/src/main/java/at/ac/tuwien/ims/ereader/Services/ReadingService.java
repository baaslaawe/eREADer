package at.ac.tuwien.ims.ereader.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import at.ac.tuwien.ims.ereader.BookView;
import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Chapter;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.Entities.Page;
import at.ac.tuwien.ims.ereader.R;

/**
 * Created by Flo on 17.07.2014.
 */
public class ReadingService extends Service {
    private TextToSpeech ttsService;
    public static final String BROADCAST_ACTION = "at.ac.tuwien.ims.ereader.Services";
    public static final String ACTION_PLAY="at.ac.tuwien.ims.ereader.Services.ACTION_PLAY";
    public static final String ACTION_PAUSE="at.ac.tuwien.ims.ereader.Services.ACTION_PAUSE";

    private BookService bookService;
    private RemoteViews notificationView;

    private Book book;
    private Locale lang;
    private List<Chapter> chapters;
    private int currentChapter;
    private HashMap<Integer, List<Page>> pages;
    private int currentPage;
    private ArrayList<String> sentences;
    private int currentSentence;

    private Boolean muted=false;
    private Boolean playing=false;
    private Boolean reading=false;

    public ReadingService() {
        super();
    }

    public void startReading() {
        if (ttsService != null) {
            broadcast(false, true);
            playing = true;

            if (ttsService.isLanguageAvailable(lang)==TextToSpeech.LANG_COUNTRY_AVAILABLE)
                ttsService.setLanguage(lang);

            updateNotificationBar();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (playing) {
                        synchronized (ttsService) {
                            bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentChapter, currentPage, currentSentence));

                            while (reading);
                            if(!playing)
                                return;

                            if (currentSentence < sentences.size()-1) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                                map.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true");

                                broadcast(false, true);
                                ttsService.speak(sentences.get(currentSentence), TextToSpeech.QUEUE_FLUSH, map);
                                reading=true;
                            } else if (sentences.size()==1) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                                map.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true");

                                broadcast(false, true);
                                ttsService.speak(sentences.get(0), TextToSpeech.QUEUE_FLUSH, map);
                                reading=true;
                                playing=false;
                            } else
                                playing = false;
                        }
                    }
                }

            }).start();
        }
    }

    public void stopReading() {
        if (ttsService != null) {
            if (ttsService.isSpeaking())
                ttsService.stop();
            playing=false;
            broadcast(false, true);
        }
        updateNotificationBar();
    }

    private void broadcast(boolean updateChapter, boolean updateContent) {
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("updateChapter", updateChapter);
        intent.putExtra("updateContent", updateContent);
        sendBroadcast(intent);
    }

    public void next(){
        if (currentChapter < chapters.size()-1) {
            Intent intent = new Intent(BROADCAST_ACTION);
            if (currentPage < pages.get(currentChapter).size() - 1) {
                currentPage++;
                intent.putExtra("updateChapter", false);
            } else {
                currentChapter++;
                currentPage=0;
                intent.putExtra("updateChapter", true);
            }
            currentSentence=0;
            bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentChapter, currentPage, currentSentence));
            intent.putExtra("updateContent", true);
            updateSentences();
            sendBroadcast(intent);
        }
    }

    public void last(){
        if (currentChapter >= 0) {
            Intent intent = new Intent(BROADCAST_ACTION);
            if (currentPage > 0) {
                currentPage--;
                intent.putExtra("updateChapter", false);
            } else if (currentChapter > 0) {
                currentChapter--;
                currentPage=pages.get(currentChapter).size()-1;
                intent.putExtra("updateChapter", true);
            }
            currentSentence=0;
            bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentChapter, currentPage, currentSentence));
            intent.putExtra("updateContent", true);
            updateSentences();
            sendBroadcast(intent);
        }
    }

    public String getCurrentSentence() {
        return sentences.get(currentSentence);
    }

    public String getCurrChapterHeading() {
        return chapters.get(currentChapter).getHeading();
    }

    public String getCurrentBookTitle() {
        return book.getTitle();
    }

    public int getCurrentPageNumber() {
        return pages.get(currentChapter).get(currentPage).getPage_nr();
    }

    public int getNumberOfChaptersInCurrentBook() {
        return chapters.size();
    }

    public int getNumberOfPagesInCurrentChapter() {
        return pages.get(currentChapter).size();
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int[] getIndicesOfCurrentSentence() {
        int x[]=new int[2];

        if(sentences.size()==1) {
            x[0]=0;
            x[1]=sentences.get(0).length();
            return x;
        } else {
            int i=0;
            for (int j = 0; j < sentences.size(); j++) {
                if (j == currentSentence)
                    break;
                i += sentences.get(j).length();
            }
            x[0] = i;
            x[1] = i + getCurrentSentence().length();
            return x;
        }
    }

    public boolean getPlaying() {
        return playing;
    }

    public void setMuted(boolean muted) {
        this.muted=muted;
        AudioManager aManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        aManager.setStreamMute(AudioManager.STREAM_MUSIC, muted);
    }

    public boolean getMuted() {
        return muted;
    }

    public String getCurrentContent() {
        return pages.get(currentChapter).get(currentPage).getContent();
    }

    public void updateBook(Book b) {
        this.book=b;
        chapters=bookService.getChaptersOfBook(book.getId());
        pages=bookService.getPagesOfChapters(chapters);

        CurrentPosition c=bookService.getCurrentPosition(book.getId());
        currentChapter=c.getCurrentChapter();
        currentPage=c.getCurrentPage();
        currentSentence=c.getCurrentSentence();

        switch (b.getLanguage()) {
            case DE:
                lang=Locale.GERMAN; //todo why u no work
                break;
            case ES:
                lang=new Locale("es", "ES");
                break;
            default:
                lang=Locale.US;
        }
        updateSentences();

        if (ttsService.isLanguageAvailable(lang)==TextToSpeech.LANG_COUNTRY_AVAILABLE)
            ttsService.setLanguage(lang);

        broadcast(true, true);
    }

    private void updateSentences() {
        String content=getCurrentContent();
        sentences=new ArrayList<String>();
        BreakIterator it=null;
        if (lang!=null)
            it = BreakIterator.getSentenceInstance(lang);
        else
            it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(content);

        int lastIndex = it.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex != BreakIterator.DONE) {
                sentences.add(content.substring(firstIndex, lastIndex));
            }
        }
    }

    private void updateNotificationBar() {
        notificationView = new RemoteViews(getPackageName(), R.layout.navigation_bar);

        PendingIntent pendingIntentAction=null;
        if(playing) {
            Intent intentAction = new Intent(ACTION_PAUSE);
            pendingIntentAction = PendingIntent.getService(getApplicationContext(),
                    0, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationView.setImageViewResource(R.id.bar_btnPlay, R.drawable.pausebtn_bar);
        } else {
            Intent intentAction = new Intent(ACTION_PLAY);
            pendingIntentAction = PendingIntent.getService(getApplicationContext(),
                    0, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationView.setImageViewResource(R.id.bar_btnPlay, R.drawable.playbtn_bar);
        }

        notificationView.setOnClickPendingIntent(R.id.bar_btnPlay, pendingIntentAction);
        notificationView.setTextViewText(R.id.bar_title_book, getCurrentBookTitle());
        notificationView.setTextViewText(R.id.bar_chapter_page, getCurrChapterHeading()+", "+getString(R.string.page)+ " "+getCurrentPageNumber());

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo_small_bar)
                        .setTicker(getString(R.string.notification_bar_welcome)+ ": "+getCurrentBookTitle())
                        .setContent(notificationView);

        Intent resultIntent = new Intent(this, BookView.class);
        Bundle b = new Bundle();
        b.putInt("book_id", (int) book.getId());
        b.putInt("chapter", currentChapter);
        resultIntent.putExtras(b);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BookView.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


    //--------------------------------------------------------------------------
    private final IBinder binder = new ReadingServiceBinder();

    public class ReadingServiceBinder extends Binder {
        public ReadingService getService() {
            return ReadingService.this;
        }
    }

    @Override
    public void onCreate (){
        super.onCreate();
        bookService=new BookService(this);

        ttsService=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    ttsService.setLanguage(Locale.US);
                    Log.d(ReadingService.class.getName(), "TTS initialized.");
                }
            }
        });

        ttsService.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                if (reading) {
                    currentSentence++;
                    reading = false;
                    if (currentSentence==sentences.size()-1) {
                        broadcast(true, true);
                    }
                    Log.d(ReadingService.class.getName(), "Stopped reading: " + sentences.get(currentSentence));
                }
            }

            @Override
            public void onError(String utteranceId) {
            }

            @Override
            public void onStart(String utteranceId) {
                Log.d(ReadingService.class.getName(), "Starting to read: " + sentences.get(currentSentence));
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(ACTION_PLAY)) {
                    Log.d(ReadingService.class.getName(), "Pressed play from Notificationbar");
                    startReading();
                } else if(action.equals(ACTION_PAUSE)) {
                    Log.d(ReadingService.class.getName(), "Pressed pause from Notificationbar");
                    stopReading();
                }
            }
            broadcast(true, true);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        if (ttsService != null) {
            stopReading();
            ttsService.shutdown();
        }
    }
}