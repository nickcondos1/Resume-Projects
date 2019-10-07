package com.amazonaws.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import javafx.util.Pair;

import com.amazonaws.services.s3.model.S3Object;

public class ProcessRequest extends Thread
{
	private Socket socket;
	private int clientNumber;
	private AmazonS3 s3;
	private BufferedReader in;
	private PrintWriter out;
	
	public ProcessRequest(Socket socket, AmazonS3 s3, int clientNumber)
	{
		this.socket = socket;
		this.clientNumber = clientNumber;
		this.s3 = s3;
	}
	
	public void run()
	{	
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			
			initializeclientInfo();
			
			while (true)
			{
				out.println("polling");
				String line;
				try {
					line = in.readLine();
					if (line.equals("upload file"))
					{	
						String fileName = in.readLine();
						String bucketName = in.readLine();
						System.out.println(fileName);
						File file = createFile(socket.getInputStream());
						s3.putObject(new PutObjectRequest(bucketName, fileName, file));
						file.deleteOnExit();
						System.out.println("Uploaded Successfully!  ClientNumber: " + this.clientNumber);
						break;
					}	
					else if (line.equals("download file"))
					{
						String bucketName = in.readLine();
						String fileName = in.readLine();
						out.println("downloading");		
						sendFileToClient(bucketName, fileName);
												
						System.out.println("Downloaded Successfuly!  ClientNumber: " + this.clientNumber);
					}
					else if (line.equals("exit"))
					{
						this.socket.close();
					}
					else
						continue;
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					this.socket.close();
					break;
				}					
			}
			


		} 
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	
	private void copyFile(InputStream source, File dest)
    		throws IOException {
	    	InputStream input = null;
	    	OutputStream output = null;
	    	try {
	    		input = source;
	    		output = new FileOutputStream(dest);
	    		byte[] buf = new byte[4096];
	    		int bytesRead;
	    		while ((bytesRead = input.read(buf)) > 0) {
	    			output.write(buf, 0, bytesRead);
	    		}
	    	} finally {
	    		input.close();
	    		output.close();
	    	}
    }
	
	private void sendFileToClient(String bucketName, String fileName)
	{
		S3Object object = s3.getObject(new GetObjectRequest(bucketName, fileName));
		
		FileInputStream f = null;
		File file = null;
		int bytesRead = 0;
		byte[] buf = new byte[4096];
		
		try {
			copyFile(object.getObjectContent(), file);
			f = new FileInputStream(file);
			
			int k = 0;
			
			while ((bytesRead = f.read(buf)) > 0) 
			{
				k += bytesRead;
				socket.getOutputStream().write(buf,0,bytesRead);
				System.out.println(Arrays.toString(buf));	
				
			}
			System.out.println("TOTAL BYTES READ: " + k);
		
			String done = "<DONE>";
			System.out.println(Arrays.toString(done.getBytes()));
			socket.getOutputStream().write(done.getBytes());
						
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
				
		file.deleteOnExit();
	}
	
	private void initializeclientInfo()
	{
		for (Bucket bucket : s3.listBuckets()) 
        {
        	out.println(bucket.getName());
        	ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucket.getName()));
        	        	
        	for (S3ObjectSummary os : objectListing.getObjectSummaries())
        	{
        		out.println(os.getKey());
        	}    	
        	out.println("NEXTBUCKET");
        }
		out.println("DONE");
	}
	
	private File createFile(InputStream source) throws IOException
	{		
	    	InputStream input = null;
	    	OutputStream output = null;
	    	File file = File.createTempFile("temppre", "tempsuf");
	    	try {
	    		input = source;
	    		output = new FileOutputStream(file);
	    		byte[] buf = new byte[4096];
	    		int bytesRead;
	    		int k = 0;
	    		Boolean isDone = false;
	    		
	    		while ((bytesRead = input.read(buf)) > 0)
	    		{
	    			System.out.println(Arrays.toString(buf));
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
	    					System.out.println("Failed");
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
	    	} finally {
	    		output.close();
	    	}
	    	System.out.println("Returning file  ClientNumber: " + this.clientNumber);
	    	return file;
    }
}
