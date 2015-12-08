package com.rdt;

public class SelectiveRepeatStrategy extends TransmissionStrategy {

    public SelectiveRepeatStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    void sentAck(long seqNo) {

    }

    @Override
    public long getNextAckNo() {
        return 0;
    }

    @Override
    boolean receivedData(long seqNo) {       // seqNo = -1L indicates corrupted data received
        return false;
    }

    @Override
    long getNextSeqNoToWrite() {
        return 0;
    }

    @Override
    void wroteSeqNo(long seqNoToWrite) {

    }

}
