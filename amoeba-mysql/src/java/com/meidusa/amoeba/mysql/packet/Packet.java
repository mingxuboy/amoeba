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
package com.meidusa.amoeba.mysql.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.mysql.io.MySqlPacketConstant;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public interface Packet extends MySqlPacketConstant,com.meidusa.amoeba.packet.Packet{
	
	/**
	 * 从buffer(含包头) 中初始化数据包。
	 * @param buffer buffer是从mysql socketChannel的流读取头4个字节计算数据包长度
	 * 				并且读取相应的长度所形成的buffer
	 */
	public void init(PacketBuffer buffer);
	
	/**
	 * 直接转化成buffer对象
	 * @return Buffer 对象
	 * @throws UnsupportedEncodingException  当String to bytes发生编码不支持的时候
	 */
	public PacketBuffer toBuffer()throws UnsupportedEncodingException;
	
	/**
	 * 将该packet写入到buffer中 
	 * @param buffer 用于输入输出的缓冲
	 * @throws UnsupportedEncodingException 当String to bytes发生编码不支持的时候
	 */
	public PacketBuffer toBuffer(PacketBuffer buffer)throws UnsupportedEncodingException;
}
