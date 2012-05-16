package util;

public class ByteManipulator {
	public static void addInt(byte[] buffer, int offset, int value) {
		buffer[offset] 	   = (byte)(value >>> 24);
        buffer[offset + 1] = (byte)(value >>> 16);
        buffer[offset + 2] = (byte)(value >>> 8);
        buffer[offset + 3] = (byte)value;
	}
	
	public static int getInt(byte[] buffer, int offset) {
		return (buffer[offset] << 24) + ((buffer[offset + 1] & 0xFF) << 16) +
				+ ((buffer[offset + 2] & 0xFF) << 8) + (buffer[offset + 3] & 0xFF);
	}
}
