package com.meidusa.amoeba.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.apache.commons.lang.math.RandomUtils;

import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.MappedByteBufferUtil;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 采用文件内存映射，文件的所有数据将会到缓存中
 * 文件不宜过大
 * @author Struct
 *
 */
public class FileLineRandomData implements RandomData<String[]>,Initialisable{
	private File file ;
	private RandomAccessFile raf = null;
	private MappedByteBuffer buffer = null;
	private int size;
	private String lineSplit;
	
	public String getLineSplit() {
		return lineSplit;
	}
	public void setLineSplit(String lineSplit) {
		this.lineSplit = lineSplit;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	
	@Override
	public void init() throws InitialisationException {
		try {
			raf = new RandomAccessFile(file,"r");
			size = raf.length() > Integer.MAX_VALUE ? Integer.MAX_VALUE: Long.valueOf(raf.length()).intValue();
			buffer = raf.getChannel().map(MapMode.READ_ONLY, 0, size);
			buffer.load();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					MappedByteBufferUtil.unmap(buffer);
					try {
						raf.close();
					} catch (IOException e) {
					}
				}
			});
		} catch (IOException e) {
			throw new InitialisationException(e);
		} 
	}
	
	
	@Override
	public String[] nextData() {
		int position = RandomUtils.nextInt(size);
		goNextNewLineHead(position);
		String[] obj = null;
		String line = readLine();
		if(lineSplit == null){
			obj = StringUtil.split(line);
		}else{
			obj = StringUtil.split(line,lineSplit);
		}
		return obj;
	}
	
	private void goNextNewLineHead(int position){
		buffer.position(position);
		boolean eol = false;
		int c = -1;
		while (!eol) {
		    switch (c = buffer.get()) {
		    case -1:
		    case '\n':
			eol = true;
			break;
		    case '\r':
			eol = true;
			int cur = buffer.position();
			if ((buffer.get()) != '\n') {
				buffer.position(cur);
			}
			break;
		    }
		    if(!eol){
			    if(position >0){
			    	buffer.position(--position);
			    }else{
			    	eol = true;
			    }
		    }
		}
	}
	
	private final String readLine() {
		StringBuffer input = new StringBuffer();
		int c = -1;
		boolean eol = false;

		while (!eol) {
		    switch (c = buffer.get()) {
		    case -1:
		    case '\n':
			eol = true;
			break;
		    case '\r':
			eol = true;
			int cur = buffer.position();
			if ((buffer.get()) != '\n') {
				buffer.position(cur);
			}
			break;
		    default:
			input.append((char)c);
			break;
		    }
		}

		if ((c == -1) && (input.length() == 0)) {
		    return null;
		}
		return input.toString();
	}
	
	public static void main(String[] args) throws Exception{
		FileLineRandomData mapping = new FileLineRandomData();
		mapping.setFile(new File("c:/1.txt"));
		mapping.init();
		
		long start = System.currentTimeMillis();
		for(int i=0;i<100;i++){
			System.out.println(mapping.nextData());
		}
		
		System.out.println("time="+(System.currentTimeMillis()-start));
	}
	
}
