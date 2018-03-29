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

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Objects;

@SuppressWarnings("serial")
public abstract class Entry implements Serializable, Comparable<Entry> {

    protected String id;
    protected String name;
    protected String created;
    protected boolean decrypted;

    /**
     * Constructs new Storj entry (bucket or file) with the provided metadata.
     * 
     * @param id
     *            the entry id
     * @param name
     *            the entry name
     * @param created
     *            the formatted UTC time of the entry creation
     * @param decrypted
     *            if the entry name is decrypted
     */
    public Entry(String id, String name, String created, boolean decrypted) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.decrypted = decrypted;
    }

    /**
     * Returns the entry id.
     * 
     * @return the entry id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the entry name. Check {@link #isDecrypted()} to see if the name has
     * been decrypted successfully.
     * 
     * @return the entry name
     * 
     * @see #isDecrypted()
     */
    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return Paths.get(name).getFileName().toString();
    }

    /**
     * Returns the when time the entry was created.
     * 
     * <p>
     * The returned value is a UTC time in the format of
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", e.g. "2016-03-04T17:01:02.629Z".
     * </p>
     * 
     * @return the formatted UTC time of the entry creation
     */
    public String getCreated() {
        return created;
    }

    /**
     * Checks if the entry name has been decrypted successfully with the
     * {@link Storj#importKeys(Keys, String) imported keys}.
     * 
     * <p>
     * If the entry name has been decrypted successfully then {@link #getName()}
     * will return the decrypted entry name. Otherwise {@link #getName()} will
     * return the encrypted entry name - the way it is stored in the Storj Bridge.
     * </p>
     * 
     * @return <code>true</code> if the entry name has been decrypted successfully,
     *         <code>false</code> otherwise
     * 
     * @see #getName()
     * @see Storj#importKeys(Keys, String)
     */
    public boolean isDecrypted() {
        return decrypted;
    }

    /**
     * The hash code value of the Entry object is the hash code value of its id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Two Entry objects are equal if their ids are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Entry)) {
            return false;
        }
        Entry entry = (Entry) o;
        return Objects.equals(id, entry.id);
    }

    /**
     * Two Entry objects are compared to each other by their entry names.
     */
    @Override
    public int compareTo(Entry other) {
        return name.compareTo(other.name);
    }

}
