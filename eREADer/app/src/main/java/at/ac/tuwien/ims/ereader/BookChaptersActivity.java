package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Chapter;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;

public class BookChaptersActivity extends Activity {
    private ImageButton optButton;
    private ImageButton srchButton;
    private ImageButton playbtn;
    private CLAdapter clAdapter;
    private boolean searchbarVisible;
    private EditText searchbar;
    private SidebarMenu sbMenu;

    private Book book;
    private BookService bookService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_chapters);

        if (getActionBar() != null)
            getActionBar().hide();
        bookService=new BookService(this);
        book=bookService.getBook(getIntent().getExtras().getInt("book_id"));

        ListView listview = (ListView)findViewById(R.id.chapterlist);
        clAdapter = new CLAdapter(bookService.getLightweightChaptersOfBook(book.getId()));

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent myIntent = new Intent(BookChaptersActivity.this, BookViewerActivity.class);
                Bundle b = new Bundle();
                b.putInt("book_id", (int) book.getId());
                b.putInt("chapter", position);
                myIntent.putExtras(b);
                startActivity(myIntent);
            }
        });
        listview.setAdapter(clAdapter);

        optButton=(ImageButton)findViewById(R.id.optnbtn_chapter);
        optButton.setOnClickListener(btnListener);
        srchButton=(ImageButton)findViewById(R.id.searchbtn_chapter);
        srchButton.setOnClickListener(btnListener);
        playbtn=(ImageButton)findViewById(R.id.playbtn_chapter);
        playbtn.setOnClickListener(btnListener);
        searchbar=(EditText)findViewById(R.id.searchinput_chapter);
        searchbar.addTextChangedListener(textWatcher);

        TextView booktitle=(TextView)findViewById(R.id.booktitle_chapters);
        booktitle.setText(book.getTitle());
        TextView author_lang=(TextView)findViewById(R.id.author_and_lang_chapters);
        String lang="";
        switch (book.getLanguage()) {
            case EN:
                lang=getString(R.string.eng);
                break;
            case DE:
                lang=getString(R.string.ger);
                break;
            case ES:
                lang=getString(R.string.esp);
                break;
        }
        author_lang.setText(book.getAuthor() + ", " + lang);
        hideSearchBar();
        clAdapter.updateChapterList();

        sbMenu=new SidebarMenu(this, false, false, false);
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
            } else if (v==srchButton) {
                if(!searchbarVisible)
                    showSearchBar();
                else
                    hideSearchBar();
            } else if (v==playbtn) {
                Intent myIntent = new Intent(BookChaptersActivity.this, BookViewerActivity.class);
                Bundle b = new Bundle();
                b.putInt("book_id", (int) book.getId());
                b.putInt("chapter", -1);
                myIntent.putExtras(b);
                startActivity(myIntent);
            }
        }
    };

    private void showSearchBar() {
        searchbar.setVisibility(View.VISIBLE);
        searchbarVisible = true;
    }

    private void hideSearchBar() {
        searchbar.setVisibility(View.GONE);
        searchbarVisible = false;
        clAdapter.updateChapterList();
    }

    private void hideSearchBarAfterSomeTime(int time) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchbarVisible)
                            if (searchbar.getText().length() == 0)
                                hideSearchBar();
                    }
                });
            }
        }, time);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(Editable s) {
            if (s.toString().isEmpty()) {
                hideSearchBarAfterSomeTime(1000);
                return;
            }
            hideSearchBarAfterSomeTime(10000);
            clAdapter.updateChapterList(s.toString());
        }
    };

    private class CLAdapter extends BaseAdapter {
        private List<Chapter> chapterlist;
        private List<Chapter> visiblechapterlist;

        private class ItemHolder {
            TextView heading;
            TextView numberOfWords;
            ImageView marker;
        }

        public CLAdapter(List<Chapter> chapterlist) {
            this.chapterlist=chapterlist;
            this.visiblechapterlist=chapterlist;
        }

        public void updateChapterList() {
            updateChapterList("");
        }

        public void updateChapterList(String s) {
            chapterlist=bookService.getLightweightChaptersOfBook(book.getId());

            if (!s.isEmpty() && searchbarVisible) {
                ArrayList<Chapter> temp = new ArrayList<Chapter>();
                for (Chapter c : chapterlist) {
                    if (c.getHeading().toLowerCase().contains(s.toLowerCase()))
                        temp.add(c);
                    else if (s.matches("\\d+"))
                        if (bookService.getNumberOfWords(c.getId()) >= Integer.parseInt(s))
                            temp.add(c);
                }
                visiblechapterlist = temp;
            } else
                visiblechapterlist=chapterlist;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return visiblechapterlist.size();
        }

        @Override
        public Chapter getItem(int position) {
            return visiblechapterlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder = null;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.chapterlist_item, parent, false);

                holder = new ItemHolder();
                holder.heading = (TextView) convertView.findViewById(R.id.chapter_heading);
                holder.numberOfWords = (TextView) convertView.findViewById(R.id.chapter_numberOfWords);
                holder.marker = (ImageView)convertView.findViewById(R.id.currPosMarker);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            holder.heading.setText(visiblechapterlist.get(position).getHeading());
            holder.numberOfWords.setText(bookService.getNumberOfWords(visiblechapterlist.get(position).getId()) + " " + getString(R.string.words));

            if (bookService.getCurrentPosition(book.getId()).getCurrentChapter()==position)
                holder.marker.setVisibility(View.VISIBLE);
            else
                holder.marker.setVisibility(View.GONE);


            return convertView;
        }
    }

    private void showMessage(String message) {
        Toast.makeText(BookChaptersActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
