/**
 *    Copyright 2014 Opower, Inc.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 **/
package com.opower.rest.client.generator.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extension of {@link java.io.BufferedInputStream} enforcing the contract where reset()
 * always returns to the beginning of the stream, and the internal buffer
 * expands automatically to the total length of content read from the underlying
 * stream.
 *
 * @author ul8b
 */
public class SelfExpandingBufferredInputStream extends BufferedInputStream {
    private static int defaultBufferSize = 8192;

    public SelfExpandingBufferredInputStream(InputStream in) {
        super(in);
        super.mark(defaultBufferSize);
    }

    /**
     * Not supported. Mark position is always zero.
     */
    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("ExpandoBufferredInputStream is always marked at index 0.");
    }

    @Override
    public synchronized int read() throws IOException {
        if (pos == marklimit) {
            expand();
        }
        return super.read();
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        while (pos + len > marklimit) {
            expand();
        }
        return super.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        while (pos + b.length > marklimit) {
            expand();
        }
        return super.read(b);
    }

    /**
     * Double the current buffer size limit. Reset to zero, then double the
     * buffer size and restore last position in the buffer.
     *
     * @throws java.io.IOException
     */
    private void expand() throws IOException {
        int lastPos = pos;
        super.reset();
        super.mark(marklimit * 2);
        pos = lastPos;
    }

    /**
     * Return the current maximum size of the internal buffer. This is
     * independent of how much data is actually contained within the buffer.
     */
    public int getBufSize() {
        return buf.length;
    }

    public int getCount() {
        return count;
    }

    public int getPos() {
        return pos;
    }

    public int getMarkLimit() {
        return marklimit;
    }

    public int getMarkPos() {
        return markpos;
    }
}
