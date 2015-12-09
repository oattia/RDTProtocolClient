package com.rdt;

import java.net.DatagramPacket;
import java.net.InetAddress;

public abstract class Packet {

    /*
    * Packet structure is as follows:
    *
    * ===========================================================
    *   CHECKSUM                |   pos: 0,  len: 2
    *   CHUNCK_LENGTH           |   pos: 2,  len: 4
    *   SEQ_NO                  |   pos: 6,  len: 4
    *   PACKET_TYPE             |   pos: 10, len
    *   Rest of header (empty)  |   pos: 10, len: PACKET_HEADER_SIZE-10
    * -----------------------------------------------------------
    *   chunckData              |   pos: PACKET_HEADER_SIZE, len: chunkLength
    * ===========================================================
    * */


    // offsets in byte array of packet
    protected static final int POS_CHECKSUM = 0;
    protected static final int POS_LENGTH = 2;
    protected static final int POS_SEQ_NO = 6;
    protected static final int POS_PACKET_TYPE = 10;

    // offsets in byte array of packet
    protected static final int T_DATA = 0;
    protected static final int T_ACK = 1;
    protected static final int T_FILE_NOT_FND = 2;
    protected static final int T_REQUEST = 3;

    protected static final int PACKET_HEADER_SIZE = 20;

    protected int chunkLength;   // Should be 16 bit only
    protected long seqNo;
    protected int packetType;
    protected byte[] chunkData;

    protected int port;
    protected InetAddress ip;

    protected int checkSum; // Should be 16 bit only

    protected byte[] packetData;


    protected void fillAttsFromPacketData() { // created by the other guy
        byte[] actualLenBytes = new byte[4];
        System.arraycopy(packetData, POS_LENGTH, actualLenBytes, 0,  4);
        chunkLength = getInt(actualLenBytes);

        byte[] seqNoBytes = new byte[4];
        System.arraycopy(packetData, POS_SEQ_NO, seqNoBytes, 0, 4);
        seqNo = getInt(seqNoBytes);

        byte[] packetTypeBytes = new byte[4];
        System.arraycopy(packetData, POS_PACKET_TYPE, packetTypeBytes, 0, 4);
        packetType = getInt(packetTypeBytes);

        chunkData = new byte[chunkLength];
        System.arraycopy(packetData, PACKET_HEADER_SIZE, chunkData, 0, chunkLength);

        byte[] receivedChecksum = new byte[2];
        System.arraycopy(packetData, POS_CHECKSUM, receivedChecksum, 0, 2);
        checkSum = getInt(receivedChecksum) & 0xFF;
    }

    public void fillPacketDataFromAtt() { // created by me
        packetData = new byte[PACKET_HEADER_SIZE + chunkLength];

        byte[] actualLenBytes = getBytes(chunkLength);
        System.arraycopy(actualLenBytes, 0, packetData, POS_LENGTH, 4);

        byte[] seqNoBytes = getBytes(seqNo);
        System.arraycopy(seqNoBytes, 0, packetData, POS_SEQ_NO, 4);

        byte[] packetTypeBytes = getBytes(packetType);
        System.arraycopy(packetTypeBytes, 0, packetData, POS_PACKET_TYPE, 4);

        System.arraycopy(chunkData, 0, packetData, PACKET_HEADER_SIZE, chunkLength);

        checkSum = computeChecksum(packetData, 2, PACKET_HEADER_SIZE + chunkLength);
        System.arraycopy(getBytes(checkSum), 0, packetData, POS_CHECKSUM, 2);
    }

    public void setChecksum(int cs) { // for testing only
        this.checkSum = cs;
    }

    public int getCheckSum(){
        return checkSum;
    }

    public void refreshChecksum() {
        checkSum = computeChecksum(packetData, 2, PACKET_HEADER_SIZE + chunkLength);
    }

    public DatagramPacket createDatagramPacket() {
        System.arraycopy(getBytes(checkSum), 0, packetData, POS_CHECKSUM, 2);
        return new DatagramPacket(packetData, packetData.length, ip, port);
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    protected static int computeChecksum(byte[] data, int start, int end) {
        int sum = 0;
        int second = 0;
        for(int i = start; i < end; i += 2) {
            second = i + 1 >= end ? 0 : data[i + 1];
            int num = ((((int)data[i])&0xff) << Byte.SIZE) + (second&0xff);
            sum += num;
            if (sum >= (1 << 16))
                sum += 1;
            sum &= 0xFFFF;
        }
        sum = ~sum;
        sum &= 0xFFFF;
        return sum;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public static byte[] getBytes(int num) {
        byte[] bytes = new byte[Integer.BYTES];
        for(int i = 0; i < Integer.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    public static byte[] getBytes(long num) {
        byte[] bytes = new byte[Long.BYTES];
        for(int i = 0; i < Long.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    public static byte[] getBytes(String str) {
        return str.getBytes();
    }

    public static int getInt(byte[] bytes){
        int value = bytes[bytes.length - 1] & 0xFF;
        for(int i = bytes.length - 2; i >= 0; i--){
            value <<= Byte.SIZE;
            value += (((int)bytes[i]) & 0xFF);
        }
        return value;
    }

    public static long getLong(byte[] bytes){
        long value= bytes[bytes.length - 1] & 0xFF;
        for(int i = bytes.length - 2; i >= 0; i--){
            value <<= Byte.SIZE;
            value += (((long)bytes[i]) & 0xFF);
        }
        return value;
    }

    public static String getString(byte[] data) {
        return new String(data);
    }

    public static int getType(DatagramPacket packet){
        byte[] data = packet.getData();
        byte[] packetTypeBytes = new byte[4];
        System.arraycopy(data, POS_PACKET_TYPE, packetTypeBytes, 0, 4);
        return getInt(packetTypeBytes);
    }

    public boolean isCorrupted(){
        return ((checkSum & 0xFF) != (computeChecksum(packetData, 2, packetData.length) & 0xFF));
    }

}
