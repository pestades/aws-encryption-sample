# Sample Project to showcase Encryption using Amazon Web Services KMS and Encryption SDK

A simple Java application illustrating how you can use AWS KMS and AWS Encryption SDK for Java to encrypt and decrypt sensitive information (such as passwords), relying on AWS KSM to store the master and data key and the AWS Encryption SDK to encrypt sensitive data at client side using those keys.

3 different scenarios are used to showcase how you can use AWS security policies to control who can access to the sensitive information:
- A CI/CD pipeline user which is expected to encrypt and decrypt a password using the KMS key.
- An script or system user which needs to decryp the password but can't encrypt.
- A hacker or unknown user which is not able to encrypt or decrypt.


## License

This sample application is distributed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Pre - Requisites

AWS prior knowledge is not really needed, as you will have detailed steps to created all required resources, but if would be great if you take a look into this brief introduction:

* [What is Key Management Service?](https://docs.aws.amazon.com/kms/latest/developerguide/overview.html)
* [Getting Started with AWS KMS](https://docs.aws.amazon.com/kms/latest/developerguide/getting-started.html)

In order to set up the working environment you need the following:

* An AWS account (free tier is valid and no extra cost are required)
* An user with enough permissions to generate policies and create/modify roles in IAM. (I used and user belonging to Administrators group)
* Maven for building the sample.

```
    mvn install
```


## Environment set up

The sample requires creating 3 users, 1 KMS key and 1 Policy.

### Create the IAM users


Detailed info about creating an IAM user in your AWS account [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_users_create.html).

Sign in to the AWS Management Console and open the [IAM Console](https://console.aws.amazon.com/iam/home#/users)

1. Add user
2. Enter "pipeline" as user name and enable Programmatic access and click next:

![Add pipeline user and enable Programmatic access](/res/CreateUser1.png)

3. No permissions are needed, click next
4. No tags are needed, click next
5. Ignore the "user has no permissions" warning message and create user

![Create user](/res/CreateUser2.png)

6. Download .cvs credentials file and store it as we'll needed it later

![Download .csv credentials](/res/CreateUser3.png)

Repeat the process and create the users "bb_starter" and "hacker".

![3 users created](/res/CreateUser4.png)


### Create the KSM key

Detailed info about creating keys [here](https://docs.aws.amazon.com/kms/latest/developerguide/create-keys.html).

1. Open the [KMS service Console](https://console.aws.amazon.com/kms), select the region of your choice and click on create key
2. For this sample a Symmetric key, click next
3. Enter "pipelineKey" as alias, the description and click next

![pipelineKey alias and description](/res/CreateKMSKey3.png)

4. No key administrator needed, click next
5. Select the pipeline user and click next

![Select pipeline user for the pipelineKey key](/res/CreateKMSKey4.png)

6. Click finish to create the key
7. Open the pipeline key and copy to clipboard the ARN to locate the key

![Copy key ARN to clipboard](/res/CreateKMSKey5.png)


### Create the IAM Policy

Detailed info about creating keys [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_create.html). 

1. Open the policies on the [IAM Policy Console](https://console.aws.amazon.com/iam/home#/policies) and click on create policy
2. Select the KMS Service, expand Write actions and select Decrypt and expand resources, add ARN, list ARNs manually and paste the ARN key, click on review policy  

![Policy configuration](/res/CreatePolicyKMS2.png)

3. Name the police "KMSPipelineKeyDecrypt", include a description and click create policy

![Policy name and description](/res/CreatePolicyKMS3.png)

### Add the policy to the bb_starter user to limit the permissions on the pipeline key.

1. Navigate to users and click on bb_starter user.
2. Click on "Add Permissions" button
3. Click on "Attach existing policies directly" button and introduce "KMSPipelineKeyDecrypt" in Filter policies text box
4. Select the policy and click review

![User bb_starter with the policy](/res/AddPolicyToUser1.png)

5. Click on "Add permissions" button.

### Configure the AWS Users credentials and region

1. Clone the sample project in case you didn't already.
2. Edit the file /src/main/resources/awsRegion.properties to set the region in which the KMS key was created
3. Edit the files pipelineCredentials.properties, bb_starterCredentials.properties and hackerCredentials.properties located on /src/main/resources/ and the user credentials from the CSV file
4. Build and run. This is a sample output:

```
mvn clean compile exec:java
```

```
*** The pipeline is trying to encrypt:
Encrypted: AYADeCE441G66ehGs8NrQuhk40IAhw.....
*** The pipeline is trying to decrypt:
Decrypted: A secret password for accessing a database
*** A process which needs the password is trying to encrypt:
Unable to encrypt for some reason:
Error Message:    User: arn:aws:iam::xxx:user/bb_starter is not authorized to perform: kms:GenerateDataKey on resource: ... (Service: AWSKMS; Status Code: 400; Error Code: AccessDeniedException; Request ID: 202293fe-1ae1-4baf-99d0-47f3f95b04b7; Proxy: null)
HTTP Status Code: 400
AWS Error Code:   AccessDeniedException
Error Type:       Client
Request ID:       202293fe-1ae1-4baf-99d0-47f3f95b04b7
*** A process which needs the password is trying to decrypt:
Decrypted: A secret password for accessing a database
The hacker is trying to encrypt:
Unable to encrypt for some reason:
Error Message:    User: arn:aws:iam::xxx:user/hacker is not authorized to perform: kms:GenerateDataKey on resource: ... (Service: AWSKMS; Status Code: 400; Error Code: AccessDeniedException; Request ID: 03b05b0d-45ae-4e35-b2e9-3052d8fa203f; Proxy: null)
HTTP Status Code: 400
AWS Error Code:   AccessDeniedException
Error Type:       Client
Request ID:       03b05b0d-45ae-4e35-b2e9-3052d8fa203f
*** Ecryption/Decryption sample executed sucessfully!

```

