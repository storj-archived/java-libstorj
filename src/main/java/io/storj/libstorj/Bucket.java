/*
 * Copyright (C) 2017 Kaloyan Raev
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

@SuppressWarnings("serial")
public class Bucket implements Serializable, Comparable<Bucket> {

    private String id;
    private String name;
    private String created;
    private boolean decrypted;

    public Bucket(String id, String name, String created, boolean decrypted) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.decrypted = decrypted;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreated() {
        return created;
    }

    public boolean isDecrypted() {
        return decrypted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Bucket)) {
            return false;
        }
        Bucket bucket = (Bucket) o;
        return Objects.equals(id, bucket.id);
    }

    @Override
    public int compareTo(Bucket other) {
        return name.compareTo(other.name);
    }
}
