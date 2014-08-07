package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;

public class MyLibraryActivity extends Activity {
    private ImageButton optButton;
    private ImageButton srchButton;
    private ImageButton addButton;
    private BLAdapter blAdapter;
    private boolean searchbarVisible;
    private EditText searchbar;

    private BookService bookService;
    private SidebarMenu sbMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_library);

        if (getActionBar() != null)
            getActionBar().hide();
        bookService=new BookService(this);

        ListView listview = (ListView)findViewById(R.id.booklist);
        blAdapter = new BLAdapter(listview, bookService.getAllBooks());

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
                Intent myIntent = new Intent(MyLibraryActivity.this, BookChaptersActivity.class);
                Bundle b = new Bundle();
                b.putInt("book_id", (int)blAdapter.getItem(position).getId());
                myIntent.putExtras(b);
                startActivity(myIntent);
            }
        });
        listview.setAdapter(blAdapter);

        optButton=(ImageButton)findViewById(R.id.optnbtn_lib);
        optButton.setOnClickListener(btnListener);
        srchButton=(ImageButton)findViewById(R.id.searchbtn_lib);
        srchButton.setOnClickListener(btnListener);
        addButton=(ImageButton)findViewById(R.id.plusbtn);
        addButton.setOnClickListener(btnListener);
        searchbar=(EditText)findViewById(R.id.searchinput);
        searchbar.addTextChangedListener(textWatcher);

        sbMenu=new SidebarMenu(this, true, false, false);

        registerForContextMenu(listview);
        hideSearchBar();
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
            } else if (v==addButton) {
                Intent myIntent = new Intent(MyLibraryActivity.this, AddBookActivity.class);
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
        blAdapter.updateBookList();
    }

    private void hideSearchBarAfterSomeTime(int time) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchbarVisible)
                            if (searchbar.getText().toString().isEmpty())
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
            blAdapter.updateBookList(s.toString());
        }
    };

    private class BLAdapter extends BaseAdapter {
        private ListView listview;
        private List<Book> booklist;
        private List<Book> visiblebooklist;

        private class ItemHolder {
            public TextView title;
            public TextView author_and_lang;
        }

        public BLAdapter(ListView listview, List<Book> bl) {
            this.listview=listview;
            this.booklist=bl;
            this.visiblebooklist=bl;
        }

        public void updateBookList() {
            updateBookList("");
        }

        public void updateBookList(String s) {
            booklist=bookService.getAllBooks();

            if (!s.isEmpty() && searchbarVisible) {
                ArrayList<Book> temp = new ArrayList<Book>();
                for (Book b : booklist) {
                    if (b.getTitle().toLowerCase().contains(s.toLowerCase()) ||
                            b.getAuthor().toLowerCase().contains(s.toLowerCase()) ||
                            b.getLanguage().toString().toLowerCase().contains(s.toLowerCase()))
                        temp.add(b);
                }
                visiblebooklist=temp;
            } else
                visiblebooklist=booklist;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return visiblebooklist.size();
        }

        @Override
        public Book getItem(int position) {
            return visiblebooklist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder = null;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.booklist_item, parent, false);

                holder = new ItemHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.author_and_lang = (TextView) convertView.findViewById(R.id.author_and_lang);

                ImageButton bt = (ImageButton) convertView.findViewById(R.id.optbtnlist);
                bt.setOnClickListener(optBtnListener);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            holder.title.setText(visiblebooklist.get(position).getTitle());
            String lang="";
            switch (visiblebooklist.get(position).getLanguage()) {
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
            holder.author_and_lang.setText(visiblebooklist.get(position).getAuthor() + ", " + lang);
            return convertView;
        }

        private View.OnClickListener optBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = listview.getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    openContextMenu(v);
                }
            }
        };
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.booklist)
            getMenuInflater().inflate(R.menu.library_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()){
            case R.id.play:
                Intent myIntent = new Intent(MyLibraryActivity.this, BookViewerActivity.class);
                Bundle b = new Bundle();
                b.putInt("book_id", (int)blAdapter.getItem(info.position).getId());
                b.putInt("chapter", -1);
                myIntent.putExtras(b);
                startActivity(myIntent);
                break;
            case R.id.delete:
                bookService.deleteBook((int)blAdapter.getItem(info.position).getId());
                showMessage(blAdapter.getItem(info.position).getTitle() + " " + getString(R.string.wasdeleted));
                blAdapter.updateBookList();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showMessage(String message) {
        Toast.makeText(MyLibraryActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
