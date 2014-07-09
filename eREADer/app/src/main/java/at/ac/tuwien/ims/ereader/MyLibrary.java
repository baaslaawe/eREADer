package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import at.ac.tuwien.ims.ereader.Entities.Book;
import at.ac.tuwien.ims.ereader.Entities.Language;

public class MyLibrary extends Activity {
    private ImageButton optButton;
    private ImageButton srchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_library);
        getActionBar().hide();

        ArrayList<Book> templist=new ArrayList<Book>();
        templist.add(new Book("The Lord Of The Rings", "J. R. R. Tolkien", Language.English));
        templist.add(new Book("Bla1", "Bla1", Language.English));
        templist.add(new Book("Bla2", "Bla2", Language.Espanol));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));
        templist.add(new Book("Bla3", "Bla3", Language.Deutsch));

        ListView listview = (ListView)findViewById(R.id.booklist);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
                showMessage("item clicked");
            }
        });
        listview.setAdapter(new BLAdapter(templist, listview));

        optButton=(ImageButton)findViewById(R.id.optnbtn);
        optButton.setOnClickListener(btnListener);

        srchButton=(ImageButton)findViewById(R.id.searchbtn);
        srchButton.setOnClickListener(btnListener);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v==optButton) {
                showMessage("optn btn clicked");
            } else if (v==srchButton) {
                showMessage("search btn clicked");
            }
        }
    };

    private class BLAdapter extends BaseAdapter {
        private ArrayList<Book> booklist;
        private ListView listview;

        private class ItemHolder {
            public TextView title;
            public TextView author_and_lang;
        }

        public BLAdapter(ArrayList<Book> booklist, ListView listview) {
            this.booklist=booklist;
            this.listview=listview;
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

                convertView.findViewById(R.id.optbtnlist).setOnClickListener(optBtnListener);

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
                    showMessage("list optn btn clicked");
                }
            }
        };
    }

    private void showMessage(String message) {
        Toast.makeText(MyLibrary.this, message, Toast.LENGTH_SHORT).show();
    }
}
