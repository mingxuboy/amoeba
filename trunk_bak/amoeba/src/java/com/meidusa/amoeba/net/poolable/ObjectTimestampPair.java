/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.net.poolable;

public class ObjectTimestampPair implements Comparable<ObjectTimestampPair> {
        Object value;
        long tstamp;

        ObjectTimestampPair(Object val) {
            this(val, System.currentTimeMillis());
        }

        ObjectTimestampPair(Object val, long time) {
            value = val;
            tstamp = time;
        }

        public String toString() {
            return value + ";" + tstamp;
        }

        public int compareTo(ObjectTimestampPair other) {
            final long tstampdiff = this.tstamp - other.tstamp;
            if (tstampdiff == 0) {
                // make sure the natural ordering is consistent with equals
                // see java.lang.Comparable Javadocs
                return System.identityHashCode(this) - System.identityHashCode(other);
            } else {
                // handle int overflow
                return (int)Math.min(Math.max(tstampdiff, Integer.MIN_VALUE), Integer.MAX_VALUE);
            }
        }
    }