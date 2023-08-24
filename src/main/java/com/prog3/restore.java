//---------------------------------------------------------------------------
// Mehakpreet Kambo
// CSS 436 Section A
// --------------------------------------------------------------------------
// Restore.java: Client app which restores from the specified bucket-name in 
// the cloud to the specified directory. 
// --------------------------------------------------------------------------
// Note: The AWS SDK Java haven't implemented the feature getSubTransfers 
//		 for MultipleFileDownload which would have allowed to print the names 
//       of the files being restored. So, I am unable to print the names.
//--------------------------------------------------------------------------

package com.prog3;
import java.io.File;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;


public class restore 
{
	public static void main(String[] args) 
	{
		if (args.length != 2) 
		{
			System.out.println("Invalid number of Parameters. Try Again!");
			return;
		}

		final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
		restoreDir(s3Client, args[0], args[1]);
	}

	//------------------------------------------------------------------------
    // restoreDir:    Carries out the restoring of the dir from cloud to local
	//				  directory  
    // Precondition:  S3 client exists, dirpath and bucket name is passed 
    // Postcondition: Files are restored from cloud to local dir, a local dir
	//    			  is created if it doesn't exist 
    //------------------------------------------------------------------------
	private static void restoreDir(AmazonS3 s3, String input, String dirPath) 
	{
		if (!input.contains("::")) 
		{
			System.out.println("Must enter in the form of \"bucket-name::directory-name\"");
			return;
		}

		if (input.split("::")[0].isEmpty()) 
		{
			System.out.println("Please enter the name of the bucket!");
			return;
		}
		if (input.split("::")[1].isEmpty()) 
		{
			System.out.println("Please enter the name of the cloud dir!");
			return;
		}

		String[] args = input.split("::");

		if (args.length < 2) 
		{
			System.out.println("args[2] need to have a bucket name and a dir name. Try Again!");
			return;
		}

		String bucket = args[0];
		String cloudDir = args[1];

		if(!bucketExists(s3, bucket))
		{
			System.out.println("The Specified Bucket doesn't exist. Try Again with a valid bucket!");
			return;
		}

		if(!cloudDirExists(cloudDir, bucket, s3))
		{
			System.out.println(cloudDir + " doesn't exist in cloud. Try Again with a valid one!");
			return;
		}

		// if the local dir doesnt exist, create one
		System.out.println("\nInput dir path: " + dirPath);

		File folder = new File(dirPath);
		if (!folder.exists()) 
		{
			System.out.println("The local dir doesn't exist at that path!");
			if (folder.mkdir()) 
			{
				System.out.println("Creating new directory at: " + dirPath);
			}
		}

		TransferManager transferManager = TransferManagerBuilder.standard().build();

		try {
			System.out.println("Restoring in local dir " + folder + " from cloud dir " + cloudDir);

			MultipleFileDownload download = transferManager.downloadDirectory(bucket, cloudDir + "/", folder);
			System.out.println("Downloading from Bucket: " + download.getBucketName());
			System.out.println("Downloading dir: " + download.getKeyPrefix().toString());
			System.out.println("Restore Status: " + download.getDescription());
			download.isDone();
			download.waitForCompletion();
			System.out.println("\nRestore Completed!");
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		} catch (AmazonClientException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
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
		return false;
	}

	//------------------------------------------------------------------------
	// cloudDirExists:	checks if the input cloud dir exists or not
	// Preconditions:	directory name is entered
	// Postconditions:	Returns true if the dir exists, false otherwise 
	//------------------------------------------------------------------------
	private static boolean cloudDirExists(String cloudDir, String bucket, AmazonS3 s3Client) 
	{
    	ListObjectsV2Result result = s3Client.listObjectsV2(bucket, cloudDir);
    	return result.getKeyCount() > 0;	
	}
}