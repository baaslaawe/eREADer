package at.ac.tuwien.ims.ereader.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import at.ac.tuwien.ims.ereader.BookViewerActivity;
import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Content;
import at.ac.tuwien.ims.ereader.Entities.CurrentPosition;
import at.ac.tuwien.ims.ereader.R;
import at.ac.tuwien.ims.ereader.Util.StaticHelper;

/**
 * Created by Flo on 17.07.2014.
 */
public class ReadingService extends Service {
    private TextToSpeech ttsService;
    private BookService bookService;

    private Book book;
    private Locale lang;
    private List<Content> contents;
    private int currentChapter;
    private ArrayList<String> sentences;
    private int currentSentence;
    private String currentContent;
    private String currentChapterHeading;
    private String currentBookTitle;

    private Boolean muted=false;
    private Boolean playing=false;
    private Boolean reading=false;

    public ReadingService() {
        super();
    }

    //todo sometimes randomly does not work in background
    //todo sometimes skips sentence when pausing on long sentence
    public void play() {
        if (ttsService != null) {
            playing=true;
            setMuted(false);
            updateNotificationBar();
            broadcastUpdate();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (playing) {
                        synchronized (ttsService) {
                            while (reading);
                            if(!playing || ttsService==null)
                                return;
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                            map.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true");
                            ttsService.speak(sentences.get(currentSentence), TextToSpeech.QUEUE_FLUSH, map);
                            reading=true;
                        }
                    }
                }
            }).start();
        }
    }

    public void pause() {
        if (ttsService != null) {
            if (ttsService.isSpeaking()) {
                ttsService.stop();
                reading=false;
            }
            playing=false;
            broadcastUpdate();
            updateNotificationBar();
        }
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(StaticHelper.BROADCAST_ACTION);
        intent.putExtra("update", true);
        sendBroadcast(intent);
    }

    public void next() {
        if (currentChapter < contents.size()-1) {
            pause();
            currentChapter++;
            currentSentence=0;
            this.currentContent=contents.get(currentChapter).getContent();
            this.currentChapterHeading = contents.get(currentChapter).getHeading();
            updateSentences();
            updateTTS();
            broadcastUpdate();
            updateNotificationBar();
        }
    }

    public void last() {
        if (currentChapter > 0) {
            pause();
            currentChapter--;
            currentSentence=0;
            this.currentContent=contents.get(currentChapter).getContent();
            this.currentChapterHeading = contents.get(currentChapter).getHeading();
            updateSentences();
            updateTTS();
            broadcastUpdate();
            updateNotificationBar();
        }
    }

    public String getCurrentSentence() {
        return sentences.get(currentSentence);
    }

    public void setCurrentSentence(int currentSentence) {
        this.currentSentence=currentSentence;
    }

    public String getCurrChapterHeading() {
        return this.currentChapterHeading;
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public int getNumberOfChaptersInCurrentBook() {
        return contents.size();
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean getMuted() {
        return muted;
    }

    public String getCurrentContent() {
        return this.currentContent;
    }

    public String getNumberOfSentences() {
        return String.valueOf(sentences.size()-1) + " "+ getString(R.string.sentences);
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

    public void setMuted(boolean muted) {
        this.muted=muted;
        AudioManager aManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        aManager.setStreamMute(AudioManager.STREAM_MUSIC, muted);
    }

    public void updateBook(Book b) {
        this.book=b;
        this.currentBookTitle=b.getTitle();
        contents =bookService.getChaptersOfBook(book.getId());

        CurrentPosition c=bookService.getCurrentPosition(book.getId());
        currentChapter=c.getCurrentContent();
        currentSentence=c.getCurrentSentence();

        switch (b.getLanguage()) {
            case DE:
                lang=new Locale("de", "DE");
                break;
            case ES:
                lang=new Locale("es", "ES");
                break;
            default:
                lang=new Locale("en", "US");
        }
        this.currentContent=contents.get(currentChapter).getContent();
        this.currentChapterHeading = contents.get(currentChapter).getHeading();

        updateSentences();
        updateTTS();
        broadcastUpdate();
    }

    private void updateTTS() {
        if(ttsService!=null) {
            if (lang != null)
                if (ttsService.isLanguageAvailable(lang) == TextToSpeech.LANG_COUNTRY_AVAILABLE)
                    ttsService.setLanguage(lang);

            SharedPreferences settings = getSharedPreferences("settings", 0);
            ttsService.setSpeechRate(settings.getFloat("tts_rate", StaticHelper.normal_Speechrate));
        }
    }

    private void updateSentences() {
        sentences=new ArrayList<String>();
        BreakIterator it=null;
        if (lang!=null)
            it = BreakIterator.getSentenceInstance(lang);
        else
            it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(currentContent);

        int lastIndex = it.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex != BreakIterator.DONE) {
                sentences.add(currentContent.substring(firstIndex, lastIndex));
            }
        }
    }

    public int[] getIndicesOfClickedSentence(Layout layout, int x, int y) {
        int[] f=new int[2];
        f[0]=0;
        f[1]=0;

        int line = layout.getLineForVertical(y);
        int clickedChar = layout.getOffsetForHorizontal(line, x);

        BreakIterator it=null;
        if (lang!=null)
            it = BreakIterator.getSentenceInstance(lang);
        else
            it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(currentContent);

        int lastIndex = it.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex != BreakIterator.DONE && clickedChar >= firstIndex && clickedChar <= lastIndex) {
                f[0]=firstIndex;
                f[1]=lastIndex;
                break;
            }
        }
        return f;
    }

    public int getSentenceNumberByClick(Layout layout, int x, int y) {
        int line = layout.getLineForVertical(y);
        int clickedChar = layout.getOffsetForHorizontal(line, x);

        BreakIterator it=null;
        if (lang!=null)
            it = BreakIterator.getSentenceInstance(lang);
        else
            it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(currentContent);

        int i=0;
        int lastIndex = it.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex != BreakIterator.DONE && clickedChar >= firstIndex && clickedChar <= lastIndex)
                break;
            i++;
        }
        return i;
    }

    private void updateNotificationBar() {
        if (ttsService!=null && book!=null) {
            RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_bar);
            notificationView.setTextViewText(R.id.bar_title_book, currentBookTitle);
            notificationView.setTextViewText(R.id.bar_chapter_page, currentChapterHeading);
            notificationView.setTextViewText(R.id.bar_word, getNumberOfSentences());
            notificationView.setOnClickPendingIntent(R.id.bar_close,
                    PendingIntent.getService(getApplicationContext(),
                            0, new Intent(StaticHelper.ACTION_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT));
            if (playing) {
                notificationView.setImageViewResource(R.id.bar_btnPlay, android.R.drawable.ic_media_pause);
                notificationView.setOnClickPendingIntent(R.id.bar_btnPlay,
                        PendingIntent.getService(
                                getApplicationContext(),
                                0,
                                new Intent(StaticHelper.ACTION_PAUSE),
                                PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                notificationView.setImageViewResource(R.id.bar_btnPlay, android.R.drawable.ic_media_play);
                notificationView.setOnClickPendingIntent(R.id.bar_btnPlay,
                        PendingIntent.getService(
                                getApplicationContext(),
                                0,
                                new Intent(StaticHelper.ACTION_PLAY),
                                PendingIntent.FLAG_UPDATE_CURRENT));
            }

            Intent resultIntent = new Intent(this, BookViewerActivity.class);
            Bundle b = new Bundle();
            b.putInt("book_id", (int) book.getId());
            b.putInt("chapter", getCurrentChapter());
            resultIntent.putExtras(b);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(BookViewerActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logo_small_bar)
                    .setTicker(getString(R.string.notification_bar_welcome) + ": " + currentBookTitle)
                    .setContent(notificationView)
                    .setOngoing(true);
            builder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(StaticHelper.NOTIFICATION_ID, builder.build());
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if(action.equalsIgnoreCase(StaticHelper.ACTION_PAUSE)) {
                    Log.d(ReadingService.class.getName(), "Pressed pause from Notificationbar");
                    pause();
                } else if (action.equalsIgnoreCase(StaticHelper.ACTION_PLAY)) {
                    Log.d(ReadingService.class.getName(), "Pressed play from Notificationbar");
                    play();
                } else if (action.equalsIgnoreCase(StaticHelper.ACTION_CLOSE)) {
                    Log.d(ReadingService.class.getName(), "Pressed close from Notificationbar");
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(StaticHelper.NOTIFICATION_ID);
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    stopSelf();
                }
            }
            broadcastUpdate();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate (){
        super.onCreate();
        bookService=new BookService(this);

        Intent intent = new Intent(StaticHelper.BROADCAST_ACTION);
        intent.putExtra("ttsStart", true);
        sendBroadcast(intent);

        ttsService=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    ttsService.setLanguage(Locale.US);
                    Log.d(ReadingService.class.getName(), "TTS initialized.");
                    Intent intent = new Intent(StaticHelper.BROADCAST_ACTION);
                    intent.putExtra("ttsDone", true);
                    sendBroadcast(intent);
                }
            }
        });

        ttsService.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                Log.d(ReadingService.class.getName(), "Stopped reading: " + sentences.get(currentSentence));
                if (currentSentence < sentences.size()-1) {
                    if(playing)
                        currentSentence++;
                    bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentChapter, currentSentence));
                    reading = false;
                } else if (currentSentence==sentences.size()-1
                        && currentChapter== contents.size()-1) {
                    Log.d(ReadingService.class.getName(), "Reached end of book.");
                    pause();
                    bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentChapter, currentSentence));
                    reading = false;
                    updateNotificationBar();
                } else {
                    Log.d(ReadingService.class.getName(), "Reached end of chapter, skipping to the next.");
                    next();
                    bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentChapter, currentSentence));
                    reading = false;
                    play();
                }
                broadcastUpdate();
            }

            @Override
            public void onError(String utteranceId) {}

            @Override
            public void onStart(String utteranceId) {
                Log.d(ReadingService.class.getName(), "Starting to read: " + sentences.get(currentSentence));
                broadcastUpdate();
            }
        });
    }

    private final IBinder binder = new ReadingServiceBinder();

    public class ReadingServiceBinder extends Binder {
        public ReadingService getService() {
            return ReadingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        if (ttsService != null) {
            pause();
            ttsService.shutdown();
        }
    }
}