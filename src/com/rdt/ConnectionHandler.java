package com.rdt;

import com.rdt.utils.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler implements Subscriber {

    private BlockingQueue<Event> mailbox;
    private TransmissionStrategy strategy;
    private DatagramSocket socket;
    private SocketListener socketListener;
    private Thread socketListenerThread;
    private FileOutputStream fileStream;
    private Random rng;
    private float plp;         // packet loss probability: from 0 to 100
    private float pep = 0.0f;         // packet error probability: from 0 to 100
    private String strategyName;
    private String fileName;
    private int windowSize;
    private int serverPort;
    private InetAddress serverIP;
    private Map<Long, DataPacket> receivedPackets;
    private TimeoutTimerTask current_ttt;

    private static final Timer TIMER = new Timer(true);
    private static final long NICENESS = 1L; // milliseconds to sleep every iteration
    private static final int CHUNK_SIZE = 1024;
    private static final long MAX_PKT_TIMEOUT = 6_000_000L;
    private static final int EXPECTED_PKT_LENGTH = 2048;


    public ConnectionHandler(ClientConfig clientConfig) {
        this.strategyName = clientConfig.getStrategy();
        this.fileName = clientConfig.getFileName();
        this.serverPort = clientConfig.getServerPort();
        this.serverIP = clientConfig.getServerIP();

        this.plp = clientConfig.getPlp();
        this.rng = new Random(clientConfig.getRngSeed());
        this.windowSize = clientConfig.getInitialWindowSize();

        mailbox = new LinkedBlockingQueue<>();
        receivedPackets = new HashMap<>();
    }

    private boolean init() {

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            return false;
        }

        long fileLen = handShake_getFileLen();
        System.out.println("lenn: " + fileLen);
        if(fileLen == -1L)
            return false;

        File file;
        try {
            file = new File(fileName);
            fileStream = new FileOutputStream(file);
        } catch(IOException e) {
            return false;
        }

        int numOfChunx = (int) Math.ceil(((double)fileLen / (double)CHUNK_SIZE));
        System.out.println("CAUNX: " + numOfChunx);
        int initialSeqNo = 1;

        if (strategyName == null) {
            throw new IllegalArgumentException();
        } else if(strategyName.equalsIgnoreCase(TransmissionStrategy.STOP_AND_WAIT)){
            strategy = new StopAndWaitStrategy(numOfChunx, initialSeqNo);
        } else if (strategyName.equalsIgnoreCase(TransmissionStrategy.SELECTIVE_REPEAT)){
            strategy = new SelectiveRepeatStrategy(numOfChunx, initialSeqNo, windowSize);
        } else if (strategyName.equalsIgnoreCase(TransmissionStrategy.GO_BACK_N)) {
            strategy = new GoBackNStrategy(numOfChunx, initialSeqNo, windowSize);
        } else {
            throw new IllegalArgumentException();
        }

        socketListener = new SocketListener(socket);
        socketListener.subscribe(this);

        socketListenerThread = new Thread(socketListener);
        socketListenerThread.start();
        return true;
    }


    private long handShake_getFileLen() {
        long fileLen;

        // THREE-WAY HAND SHAKING
        // send request packet
        RequestPacket pkt = new RequestPacket(fileName, serverPort, serverIP);
        try {
            socket.send(pkt.createDatagramPacket());
        } catch (IOException e) {
            System.err.println("Could not send request");
            return -1L;
        }

        // receive server response
        current_ttt = new TimeoutTimerTask(this.socket);
        current_ttt.subscribe(this);
        TIMER.schedule(current_ttt, MAX_PKT_TIMEOUT);
        DatagramPacket dtgrm = new DatagramPacket(new byte[EXPECTED_PKT_LENGTH], EXPECTED_PKT_LENGTH);
        try {
            socket.receive(dtgrm);

            current_ttt.cancel();
            if(Packet.getType(dtgrm) == Packet.T_ACK) {
                AckPacket ack = new AckPacket(dtgrm);
                if (ack.isCorrupted()) {
                    System.err.println("Server sent corrupted ACK");
                    return -1L;
                } else {
                    fileLen = ack.getAckNo();
                    serverPort = ack.getPort();
                    socket.connect(serverIP, serverPort);
                }
            }else {
                System.err.println("File Not found");
                return -1L;
            }
        } catch (IOException e) {
            System.err.println("No response received from server");
            return -1L;
        }

        // send ACK packet
        AckPacket ack3 = new AckPacket(0, serverPort, serverIP);
        try {
            socket.send(ack3.createDatagramPacket());
        } catch (IOException e) {
            System.err.println("Could not send ack");
            return -1L;
        }

        return fileLen;
    }

    public void run() {
        if(!init())
            return;

        current_ttt = new TimeoutTimerTask(this.socket);
        current_ttt.subscribe(this);
        TIMER.schedule(current_ttt, MAX_PKT_TIMEOUT);

        boolean error = false;
        while (!strategy.isDone() && !error) {
            // send ACK's
            long seqNo;
            while((seqNo = strategy.getNextAckNo()) != -1L) {
                AckPacket pkt = new AckPacket(seqNo, serverPort, serverIP);
                sendAckPacket(pkt);
            }

            // write to file
            long seqNoToWrite;
            while((seqNoToWrite = strategy.getNextSeqNoToWrite()) != -1L) {
                try {
                    fileStream.write(receivedPackets.get(seqNoToWrite).getChunkData() );
                    receivedPackets.remove(seqNoToWrite);
                    strategy.wroteSeqNo(seqNoToWrite);
                } catch (IOException e) {
                    System.err.println("Could not write to file");
                    error = true;
                    break;
                }
            }

            try {
                fileStream.flush();
            } catch (IOException e) { }

            // Check if new DataPackets arrived OR timer fired
            if(! consumeMailbox() )     // timer fired: server timed-out
                break;

            try {
                Thread.sleep(NICENESS);
            } catch (InterruptedException e){ }
        }
        System.out.println(strategy.isDone() + " : " + error);
        clean();
    }

    private void clean() {
        socket.close();
        current_ttt.cancel();
        try {
            fileStream.close();
        } catch (IOException e) {

        }
        TIMER.purge();
    }


    private void sendAckPacket(AckPacket pkt) {
        try {
            if(rng.nextFloat() < pep) { // send corrupted
                pkt.setChecksum(pkt.getCheckSum() * 2 + 1);
                if(pkt.isCorrupted()) {
                    System.out.println("Corrupted data in " + pkt.getAckNo());
                } else {
                    System.out.println("MISTAKE");
                }
            } else {
                pkt.refreshChecksum();
            }
            if(rng.nextFloat() >= plp) {
                socket.send(pkt.createDatagramPacket());
                System.out.println("Sent Ack for: " + pkt.getAckNo());
            } else {
                System.out.println("Dropped Ack for: " + pkt.getAckNo());
            }

            strategy.sentAck(pkt.getAckNo());
        } catch (IOException e){
            //TODO
        }
    }

    private boolean consumeMailbox() {     // returns false if timer fired => server timed-out
        while (!mailbox.isEmpty()) {
            Event e = mailbox.poll();
            if(e instanceof TimeoutEvent) {
                return false;
            } else if(e instanceof DataRecvEvent) {
                handleDataRecvEvent((DataRecvEvent) e);
                current_ttt.cancel();
                current_ttt = new TimeoutTimerTask(this.socket);
                current_ttt.subscribe(this);
                TIMER.schedule(current_ttt, MAX_PKT_TIMEOUT);
            }
        }
        return true;
    }

    private void handleDataRecvEvent(DataRecvEvent e) {
        long seqNo = e.getSeqNo();
        if(e.getDataPkt().isCorrupted()) {
            System.out.println("Received corrupted " + e.getSeqNo());
            return;
        } else {
            System.out.println("Received correctly " + e.getSeqNo());
        }

        if(strategy.receivedData(seqNo))      // should I keep that packet?
            receivedPackets.put(seqNo, e.getDataPkt());
    }


    @Override
    public void update(Event e) {
        if(e instanceof DataRecvEvent) {
            mailbox.offer(e);
        } else if (e instanceof TimeoutEvent) {
            mailbox.offer(e);
        } else {
            // Do nothing ...
        }
    }
}
