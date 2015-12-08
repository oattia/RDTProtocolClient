package com.rdt;

import com.rdt.utils.DataRecvEvent;
import com.rdt.utils.Event;
import com.rdt.utils.Publisher;
import com.rdt.utils.Subscriber;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;

public class SocketListener implements Runnable, Publisher {

    private static final long NICENESS = 50;
    private static final int EXPECTED_LENGTH = 2048;

    private DatagramSocket socket;
    private Set<Subscriber> subscribers = new HashSet<>();

    public SocketListener(DatagramSocket socket) {
        if(socket == null || socket.isClosed())
            throw new IllegalArgumentException();

        this.socket = socket;
    }

    @Override
    public void publish(Event e) {
        for(Subscriber s : subscribers) {
            s.update(e);
        }
    }

    @Override
    public void subscribe(Subscriber s) {
        subscribers.add(s);
    }

    @Override
    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }

    @Override
    public void run() {
        while(!socket.isClosed()) {

            DatagramPacket dtgrm = new DatagramPacket(new byte[EXPECTED_LENGTH], EXPECTED_LENGTH);

            try {
                socket.receive(dtgrm);
                DataRecvEvent dataE = new DataRecvEvent(dtgrm);
                publish(dataE);
            } catch (IOException e) {
               continue;
            }

            try {
                Thread.sleep(NICENESS);
            } catch (InterruptedException e){
                // TODO
            }

        }
    }
}
