/*
 * Copyright (C) 2017 Kaloyan Raev
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class Storj {

    static {
        loadLibrary();
    }

    /**
     * We need this in a separate method, so it can be mocked by JMockit and make
     * tests independent of native libraries.
     */
    private static void loadLibrary() {
        System.loadLibrary("storj-java");
    }

    private static final String DEFAULT_HOST = "api.storj.io";

    private static Storj instance;
    private static Path configDir;
    private static Path downloadDir;

    private String host;
    private Keys keys;

    private Storj() {
        host = DEFAULT_HOST;
        keys = null;
    }

    public static Storj getInstance() {
        if (instance == null) {
            instance = new Storj();
        }
        return instance;
    }

    public static void setConfigDirectory(Path dir) {
        configDir = dir;
    }

    public static void setDownloadDirectory(Path dir) {
        downloadDir = dir;
    }

    public static native long getTimestamp();

    public static native String generateMnemonic(int strength);

    public static native boolean checkMnemonic(String mnemonic);

    public void getInfo(GetInfoCallback callback) {
        _getInfo(callback);
    }

    public void register(String user, String pass, RegisterCallback callback) {
        _register(user, pass, callback);
    }

    public boolean keysExist() {
        return Files.exists(getAuthFile());
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

    public boolean deleteKeys() throws IOException {
        boolean success = Files.deleteIfExists(getAuthFile());
        if (success) {
            keys = null;
        }
        return success;
    }

    public boolean verifyKeys(String user, String pass) {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = { false };

        _getBuckets(user, pass, "", new GetBucketsCallback() {
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
        _getBuckets(keys.getUser(), keys.getPass(), keys.getMnemonic(), callback);
    }

    public void getBucket(String bucketId, GetBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _getBucket(keys.getUser(), keys.getPass(), keys.getMnemonic(), bucketId, callback);
    }

    public void createBucket(String bucketName, CreateBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _createBucket(keys.getUser(), keys.getPass(), keys.getMnemonic(), bucketName, callback);
    }

    public void deleteBucket(Bucket bucket, DeleteBucketCallback callback) throws KeysNotFoundException {
        checkKeys();
        _deleteBucket(keys.getUser(), keys.getPass(), keys.getMnemonic(), bucket.getId(), callback);
    }

    public void listFiles(Bucket bucket, ListFilesCallback callback) throws KeysNotFoundException {
        checkKeys();
        _listFiles(keys.getUser(), keys.getPass(), keys.getMnemonic(), bucket.getId(), callback);
    }

    public void deleteFile(Bucket bucket, File file, DeleteFileCallback callback) throws KeysNotFoundException {
        checkKeys();
        _deleteFile(keys.getUser(), keys.getPass(), keys.getMnemonic(), bucket.getId(), file.getId(), callback);
    }

    public void downloadFile(Bucket bucket, File file, DownloadFileCallback callback) throws KeysNotFoundException {
        checkDownloadDir();
        checkKeys();
        String path = downloadDir.resolve(file.getName()).toString();
        _downloadFile(bucket.getId(), file, path, keys.getUser(), keys.getPass(), keys.getMnemonic(), callback);
    }

    public void uploadFile(Bucket bucket, String filePath, UploadFileCallback callback) throws KeysNotFoundException {
        checkKeys();
        _uploadFile(bucket.getId(), filePath, keys.getUser(), keys.getPass(), keys.getMnemonic(), callback);
    }

    private Path getAuthFile() throws IllegalStateException {
        if (configDir == null) {
            throw new IllegalStateException("appDir is not set");
        }
        return configDir.resolve(host + ".json");
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

    private native void _getInfo(GetInfoCallback callback);

    private native void _register(String user, String pass, RegisterCallback callback);

    private native Keys _exportKeys(String location, String passphrase);

    private native boolean _writeAuthFile(String location, String user, String pass, String mnemonic, String passphrase);

    private native void _getBuckets(String user, String pass, String mnemonic, GetBucketsCallback callback);

    private native void _getBucket(String user, String pass, String mnemonic, String bucketId, GetBucketCallback callback);

    private native void _createBucket(String user, String pass, String mnemonic, String bucketName, CreateBucketCallback callback);

    private native void _deleteBucket(String user, String pass, String mnemonic, String bucketId, DeleteBucketCallback callback);

    private native void _listFiles(String user, String pass, String mnemonic, String bucketId, ListFilesCallback callback);

    private native void _deleteFile(String user, String pass, String mnemonic, String bucketId, String fileId, DeleteFileCallback callback);

    private native void _downloadFile(String bucketId, File file, String path, String user, String pass, String mnemonic, DownloadFileCallback callback);

    private native void _uploadFile(String bucketId, String filePath, String user, String pass, String mnemonic, UploadFileCallback callback);

}
