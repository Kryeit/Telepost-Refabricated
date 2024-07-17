package com.kryeit.telepost.storage.bytes;

import net.minecraft.util.math.Vec3d;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class WritableByteArray {
    private final ByteArrayOutputStream data;

    public WritableByteArray(int length) {
        data = new ByteArrayOutputStream(length);
    }

    public WritableByteArray() {
        data = new ByteArrayOutputStream();
    }

    public void writeByte(int b) {
        data.write(b);
    }

    public void writeInt(int i) {
        writeByte(i >> 24);
        writeByte(i >> 16);
        writeByte(i >> 8);
        writeByte(i);
    }

    public void writeDouble(double d) {
        writeBytes(ByteBuffer.allocate(8).putDouble(d).array());
    }

    public void writeLong(long l) {
        writeByte((int) (l >> 56));
        writeByte((int) (l >> 48));
        writeByte((int) (l >> 40));
        writeByte((int) (l >> 32));
        writeByte((int) (l >> 24));
        writeByte((int) (l >> 16));
        writeByte((int) (l >> 8));
        writeByte((int) l);
    }

    public void writeString(String s) {
        int length = s.length();
        writeInt(length);
        for (int i = 0; i < length; i++) {
            writeByte(s.charAt(i));
        }
    }

    public void writeLocation(Vec3d loc) {
        writeDouble(loc.getX());
        writeDouble(loc.getY());
        writeDouble(loc.getZ());
    }

    public void writeUUID(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writeBoolean(boolean b) {
        data.write(b ? 1 : 0);
    }

    public void writeBytes(byte[] bytes) {
        for (byte b : bytes) writeByte(b);
    }

    public byte[] toByteArray() {
        return data.toByteArray();
    }
}
