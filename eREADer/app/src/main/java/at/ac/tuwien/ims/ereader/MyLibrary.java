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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Language;

public class MyLibrary extends Activity {
    private ImageButton optButton;
    private ImageButton srchButton;
    private BLAdapter blAdapter;
    private boolean searchbarVisible;
    private EditText searchbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_library);

        if (getActionBar() != null)
            getActionBar().hide();

        //todo remove if persistent
        ArrayList<Book> booklist =new ArrayList<Book>();
        Book testb=new Book("The Lord Of The Rings", "J. R. R. Tolkien", Language.English);
        ArrayList<String> testbContent = new ArrayList<String>();
        testbContent.add("1 BLABLABLABLABLA");
        testbContent.add("3 BLABLABLABLABLA");
        testbContent.add("4 BLABLABLABLABLA");
        testbContent.add("5 BLABLABLABLABLA");
        testb.setContent(testbContent);
        ArrayList<String> testbChapter = new ArrayList<String>();
        testbChapter.add("Chapter 1");
        testbChapter.add("Chapter 2");
        testbChapter.add("Chapter 3");
        testbChapter.add("Chapter 4");
        testbChapter.add("Chapter 5");
        testb.setChapters(testbChapter);
        booklist.add(testb);
        booklist.add(new Book("Bla1", "Bla1", Language.English));
        booklist.add(new Book("Bla2", "Bla2", Language.Espanol));
        booklist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        booklist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        booklist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        booklist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        booklist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        booklist.add(new Book("Bla3", "Bla3", Language.Deutsch));

        ListView listview = (ListView)findViewById(R.id.booklist);
        blAdapter = new BLAdapter(listview, booklist);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
                Intent myIntent = new Intent(MyLibrary.this, BookView.class);
                Bundle b = new Bundle();
                b.putInt("list", position);
                myIntent.putExtras(b);
                startActivity(myIntent);
            }
        });
        listview.setAdapter(blAdapter);

        optButton=(ImageButton)findViewById(R.id.optnbtn_lib);
        optButton.setOnClickListener(btnListener);

        srchButton=(ImageButton)findViewById(R.id.searchbtn_lib);
        srchButton.setOnClickListener(btnListener);

        searchbar=(EditText)findViewById(R.id.searchinput);
        searchbar.addTextChangedListener(textWatcher);
        hideSearchBar();
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==optButton) {
                //todo
                showMessage("optn btn clicked lib");
            } else if (v==srchButton) {
                if(!searchbarVisible) {
                    showSearchBar();
                } else {
                    hideSearchBar();
                }
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
            if (s.toString().length()==0) {
                hideSearchBarAfterSomeTime(5000);
                return;
            }
            blAdapter.updateBookList(s.toString());
        }
    };

    private class BLAdapter extends BaseAdapter {
        private ListView listview;
        private ArrayList<Book> initialBooklist;
        private ArrayList<Book> booklist;

        private class ItemHolder {
            public TextView title;
            public TextView author_and_lang;
        }

        public BLAdapter(ListView listview, ArrayList<Book> booklist) {
            this.listview=listview;
            this.booklist=booklist;
            this.initialBooklist=booklist;
        }

        public void updateBookList(String s) {
            if (s.length()==0 || !searchbarVisible) {
                booklist=initialBooklist;
                return;
            }
            ArrayList<Book> temp = new ArrayList<Book>();
            for (Book b : initialBooklist) {
                if (b.getTitle().toLowerCase().contains(s.toLowerCase()) ||
                        b.getAuthor().toLowerCase().contains(s.toLowerCase()) ||
                        b.getLanguage().toString().toLowerCase().contains(s.toLowerCase()))
                    temp.add(b);
            }
            booklist=temp;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return booklist.size();
        }

        @Override
        public Book getItem(int position) {
            return booklist.get(position);
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

                ImageButton bt=(ImageButton)convertView.findViewById(R.id.optbtnlist);
                bt.setOnClickListener(optBtnListener);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            holder.title.setText(booklist.get(position).getTitle());
            holder.author_and_lang.setText(booklist.get(position).getAuthor() + ", " + booklist.get(position).getLanguage());

            return convertView;
        }

        private View.OnClickListener optBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = listview.getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    registerForContextMenu(v);
                    openContextMenu(v);
                }
            }
        };
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.library_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()){
            case R.id.play:
                //todo
                showMessage("cntxt menu play pressed");
                break;
            case R.id.delete:
                //todo
                showMessage("cntxt menu delete pressed");
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showMessage(String message) {
        Toast.makeText(MyLibrary.this, message, Toast.LENGTH_SHORT).show();
    }
}
