package org.fict.ficttools;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.fict.ficttools.Selection.Attendance;
import org.fict.ficttools.Selection.Members;
import org.fict.ficttools.Selection.ToolsActivity;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ImageView splashlogo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        splashlogo = findViewById(R.id.splashlogo_iv);
        Animation faded = AnimationUtils.loadAnimation(this,R.anim.splashtransition);
        splashlogo.startAnimation(faded);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_event) {
            Intent go = new Intent(NavigationActivity.this, Attendance.class);
            startActivity(go);
        } else if (id == R.id.nav_members) {
            Intent go = new Intent(NavigationActivity.this, Members.class);
            startActivity(go);
        } else if (id == R.id.nav_tools) {
            Intent go = new Intent(NavigationActivity.this, ToolsActivity.class);
            startActivity(go);
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "See you around, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName()+"!", Toast.LENGTH_LONG).show();
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent go = new Intent(NavigationActivity.this, SplashScreen.class);
                            go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(go);
                        }
                    });
        }
        else if (id == R.id.nav_generate) {
            Intent go = new Intent(NavigationActivity.this, generateActivity.class);
            startActivity(go);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
