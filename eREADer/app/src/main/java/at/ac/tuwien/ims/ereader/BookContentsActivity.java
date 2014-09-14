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
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Content;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;

/**
 * Activity to display content chunks of one specific eBook.
 *
 * @author Florian Schuster
 */
public class BookContentsActivity extends Activity {
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
        SuperActivityToast.onRestoreState(savedInstanceState, BookContentsActivity.this);
        setContentView(R.layout.activity_book_contents);

        if (getActionBar() != null)
            getActionBar().hide();
        bookService=new BookService(this);
        book=bookService.getBook(getIntent().getExtras().getInt("book_id"));

        ListView listview = (ListView)findViewById(R.id.chapterlist);
        clAdapter = new CLAdapter(bookService.getLightweightContentsOfBook(book.getId()));

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent myIntent = new Intent(BookContentsActivity.this, BookViewerActivity.class);
                Bundle b = new Bundle();
                b.putInt("book_id", (int) book.getId());
                b.putInt("chapter", position);
                myIntent.putExtras(b);
                startActivity(myIntent);
            }
        });
        listview.setAdapter(clAdapter);

        optButton=(ImageButton)findViewById(R.id.optnbtn_chapter);
        optButton.setOnTouchListener(btnListener);
        srchButton=(ImageButton)findViewById(R.id.searchbtn_chapter);
        srchButton.setOnTouchListener(btnListener);
        playbtn=(ImageButton)findViewById(R.id.playbtn_chapter);
        playbtn.setOnTouchListener(btnListener);
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

        sbMenu=new SidebarMenu(this, false, false, false, false);
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

    /**
     * A OnTouchListener for the existing buttons in this activity.
     *
     */
    private View.OnTouchListener btnListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent m) {
            if (v==optButton) {
                if(m.getAction()==MotionEvent.ACTION_UP)
                    if(sbMenu.getMenuDrawer().isMenuVisible())
                        sbMenu.getMenuDrawer().closeMenu();
                    else
                        sbMenu.getMenuDrawer().openMenu();
            } else if (v==srchButton) {
                if(m.getAction()==MotionEvent.ACTION_UP)
                    if(!searchbarVisible)
                        showSearchBar();
                    else
                        hideSearchBar();
            } else if (v==playbtn) {
                ((ImageButton)v).setImageResource(R.drawable.playbtn);
                if(m.getAction()==MotionEvent.ACTION_DOWN)
                    ((ImageButton)v).setImageResource(R.drawable.playbtn_pressed);
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    ((ImageButton)v).setImageResource(R.drawable.playbtn);
                    Intent myIntent = new Intent(BookContentsActivity.this, BookViewerActivity.class);
                    Bundle b = new Bundle();
                    b.putInt("book_id", (int) book.getId());
                    b.putInt("chapter", -1);
                    myIntent.putExtras(b);
                    startActivity(myIntent);
                }
            }
            return true;
        }
    };

    /**
     * Shows the searchbar, opens the keyboard and focuses input to it.
     *
     */
    private void showSearchBar() {
        searchbar.setVisibility(View.VISIBLE);
        searchbarVisible = true;

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        searchbar.requestFocus();
        inputMethodManager.showSoftInput(searchbar, 0);
    }

    /**
     * Closes the searchbar and closes the keyboard.
     *
     */
    private void hideSearchBar() {
        searchbar.setVisibility(View.GONE);
        searchbarVisible = false;
        clAdapter.updateChapterList();
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException n) {
            //do nothing
        }
    }

    /**
     * Hides the searchbar after a specific time
     *
     * @param time until searchbar is closed
     */
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

    /**
     * Textwatcher that observes the searchbar and closes it after some time if not used.
     *
     */
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

    /**
     * BaseAdapter that fills the list of Contents with Listitems.
     *
     */
    private class CLAdapter extends BaseAdapter {
        private List<Content> chapterlist;
        private List<Content> visiblechapterlist;

        private class ItemHolder {
            TextView heading;
            TextView numberOfWords;
            ImageView marker;
        }

        public CLAdapter(List<Content> chapterlist) {
            this.chapterlist=chapterlist;
            this.visiblechapterlist=chapterlist;
        }

        public void updateChapterList() {
            updateChapterList("");
        }

        public void updateChapterList(String s) {
            chapterlist=bookService.getLightweightContentsOfBook(book.getId());

            if (!s.isEmpty() && searchbarVisible) {
                ArrayList<Content> temp = new ArrayList<Content>();
                for (Content c : chapterlist) {
                    if (c.getHeading().toLowerCase().contains(s.toLowerCase()))
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
        public Content getItem(int position) {
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

            holder.heading.setText(getString(R.string.content)+ " "+ visiblechapterlist.get(position).getHeading());
            holder.numberOfWords.setText(bookService.getNumberOfWords(visiblechapterlist.get(position).getId()) + " " + getString(R.string.words));

            if (bookService.getCurrentPosition(book.getId()).getCurrentContent()==position)
                holder.marker.setVisibility(View.VISIBLE);
            else
                holder.marker.setVisibility(View.GONE);
            return convertView;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SuperActivityToast.onSaveState(outState);
    }
}
