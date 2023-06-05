package com.myhome.server.api.controller;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.service.*;
import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.FileServerPublicEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
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
    /**
     * file server public
     * upload : O
     * download : O
     * move : O
     * update : O
     * delete : O
     * */

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    @Autowired
    FileServerPublicService service = new FileServerPublicServiceImpl();

    @Autowired
    FileServerThumbNailService thumbNailService = new FileServerThumbNailServiceImpl();

    @Autowired
    FileServerPrivateService privateService = new FileServerPrivateServiceImpl();

    @GetMapping("/checkFileState")
    public ResponseEntity<Void> checkFileState(){
        service.publicFileStateCheck();
//        privateService.privateFileCheck();
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
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getPublicFileInfo") // get PublicFile info
    public ResponseEntity<FileServerPublicEntity> getPublicFileInfo(@RequestParam String path){
        FileServerPublicEntity fileServerPublicService = service.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }
    @GetMapping("/getPublicFilesInfo") // get PublicFiles info list
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFilesInfo(@RequestParam String location){

        List<FileServerPublicEntity> list = service.findByLocation(location);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @GetMapping("/movePublicFileInfo")
    public ResponseEntity<Integer> movePublicFileInfo(@RequestParam String path, @RequestParam String location){
        int result = service.moveFile(path, location);
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
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/uploadPublicFile") // upload files
    public ResponseEntity<List<String>> uploadPublicFile(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model)
    {
        System.out.println("uploadPublicFile : " + path);
        List<String> resultArr = service.uploadFiles(uploadFile, path, model);
        return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
    }
    @PutMapping("/updatePublicFileInfo")
    public ResponseEntity<Integer> updatePublicFileInfo(@RequestBody FileServerPublicDto dto){
        int result = service.updateByFileServerPublicEntity(new FileServerPublicEntity(dto));
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
     * */

    @GetMapping("/getPrivateFileInfo/{path}") // get PrivateFile info
    public ResponseEntity<FileServerPrivateEntity> getPrivateFileInfo(@PathVariable String path){
        FileServerPrivateEntity fileServerPublicService = privateService.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }
    @GetMapping("/getPrivateFilesInfo/{location}") // get PrivateFile info list
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateFilesInfo(@PathVariable String location){
        List<FileServerPrivateEntity> list = privateService.findByLocation(location);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @GetMapping("/movePrivateFileInfo")
    public ResponseEntity<Integer> movePrivateFileInfo(@RequestParam String path, @RequestParam String location){
        int result = service.moveFile(path, location);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PostMapping("/downloadPrivateFile")
    public ResponseEntity<Resource> downloadPrivateFile(@RequestBody FileServerPrivateDto dto){
        Path path = Paths.get(dto.getLocation()+File.separator+dto.getName()); // file path setting
        try{
            HttpHeaders httpHeaders = privateService.getHttpHeaders(path, dto.getName());
            Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/downloadPrivateMedia/{uuid}")
    public ResponseEntity<Resource> downloadPrivateMedia(@PathVariable String uuid){
        FileServerPublicEntity entity = service.findByUuidName(uuid);
        Path path = Paths.get(entity.getLocation()+File.separator+entity.getName());
        try{
            HttpHeaders httpHeaders = privateService.getHttpHeaders(path, entity.getName());
            Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/uploadPrivateFile/{token}") // upload PrivateFiles
    public ResponseEntity<List<String>> uploadPrivateFileInfo(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model, @PathVariable String token)
    {
        List<String> resultArr = privateService.uploadFiles(uploadFile, path, token, model);
        return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
    }
    @PutMapping("/updatePrivateFileInfo")
    public ResponseEntity<Integer> updatePrivateFileInfo(@RequestBody FileServerPrivateDto dto){
        int result = privateService.updateByFileServerPublicEntity(new FileServerPrivateEntity(dto));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @DeleteMapping("/deletePrivateFileInfo/{path}")
    public ResponseEntity<Long> deletePrivateFileInfo(@PathVariable String path){
        long result = privateService.deleteByPath(path); // delete file
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
