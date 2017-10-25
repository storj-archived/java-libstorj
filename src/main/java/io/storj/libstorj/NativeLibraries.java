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

import java.util.HashMap;
import java.util.Map;

public class NativeLibraries {

    // Used to load the 'storj-java' library on application startup.
    static {
        System.loadLibrary("storj-java");
    }

    public static Map<String, String> getVersions() {
        Map<String, String> map = new HashMap<>();
        map.put("JSON-C", getJsonCVersion());
        map.put("cURL", getCurlVersion());
        map.put("libuv", getLibuvVersion());
        map.put("Nettle", getNettleVersion());
//        map.put("Microhttpd", getMHDVersion());
        return map;
    }

    public static native String getJsonCVersion();
    public static native String getCurlVersion();
    public static native String getLibuvVersion();
    public static native String getNettleVersion();
    public static native String getMHDVersion();

}
