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
 * Callback interface for receiving the response of the <code>getBucket()</code>
 * and <code>getBuckets()</code> methods.
 * 
 * <p>
 * If info is requested for multiple buckets then this callback will be invoked
 * once for each bucket.
 * </p>
 * 
 * @see Storj#getBucket(String, GetBucketCallback)
 * @see Storj#getBuckets(String[], GetBucketCallback)
 */
public interface GetBucketCallback {

    /**
     * Called if the bucket info was retrieved successfully.
     * 
     * @param bucket
     *            a {@link Bucket} object with the result
     */
    void onBucketReceived(Bucket bucket);

    /**
     * Called if getting info about the bucket finished with error.
     * 
     * @param bucketId
     *            the bucket id this error applies to
     * @param code
     *            the error code
     * @param message
     *            the error message
     */
    void onError(String bucketId, int code, String message);

}
