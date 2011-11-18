package com.meidusa.amoeba.benchmark;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.StringUtil;

/**
 * @author Struct
 *
 */
public class FileLineData implements RandomData<Object>,Initialisable{
	private File file ;
	private RandomAccessFile raf = null;
	private String lineSplit;
	private boolean needSplit = true;
	private boolean closed = false;
	public boolean isNeedSplit() {
		return needSplit;
	}
	public void setNeedSplit(boolean needSplit) {
		this.needSplit = needSplit;
	}
	public String getLineSplit() {
		return lineSplit;
	}
	public void setLineSplit(String lineSplit) {
		if(StringUtil.isEmpty(lineSplit)){
			lineSplit = null;
		}else{
			this.lineSplit = lineSplit;
		}
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

			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					closed = true;
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
	public synchronized Object  nextData() {
		if(closed) throw new IllegalStateException("file closed..");
		String line = null;
		try {
			line = raf.readLine();
			
			while(StringUtil.isEmpty(line) && line != null){
				line = raf.readLine();
			}
			
			if(line == null){
				throw new EOFException("end of File="+this.getFile().getAbsolutePath());
			}

		} catch (IOException e) {
			closed = true;
			throw new IllegalStateException(e);
		}
		String[] obj = null;
		if(needSplit){
			if(lineSplit == null){
				obj = StringUtil.split(line);
			}else{
				obj = StringUtil.split(line,lineSplit);
			}
			return obj;
		}else{
			return line;
		}
	}
	
	public static void main(String[] args) throws Exception{
		final FileLineData mapping = new FileLineData();
		mapping.setFile(new File("c:/role.txt"));
		mapping.init();
		List<Thread> list = new ArrayList<Thread>();
		long start = System.currentTimeMillis();
		for(int j=0;j<1;j++){
			Thread thread = new Thread(){
				public void run(){
					for(int i=0;i<1000;i++){
						System.out.println(mapping.nextData());
					}
				}
			};
			list.add(thread);
			thread.start();
		}
		
		for(int i=0;i<list.size();i++){
			list.get(i).join();
		}
		
		System.out.println("time="+(System.currentTimeMillis()-start));
	}
	
}
