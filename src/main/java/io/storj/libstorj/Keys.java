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
 * A class representing the keys required for accessing the Storj network:
 * <ul>
 * <li>user's email for authentication with the Storj Bridge
 * <li>user's password for authentication with the Storj Bridge
 * <li>mnemonic for encrypting and decrypting the transferred files
 * </ul>
 */
public class Keys {

    private String user;
    private String pass;
    private String mnemonic;

    /**
     * Constructs a new Keys object with the provided credentials.
     * 
     * @param user
     *            the user's email
     * @param pass
     *            the user's password
     * @param mnemonic
     *            a 12 or 24-word mnemonic
     */
    public Keys(String user, String pass, String mnemonic) {
        this.user = user;
        this.pass = pass;
        this.mnemonic = mnemonic;
    }

    /**
     * Returns the user's email.
     * 
     * @return an email address
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the user's password.
     * 
     * @return a password
     */
    public String getPass() {
        return pass;
    }

    /**
     * Returns the mnemonic for encrypting and decrypting files.
     * 
     * @return a 12 or 24-word mnemonic
     */
    public String getMnemonic() {
        return mnemonic;
    }
}
