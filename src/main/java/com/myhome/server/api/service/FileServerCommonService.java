package com.myhome.server.api.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FileServerCommonService {
    int[] getStorageUsage(String mode);
    String changeUnderBarToSeparator(String path);
    String changeSeparatorToUnderBar(String path);
    ResponseEntity<Resource> downloadThumbNail(String uuid);
}
