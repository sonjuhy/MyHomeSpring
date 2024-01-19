package com.myhome.server.api.service;

public interface FileServerCommonService {
    int[] getStorageUsage(String mode);
    String changeUnderBarToSeparator(String path);
    String changeSeparatorToUnderBar(String path);
}
