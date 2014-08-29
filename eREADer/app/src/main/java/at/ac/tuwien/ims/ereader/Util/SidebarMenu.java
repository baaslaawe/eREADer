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
package at.ac.tuwien.ims.ereader.Util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ims.ereader.AddBookActivity;
import at.ac.tuwien.ims.ereader.HelpActivity;
import at.ac.tuwien.ims.ereader.MyLibraryActivity;
import at.ac.tuwien.ims.ereader.R;
import at.ac.tuwien.ims.ereader.SettingsActivity;

/**
 * A class that handles the sidebar menu that is displayed in all activities to navigate through the
 * application.
 *
 * @author Florian Schuster
 */
public class SidebarMenu {
    private List<SidebarItem> sidebarItems;
    private SBAdapter sbAdapter;
    private Activity activity;
    private MenuDrawer mDrawer;

    public SidebarMenu(Activity a, boolean libActive, boolean settActive, boolean helpActive, boolean addactive) {
        this.activity=a;
        sidebarItems=new ArrayList<SidebarItem>();
        sidebarItems.add(new SidebarItem(a.getString(R.string.library), R.drawable.libbtn,
                new Intent(activity, MyLibraryActivity.class), libActive));
        sidebarItems.add(new SidebarItem(a.getString(R.string.addbook), R.drawable.addbtn,
                new Intent(activity, AddBookActivity.class), addactive));
        sidebarItems.add(new SidebarItem(a.getString(R.string.settings), R.drawable.settbtn,
                new Intent(activity, SettingsActivity.class), settActive));
        sidebarItems.add(new SidebarItem(a.getString(R.string.help), R.drawable.helpbtn,
                new Intent(activity, HelpActivity.class), helpActive));

        mDrawer = MenuDrawer.attach(activity, MenuDrawer.Type.OVERLAY, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        mDrawer.setMenuView(R.layout.sidebar_menu);

        ListView listview = (ListView)activity.findViewById(R.id.sidebar_list);
        sbAdapter = new SBAdapter();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
                if (!sbAdapter.getItem(position).isActive())
                    activity.startActivity(sbAdapter.getItem(position).getPress_intent());
            }
        });
        listview.setAdapter(sbAdapter);
    }

    public MenuDrawer getMenuDrawer() {
        return mDrawer;
    }

    private class SBAdapter extends BaseAdapter {
        private class ItemHolder {
            public TextView name;
            public ImageView icon;
            public LinearLayout bg;
        }

        @Override
        public int getCount() {
            return sidebarItems.size();
        }

        @Override
        public SidebarItem getItem(int position) {
            return sidebarItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder;

            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.sidebar_item, parent, false);

                holder = new ItemHolder();
                holder.name = (TextView) convertView.findViewById(R.id.item_name);
                holder.icon = (ImageView) convertView.findViewById(R.id.item_icon);
                holder.bg = (LinearLayout) convertView.findViewById(R.id.sidebar_item_bg);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            holder.name.setText(sidebarItems.get(position).getName());
            holder.icon.setImageResource(sidebarItems.get(position).getIcon_id());
            if(sidebarItems.get(position).isActive())
                holder.bg.setBackgroundColor(Color.parseColor(StaticHelper.COLOR_Grey));
            else {
                int color = Color.TRANSPARENT;
                Drawable background = holder.bg.getBackground();
                if (background instanceof ColorDrawable)
                    color=((ColorDrawable)background).getColor();

                if(color!=Color.TRANSPARENT && color!=Color.parseColor(StaticHelper.COLOR_DarkGrey))
                    holder.bg.setBackgroundColor(Color.parseColor(StaticHelper.COLOR_White));
            }
            return convertView;
        }
    }

    private class SidebarItem {
        private String name;
        private int icon_id;
        private Intent press_intent;
        private boolean active;

        private SidebarItem(String name, int icon_id, Intent press_intent, boolean active) {
            this.name = name;
            this.icon_id = icon_id;
            this.press_intent = press_intent;
            this.active = active;
        }

        private String getName() {
            return name;
        }

        private int getIcon_id() {
            return icon_id;
        }

        private Intent getPress_intent() {
            return press_intent;
        }

        private boolean isActive() {
            return active;
        }
    }
}
