package com.rdt;

import com.rdt.utils.DataRecvEvent;
import com.rdt.utils.TimeoutTimerTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;

public class Client {

    private ClientConfig clientConfig;

    public Client(ClientConfig clientConfig){

        this.clientConfig = clientConfig;
    }

    public void run() {

        ConnectionHandler connectionHandler = new ConnectionHandler(clientConfig);
        connectionHandler.run();

    }
}