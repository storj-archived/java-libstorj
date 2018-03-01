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

/**
 * Callback interface for receiving the progress of executing the
 * <code>downloadFile()</code> and <code>downloadFiles()</code> methods.
 * 
 * @see Storj#downloadFile(Bucket, File, DownloadFileCallback)
 * @see Storj#downloadFile(Bucket, File, String, DownloadFileCallback)
 * @see Storj#downloadFile(String, String, String, DownloadFileCallback)
 * @see Storj#downloadFiles(Bucket, File[], DownloadFileCallback)
 * @see Storj#downloadFiles(Bucket, File[], String[], DownloadFileCallback)
 * @see Storj#downloadFiles(String, String[], String[], DownloadFileCallback)
 */
public interface DownloadFileCallback {

    /**
     * Called when new progress is reported.
     * 
     * @param fileId
     *            the id of the file being downloaded
     * @param progress
     *            the current progress from <code>0</code> to <code>1</code>, e.g.
     *            <code>0.75</code> means 75% completed
     * @param downloadedBytes
     *            the number of bytes already downloaded
     * @param totalBytes
     *            the total bytes to be downloaded
     */
    void onProgress(String fileId, double progress, long downloadedBytes, long totalBytes);

    /**
     * Called if the file was downloaded successfully.
     * 
     * @param fileId
     *            the id of the downloaded file
     * @param localPath
     *            the local path (including file name) of the downloaded file
     */
    void onComplete(String fileId, String localPath);

    /**
     * Called if downloaded the file finished with error.
     * 
     * @param fileId
     *            the id of the file being downloaded
     * @param code
     *            the error code
     * @param message
     *            the error message
     */
    void onError(String fileId, int code, String message);

}
