package com.pestades.samples;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.internal.ProfileKeyConstants;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.regions.Regions;

// This is a sample for enrypting and decrypting sensitive information using AWS Key Management Service and Encryption SDK.
// More info on https://aws.amazon.com/es/kms/ and https://docs.aws.amazon.com/aws-crypto-tools/index.html
public class AWSEncryptSample {

	// TODO: explore Master and Data Key usage
	// TODO: create screenshots from AWS console to create users, policies, and key
	// and paste on README
	public static void main(String[] args) {

		// Pipeline user; can encrypt and decrypt
		BasicAWSCredentials pipelineIdentity = getCredentials("pipelineCredentials");

		// BB starter user; can only decrypt
		BasicAWSCredentials bbStarterIdentity = getCredentials("bb_starterCredentials");

		// Hacker or unknown user which doesn't have access to master key (only
		// admin grants that); can't encrypt or decrypt
		BasicAWSCredentials hackerIdentity = getCredentials("hackerCredentials");

		final String awsRegion = getAWSRegion();

		// Symmetric key store in KMS service, located using an alias
		final String keyAlias = "alias/pipelineKey";

		// super secret password to encrypt and decrypt
		final String backEndPassword = "A secret password for accessing a database";
		final byte[] thePwd = backEndPassword.getBytes(StandardCharsets.UTF_8);

		// Instantiate the SDK client
		final AwsCrypto crypto = new AwsCrypto();

		// Create an encryption context.
		// Most encrypted data should have an associated encryption context
		// to protect integrity. This sample uses placeholder values.
		//
		// For more information see:
		// blogs.aws.amazon.com/security/post/Tx2LZ6WBJJANTNW/How-to-Protect-the-Integrity-of-Your-Encrypted-Data-by-Using-AWS-Key-Management
		final Map<String, String> encryptionContext = Collections.singletonMap("ExampleContextKey",
				"ExampleContextValue");

		// Instantiate a KMS master key provider for the pipeline Identity
		final KmsMasterKeyProvider masterKeyProviderForPipeline = KmsMasterKeyProvider.builder()
				.withCredentials(new AWSStaticCredentialsProvider(pipelineIdentity)).withDefaultRegion(awsRegion)
				.withKeysForEncryption(keyAlias).build();

		// Encrypt data by pipeline should be allowed
		System.out.println("*** The pipeline is trying to encrypt:");
		final byte[] theEncryptedPwd = encrypt(crypto, masterKeyProviderForPipeline, thePwd, encryptionContext);

		// Decrypt data by pipeline should be allowed
		System.out.println("*** The pipeline is trying to decrypt:");
		decrypt(crypto, masterKeyProviderForPipeline, theEncryptedPwd);

		// Instantiate a KMS master key provider for the BB starter
		final KmsMasterKeyProvider masterKeyProviderForBBStarted = KmsMasterKeyProvider.builder()
				.withCredentials(new AWSStaticCredentialsProvider(bbStarterIdentity)).withDefaultRegion(awsRegion)
				.withKeysForEncryption(keyAlias).build();

		// Encrypt data by the bb starter is not allowed
		System.out.println("*** A process which needs the password is trying to encrypt:");
		byte[] thisIsNullAsBBStartedCantEncrypt = encrypt(crypto, masterKeyProviderForBBStarted, theEncryptedPwd,
				encryptionContext);

		// Decrypt data by the bb starter should be allowed
		System.out.println("*** A process which needs the password is trying to decrypt:");
		decrypt(crypto, masterKeyProviderForBBStarted, theEncryptedPwd);

		// Instantiate a KMS master key provider for the hacker
		// The hacker doens't have access to the master key so cann't encrypt or decrypt
		final KmsMasterKeyProvider masterKeyProviderForHacker = KmsMasterKeyProvider.builder()
				.withCredentials(new AWSStaticCredentialsProvider(hackerIdentity))
				.withDefaultRegion(Regions.EU_WEST_3.getName().toString()).withKeysForEncryption(keyAlias).build();

		// Encrypt data by the hacker is not allowed
		System.out.println("The hacker is trying to encrypt:");
		byte[] thisIsNullAsHackerCantEncrypt = encrypt(crypto, masterKeyProviderForHacker, theEncryptedPwd,
				encryptionContext);

		// Decrypt data by the hacker is not allowed
		// Temporarily commented due circular reference execption, which breaks the
		// execution
//		System.out.println("*** The hacker is trying to decrypt:");
//		String thisIsNullAsHackerCantDecrypt = decrypt(crypto, masterKeyProviderForHacker, theEncryptedPwd);

		System.out.println("*** Ecryption/Decryption sample executed sucessfully!");

	}

	// Encrypt some data
	private static byte[] encrypt(AwsCrypto crypto, KmsMasterKeyProvider masterKeyProvider, byte[] clearText,
			Map<String, String> encryptionContext) {
		try {

			final CryptoResult<byte[], KmsMasterKey> encryptResult = crypto.encryptData(masterKeyProvider, clearText,
					encryptionContext);
			final byte[] ciphertext = encryptResult.getResult();
			final String encrypted = new String(Base64.getEncoder().encodeToString(ciphertext));
			System.out.println("Encrypted: " + encrypted);
			return ciphertext;

		} catch (AmazonServiceException ase) {
			System.out.println("Unable to encrypt for some reason:");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
			// TODO: avoid returing null
			return null;
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
			// TODO: avoid returing null
			return null;
		}

	}

	// Decrypt some data
	private static String decrypt(AwsCrypto crypto, KmsMasterKeyProvider masterKeyProvider, byte[] encryptedText) {
		final CryptoResult<byte[], KmsMasterKey> decryptResult = crypto.decryptData(masterKeyProvider, encryptedText);
		final String decrypted = new String(decryptResult.getResult(), StandardCharsets.UTF_8);
		System.out.println("Decrypted: " + decrypted);
		return decrypted;
	}

	// Build principal (user) credential from file.
	// Note: This is an example, a real implementation should follow AWS best
	// practices for managing access key, more info;
	// https://docs.aws.amazon.com/general/latest/gr/aws-access-keys-best-practices.html
	private static BasicAWSCredentials getCredentials(String fileName) {
		Properties prop = loadPropertiesFile(fileName);
		return new BasicAWSCredentials(prop.getProperty(ProfileKeyConstants.AWS_ACCESS_KEY_ID),
				prop.getProperty(ProfileKeyConstants.AWS_SECRET_ACCESS_KEY));
	}

	// Get the AWS region used to locate KMS keys. More info on
	// https://aws.amazon.com/about-aws/global-infrastructure/regions_az/
	private static String getAWSRegion() {
		Properties prop = loadPropertiesFile("awsRegion");
		return prop.getProperty(ProfileKeyConstants.REGION);
	}

	// Loads a properties file.
	private static Properties loadPropertiesFile(String theFile) {
		Properties prop = new Properties();
		InputStream is = null;
		final String propertiesFileName = theFile + ".properties";

		try {
			is = AWSEncryptSample.class.getClassLoader().getResourceAsStream(propertiesFileName);
			prop.load(is);

			return prop;

		} catch (Exception e) {
			System.out.println(
					theFile + " file not found. Please add it to your resorces folder or classpath and configure it.");
			System.exit(1);
			return null;
		}
	}

}
