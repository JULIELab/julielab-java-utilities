package de.julielab.java.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Objects;

/**
 * From https://stackoverflow.com/a/62160797/1314955
 */
public class StringIteratorInputStream extends InputStream {
    private CharsetEncoder encoder;
    private Iterator<String> strings;
    private CharBuffer current;
    private ByteBuffer pending;

    public StringIteratorInputStream(Iterator<String> it) {
        this(it, Charset.defaultCharset());
    }
    public StringIteratorInputStream(Iterator<String> it, Charset cs) {
        encoder = cs.newEncoder();
        strings = Objects.requireNonNull(it);
    }

    @Override
    public int read() throws IOException {
        for(;;) {
            if(pending != null && pending.hasRemaining())
                return pending.get() & 0xff;
            if(!ensureCurrent()) return -1;
            if(pending == null) pending = ByteBuffer.allocate(4096);
            else pending.compact();
            encoder.encode(current, pending, !strings.hasNext());
            pending.flip();
        }
    }

    private boolean ensureCurrent() {
        while(current == null || !current.hasRemaining()) {
            if(!strings.hasNext()) return false;
            current = CharBuffer.wrap(strings.next());
        }
        return true;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        // Objects.checkFromIndexSize(off, len, b.length); // JDK 9
        int transferred = 0;
        if(pending != null && pending.hasRemaining()) {
            boolean serveByBuffer = pending.remaining() >= len;
            pending.get(b, off, transferred = Math.min(pending.remaining(), len));
            if(serveByBuffer) return transferred;
            len -= transferred;
            off += transferred;
        }
        ByteBuffer bb = ByteBuffer.wrap(b, off, len);
        while(bb.hasRemaining() && ensureCurrent()) {
            int r = bb.remaining();
            encoder.encode(current, bb, !strings.hasNext());
            transferred += r - bb.remaining();
        }
        return transferred == 0? -1: transferred;
    }
}
