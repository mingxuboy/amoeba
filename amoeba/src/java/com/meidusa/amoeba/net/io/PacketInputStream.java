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
package com.meidusa.amoeba.net.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * 
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class PacketInputStream extends InputStream
{
	
	/**
     * 用于读取可读通道的缓存对象，初始化容量={@link #INITIAL_BUFFER_CAPACITY}，不够时候每次增长上一次的一倍
     */
    protected ByteBuffer _buffer;

    /** 整个封包长度，包括包头 */
    protected int _length = -1;

    /**
     * 当前buffer 中的字节长度
     */
    protected int _have = 0;

    /**
     * 初始化buffer 容量,默认32字节
     */
    protected static final int INITIAL_BUFFER_CAPACITY = 32;

    /** 最大容量 */
    protected static final int MAX_BUFFER_CAPACITY = 512 * 1024;
    
    /**
     * Creates a new framed input stream.
     */
    public PacketInputStream ()
    {
        _buffer = ByteBuffer.allocate(INITIAL_BUFFER_CAPACITY);
    }

    /**
     * Reads a packet from the provided channel, appending to any partially
     * read packet. If the entire packet data is not yet available,
     * <code>readPacket</code> will return false, otherwise true.
     *
     * <p> <em>Note:</em> when this method returns true, it is required
     * that the caller read <em>all</em> of the packet data from the stream
     * before again calling {@link #readPacket} as the previous packet's
     * data will be elimitated upon the subsequent call.
     *
     * @return true if the entire packet has been read, false if the buffer
     * contains only a partial packet.
     */
    public boolean readPacket (ReadableByteChannel source)
        throws IOException
    {
        // flush data from any previous frame from the buffer
        if (_buffer.limit() == _length) {
            // this will remove the old frame's bytes from the buffer,
            // shift our old data to the start of the buffer, position the
            // buffer appropriately for appending new data onto the end of
            // our existing data, and set the limit to the capacity
        	_buffer.limit(_have);
            _buffer.position(_length);
            _buffer.compact();
            _have -= _length;
            if(_have < 0){
            	_have = 0;
            }
            // we may have picked up the next frame in a previous read, so
            // try decoding the length straight away
            _length = decodeLength();
        }

        // we may already have the next frame entirely in the buffer from
        // a previous read
        if (checkForCompletePacket()) {
            return true;
        }

        // read whatever data we can from the source
        do {
            int got = source.read(_buffer);
            if (got == -1) {
                throw new EOFException();
            }
            _have += got;

            if (_length == -1) {
                // if we didn't already have our length, see if we now
                // have enough data to obtain it
                _length = decodeLength();
            }
            
            if(_length < -1){
            	throw new IOException("decodeLength error:_length="+_length);
            }

            // if there's room remaining in the buffer, that means we've
            // read all there is to read, so we can move on to inspecting
            // what we've got
            if (_buffer.remaining() > 0) {
                break;
            }

            // additionally, if the buffer happened to be exactly as long
            // as we needed, we need to break as well
            if ((_length > 0) && (_have >= _length)) {
                break;
            }

            // otherwise, we've filled up our buffer as a result of this
            // read, expand it and try reading some more
            int newSize = _buffer.capacity() << 1;
            newSize = newSize>_length ? newSize:_length+16;
            ByteBuffer newbuf = ByteBuffer.allocate(newSize);
            newbuf.put((ByteBuffer)_buffer.flip());
            _buffer = newbuf;

            // don't let things grow without bounds
        } while (_buffer.capacity() < MAX_BUFFER_CAPACITY);

        // finally check to see if there's a complete frame in the buffer
        // and prepare to serve it up if there is
        return checkForCompletePacket();
    }

    /**
     * Decodes and returns the length of the current packet from the buffer
     * if possible. Returns -1 otherwise.
     */
    protected abstract int decodeLength ();

    /**
     * Returns true if a complete frame is in the buffer, false otherwise.
     * If a complete packet is in the buffer, the buffer will be prepared
     * to deliver that frame via our {@link InputStream} interface.
     */
    protected boolean checkForCompletePacket ()
    {
        if (_length == -1 || _have < _length || _length < getHeaderSize()) {
            return false;
        }

        // prepare the buffer such that this frame can be read
        _buffer.position(getHeaderSize());
        _buffer.limit(_length);
        return true;
    }

    /**
     * Reads the next byte of data from this input stream. The value byte
     * is returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the
     * stream has been reached, the value <code>-1</code> is returned.
     *
     * <p>This <code>read</code> method cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream has been reached.
     */
    public int read ()
    {
        return (_buffer.remaining() > 0) ? (_buffer.get() & 0xFF) : -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes
     * from this input stream. If <code>pos</code> equals
     * <code>count</code>, then <code>-1</code> is returned to indicate
     * end of file. Otherwise, the number <code>k</code> of bytes read is
     * equal to the smaller of <code>len</code> and
     * <code>count-pos</code>. If <code>k</code> is positive, then bytes
     * <code>buf[pos]</code> through <code>buf[pos+k-1]</code> are copied
     * into <code>b[off]</code> through <code>b[off+k-1]</code> in the
     * manner performed by <code>System.arraycopy</code>. The value
     * <code>k</code> is added into <code>pos</code> and <code>k</code> is
     * returned.
     *
     * <p>This <code>read</code> method cannot block.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     *
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of the
     * stream has been reached.
     */
    public int read (byte[] b, int off, int len)
    {
        // if they want no bytes, we give them no bytes; this is
        // purportedly the right thing to do regardless of whether we're
        // at EOF or not
        if (len == 0) {
            return 0;
        }

        // trim the amount to be read to what is available; if they wanted
        // bytes and we have none, return -1 to indicate EOF
        if ((len = Math.min(len, _buffer.remaining())) == 0) {
            return -1;
        }

        _buffer.get(b, off, len);
        return len;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer
     * bytes might be skipped if the end of the input stream is reached.
     * The actual number <code>k</code> of bytes to be skipped is equal to
     * the smaller of <code>n</code> and <code>count-pos</code>. The value
     * <code>k</code> is added into <code>pos</code> and <code>k</code> is
     * returned.
     *
     * @param n the number of bytes to be skipped.
     *
     * @return the actual number of bytes skipped.
     */
    public long skip (long n)
    {
        throw new UnsupportedOperationException();
    }

    public int available ()
    {
        return _buffer.remaining();
    }

    /**
     * Always returns false as framed input streams do not support
     * marking.
     */
    public boolean markSupported ()
    {
        return false;
    }

    public int getLength(){
    	return _length;
    }
    /**
     * Does nothing, as marking is not supported.
     */
    public void mark (int readAheadLimit)
    {
        // not supported; do nothing
    }

    /**
     * Resets the buffer to the beginning of the buffered frames.
     */
    public void reset ()
    {
        // position our buffer at the beginning of the frame data
        _buffer.position(getHeaderSize());
    }
    public String toString(){
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("buffer:").append(_buffer).append(",length:").append(_length).append(",have:").append(_have);
    	return buffer.toString();
    }
    /**
     *  
     * @return packet header size
     */
    public abstract int getHeaderSize();
    
}
