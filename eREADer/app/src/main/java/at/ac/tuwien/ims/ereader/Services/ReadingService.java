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
 * A Service that handles reading books and connects with BookViewActivity to update its content.
 *
 * @author Florian Schuster
 */
public class ReadingService extends Service {
    private TextToSpeech ttsService;
    private BookService bookService;

    private Book book;
    private Locale lang;
    private List<Content> contents;
    private int currentContent;
    private ArrayList<String> sentences;
    private int currentSentence;
    private String currentContentString;
    private String currentContentHeading;
    private String currentBookTitle;

    private Boolean muted=false;
    private Boolean playing=false;
    private Boolean reading=false;

    /**
     * Constructor for this service.
     */
    public ReadingService() {
        super();
    }

    /**
     * Method is called when Service is created. Initiates the TTS-engine and applies the
     * UtteranceProgressListener that handles actions before and after a sentence is read.
     *
     */
    @Override
    public void onCreate(){
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
                    bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentContent, currentSentence));
                    reading = false;
                } else if (currentSentence==sentences.size()-1 && currentContent == contents.size()-1) {
                    Log.d(ReadingService.class.getName(), "Reached end of book.");
                    pause();
                    bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentContent, currentSentence));
                    reading = false;
                    updateNotificationBar();
                } else {
                    Log.d(ReadingService.class.getName(), "Reached end of chapter, skipping to the next.");
                    next();
                    bookService.updateCurrentPosition(new CurrentPosition(book.getId(), currentContent, currentSentence));
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

    /**
     * Method that handles reaceived intents, as pausing and playing from the notification bar and
     * also closing the notification bar.
     *
     */
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

    /**
     * OnDestroy method that cleanly shuts down the TTS-service and closes the notification in the
     * notification bar.
     *
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ttsService != null) {
            pause();
            ttsService.shutdown();
        }
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(StaticHelper.NOTIFICATION_ID);
        Log.d(ReadingService.class.getName(), "Readingservice destroyed.");
    }

    /**
     * Method that updates the notification bar and the TTS-service, sends a broadcast and then
     * plays the current sentence.
     *
     */
    public void play() {
        if (ttsService != null) {
            playing=true;
            setMuted(false);
            updateNotificationBar();
            broadcastUpdate();
            updateTTS();

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

    /**
     *Method that stops the TTS-service and therefore pauses the read sentence.
     *
     */
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

    /**
     *Helper method that sends a broadcast to the BookViewerActivity to update its contents.
     *
     */
    private void broadcastUpdate() {
        Intent intent = new Intent(StaticHelper.BROADCAST_ACTION);
        intent.putExtra("update", true);
        sendBroadcast(intent);
    }

    /**
     * Pauses and skips to the next content of the book.
     *
     */
    public void next() {
        if (currentContent<contents.size()-1) {
            pause();
            currentContent++;
            currentSentence=0;
            this.currentContentString=contents.get(currentContent).getContent();
            this.currentContentHeading=contents.get(currentContent).getHeading();
            updateSentences();
            broadcastUpdate();
            updateNotificationBar();
        }
    }

    /**
     * Pauses and skips to the last content of the book.
     *
     */
    public void last() {
        if (currentContent>0) {
            pause();
            currentContent--;
            currentSentence=0;
            this.currentContentString=contents.get(currentContent).getContent();
            this.currentContentHeading=contents.get(currentContent).getHeading();
            updateSentences();
            broadcastUpdate();
            updateNotificationBar();
        }
    }

    /**
     * Returns the current sentence.
     *
     * @return a String with the current sentence
     */
    public String getCurrentSentence() {
        return sentences.get(currentSentence);
    }

    /**
     * Sets the current number of a sentence in the sentence list.
     *
     * @param currentSentence the number of the new current sentence
     */
    public void setCurrentSentence(int currentSentence) {
        this.currentSentence=currentSentence;
    }

    /**
     * Returns the current content's heading.
     *
     * @return a String with the current content heading
     */
    public String getCurrContentHeading() {
        return this.currentContentHeading;
    }

    /**
     * Returns the number of the current content chunk.
     *
     * @return the number of the current content chunk
     */
    public int getCurrentContent() {
        return currentContent;
    }

    /**
     * Returns the size of the content chunks list.
     *
     * @return the number of all content chunks
     */
    public int getNumberOfContentsInCurrentBook() {
        return contents.size();
    }

    /**
     * Returns true if TTS-service is reading or false if it is not.
     *
     * @return boolean if service is playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Returns true if Service is muted or false if it is not.
     *
     * @return boolean if service is muted
     */
    public boolean getMuted() {
        return muted;
    }

    /**
     * Returns a String with the complete current content chunk.
     *
     * @return a String with the current content
     */
    public String getCurrentContentString() {
        return this.currentContentString;
    }

    /**
     * Returns a String with the size of the current sentence list for the notification bar.
     *
     * @return a String like "123 Sentences"
     */
    private String getNumberOfSentences() {
        return String.valueOf(sentences.size()-1) + " "+ getString(R.string.sentences);
    }

    /**
     * Sets the service to either muted or not.
     *
     * @param muted boolean if the service should be muted or not
     */
    public void setMuted(boolean muted) {
        this.muted=muted;
        AudioManager aManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        aManager.setStreamMute(AudioManager.STREAM_MUSIC, muted);
    }

    /**
     * Returns an array with 2 numbers that indicate the starting and ending position of the
     * currently read sentence in the content.
     *
     * @return int array with indices
     */
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

    /**
     * Updates the currently read book: Updates the content, the sentences and gets the current
     * position in the book.
     *
     * @param b book to be read
     */
    public void updateBook(Book b) {
        this.book=b;
        this.currentBookTitle=b.getTitle();
        contents=bookService.getContentsOfBook(book.getId());

        CurrentPosition c=bookService.getCurrentPosition(book.getId());
        currentContent =c.getCurrentContent();
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
        this.currentContentString =contents.get(currentContent).getContent();
        this.currentContentHeading = contents.get(currentContent).getHeading();

        updateSentences();
        updateTTS();
        broadcastUpdate();
    }

    /**
     * Updates TTS settings: spoken language and speech rate.
     *
     */
    private void updateTTS() {
        if(ttsService!=null) {
            if (lang != null)
                if (ttsService.isLanguageAvailable(lang) == TextToSpeech.LANG_COUNTRY_AVAILABLE)
                    ttsService.setLanguage(lang);

            SharedPreferences settings = getSharedPreferences("settings", 0);
            ttsService.setSpeechRate(settings.getFloat("tts_rate", StaticHelper.normal_Speechrate));
        }
    }

    /**
     * Updates the sentence ArrayList that is used to read the content sentence by sentence.
     * Uses a Breakiterator with the Locale of the book.
     *
     */
    private void updateSentences() {
        sentences=new ArrayList<String>();
        BreakIterator it=null;
        if (lang!=null)
            it = BreakIterator.getSentenceInstance(lang);
        else
            it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(currentContentString);

        int lastIndex = it.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex != BreakIterator.DONE) {
                sentences.add(currentContentString.substring(firstIndex, lastIndex));
            }
        }
    }

    /**
     * Returns the starting and ending indices in an array of the currently touched sentence.
     *
     * @param layout the layout of the TextView
     * @param x coordinate where user touched the screen
     * @param y coordinate where user touched the screen
     * @return an int array containing indices
     */
    public int[] getIndicesOfClickedSentence(Layout layout, int x, int y) {
        int[] f=new int[2];
        f[0]=0;
        f[1]=0;

        int line=layout.getLineForVertical(y);
        int clickedChar=layout.getOffsetForHorizontal(line, x);

        BreakIterator it=null;
        if (lang!=null)
            it=BreakIterator.getSentenceInstance(lang);
        else
            it=BreakIterator.getSentenceInstance(Locale.US);
        it.setText(currentContentString);

        int lastIndex = it.first();
        while (lastIndex!=BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex!=BreakIterator.DONE && clickedChar >= firstIndex && clickedChar <= lastIndex) {
                f[0]=firstIndex;
                f[1]=lastIndex;
                break;
            }
        }
        return f;
    }

    /**
     * Returns the snumber of the currently touched sentence.
     *
     * @param layout the layout of the TextView
     * @param x coordinate where user touched the screen
     * @param y coordinate where user touched the screen
     * @return int with the picked sentence number
     */
    public int getSentenceNumberByClick(Layout layout, int x, int y) {
        int line = layout.getLineForVertical(y);
        int clickedChar = layout.getOffsetForHorizontal(line, x);

        BreakIterator it=null;
        if (lang!=null)
            it = BreakIterator.getSentenceInstance(lang);
        else
            it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(currentContentString);

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

    /**
     * Updates the content of the notification bar of the service.     *
     * Sets Buttons to either play or pause depending on the current reading state.
     *
     */
    private void updateNotificationBar() {
        if (ttsService!=null && book!=null) {
            RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_bar);
            notificationView.setTextViewText(R.id.bar_title_book, currentBookTitle);
            notificationView.setTextViewText(R.id.bar_chapter_page, currentContentHeading);
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
            b.putInt("chapter", getCurrentContent());
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
    //ServiceBinder

    private final IBinder binder = new ReadingServiceBinder();

    public class ReadingServiceBinder extends Binder {
        public ReadingService getService() {
            return ReadingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(ReadingService.class.getName(), "Readingservice onBind.");
        return binder;
    }
}