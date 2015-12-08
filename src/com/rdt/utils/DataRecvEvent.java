package com.rdt.utils;

import com.rdt.DataPacket;

import java.net.DatagramPacket;

public class DataRecvEvent implements Event {

    private DataPacket packet;

    public DataRecvEvent(DatagramPacket pkt) {
        this.packet = new DataPacket(pkt);
    }

    public long getSeqNo() {
        return packet.getSeqNo();
    }

    public DataPacket getDataPkt() { return packet; }
}
