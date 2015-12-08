package com.rdt;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String configFileName = "./client.in";
        ClientConfig clientConfig = null;
        try {
            clientConfig = ClientConfig.parseConfigFile(configFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Client client = new Client(clientConfig);
        client.run();
    }
}
