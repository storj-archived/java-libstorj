package io.storj.libstorj;

import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class StorjTest {

    private Storj storj;
    private Bucket bucket;
    private File file;

    @Before
    public void setup() throws MalformedURLException {
        storj = new Storj("http://localhost:6382")
                .setDownloadDirectory(Paths.get(System.getProperty("java.io.tmpdir")));
        bucket = new Bucket("74b9ce6f3c25f772ccdaaf08", "test", null, true);
        file = new File("file-id", "file-name", null, true, 1, null, null, null, null);
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
    public void testUploadFile() {
        storj.uploadFile(bucket, Paths.get("/tmp/file-name"), new UploadFileCallback() {
            @Override
            public void onProgress(String filePath, double progress, long uploadedBytes, long totalBytes) {
            }

            @Override
            public void onError(String filePath, String message) {
                System.out.println(message);
            }

            @Override
            public void onComplete(String filePath, String fileId) {
                System.out.println(fileId);
            }
        });
    }

    @Test
    public void testDownloadFile() {
        storj.downloadFile(bucket, file, new DownloadFileCallback() {
            @Override
            public void onProgress(File file, double progress, long downloadedBytes, long totalBytes) {
            }

            @Override
            public void onComplete(File file, String localPath) {
                System.out.println(localPath);
            }

            @Override
            public void onError(File file, String message) {
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
