package com.rdt;

import java.net.DatagramPacket;
import java.net.InetAddress;

public abstract class Packet {

    /*
    * Packet structure is as follows:
    *
    * ===========================================================
    *   CHECKSUM                |   pos: 0, len: 2
    *   PACKET_TYPE             |   pos:
    *   CHUNCK_LENGTH           |   pos: 2, len: 4
    *   SEQ_NO                  |   pos: 6, len: 4
    *   Rest of header (empty)  |   pos: 10, len: PACKET_HEADER_SIZE-10
    * -----------------------------------------------------------
    *   chunckData              |   pos: PACKET_HEADER_SIZE, len: chunkLength
    * ===========================================================
    *
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

    protected int checkSum; // Should be 16 bit only
    protected int chunkLength;   // Should be 16 bit only
    protected boolean isCorrupted;
    protected byte[] chunkData;
    protected int packetType;
    protected long seqNo;
    protected int port;
    protected InetAddress ip;



    public Packet() {    }


    public Packet(DatagramPacket packet){
        byte[] data = packet.getData();

        byte[] actualLenBytes = new byte[4];
        System.arraycopy(data, POS_LENGTH, actualLenBytes, 0,  4);
        chunkLength = getInt(actualLenBytes);

        chunkData = new byte[chunkLength];
        System.arraycopy(data, PACKET_HEADER_SIZE, chunkData, 0, chunkLength);

        byte[] seqNoBytes = new byte[4];
        System.arraycopy(data, POS_SEQ_NO, seqNoBytes, 0, 4);
        seqNo = getInt(seqNoBytes);

        byte[] packetTypeBytes = new byte[4];
        System.arraycopy(data, POS_PACKET_TYPE, packetTypeBytes, 0, 4);
        packetType = getInt(packetTypeBytes);

        byte[] receivedChecksum = new byte[2];
        System.arraycopy(chunkData, POS_CHECKSUM, receivedChecksum, 0, 2);

        checkSum = computeChecksum(data, 2, PACKET_HEADER_SIZE + chunkLength);
        isCorrupted = (checkSum == getInt(receivedChecksum) );

        port = packet.getPort();
        ip = packet.getAddress();
    }


    public DatagramPacket createDatagramPacket() {
        byte[] packetData = new byte[chunkLength + PACKET_HEADER_SIZE];

        byte[] actualLenBytes = getBytes(chunkLength);
        System.arraycopy(actualLenBytes, 0, packetData, POS_LENGTH, 4);

        byte[] seqNoBytes = getBytes(seqNo);
        System.arraycopy(seqNoBytes, 0, packetData, POS_SEQ_NO, 4);

        System.arraycopy(chunkData, 0, packetData, PACKET_HEADER_SIZE, chunkLength);

        byte[] packetTypeBytes = getBytes(packetType);
        System.arraycopy(packetTypeBytes, 0, packetData, POS_PACKET_TYPE, 4);

        checkSum = computeChecksum(packetData, 2, PACKET_HEADER_SIZE + chunkLength);
        System.arraycopy(getBytes(checkSum), 0, packetData, POS_CHECKSUM, 2);

        return new DatagramPacket(packetData, 0, packetData.length, ip, port);
    }


    public byte[] getChunkData() {
        return chunkData;
    }

    public void setChunkData(byte[] chunkData) {
        this.chunkData = chunkData;
    }


    protected static int computeChecksum(byte[] data, int start, int end) {
        int sum = 0;
        int second;
        for(int i=start; i<end; i+=2) {
            second = i+1 < end ? data[i+1] : 0;
            int num = ((((int)data[i])&0xff) << Byte.SIZE) + ((second)&0xff);
            sum += num;
            if (sum >= (1 << 16))
                sum += 1;
            sum %= (1 << 16);
        }
        sum = ~sum;
        return sum;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }


    protected static byte[] getBytes(int num) {
        byte[] bytes = new byte[Integer.BYTES];
        for(int i = 0; i < Integer.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    protected static byte[] getBytes(long num) {
        byte[] bytes = new byte[Long.BYTES];
        for(int i = 0; i < Long.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    protected static byte[] getBytes(String str) {
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


    protected String getString(byte[] data) {
        return new String(data);
    }


    public static int getType(DatagramPacket packet){
        byte[] data = packet.getData();
        byte[] packetTypeBytes = new byte[4];
        System.arraycopy(data, POS_PACKET_TYPE, packetTypeBytes, 0, 4);

        return getInt(packetTypeBytes);
    }

    public boolean isCorrupted(){
        return isCorrupted;
    }
}
