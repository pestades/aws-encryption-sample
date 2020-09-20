# Sample Project to showcase Encryption using Amazon Web Services KMS and Encryption SDK

A simple Java application illustrating how you can use AWS KMS and AWS Encryption SDK for Java to encrypt and decrypt sensitive information (such as passwords), relying on AWS KSM to store the master and data key and the AWS Encryption SDK to encrypt sensitive data at client side using those keys.

3 different scenarios are used to showcase how you can use AWS security policies to control who can access to the sensitive information:
- A CI/CD pipeline user which is expected to encrypt and decrypt a password.
- An script or system user which needs to decryp the password but can't encrypt.
- A hacker or unknown user which is not able to encrypt or decrypt.


## Requirements

You have a choice of Maven or Gradle

The only requirement of this application is Maven. All other dependencies can
be installed by building the maven package:
    
    mvn package

Otherwise with gradle you can run the gradlew wrapper and all dependencies will be installed:

    gradlew build

## Basic Configuration

You need to set up your AWS security credentials before the sample code is able
to connect to AWS. You can do this by creating a file named "credentials" at ~/.aws/ 
(C:\Users\USER_NAME\.aws\ for Windows users) and saving the following lines in the file:

    [default]
    aws_access_key_id = <your access key id>
    aws_secret_access_key = <your secret key>

See the [Security Credentials](http://aws.amazon.com/security-credentials) page
for more information on getting your keys.

## Running the AWS Encryption sample

### Prerequisites
You will need to go to [IAM policies page](https://console.aws.amazon.com/iam/home?#policies), search for the String "S3,"
and "Attach" the "AmazonS3FullAccess" policy to the user whose credentials exist in 
your `~/.aws/credentials` file. Otherwise, you will likely get a `AmazonServiceException`/`Access Denied`/`403` error.

This sample application connects to Amazon's [Simple Storage Service (S3)](http://aws.amazon.com/s3),
creates a bucket, and uploads a file to that bucket. The code will generate a
bucket name for you, as well as an example file to upload. All you need to do
is run it.

Maven:

    mvn clean compile exec:java

Gradle:

    gradlew clean build run


When you start making your own buckets, the S3 documentation provides a good overview
of the [restrictions for bucket names](http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html).

## License

This sample application is distributed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

