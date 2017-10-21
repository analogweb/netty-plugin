package org.analogweb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import org.analogweb.ReadableBuffer;
import org.analogweb.WritableBuffer;
import org.analogweb.util.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * @author y2k2mt
 */
public class ByteBufWritableBuffer implements WritableBuffer {

	private ByteBuf byteBuf;

	public static ByteBufWritableBuffer writeBuffer(ByteBuf byteBuf) {
		return new ByteBufWritableBuffer(byteBuf);
	}

	ByteBufWritableBuffer(ByteBuf byteBuf) {
		this.byteBuf = byteBuf;
	}

	protected ByteBuf getByteBuf() {
		return this.byteBuf;
	}

	@Override
	public WritableBuffer writeBytes(byte[] bytes) throws IOException {
		this.byteBuf = getByteBuf().writeBytes(bytes);
		return this;
	}

	@Override
	public WritableBuffer writeBytes(byte[] bytes, int index, int length)
			throws IOException {
		this.byteBuf = getByteBuf().writeBytes(bytes, index, length);
		return this;
	}

	@Override
	public WritableBuffer writeBytes(ByteBuffer buffer) throws IOException {
		this.byteBuf = getByteBuf().writeBytes(buffer);
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

	@Override
	public WritableBuffer from(ReadableBuffer readable) throws IOException {
		if (readable instanceof ByteBufReadableBuffer) {
			((ByteBufReadableBuffer) readable).getByteBuf().readBytes(
					getByteBuf());
		} else {
			IOUtils.copy(readable.asChannel(), asChannel());
		}
		return this;
	}
}
