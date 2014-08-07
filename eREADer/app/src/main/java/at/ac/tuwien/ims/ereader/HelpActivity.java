package at.ac.tuwien.ims.ereader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import net.simonvt.menudrawer.MenuDrawer;

import at.ac.tuwien.ims.ereader.Util.SidebarMenu;

/**
 * Created by Flo on 07.08.2014.
 */
public class HelpActivity extends Activity {
    private ImageButton menuBtn;
    private SidebarMenu sbMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        if (getActionBar() != null)
            getActionBar().hide();

        menuBtn=(ImageButton)findViewById(R.id.optnbtn_help);
        menuBtn.setOnClickListener(btnListener);

        sbMenu=new SidebarMenu(this, false, false, true);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v==menuBtn) {
                if(sbMenu.getMenuDrawer().isMenuVisible())
                    sbMenu.getMenuDrawer().closeMenu();
                else
                    sbMenu.getMenuDrawer().openMenu();
            }
        }
    };

    @Override
    public void onBackPressed() {
        final int drawerState = sbMenu.getMenuDrawer().getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            sbMenu.getMenuDrawer().closeMenu();
            return;
        }
        super.onBackPressed();
    }
}
