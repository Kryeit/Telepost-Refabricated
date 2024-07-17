package com.kryeit.telepost.storage.bytes;

import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ReadableByteArray {
    private final byte[] data;
    private int position = 0;

    public ReadableByteArray(byte[] data) {
        this.data = data;
    }

    public byte readByte() {
        return data[position++];
    }

    public int readInt() {
        return (readByte() << 24) | (readByte() << 16) | (readByte() << 8) | readByte();
    }

    public String readString() {
        int length = readInt();
        StringBuilder resultBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            resultBuilder.append((char) readByte());
        }
        return resultBuilder.toString();
    }

    public Vec3d readLocation() {
        double x = readDouble();
        double y = readDouble();
        double z = readDouble();
        return new Vec3d(x, y, z);
    }

    public double readDouble() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.put(readBytes(Double.BYTES));
        byteBuffer.flip();
        return byteBuffer.getDouble();
    }

    public long readLong() {
        return (long) readByte() << 56
                | (long) readByte() << 48
                | (long) readByte() << 40
                | (long) readByte() << 32
                | (long) readByte() << 24
                | (long) readByte() << 16
                | (long) readByte() << 8
                | readByte();
    }

    public byte[] readBytes(int length) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) out[i] = readByte();
        return out;
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public boolean readBoolean() {
        return data[position++] != 0;
    }

    public boolean hasRemaining() {
        return position < data.length;
    }
}
