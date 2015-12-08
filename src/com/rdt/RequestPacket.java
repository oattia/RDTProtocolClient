package com.rdt;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class RequestPacket extends Packet {

    private String fileName;

    public RequestPacket(String requestedFilePath, int port, InetAddress ip){
        this.packetType = T_REQUEST;

        this.fileName = requestedFilePath;
        this.chunkData = getBytes(fileName);
        this.chunkLength = this.chunkData.length;
        this.seqNo = 0;// checksum can't be computed without creating the datagramPacket
        this.port = port;
        this.ip = ip;
    }

    public RequestPacket(DatagramPacket packet) {
        super(packet);
        this.packetType = T_REQUEST;
    }

    public String getFileName() {
        return getString(chunkData);
    }
}
