package com.rdt;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class DataPacket extends Packet {

    public DataPacket(byte[] chunkData, int actualLen, long seqNo, int port, InetAddress ip){
        this.chunkLength = actualLen;
        this.chunkData = chunkData;
        this.seqNo = seqNo;
        this.packetType = T_DATA;
        this.port = port;
        this.ip = ip;
        fillPacketDataFromAtt();
    }

    public DataPacket(DatagramPacket pkt){
        if(pkt == null)
            throw new IllegalArgumentException("Null Datagram");
        this.packetData = pkt.getData();
        this.port = pkt.getPort();
        this.ip = pkt.getAddress();
        fillAttsFromPacketData();
    }

    public long getSeqNo() {
        return seqNo;
    }
}