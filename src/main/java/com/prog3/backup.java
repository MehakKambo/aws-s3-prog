//---------------------------------------------------------------------------
// Mehakpreet Kambo
// CSS 436 Section A
// --------------------------------------------------------------------------
// Backup.java: Client app which makes a backup to the cloud of the specified 
// directory to the specified “bucket” in either Azure of AWS
// --------------------------------------------------------------------------
// Note: S3 doesn't know "folders" and works with keys since it is no file 
// system. So, this program doesn't support backing up empty directories. 
// --------------------------------------------------------------------------

package com.prog3;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

public class backup 
{

	public static void main(String[] args) 
	{
		if (args.length != 2) 
		{
			System.out.println("Invalid number of Parameters. Try Again!");
			return;
		}
			
		final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
		backUp(s3Client, args[0], args[1]);
	}

	//------------------------------------------------------------------------
    // backUp:        carries out the backup algorithm   
    // Precondition:  S3 client exists, dirpath and bucket name is passed 
    // Postcondition: Files are backed up to the cloud from the local dir, a 
	//    			  bucket is created if it didn't existed, and no files are 
	//  			  backed up if they weren't modified. 
    //------------------------------------------------------------------------
	private static void backUp(AmazonS3 s3Client, String dirPath, String input) 
	{
		if (!input.contains("::")) 
		{
			System.out.println("Must enter in the form of \"bucket-name::directory-name\"!");
			return;
		}
		
		if(input.split("::")[0].isEmpty()) 
		{
			System.out.println("Please enter the name of the bucket!");
			return;
		}
		if(input.split("::")[1].isEmpty()) 
		{
			System.out.println("Please enter the name of the cloud dir!");
			return;
		}

		//check if input local dir exists
		File file = new File(dirPath);
		if (!file.exists()) 
		{
			System.out.println("Wrong Path or the directory doesn't exist. Try Again!");
			return;
		}
		
		String[] args = input.split("::");
		if(args.length < 2) 
		{
			System.out.println("args[2] need to have a bucket name and a dir name. Try Again!");
			return;
		}
		
		String bucket = args[0];
		String cloudDir = args[1];

		// if the bucket doesn't exist create one 
		if (!bucketExists(s3Client, bucket)) 
		{
			try {
				System.out.println("Creating new bucket: " + bucket);
				s3Client.createBucket(bucket);
			} catch (AmazonS3Exception e) {
				System.out.println(e.getErrorMessage());
				System.exit(1);
			}
		}

		if (notModified(s3Client, dirPath, bucket, cloudDir)) 
		{
			System.out.println("The local directory has not been modified.");
			System.out.println("So, no changes were made to the cloud dir!");
			return;
		}

		// upload from local to cloud bucket
		TransferManager transferManager = TransferManagerBuilder.standard().build();
		try {
			System.out.println("\nInput dir path: " + dirPath);
			
			File folder = new File(dirPath);
			if (!folder.exists()) 
			{
				System.out.println("Wrong Path or the directory doesn't exist. Try Again!");
				return;
			}

			System.out.println("Backing up in bucket \"" + bucket + "\" in dir \"" + cloudDir + "\"\n");
			
			//Upload the dir
			MultipleFileUpload upload = transferManager.uploadDirectory(bucket, cloudDir, folder, true);
			
			// print the name of the files being uploaded
			Collection<? extends Upload> uploads = new ArrayList<Upload>();
			uploads = upload.getSubTransfers();
			for (Upload u : uploads) 
			{
				System.out.println(u.getDescription());
			}

			upload.waitForCompletion();			
			System.out.println("\nBackup Completed!");
		} catch (AmazonServiceException e) {
			System.out.println(e.getErrorMessage());
			System.exit(1);
		} catch (AmazonClientException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		transferManager.shutdownNow();
	}

	//------------------------------------------------------------------------
	// bucketExists:	checks if the input bucket exists or not
	// Preconditions:	Valid S3 client is entered, a bucket name is entered
	// Postconditions:	Returns true if the bucket exists for that client, 
	//					false otherwise 
	//------------------------------------------------------------------------
	private static boolean bucketExists(AmazonS3 s3Client, String bucketName) 
	{
		List<Bucket> buckets = s3Client.listBuckets();
		for (Bucket b : buckets) 
		{
			if (b.getName().equals(bucketName)) 
			{
				return true;
			}
		}
		System.out.println(bucketName + " bucket doesn't exist");
		return false;
	}

	//------------------------------------------------------------------------
	// modified:	   Recursively compares the files on the local machine and 
	//  			   the cloud bucket to avoid backing up any duplicates.
	// Preconditions:  S3 client is valid, dir path , bucket and dir name for 
	//				   cloud is passed in
	// Postconditions: Returns true if the files on cloud were not modified 
	// 				   locally, false otherwise
	//------------------------------------------------------------------------ 
	public static boolean notModified(AmazonS3 s3Client, String localPath, String bucket, String cloudDir) 
	{
		File local = new File(localPath);
		File[] dirFiles = local.listFiles();

		List<File> dirList = new ArrayList<>();
		for (File fl : dirFiles) 
		{
			if (!fl.isDirectory()) 
			{
				String fileName = cloudDir + "/" + fl.getName();

				//file doesn't exist on cloud, modified
				if (!s3Client.doesObjectExist(bucket, fileName)) 
				{ 
					return false;
				} 
				else 
				{
					Date localLastModified = new Date(local.lastModified());
					Date cloudLastModified = s3Client.getObject(bucket, fileName).getObjectMetadata().getLastModified();
					
					// file has been modified locally, modified
					if (localLastModified.after(cloudLastModified)) 
					{ 
						return false;
					}
				}
			}
			//fl is a dir, add to the list 
			else 
			{
				dirList.add(fl);
			}
		}

		//exploring directories
		for (File fl : dirList) 
		{ 
			Path path = Paths.get(fl.getAbsolutePath());
			try (DirectoryStream<Path> dir = Files.newDirectoryStream(path)) {
				boolean dirEmpty = dir.iterator().hasNext(); 

				// if empty, backup empty object
				if (!dirEmpty) 
				{ 
					System.out.println("Uploading " + fl.getName() + " (empty)!!");
					s3Client.putObject(bucket, cloudDir + "/" + fl.getName() + "/", "");
				}
				
				//check if the files in the dir were modified
				boolean modified = notModified(s3Client, fl.getAbsolutePath(), bucket, cloudDir + "/" + fl.getName());
				if (!modified) 
				{
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}