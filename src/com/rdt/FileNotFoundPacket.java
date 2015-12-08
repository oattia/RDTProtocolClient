package com.rdt;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class FileNotFoundPacket extends Packet {

    private String fileName;

    public FileNotFoundPacket(String requestedFilePath, int port, InetAddress ip){
        this.packetType = T_FILE_NOT_FND;

        this.fileName = requestedFilePath;
        this.chunkData = getBytes(fileName);
        this.chunkLength = this.chunkData.length;
        this.seqNo = 0;// checksum can't be computed without creating the datagramPacket
        this.port = port;
        this.ip = ip;
    }

    public FileNotFoundPacket(DatagramPacket packet){
        super(packet);
        fileName = getString(chunkData);
    }

}
