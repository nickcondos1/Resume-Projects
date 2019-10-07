package pcap.project.pcap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class MyListAdapter extends BaseExpandableListAdapter implements View.OnClickListener
{
    private Activity activity;
    private LinkedList<Bitmap> images;
    private ArrayList<String> files;

    public MyListAdapter(Activity activity, ArrayList<String> files)
    {
        this.activity = activity;
        images = new LinkedList<>();
        this.files = files;

        for (int i = 0; i < files.size(); i++)
        {
            if (!files.get(i).equals("number"))
            {
                try
                {
                    FileInputStream is = this.activity.openFileInput(files.get(i));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    images.addLast(bitmap);
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "Something went wrong loading the gallery", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                this.files.remove(i);
            }
        }
    }

    @Override
    public int getGroupCount() {
        return images.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //return images.get(groupPosition).numberOfModels();
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return images.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        //return images.get(groupPosition).getModel(childPosition);
        return "Testing";
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
        ImageView pic = convertView.findViewById(R.id.groupPic);
        pic.setImageBitmap(images.get(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.child_layout, null);
        }

        TextView delete = convertView.findViewById(R.id.childText);
        delete.setTag(R.id.group_num, groupPosition);
        delete.setTag(R.id.posn_num, childPosition);
        delete.setOnClickListener(this);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onClick(View v) {
        View view = activity.findViewById(R.id.linearView);

        final int group = (int) v.getTag(R.id.group_num);
        final int child = (int) v.getTag(R.id.posn_num);

        Snackbar snackbar = Snackbar.make(view, "Do you want to delete?", Snackbar.LENGTH_LONG)
                .setAction("Delete", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        images.remove(group);
                        activity.deleteFile(files.get(group));
                        notifyDataSetChanged();
                    }
                });
        snackbar.show();

    }
}
