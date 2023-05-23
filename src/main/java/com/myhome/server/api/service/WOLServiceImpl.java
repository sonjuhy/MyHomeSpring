package com.myhome.server.api.service;

import com.myhome.server.db.entity.ComputerEntity;
import com.myhome.server.db.repository.ComputerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class WOLServiceImpl implements  WOLService{

    private final int port = 9;

    @Autowired
    private ComputerRepository repository;

    @Override
    public void wake(String macStr) {
        try{
            // Construct the magic packet
            byte[] macBytes = getMacBytes(macStr);
            byte[] magicBytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                magicBytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < magicBytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, magicBytes, i, macBytes.length);
            }

            // Send the magic packet to the broadcast address
            InetAddress address = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(magicBytes, magicBytes.length, address, port);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            System.out.println("Magic packet sent to " + macStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static byte[] getMacBytes(String macAddress) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macAddress.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
    @Override
    public boolean ping(String ip, int wait) {
        try {
            final InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(wait);
        } catch (Exception ignored) {}

        return false;
    }

    @Override
    public ComputerEntity getComputerInfo(String name) {
        ComputerEntity computerEntity = repository.findByComputerName(name);
        return computerEntity;
    }

    @Override
    public List<ComputerEntity> getComputerInfoList() {
        List<ComputerEntity> list = repository.findAll();
        return list;
    }

    @Override
    public List<String> getComputerNameList() {
        List<ComputerEntity> list = repository.findAll();
        List<String> computerList = new ArrayList<>();
        for(ComputerEntity entity : list){
            computerList.add(entity.getComputerName());
        }
        return computerList;
    }
}
