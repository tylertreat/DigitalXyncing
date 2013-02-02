package com.digitalxyncing.client;

import java.io.IOException;
import java.net.*;

public class DatagramClient {

    private static final int PORT = 1337;

    private String destination;

    public DatagramClient(String destination) {
        this.destination = destination;
    }

    public void sendData() {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(destination);
            System.out.println("Attemping to connect to " + address + ") via UDP port 9876");

            byte[] sendData = new byte[1024];

            String message = "This is a test!";
            sendData = message.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, PORT);
            while (true) {
                clientSocket.send(sendPacket);
                System.out.println("Data sent");
                Thread.sleep(5);
            }
        } catch (SocketException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void listen() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(PORT);
            byte[] receiveData = new byte[1024];
            while (true) {
                receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                System.out.println("Received " + new String(receivePacket.getData()));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}