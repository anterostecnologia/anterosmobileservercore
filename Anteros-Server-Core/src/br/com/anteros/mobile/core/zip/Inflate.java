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
/* -*-mode:java; c-basic-offset:2; -*- */
/*
Copyright (c) 2000,2001,2002,2003 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * This program is based on zlib-1.1.3, so all credit should go authors
 * Jean-loup Gailly(jloup@gzip.org) and Mark Adler(madler@alumni.caltech.edu)
 * and contributors of zlib.
 */
/* -----------------------------------------------------------------------------
    OpenBaseMovil Core Library, foundation of the OpenBaseMovil database and tools
    Copyright (C) 2004-2008 Elondra S.L.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.
    If not, see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
----------------------------------------------------------------------------- */


package br.com.anteros.mobile.core.zip;



final class Inflate
{

//    private static final int MAX_WBITS = 15; // 32K LZ77 window

    // preset dictionary flag in zlib header
    private static final int PRESET_DICT = 0x20;

    static final int Z_NO_FLUSH = 0;
    static final int Z_PARTIAL_FLUSH = 1;
    static final int Z_SYNC_FLUSH = 2;
    static final int Z_FULL_FLUSH = 3;
    static final int Z_FINISH = 4;

    private static final int Z_DEFLATED = 8;

    private static final int Z_OK = 0;
    private static final int Z_STREAM_END = 1;
    private static final int Z_NEED_DICT = 2;
//    private static final int Z_ERRNO = -1;
    private static final int Z_STREAM_ERROR = -2;
    private static final int Z_DATA_ERROR = -3;
//    private static final int Z_MEM_ERROR = -4;
    private static final int Z_BUF_ERROR = -5;
//    private static final int Z_VERSION_ERROR = -6;

    private static final int METHOD = 0;   // waiting for method byte
    private static final int FLAG = 1;     // waiting for flag byte
    private static final int DICT4 = 2;    // four dictionary check bytes to go
    private static final int DICT3 = 3;    // three dictionary check bytes to go
    private static final int DICT2 = 4;    // two dictionary check bytes to go
    private static final int DICT1 = 5;    // one dictionary check byte to go
    private static final int DICT0 = 6;    // waiting for inflateSetDictionary
    private static final int BLOCKS = 7;   // decompressing blocks
    private static final int CHECK4 = 8;   // four check bytes to go
    private static final int CHECK3 = 9;   // three check bytes to go
    private static final int CHECK2 = 10;  // two check bytes to go
    private static final int CHECK1 = 11;  // one check byte to go
    private static final int DONE = 12;    // finished check, done
    private static final int BAD = 13;     // got an error--stay here

    int mode;                            // current inflate mode

    // mode dependent information
    int method;        // if FLAGS, method byte

    // if CHECK, check values to compare
    long[] was = new long[1]; // computed check value
    long need;               // stream check value

    // if BAD, inflateSync's marker bytes count
    int marker;

    // mode independent information
    int nowrap;          // flag for no wrapper
    int wbits;            // log2(window size)  (8..15, defaults to 15)

    InfBlocks blocks;     // current inflate_blocks state

    int inflateReset( ZStream z )
    {
        if( z == null || z.istate == null )
        {
            return Z_STREAM_ERROR;
        }

        z.total_in = z.total_out = 0;
        z.msg = null;
        z.istate.mode = z.istate.nowrap != 0 ? BLOCKS : METHOD;
        z.istate.blocks.reset( z, null );
        return Z_OK;
    }

    int inflateEnd( ZStream z )
    {
        if( blocks != null )
        {
            blocks.free( z );
        }
        blocks = null;
        //    ZFREE(z, z->state);
        return Z_OK;
    }

    int inflateInit( ZStream z, int w )
    {
        z.msg = null;
        blocks = null;

        // handle undocumented nowrap option (no zlib header or check)
        nowrap = 0;
        if( w < 0 )
        {
            w = - w;
            nowrap = 1;
        }

        // set window size
        if( w < 8 || w > 15 )
        {
            inflateEnd( z );
            return Z_STREAM_ERROR;
        }
        wbits = w;

        z.istate.blocks = new InfBlocks(
                z,
                z.istate.nowrap != 0 ? null : this,
                1 << w
        );

        // reset state
        inflateReset( z );
        return Z_OK;
    }

    int inflate( ZStream z, int f )
    {
        int r;
        int b;

        if( z == null || z.istate == null || z.next_in == null )
        {
            return Z_STREAM_ERROR;
        }
        f = f == Z_FINISH ? Z_BUF_ERROR : Z_OK;
        r = Z_BUF_ERROR;
        while( true )
        {
            switch( z.istate.mode )
            {
                case METHOD:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    if( (
                            ( z.istate.method = z.next_in[z.next_in_index++] ) &
                            0xf
                    ) != Z_DEFLATED )
                    {
                        z.istate.mode = BAD;
                        z.msg = "unknown compression method";
                        z.istate.marker = 5;       // can't try inflateSync
                        break;
                    }
                    if( ( z.istate.method >> 4 ) + 8 > z.istate.wbits )
                    {
                        z.istate.mode = BAD;
                        z.msg = "invalid window size";
                        z.istate.marker = 5;       // can't try inflateSync
                        break;
                    }
                    z.istate.mode = FLAG;
                case FLAG:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    b = ( z.next_in[z.next_in_index++] ) & 0xff;

                    if( ( ( ( z.istate.method << 8 ) + b ) % 31 ) != 0 )
                    {
                        z.istate.mode = BAD;
                        z.msg = "incorrect header check";
                        z.istate.marker = 5;       // can't try inflateSync
                        break;
                    }

                    if( ( b & PRESET_DICT ) == 0 )
                    {
                        z.istate.mode = BLOCKS;
                        break;
                    }
                    z.istate.mode = DICT4;
                case DICT4:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need = (
                            ( z.next_in[z.next_in_index++] & 0xff ) << 24
                    ) & 0xff000000L;
                    z.istate.mode = DICT3;
                case DICT3:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need += (
                            (
                                    z.next_in[z.next_in_index++] & 0xff
                            ) << 16
                    ) & 0xff0000L;
                    z.istate.mode = DICT2;
                case DICT2:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need += (
                            (
                                    z.next_in[z.next_in_index++] & 0xff
                            ) << 8
                    ) & 0xff00L;
                    z.istate.mode = DICT1;
                case DICT1:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need += ( z.next_in[z.next_in_index++] & 0xffL );
                    z.adler = z.istate.need;
                    z.istate.mode = DICT0;
                    return Z_NEED_DICT;
                case DICT0:
                    z.istate.mode = BAD;
                    z.msg = "need dictionary";
                    z.istate.marker = 0;       // can try inflateSync
                    return Z_STREAM_ERROR;
                case BLOCKS:

                    r = z.istate.blocks.proc( z, r );
                    if( r == Z_DATA_ERROR )
                    {
                        z.istate.mode = BAD;
                        z.istate.marker = 0;     // can try inflateSync
                        break;
                    }
                    if( r == Z_OK )
                    {
                        r = f;
                    }
                    if( r != Z_STREAM_END )
                    {
                        return r;
                    }
                    r = f;
                    z.istate.blocks.reset( z, z.istate.was );
                    if( z.istate.nowrap != 0 )
                    {
                        z.istate.mode = DONE;
                        break;
                    }
                    z.istate.mode = CHECK4;
                case CHECK4:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need = (
                            ( z.next_in[z.next_in_index++] & 0xff ) << 24
                    ) & 0xff000000L;
                    z.istate.mode = CHECK3;
                case CHECK3:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need += (
                            (
                                    z.next_in[z.next_in_index++] & 0xff
                            ) << 16
                    ) & 0xff0000L;
                    z.istate.mode = CHECK2;
                case CHECK2:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need += (
                            (
                                    z.next_in[z.next_in_index++] & 0xff
                            ) << 8
                    ) & 0xff00L;
                    z.istate.mode = CHECK1;
                case CHECK1:

                    if( z.avail_in == 0 )
                    {
                        return r;
                    }
                    r = f;

                    z.avail_in--;
                    z.total_in++;
                    z.istate.need += ( z.next_in[z.next_in_index++] & 0xffL );

                    if( ( (int) ( z.istate.was[0] ) ) !=
                        ( (int) ( z.istate.need ) ) )
                    {
                        z.istate.mode = BAD;
                        z.msg = "incorrect data check";
                        z.istate.marker = 5;       // can't try inflateSync
                        break;
                    }

                    z.istate.mode = DONE;
                case DONE:
                    return Z_STREAM_END;
                case BAD:
                    return Z_DATA_ERROR;
                default:
                    return Z_STREAM_ERROR;
            }
        }
    }


    int inflateSetDictionary( ZStream z, byte[] dictionary, int dictLength )
    {
        int index = 0;
        int length = dictLength;
        if( z == null || z.istate == null || z.istate.mode != DICT0 )
        {
            return Z_STREAM_ERROR;
        }

        if( z._adler.adler32( 1L, dictionary, 0, dictLength ) != z.adler )
        {
            return Z_DATA_ERROR;
        }

        z.adler = z._adler.adler32( 0, null, 0, 0 );

        if( length >= ( 1 << z.istate.wbits ) )
        {
            length = ( 1 << z.istate.wbits ) - 1;
            index = dictLength - length;
        }
        z.istate.blocks.set_dictionary( dictionary, index, length );
        z.istate.mode = BLOCKS;
        return Z_OK;
    }

    private static byte[] mark = {(byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff};

    int inflateSync( ZStream z )
    {
        int n;       // number of bytes to look at
        int p;       // pointer to bytes
        int m;       // number of marker bytes found in a row
        long r, w;   // temporaries to save total_in and total_out

        // set up
        if( z == null || z.istate == null )
        {
            return Z_STREAM_ERROR;
        }
        if( z.istate.mode != BAD )
        {
            z.istate.mode = BAD;
            z.istate.marker = 0;
        }
        if( ( n = z.avail_in ) == 0 )
        {
            return Z_BUF_ERROR;
        }
        p = z.next_in_index;
        m = z.istate.marker;

        // search
        while( n != 0 && m < 4 )
        {
            if( z.next_in[p] == mark[m] )
            {
                m++;
            }
            else if( z.next_in[p] != 0 )
            {
                m = 0;
            }
            else
            {
                m = 4 - m;
            }
            p++;
            n--;
        }

        // restore
        z.total_in += p - z.next_in_index;
        z.next_in_index = p;
        z.avail_in = n;
        z.istate.marker = m;

        // return no joy or set up to restart on a new block
        if( m != 4 )
        {
            return Z_DATA_ERROR;
        }
        r = z.total_in;
        w = z.total_out;
        inflateReset( z );
        z.total_in = r;
        z.total_out = w;
        z.istate.mode = BLOCKS;
        return Z_OK;
    }

    // Returns true if inflate is currently at the end of a block generated
    // by Z_SYNC_FLUSH or Z_FULL_FLUSH. This function is used by one PPP
    // implementation to provide an additional safety check. PPP uses Z_SYNC_FLUSH
    // but removes the length bytes of the resulting empty stored block. When
    // decompressing, PPP checks that at the end of input packet, inflate is
    // waiting for these length bytes.
    int inflateSyncPoint( ZStream z )
    {
        if( z == null || z.istate == null || z.istate.blocks == null )
        {
            return Z_STREAM_ERROR;
        }
        return z.istate.blocks.sync_point();
    }
}
