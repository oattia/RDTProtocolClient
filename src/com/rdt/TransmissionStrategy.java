package com.rdt;

public abstract class TransmissionStrategy {

    // To check if done ...
    protected int numOfPackets;
    protected long initSeqNo;

    // Running variables
    protected int windowSize;
    protected long base; // first not acked.
    protected long nextAckNum;
    protected long nextSeqToWrite;

    public static final String STOP_AND_WAIT = "StopAndWait";
    public static final String GO_BACK_N = "GoBackN";
    public static final String SELECTIVE_REPEAT = "SelectiveRepeat";


    public TransmissionStrategy(int numOfPackets, long initSeqNo, int initWindowSize){
        this.numOfPackets = numOfPackets;
        this.initSeqNo = initSeqNo;
        this.windowSize = initWindowSize;

        this.nextAckNum = initSeqNo;
        this.base = initSeqNo;
        this.nextSeqToWrite = initSeqNo;
    }

    abstract boolean isDone();

    abstract void sentAck(long seqNo);

    abstract long getNextAckNo();

    /*
    * @param    seqNo = -1L indicates corrupted data received
    * @return   True if the packet should be kept, False to discard
    * */
    abstract boolean receivedData(long seqNo);

    abstract long getNextSeqNoToWrite();

    abstract void wroteSeqNo(long seqNoToWrite);

    public long[] getWindow(){
        long[] w = { base, base + windowSize };
        return w;
    }

    public int getNumOfPackets() {
        return numOfPackets;
    }

    public long getInitSeqNo() {
        return initSeqNo;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public long getBase() {
        return base;
    }

    public long getNextAckNum() {
        return nextAckNum;
    }
}