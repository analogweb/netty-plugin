package org.analogweb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.analogweb.ReadableBuffer;
import org.analogweb.WritableBuffer;
import org.analogweb.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

/**
 * @author y2k2mt
 */
public class ByteBufReadableBuffer implements ReadableBuffer{
    private ByteBuf byteBuf;

    public static ByteBufReadableBuffer readBuffer(ByteBuf byteBuf) {
        return new ByteBufReadableBuffer(byteBuf);
    }

    ByteBufReadableBuffer(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    protected ByteBuf getByteBuf(){
        return this.byteBuf;
    }

    @Override
    public ReadableBuffer read(byte[] dst, int index, int length) throws IOException {
        getByteBuf().readBytes(dst,index,length);
        return this;
    }

    @Override
    public ReadableBuffer read(ByteBuffer buffer) throws IOException {
        getByteBuf().readBytes(buffer);
        return this;
    }

    @Override
    public String asString(Charset charset) throws IOException {
        return new String(getByteBuf().array(),charset);
    }

    @Override
    public InputStream asInputStream() throws IOException {
        return new ByteBufInputStream(getByteBuf());
    }

    @Override
    public ReadableByteChannel asChannel() throws IOException {
        return new ReadableByteChannel() {

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public int read(ByteBuffer dst) throws IOException {
                getByteBuf().readBytes(dst);
                return getByteBuf().arrayOffset();
            }
        };
    }

    @Override
    public ReadableBuffer to(WritableBuffer writable) throws IOException {
        if(writable instanceof  ByteBufWritableBuffer){
            ((ByteBufWritableBuffer)writable).getByteBuf().writeBytes(getByteBuf());
        } else {
            IOUtils.copy(asChannel(),writable.asChannel());
        }
        return this;
    }

    @Override
    public long getLength() {
        return getByteBuf().arrayOffset();
    }
}
