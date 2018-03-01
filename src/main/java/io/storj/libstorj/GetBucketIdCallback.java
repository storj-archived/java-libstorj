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

/**
 * Callback interface for receiving the response of the
 * <code>getBucketId()</code> and <code>getBucketIds()</code> methods.
 * 
 * @see Storj#getBucketId(String, GetBucketIdCallback)
 * @see Storj#getBucketIds(String[], GetBucketIdCallback)
 */
public interface GetBucketIdCallback {

    /**
     * Called if the bucket id was retrieved successfully.
     * 
     * @param bucketName
     *            the bucket name the received bucket id applies to
     * @param bucketId
     *            the received bucket id
     */
    void onBucketIdReceived(String bucketName, String bucketId);

    /**
     * Called if getting the bucket id finished with error.
     * 
     * @param bucketName
     *            the bucket name this error applies to
     * @param code
     *            the error code
     * @param message
     *            the error message
     */
    void onError(String bucketName, int code, String message);

}
