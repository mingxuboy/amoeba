
package com.meidusa.amoeba.mysql.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Used to de-compress packets from the MySQL server when protocol-level
 * compression is turned on.
 * 
 * copy from mysql AB
 */
class CompressedInputStream extends InputStream {
	/** The packet data after it has been un-compressed */
	private byte[] buffer;

	/** The stream we are reading from the server */
	private InputStream in;

	/** The ZIP inflater used to un-compress packets */
	private Inflater inflater;

	/**
	 * The buffer to read packet headers into
	 */
	private byte[] packetHeaderBuffer = new byte[7];

	/** The position we are reading from */
	private int pos = 0;

	/**
	 * Creates a new CompressedInputStream that reads the given stream from the
	 * server.
	 * 
	 * @param conn
	 *            DOCUMENT ME!
	 * @param streamFromServer
	 */
	public CompressedInputStream(InputStream streamFromServer) {
		this.in = streamFromServer;
		this.inflater = new Inflater();
	}

	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		if (this.buffer == null) {
			return this.in.available();
		}

		return this.buffer.length - this.pos + this.in.available();
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		this.in.close();
		this.buffer = null;
		this.inflater = null;
	}

	/**
	 * Retrieves and un-compressed (if necessary) the next packet from the
	 * server.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void getNextPacketFromServer() throws IOException {
		byte[] uncompressedData = null;

		int lengthRead = readFully(this.packetHeaderBuffer, 0, 7);

		if (lengthRead < 7) {
			throw new IOException("Unexpected end of input stream");
		}

		int compressedPacketLength = ((this.packetHeaderBuffer[0] & 0xff))
				+ (((this.packetHeaderBuffer[1] & 0xff)) << 8)
				+ (((this.packetHeaderBuffer[2] & 0xff)) << 16);

		int uncompressedLength = ((this.packetHeaderBuffer[4] & 0xff))
				+ (((this.packetHeaderBuffer[5] & 0xff)) << 8)
				+ (((this.packetHeaderBuffer[6] & 0xff)) << 16);

		
		if (uncompressedLength > 0) {
			uncompressedData = new byte[uncompressedLength];

			byte[] compressedBuffer = new byte[compressedPacketLength];

			readFully(compressedBuffer, 0, compressedPacketLength);

			try {
				this.inflater.reset();
			} catch (NullPointerException npe) {
				this.inflater = new Inflater();
			}

			this.inflater.setInput(compressedBuffer);

			try {
				this.inflater.inflate(uncompressedData);
			} catch (DataFormatException dfe) {
				throw new IOException(
						"Error while uncompressing packet from server.");
			}

			this.inflater.end();
		} else {
			

			//	
			// Read data, note this this code is reached when using
			// compressed packets that have not been compressed, as well
			//
			uncompressedData = new byte[compressedPacketLength];
			readFully(uncompressedData, 0, compressedPacketLength);
		}

		

		if ((this.buffer != null) && (this.pos < this.buffer.length)) {
			

			int remaining = this.buffer.length - this.pos;
			byte[] newBuffer = new byte[remaining + uncompressedData.length];

			int newIndex = 0;

			for (int i = this.pos; i < this.buffer.length; i++)
				newBuffer[newIndex++] = this.buffer[i];

			System.arraycopy(uncompressedData, 0, newBuffer, newIndex,
					uncompressedData.length);

			uncompressedData = newBuffer;
		}

		this.pos = 0;
		this.buffer = uncompressedData;

		return;
	}

	/**
	 * Determines if another packet needs to be read from the server to be able
	 * to read numBytes from the stream.
	 * 
	 * @param numBytes
	 *            the number of bytes to be read
	 * 
	 * @throws IOException
	 *             if an I/O error occors.
	 */
	private void getNextPacketIfRequired(int numBytes) throws IOException {
		if ((this.buffer == null)
				|| ((this.pos + numBytes) > this.buffer.length)) {
			getNextPacketFromServer();
		}
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		try {
			getNextPacketIfRequired(1);
		} catch (IOException ioEx) {
			return -1;
		}

		return this.buffer[this.pos++] & 0xff;
	}

	/**
	 * @see java.io.InputStream#read(byte)
	 */
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * @see java.io.InputStream#read(byte, int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		}

		if (len <= 0) {
			return 0;
		}

		try {
			getNextPacketIfRequired(len);
		} catch (IOException ioEx) {
			return -1;
		}

		System.arraycopy(this.buffer, this.pos, b, off, len);
		this.pos += len;

		return len;
	}

	private final int readFully(byte[] b, int off, int len) throws IOException {
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}

		int n = 0;

		while (n < len) {
			int count = this.in.read(b, off + n, len - n);

			if (count < 0) {
				throw new EOFException();
			}

			n += count;
		}

		return n;
	}

	/**
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		long count = 0;

		for (long i = 0; i < n; i++) {
			int bytesRead = read();

			if (bytesRead == -1) {
				break;
			}

			count++;
		}

		return count;
	}
}
