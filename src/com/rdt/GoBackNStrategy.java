package com.rdt;

public class GoBackNStrategy extends TransmissionStrategy {
    public GoBackNStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
    }

    @Override
    boolean isDone() {
        return base == (numOfPackets + initSeqNo) && (nextSeqToWrite == base);
    }

    @Override
    void sentAck(long seqNo) {
        nextAckNum++;
    }

    @Override
    long getNextAckNo() {
        if(nextAckNum < base)
            return nextAckNum;
        else
            return -1;
    }

    @Override
    boolean receivedData(long seqNo) {       // seqNo = -1L indicates corrupted data received
        if(seqNo == base) {
            base++;
            return true;
        }
        return false;
    }

    @Override
    long getNextSeqNoToWrite() {
        if(nextSeqToWrite < base)
            return nextSeqToWrite;
        else
            return -1;
    }

    @Override
    void wroteSeqNo(long seqNoToWrite) {
        nextSeqToWrite++;
    }

}
