/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mysql.net;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandInfo{
		private byte[] buffer;
		private final AtomicInteger completedCount = new AtomicInteger(0);
		private boolean main;
		private int statusCode;
		private Runnable runnable;
		private boolean isMerged = false;
		public boolean isMerged() {
			return isMerged;
		}

		public void setMerged(boolean isMerged) {
			this.isMerged = isMerged;
		}

		public byte[] getBuffer() {
			return buffer;
		}

		public void setBuffer(byte[] buffer) {
			this.buffer = buffer;
		}

		public boolean isMain() {
			return main;
		}

		public void setMain(boolean main) {
			this.main = main;
		}

		public Runnable getRunnable() {
			return runnable;
		}

		public void setRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		public AtomicInteger getCompletedCount() {
			return completedCount;
		}

		public boolean equals(Object object){
			if(object instanceof CommandInfo){
				CommandInfo info = (CommandInfo)object;
				return Arrays.equals(buffer, info.buffer);
			}
			return false;
		}
		
		public int hashCode(){
			return 37 + buffer.hashCode();
		}

		public int getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
	}