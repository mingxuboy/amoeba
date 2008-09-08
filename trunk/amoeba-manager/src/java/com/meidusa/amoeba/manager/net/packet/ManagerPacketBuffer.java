package com.meidusa.amoeba.manager.net.packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * 
 * @author struct
 *
 */
public class ManagerPacketBuffer extends AbstractPacketBuffer {

	public ObjectInputStream ois;
	public ObjectOutputStream oos;
	public ManagerPacketBuffer(int size) {
		super(size);
	}
	
	public ManagerPacketBuffer(byte[] buf) {
		super(buf);
	}
	
	public Object readObject(){
		try {
			if(ois == null){
				ois = new ObjectInputStream(this.asInputStream());
			}
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void writeObject(Object object){
		try {
			if(oos == null){
				oos = new ObjectOutputStream(this.asOutputStream());
			}
			oos.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
