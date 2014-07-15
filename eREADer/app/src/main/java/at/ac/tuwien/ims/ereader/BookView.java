package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
    private int currentPage=0; //todo change
    private boolean playing=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        if (getActionBar() != null)
            getActionBar().hide();

        int selectedBook=getIntent().getExtras().getInt("list");

        //todo remove if persistent
        ArrayList<Book> templist=new ArrayList<Book>();
        Book testb=new Book("The Lord Of The Rings", "J. R. R. Tolkien", Language.EN);
        templist.add(testb);
        templist.add(new Book("Bla1", "Bla1", Language.EN));
        templist.add(new Book("Bla2", "Bla2", Language.ES));
        templist.add(new Book("Bla3", "Bla3", Language.DE));
        templist.add(new Book("Bla3", "Bla3", Language.DE));
        templist.add(new Book("Bla3", "Bla3", Language.DE));
        templist.add(new Book("Bla3", "Bla3", Language.DE));
        templist.add(new Book("Bla3", "Bla3", Language.DE));
        templist.add(new Book("Bla3", "Bla3", Language.DE));

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
        updateText();
        fbButton.setAlpha(0.2f);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==optButton) {
                Intent myIntent = new Intent(BookView.this, Settings.class);
                startActivity(myIntent);
            } else if (v==playButton) {
                //todo
                if (playing) {
                    playing=false;
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.pausebtn));
                } else {
                    playing=true;
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.playbtn));
                }
            } else if (v==ffButton) {
                //todo
                if (currentChapter < book.getChapters().size()-1) {
                    currentChapter++;
                    updateText();
                }
            } else if(v==fbButton) {
                //todo
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
        content.setText(book.getContent(currentChapter, currentPage));
        chap_page.setText(book.getChapterHeading(currentChapter) + ", "+ getString(R.string.page)+" "+ currentPage);

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
