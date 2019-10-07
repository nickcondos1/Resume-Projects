package cc.rain.com.rain;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class ClientConnect extends Thread
{

    public static boolean CLIENT_INIT = false;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private Context context;
    private static boolean killClient;
    private LinkedList<RainFileObject> bucketList;
    private boolean downloadFile;
    private String bucketName;
    private String fileName;
    private boolean uploadfile;
    private Uri uri;
    private String uploadFile;
    private String bucketUpload;

    public ClientConnect(Context context)
    {
        this.context = context;
        this.killClient = false;
        this.bucketList = new LinkedList<>();
        this.downloadFile = false;
        this.bucketName = "";
        this.fileName = "";
        this.uploadfile = false;
        this.uploadFile = "";
        this.uri = null;
        this.bucketUpload = "";
    }

    public void run()
    {
        try {
            connectToServer();
            initializeBucketInfo();

            while (!killClient)
            {
                String line = "";
                if (downloadFile)
                {
                    doDownload();
                }

                if (uploadfile)
                {
                    out.println("upload file");
                    out.println(this.uploadFile);
                    out.println(this.bucketUpload);
                    sendFile(this.uri);
                    uploadfile = false;
                }
                try {
                    line = in.readLine();
                    out.println("waiting");

                    if (line.equals("downloading")) {
                        createFile(socket.getInputStream(), fileName);
                    } else
                        continue;
                }
                catch (Exception e)
                {
					Log.e("Error", e.getMessage() + "");
                    continue;
                }
            }
            out.println("exit");

        } catch (IOException e) {
            Log.e("Error", e.getMessage() + "");
        }
    }

    public void setUpload(Uri uri, String fileName, String bucket)
    {
        this.uploadFile = fileName;
        this.bucketUpload = bucket;
        this.uri = uri;
        this.uploadfile = true;
    }

    //Downloading
    private void createFile(InputStream source, String fileName) throws IOException
    {
        InputStream input = null;
        OutputStream output = null;

        File file = new File(context.getExternalFilesDir(null), fileName);

            input = source;
            output = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int bytesRead;
            int k = 0;
            Boolean isDone = false;

            String s = "";

            try {

            while ((bytesRead = input.read(buf)) > 0)
            {
                int i;
                int j = 0;
                for (i = 0; i < bytesRead; i++)
                {
                    try {
                        if (buf[i] == 60 && buf[i+1] == 68 && buf[i+2] == 79 && buf[i+3] == 78 && buf[i+4] == 69 && buf[i+5] == 62)
                        {
                            isDone = true;
                            j = i;
                            break;
                        }
                    }
                    catch(Exception e)
                    {
                        System.out.println("Failed somewhere");
                    }
                }
                if (!isDone)
                {
                    output.write(buf, 0, bytesRead);

                }
                else
                {
                    output.write(buf, 0, j);
                    break;
                }
            }
        }
        catch(Exception e)
        {
            Log.e("DOWNLOAD ERROR", e.getMessage());
        }
        finally {
            output.close();
        }
    }

    public void setDownloadFile(String bucketName, String fileName)
    {
        this.bucketName = bucketName;
        this.fileName = fileName;
        this.downloadFile = true;
    }

    private void doDownload()
    {
        out.println("download file");
        out.println(bucketName);
        out.println(fileName);
        this.downloadFile = false;
    }

    private void initializeBucketInfo() throws IOException
    {
        String line = "";
        int k = 0;
        while (!(line = in.readLine()).equals("DONE"))
        {
            Log.e("BucketName: ", line);
            this.bucketList.add(new RainFileObject(line));
            while (!(line = in.readLine()).equals("NEXTBUCKET"))
            {
                Log.e("Bucketfile: ", line);
                this.bucketList.get(k).addFile(line);
            }
            k++;
        }
        CLIENT_INIT = true;
    }

    public static void killClient()
    {
        killClient = true;
    }

    public LinkedList<RainFileObject> getBucketList() {
        return bucketList;
    }

    private void sendFile(Uri uri) throws IOException
    {
        InputStream input = context.getContentResolver().openInputStream(uri);
        OutputStream output = null;

        try {
            output = socket.getOutputStream();
            byte[] buf = new byte[4096];
            int bytesRead;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while ((bytesRead = input.read(buf)) > 0)
            {
                Log.e("UPLOAD: ", Arrays.toString(buf));
                output.write(buf, 0, bytesRead);
            }

            String done = "<DONE>";
            Log.e("UPLOAD", Arrays.toString(done.getBytes()));
            output.write(done.getBytes());
        } finally {
            input.close();
        }
    }

    public void connectToServer() throws IOException
	{
        String serverAddress = "10.104.168.73";

        // Make connection and initialize streams
        socket = new Socket(serverAddress, 9910);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
}
