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

public class SampleApp {

    public static void main(String[] args) {
        Storj storj = new Storj();
        storj.getBuckets(new GetBucketsCallback() {
            @Override
            public void onError(int code, String message) {
                System.out.printf("[%d] %s\n", code, message);
            }

            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                for (Bucket bucket : buckets) {
                    System.out.printf("%s %s\n", bucket.getId(), bucket.getName());
                }
            }
        });
    }

}
