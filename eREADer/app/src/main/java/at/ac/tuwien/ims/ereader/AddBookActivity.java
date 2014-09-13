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
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.DownloadHost;
import at.ac.tuwien.ims.ereader.Entities.Language;
import at.ac.tuwien.ims.ereader.Services.BookService;
import at.ac.tuwien.ims.ereader.Services.ServiceException;
import at.ac.tuwien.ims.ereader.Util.SidebarMenu;
import at.ac.tuwien.ims.ereader.Util.SimpleFileDialog;
import at.ac.tuwien.ims.ereader.Util.StaticHelper;

/**
 * Activity to add eBooks and inform user about download hosts.
 *
 * @author Florian Schuster
 */
public class AddBookActivity extends Activity {
    private Button add_button;
    private ImageButton optButton;
    private BookService bookService;
    private SidebarMenu sbMenu;
    private SimpleFileDialog fileDialog;

    private AlertDialog dialogLanguage;
    private Spinner langspinner_lang;
    private String temp_chosendir;

    private AlertDialog dialogEdit;
    private EditText author;
    private EditText title;
    private Spinner langspinner_edit;
    private long tempbook_id;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private List<String> contentString;

    private TextView url_gutenberg;
    private TextView url_freeebooks;
    private TextView url_mobileread;

    private String dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SuperActivityToast.onRestoreState(savedInstanceState, AddBookActivity.this);
        setContentView(R.layout.activity_addbook);
        if (getActionBar() != null)
            getActionBar().hide();

        bookService=new BookService(this);
        contentString=new ArrayList<String>();

        add_button=(Button)findViewById(R.id.add_button);
        add_button.setOnTouchListener(btnListener);
        optButton=(ImageButton)findViewById(R.id.optnbtn_add);
        optButton.setOnTouchListener(btnListener);

        url_gutenberg=(TextView)findViewById(R.id.gutenberg_url);
        url_gutenberg.setText(getString(R.string.gutenberg_url)+", " + getString(R.string.multiple_langs));
        url_freeebooks=(TextView)findViewById(R.id.freeebooks_url);
        url_freeebooks.setText(getString(R.string.free_ebooks_url)+", " + getString(R.string.multiple_langs));
        url_mobileread=(TextView)findViewById(R.id.mobileread_url);
        url_mobileread.setText(getString(R.string.mobileread_url)+", " + getString(R.string.ger));

        findViewById(R.id.gutenberg_howto).setOnTouchListener(howToButtonListener);
        findViewById(R.id.freeEbooks_howto).setOnTouchListener(howToButtonListener);
        findViewById(R.id.mobileread_howto).setOnTouchListener(howToButtonListener);
        findViewById(R.id.general_howto).setOnTouchListener(howToButtonListener);

        findViewById(R.id.gutenberg_host).setOnClickListener(onHostClickListener);
        findViewById(R.id.freeEbooks_host).setOnClickListener(onHostClickListener);
        findViewById(R.id.mobileread_host).setOnClickListener(onHostClickListener);
        findViewById(R.id.general_host).setOnClickListener(onHostClickListener);

        sbMenu=new SidebarMenu(this, false, false, false, true);

        fileDialog=new SimpleFileDialog(AddBookActivity.this, getString(R.string.select_book), new SimpleFileDialog.SimpleFileDialogListener() {
            @Override
            public void onChosenDir(final String chosenDir) {
                if(chosenDir.endsWith(".pdf")||chosenDir.endsWith(".txt")||chosenDir.endsWith(".html")||chosenDir.endsWith(".htm")||chosenDir.endsWith(".epub")) {
                    contentString.add(chosenDir.substring(chosenDir.lastIndexOf("/") + 1).trim());
                    if(chosenDir.endsWith(".epub")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AddTask().execute(chosenDir);
                            }
                        });
                    } else {
                        temp_chosendir = chosenDir;
                        dialogLanguage.show();
                    }
                } else {
                    showMessage(getString(R.string.format_not_supported));
                }
            }
        });

        View languageDialogView = getLayoutInflater().inflate(R.layout.dialog_selectlanguage, null);
        AlertDialog.Builder languageAlertBuilder = new AlertDialog.Builder(this);
        languageAlertBuilder.setView(languageDialogView)
                .setPositiveButton(R.string.save, dialogLangClickListener)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(getString(R.string.select_language_book));
        dialogLanguage =languageAlertBuilder.create();
        langspinner_lang=(Spinner)languageDialogView.findViewById(R.id.dialog_lang);
        String[] array=new String[]{
                getString(R.string.ger),
                getString(R.string.eng),
                getString(R.string.esp),
                getString(R.string.fr)};
        langspinner_lang.setAdapter(new ArrayAdapter(AddBookActivity.this, android.R.layout.simple_spinner_dropdown_item, array));

        View editView = getLayoutInflater().inflate(R.layout.dialog_editbook, null);
        AlertDialog.Builder editBuilder = new AlertDialog.Builder(this);
        editBuilder.setView(editView)
                .setPositiveButton(R.string.save, dialogEditClickListener)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(getString(R.string.edit_after_insert));
        dialogEdit=editBuilder.create();
        author=(EditText)editView.findViewById(R.id.dialog_author);
        title=(EditText)editView.findViewById(R.id.dialog_title);
        langspinner_edit=(Spinner)editView.findViewById(R.id.dialog_lang);
        langspinner_edit.setAdapter(new ArrayAdapter(AddBookActivity.this, android.R.layout.simple_spinner_dropdown_item, array));

        notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.adding_book))
                .setSmallIcon(R.drawable.logo_small_bar_dl)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setProgress(0, 0, true);

        //todo not tested on phones with external sd cards
        dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        if(dir.isEmpty())
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();
        //todo is empty on some phones?
    }

    /**
     * OnClickListener that handles clicking a downloadhost item.
     *
     */
    View.OnClickListener onHostClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.gutenberg_host:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gutenberg_url))));
                    break;
                case R.id.freeEbooks_host:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.free_ebooks_url))));
                    break;
                case R.id.mobileread_host:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.mobileread_url))));
                    break;
                case R.id.general_host:
                    AlertDialog.Builder ab = new AlertDialog.Builder(AddBookActivity.this);
                    ab.setMessage(getString(R.string.general_download_howto)).setNeutralButton(getString(R.string.understood), null).show();
                    break;
            }
        }
    };

    /**
     * OnTouchListener that handles the questionmark ImageButton in a downloadhost item.
     *
     */
    View.OnTouchListener howToButtonListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent m) {
            if (m.getAction() == MotionEvent.ACTION_DOWN) {
                ((ImageButton)v).setImageResource(R.drawable.howtobtn_pressed);
            } else if(m.getAction()==MotionEvent.ACTION_UP) {
                ((ImageButton)v).setImageResource(R.drawable.howtobtn);

                switch(v.getId()) {
                    case R.id.gutenberg_howto:
                        new AlertDialog.Builder(AddBookActivity.this)
                                .setMessage(getString(R.string.gutenberg_howto))
                                .setNeutralButton(getString(R.string.understood), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gutenberg_url))));
                                    }
                                })
                                .setTitle(getString(R.string.dl_info)).show();
                        break;
                    case R.id.freeEbooks_howto:
                        new AlertDialog.Builder(AddBookActivity.this)
                                .setMessage(getString(R.string.free_ebooks_howto))
                                .setNeutralButton(getString(R.string.understood), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.free_ebooks_url))));
                                    }
                                })
                                .setTitle(getString(R.string.dl_info)).show();
                        break;
                    case R.id.mobileread_howto:
                        new AlertDialog.Builder(AddBookActivity.this)
                                .setMessage(getString(R.string.mobileread_howto))
                                .setNeutralButton(getString(R.string.understood), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.mobileread_url))));
                                    }
                                })
                                .setTitle(getString(R.string.dl_info)).show();
                        break;
                    case R.id.general_howto:
                        new AlertDialog.Builder(AddBookActivity.this)
                                .setMessage(getString(R.string.general_download_howto))
                                .setNeutralButton(getString(R.string.understood), null)
                                .setTitle(getString(R.string.dl_info)).show();
                        break;
                }
            }
            return true;
        }
    };

    /**
     * OnClickListener that handles the save button inside the language select alert builder that is
     * opened before a TXT/PDF/HTML eBook is loaded.
     *
     */
    DialogInterface.OnClickListener dialogLangClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String l=(String) langspinner_lang.getSelectedItem();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AddTask().execute(temp_chosendir, l);
                }
            });
        }
    };

    /**
     * OnClickListener that handles the save book button inside the edit book alert builder that is
     * opened after a book is added but has unkown author/title/language.
     *
     */
    DialogInterface.OnClickListener dialogEditClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String aut=author.getText().length()==0 ? author.getHint().toString() : author.getText().toString();
            String tit=title.getText().length()==0 ? title.getHint().toString() : title.getText().toString();
            Language l=Language.getLanguageFromCode(langspinner_edit.getSelectedItemPosition());
            bookService.updateBook(new Book(tempbook_id, tit, aut, l));
            showMessage(getString(R.string.success_ebook_add));
            startActivity(new Intent(AddBookActivity.this, MyLibraryActivity.class));
        }
    };

    /**
     * Asynchronous Task that adds a book by its format and informs the user if it is done or
     * if an error has occurred.
     * Also informs the user via notification what books are being added.
     *
     */
    private class AddTask extends AsyncTask<String, Integer, Book> {
        protected Book doInBackground(String... vars) {
            try {
                String URI=vars[0];
                String cont="";
                for(int i=0;i<contentString.size();i++) {
                    cont+=contentString.get(i);
                    if(i<contentString.size()-1)
                        cont+=", ";
                }
                mBuilder.setContentText(cont);
                notificationManager.notify(StaticHelper.NOTIFICATION_ID_ADD, mBuilder.build());
                //todo notification is not updated when adding multiple books...

                if(URI.endsWith(".epub"))
                    return bookService.addBookAsEPUB(URI);
                else if(URI.endsWith(".pdf"))
                    return bookService.addBookAsPDF(URI, vars[1]);
                else if(URI.endsWith(".txt"))
                    return bookService.addBookAsTXT(URI, vars[1]);
                else if(URI.endsWith(".html")||URI.endsWith(".htm"))
                    return bookService.addBookAsHTML(URI, vars[1]);
            } catch(ServiceException s) {
                showMessage(s.getMessage());
                return null;
            }
            return null;
        }

        protected void onPostExecute(Book book) {
            notificationManager.cancel(StaticHelper.NOTIFICATION_ID_ADD);
            if(!contentString.isEmpty())
                contentString.remove(0);
            if(book!=null) {
                if(book.getTitle().equals(getString(R.string.no_title)) || book.getAuthor().equals(getString(R.string.no_author)) || book.getLanguage()==Language.UNKNOWN) {
                    tempbook_id=book.getId();
                    author.setHint(book.getAuthor());
                    title.setHint(book.getTitle());
                    langspinner_edit.setSelection(book.getLanguage().getCode());
                    dialogEdit.show();
                } else {
                    showMessage(getString(R.string.success_ebook_add));
                    startActivity(new Intent(AddBookActivity.this, MyLibraryActivity.class));
                }
            }
        }
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
            if (v==add_button) {
                if(m.getAction()==MotionEvent.ACTION_DOWN)
                    v.setBackgroundColor(Color.parseColor(StaticHelper.COLOR_Blue));
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    fileDialog.chooseFile_or_Dir(dir);
                    v.setBackgroundColor(Color.parseColor(StaticHelper.COLOR_Grey));
                }
            } else if(v==optButton) {
                if(m.getAction()==MotionEvent.ACTION_UP)
                    if(sbMenu.getMenuDrawer().isMenuVisible())
                        sbMenu.getMenuDrawer().closeMenu();
                    else
                        sbMenu.getMenuDrawer().openMenu();
            }
            return true;
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

     /*
    //use with find(Environment.getExternalStorageDirectory()); if synchonization is needed
    private List<String> files=new ArrayList<String>();
    public void find(File dir) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    find(listFile[i]);
                } else {
                    String fileName=listFile[i].getName();
                    if (fileName.endsWith(".pdf")||fileName.endsWith(".txt")||fileName.endsWith(".html")||fileName.endsWith(".htm")||fileName.endsWith(".epub")){
                        files.add(fileName);
                    }
                }
            }
        }
    }*/
}