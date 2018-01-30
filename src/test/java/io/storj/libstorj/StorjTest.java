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

import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StorjTest {

    private Storj storj;
    private Bucket bucket;
    private File file;

    @Before
    public void setup() throws MalformedURLException {
        storj = new Storj("http://localhost:6382")
                .setDownloadDirectory(new java.io.File(System.getProperty("java.io.tmpdir")));
        bucket = new Bucket("74b9ce6f3c25f772ccdaaf08", "test", null, true);
        file = new File("file-id", "74b9ce6f3c25f772ccdaaf08", "file-name", null, true, 1, null, null, null, null);
    }

    @Test
    public void testTimestamp() {
        assert(Storj.getTimestamp() > 0);
    }

    @Test
    public void testGetInfo() {
        storj.getInfo(new GetInfoCallback() {
            @Override
            public void onInfoReceived(String title, String description, String version, String host) {
                System.out.println("Title: " + title);
                System.out.println("Description: " + description);
                System.out.println("Version: " + version);
                System.out.println("Host: " + host);
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testGetBuckets() {
        storj.getBuckets(new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                System.out.println(buckets);
            }
            
            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testGetBucket() {
        storj.getBucket("74b9ce6f3c25f772ccdaaf08", new GetBucketCallback() {
            @Override
            public void onBucketReceived(Bucket bucket) {
                System.out.println(bucket);
            }
            
            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testGetBucketId() {
        storj.getBucketId("test", new GetBucketIdCallback() {
            @Override
            public void onBucketIdReceived(String bucketId) {
                System.out.println(bucketId);
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testCreateBucket() {
        storj.createBucket("test", new CreateBucketCallback() {
            @Override
            public void onBucketCreated(Bucket bucket) {
                System.out.println(bucket);
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testDeleteBucket() {
        storj.deleteBucket(bucket, new DeleteBucketCallback() {
            @Override
            public void onBucketDeleted() {
                System.out.println("Bucket deleted");
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testListFiles() {
        storj.listFiles(bucket, new ListFilesCallback() {
            @Override
            public void onFilesReceived(File[] files) {
                System.out.println(files);
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testGetFile() {
        storj.getFile(bucket, file.getId(), new GetFileCallback() {
            @Override
            public void onFileReceived(File file) {
                System.out.println(file);
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testGetFileId() {
        storj.getFileId(bucket, "file-name", new GetFileIdCallback() {
            @Override
            public void onFileIdReceived(String fileId) {
                System.out.println(fileId);
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testUploadFile() {
        storj.uploadFile(bucket, "/tmp/file-name", new UploadFileCallback() {
            @Override
            public void onProgress(String filePath, double progress, long uploadedBytes, long totalBytes) {
            }

            @Override
            public void onError(String filePath, int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }

            @Override
            public void onComplete(String filePath, File file) {
                System.out.println(file.getId());
            }
        });
    }

    @Test
    public void testDownloadFile() {
        storj.downloadFile(bucket, file, new DownloadFileCallback() {
            @Override
            public void onProgress(String fileId, double progress, long downloadedBytes, long totalBytes) {
            }

            @Override
            public void onComplete(String fileId, String localPath) {
                System.out.println(localPath);
            }

            @Override
            public void onError(String fileId, int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testDeleteFile() {
        storj.deleteFile(bucket, file, new DeleteFileCallback() {
            @Override
            public void onFileDeleted() {
                System.out.println("File deleted");
            }

            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }
        });
    }

    @Test
    public void testVerifyCredentials() throws InterruptedException {
        storj.verifyKeys("myuser", "mypass");
    }

    @Test
    public void testVerifyKeys() throws InterruptedException {
        storj.verifyKeys(new Keys("myuser", "mypass", "mymnemonic"));
    }

    @Test
    public void testGetErrorMessage() {
        Assert.assertEquals("No such file or directory",
                Storj.getErrorMessage(Storj.ENOENT));
        Assert.assertEquals("Permission denied",
                Storj.getErrorMessage(Storj.EACCES));
        Assert.assertEquals("No errors",
                Storj.getErrorMessage(Storj.NO_ERROR));
        Assert.assertEquals("Unsupported protocol",
                Storj.getErrorMessage(Storj.CURLE_UNSUPPORTED_PROTOCOL));
        Assert.assertEquals("URL using bad/illegal format or missing URL",
                Storj.getErrorMessage(Storj.CURLE_URL_MALFORMAT));
        Assert.assertEquals("Couldn't resolve proxy name",
                Storj.getErrorMessage(Storj.CURLE_COULDNT_RESOLVE_PROXY));
        Assert.assertEquals("Couldn't resolve host name",
                Storj.getErrorMessage(Storj.CURLE_COULDNT_RESOLVE_HOST));
        Assert.assertEquals("Couldn't connect to server",
                Storj.getErrorMessage(Storj.CURLE_COULDNT_CONNECT));
        Assert.assertEquals("Out of memory",
                Storj.getErrorMessage(Storj.CURLE_OUT_OF_MEMORY));
        Assert.assertEquals("Timeout was reached",
                Storj.getErrorMessage(Storj.CURLE_OPERATION_TIMEDOUT));
        Assert.assertEquals("Bad Request",
                Storj.getErrorMessage(Storj.HTTP_BAD_REQUEST));
        Assert.assertEquals("Unauthorized",
                Storj.getErrorMessage(Storj.HTTP_UNAUTHORIZED));
        Assert.assertEquals("Not Found",
                Storj.getErrorMessage(Storj.HTTP_NOT_FOUND));
        Assert.assertEquals("Conflict",
                Storj.getErrorMessage(Storj.HTTP_CONFLICT));
        Assert.assertEquals("Transfer Rate Limit Reached",
                Storj.getErrorMessage(Storj.HTTP_TRANSFER_RATE_LIMIT));
        Assert.assertEquals("Too Many Requests",
                Storj.getErrorMessage(Storj.HTTP_TOO_MANY_REQUESTS));
        Assert.assertEquals("Internal Server Error",
                Storj.getErrorMessage(Storj.HTTP_INTERNAL_SERVER_ERROR));
        Assert.assertEquals("Service Unavailable",
                Storj.getErrorMessage(Storj.HTTP_SERVICE_UNAVAILABLE));
        Assert.assertEquals("Failed to initialize Storj environment",
                Storj.getErrorMessage(Storj.STORJ_ENV_INIT_ERROR));
        Assert.assertEquals("Bridge request error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_REQUEST_ERROR));
        Assert.assertEquals("Bridge request authorization error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_AUTH_ERROR));
        Assert.assertEquals("Bridge request token error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_TOKEN_ERROR));
        Assert.assertEquals("Bridge request timeout error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_TIMEOUT_ERROR));
        Assert.assertEquals("Bridge request internal error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_INTERNAL_ERROR));
        Assert.assertEquals("Bridge rate limit error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_RATE_ERROR));
        Assert.assertEquals("Bucket is not found",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_BUCKET_NOTFOUND_ERROR));
        Assert.assertEquals("File is not found",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_FILE_NOTFOUND_ERROR));
        Assert.assertEquals("Unexpected JSON response",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_JSON_ERROR));
        Assert.assertEquals("Bridge frame request error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_FRAME_ERROR));
        Assert.assertEquals("Bridge request pointer error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_POINTER_ERROR));
        Assert.assertEquals("Bridge request replace pointer error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_REPOINTER_ERROR));
        Assert.assertEquals("Bridge file info error",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_FILEINFO_ERROR));
        Assert.assertEquals("File already exists",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_BUCKET_FILE_EXISTS));
        Assert.assertEquals("Unable to receive storage offer",
                Storj.getErrorMessage(Storj.STORJ_BRIDGE_OFFER_ERROR));
        Assert.assertEquals("File integrity error",
                Storj.getErrorMessage(Storj.STORJ_FILE_INTEGRITY_ERROR));
        Assert.assertEquals("File write error",
                Storj.getErrorMessage(Storj.STORJ_FILE_WRITE_ERROR));
        Assert.assertEquals("File encryption error",
                Storj.getErrorMessage(Storj.STORJ_FILE_ENCRYPTION_ERROR));
        Assert.assertEquals("File size error",
                Storj.getErrorMessage(Storj.STORJ_FILE_SIZE_ERROR));
        Assert.assertEquals("File decryption error",
                Storj.getErrorMessage(Storj.STORJ_FILE_DECRYPTION_ERROR));
        Assert.assertEquals("File hmac generation error",
                Storj.getErrorMessage(Storj.STORJ_FILE_GENERATE_HMAC_ERROR));
        Assert.assertEquals("File read error",
                Storj.getErrorMessage(Storj.STORJ_FILE_READ_ERROR));
        Assert.assertEquals("File missing shard error",
                Storj.getErrorMessage(Storj.STORJ_FILE_SHARD_MISSING_ERROR));
        Assert.assertEquals("File recover error",
                Storj.getErrorMessage(Storj.STORJ_FILE_RECOVER_ERROR));
        Assert.assertEquals("File resize error",
                Storj.getErrorMessage(Storj.STORJ_FILE_RESIZE_ERROR));
        Assert.assertEquals("File unsupported erasure code error",
                Storj.getErrorMessage(Storj.STORJ_FILE_UNSUPPORTED_ERASURE));
        Assert.assertEquals("File create parity error",
                Storj.getErrorMessage(Storj.STORJ_FILE_PARITY_ERROR));
        Assert.assertEquals("Memory error",
                Storj.getErrorMessage(Storj.STORJ_MEMORY_ERROR));
        Assert.assertEquals("Memory mapped file error",
                Storj.getErrorMessage(Storj.STORJ_MAPPING_ERROR));
        Assert.assertEquals("Memory mapped file unmap error",
                Storj.getErrorMessage(Storj.STORJ_UNMAPPING_ERROR));
        Assert.assertEquals("Queue error",
                Storj.getErrorMessage(Storj.STORJ_QUEUE_ERROR));
        Assert.assertEquals("Meta encryption error",
                Storj.getErrorMessage(Storj.STORJ_META_ENCRYPTION_ERROR));
        Assert.assertEquals("Meta decryption error",
                Storj.getErrorMessage(Storj.STORJ_META_DECRYPTION_ERROR));
        Assert.assertEquals("Unable to decode hex string",
                Storj.getErrorMessage(Storj.STORJ_HEX_DECODE_ERROR));

    }

}
