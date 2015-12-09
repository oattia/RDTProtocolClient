package com.rdt;

public class GoBackNStrategy extends TransmissionStrategy {

    private boolean lock;
    
    public GoBackNStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
        lock = false;
    }

    @Override
    boolean isDone() {
        return base == (numOfPackets + initSeqNo) && (nextSeqToWrite == base);
    }

    @Override
    void sentAck(long seqNo) {
    }

    @Override
    long getNextAckNo() {
        if(!lock)
            return -1L;

        long toRet = base-1;
        lock = false;
        return toRet;
    }

    @Override
    boolean receivedData(long seqNo) {       // seqNo = -1L indicates corrupted data received
        if(seqNo == base) {
            base++;
            lock = true;
            return true;
        } else if(seqNo < base) {
            lock = false;
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
        if(seqNoToWrite == nextSeqToWrite)
            nextSeqToWrite++;
    }

}
