package cc.rain.com.rain;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ClientConnect cc;
    private Uri myUri;
    private String fileName;
    private MyListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        cc = new ClientConnect(this.getApplicationContext());
        cc.start();

        while (!ClientConnect.CLIENT_INIT) {
            //Waiting for information from AWS to be loaded
        }
        ExpandableListView expandList = findViewById(R.id.expandList);
        listAdapter = new MyListAdapter(this, cc.getBucketList(), cc);
        expandList.setAdapter(listAdapter);

        Intent received = getIntent();
        String action = received.getAction();
        String type = received.getType();

        if (received != null && Intent.ACTION_SEND.equals(action) && type != null)
        {   try {
            if (type.startsWith("image/") || type.startsWith("audio/"))
            {
                myUri = received.getParcelableExtra(Intent.EXTRA_STREAM);
                fileName = getFileName(myUri);
                TextView txt = findViewById(R.id.fileToUpload);
                txt.setText(fileName);
                Log.e("UPLOAD", fileName);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Receiving picture failed", Toast.LENGTH_SHORT);
        }
        }

        initializeUploadListener();
    }

    private void initializeUploadListener()
    {
        ImageView view = findViewById(R.id.uploadButton);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                TextView bucket = findViewById(R.id.LocationFile);
                TextView file = findViewById(R.id.fileToUpload);
                if (!bucket.getText().equals("Select Folder") && !file.getText().equals("Upload File"))
                {
                    cc.setUpload(myUri, fileName, bucket.getText().toString());
                    int index = getIndexOfBucket(bucket.getText().toString());
                    cc.getBucketList().get(index).addFile(fileName);
                    listAdapter.notifyDataSetChanged();
                }

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /*check whether you're working on correct request using requestCode , In this case 1*/

        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            this.myUri = data.getData(); //declared above Uri audio;
            TextView txt = findViewById(R.id.fileToUpload);
            txt.setText(getFileName(myUri));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ClientConnect.killClient();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ClientConnect.killClient();
    }

    private int getIndexOfBucket(String bucket)
    {
        int i = 0;
        for (RainFileObject o : cc.getBucketList())
        {
            if (o.getBucketName().equals(bucket))
                return i;

            i++;
        }
        return i;
    }
}
