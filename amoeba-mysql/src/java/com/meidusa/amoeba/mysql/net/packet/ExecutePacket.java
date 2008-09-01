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
package com.meidusa.amoeba.mysql.net.packet;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.meidusa.amoeba.mysql.context.MysqlProxyRuntimeContext;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.util.StaticString;
import com.meidusa.amoeba.util.ThreadLocalMap;

/**
 * <pre>
  * Bytes                Name
 *  -----                ----
 *  1                    code
 *  4                    statement_id
 *  1                    flags
 *  4                    iteration_count
 *  (param_count+7)/8    null_bit_map
 *  1                    new_parameter_bound_flag 如果为1表示preparedStatment有参数绑定，否则则为0
 *  n*2                  type of parameters (only if new_params_bound = 1)
 *  
 *  code:          always COM_EXECUTE
 *  
 *  statement_id:  statement identifier
 *  
 *  flags:         reserved for future use. In MySQL 4.0, always 0.
 *                 In MySQL 5.0: 
 *                   0: CURSOR_TYPE_NO_CURSOR
 *                   1: CURSOR_TYPE_READ_ONLY
 *                   2: CURSOR_TYPE_FOR_UPDATE
 *                   4: CURSOR_TYPE_SCROLLABLE
 *  
 *  iteration_count: reserved for future use. Currently always 1.
 *  
 *  null_bit_map:  A bitmap indicating parameters that are NULL.
 *                 Bits are counted from LSB, using as many bytes
 *                 as necessary ((param_count+7)/8)
 *                 i.e. if the first parameter (parameter 0) is NULL, then
 *                 the least significant bit in the first byte will be 1.
 *  
 *  new_parameter_bound_flag:   Contains 1 if this is the first time
 *                              that "execute" has been called, or if
 *                              the parameters have been rebound.
 *  
 *  type:          Occurs once for each parameter that is not NULL.
 *                 The highest significant bit of this 16-bit value
 *                 encodes the unsigned property. The other 15 bits
 *                 are reserved for the type (only 8 currently used).
 *                 This block is sent when parameters have been rebound
 *                 or when a prepared statement is executed for the 
 *                 first time.
 * </pre>
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class ExecutePacket extends CommandPacket {
	public long statementId;
	public byte flags;
	public long iterationCount;
	public byte newParameterBoundFlag;
	protected transient int parameterCount;
	private BindValue[] values;
	private Map<Integer,Object> longPrameters;
	public ExecutePacket(int parameterCount,Map<Integer,Object> longPrameters){
		this.parameterCount = parameterCount;
		values = new BindValue[parameterCount];
		this.longPrameters = longPrameters;
	}
	
	public static long readStatmentID(byte[] buffer){
		byte[] b = buffer;
		int position = 5;
		return ((long) b[position++] & 0xff)
				| (((long) b[position++] & 0xff) << 8)
				| ((long) (b[position++] & 0xff) << 16)
				| ((long) (b[position++] & 0xff) << 24);
	}
	
	
	@Override
	public void init(AbstractPacketBuffer myBuffer){
		super.init(myBuffer);
		MysqlPacketBuffer buffer = (MysqlPacketBuffer)myBuffer;
		statementId = buffer.readLong();
		flags = buffer.readByte();
		iterationCount = buffer.readLong();
		int nullCount = (this.parameterCount + 7) / 8;
		byte[] nullBitsBuffer = new byte[nullCount];
		for(int i=0;i<nullCount;i++){
			nullBitsBuffer[i] = buffer.readByte();
		}
		
		newParameterBoundFlag = buffer.readByte();
		
		for (int i = 0; i < this.parameterCount; i++) {
			if(values[i] == null){
				values[i] = new BindValue();
			}
		}
		
		if(newParameterBoundFlag == (byte)1){
			for (int i = 0; i < this.parameterCount; i++) {
				this.values[i].bufferType = buffer.readInt();
			}
		}

		for (int i = 0; i < this.parameterCount; i++) {
			if(longPrameters != null && longPrameters.get(i) != null){
				values[i].isLongData = true;
			}else{
				if((nullBitsBuffer[i / 8] & (1 << (i & 7))) != 0){
					values[i].isNull = true;
				}else{
					readBindValue(buffer,values[i]);
				}
			}
		}
		
	}
	
	@Override
	public void write2Buffer(AbstractPacketBuffer myBuffer) throws UnsupportedEncodingException {
		super.write2Buffer(myBuffer);
		MysqlPacketBuffer buffer = (MysqlPacketBuffer)myBuffer;
		buffer.writeLong(statementId);
		buffer.writeByte(flags);
		buffer.writeLong(iterationCount);
		buffer.writeByte(newParameterBoundFlag);
		int nullCount = (this.parameterCount + 7) / 8;

		int nullBitsPosition = buffer.getPosition();

		for (int i = 0; i < nullCount; i++) {
			buffer.writeByte((byte) 0);
		}
		byte[] nullBitsBuffer = new byte[nullCount];
		
		if(newParameterBoundFlag == (byte)1){
			for (int i = 0; i < this.parameterCount; i++) {
				buffer.writeInt(this.values[i].bufferType);
			}
		}
		
		for (int i = 0; i < this.parameterCount; i++) {
			if (!this.values[i].isLongData) {
				if (!this.values[i].isNull) {
					storeBinding(buffer, this.values[i]);
				} else {
					nullBitsBuffer[i / 8] |= (1 << (i & 7));
				}
			}
		}
		
		int endPosition = buffer.getPosition();
		buffer.setPosition(nullBitsPosition);
		buffer.writeBytesNoNull(nullBitsBuffer);
		buffer.setPosition(endPosition);
	}
	
	private void readBindValue(MysqlPacketBuffer packet, BindValue bindValue) {

		//
		// Handle primitives first
		//
		switch (bindValue.bufferType) {

		case MysqlDefs.FIELD_TYPE_TINY:
			bindValue.byteBinding = packet.readByte();
			return;
		case MysqlDefs.FIELD_TYPE_SHORT:
			bindValue.shortBinding =  (short)packet.readInt();
			return;
		case MysqlDefs.FIELD_TYPE_LONG:
			bindValue.longBinding = packet.readLong();
			return;
		case MysqlDefs.FIELD_TYPE_LONGLONG:
			bindValue.longBinding = packet.readLongLong();
			return;
		case MysqlDefs.FIELD_TYPE_FLOAT:
			bindValue.floatBinding = packet.readFloat();
			return;
		case MysqlDefs.FIELD_TYPE_DOUBLE:
			bindValue.doubleBinding = packet.readDouble();
			return;
		case MysqlDefs.FIELD_TYPE_TIME:
			bindValue.value = readTime(packet);
			return;
		case MysqlDefs.FIELD_TYPE_DATE:
		case MysqlDefs.FIELD_TYPE_DATETIME:
		case MysqlDefs.FIELD_TYPE_TIMESTAMP:
			bindValue.value = readDate(packet);
			return;
		case MysqlDefs.FIELD_TYPE_VAR_STRING:
		case MysqlDefs.FIELD_TYPE_STRING:
		case MysqlDefs.FIELD_TYPE_VARCHAR:
			MysqlProxyRuntimeContext context = ((MysqlProxyRuntimeContext)MysqlProxyRuntimeContext.getInstance());
			String charset = context.getServerCharset();
			bindValue.value = packet.readLengthCodedString(charset);
		}
	}
	
	public Object[] getParameters(){
		Object[] result = new Object[values.length];
		int index = 0;
		for(BindValue bindValue: values){
			switch (bindValue.bufferType) {

			case MysqlDefs.FIELD_TYPE_TINY:
				result[index++] = bindValue.byteBinding;
				break;
			case MysqlDefs.FIELD_TYPE_SHORT:
				result[index++] =  bindValue.shortBinding;
				break;
			case MysqlDefs.FIELD_TYPE_LONG:
				result[index++] =  bindValue.longBinding;
				break;
			case MysqlDefs.FIELD_TYPE_LONGLONG:
				result[index++] = bindValue.longBinding;
				break;
			case MysqlDefs.FIELD_TYPE_FLOAT:
				result[index++] = bindValue.floatBinding;
				break;
			case MysqlDefs.FIELD_TYPE_DOUBLE:
				result[index++] = bindValue.doubleBinding;
				break;
			case MysqlDefs.FIELD_TYPE_TIME:
				result[index++] = bindValue.value;
				break;
			case MysqlDefs.FIELD_TYPE_DATE:
			case MysqlDefs.FIELD_TYPE_DATETIME:
			case MysqlDefs.FIELD_TYPE_TIMESTAMP:
				result[index++] = bindValue.value;
				break;
			case MysqlDefs.FIELD_TYPE_VAR_STRING:
			case MysqlDefs.FIELD_TYPE_STRING:
			case MysqlDefs.FIELD_TYPE_VARCHAR:
				result[index++] = bindValue.value;
				break;
			}
		}
		return result;
	}
	/**
	 * Method storeBinding.
	 * 
	 * @param packet
	 * @param bindValue
	 * @param mysql
	 *            DOCUMENT ME!
	 * 
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	private void storeBinding(MysqlPacketBuffer packet, BindValue bindValue){
			Object value = bindValue.value;

			//
			// Handle primitives first
			//
		switch (bindValue.bufferType) {

			case MysqlDefs.FIELD_TYPE_TINY:
				packet.writeByte(bindValue.byteBinding);
				return;
			case MysqlDefs.FIELD_TYPE_SHORT:
				packet.writeInt(bindValue.shortBinding);
				return;
			case MysqlDefs.FIELD_TYPE_LONG:
				packet.writeLong(bindValue.intBinding);
				return;
			case MysqlDefs.FIELD_TYPE_LONGLONG:
				packet.writeLongLong(bindValue.longBinding);
				return;
			case MysqlDefs.FIELD_TYPE_FLOAT:
				packet.writeFloat(bindValue.floatBinding);
				return;
			case MysqlDefs.FIELD_TYPE_DOUBLE:
				packet.writeDouble(bindValue.doubleBinding);
				return;
			case MysqlDefs.FIELD_TYPE_TIME:
				storeTime(packet, (Time) value);
				return;
			case MysqlDefs.FIELD_TYPE_DATE:
			case MysqlDefs.FIELD_TYPE_DATETIME:
			case MysqlDefs.FIELD_TYPE_TIMESTAMP:
				storeDateTime(packet, (java.util.Date) value);
				return;
			case MysqlDefs.FIELD_TYPE_VAR_STRING:
			case MysqlDefs.FIELD_TYPE_STRING:
			case MysqlDefs.FIELD_TYPE_VARCHAR:{
				if (value instanceof byte[]) {
					packet.writeLenBytes((byte[]) value);
				}else{
					packet.writeLenBytes(((String) value).getBytes());
				}
				return;
			}
		}
	}
			
	
	private void storeDateTime(MysqlPacketBuffer intoBuf, Date dt) {
		Calendar sessionCalendar = (Calendar)ThreadLocalMap.get(StaticString.CALENDAR);
		java.util.Date oldTime = sessionCalendar.getTime();
		try {
			sessionCalendar.setTime(dt);
			
			if (dt instanceof java.sql.Date) {
				sessionCalendar.set(Calendar.HOUR_OF_DAY, 0);
				sessionCalendar.set(Calendar.MINUTE, 0);
				sessionCalendar.set(Calendar.SECOND, 0);
			}

			byte length = (byte) 7;

			if (dt instanceof java.sql.Timestamp) {
				length = (byte) 11;
			}

			intoBuf.writeByte(length); // length

			int year = sessionCalendar.get(Calendar.YEAR);
			int month = sessionCalendar.get(Calendar.MONTH) + 1;
			int date = sessionCalendar.get(Calendar.DAY_OF_MONTH);
			
			intoBuf.writeInt(year);
			intoBuf.writeByte((byte) month);
			intoBuf.writeByte((byte) date);

			if (dt instanceof java.sql.Date) {
				intoBuf.writeByte((byte) 0);
				intoBuf.writeByte((byte) 0);
				intoBuf.writeByte((byte) 0);
			} else {
				intoBuf.writeByte((byte) sessionCalendar
						.get(Calendar.HOUR_OF_DAY));
				intoBuf.writeByte((byte) sessionCalendar
						.get(Calendar.MINUTE));
				intoBuf.writeByte((byte) sessionCalendar
						.get(Calendar.SECOND));
			}

			if (length == 11) {
				intoBuf.writeLong(((java.sql.Timestamp) dt).getNanos());
			}
		
		} finally {
			sessionCalendar.setTime(oldTime);
		}
	}

	private void storeTime(MysqlPacketBuffer intoBuf, Time tm){
		
		intoBuf.writeByte((byte) 8); // length
		intoBuf.writeByte((byte) 0); // neg flag
		intoBuf.writeLong(0); // tm->day, not used

		Calendar cal = (Calendar)ThreadLocalMap.get(StaticString.CALENDAR);
		
		synchronized (cal) {
			cal.setTime(tm);
			intoBuf.writeByte((byte) cal.get(Calendar.HOUR_OF_DAY));
			intoBuf.writeByte((byte) cal.get(Calendar.MINUTE));
			intoBuf.writeByte((byte) cal.get(Calendar.SECOND));

			// intoBuf.writeLongInt(0); // tm-second_part
		}
	}
	
	protected Time readTime(MysqlPacketBuffer intoBuf){
		intoBuf.readByte();
		intoBuf.readByte();
		intoBuf.readLong();
		int hour = intoBuf.readByte();
		int minute = intoBuf.readByte();
		int second = intoBuf.readByte();
		Calendar cal = (Calendar)ThreadLocalMap.get(StaticString.CALENDAR);
		cal.set(0, 0, 0, hour, minute, second);
		return new Time(cal.getTimeInMillis());
	}
	
	protected Date readDate(MysqlPacketBuffer intoBuf){
		byte length = intoBuf.readByte(); // length
		int year = intoBuf.readInt();
		byte month = intoBuf.readByte();
		byte date = intoBuf.readByte();
		int hour = intoBuf.readByte();
		int minute = intoBuf.readByte();
		int second = intoBuf.readByte();
		if (length == 11) {
			long nanos = intoBuf.readLong();
			Calendar cal = (Calendar)ThreadLocalMap.get(StaticString.CALENDAR);
			cal.set(year, month, date, hour, minute, second);
			Timestamp time = new Timestamp(cal.getTimeInMillis());
			time.setNanos((int)nanos);
			return time;
		}else{
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, date, hour, minute, second);
			return cal.getTime();
		}
	}
	
	
	
	
	protected int calculatePacketSize(){
		int packLength = super.calculatePacketSize();
		packLength += 4+1+4+1;
		return packLength;
	}
	
	public static void main(String[] args){
		int parameterCount = 12;
		int nullCount = (parameterCount + 7) / 8;
		byte[] nullBitsBuffer = new byte[nullCount];
		
		for (int i = 0; i < parameterCount; i++) {
			nullBitsBuffer[i / 8] |= (1 << (i & 7));
		}
		System.out.println(Arrays.toString(nullBitsBuffer));
	}
}
