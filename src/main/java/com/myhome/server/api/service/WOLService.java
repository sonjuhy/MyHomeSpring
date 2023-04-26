package com.myhome.server.api.service;

import com.myhome.server.db.entity.ComputerEntity;

import java.util.List;

public interface WOLService {
    void wake(String mac);
    boolean ping(String ip, int wait);
    ComputerEntity getComputerInfo(String name);
    List<ComputerEntity> getComputerInfoList();
    List<String> getComputerNameList();
}
