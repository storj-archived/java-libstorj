/*
 * Copyright (C) 2017-2018 Kaloyan Raev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.storj.libstorj;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Java object wrapper of the libstorj native library.
 */
public class Storj {

    /**
     * Error code for "No such file or directory" error that occured on the local
     * file system.
     * 
     * <p>
     * This error code is usually returned on upload and download operations. For
     * example, if the file to be uploaded does not exist, or the directory to save
     * the downloaded file does not exist.
     * </p>
     */
    public static final int ENOENT = -2;

    /**
     * Error code for "Permission denied" error that occured on the local file
     * system.
     * 
     * <p>
     * This error code is usually returned on upload and download operations. For
     * example, if the file to be uploaded cannot be accessed due to insufficient
     * permissions, or there are not enough permissions to save the downloaded file
     * to the selected directory.
     * </p>
     */
    public static final int EACCES = -13;

    /**
     * Error code if there is no error.
     */
    public static final int NO_ERROR = 0;

    /**
     * Error code if the Bridge URL uses protocol that is not supported.
     */
    public static final int CURLE_UNSUPPORTED_PROTOCOL = 1;

    /**
     * Error code if the Bridge URL is not properly formatted.
     */
    public static final int CURLE_URL_MALFORMAT = 3;

    /**
     * Error code if the given proxy host could not be resolved.
     */
    public static final int CURLE_COULDNT_RESOLVE_PROXY = 5;

    /**
     * Error code if the Bridge host could not be resolved.
     */
    public static final int CURLE_COULDNT_RESOLVE_HOST = 6;

    /**
     * Error code if failed to connect to the Bridge server or to the proxy.
     */
    public static final int CURLE_COULDNT_CONNECT = 7;

    /**
     * Error code if a memory allocation request failed in the libcurl native
     * library.
     */
    public static final int CURLE_OUT_OF_MEMORY = 27;

    /**
     * Error code if a network operation timed out.
     */
    public static final int CURLE_OPERATION_TIMEDOUT = 28;
    
    /**
     * Error code if an invalid request was sent to the Bridge.
     * 
     * <p>
     * For example, if a malformed bucket id or file id is provided in the request.
     * </p>
     */
    public static final int HTTP_BAD_REQUEST = 400;

    /**
     * Error code if the Bridge was requested with invalid credentials.
     */
    public static final int HTTP_UNAUTHORIZED = 401;

    /**
     * Error code if the requested resource from the Bridge did not exist.
     * 
     * <p>
     * For example, if a bucket with the given id (which is not malformed) does not
     * exist.
     * </p>
     */
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * Error code if a resource could not be created due to conflict.
     * 
     * <p>
     * For example, if a resource with the same name already exists.
     * </p>
     */
    public static final int HTTP_CONFLICT = 409;

    /**
     * Error code if the transfer rate limit for this account was reached.
     */
    public static final int HTTP_TRANSFER_RATE_LIMIT = 420;

    /**
     * Error code if the request rate limit for this account was reached.
     */
    public static final int HTTP_TOO_MANY_REQUESTS = 429;

    /**
     * Error code if an internal server error occured on the Bridge.
     */
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    /**
     * Error code if the Storj network is temporarily overloaded.
     * 
     * <p>
     * For example, if the Bridge could not establish a contract for a shard within
     * a specific period of time.
     * </p>
     */
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;

    /**
     * Error code if the Storj environment could not be initialized.
     * 
     * <p>
     * Most probably due to memory allocation issues.
     * </p>
     */
    public static final int STORJ_ENV_INIT_ERROR = 900;

    /**
     * Error code for general error with requesting the Bridge during a download and
     * upload operations.
     */
    public static final int STORJ_BRIDGE_REQUEST_ERROR = 1000;

    /**
     * Error code if a download or upload operation was requested with invalid
     * credentials.
     */
    public static final int STORJ_BRIDGE_AUTH_ERROR = 1001;

    /**
     * Error code if failed to request a token during a download or upload
     * operation.
     */
    public static final int STORJ_BRIDGE_TOKEN_ERROR = 1002;

    /**
     * Error code if a request to the Bridge timed out during a download or upload
     * operation.
     */
    public static final int STORJ_BRIDGE_TIMEOUT_ERROR = 1003;

    /**
     * Error code if an Internal Server Error (500) occured on the Bridge during a
     * download or upload operation.
     */
    public static final int STORJ_BRIDGE_INTERNAL_ERROR = 1004;

    /**
     * Error code if a Transfer Rate Limit (420) or Request Rate Limit (429) error
     * occured on the Bridge during a download or upload operation.
     */
    public static final int STORJ_BRIDGE_RATE_ERROR = 1005;

    /**
     * Error code if an attempt was made to upload a file to a bucket that does not
     * exist.
     */
    public static final int STORJ_BRIDGE_BUCKET_NOTFOUND_ERROR = 1006;

    /**
     * Error code if an attempt was made to download a file that does not exist.
     */
    public static final int STORJ_BRIDGE_FILE_NOTFOUND_ERROR = 1007;

    /**
     * Error code if the Bridge returned an unexpected JSON response.
     */
    public static final int STORJ_BRIDGE_JSON_ERROR = 1008;

    /**
     * Error code if the Bridge failed to request a frame during file upload.
     */
    public static final int STORJ_BRIDGE_FRAME_ERROR = 1009;

    /**
     * Error code if the Bridge failed to request the pointers to the file being
     * downloaded.
     */
    public static final int STORJ_BRIDGE_POINTER_ERROR = 1010;

    /**
     * Error code if the Bridge failed to replace a pointer to the file being
     * downloaded.
     */
    public static final int STORJ_BRIDGE_REPOINTER_ERROR = 1011;

    /**
     * Error code if the Bridge failed to get the metainfo about the file being
     * downloaded.
     */
    public static final int STORJ_BRIDGE_FILEINFO_ERROR = 1012;

    /**
     * Error code if a file with the same name as the file being uploaded already
     * exists in the bucket.
     */
    public static final int STORJ_BRIDGE_BUCKET_FILE_EXISTS = 1013;

    /**
     * Error code if the Bridge failed to receive a storage offer from the Storj
     * network for the file being uploaded.
     */
    public static final int STORJ_BRIDGE_OFFER_ERROR = 1014;

    /**
     * Error code if info about the file being uploaded could not be retrieved from
     * the local file system.
     */
    public static final int STORJ_FILE_INTEGRITY_ERROR = 3000;

    /**
     * Error code if failed to write the file to the local file system.
     */
    public static final int STORJ_FILE_WRITE_ERROR = 3001;

    /**
     * Error code if failed to encrypt the file.
     */
    public static final int STORJ_FILE_ENCRYPTION_ERROR = 3002;

    /**
     * Error code if the file selected for upload has invalid size.
     */
    public static final int STORJ_FILE_SIZE_ERROR = 3003;

    /**
     * Error code if failed to decrypt the file.
     */
    public static final int STORJ_FILE_DECRYPTION_ERROR = 3004;

    /**
     * Error code if failed to generate the HMAC checksum for the file.
     */
    public static final int STORJ_FILE_GENERATE_HMAC_ERROR = 3005;

    /**
     * Error code if error occurred while reading the file being uploaded from the
     * local file system.
     */
    public static final int STORJ_FILE_READ_ERROR = 3006;

    /**
     * Error code if the file has too many missing shards to be recovered from the
     * Storj network.
     */
    public static final int STORJ_FILE_SHARD_MISSING_ERROR = 3007;

    /**
     * Error code if failed to reconstruct the file from the downloaded shards.
     */
    public static final int STORJ_FILE_RECOVER_ERROR = 3008;

    /**
     * Error code if failed to resize the file on the local file system while shards
     * were being downloaded.
     */
    public static final int STORJ_FILE_RESIZE_ERROR = 3009;

    /**
     * Error code if the file being downloaded was encrypted with unsupported
     * erasure codes.
     * 
     * <p>
     * Currently only "reedsolomon" codes are supported.
     * </p>
     */
    public static final int STORJ_FILE_UNSUPPORTED_ERASURE = 3010;

    /**
     * Error code if failed to create parity shards for the file being uploaded.
     */
    public static final int STORJ_FILE_PARITY_ERROR = 3011;

    /**
     * Error code if a memory allocation request failed in the libstorj native
     * library.
     */
    public static final int STORJ_MEMORY_ERROR = 4000;

    /**
     * Error code if failed to map the file being downloaded to memory.
     */
    public static final int STORJ_MAPPING_ERROR = 4001;

    /**
     * Error code if failed to unmap the file being downloaded from memory.
     */
    public static final int STORJ_UNMAPPING_ERROR = 4002;

    /**
     * Error code if error occurred with the event loop of the libstorj native
     * library.
     */
    public static final int STORJ_QUEUE_ERROR = 5000;

    /**
     * Error code if failed to encrypt the bucket or file metadata.
     */
    public static final int STORJ_META_ENCRYPTION_ERROR = 6000;

    /**
     * Error code if failed to decrypt the bucket or file metadata.
     */
    public static final int STORJ_META_DECRYPTION_ERROR = 6001;

    /**
     * Error code if failed to generate the file key for encryption.
     */
    public static final int STORJ_HEX_DECODE_ERROR = 7000;

    private static final String DEFAULT_PROTO = "https";
    private static final String DEFAULT_HOST = "api.storj.io";
    private static final int DEFAULT_PORT = 443;

    private static String USER_AGENT;

    private String proto;
    private String host;
    private int port;
    private Keys keys;
    private java.io.File configDir;
    private java.io.File downloadDir;

    static {
        loadLibrary();

        // Set User Agent
        String version = getVersion();
        if (version == null) {
            USER_AGENT = "java-libstorj";
        } else {
            USER_AGENT = "java-libstorj-" + version;
        }
    }

    /**
     * We need this in a separate method, so it can be mocked by JMockit and make
     * tests independent of native libraries.
     */
    private static void loadLibrary() {
        System.loadLibrary("storj-java");
    }

    private static String getVersion() {
        try {
            Properties properties = new Properties();
            InputStream in = Storj.class.getClassLoader().getResourceAsStream("version.properties");
            if (in != null) {
                properties.load(in);
                return properties.getProperty("version");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Constructs a Storj object with default configuraton.
     * 
     * <p>
     * It will be configured to connect to the Bridge hosted at
     * <code>https://api.storj.io</code>.
     * </p>
     */
    public Storj() {
        this(DEFAULT_PROTO, DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Constructs a Storj object to connect to the Bridge hosted at the provided
     * URL.
     * 
     * @param bridgeUrl
     *            a String object with the Bridge URL
     * @throws MalformedURLException
     *             if the provided String represents a malformed URL
     */
    public Storj(String bridgeUrl) throws MalformedURLException {
        this(new URL(bridgeUrl));
    }

    /**
     * Constructs a Storj object to connect to the Bridge hosted at the provided
     * URL.
     * 
     * @param bridgeUrl
     *            the Bridge URL
     */
    public Storj(URL bridgeUrl) {
        this(bridgeUrl.getProtocol(), bridgeUrl.getHost(), bridgeUrl.getPort());
    }

    private Storj(String bridgeProtocol, String bridgeHost, int bridgePort) {
        proto = bridgeProtocol;
        host = bridgeHost;
        port = bridgePort;
        
        if (port == -1) {
            switch (bridgeProtocol) {
            case "http":
                port = 80;
                break;
            case "https":
                port = 443;
                break;
            }
        }

        configDir = new java.io.File(System.getProperty("user.home"), ".storj");
        downloadDir = new java.io.File(System.getProperty("user.dir"));
    }

    /**
     * Configure the locaton of the configuration directory.
     * 
     * <p>
     * The configuration directory is where the authentication files are stored.
     * </p>
     * 
     * <p>
     * The default location of the configuration directory is '.storj' under the
     * user home directory, e.g. <code>/home/myuser/.storj</code> or
     * <code>C:\Users\myuser\.storj</code>.
     * </p>
     * 
     * @param dir
     *            a directory
     * @return a reference to this Storj object
     */
    public Storj setConfigDirectory(java.io.File dir) {
        configDir = dir;
        return this;
    }

    /**
     * Configure the default directory for downloading files.
     * 
     * <p>
     * Downloading files to a custom directory is still possible using the
     * {@link #downloadFile(Bucket, File, String, DownloadFileCallback)} variant
     * that takes a <code>localPath</code> parameter.
     * </p>
     * 
     * <p>
     * The default download directory is the program's working directory.
     * </p>
     * 
     * @param dir
     *            a directory
     * @return a reference to this Storj object
     */
    public Storj setDownloadDirectory(java.io.File dir) {
        downloadDir = dir;
        return this;
    }

    /**
     * Returns the current unix timestamp in milliseconds.
     * 
     * @return a unix timestamp
     */
    public static native long getTimestamp();

    /**
     * Generates a new random mnemonic.
     * 
     * <p>
     * This will generate a new random mnemonic with 128 to 256 bits of entropy.
     * </p>
     * 
     * @param strength
     *            the bits of entropy
     * @return a 12 or 24-word mnemonic
     */
    public static native String generateMnemonic(int strength);

    /**
     * Checks that a mnemonic is valid.
     * 
     * <p>
     * This will check that a mnemonic has been entered correctly by verifying the
     * checksum, and that words are a part of the dictionary.
     * </p>
     * 
     * @param mnemonic
     *            the mnemonic
     * @return <code>true</code> on success and <code>false</code> on failure
     */
    public static native boolean checkMnemonic(String mnemonic);

    /**
     * Returns the error message for the given error code.
     * 
     * <p>
     * Note that the <code>message</code> parameter of the <code>onError()</code>
     * method in callbacks often provides more detailed error message than this
     * method.
     * </p>
     * 
     * @param code
     *            the error code
     * @return an error message
     */
    public static String getErrorMessage(int code) {
        switch (code) {
        case HTTP_BAD_REQUEST:
            return "Bad Request";
        case HTTP_UNAUTHORIZED:
            return "Unauthorized";
        case HTTP_NOT_FOUND:
            return "Not Found";
        case HTTP_CONFLICT:
            return "Conflict";
        case HTTP_TRANSFER_RATE_LIMIT:
            return "Transfer Rate Limit Reached";
        case HTTP_TOO_MANY_REQUESTS:
            return "Too Many Requests";
        case HTTP_INTERNAL_SERVER_ERROR:
            return "Internal Server Error";
        case HTTP_SERVICE_UNAVAILABLE:
            return "Service Unavailable";
        case STORJ_ENV_INIT_ERROR:
            return "Failed to initialize Storj environment";
        default:
            return _getErrorMessage(code);
        }
    }

    /**
     * Returns the Storj Bridge API information.
     * 
     * @param callback
     *            an implementation of the {@link GetInfoCallback} interface to
     *            receive the response
     */
    public void getInfo(GetInfoCallback callback) {
        _getInfo(new Environment(), callback);
    }

    /**
     * Registers a new user in the Storj Bridge.
     * 
     * @param user
     *            the user's email
     * @param pass
     *            the user's password
     * @param callback
     *            an implementation of the {@link RegisterCallback} interface to
     *            receive the response
     */
    public void register(String user, String pass, RegisterCallback callback) {
        _register(new Environment(user, pass), callback);
    }

    /**
     * Checks if the user's keys have already been imported.
     * 
     * @return <code>true</code> if the keys are imported, and <code>false</code>
     *         otherwise
     */
    public boolean keysExist() {
        return getAuthFile().exists();
    }

    /**
     * Returns the user's keys that have been imported by decrypting the
     * authentication file with the provided passphrase.
     * 
     * @param passphrase
     *            the passphrase to decrypt the keys, can be an empty String, but
     *            not <code>null</code>
     * @return a {@code Key} object with the user's keys, or <code>null</code> if
     *         the keys have not been imported yet.
     */
    public Keys getKeys(String passphrase) {
        if (keys == null) {
            keys = _exportKeys(getAuthFile().toString(), passphrase);
        }
        return keys;
    }

    /**
     * Import the provided keys and encrypt them with the provided passphrase in an
     * authentication file.
     * 
     * @param keys
     *            a {@link Keys} object with the user's keys
     * @param passphrase
     *            the passphrase to encrypt the keys, can be an empty String, but
     *            not <code>null</code>
     * @return <code>true</code> if importing keys was successful,
     *         <code>false</code> otherwise
     */
    public boolean importKeys(Keys keys, String passphrase) {
        boolean success = _writeAuthFile(getAuthFile().toString(), keys.getUser(), keys.getPass(), keys.getMnemonic(),
                passphrase);
        if (success) {
            this.keys = keys;
        }
        return success;
    }

    /**
     * Deletes the authentication file with user's keys and clear them from memory.
     * 
     * @return <code>true</code> if deleting the authentication file was successful,
     *         <code>false</code> otherwise
     */
    public boolean deleteKeys() {
        boolean success = getAuthFile().delete();
        if (success) {
            keys = null;
        }
        return success;
    }

    /**
     * Verifies if the provided credentials are valid.
     * 
     * <p>
     * This will try to list the buckets with the provided user and password. It
     * will block until the response is received.
     * </p>
     * 
     * @param user
     *            the user's email
     * @param pass
     *            the user's password
     * @return {@link #NO_ERROR} if the user and password match;
     *         {@link #HTTP_UNAUTHORIZED} if the credentials are invalid; or other
     *         error codes due to network issues.
     * @throws InterruptedException
     *             if the request was interrupted
     */
    public int verifyKeys(String user, String pass) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] result = { NO_ERROR };

        _getBuckets(new Environment(user, pass), new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                // success
                latch.countDown();
            }

            @Override
            public void onError(int code, String message) {
                result[0] = code;
                latch.countDown();
            }
        });

        latch.await();

        return result[0];
    }

    /**
     * Verifies if the provided keys (user, password and mnemonic) are valid.
     * 
     * <p>
     * This will try to list the buckets and decrypt their metadata with the
     * provided keys. It will block until the response is received.
     * </p>
     * 
     * @param keys
     *            the user's keys
     * @return {@link #NO_ERROR} if the user and password match, and the mnemonic
     *         can decrypt the metadata; {@link #HTTP_UNAUTHORIZED} if the
     *         credentials are invalid; {@link #STORJ_FILE_DECRYPTION_ERROR} if the
     *         user and password match, but the mnemonic could not decrypt any
     *         metadata; or other error codes due to network issues.
     * @throws InterruptedException
     *             if the request was interrupted
     */
    public int verifyKeys(Keys keys) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] result = { NO_ERROR };

        _getBuckets(new Environment(keys), new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                boolean validMnemonic = false;

                if (buckets.length == 0) {
                    // empty account, assume valid mnemonic
                    validMnemonic = true;
                }

                for (Bucket bucket : buckets) {
                    if (bucket.isDecrypted()) {
                        validMnemonic = true;
                        break;
                    }
                }

                if (!validMnemonic) {
                    result[0] = STORJ_META_DECRYPTION_ERROR;
                }

                latch.countDown();
            }

            @Override
            public void onError(int code, String message) {
                result[0] = code;
                latch.countDown();
            }
        });

        latch.await();

        return result[0];
    }

    /**
     * Lists available buckets for the user.
     * 
     * @param callback
     *            an implementation of the {@link GetBucketsCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getBuckets(GetBucketsCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getBuckets(new Environment(), callback);
    }

    /**
     * Gets info about a specific bucket.
     * 
     * @param bucketId
     *            the bucket id
     * @param callback
     *            an implementation of the {@link GetBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getBucket(String bucketId, GetBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getBucket(new Environment(), bucketId, callback);
    }

    /**
     * Gets the bucket id by the provided bucket name.
     * 
     * @param bucketName
     *            the bucket name
     * @param callback
     *            an implementation of the {@link GetBucketIdCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getBucketId(String bucketName, GetBucketIdCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getBucketId(new Environment(), bucketName, callback);
    }

    /**
     * Creates a new bucket.
     * 
     * @param bucketName
     *            a name for the new bucket
     * @param callback
     *            an implementation of the {@link CreateBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void createBucket(String bucketName, CreateBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _createBucket(new Environment(), bucketName, callback);
    }

    /**
     * Deletes the specified bucket.
     * 
     * @param bucket
     *            a {@link Bucket} object
     * @param callback
     *            an implementation of the {@link DeleteBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteBucket(Bucket bucket, DeleteBucketCallback callback) throws KeysNotFoundException {
        deleteBucket(bucket.getId(), callback);
    }

    /**
     * Deletes the specified bucket.
     * 
     * @param bucketId
     *            the bucket id
     * @param callback
     *            an implementation of the {@link DeleteBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteBucket(String bucketId, DeleteBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _deleteBucket(new Environment(), bucketId, callback);
    }

    /**
     * Gets a list of all files in a bucket.
     * 
     * @param bucket
     *            a {@link Bucket} object
     * @param callback
     *            an implementation of the {@link ListFilesCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void listFiles(Bucket bucket, ListFilesCallback callback) throws KeysNotFoundException {
        listFiles(bucket.getId(), callback);
    }

    /**
     * Gets a list of all files in a bucket.
     * 
     * @param bucketId
     *            the bucket id
     * @param callback
     *            an implementation of the {@link ListFilesCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void listFiles(String bucketId, ListFilesCallback callback) throws KeysNotFoundException {
        checkKeys();
        _listFiles(new Environment(), bucketId, callback);
    }

    /**
     * Gets info about a file.
     * 
     * @param bucket
     *            the {@link Bucket} containing the file
     * @param fileId
     *            the file id
     * @param callback
     *            an implementation of the {@link GetFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFile(Bucket bucket, String fileId, GetFileCallback callback) throws KeysNotFoundException {
        getFile(bucket.getId(), fileId, callback);
    }

    /**
     * Gets info about a file.
     * 
     * @param bucketId
     *            the id of the bucket containing the file
     * @param fileId
     *            the file id
     * @param callback
     *            an implementation of the {@link GetFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFile(String bucketId, String fileId, GetFileCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getFile(new Environment(), bucketId, fileId, callback);
    }

    /**
     * Gets the file id by the provided file name.
     * 
     * @param bucket
     *            the {@link Bucket} containing the file
     * @param fileName
     *            the file name
     * @param callback
     *            an implementation of the {@link GetFileIdCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFileId(Bucket bucket, String fileName, GetFileIdCallback callback) throws KeysNotFoundException {
        getFileId(bucket.getId(), fileName, callback);
    }

    /**
     * Gets the file id by the provided file name.
     * 
     * @param bucketId
     *            the id of the bucket containing the file
     * @param fileName
     *            the file name
     * @param callback
     *            an implementation of the {@link GetFileIdCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFileId(String bucketId, String fileName, GetFileIdCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getFileId(new Environment(), bucketId, fileName, callback);
    }

    /**
     * Deletes a file.
     * 
     * @param bucket
     *            the {@link Bucket} containing the file
     * @param file
     *            the {@link File} to detele
     * @param callback
     *            an implementation of the {@link DeleteFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteFile(Bucket bucket, File file, DeleteFileCallback callback) throws KeysNotFoundException {
        deleteFile(bucket.getId(), file.getId(), callback);
    }

    /**
     * Deletes a file.
     * 
     * @param bucketId
     *            the id of the bucket containing the file
     * @param fileId
     *            the id of the file to delete
     * @param callback
     *            an implementation of the {@link DeleteFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteFile(String bucketId, String fileId, DeleteFileCallback callback) throws KeysNotFoundException {
        checkKeys();
        _deleteFile(new Environment(), bucketId, fileId, callback);
    }

    /**
     * Downloads a file to the default download directory.
     * 
     * <p>
     * The default download directory can be configured using
     * {@link #setDownloadDirectory(java.io.File)}.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} containing the file
     * @param file
     *            the {@link File} to download
     * @param callback
     *            an implementation of the {@link DownloadFileCallback} interface to
     *            receive the download progress
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void downloadFile(Bucket bucket, File file, DownloadFileCallback callback) throws KeysNotFoundException {
        checkDownloadDir();
        String localPath = new java.io.File(downloadDir, file.getName()).getPath();
        downloadFile(bucket, file, localPath, callback);
    }

    /**
     * Downloads a file to the provided local path.
     * 
     * @param bucket
     *            the {@link Bucket} containing the file
     * @param file
     *            the {@link File} to download
     * @param localPath
     *            the local path (including file name) to download the file to
     * @param callback
     *            an implementation of the {@link DownloadFileCallback} interface to
     *            receive the download progress
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void downloadFile(Bucket bucket, File file, String localPath, DownloadFileCallback callback)
            throws KeysNotFoundException {
        downloadFile(bucket.getId(), file.getId(), localPath, callback);
    }

    /**
     * Downloads a file to the provided local path.
     * 
     * @param bucketId
     *            the id of the bucket containing the file
     * @param fileId
     *            the id of the file to download
     * @param localPath
     *            the local path (including file name) to download the file to
     * @param callback
     *            an implementation of the {@link DownloadFileCallback} interface to
     *            receive the download progress
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void downloadFile(String bucketId, String fileId, String localPath, DownloadFileCallback callback)
            throws KeysNotFoundException {
        checkKeys();
        _downloadFile(new Environment(), bucketId, fileId, localPath, callback);
    }

    /**
     * Uploads a file to a bucket.
     * 
     * <p>
     * This will upload the file with the same name it has on the local storage.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} to upload the file to
     * @param localPath
     *            the local path (including file name) of the file to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void uploadFile(Bucket bucket, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        uploadFile(bucket.getId(), localPath, callback);
    }

    /**
     * Uploads a file to a bucket.
     * 
     * <p>
     * This will upload the file with the same name it has on the local storage.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket to upload the file to
     * @param localPath
     *            the local path (including file name) of the file to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void uploadFile(String bucketId, String localPath, UploadFileCallback callback)
            throws KeysNotFoundException {
        uploadFile(bucketId, new java.io.File(localPath).getName(), localPath, callback);
    }

    /**
     * Uploads a file to a bucket.
     * 
     * <p>
     * This allows uploading the file with a different name than the one it has on
     * the local storage.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} to upload the file to
     * @param fileName
     *            the name to assign to the uploaded file
     * @param localPath
     *            the local path (including file name) of the file to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void uploadFile(Bucket bucket, String fileName, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        uploadFile(bucket.getId(), fileName, localPath, callback);
    }

    /**
     * Uploads a file to a bucket.
     * 
     * <p>
     * This allows uploading the file with a different name than the one it has on
     * the local storage.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket to upload the file to
     * @param fileName
     *            the name to assign to the uploaded file
     * @param localPath
     *            the local path (including file name) of the file to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void uploadFile(String bucketId, String fileName, String localPath, UploadFileCallback callback)
            throws KeysNotFoundException {
        checkKeys();
        _uploadFile(new Environment(), bucketId, fileName, localPath, callback);
    }

    private java.io.File getAuthFile() throws IllegalStateException {
        if (configDir == null) {
            throw new IllegalStateException("config dir is not set");
        }
        return new java.io.File(configDir, host + ".json");
    }

    private void checkDownloadDir() throws IllegalStateException {
        if (downloadDir == null) {
            throw new IllegalStateException("Download directory is not set");
        }
    }

    private void checkKeys() throws KeysNotFoundException {
        if (getKeys("") == null) {
            throw new KeysNotFoundException();
        }
    }

    private static native String _getErrorMessage(int code);

    private native void _getInfo(Environment env, GetInfoCallback callback);

    private native void _register(Environment env, RegisterCallback callback);

    private native Keys _exportKeys(String location, String passphrase);

    private native boolean _writeAuthFile(String location, String user, String pass, String mnemonic, String passphrase);

    private native void _getBuckets(Environment env, GetBucketsCallback callback);

    private native void _getBucket(Environment env, String bucketId, GetBucketCallback callback);

    private native void _getBucketId(Environment env, String bucketName, GetBucketIdCallback callback);

    private native void _createBucket(Environment env, String bucketName, CreateBucketCallback callback);

    private native void _deleteBucket(Environment env, String bucketId, DeleteBucketCallback callback);

    private native void _listFiles(Environment env, String bucketId, ListFilesCallback callback);

    private native void _getFile(Environment env, String bucketId, String fileId, GetFileCallback callback);

    private native void _getFileId(Environment env, String bucketId, String fileName, GetFileIdCallback callback);

    private native void _deleteFile(Environment env, String bucketId, String fileId, DeleteFileCallback callback);

    private native void _downloadFile(Environment env, String bucketId, String fileId, String path,
            DownloadFileCallback callback);

    private native void _uploadFile(Environment env, String bucketId, String fileName, String localPath,
            UploadFileCallback callback);

    private class Environment {

        String proto;
        String host;
        int port;

        String user;
        String pass;
        String mnemonic;

        String userAgent;
        String proxyUrl;
        String caInfoPath;
        long lowSpeedLimit;
        long lowSpeedTime;
        long timeout;

        int logLevel;

        private Environment() {
            this(keys);
        }

        private Environment(String user, String pass) {
            this(new Keys(user, pass, null));
        }

        private Environment(Keys keys) {
            proto = Storj.this.proto;
            host = Storj.this.host;
            port = Storj.this.port;

            if (keys != null) {
                user = keys.getUser();
                pass = keys.getPass();
                mnemonic = keys.getMnemonic();
            }

            userAgent = USER_AGENT;
            proxyUrl = null; // TODO
            caInfoPath = System.getenv("STORJ_CAINFO"); // TODO
        }

    }

}
