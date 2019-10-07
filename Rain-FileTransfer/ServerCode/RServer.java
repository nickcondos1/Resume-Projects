package com.amazonaws.samples;

import java.io.IOException;
import java.net.ServerSocket;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class RServer
{
	public static void main(String[] args) throws IOException 
	{
		ServerSocket listener = new ServerSocket(9910);
		ProcessRequest request;
		int clientNumber = 1;
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
	    
	    try {
	        while (!listener.isClosed()) 
	        {
	        	request = new ProcessRequest(listener.accept(), s3, clientNumber);
	        	request.start();
	        	clientNumber++;
	        }
	    }
	    catch(Exception e)
	    {
	    	System.exit(0);
	    }
	    finally {
	        listener.close();
	        System.exit(0);
	        
	    }
	}
}

