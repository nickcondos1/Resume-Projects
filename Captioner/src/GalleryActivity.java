package pcap.project.pcap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class GalleryActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (getIntent()!= null)
        {
            ArrayList<String> files = (ArrayList<String>)getIntent().getSerializableExtra("fileList");


            ExpandableListView expandList = findViewById(R.id.expandList);
            MyListAdapter listAdapter = new MyListAdapter(this, files);
            expandList.setAdapter(listAdapter);
        }
    }
}
