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
