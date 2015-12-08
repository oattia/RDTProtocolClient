package com.rdt;

public abstract class TransmissionStrategy {

    // To check if done ...
    protected int numOfPackets;
    protected long initSeqNo;

    // Running variables
    protected int windowSize;
    protected long base; // first not acked.
    protected long nextSeqNum;

    public static final String STOP_AND_WAIT = "StopAndWait";
    public static final String GO_BACK_N = "GoBackN";
    public static final String SELECTIVE_REPEAT = "SelectiveRepeat";


    public TransmissionStrategy(int numOfPackets, long initSeqNo, int initWindowSize){
        this.numOfPackets = numOfPackets;
        this.initSeqNo = initSeqNo;
        this.windowSize = initWindowSize;

        this.nextSeqNum = initSeqNo;
        this.base = initSeqNo;
    }

    abstract boolean isDone();

    abstract void sentAck(long seqNo);

    abstract long getNextAckNo();

    /*
    * @param    seqNo = -1L indicates corrupted data received
    * @return   True if the packet should be kept, False to discard
    * */
    abstract boolean receivedData(long seqNo);

    public long[] getWindow(){
        long[] w = { base, base + windowSize };
        return w;
    }


    abstract long getNextSeqNoToWrite();

    abstract void wroteSeqNo(long seqNoToWrite);
}