package com.rdt.utils;

import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

public class TimeoutTimerTask extends TimerTask implements Publisher {

    private DatagramSocket socket;
    private Set<Subscriber> subscribers = new HashSet<>();

    public TimeoutTimerTask(DatagramSocket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        socket.close();
        publish(new TimeoutEvent());
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
}
