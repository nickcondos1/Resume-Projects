package cc.rain.com.rain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

public class RainFileObject implements Serializable
{
    private String bucketName;
    private LinkedList<String> files;

    public RainFileObject(String bucketName)
    {
        this.bucketName = bucketName;
        this.files = new LinkedList<>();
    }

    public void addFile(String file)
    {
        this.files.add(file);
    }

    public String getBucketName()
    {
        return this.bucketName;
    }

    public LinkedList<String> getFiles()
    {
        return files;
    }

    public int getFileCount()
    {
        return this.files.size();
    }


}
