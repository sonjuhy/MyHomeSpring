package com.myhome.server.api.controller;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.service.*;
import com.myhome.server.db.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@RestController()
@RequestMapping("/file")
public class FileServerController {

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    @Autowired
    FileServerPublicService service;

    @Autowired
    FileServerThumbNailService thumbNailService;

    @Autowired
    FileServerPrivateService privateService;

    @GetMapping("/checkFileState")
    public ResponseEntity<String> checkFileState(){
        try {
            service.publicFileStateCheck();
        }
        catch (Exception e){
            return new ResponseEntity<>("publicFileCheck error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            privateService.privateFileCheck();
        }
        catch (Exception e){
            return new ResponseEntity<>("privateFileCheck error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
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
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    /**
     * file server public
     * upload : O
     * download : O
     * move : O
     * update : O
     * delete : O
     * */

    @GetMapping("/getPublicFileInfo") // get PublicFile info
    public ResponseEntity<FileServerPublicEntity> getPublicFileInfo(@RequestParam String path){
        FileServerPublicEntity fileServerPublicService = service.findByPath(path);
        if(fileServerPublicService != null) return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
    @GetMapping("/getPublicFilesInfo") // get PublicFiles info list
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFilesInfo(@RequestParam String location){

        List<FileServerPublicEntity> list = service.findByLocation(location, 0);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
    @GetMapping("/movePublicFileInfo")
    public ResponseEntity<Integer> movePublicFileInfo(@RequestParam(value="path") String path, @RequestParam(value="location") String location){
        int result = service.moveFile(path, location);
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
//        System.out.println("MovePublicFileInfo : " + path + ", " + location);
//        return new ResponseEntity<>(0, HttpStatus.OK);
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
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getPublicTrashFiles")
    public ResponseEntity<List<FileServerPublicEntity>> getPublicTrashFiles(@RequestParam String location){
        List<FileServerPublicEntity> list = service.findByLocation(location, 1);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/uploadPublicFile") // upload files
    public ResponseEntity<List<String>> uploadPublicFile(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model)
    {
        System.out.println("uploadPublicFile : " + path);
        List<String> resultArr = service.uploadFiles(uploadFile, path, model);
        if(resultArr != null && resultArr.size() > 0) return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/restorePublicFile")
    public ResponseEntity<Integer> restorePublic(@RequestParam String uuid){
        int result = service.restore(uuid);
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/deletePublicFileToTrash")
    public ResponseEntity<Long> deletePublicFileTrash(@RequestParam String uuid){
        long result = service.moveTrash(uuid);
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/deletePublicFileInfo/{path}")
    public ResponseEntity<Long> deletePublicFileInfo(@PathVariable String path){
        long result = service.deleteByPath(path); // delete file
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    /**
     * file server private
     * upload : O
     * download : O
     * move : O
     * update : O
     * delete : O
     * */

    @GetMapping("/getPrivateFileInfo") // get PrivateFile info
    public ResponseEntity<FileServerPrivateEntity> getPrivateFileInfo(@RequestParam String path){
        FileServerPrivateEntity fileServerPublicService = privateService.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }
    @GetMapping("/getPrivateFilesInfo") // get PrivateFile info list
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateFilesInfo(@RequestParam String location){
        List<FileServerPrivateEntity> list = privateService.findByLocation(location, 0);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
    @GetMapping("/getPrivateTrashFiles")
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateTrashFiles(@RequestParam String location){
        List<FileServerPrivateEntity> list = privateService.findByLocation(location, 1);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
    @PostMapping("/uploadPrivateFile/{token}") // upload PrivateFiles
    public ResponseEntity<List<String>> uploadPrivateFileInfo(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model, @PathVariable String token)
    {
        List<String> resultArr = privateService.uploadFiles(uploadFile, path, token, model);
        if(resultArr != null && resultArr.size() > 0) return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PutMapping("/restorePrivateFile")
    public ResponseEntity<Integer> restorePrivateFile(@RequestParam(value="uuid") String uuid, @RequestParam(value="accessToken") String accessToken){
        int result = privateService.restore(uuid, accessToken);
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @DeleteMapping("/deletePrivateFileToTrash")
    public ResponseEntity<Long> deletePrivateFileTrash(@RequestParam(value="uuid") String uuid, @RequestParam(value="accessToken") String accessToken){
        long result = privateService.moveTrash(uuid, accessToken);
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @DeleteMapping("/deletePrivateFileInfo")
    public ResponseEntity<Long> deletePrivateFileInfo(@RequestParam(value="path") String path, @RequestParam(value="accessToken") String accessToken){
        long result = privateService.deleteByPath(path, accessToken); // delete file
        if(result == 0) return new ResponseEntity<>(result, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
