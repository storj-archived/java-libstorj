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

import io.storj.libstorj.fs.Dir;
import io.storj.libstorj.fs.ListCallback;
import io.storj.libstorj.fs.StorjFS;

public class FileTreeExample {

    private static StorjFS fs;

    public static void main(String[] args) throws InterruptedException {
        Storj storj = new Storj();
        fs = new StorjFS(storj);

        printFileTree();

        storj.destroy();
    }

    private static void printFileTree() throws InterruptedException {
        printFileTree(null, 0);
    }

    private static void printFileTree(Dir dir, int indent) throws InterruptedException {
        Entry[] entries = list(dir);
        for (Entry entry : entries) {
            printEntry(entry, indent);
            if (entry instanceof Dir) {
                printFileTree((Dir) entry, indent + 1);
            }
        }
    }

    private static void printEntry(Entry entry, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
        System.out.println(entry.getSimpleName());
    }

    private static Entry[] list(Dir dir) throws InterruptedException {
        final Entry[][] result = new Entry[1][];
        
        final CountDownLatch latch = new CountDownLatch(1);
        fs.list(dir, new ListCallback() {
            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
                latch.countDown();
            }

            @Override
            public void onEntriesReceived(Entry[] entries) {
                result[0] = entries;
                latch.countDown();
            }
        });

        latch.await();

        return result[0];
    }

}
