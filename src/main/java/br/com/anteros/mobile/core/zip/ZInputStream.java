/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.mobile.core.zip;

import java.io.IOException;
import java.io.InputStream;

public class ZInputStream extends InputStream {

	protected ZStream z = new ZStream();
	protected int bufsize = 512;
	protected int flush = ZStream.Z_NO_FLUSH;
	protected byte[] buf = new byte[bufsize];
	protected byte[] buf1 = new byte[1];
	protected boolean compress;

	private boolean nomoreinput;

	protected InputStream in;

	public ZInputStream(InputStream in) {
		this(in, false);
	}

	public ZInputStream(InputStream in, boolean nowrap) {
		this.in = in;
		z.inflateInit(nowrap);
		compress = false;
		z.next_in = buf;
		z.next_in_index = 0;
		z.avail_in = 0;
	}

	public ZInputStream(InputStream in, int level) {
		this.in = in;
		z.deflateInit(level);
		compress = true;
		z.next_in = buf;
		z.next_in_index = 0;
		z.avail_in = 0;
	}

	public int read() throws IOException {
		if (read(buf1, 0, 1) == -1)
			return (-1);
		return (buf1[0] & 0xFF);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0)
			return (0);
		int err;
		z.next_out = b;
		z.next_out_index = off;
		z.avail_out = len;
		do {
			if ((z.avail_in == 0) && (!nomoreinput)) {
				z.next_in_index = 0;
				z.avail_in = in.read(buf, 0, bufsize);
				if (z.avail_in == -1) {
					z.avail_in = 0;
					nomoreinput = true;
				}
			}
			if (compress)
				err = z.deflate(flush);
			else
				err = z.inflate(flush);
			if (nomoreinput && (err == ZStream.Z_BUF_ERROR))
				return (-1);
			if (err != ZStream.Z_OK && err != ZStream.Z_STREAM_END) {
				throw new ZStreamException((compress ? "de" : "in") + "flating: " + z.msg);
			}
			if ((nomoreinput || err == ZStream.Z_STREAM_END) && (z.avail_out == len))
				return (-1);
		} while (z.avail_out == len && err == ZStream.Z_OK);
		return (len - z.avail_out);
	}

	public int getCompressionRatio() {
		return z.total_out != 0 ? 100 - (int) (z.total_in * 100 / z.total_out) : 0;
	}

	public long skip(long n) throws IOException {
		int len = 512;
		if (n < len) 
			len = (int) n;
		byte[] tmp = new byte[len];
		return ((long) read(tmp));
	}

	public int getFlushMode() {
		return (flush);
	}

	public void setFlushMode(int flush) {
		this.flush = flush;
	}

	public long getTotalIn() {
		return z.total_in;
	}

	public long getTotalOut() {
		return z.total_out;
	}

	public void close() throws IOException {
		in.close();
	}
}
