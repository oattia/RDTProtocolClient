package com.rdt;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class DataPacket extends Packet {


    public DataPacket(byte[] chunkData, int actualLen, long seqNo, int port, InetAddress ip){
        this.packetType = T_DATA;
        this.chunkData = chunkData;
        this.chunkLength = actualLen;
        this.seqNo = seqNo;// checksum can't be computed without creating the datagramPacket
        this.port = port;
        this.ip = ip;
    }


    public DataPacket(DatagramPacket packet){
        super(packet);
    }

    public long getSeqNo() {
        return seqNo;
    }
}