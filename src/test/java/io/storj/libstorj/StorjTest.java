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
            public void onError(String message) {
                System.out.println(message);
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
            public void onError(String message) {
                System.out.println(message);
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
            public void onError(String message) {
                System.out.println(message);
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
            public void onError(String message) {
                System.out.println(message);
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
            public void onError(String message) {
                System.out.println(message);
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
            public void onError(String message) {
                System.out.println(message);
            }
        });
    }

    @Test
    public void testGetFiles() {
        storj.getFile(bucket, file.getId(), new GetFileCallback() {
            @Override
            public void onFileReceived(File file) {
                System.out.println(file);
            }

            @Override
            public void onError(String message) {
                System.out.println(message);
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
            public void onError(String filePath, String message) {
                System.out.println(message);
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
            public void onError(String fileId, String message) {
                System.out.println(message);
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
            public void onError(String message) {
                System.out.println(message);
            }
        });
    }

}
