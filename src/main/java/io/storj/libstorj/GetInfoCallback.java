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
 * Callback interface for receiving the response from the <code>getInfo()</code>
 * method.
 * 
 * @see Storj#getInfo(GetInfoCallback)
 */
public interface GetInfoCallback {

    /**
     * Called when the {@link Storj#getInfo(GetInfoCallback)} method finishes
     * successfully.
     * 
     * @param title
     *            the title of the Bridge API server
     * @param description
     *            the description of the Bridge API server
     * @param version
     *            the version of the Bridge API server
     * @param host
     *            the host the Bridge API server is listening for requests at
     */
    void onInfoReceived(String title, String description, String version, String host);

    /**
     * Called when the {@link Storj#getInfo(GetInfoCallback)} method finishes with
     * error.
     * 
     * @param code
     *            the error code
     * @param message
     *            the error message
     */
    void onError(int code, String message);

}
