# Cloud Backup and Restore Program using Java and AWS S3

This Java program allows you to recursively traverse a local directory, create backups to AWS S3, and restore data from S3 to a local directory while preserving the original directory structure.

## Prerequisites

- You need to have an AWS account with access to an S3 bucket for storing backup data.
- Make sure you have the AWS SDK for Java installed and configured on your machine with proper credentials.

## Usage

### Backup Operation

To create a backup to AWS S3:

```bash
java -jar CloudBackupRestore.jar backup local-directory-name bucket-name::directory-name
```
- `local-directory-name`: The path to the local directory you want to back up.
- `bucket-name::directory-name`: The target bucket and directory path in the format
- `bucket-name::directory-name`. If the bucket doesn't exist, it will be created.


### Restore Operation

To restore data from AWS S3:

```bash
java -jar CloudBackupRestore.jar restore bucket-name::directory-name local-directory-name
```
- `bucket-name::directory-name`: The name of the bucket and directory you want to restore from.
- `local-directory-name`: The path to the local directory where you want to restore the data.

### Notes

- This program utilizes the AWS SDK for Java to interact with AWS services.
- During backup, the original directory structure is maintained in the S3 bucket.
- During restore, the original directory structure is recreated in the local directory.

### Disclaimer
This program is provided as-is and may have limitations or security considerations. Make sure to review and adapt the code according to your specific requirements and security best practices.

### License
This project is licensed under the `MIT License`.

## Important
Please make sure to replace placeholders such as `local-directory-name`, `bucket-name`, and `CloudBackupRestore.jar` with actual values and filenames according to your project setup. Also, ensure that the AWS SDK for Java is properly configured and accessible on your machine.
