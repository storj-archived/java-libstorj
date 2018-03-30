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

/**
 * Java object wrapper of the libstorj native library.
 */
public class Storj {

    /**
     * Error code if there is no error.
     */
    public static final int NO_ERROR = 0;

    /**
     * Error code if file transfer was canceled.
     */
    public static final int TRANSFER_CANCELED = 1;
    
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
     * Error code if the requested operation is forbidden.
     * 
     * <p>
     * For example, if the user account has not been activated yet.
     * </p>
     */
    public static final int HTTP_FORBIDDEN = 403;

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
     * Error code for general error with requesting the Bridge during a download or
     * upload operation.
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
     * Error code for general error with requesting a farmer during a download or
     * upload operation.
     */
    public static final int STORJ_FARMER_REQUEST_ERROR = 2000;

    /**
     * Error code if a request to a farmer timed out during a download or upload
     * operation.
     */
    public static final int STORJ_FARMER_TIMEOUT_ERROR = 2001;

    /**
     * Error code if a farmer was requested with invalid credentials.
     */
    public static final int STORJ_FARMER_AUTH_ERROR = 2002;

    /**
     * Error code if a farmer is exhausted.
     */
    public static final int STORJ_FARMER_EXHAUSTED_ERROR = 2003;

    /**
     * Error code for farmer integrity problems.
     */
    public static final int STORJ_FARMER_INTEGRITY_ERROR = 2004;

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

    /**
     * Error code if the Bridge URL uses protocol that is not supported.
     */
    public static final int CURLE_UNSUPPORTED_PROTOCOL = 10001;

    /**
     * Error code if the Bridge URL is not properly formatted.
     */
    public static final int CURLE_URL_MALFORMAT = 10003;

    /**
     * Error code if the given proxy host could not be resolved.
     */
    public static final int CURLE_COULDNT_RESOLVE_PROXY = 10005;

    /**
     * Error code if the Bridge host could not be resolved.
     */
    public static final int CURLE_COULDNT_RESOLVE_HOST = 10006;

    /**
     * Error code if failed to connect to the Bridge server or to the proxy.
     */
    public static final int CURLE_COULDNT_CONNECT = 10007;

    /**
     * Error code if a memory allocation request failed in the libcurl native
     * library.
     */
    public static final int CURLE_OUT_OF_MEMORY = 10027;

    /**
     * Error code if a network operation timed out.
     */
    public static final int CURLE_OPERATION_TIMEDOUT = 10028;

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
    public static final int ENOENT = 20002;

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
    public static final int EACCES = 20013;

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

    private EventLoopRunner looper;

    /**
     * Pointer to the address of the storj_env_t struct in the native library.
     */
    private long env;

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
     * 
     * <p>
     * After completing the work with this object make sure to call
     * {@link #destroy()} to release the allocated native resources.
     * </p>
     * 
     * @see Storj#destroy()
     */
    public Storj() {
        this(DEFAULT_PROTO, DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Constructs a Storj object to connect to the Bridge hosted at the provided
     * URL.
     * 
     * <p>
     * After completing the work with this object make sure to call
     * {@link #destroy()} to release the allocated native resources.
     * </p>
     * 
     * @param bridgeUrl
     *            a String object with the Bridge URL
     * @throws MalformedURLException
     *             if the provided String represents a malformed URL
     * @see Storj#destroy()
     */
    public Storj(String bridgeUrl) throws MalformedURLException {
        this(new URL(bridgeUrl));
    }

    /**
     * Constructs a Storj object to connect to the Bridge hosted at the provided
     * URL.
     * 
     * <p>
     * After completing the work with this object make sure to call
     * {@link #destroy()} to release the allocated native resources.
     * </p>
     * 
     * @param bridgeUrl
     *            the Bridge URL
     * @see Storj#destroy()
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
        
        if (!configDir.exists()) {
            boolean success = configDir.mkdirs();
            if (!success) {
                throw new IllegalStateException("config dir does not exists and could not create it");
            }
        }
            
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
        case HTTP_FORBIDDEN:
            return "Forbidden";
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
        long env = initEnv();

        _getInfo(env, callback);
        _runEventLoop(env);

        destroyEnv(env);
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
        long env = initEnv(user, pass);

        _register(env, callback);
        _runEventLoop(env);

        destroyEnv(env);
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
     * <p>
     * The authentication file is located in the configuration directory.
     * </p>
     * 
     * @param passphrase
     *            the passphrase to decrypt the keys, can be an empty String, but
     *            not <code>null</code>
     * @return a {@code Key} object with the user's keys, or <code>null</code> if
     *         the keys have not been imported yet.
     * @see #setConfigDirectory(java.io.File)
     */
    public Keys getKeys(String passphrase) {
        if (keys == null) {
            keys = _exportKeys(getAuthFile().toString(), passphrase);
        }
        return keys;
    }

    /**
     * Imports the provided keys and encrypt them with the provided passphrase in an
     * authentication file.
     * 
     * <p>
     * The authentication file is located in the configuration directory.
     * </p>
     * 
     * @param keys
     *            a {@link Keys} object with the user's keys
     * @param passphrase
     *            the passphrase to encrypt the keys, can be an empty String, but
     *            not <code>null</code>
     * @return <code>true</code> if importing keys was successful,
     *         <code>false</code> otherwise
     * @see #setConfigDirectory(java.io.File)
     */
    public boolean importKeys(Keys keys, String passphrase) {
        boolean success = _writeAuthFile(getAuthFile().toString(), keys.getUser(), keys.getPass(), keys.getMnemonic(),
                passphrase);
        if (success) {
            this.keys = keys;
            // re-init Storj env
            destroyEnv(env);
            env = initEnv(keys);
            // start the event loop runner if not running yet
            startLooper();
        }
        return success;
    }

    /**
     * Deletes the authentication file with user's keys and clear them from memory.
     * 
     * <p>
     * The authentication file is located in the configuration directory.
     * </p>
     * 
     * @return <code>true</code> if deleting the authentication file was successful,
     *         <code>false</code> otherwise
     * @see #setConfigDirectory(java.io.File)
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
     *         {@link #HTTP_UNAUTHORIZED} if the credentials are invalid;
     *         {@link #HTTP_FORBIDDEN} if the account has not been activated yet; or
     *         other error codes due to network issues.
     * @throws InterruptedException
     *             if the request was interrupted
     */
    public int verifyKeys(String user, String pass) throws InterruptedException {
        long env = initEnv(user, pass);
        final int[] result = { NO_ERROR };

        _getBuckets(env, new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                // success
            }

            @Override
            public void onError(int code, String message) {
                result[0] = code;
            }
        });

        _runEventLoop(env);

        destroyEnv(env);

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
     * <p>
     * If the provided mnemonic could not decrypt any bucket then a second attempt
     * will be made to decrypt all buckets with empty mnemonic. If this second
     * attempt is successful then the orignally provided mnemonic will be accepted
     * as valid. This covers the use case where the user account was created on
     * app.storj.io and the user has created one or more buckets there.
     * </p>
     * 
     * @param keys
     *            the user's keys
     * @return {@link #NO_ERROR} if the user and password match, and the mnemonic
     *         can decrypt the metadata; {@link #HTTP_UNAUTHORIZED} if the
     *         credentials are invalid; {@link #HTTP_FORBIDDEN} if the account has
     *         not been activated yet; {@link #STORJ_FILE_DECRYPTION_ERROR} if the
     *         user and password match, but the mnemonic could not decrypt any
     *         metadata; or other error codes due to network issues.
     * @throws InterruptedException
     *             if the request was interrupted
     */
    public int verifyKeys(Keys keys) throws InterruptedException {
        long env = initEnv(keys);
        final int[] result = { NO_ERROR };

        _getBuckets(env, new GetBucketsCallback() {
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
            }

            @Override
            public void onError(int code, String message) {
                result[0] = code;
            }
        });

        _runEventLoop(env);

        destroyEnv(env);

        if (result[0] != STORJ_META_DECRYPTION_ERROR) {
            return result[0];
        }

        // The mnemonic could not decrypt any of the buckets. Make another attempt with
        // empty mnemonic. If all buckets can be decrypted with empty mnemonic then it
        // seems that account was created on app.storj.io and all buckets were created
        // there. In this case accept the originally provided mnemonic as valid.
        env = initEnv(keys.getUser(), keys.getPass());
        final int[] result2 = { NO_ERROR };
        
        _getBuckets(env, new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                boolean validMnemonic = true;

                for (Bucket bucket : buckets) {
                    if (!bucket.isDecrypted()) {
                        validMnemonic = false;
                        break;
                    }
                }

                if (!validMnemonic) {
                    result2[0] = STORJ_META_DECRYPTION_ERROR;
                }
            }

            @Override
            public void onError(int code, String message) {
                result2[0] = code;
            }
        });

        _runEventLoop(env);

        destroyEnv(env);

        return result2[0];
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
        checkEnv();
        _getBuckets(env, callback);
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
        checkEnv();
        _getBucket(env, bucketId, callback);
    }

    /**
     * Gets info about the specified list of buckets.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each bucket id
     * from the list. The requests will run in parallel. The callback will be
     * invoked once for each bucket id. The order of invoking the callback is
     * undetermined and most likely will be different than the order of bucket ids
     * in the list.
     * </p>
     * 
     * @param bucketIds
     *            an array of bucket ids
     * @param callback
     *            an implementation of the {@link GetBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getBuckets(String bucketIds[], GetBucketCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (String bucketId : bucketIds) {
            _getBucket(env, bucketId, callback);
        }
    }

    /**
     * Gets the bucket id by the specified bucket name.
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
        checkEnv();
        _getBucketId(env, bucketName, callback);
    }

    /**
     * Gets the bucket ids for the specified list of bucket names.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each bucket name
     * from the list. The requests will run in parallel. The callback will be
     * invoked once for each bucket name. The order of invoking the callback is
     * undetermined and most likely will be different than the order of bucket names
     * in the list.
     * </p>
     * 
     * @param bucketNames
     *            an array of bucket names
     * @param callback
     *            an implementation of the {@link GetBucketIdCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getBucketIds(String[] bucketNames, GetBucketIdCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (String bucketName : bucketNames) {
            _getBucketId(env, bucketName, callback);
        }
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
        checkEnv();
        _createBucket(env, bucketName, callback);
    }

    /**
     * Creates a list of new buckets.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each bucket name
     * from the list. The requests will run in parallel. The callback will be
     * invoked once for each bucket name. The order of invoking the callback is
     * undetermined and most likely will be different than the order of bucket names
     * in the list.
     * </p>
     * 
     * @param bucketNames
     *            an array of bucket names
     * @param callback
     *            an implementation of the {@link CreateBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void createBuckets(String[] bucketNames, final CreateBucketCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (final String bucketName : bucketNames) {
            _createBucket(env, bucketName, callback);
        }
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
        checkEnv();
        _deleteBucket(env, bucketId, callback);
    }

    /**
     * Deletes the specified list of buckets.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each bucket from
     * the list. The requests will run in parallel. The callback will be invoked
     * once for each bucket. The order of invoking the callback is undetermined and
     * most likely will be different than the order of buckets in the list.
     * </p>
     * 
     * @param buckets
     *            an array of {@link Bucket} objects
     * @param callback
     *            an implementation of the {@link DeleteBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteBuckets(Bucket[] buckets, DeleteBucketCallback callback) throws KeysNotFoundException {
        deleteBuckets(getBucketIds(buckets), callback);
    }

    /**
     * Deletes the specified list of buckets.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each bucket id
     * from the list. The requests will run in parallel. The callback will be
     * invoked once for each bucket id. The order of invoking the callback is
     * undetermined and most likely will be different than the order of bucket ids
     * in the list.
     * </p>
     * 
     * @param bucketIds
     *            an array of bucket ids
     * @param callback
     *            an implementation of the {@link DeleteBucketCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteBuckets(String[] bucketIds, DeleteBucketCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (String bucketId : bucketIds) {
            _deleteBucket(env, bucketId, callback);
        }
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
        checkEnv();
        _listFiles(env, bucketId, callback);
    }

    /**
     * Gets a list of all files for each bucket in the specified list.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each bucket from
     * the list. The requests will run in parallel. The callback will be invoked
     * once for each bucket. The order of invoking the callback is undetermined and
     * most likely will be different than the order of buckets in the list.
     * </p>
     * 
     * @param buckets
     *            an array of {@link Bucket} objects
     * @param callback
     *            an implementation of the {@link ListFilesCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void listFiles(Bucket[] buckets, ListFilesCallback callback) throws KeysNotFoundException {
        listFiles(getBucketIds(buckets), callback);
    }

    /**
     * Gets a list of all files for each bucket in the specified list.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each bucket id
     * from the list. The requests will run in parallel. The callback will be
     * invoked once for each bucket id. The order of invoking the callback is
     * undetermined and most likely will be different than the order of bucket ids
     * in the list.
     * </p>
     * 
     * @param bucketIds
     *            an array of bucket ids
     * @param callback
     *            an implementation of the {@link ListFilesCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void listFiles(String[] bucketIds, ListFilesCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (String bucketId : bucketIds) {
            _listFiles(env, bucketId, callback);
        }
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
        checkEnv();
        _getFile(env, bucketId, fileId, callback);
    }

    /**
     * Gets info about the specified list of files.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each file id from
     * the list. The requests will run in parallel. The callback will be invoked
     * once for each file id. The order of invoking the callback is undetermined and
     * most likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} containing the files
     * @param fileIds
     *            an array of file ids
     * @param callback
     *            an implementation of the {@link GetFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFiles(Bucket bucket, String[] fileIds, GetFileCallback callback) throws KeysNotFoundException {
        getFiles(bucket.getId(), fileIds, callback);
    }

    /**
     * Gets info about the specified list of files.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each file id from
     * the list. The requests will run in parallel. The callback will be invoked
     * once for each file id. The order of invoking the callback is undetermined and
     * most likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket containing the files
     * @param fileIds
     *            an array of file ids
     * @param callback
     *            an implementation of the {@link GetFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFiles(String bucketId, String[] fileIds, GetFileCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (String fileId : fileIds) {
            _getFile(env, bucketId, fileId, callback);
        }
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
        checkEnv();
        _getFileId(env, bucketId, fileName, callback);
    }

    /**
     * Gets the file ids for the specified list of file names.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each file name
     * from the list. The requests will run in parallel. The callback will be
     * invoked once for each file name. The order of invoking the callback is
     * undetermined and most likely will be different than the order of file names
     * in the list.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} containing the files
     * @param fileNames
     *            an array of file names
     * @param callback
     *            an implementation of the {@link GetFileIdCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFileIds(Bucket bucket, String[] fileNames, GetFileIdCallback callback) throws KeysNotFoundException {
        getFileIds(bucket.getId(), fileNames, callback);
    }
    
    /**
     * Gets the file ids for the specified list of file names.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each file name
     * from the list. The requests will run in parallel. The callback will be
     * invoked once for each file name. The order of invoking the callback is
     * undetermined and most likely will be different than the order of file names
     * in the list.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket containing the files
     * @param fileNames
     *            an array of file names
     * @param callback
     *            an implementation of the {@link GetFileIdCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void getFileIds(String bucketId, String[] fileNames, GetFileIdCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (String fileName : fileNames) {
            _getFileId(env, bucketId, fileName, callback);
        }
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
        checkEnv();
        _deleteFile(env, bucketId, fileId, callback);
    }
    
    /**
     * Deletes the specified list of files.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each file from
     * the list. The requests will run in parallel. The callback will be invoked
     * once for each file. The order of invoking the callback is undetermined and
     * most likely will be different than the order of files in the list.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} containing the files
     * @param files
     *            an array of {@link File} objects to delete
     * @param callback
     *            an implementation of the {@link DeleteFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteFiles(Bucket bucket, File[] files, DeleteFileCallback callback) throws KeysNotFoundException {
        deleteFiles(bucket.getId(), getFileIds(files), callback);
    }

    /**
     * Deletes the specified list of files.
     * 
     * <p>
     * This method will issue a seperate request to the bridge for each file id from
     * the list. The requests will run in parallel. The callback will be invoked
     * once for each file id. The order of invoking the callback is undetermined and
     * most likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket containing the files
     * @param fileIds
     *            an array of file ids to delete
     * @param callback
     *            an implementation of the {@link DeleteFileCallback} interface to
     *            receive the response
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     */
    public void deleteFiles(String bucketId, String[] fileIds, DeleteFileCallback callback) throws KeysNotFoundException {
        checkEnv();
        for (String fileId : fileIds) {
            _deleteFile(env, bucketId, fileId, callback);
        }
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
     * @return a pointer to the download state that can be passed to
     *         {@link #_cancelDownload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #setDownloadDirectory(java.io.File)
     * @see #cancelDownload(long)
     */
    public long downloadFile(Bucket bucket, File file, DownloadFileCallback callback) throws KeysNotFoundException {
        checkDownloadDir();
        String localPath = new java.io.File(downloadDir, file.getName()).getPath();
        return downloadFile(bucket, file, localPath, callback);
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
     * @return a pointer to the download state that can be passed to
     *         {@link #_cancelDownload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelDownload(long)
     */
    public long downloadFile(Bucket bucket, File file, String localPath, DownloadFileCallback callback) throws KeysNotFoundException {
        return downloadFile(bucket.getId(), file.getId(), localPath, callback);
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
     * @return a pointer to the download state that can be passed to
     *         {@link #_cancelDownload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelDownload(long)
     */
    public long downloadFile(String bucketId, String fileId, String localPath, DownloadFileCallback callback) throws KeysNotFoundException {
        checkEnv();
        return _downloadFile(env, bucketId, fileId, localPath, callback);
    }

    /**
     * Downloads the specified list of files to the default download directory.
     * 
     * <p>
     * The default download directory can be configured using
     * {@link #setDownloadDirectory(java.io.File)}.
     * </p>
     * 
     * <p>
     * This method will download all files in parallel. The callback will be invoked
     * for all files. The order of invoking the callback is undetermined and most
     * likely will be different than the order of files in the list.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} containing the files
     * @param files
     *            an array of {@link File} objects to download
     * @param callback
     *            an implementation of the {@link DownloadFileCallback} interface to
     *            receive the download progress
     * @return an array with pointers to the download state of each file that can be
     *         passed to {@link #_cancelDownload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #setDownloadDirectory(java.io.File)
     * @see #cancelDownload(long)
     */
    public long[] downloadFiles(Bucket bucket, File[] files, DownloadFileCallback callback) throws KeysNotFoundException {
        checkDownloadDir();

        String[] localPaths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            localPaths[i] = new java.io.File(downloadDir, files[i].getName()).getPath();
        }

        return downloadFiles(bucket, files, localPaths, callback);
    }

    /**
     * Downloads the specified list of files to the provided local paths.
     * 
     * <p>
     * This method will download all files in parallel. The callback will be invoked
     * for all files. The order of invoking the callback is undetermined and most
     * likely will be different than the order of files in the list.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} containing the files
     * @param files
     *            an array of {@link File} objects to download
     * @param localPaths
     *            an array of local paths (including file name) to download the
     *            files to
     * @param callback
     *            an implementation of the {@link DownloadFileCallback} interface to
     *            receive the download progress
     * @return an array with pointers to the download state of each file that can be
     *         passed to {@link #_cancelDownload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelDownload(long)
     */
    public long[] downloadFiles(Bucket bucket, File[] files, String[] localPaths, DownloadFileCallback callback) throws KeysNotFoundException {
        return downloadFiles(bucket.getId(), getFileIds(files), localPaths, callback);
    }

    /**
     * Downloads the specified list of files to the provided local paths.
     * 
     * <p>
     * This method will download all files in parallel. The callback will be invoked
     * for all files. The order of invoking the callback is undetermined and most
     * likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket containing the files
     * @param fileIds
     *            an array of file ids to download
     * @param localPaths
     *            an array of local paths (including file name) to download the
     *            files to
     * @param callback
     *            an implementation of the {@link DownloadFileCallback} interface to
     *            receive the download progress
     * @return an array with pointers to the download state of each file that can be
     *         passed to {@link #_cancelDownload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelDownload(long)
     */
    public long[] downloadFiles(String bucketId, String[] fileIds, String[] localPaths, DownloadFileCallback callback) throws KeysNotFoundException {
        checkEnv();

        long[] states = new long[fileIds.length];

        for (int i = 0; i < fileIds.length; i++) {
            states[i] = _downloadFile(env, bucketId, fileIds[i], localPaths[i], callback);
        }

        return states;
    }

    /**
     * Cancels the file download for the specified download state.
     * 
     * @param downloadState
     *            a pointer to the download state returned by the
     *            <code>downloadFile()</code> methods
     * 
     * @return <code>true</code> if the download was canceled successfully,
     *         <code>false</code> otherwise
     * 
     * @see #downloadFile(Bucket, File, DownloadFileCallback)
     * @see #downloadFile(Bucket, File, String, DownloadFileCallback)
     * @see #downloadFile(String, String, String, DownloadFileCallback)
     */
    public boolean cancelDownload(long downloadState) {
        return _cancelDownload(downloadState);
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
     * @return a pointer to the upload state that can be passed to
     *         {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long uploadFile(Bucket bucket, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        return uploadFile(bucket.getId(), localPath, callback);
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
     * @return a pointer to the upload state that can be passed to
     *         {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long uploadFile(String bucketId, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        return uploadFile(bucketId, new java.io.File(localPath).getName(), localPath, callback);
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
     * @return a pointer to the upload state that can be passed to
     *         {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long uploadFile(Bucket bucket, String fileName, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        return uploadFile(bucket.getId(), fileName, localPath, callback);
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
     * @return a pointer to the upload state that can be passed to
     *         {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long uploadFile(String bucketId, String fileName, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        checkEnv();
        return _uploadFile(env, bucketId, fileName, localPath, callback);
    }

    /**
     * Uploads the specified list of files.
     * 
     * <p>
     * This will upload the files with the same name they have on the local storage.
     * </p>
     * 
     * <p>
     * This method will upload all files in parallel. The callback will be invoked
     * for all files. The order of invoking the callback is undetermined and most
     * likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} to upload the files to
     * @param localPaths
     *            the local paths (including file name) of the files to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @return an array of pointers to the upload state of each file that can be
     *         passed to {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long[] uploadFiles(Bucket bucket, String[] localPaths, UploadFileCallback callback) throws KeysNotFoundException {
        return uploadFiles(bucket.getId(), localPaths, callback);
    }

    /**
     * Uploads the specified list of files.
     * 
     * <p>
     * This will upload the files with the same name they have on the local storage.
     * </p>
     * 
     * <p>
     * This method will upload all files in parallel. The callback will be invoked
     * for all files. The order of invoking the callback is undetermined and most
     * likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket to upload the files to
     * @param localPaths
     *            the local paths (including file name) of the files to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @return an array of pointers to the upload state of each file that can be
     *         passed to {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long[] uploadFiles(String bucketId, String[] localPaths, UploadFileCallback callback) throws KeysNotFoundException {
        String[] fileNames = new String[localPaths.length];
        for (int i = 0; i < localPaths.length; i++) {
            fileNames[i] = new java.io.File(localPaths[i]).getName();
        }

        return uploadFiles(bucketId, fileNames, localPaths, callback);
    }

    /**
     * Uploads the specified list of files.
     * 
     * <p>
     * This allows uploading the files with a different name than the ones they have
     * on the local storage.
     * </p>
     * 
     * <p>
     * This method will upload all files in parallel. The callback will be invoked
     * for all files. The order of invoking the callback is undetermined and most
     * likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucket
     *            the {@link Bucket} to upload the files to
     * @param fileNames
     *            an array of names to assign to the uploaded files
     * @param localPaths
     *            the local paths (including file name) of the files to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @return an array of pointers to the upload state of each file that can be
     *         passed to {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long[] uploadFiles(Bucket bucket, String[] fileNames, String[] localPaths, UploadFileCallback callback) throws KeysNotFoundException {
        return uploadFiles(bucket.getId(), fileNames, localPaths, callback);
    }

    /**
     * Uploads the specified list of files.
     * 
     * <p>
     * This allows uploading the files with a different name than the ones they have
     * on the local storage.
     * </p>
     * 
     * <p>
     * This method will upload all files in parallel. The callback will be invoked
     * for all files. The order of invoking the callback is undetermined and most
     * likely will be different than the order of file ids in the list.
     * </p>
     * 
     * @param bucketId
     *            the id of the bucket to upload the files to
     * @param fileNames
     *            an array of names to assign to the uploaded files
     * @param localPaths
     *            the local paths (including file name) of the files to upload
     * @param callback
     *            an implementation of the {@link UploadFileCallback} interface to
     *            receive the upload progress
     * @return an array of pointers to the upload state of each file that can be
     *         passed to {@link #_cancelUpload(long)}
     * @throws KeysNotFoundException
     *             if the user's keys have not been imported yet
     * @see #cancelUpload(long)
     */
    public long[] uploadFiles(String bucketId, String[] fileNames, String[] localPaths, UploadFileCallback callback) throws KeysNotFoundException {
        checkEnv();

        long[] states = new long[fileNames.length];

        for (int i = 0; i < fileNames.length; i++) {
            states[i] = _uploadFile(env, bucketId, fileNames[i], localPaths[i], callback);
        }

        return states;
    }

    /**
     * Cancels the file upload for the specified upload state.
     * 
     * @param uploadState
     *            a pointer to the upload state returned by the
     *            <code>uploadFile()</code> methods
     * 
     * @return <code>true</code> if the upload was canceled successfully,
     *         <code>false</code> otherwise
     * 
     * @see #uploadFile(Bucket, String, UploadFileCallback)
     * @see #uploadFile(String, String, UploadFileCallback)
     * @see #uploadFile(Bucket, String, String, UploadFileCallback)
     * @see #uploadFile(String, String, String, UploadFileCallback)
     */
    public boolean cancelUpload(long uploadState) {
        return _cancelUpload(uploadState);
    }

    private String[] getBucketIds(Bucket[] buckets) {
        String[] bucketIds = new String[buckets.length];
        for (int i = 0; i < buckets.length; i++) {
            bucketIds[i] = buckets[i].getId();
        }
        return bucketIds;
    }

    private String[] getFileIds(File[] files) {
        String[] fileIds = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileIds[i] = files[i].getId();
        }
        return fileIds;
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

    private void checkEnv() throws KeysNotFoundException {
        checkKeys();
        if (env == 0) {
            env = initEnv(keys);
            startLooper();
        }
    }

    private long initEnv() throws KeysNotFoundException {
        return initEnv(new Keys(null, null, null));
    }

    private long initEnv(String user, String pass) {
        return initEnv(new Keys(user, pass, null));
    }

    private long initEnv(Keys keys) {
        long env = _initEnv(proto, host, port, keys.getUser(), keys.getPass(), keys.getMnemonic(), USER_AGENT, null,
                System.getenv("STORJ_CAINFO"));

        if (env == 0) {
            throw new IllegalStateException("Failed to initialize Storj environment");
        }

        return env;
    }

    private void destroyEnv(long env) {
        if (env != 0) {
            _destroyEnv(env);
        }
    }

    private void startLooper() {
        if (looper == null) {
            looper = new EventLoopRunner();
            looper.start();
        }
    }

    /**
     * Stops the event loop thread and releases the native resources allocated by
     * the native library.
     */
    public void destroy() {
        long _env = env;
        env = 0;
        destroyEnv(_env);
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
    }

    private static native String _getErrorMessage(int code);

    private native long _initEnv(String proto, String host, int port, String user, String pass, String mnemonic,
            String userAgent, String proxyUrl, String caInfoPath);

    private native void _destroyEnv(long env);

    private native void _runEventLoop(long env);

    private native void _getInfo(long env, GetInfoCallback callback);

    private native void _register(long env, RegisterCallback callback);

    private native Keys _exportKeys(String location, String passphrase);

    private native boolean _writeAuthFile(String location, String user, String pass, String mnemonic, String passphrase);

    private native void _getBuckets(long env, GetBucketsCallback callback);

    private native void _getBucket(long env, String bucketId, GetBucketCallback callback);

    private native void _getBucketId(long env, String bucketName, GetBucketIdCallback callback);

    private native void _createBucket(long env, String bucketName, CreateBucketCallback callback);

    private native void _deleteBucket(long env, String bucketId, DeleteBucketCallback callback);

    private native void _listFiles(long env, String bucketId, ListFilesCallback callback);

    private native void _getFile(long env, String bucketId, String fileId, GetFileCallback callback);

    private native void _getFileId(long env, String bucketId, String fileName, GetFileIdCallback callback);

    private native void _deleteFile(long env, String bucketId, String fileId, DeleteFileCallback callback);

    private native long _downloadFile(long env, String bucketId, String fileId, String path,
            DownloadFileCallback callback);

    private native boolean _cancelDownload(long downloadState);

    private native long _uploadFile(long env, String bucketId, String fileName, String localPath,
            UploadFileCallback callback);

    private native boolean _cancelUpload(long uploadState);


    private class EventLoopRunner extends Thread {

        @Override
        public void run() {
            while (env != 0) {
                _runEventLoop(env);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

    }

}
