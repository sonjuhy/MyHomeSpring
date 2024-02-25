package com.myhome.server.api.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;

public interface FileServerCommonService {
    int[] getStorageUsage(String mode);
    Resource getDefaultVideoIconFile();
    Resource getDefaultImageIconFile();
    String changeUnderBarToSeparator(String path);
    String changeSeparatorToUnderBar(String path);
    ResponseEntity<Resource> downloadThumbNail(String uuid);
}
