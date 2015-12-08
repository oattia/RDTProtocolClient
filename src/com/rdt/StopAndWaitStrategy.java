package com.rdt;

public class StopAndWaitStrategy extends TransmissionStrategy {

    protected long nextAckNum;
    protected long nextSeqToWrite;

    public StopAndWaitStrategy(int numOfPackets, long initSeqNo) {
        super(numOfPackets, initSeqNo, 1);
        nextAckNum = 1;
        nextSeqToWrite = 1;
        System.out.println(base + " : " + numOfPackets + " : " + initSeqNo);
    }

    @Override
    public boolean isDone() {
        return (base == (numOfPackets + initSeqNo)) && (nextAckNum == base);
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
            return -1L;
    }

    @Override
    boolean receivedData(long seqNo) {
        if(seqNo == base) {
            base++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    long getNextSeqNoToWrite() {
        if(nextSeqToWrite < base)
            return nextSeqToWrite;
        else
            return -1L;
    }

    @Override
    void wroteSeqNo(long seqNoToWrite) {
        nextSeqToWrite++;
    }
}