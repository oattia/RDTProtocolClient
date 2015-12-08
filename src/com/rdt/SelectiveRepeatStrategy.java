package com.rdt;

import java.util.HashSet;
import java.util.Set;

public class SelectiveRepeatStrategy extends TransmissionStrategy {

    private Set<Long> receivedSeq;      // items added when receive
    private Set<Long> notAcked;

    public SelectiveRepeatStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
        receivedSeq = new HashSet<>();
        notAcked    = new HashSet<>();
        nextSeqToWrite = 1;
    }

    @Override
    public boolean isDone() {
        return (base == (numOfPackets + initSeqNo)) && notAcked.isEmpty() && receivedSeq.isEmpty();
    }

    @Override
    void sentAck(long seqNo) {      // update base with Ack's
        notAcked.remove(seqNo);
        while( receivedSeq.contains(base) ) {
            base++;
        }
    }

    @Override
    public long getNextAckNo() {
        if( notAcked.isEmpty() )
            return -1L;
        else
            return notAcked.iterator().next();
    }

    @Override
    boolean receivedData(long seqNo) {       // seqNo = -1L indicates corrupted data received
        boolean keep;
        if(seqNo >= base) {
            receivedSeq.add(seqNo);
            keep = true;
        } else {
            keep = false;
        }
        notAcked.add(seqNo);
        return keep;
    }

    @Override
    long getNextSeqNoToWrite() {
        if( receivedSeq.contains(nextSeqToWrite) && !notAcked.contains(nextSeqToWrite) )
            return nextSeqToWrite;
        return -1L;
    }

    @Override
    void wroteSeqNo(long seqNo) {
        receivedSeq.remove(seqNo);
        nextSeqToWrite = seqNo+1;
    }

}