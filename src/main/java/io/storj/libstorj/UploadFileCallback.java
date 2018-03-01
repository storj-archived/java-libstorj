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
 * <code>uploadFile()</code> and <code>uploadFiles()</code> methods.
 * 
 * @see Storj#uploadFile(Bucket, String, UploadFileCallback)
 * @see Storj#uploadFile(String, String, UploadFileCallback)
 * @see Storj#uploadFile(Bucket, String, String, UploadFileCallback)
 * @see Storj#uploadFile(String, String, String, UploadFileCallback)
 * @see Storj#uploadFiles(Bucket, String[], UploadFileCallback)
 * @see Storj#uploadFiles(String, String[], UploadFileCallback)
 * @see Storj#uploadFiles(Bucket, String[], String[], UploadFileCallback)
 * @see Storj#uploadFiles(String, String[], String[], UploadFileCallback)
 */
public interface UploadFileCallback {

    /**
     * Called when new progress is reported.
     * 
     * @param filePath
     *            the local path (including file name) of the file being uploaded
     * @param progress
     *            the current progress from <code>0</code> to <code>1</code>, e.g.
     *            <code>0.75</code> means 75% completed
     * @param uploadedBytes
     *            the number of bytes already uploaded
     * @param totalBytes
     *            the total bytes to be uploaded
     */
    void onProgress(String filePath, double progress, long uploadedBytes, long totalBytes);

    /**
     * Called if the file was uploaded successfully.
     * 
     * @param filePath
     *            the local path (including file name) of the uploaded file
     * @param file
     *            a {@link File} object of the uploaded file
     */
    void onComplete(String filePath, File file);

    /**
     * Called if uploaded the file finished with error.
     * 
     * @param filePath
     *            the local path (including file name) of the file being uploaded
     * @param code
     *            the error code
     * @param message
     *            the error message
     */
    void onError(String filePath, int code, String message);

}
