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
public class ByteBufReadableBuffer implements ReadableBuffer {

	private ByteBuf byteBuf;
	private long contentLength = 0;

	public static ByteBufReadableBuffer readBuffer(ByteBuf byteBuf) {
		return new ByteBufReadableBuffer(byteBuf);
	}

	ByteBufReadableBuffer(ByteBuf byteBuf) {
		this.byteBuf = byteBuf;
	}

	protected ByteBuf getByteBuf() {
		return this.byteBuf;
	}

	@Override
	public ReadableBuffer read(byte[] dst, int index, int length)
			throws IOException {
		getByteBuf().readBytes(dst, index, length);
		this.contentLength += length - index;
		return this;
	}

	@Override
	public ReadableBuffer read(ByteBuffer buffer) throws IOException {
		ByteBuf b = getByteBuf();
		b.readBytes(buffer);
		this.contentLength += buffer.remaining();
		return this;
	}

	@Override
	public String asString(Charset charset) throws IOException {
		ByteBuf b = getByteBuf();
		this.contentLength = b.readableBytes() - b.readerIndex();
		return getByteBuf().toString(charset);
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
				return getByteBuf().isReadable();
			}

			@Override
			public void close() throws IOException {
			}

			@Override
			public int read(ByteBuffer dst) throws IOException {
				int length = dst.remaining();
				getByteBuf().readBytes(dst);
				return length;
			}
		};
	}

	@Override
	public ReadableBuffer to(WritableBuffer writable) throws IOException {
		ByteBuf b = getByteBuf();
		if (writable instanceof ByteBufWritableBuffer) {
			((ByteBufWritableBuffer) writable).getByteBuf().writeBytes(b);
		} else {
			IOUtils.copy(asChannel(), writable.asChannel());
		}
		this.contentLength = b.readableBytes() - b.readerIndex();
		return this;
	}

	@Override
	public long getLength() {
		return this.contentLength;
	}
}