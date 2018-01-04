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

public class Storj {

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

    public Storj() {
        this(DEFAULT_PROTO, DEFAULT_HOST, DEFAULT_PORT);
    }

    public Storj(String bridgeUrl) throws MalformedURLException {
        this(new URL(bridgeUrl));
    }

    public Storj(URL bridgeUrl) {
        this(bridgeUrl.getProtocol(), bridgeUrl.getHost(), bridgeUrl.getPort());
    }

    private Storj(String bridgeProtocol, String bridgeHost, int bridgePort) {
        proto = bridgeProtocol;
        host = bridgeHost;
        port = bridgePort;

        configDir = new java.io.File(System.getProperty("user.home"), ".storj");
        downloadDir = new java.io.File(System.getProperty("user.dir"));
    }

    public Storj setConfigDirectory(java.io.File dir) {
        configDir = dir;
        return this;
    }

    public Storj setDownloadDirectory(java.io.File dir) {
        downloadDir = dir;
        return this;
    }

    public static native long getTimestamp();

    public static native String generateMnemonic(int strength);

    public static native boolean checkMnemonic(String mnemonic);

    public void getInfo(GetInfoCallback callback) {
        _getInfo(new Environment(), callback);
    }

    public void register(String user, String pass, RegisterCallback callback) {
        _register(new Environment(user, pass), callback);
    }

    public boolean keysExist() {
        return getAuthFile().exists();
    }

    public Keys getKeys(String passphrase) {
        if (keys == null) {
            keys = _exportKeys(getAuthFile().toString(), passphrase);
        }
        return keys;
    }

    /**
     *
     * @param keys
     * @param passphrase
     * @return <code>true</code> if importing keys was successful, <code>false</code> otherwise
     */
    public boolean importKeys(Keys keys, String passphrase) {
        boolean success = _writeAuthFile(getAuthFile().toString(), keys.getUser(), keys.getPass(), keys.getMnemonic(),
                passphrase);
        if (success) {
            this.keys = keys;
        }
        return success;
    }

    public boolean deleteKeys() {
        boolean success = getAuthFile().delete();
        if (success) {
            keys = null;
        }
        return success;
    }

    public boolean verifyKeys(String user, String pass) {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = { false };

        _getBuckets(new Environment(user, pass), new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                result[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                // TODO better error handling to determine if error is due to authentication error or network error
                result[0] = false;
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            // TODO better error handling
            result[0] = false;
        }

        return result[0];
    }

    public void getBuckets(GetBucketsCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getBuckets(new Environment(), callback);
    }

    public void getBucket(String bucketId, GetBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getBucket(new Environment(), bucketId, callback);
    }

    public void createBucket(String bucketName, CreateBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _createBucket(new Environment(), bucketName, callback);
    }

    public void deleteBucket(Bucket bucket, DeleteBucketCallback callback) throws KeysNotFoundException {
        deleteBucket(bucket.getId(), callback);
    }

    public void deleteBucket(String bucketId, DeleteBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _deleteBucket(new Environment(), bucketId, callback);
    }

    public void listFiles(Bucket bucket, ListFilesCallback callback) throws KeysNotFoundException {
        listFiles(bucket.getId(), callback);
    }

    public void listFiles(String bucketId, ListFilesCallback callback) throws KeysNotFoundException {
        checkKeys();
        _listFiles(new Environment(), bucketId, callback);
    }

    public void getFile(Bucket bucket, File file, GetFileCallback callback) throws KeysNotFoundException {
        getFile(bucket.getId(), file.getId(), callback);
    }

    public void getFile(String bucketId, String fileId, GetFileCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getFile(new Environment(), bucketId, fileId, callback);
    }

    public void deleteFile(Bucket bucket, File file, DeleteFileCallback callback) throws KeysNotFoundException {
        deleteFile(bucket.getId(), file.getId(), callback);
    }

    public void deleteFile(String bucketId, String fileId, DeleteFileCallback callback) throws KeysNotFoundException {
        checkKeys();
        _deleteFile(new Environment(), bucketId, fileId, callback);
    }

    public void downloadFile(Bucket bucket, File file, DownloadFileCallback callback) throws KeysNotFoundException {
        checkDownloadDir();
        String localPath = new java.io.File(downloadDir, file.getName()).getPath();
        downloadFile(bucket, file, localPath, callback);
    }

    public void downloadFile(Bucket bucket, File file, String localPath, DownloadFileCallback callback)
            throws KeysNotFoundException {
        downloadFile(bucket.getId(), file.getId(), localPath, callback);
    }

    public void downloadFile(String bucketId, String fileId, String localPath, DownloadFileCallback callback)
            throws KeysNotFoundException {
        checkKeys();
        _downloadFile(new Environment(), bucketId, fileId, localPath, callback);
    }

    public void uploadFile(Bucket bucket, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        uploadFile(bucket.getId(), localPath, callback);
    }

    public void uploadFile(String bucketId, String localPath, UploadFileCallback callback)
            throws KeysNotFoundException {
        uploadFile(bucketId, new java.io.File(localPath).getName(), localPath, callback);
    }

    public void uploadFile(Bucket bucket, String fileName, String localPath, UploadFileCallback callback) throws KeysNotFoundException {
        uploadFile(bucket.getId(), fileName, localPath, callback);
    }

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

    private native void _getInfo(Environment env, GetInfoCallback callback);

    private native void _register(Environment env, RegisterCallback callback);

    private native Keys _exportKeys(String location, String passphrase);

    private native boolean _writeAuthFile(String location, String user, String pass, String mnemonic, String passphrase);

    private native void _getBuckets(Environment env, GetBucketsCallback callback);

    private native void _getBucket(Environment env, String bucketId, GetBucketCallback callback);

    private native void _createBucket(Environment env, String bucketName, CreateBucketCallback callback);

    private native void _deleteBucket(Environment env, String bucketId, DeleteBucketCallback callback);

    private native void _listFiles(Environment env, String bucketId, ListFilesCallback callback);

    private native void _getFile(Environment env, String bucketId, String fileId, GetFileCallback callback);

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
