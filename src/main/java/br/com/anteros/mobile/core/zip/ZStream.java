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

public final class ZStream {
	public static final int Z_NO_COMPRESSION = 0;
	public static final int Z_BEST_SPEED = 1;
	public static final int Z_BEST_COMPRESSION = 9;
	public static final int Z_DEFAULT_COMPRESSION = -1;

	public static final int Z_FILTERED = 1;
	public static final int Z_HUFFMAN_ONLY = 2;
	public static final int Z_DEFAULT_STRATEGY = 0;

	public static final int Z_NO_FLUSH = 0;
	public static final int Z_PARTIAL_FLUSH = 1;
	public static final int Z_SYNC_FLUSH = 2;
	public static final int Z_FULL_FLUSH = 3;
	public static final int Z_FINISH = 4;

	public static final int Z_OK = 0;
	public static final int Z_STREAM_END = 1;
	public static final int Z_NEED_DICT = 2;
	public static final int Z_ERRNO = -1;
	public static final int Z_STREAM_ERROR = -2;
	public static final int Z_DATA_ERROR = -3;
	public static final int Z_MEM_ERROR = -4;
	public static final int Z_BUF_ERROR = -5;
	public static final int Z_VERSION_ERROR = -6;

	private static final int MAX_WBITS = 15; 
	private static final int DEF_WBITS = MAX_WBITS;

	public byte[] next_in; 
	public int next_in_index;
	public int avail_in; 
	public long total_in; 

	public byte[] next_out; 
	public int next_out_index;
	public int avail_out; 
	public long total_out; 
	public String msg;

	Deflate dstate;
	Inflate istate;
	int data_type; 

	public long adler;
	Adler32 _adler = new Adler32();

	public int inflateInit() {
		return inflateInit(DEF_WBITS);
	}

	public int inflateInit(boolean nowrap) {
		return inflateInit(DEF_WBITS, nowrap);
	}

	public int inflateInit(int w) {
		return inflateInit(w, false);
	}

	public int inflateInit(int w, boolean nowrap) {
		istate = new Inflate();
		return istate.inflateInit(this, nowrap ? -w : w);
	}

	public int inflate(int f) {
		if (istate == null) {
			return Z_STREAM_ERROR;
		}
		return istate.inflate(this, f);
	}

	public int inflateEnd() {
		if (istate == null) {
			return Z_STREAM_ERROR;
		}
		int ret = istate.inflateEnd(this);
		istate = null;
		return ret;
	}

	public int inflateSync() {
		if (istate == null) {
			return Z_STREAM_ERROR;
		}
		return istate.inflateSync(this);
	}

	public int inflateSetDictionary(byte[] dictionary, int dictLength) {
		if (istate == null) {
			return Z_STREAM_ERROR;
		}
		return istate.inflateSetDictionary(this, dictionary, dictLength);
	}

	public int deflateInit(int level) {
		return deflateInit(level, MAX_WBITS);
	}

	public int deflateInit(int level, boolean nowrap) {
		return deflateInit(level, MAX_WBITS, nowrap);
	}

	public int deflateInit(int level, int bits) {
		return deflateInit(level, bits, false);
	}

	public int deflateInit(int level, int bits, boolean nowrap) {
		dstate = new Deflate();
		return dstate.deflateInit(this, level, nowrap ? -bits : bits);
	}

	public int deflate(int flush) {
		if (dstate == null) {
			return Z_STREAM_ERROR;
		}
		return dstate.deflate(this, flush);
	}

	public int deflateEnd() {
		if (dstate == null) {
			return Z_STREAM_ERROR;
		}
		int ret = dstate.deflateEnd();
		dstate = null;
		return ret;
	}

	public int deflateParams(int level, int strategy) {
		if (dstate == null) {
			return Z_STREAM_ERROR;
		}
		return dstate.deflateParams(this, level, strategy);
	}

	public int deflateSetDictionary(byte[] dictionary, int dictLength) {
		if (dstate == null) {
			return Z_STREAM_ERROR;
		}
		return dstate.deflateSetDictionary(this, dictionary, dictLength);
	}

	void flush_pending() {
		int len = dstate.pending;

		if (len > avail_out) {
			len = avail_out;
		}
		if (len == 0) {
			return;
		}

		if (dstate.pending_buf.length <= dstate.pending_out || next_out.length <= next_out_index
				|| dstate.pending_buf.length < (dstate.pending_out + len) || next_out.length < (next_out_index + len)) {
		}

		System.arraycopy(dstate.pending_buf, dstate.pending_out, next_out, next_out_index, len);

		next_out_index += len;
		dstate.pending_out += len;
		total_out += len;
		avail_out -= len;
		dstate.pending -= len;
		if (dstate.pending == 0) {
			dstate.pending_out = 0;
		}
	}

	int read_buf(byte[] buf, int start, int size) {
		int len = avail_in;

		if (len > size)
			len = size;
		if (len == 0)
			return 0;

		avail_in -= len;

		if (dstate.noheader == 0) {
			adler = _adler.adler32(adler, next_in, next_in_index, len);
		}
		System.arraycopy(next_in, next_in_index, buf, start, len);
		next_in_index += len;
		total_in += len;
		return len;
	}

	public void free() {
		next_in = null;
		next_out = null;
		msg = null;
		_adler = null;
	}
}
