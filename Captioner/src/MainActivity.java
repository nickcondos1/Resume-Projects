package pcap.project.pcap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener {

    public static int picNumber = 0;
    private static final int PERMISSION_REQUEST = 5;
    private static final int PERMISSION_REQUEST2 = 10;

    public static final int RESULT_GALLERY = 5;
    public static final int RESULT_EDITED = 6;
    private Bitmap bitmap;
    private String currentPath;
    private FloatingActionButton fab;
    private FloatingActionButton save;
    private Activity mainActivity;

    private PictureDraw currentPic;
    private Bitmap copy;

    private FloatingActionButton left;
    private FloatingActionButton right;
    private FloatingActionButton up;
    private FloatingActionButton down;
    private FloatingActionButton exitDraw;
    private LinkedList<String> captions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainActivity = MainActivity.this;
        captions = new LinkedList<>();
        parseTextFile("CaptionFile/SimpleCaptions");

        try {
            if (!getFileStreamPath("number").exists()) {
                FileOutputStream stream = openFileOutput("number", Context.MODE_PRIVATE);
                stream.write(String.valueOf(picNumber).getBytes());
            }
            else
            {
                FileInputStream stream = openFileInput("number");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                picNumber = Integer.parseInt(reader.readLine());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Intent received = getIntent();
        String action = received.getAction();
        String type = received.getType();

        if (received != null && Intent.ACTION_SEND.equals(action) && type != null)
        {   try {
                if (type.startsWith("image/")) {
                    Uri image = received.getParcelableExtra(Intent.EXTRA_STREAM);
                    InputStream imageStream = getContentResolver().openInputStream(image);
                    bitmap = BitmapFactory.decodeStream(imageStream);
                    ImageView imageView = findViewById(R.id.mainPicture);
                    imageView.setImageBitmap(bitmap);
                    imageStream.close();

                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Receiving picture failed", Toast.LENGTH_SHORT);
            }
        }

        currentPath = "";
        if (savedInstanceState != null)
        {
            currentPath = savedInstanceState.getString("currentPicture");
            buildPicture(currentPath);
        }

        save = findViewById(R.id.saveToGal);
        fab = findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        left = findViewById(R.id.leftFAB);
        right = findViewById(R.id.rightFAB);
        up = findViewById(R.id.upFAB);
        down = findViewById(R.id.downFAB);
        exitDraw = findViewById(R.id.exitFAB);
        left.hide();
        right.hide();
        up.hide();
        down.hide();
        exitDraw.hide();

        currentPic = findViewById(R.id.currentPicture);
        if (bitmap != null)
        {
            currentPic.setPicture(makeCopy(bitmap));
            currentPic.setCaptionPosition(bitmap.getWidth(), bitmap.getHeight());
            currentPic.invalidate();
        }
        else
        {
            ImageView imageView = findViewById(R.id.mainPicture);
            Bitmap temp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            currentPic.setPicture(makeCopy(temp));
            currentPic.setCaptionPosition(temp.getWidth(), temp.getHeight());
        }
        goBack();

        initializeListeners();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }

    }

    private void initializeListeners()
    {
        Button changeCaption = findViewById(R.id.changeCaption);
        changeCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText myText = findViewById(R.id.myEditText);
                currentPic.setCaption(myText.getText().toString());
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showPopup(view);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //numberToFile();
                try {
                    picNumber++;
                    copy = currentPic.getPicture();

                    String filename = "myPic" + MainActivity.picNumber;
                    FileOutputStream stream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    copy.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    stream.close();

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Saving picture failed in editor", Toast.LENGTH_SHORT).show();
                }

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Saving picture to your phone", Toast.LENGTH_LONG);
                    MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), currentPic.getPicture(), "myPicture", "03/23/2019");
                }
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                currentPic.moveLeft(event);
                return true;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                currentPic.moveRight(event);
                return true;
            }
        });
        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                currentPic.moveUp(event);
                return true;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                currentPic.moveDown(event);
                return true;
            }
        });
        exitDraw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fab.show();
                left.hide();
                right.hide();
                up.hide();
                down.hide();
                exitDraw.hide();
                save.show();
                return true;
            }
        });
    }

    private void numberToFile()
    {
        try {
            picNumber++;
            if (picNumber > 4)
                picNumber = 1;
            FileOutputStream stream = openFileOutput("number", Context.MODE_PRIVATE);
            stream.write(String.valueOf(picNumber).getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(), "Saving picture to your phone", Toast.LENGTH_LONG);
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "myPicture", "AddFunctionCallToGetDate");
            }
            else {
                Toast.makeText(this, "You need permission enabled to save a picture", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == PERMISSION_REQUEST2)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker, RESULT_GALLERY);
            }
            else
            {
                Toast.makeText(this,"You need permission enabled to open an image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera)
        {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST2);
            }
            else {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker, RESULT_GALLERY);
            }

        }
        else if (id == R.id.nav_gallery)
        {
            ArrayList<String> files = new ArrayList<>();
            for (int i = 0; i < fileList().length; i++)
            {
                files.add(0, fileList()[i]);
            }

            Intent gallery = new Intent(this, GalleryActivity.class);
            gallery.putExtra("fileList", files);
            startActivity(gallery);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_GALLERY && resultCode == RESULT_OK && data != null)
        {
            Uri image = data.getData();
            currentPath = image.toString();
            buildPicture(currentPath);
        }
        else if (requestCode == RESULT_EDITED && resultCode == RESULT_OK && data != null)
        {
            String filename = data.getStringExtra("myPic" + picNumber);
            try {
                FileInputStream is = this.openFileInput(filename);
                bitmap = BitmapFactory.decodeStream(is);
                is.close();

                ImageView imageView = findViewById(R.id.mainPicture);
                imageView.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Loading picture failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("currentPicture", currentPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null)
        {
            currentPath = savedInstanceState.getString("currentPicture");
            buildPicture(currentPath);
        }
    }

    private void buildPicture(String uri)
    {
        try {
            Uri image = Uri.parse(uri);
            String path = getRealPathFromURI(image);
            InputStream imageStream = getContentResolver().openInputStream(image);
            bitmap = BitmapFactory.decodeStream(imageStream);

            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6)
                matrix.postRotate(90);
            else if (orientation == 3)
                matrix.postRotate(180);
            else if (orientation == 8)
                matrix.postRotate(270);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);


            currentPic = findViewById(R.id.currentPicture);
            currentPic.setPicture(makeCopy(bitmap));
            currentPic.setCaptionPosition(bitmap.getWidth(), bitmap.getHeight());
            currentPic.invalidate();
            //ImageView imageView = findViewById(R.id.mainPicture);
            //imageView.setImageBitmap(bitmap);
            imageStream.close();
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
            Toast.makeText(this, "Something went wrong grabbing a picture", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap makeCopy(Bitmap bit)
    {
        return bit.copy(Bitmap.Config.ARGB_8888, true);
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void showPopup(View v)
    {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        if (item.getItemId() == R.id.changeSettings)
        {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.moveText)
        {
            fab.hide();
            left.show();
            right.show();
            up.show();
            down.show();
            exitDraw.show();
            save.hide();
            return true;
        }
        else if (item.getItemId() == R.id.randomCaption)
        {
            Random rand = new Random();
            int index = rand.nextInt(captions.size());
            currentPic.setCaption(captions.get(index));
        }
        return false;
    }

    private void goBack()
    {
        currentPic.setFinished(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String font = pref.getString("font_key", "fonts/spacemeatball.otf");
        String color = pref.getString("fill_color", "WHITE");
        String borderColor = pref.getString("border_color", "BLACK");
        String textSize = pref.getString("text_Size", "100");
        boolean borderOnOff = pref.getBoolean("border_onOff", true);
        String borderSize = pref.getString("border_size", "7");

        if (currentPic != null)
            currentPic.setSettings(font, color, borderColor, textSize, borderOnOff, borderSize);

    }

    private void parseTextFile(String filename)
    {
        try
        {
            AssetManager manager = getResources().getAssets();
            Scanner scanner = new Scanner(manager.open(filename));

            while (scanner.hasNextLine())
            {
                captions.addLast(scanner.nextLine());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
