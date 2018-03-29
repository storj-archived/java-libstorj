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
package io.storj.libstorj.fs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.storj.libstorj.Bucket;
import io.storj.libstorj.Entry;
import io.storj.libstorj.File;
import io.storj.libstorj.GetBucketsCallback;
import io.storj.libstorj.ListFilesCallback;
import io.storj.libstorj.Storj;

public class StorjFS {

    private Storj storj;

    public StorjFS(Storj storj) {
        this.storj = storj;
    }

    public void list(ListCallback callback) {
        list(null, callback);
    }

    public void list(final Dir dir, final ListCallback callback) {
        if (dir == null) {
            listRoot(callback);
        } else {
            listDir(dir, callback);
        }
    }

    private void listRoot(final ListCallback callback) {
        storj.getBuckets(new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                Dir[] dirs = new Dir[buckets.length];
                for (int i = 0; i < buckets.length; i++) {
                    dirs[i] = new Dir(buckets[i]);
                }
                callback.onEntriesReceived(dirs);
            }

            @Override
            public void onError(int code, String message) {
                callback.onError(code, message);
            }
        });
    }

    private void listDir(final Dir dir, final ListCallback callback) {
        storj.listFiles(dir.bucketId, new ListFilesCallback() {
            @Override
            public void onFilesReceived(String bucketId, File[] files) {
                List<Entry> result = new ArrayList<>();
                Path dirPath = Paths.get(dir.getName());
                for (File file : files) {
                    Path filePath = Paths.get(file.getName());
                    boolean add = false;

                    if (dir.isBucket()) {
                        add = filePath.getNameCount() == 1;
                    } else {
                        add = filePath.startsWith(dirPath) && filePath.getNameCount() == dirPath.getNameCount() + 1;
                    }

                    if (add) {
                        if (file.isDirectory()) {
                            result.add(new Dir(file));
                        } else {
                            result.add(file);
                        }
                    }
                }
                // Second pass to look for missing dir objects
                for (File file : files) {
                    Path filePath = Paths.get(file.getName());
                    String dirName = null;

                    if (dir.isBucket()) {
                        if (filePath.getNameCount() > 1) {
                            dirName = filePath.getName(0).toString() + "/";
                            if (!containsDirName(result, dirName)) {
                                result.add(new Dir(dirName, file.getBucketId()));
                            }
                        }
                    } else if (filePath.startsWith(dirPath) && filePath.getNameCount() > dirPath.getNameCount() + 1) {
                        dirName = filePath.subpath(0, filePath.getNameCount() - 1).toString() + "/";
                    }

                    if (dirName != null && !containsDirName(result, dirName)) {
                        result.add(new Dir(dirName, file.getBucketId()));
                    }
                }
                callback.onEntriesReceived(result.toArray(new Entry[result.size()]));
            }

            @Override
            public void onError(String bucketId, int code, String message) {
                callback.onError(code, message);
            }
            
            private boolean containsDirName(List<Entry> list, String dirName) {
                for (Entry entry : list) {
                    if (entry.getName().equals(dirName)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

}
