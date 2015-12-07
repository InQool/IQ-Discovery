
package com.rdksys.oai.data;

import org.openarchives.oai._2.RecordType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;

/**
 * @author David Uvalle, david.uvalle@gmail.com
 * @version 0.1
 * 
 */
@SuppressWarnings("all")
public class RecordIterator implements Iterator<RecordType>  {
	
	private int index = 0;
	private int gindex = 0;
	private int next_pointer = 0;
	private RecordType[] record;
	private int num_records = 0;
	private int MAX = 100;
	private File file2 = null;
	private FileInputStream fis2 = null;
	private ObjectInputStream bis2 = null;
	
	public RecordIterator(String filename) {	
		File file = null;
		FileInputStream fis = null;
		ObjectInputStream bis = null;
		try {	
			file2 = new File(filename);
			fis2 = new FileInputStream(file2); 
			bis2 = new ObjectInputStream(fis2);
			
			file = new File(filename);
			fis = new FileInputStream(file);
			bis = new ObjectInputStream(fis);
			
			RecordType tmp = null;
			while((tmp=(RecordType)bis.readObject())!=null) {
				num_records++;
			}
			
		}
		catch(Exception e) { }
		finally {
			try {
				
				bis.close();
			}
			catch(IOException e) { }
		}
		
		this.num_records = num_records;
	}
	
	private void getRecordBuffer() {
		record = new RecordType[MAX];
		index = 0;		
		for(int i=0;i<MAX;i++)
			record[i] = new RecordType();
		
		try {
			RecordType tmp;
			
			if( (num_records-gindex) < MAX)
			{
				MAX = num_records - gindex;
			}
			
			for(int i=0;i<MAX;i++) {
				tmp = (RecordType)bis2.readObject();
				record[i] = tmp;
				gindex++;
			}
			
		}
		catch(Exception e) { }
	}

	public boolean hasNext() {
		if(next_pointer == num_records)
			return false;
		return true;
	}

	public RecordType next() {
		if( (index%MAX)==0)
			getRecordBuffer();
		
		next_pointer++;
		
		return record[index++];
	}

	public void remove() {
	}

    public void close() {
        try {
            bis2.close();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
