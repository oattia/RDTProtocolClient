package com.rdt;

public class GoBackNStrategy extends TransmissionStrategy {
    public GoBackNStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
    }

    @Override
    boolean isDone() {
        return false;
    }

    @Override
    void sentAck(long seqNo) {

    }

    @Override
    long getNextAckNo() {
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
