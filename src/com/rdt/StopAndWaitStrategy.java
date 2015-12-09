package com.rdt;

public class StopAndWaitStrategy extends TransmissionStrategy {

    public StopAndWaitStrategy(int numOfPackets, long initSeqNo) {
        super(numOfPackets, initSeqNo, 1);
        nextAckNum = 1;
        nextSeqToWrite = 1;
        System.out.println(base + " : " + numOfPackets + " : " + initSeqNo);
    }

    @Override
    public boolean isDone() {
        return (base == (numOfPackets + initSeqNo)) && (nextAckNum == base) && (nextSeqToWrite == base);
    }

    @Override
    void sentAck(long seqNo) {
        if(seqNo == nextAckNum)
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
        }
        return false;
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
        if(seqNoToWrite == nextSeqToWrite)
            nextSeqToWrite++;
    }
}