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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Language;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;
import at.ac.tuwien.ims.ereader.Util.StaticHelper;

/**
 * Activity to display all available eBooks in the database.
 *
 * @author Florian Schuster
 */
public class MyLibraryActivity extends Activity {
    private ImageButton optButton;
    private ImageButton srchButton;
    private ImageButton addButton;
    private BLAdapter blAdapter;
    private boolean searchbarVisible;
    private EditText searchbar;

    private BookService bookService;
    private SidebarMenu sbMenu;

    private AlertDialog dialogEdit;
    private EditText author;
    private EditText title;
    private Spinner langspinner;

    private AdapterView.AdapterContextMenuInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SuperActivityToast.onRestoreState(savedInstanceState, MyLibraryActivity.this);
        setContentView(R.layout.activity_my_library);
        if (getActionBar() != null)
            getActionBar().hide();

        SharedPreferences settings = getSharedPreferences("settings", 0);
        if(!settings.getBoolean("startUpHelpSeen", false)) {
            startActivity(new Intent(MyLibraryActivity.this, HelpActivity.class));
        }

        bookService=new BookService(this);

        ListView listview = (ListView)findViewById(R.id.booklist);
        blAdapter = new BLAdapter(listview, bookService.getAllBooksAlphabetically());

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
                Intent myIntent = new Intent(MyLibraryActivity.this, BookContentsActivity.class);
                Bundle b = new Bundle();
                b.putInt("book_id", (int) blAdapter.getItem(position).getId());
                myIntent.putExtras(b);
                startActivity(myIntent);
            }
        });
        listview.setAdapter(blAdapter);

        optButton=(ImageButton)findViewById(R.id.optnbtn_lib);
        optButton.setOnTouchListener(btnListener);
        srchButton=(ImageButton)findViewById(R.id.searchbtn_lib);
        srchButton.setOnTouchListener(btnListener);
        addButton=(ImageButton)findViewById(R.id.plusbtn);
        addButton.setOnTouchListener(btnListener);
        searchbar=(EditText)findViewById(R.id.searchinput);
        searchbar.addTextChangedListener(textWatcher);

        sbMenu=new SidebarMenu(this, true, false, false, false);

        View editView = getLayoutInflater().inflate(R.layout.dialog_editbook, null);
        AlertDialog.Builder editBuilder = new AlertDialog.Builder(this);
        editBuilder.setView(editView)
                .setPositiveButton(R.string.save, dialogEditClickListener)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(getString(R.string.edit_book));
        dialogEdit=editBuilder.create();

        author=(EditText)editView.findViewById(R.id.dialog_author);
        title=(EditText)editView.findViewById(R.id.dialog_title);
        langspinner=(Spinner)editView.findViewById(R.id.dialog_lang);
        String[] array=new String[]{
                getString(R.string.ger),
                getString(R.string.eng),
                getString(R.string.esp),
                getString(R.string.fr),
                getString(R.string.unknown)};
        langspinner.setAdapter(new ArrayAdapter(MyLibraryActivity.this, android.R.layout.simple_spinner_dropdown_item, array));
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
            } else if (v==addButton) {
                ((ImageButton)v).setImageResource(R.drawable.plusbtn);
                if(m.getAction()== MotionEvent.ACTION_DOWN)
                    ((ImageButton)v).setImageResource(R.drawable.plusbtn_pressed);
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    ((ImageButton)v).setImageResource(R.drawable.plusbtn);
                    Intent myIntent = new Intent(MyLibraryActivity.this, AddBookActivity.class);
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
        blAdapter.updateBookList();
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
                            if (searchbar.getText().toString().isEmpty())
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
            blAdapter.updateBookList(s.toString());
        }
    };

    /**
     * BaseAdapter that fills the list of ebooks with Listitems.
     *
     */
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
            booklist=bookService.getAllBooksAlphabetically();

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
                bt.setOnTouchListener(optBtnListener);
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
                case FR:
                    lang=getString(R.string.fr);
                    break;
            }
            holder.author_and_lang.setText(visiblebooklist.get(position).getAuthor() + ", " + lang);
            return convertView;
        }

        private View.OnTouchListener optBtnListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent m) {
                ((ImageButton)v).setImageResource(R.drawable.setbtn);
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton)v).setImageResource(R.drawable.setbtn_pressed);
                } else if(m.getAction()==MotionEvent.ACTION_UP) {
                    ((ImageButton)v).setImageResource(R.drawable.setbtn);
                    final int position = listview.getPositionForView(v);
                    if (position != ListView.INVALID_POSITION) {
                        openContextMenu(v);
                    }
                }
                return true;
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
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()){
            case R.id.play:
                Intent myIntent = new Intent(MyLibraryActivity.this, BookViewerActivity.class);
                Bundle b = new Bundle();
                b.putInt("book_id", (int)blAdapter.getItem(info.position).getId());
                b.putInt("chapter", -1);
                myIntent.putExtras(b);
                startActivity(myIntent);
                break;
            case R.id.edit:
                author.setHint(blAdapter.getItem(info.position).getAuthor());
                title.setHint(blAdapter.getItem(info.position).getTitle());
                langspinner.setSelection(blAdapter.getItem(info.position).getLanguage().getCode());
                dialogEdit.show();
                break;
            case R.id.delete:
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(MyLibraryActivity.this);
                deleteBuilder.setTitle(getString(R.string.delete_book))
                        .setMessage(getString(R.string.sure_delete) + "\n" + blAdapter.getItem(info.position).getTitle())
                        .setPositiveButton(getString(R.string.delete), dialogDeleteClickListener)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    DialogInterface.OnClickListener dialogEditClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String aut=author.getText().length()==0 ? author.getHint().toString() : author.getText().toString();
            String tit=title.getText().length()==0 ? title.getHint().toString() : title.getText().toString();
            Language l=Language.getLanguageFromCode(langspinner.getSelectedItemPosition());
            bookService.updateBook(new Book(blAdapter.getItem(info.position).getId(), tit, aut, l));
            blAdapter.updateBookList();
        }
    };

    DialogInterface.OnClickListener dialogDeleteClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            bookService.deleteBook(blAdapter.getItem(info.position).getId());
            showMessage(blAdapter.getItem(info.position).getTitle() + " " + getString(R.string.wasdeleted));
            blAdapter.updateBookList();
        }
    };

    private void showMessage(String message) {
        SuperToast toast=new SuperToast(this);
        toast.setText(message);
        toast.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SuperActivityToast.onSaveState(outState);
    }
}
