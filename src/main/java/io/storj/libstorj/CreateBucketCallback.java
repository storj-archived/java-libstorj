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
 * <code>createBucket()</code> method.
 * 
 * @see Storj#createBucket(String, CreateBucketCallback)
 */
public interface CreateBucketCallback {

    /**
     * Called if the bucket was created successfully.
     * 
     * @param bucket
     *            a {@link Bucket} object with the newly created bucket
     */
    void onBucketCreated(Bucket bucket);

    /**
     * Called if creating the bucket finished with error.
     * 
     * @param message
     *            the error message
     */
    void onError(String message);

}
