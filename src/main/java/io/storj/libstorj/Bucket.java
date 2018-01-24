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

import java.io.Serializable;
import java.util.Objects;

/**
 * A class representing a bucket in the Storj Bridge.
 */
@SuppressWarnings("serial")
public class Bucket implements Serializable, Comparable<Bucket> {

    private String id;
    private String name;
    private String created;
    private boolean decrypted;

    /**
     * Constructs new Bucket object with the provided metadata.
     * 
     * @param id
     *            the bucket id
     * @param name
     *            the bucket name
     * @param created
     *            the formatted UTC time of the bucket creation
     * @param decrypted
     *            if the bucket name is decrypted
     */
    public Bucket(String id, String name, String created, boolean decrypted) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.decrypted = decrypted;
    }

    /**
     * Returns the bucket id.
     * 
     * @return the bucket id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the bucket name. Check {@link #isDecrypted()} to see if the name has
     * been decrypted successfully.
     * 
     * @return the bucket name
     * 
     * @see #isDecrypted()
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the when time the bucket was created.
     * 
     * <p>
     * The returned value is a UTC time in the format of
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", e.g. "2016-03-04T17:01:02.629Z".
     * </p>
     * 
     * @return the formatted UTC time of the bucket creation
     */
    public String getCreated() {
        return created;
    }

    /**
     * Checks if the bucket name has been decrypted successfully with the
     * {@link Storj#importKeys(Keys, String) imported keys}.
     * 
     * <p>
     * If the bucket name has been decrypted successfully then {@link #getName()}
     * will return the decrypted bucket name. Otherwise {@link #getName()} will
     * return the encrypted bucket name - the way it is stored in the Storj Bridge.
     * </p>
     * 
     * @return <code>true</code> if the bucket name has been decrypted successfully,
     *         <code>false</code> otherwise
     * 
     * @see #getName()
     * @see Storj#importKeys(Keys, String)
     */
    public boolean isDecrypted() {
        return decrypted;
    }

    /**
     * The hash code value of the Bucket object is the hash code value of its id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Two Bucket objects are equal if their ids are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Bucket)) {
            return false;
        }
        Bucket bucket = (Bucket) o;
        return Objects.equals(id, bucket.id);
    }

    /**
     * Two Bucket objects are compared to each other by their bucket names.
     */
    @Override
    public int compareTo(Bucket other) {
        return name.compareTo(other.name);
    }
}
