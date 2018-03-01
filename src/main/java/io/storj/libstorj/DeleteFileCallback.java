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
 * Callback interface for receiving the response from the
 * <code>deleteFile()</code> and <code>deleteFiles()</code> methods.
 * 
 * @see Storj#deleteFile(Bucket, File, DeleteFileCallback)
 * @see Storj#deleteFile(String, String, DeleteFileCallback)
 * @see Storj#deleteFiles(Bucket, File[], DeleteFileCallback)
 * @see Storj#deleteFiles(String, String[], DeleteFileCallback)
 */
public interface DeleteFileCallback {

    /**
     * Called if the file was deleted successfully.
     * 
     * @param fileId
     *            the id of the file that was deleted
     */
    void onFileDeleted(String fileId);

    /**
     * Called if deleting the file finished with error.
     * 
     * @param fileId
     *            the file id this error applies to
     * @param code
     *            the error code
     * @param message
     *            the error message
     */
    void onError(String fileId, int code, String message);

}
