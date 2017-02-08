package org.analogweb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import org.analogweb.WritableBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * @author y2k2mt
 */
public class ByteBufWritableBuffer implements WritableBuffer {

    private ByteBuf byteBuf;

    public static ByteBufWritableBuffer writeBuffer(ByteBuf byteBuf){
        return new ByteBufWritableBuffer(byteBuf);
    }

    ByteBufWritableBuffer(ByteBuf byteBuf){
        this.byteBuf = byteBuf;
    }

    protected ByteBuf getByteBuf(){
        return this.byteBuf;
    }

    @Override
    public WritableBuffer writeBytes(byte[] bytes) throws IOException {
        getByteBuf().writeBytes(bytes);
        return this;
    }

    @Override
    public WritableBuffer writeBytes(byte[] bytes, int index, int length) throws IOException {
        getByteBuf().writeBytes(bytes,index,length);
        return this;
    }

    @Override
    public WritableBuffer writeBytes(ByteBuffer buffer) throws IOException {
        getByteBuf().writeBytes(buffer);
        return this;
    }

    @Override
    public OutputStream asOutputStream() throws IOException {
        return new ByteBufOutputStream(getByteBuf());
    }

    @Override
    public WritableByteChannel asChannel() throws IOException {
        return new WritableByteChannel() {
            @Override
            public int write(ByteBuffer src) throws IOException {
                ByteBuf buf = getByteBuf();
                buf.writeBytes(src);
                return buf.arrayOffset();
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}
