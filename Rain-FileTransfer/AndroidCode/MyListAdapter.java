package cc.rain.com.rain;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class MyListAdapter extends BaseExpandableListAdapter implements View.OnClickListener
{
    private Activity activity;
    private LinkedList<RainFileObject> list;
    private ClientConnect cc;
    private int groupNum;
    private int childNum;


    public MyListAdapter(Activity activity, LinkedList<RainFileObject> list, ClientConnect cc)
    {
        this.activity = activity;
        this.list = list;
        this.cc = cc;
    }

    @Override
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.list.get(groupPosition).getFileCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.list.get(groupPosition).getFiles().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {

            convertView = activity.getLayoutInflater().inflate(R.layout.group_layout, null);
        }
        TextView textView = convertView.findViewById(R.id.bucketName);
        textView.setText(this.list.get(groupPosition).getBucketName());

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.child_layout, null);
        }

        this.groupNum = groupPosition;
        this.childNum = childPosition;

        TextView bucketSelected = activity.findViewById(R.id.LocationFile);
        bucketSelected.setText(list.get(groupPosition).getBucketName());

        TextView childFile = convertView.findViewById(R.id.childFile);
        childFile.setText(list.get(groupPosition).getFiles().get(childPosition));

        TextView delete = convertView.findViewById(R.id.deleteChild);
        delete.setTag(R.id.group_num, groupPosition);
        delete.setTag(R.id.posn_num, childPosition);
        delete.setTag(R.id.action, "delete");
        delete.setOnClickListener(this);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onClick(View v)
    {
        View view = activity.findViewById(R.id.deleteChild);
        View view2 = activity.findViewById(R.id.childFile);


        final int group = (int) v.getTag(R.id.group_num);
        final int child = (int) v.getTag(R.id.posn_num);

        if (v.getId() == view.getId())
        {
            Snackbar snackbar = Snackbar.make(view, "Do you want to Download?", Snackbar.LENGTH_LONG)
                    .setAction("Download", new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Toast.makeText(activity.getApplicationContext(), "Downloading...", Toast.LENGTH_LONG).show();
                            cc.setDownloadFile(list.get(group).getBucketName(), list.get(group).getFiles().get(child));
                            //list.get(group).getFiles().remove(child);
                            //notifyDataSetChanged();
                        }
                    });
            snackbar.show();
        }

    }

    public void notifyDataUpdated()
    {
        notifyDataSetChanged();
    }
}
