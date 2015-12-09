package com.rdt;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class AckPacket extends Packet {


    public AckPacket(long seqNo, int port, InetAddress ip){
        this.chunkLength = 2;
        this.chunkData = new byte[chunkLength];
        for(int i = 0; i< this.chunkLength; i++)        // fill data with specified length of bytes of ones or zeros
            chunkData[i] = (i % 2 == 0 )? ((byte)0x00): ((byte)0xff);

        this.seqNo = seqNo;                     // seqNo represents ackNo
        this.packetType = T_ACK;
        this.port = port;
        this.ip = ip;
        fillPacketDataFromAtt();
    }

    public AckPacket(DatagramPacket pkt) {
        if(pkt == null)
            throw new IllegalArgumentException("Null Datagram");
        this.packetData = pkt.getData();
        this.port = pkt.getPort();
        this.ip = pkt.getAddress();
        fillAttsFromPacketData();
    }

    public long getAckNo() {
        return seqNo;
    }
}
