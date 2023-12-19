package com.myhome.server.api.controller;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.service.*;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@RestController()
@RequestMapping("/file")
public class FileServerController {

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    private final static String TOPIC_CLOUD_LOG = "cloud-log-topic";

    @Autowired
    FileServerThumbNailService thumbNailService;

    @Autowired
    FileServerPublicService service;

    @Autowired
    FileServerPrivateService privateService;

    @Autowired
    FileDefaultPathRepository defaultPathRepository;

    @Autowired
    LogComponent logComponent;

    /**
     * COMMON PART
     * check File(Public, Private both) : O
     * downloadThumbnail : O
     * checkThumbnail : O
     * getDefaultPath : O
     */
    @GetMapping("/checkFileState")
    public ResponseEntity<String> checkFileState(){
        StringWriter sw = new StringWriter();
        try {
            service.publicFileStateCheck();
        }
        catch (Exception e){
            e.printStackTrace(new PrintWriter(sw));
            return new ResponseEntity<>("publicFileCheck error : "+sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            privateService.privateFileCheck();
        }
        catch (Exception e){
            e.printStackTrace(new PrintWriter(sw));
            return new ResponseEntity<>("privateFileCheck error : "+sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("File check Success", HttpStatus.OK);
    }

    @GetMapping("/checkPublicFileStatus")
    public ResponseEntity<String> checkPublicFileStatus(){
        StringWriter sw = new StringWriter();
        try {
            service.publicFileStateCheck();
        }
        catch (Exception e){
            e.printStackTrace(new PrintWriter(sw));
            return new ResponseEntity<>("publicFileCheck error : "+sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Public file check Success", HttpStatus.OK);
    }

    @GetMapping("/checkPrivateFileStatus")
    public ResponseEntity<String> checkPrivateFileStatus(){
        StringWriter sw = new StringWriter();
        try {
            privateService.privateFileCheck();
        }
        catch (Exception e){
            e.printStackTrace(new PrintWriter(sw));
            return new ResponseEntity<>("Private FileCheck error : "+sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Private file check Success", HttpStatus.OK);
    }

    @GetMapping("/downloadThumbNail/{uuid}")
    public ResponseEntity<Resource> downloadThumbNail(@PathVariable String uuid){
        FileServerThumbNailEntity entity = thumbNailService.findByUUID(uuid);
        if(entity != null){
            Path path = Paths.get(entity.getPath());
            try{
                HttpHeaders httpHeaders = service.getHttpHeader(path, entity.getOriginName());
                Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadThumbnail error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/checkThumbNailFile/{uuid}")
    public ResponseEntity<Boolean> checkThumbNailFileExist(@PathVariable String uuid){
        boolean result = thumbNailService.checkThumbNailExist(uuid);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/getDefaultPath")
    public ResponseEntity<List<FileDefaultPathEntity>> getDefaultPath(){
        List<FileDefaultPathEntity> list = defaultPathRepository.findAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    /**
     * file server public
     * upload : O
     * download : O
     * move : O
     * update : O
     * delete : O
     * getList Page : O
     * getTrashList Page : O
     * */

    @GetMapping("/getPublicFileInfo") // get PublicFile info
    public ResponseEntity<FileServerPublicEntity> getPublicFileInfo(@RequestParam String path){
        FileServerPublicEntity fileServerPublicService = service.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }
    @GetMapping("/getPublicFilesInfo") // get PublicFiles info list
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFilesInfo(@RequestParam String location){
        List<FileServerPublicEntity> list = service.findByLocation(location, 0);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @GetMapping("/getPublicFilesPageInfo") // get PublicFiles info list
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFilesPageInfo(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPublicEntity> list = service.findByLocationPage(location, 0, size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @GetMapping("/movePublicFileInfo")
    public ResponseEntity<Integer> movePublicFileInfo(@RequestParam(value="path") String path, @RequestParam(value="location") String location){
        int result = service.moveFile(path, location);
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PostMapping("/downloadPublicFile")
    public ResponseEntity<Resource> downloadPublicFile(@RequestBody FileServerPublicDto dto){
        System.out.println("downloadPublic : " + dto.toString());
        FileServerPublicEntity entity = service.findByUuidName(dto.getUuidName());
        if(entity != null){
            Path path = Paths.get(entity.getPath()); // file path setting
            try{
                HttpHeaders httpHeaders = service.getHttpHeader(path, dto.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPublicFile error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/downloadPublicMedia/{uuid}")
    public ResponseEntity<Resource> downloadPublicMedia(@PathVariable String uuid){
        FileServerPublicEntity entity = service.findByUuidName(uuid);
        if(entity != null){
            Path path = Paths.get(entity.getPath());
            try{
                HttpHeaders httpHeaders = service.getHttpHeader(path, entity.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPublicMedia error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/getPublicTrashFiles")
    public ResponseEntity<List<FileServerPublicEntity>> getPublicTrashFiles(@RequestParam String location){
        List<FileServerPublicEntity> list = service.findByLocation(location, 1);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/getPublicTrashPageFiles")
    public ResponseEntity<List<FileServerPublicEntity>> getPublicTrashPageFiles(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPublicEntity> list = service.findByLocationPage(location, 1, size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @PostMapping("/uploadPublicFile") // upload files
    public ResponseEntity<List<String>> uploadPublicFile(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model)
    {
        System.out.println("uploadPublicFile : " + path);
        List<String> resultArr = service.uploadFiles(uploadFile, path, model);
        return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
    }

    @PostMapping("/mkdirPublic")
    public ResponseEntity<Void> mkdirPublic(@RequestParam String path){
        if(service.mkdir(path)){
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updatePublicFileInfo")
    public ResponseEntity<Integer> updatePublicFileInfo(@RequestBody FileServerPublicDto dto){
        int result = service.updateByFileServerPublicEntity(new FileServerPublicEntity(dto));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/restorePublicFile")
    public ResponseEntity<Integer> restorePublic(@RequestParam String uuid){
        int result = service.restore(uuid);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/deletePublicFileToTrash")
    public ResponseEntity<Long> deletePublicFileTrash(@RequestParam String uuid){
        long result = service.moveTrash(uuid);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/deletePublicFileInfo/{path}")
    public ResponseEntity<Long> deletePublicFileInfo(@PathVariable String path){
        long result = service.deleteByPath(path); // delete file
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    /**
     * file server private
     * upload : O
     * download : O
     * move : O
     * update : O
     * delete : O
     * getList Page : O
     * getTrashList Page : O
     * */

    @GetMapping("/getPrivateFileInfo") // get PrivateFile info
    public ResponseEntity<FileServerPrivateEntity> getPrivateFileInfo(@RequestParam String path){
        FileServerPrivateEntity fileServerPublicService = privateService.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }
    @GetMapping("/getPrivateFilesInfo") // get PrivateFile info list
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateFilesInfo(@RequestParam String location){
        List<FileServerPrivateEntity> list = privateService.findByLocation(location, 0);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/getPrivateFilesPageInfo") // get PrivateFile info list
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateFilesPageInfo(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPrivateEntity> list = privateService.findByLocationPage(location, 0, size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/movePrivateFileInfo")
    public ResponseEntity<Integer> movePrivateFileInfo(@RequestParam String path, @RequestParam String location, @RequestParam String accessToken){
        int result = privateService.moveFile(path, location, accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PostMapping("/downloadPrivateFile")
    public ResponseEntity<Resource> downloadPrivateFile(@RequestBody FileServerPrivateDto dto){
        FileServerPrivateEntity entity = privateService.findByUuid(dto.getUuidName());
        if(entity != null){
            Path path = Paths.get(entity.getPath()); // file path setting
            try{
                System.out.println("FileServerControl private file download : " + dto);
                HttpHeaders httpHeaders = privateService.getHttpHeaders(path, dto.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPrivateFile error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/downloadPrivateMedia/{uuid}")
    public ResponseEntity<Resource> downloadPrivateMedia(@PathVariable String uuid){
        FileServerPrivateEntity entity = privateService.findByUuid(uuid);
        if(entity != null){
            Path path = Paths.get(entity.getPath());
            try{
                HttpHeaders httpHeaders = privateService.getHttpHeaders(path, entity.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPrivateMedia error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
    @GetMapping("/getPrivateTrashFilesInfo")
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateTrashFiles(@RequestParam String location){
        List<FileServerPrivateEntity> list = privateService.findByLocation(location, 1);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/getPrivateTrashFilesPageInfo")
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateTrashPageFiles(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPrivateEntity> list = privateService.findByLocationPage(location, 1, size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/uploadPrivateFile/{token}") // upload PrivateFiles
    public ResponseEntity<List<String>> uploadPrivateFileInfo(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model, @PathVariable String token)
    {
        List<String> resultArr = privateService.uploadFiles(uploadFile, path, token, model);
        return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
    }

    @PostMapping("/mkdirPrivate")
    public ResponseEntity<Void> mkdirPrivate(@RequestParam String path, @RequestParam String token){
        if(privateService.mkdir(path, token)) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updatePrivateFileInfo")
    public ResponseEntity<Integer> updatePrivateFileInfo(@RequestBody FileServerPrivateDto dto){
        int result = privateService.updateByFileServerPrivateEntity(new FileServerPrivateEntity(dto));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PutMapping("/restorePrivateFile")
    public ResponseEntity<Integer> restorePrivateFile(@RequestParam(value="uuid") String uuid, @RequestParam(value="accessToken") String accessToken){
        int result = privateService.restore(uuid, accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @DeleteMapping("/deletePrivateFileToTrash")
    public ResponseEntity<Long> deletePrivateFileTrash(@RequestParam(value="uuid") String uuid, @RequestParam(value="accessToken") String accessToken){
        long result = privateService.moveTrash(uuid, accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @DeleteMapping("/deletePrivateFileInfo")
    public ResponseEntity<Long> deletePrivateFileInfo(@RequestParam(value="path") String path, @RequestParam(value="accessToken") String accessToken){
        long result = privateService.deleteByPath(path, accessToken); // delete file
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
