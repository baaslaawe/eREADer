package at.ac.tuwien.ims.ereader.Services;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

import at.ac.tuwien.ims.ereader.Entities.Book;

/**
 * Created by Flo on 17.07.2014.
 */
public class ReadingService {
    private TextToSpeech ttsService;
    private Book book;
    private ArrayList<String> sentences;
    private Locale lang;
    private int currentSentence;
    private boolean ttsdone=false;

    public ReadingService(Context c, Book b, int currentSentence) {
        this.book=b;
        this.currentSentence=currentSentence;

        switch (book.getLanguage()) {
            case DE:
                lang=Locale.GERMAN; //todo why u no work
                break;
            case ES:
                lang=new Locale("es", "ES");
                break;
            default:
                lang=Locale.US;
        }

        ttsService= new TextToSpeech(c.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    ttsdone=true;
                }
            }
        });
    }

    public void updatePage(String s, int curr) {
        sentences=new ArrayList<String>();
        BreakIterator it = BreakIterator.getSentenceInstance(lang);
        it.setText(s);

        int lastIndex = it.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = it.next();

            if (lastIndex != BreakIterator.DONE) {
                sentences.add(s.substring(firstIndex, lastIndex));
            }
        }
        currentSentence=curr;
    }

    public void readPage() {
        readText(sentences.get(currentSentence));
    }

    public int getCurrentSentence() {
        return currentSentence;
    }

    public void readText(String currentSentence) {
        if (ttsService != null)
            if (ttsdone) {
                if (ttsService.isLanguageAvailable(lang)==TextToSpeech.LANG_COUNTRY_AVAILABLE)
                    ttsService.setLanguage(lang);
                ttsService.speak(currentSentence, TextToSpeech.QUEUE_FLUSH, null);
            }
    }

    public void stopReadingCurrentText() {
        if (ttsService != null)
            if(ttsService.isSpeaking())
                ttsService.stop();
    }

    public void close() {
        if (ttsService != null) {
            stopReadingCurrentText();
            ttsService.shutdown();
        }
    }
}