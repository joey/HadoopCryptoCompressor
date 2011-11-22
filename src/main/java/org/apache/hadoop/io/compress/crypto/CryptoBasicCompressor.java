package org.apache.hadoop.io.compress.crypto;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.Compressor;

import sec.util.Crypto;


public class CryptoBasicCompressor implements Compressor {

	Crypto crypto;
	
	Long read = new Long(0l);
	Long wrote = new Long(0l);
	ByteBuffer in;
	ByteBuffer remain;

	private boolean finish = false;

	private boolean finished = false;
	
	public CryptoBasicCompressor(String key){
		crypto = new Crypto(key);
	}
	
	@Override
	public synchronized int compress(byte[] buf, int off, int len) throws IOException {
		finished  = false;
		if(remain != null && remain.remaining()>0){
			int size = Math.min(buf.length, remain.remaining());
			remain.get(buf, off, size);
			wrote += size;
			if(!remain.hasRemaining()){
				remain = null;
				finished = true;
			}
			return size;
		}
		if(in==null || in.remaining()<=0){
			finished = true;
			return 0;
		}
		byte[] w = new byte[in.remaining()];
		in.get(w);
		byte[] b = crypto.encrypt(w);
		int size = Math.min(buf.length, b.length);
		remain = ByteBuffer.wrap(b);
		remain.get(buf, off, size);
		wrote += size;
		if(remain.remaining()<=0){
			finished = true;
		}
		return size;
	}

	@Override
	public void end() {

	}

	@Override
	public synchronized void finish() {
	    finish  = true;
	 }


	@Override
	public boolean finished() {
		return finish && finished && (remain==null || remain.remaining() <= 0);
	}

	@Override
	public long getBytesRead() {
		return read;
	}

	@Override
	public long getBytesWritten() {
		return wrote;
	}

	@Override
	public boolean needsInput() {
		return (in == null || in.remaining()<=0) && (remain == null || remain.remaining() <=0);
	}

	@Override
	public void reinit(Configuration arg0) {

	}

	@Override
	public void reset() {

	}

	@Override
	public void setDictionary(byte[] arg0, int arg1, int arg2) {
	}

	@Override
	public synchronized void setInput(byte[] buf, int offset, int length) {
		in = ByteBuffer.wrap(buf,offset,length);
		read+=in.remaining();
		finished  = false;
	}

}