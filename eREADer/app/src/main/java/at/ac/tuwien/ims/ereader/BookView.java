package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Language;

/**
 * Created by Flo on 09.07.2014.
 */
public class BookView extends Activity {
    private ImageButton optButton;
    private ImageButton ffButton;
    private ImageButton fbButton;
    private ImageButton playButton;
    private TextView content;
    private TextView title;
    private TextView chap_page;
    private SeekBar volumeBar;

    private Book book;
    private int volume=50;
    private int currentChapter=0;
    private boolean playing=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        getActionBar().hide();

        int selectedBook=getIntent().getExtras().getInt("list");

        //todo remove if persistent
        ArrayList<Book> templist=new ArrayList<Book>();
        Book testb=new Book("The Lord Of The Rings", "J. R. R. Tolkien", Language.English);
        ArrayList<String> testbContent = new ArrayList<String>();
        testbContent.add("1 BLABLABLABLABLABLABLABLABLABLABLABLABLABLABL\nABLABLABLA\nBLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABL\nABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLAB\nLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLA\nBLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA\nBLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABL\nABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLAB\nLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA");
        testbContent.add("2 BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA" +
                "BLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLABLA");
        testbContent.add("3 BLABLABLABLABLA");
        testbContent.add("4 BLABLABLABLABLA");
        testb.setContent(testbContent);
        ArrayList<String> testbChapter = new ArrayList<String>();
        testbChapter.add("Chapter 1");
        testbChapter.add("Chapter 2");
        testbChapter.add("Chapter 3");
        testbChapter.add("Chapter 4");
        testb.setChapters(testbChapter);
        templist.add(testb);
        templist.add(new Book("Bla1", "Bla1", Language.English));
        templist.add(new Book("Bla2", "Bla2", Language.Espanol));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));

        optButton=(ImageButton)findViewById(R.id.optnbtn_book);
        optButton.setOnClickListener(btnListener);
        playButton=(ImageButton)findViewById(R.id.playbtn);
        playButton.setOnClickListener(btnListener);
        ffButton=(ImageButton)findViewById(R.id.ffbtn);
        ffButton.setOnClickListener(btnListener);
        fbButton=(ImageButton)findViewById(R.id.fbbtn);
        fbButton.setOnClickListener(btnListener);
        content=(TextView)findViewById(R.id.book_text);
        title=(TextView)findViewById(R.id.bktitletxt);
        chap_page=(TextView)findViewById(R.id.chap_page_txt);
        volumeBar =(SeekBar)findViewById(R.id.soundbar);
        volumeBar.setOnSeekBarChangeListener(seekBarChangeListener);

        book=templist.get(selectedBook);
        title.setText(book.getTitle());
        chap_page.setText(book.getChapters().get(currentChapter) + ", Page XX");
        content.setText(book.getContent().get(currentChapter));
        fbButton.setAlpha(0.2f);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==optButton) {
                //todo
                showMessage("optn btn clicked book");
            } else if (v==playButton) {
                if (playing) {
                    playing=false;
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.pausebtn));
                } else {
                    playing=true;
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.playbtn));
                }
            } else if (v==ffButton) {
                if (currentChapter < book.getChapters().size()-1) {
                    currentChapter++;
                    updateText();
                }
            } else if(v==fbButton) {
                if (currentChapter > 0) {
                    currentChapter--;
                    updateText();
                }
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            volume=progress;
        }
    };

    private void updateText() {
        content.setText(book.getContent().get(currentChapter));
        chap_page.setText(book.getChapters().get(currentChapter) + ", Page XX");

        if (currentChapter == 0)
            fbButton.setAlpha(0.2f);
        if (currentChapter == book.getChapters().size()-1)
            ffButton.setAlpha(0.2f);
        if (currentChapter>0)
            fbButton.setAlpha(1.f);
        if (currentChapter<book.getChapters().size()-1)
            ffButton.setAlpha(1.f);
    }

    private void showMessage(String message) {
        Toast.makeText(BookView.this, message, Toast.LENGTH_SHORT).show();
    }
}
