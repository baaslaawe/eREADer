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
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
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
    private DHAdapter dhAdapter;
    private SidebarMenu sbMenu;
    private SimpleFileDialog fileDialog;
    private SuperActivityToast addToast;

    private AlertDialog dialogEdit;
    private Spinner langspinner;
    private String chosenDir2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbook);
        if (getActionBar() != null)
            getActionBar().hide();

        bookService=new BookService(this);

        add_button=(Button)findViewById(R.id.add_button);
        add_button.setOnTouchListener(btnListener);
        optButton=(ImageButton)findViewById(R.id.optnbtn_add);
        optButton.setOnTouchListener(btnListener);

        ListView listview = (ListView)findViewById(R.id.downloadhosts_list);
        dhAdapter = new DHAdapter(listview, getDownloadHosts());
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
                String uri=dhAdapter.getItem(position).getURL();
                if (uri.isEmpty()) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(AddBookActivity.this);
                    ab.setMessage(dhAdapter.getItem(position).getHow_to_string()).setNeutralButton(getString(R.string.understood), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }).show();
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            }
        });
        listview.setAdapter(dhAdapter);

        sbMenu=new SidebarMenu(this, false, false, false);

        fileDialog =new SimpleFileDialog(AddBookActivity.this, "FileOpen",
                new SimpleFileDialog.SimpleFileDialogListener() {
                    @Override
                    public void onChosenDir(final String chosenDir) {
                        chosenDir2=chosenDir;
                        if(chosenDir.endsWith(".pdf")||chosenDir.endsWith(".txt")) {
                            dialogEdit.show();
                        } else {
                            addToast.show();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AddTask().execute(chosenDir);
                                }
                            });
                        }
                    }
                });
        fileDialog.Default_File_Name = "";

        addToast = new SuperActivityToast(this, SuperToast.Type.PROGRESS);
        addToast.setText(getString(R.string.parse_str));
        addToast.setIndeterminate(true);
        addToast.setProgressIndeterminate(true);

        View editView = getLayoutInflater().inflate(R.layout.dialog_selectlanguage, null);
        AlertDialog.Builder editBuilder = new AlertDialog.Builder(this);
        editBuilder.setView(editView)
                .setPositiveButton(R.string.save, dialogEditClickListener)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(getString(R.string.select_language_book));
        dialogEdit=editBuilder.create();

        langspinner=(Spinner)editView.findViewById(R.id.dialog_lang);
        String[] array=new String[]{
                getString(R.string.ger),
                getString(R.string.eng),
                getString(R.string.esp),
                getString(R.string.fr)};
        langspinner.setAdapter(new ArrayAdapter(AddBookActivity.this, android.R.layout.simple_spinner_dropdown_item, array));

        //todo add help for downloading
    }

    DialogInterface.OnClickListener dialogEditClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String l=(String)langspinner.getSelectedItem();
            addToast.show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AddTask().execute(chosenDir2, l);
                }
            });
        }
    };

    /**
     * Asynchronous Task that adds a book by its format and informs the user if it is done or
     * if an error has occurred.
     *
     */
    private class AddTask extends AsyncTask<String, Integer, Boolean> {
        protected Boolean doInBackground(String... vars) {
            try {
                String URI=vars[0];
                if(URI.endsWith(".epub")) {
                    bookService.addBookAsEPUB(URI);
                } else if(URI.endsWith(".pdf")) {
                    bookService.addBookAsPDF(URI, vars[1]);
                } else if(URI.endsWith(".txt")) {
                    bookService.addBookAsTXT(URI, vars[1]);
                } else
                    throw new ServiceException(getString(R.string.format_not_supported));
            } catch(ServiceException s) {
                showMessage(s.getMessage());
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean success) {
            addToast.dismiss();
            if(success) {
                showMessage(getString(R.string.success_ebook_add));
                startActivity(new Intent(AddBookActivity.this, MyLibraryActivity.class));
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

    /**todo http://allesebook.de/kostenlose-ebooks/
     * A method that fills a list with available DownloadHosts.
     *
     * @return a list with DownloadHosts
     */
    private List<DownloadHost> getDownloadHosts() {
        ArrayList<DownloadHost> dhList=new ArrayList<DownloadHost>();
        dhList.add(new DownloadHost(getString(R.string.gutenberg_name), "http://m.gutenberg.org/", getString(R.string.gutenberg_howto), Language.EN));
        dhList.add(new DownloadHost(getString(R.string.free_ebooks_name), "http://www.free-ebooks.net", getString(R.string.free_ebooks_howto), Language.EN));
        dhList.add(new DownloadHost(getString(R.string.mobileread_name), "http://wiki.mobileread.com/wiki/Free_eBooks-de/de", getString(R.string.mobileread_howto), Language.DE));
        dhList.add(new DownloadHost(getString(R.string.general_download_name), "", getString(R.string.general_download_howto), null));
        return dhList;
    }

    /**
     * BaseAdapter that fills the list of DownloadHosts with Listitems.
     *
     */
    private class DHAdapter extends BaseAdapter {
        private ListView listview;
        private List<DownloadHost> dhList;

        private class ItemHolder {
            public TextView page_name;
            public TextView site_url;
        }

        public DHAdapter(ListView listview, List<DownloadHost> dhList) {
            this.listview=listview;
            this.dhList=dhList;
        }

        @Override
        public int getCount() {
            return dhList.size();
        }

        @Override
        public DownloadHost getItem(int position) {
            return dhList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder = null;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.downloadlist_item, parent, false);

                holder = new ItemHolder();
                holder.page_name = (TextView) convertView.findViewById(R.id.page_name);
                holder.site_url = (TextView) convertView.findViewById(R.id.site_url);

                ImageButton bt = (ImageButton) convertView.findViewById(R.id.howtobtnlist);
                bt.setOnTouchListener(howtobtnListener);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            holder.page_name.setText(dhList.get(position).getSite_name());
            if(dhList.get(position).getLanguage()==null)
                holder.site_url.setText(dhList.get(position).getURL());
            else {
                String lang="";
                switch (dhList.get(position).getLanguage()) {
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
                holder.site_url.setText(lang+", "+dhList.get(position).getURL());
            }
            return convertView;
        }

        private View.OnTouchListener howtobtnListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent m) {
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton)v).setImageResource(R.drawable.howtobtn_pressed);
                } else if(m.getAction()==MotionEvent.ACTION_UP) {
                    ((ImageButton)v).setImageResource(R.drawable.howtobtn);
                    final int position = listview.getPositionForView(v);
                    if (position != ListView.INVALID_POSITION) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(AddBookActivity.this);
                        ab.setMessage(dhList.get(position).getHow_to_string()).setNeutralButton(getString(R.string.understood), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        }).show();
                    }
                }
                return true;
            }
        };
    }

    private View.OnTouchListener btnListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent m) {
            if (v==add_button) {
                if(m.getAction()==MotionEvent.ACTION_DOWN)
                    v.setBackgroundColor(Color.parseColor(StaticHelper.COLOR_Blue));
                else if(m.getAction()==MotionEvent.ACTION_UP) {
                    fileDialog.chooseFile_or_Dir();
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
}