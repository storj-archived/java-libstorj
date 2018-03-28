/*
 * Copyright (C) 2018 Kaloyan Raev
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

import java.util.concurrent.CountDownLatch;

public class FileTreeExample {

    private static Storj storj;

    public static void main(String[] args) throws InterruptedException {
        storj = new Storj();

        printFileTree();

        storj.destroy();
    }

    private static void printFileTree() throws InterruptedException {
        Bucket[] buckets = getBuckets();
        for (Bucket bucket : buckets) {
            System.out.println(bucket.getName());
            printFileTree(bucket);
        }
    }

    private static void printFileTree(Bucket bucket) throws InterruptedException {
        File[] files = getChildren(bucket);
        for (File f : files) {
            System.out.println("  " + f.getFileName());
            if (f.isDirectory()) {
                printFileTree(f, 2);
            }
        }
    }

    private static void printFileTree(File file, int indent) throws InterruptedException {
        File[] files = getChildren(file);
        for (File f : files) {
            for (int i = 0; i < indent; i++) {
                System.out.print("  ");
            }
            System.out.println(f.getFileName());
            if (f.isDirectory()) {
                printFileTree(f, indent + 1);
            }
        }
    }

    private static Bucket[] getBuckets() throws InterruptedException {
        final Bucket[][] result = new Bucket[1][];
        
        final CountDownLatch latch = new CountDownLatch(1);
        storj.getBuckets(new GetBucketsCallback() {
            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
                latch.countDown();
            }

            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                result[0] = buckets;
                latch.countDown();
            }
        });

        latch.await();

        return result[0];
    }

    private static File[] getChildren(Bucket bucket) throws InterruptedException {
        final File[][] result = new File[1][];

        final CountDownLatch latch = new CountDownLatch(1);
        storj.getChildren(bucket, new ListFilesCallback() {
            @Override
            public void onError(String bucketId, int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
                latch.countDown();
            }

            @Override
            public void onFilesReceived(String bucketId, File[] files) {
                result[0] = files;
                latch.countDown();
            }
        });

        latch.await();

        return result[0];
    }

    private static File[] getChildren(File file) throws InterruptedException {
        final File[][] result = new File[1][];

        final CountDownLatch latch = new CountDownLatch(1);
        storj.getChildren(file, new ListFilesCallback() {
            @Override
            public void onError(String bucketId, int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
                latch.countDown();
            }

            @Override
            public void onFilesReceived(String bucketId, File[] files) {
                result[0] = files;
                latch.countDown();
            }
        });

        latch.await();

        return result[0];
    }

}
