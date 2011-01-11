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
package com.meidusa.amoeba.data;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Used to track and report stats on the connection landscape.
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class ConMgrStats implements Cloneable
{

    /** The number of connection events since the server started up. */
    public AtomicLong connects = new AtomicLong(0);

    /** The number of disconnection events since the server started up. */
    public AtomicLong disconnects = new AtomicLong(0);

    @Override // from Object
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }
}
