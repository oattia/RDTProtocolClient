package com.rdt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

public class ClientConfig {

    private InetAddress serverIP;
    private int serverPort;
    private int clientPort;
    private String fileName;
    private long rngSeed;
    private float plp;
    private float pep = 0.05f;
    private String strategy;
    private int initialWindowSize;      // As far as I understand: should be set to the same at the server

    private ClientConfig() {

    }

    public static ClientConfig parseConfigFile(String fileName) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            throw e;
        }

        ClientConfig cc = new ClientConfig();
        cc.serverIP = InetAddress.getByName(in.readLine());
        cc.serverPort = Integer.parseInt(in.readLine());
        cc.clientPort = Integer.parseInt(in.readLine());
        cc.fileName = in.readLine();
        cc.rngSeed= Long.parseLong(in.readLine());
        cc.plp = Float.parseFloat(in.readLine());
        cc.strategy = in.readLine();
        cc.initialWindowSize = Integer.parseInt(in.readLine());

        cc.strategy = cc.strategy == null ? "StopAndWait" : cc.strategy;
        in.close();
        return cc;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getClientPort() { return clientPort; }

    public InetAddress getServerIP() { return serverIP; }

    public String getFileName() { return fileName; }

    public int getInitialWindowSize() { return initialWindowSize; }

    public float getPlp() {
        return plp;
    }

    public float getPep() {
        return pep;
    }

    public long getRngSeed() { return rngSeed; }

    public String getStrategy() {
        return strategy;
    }
}
