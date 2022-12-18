package com.myhome.server.api.controller;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.service.FileServerPrivateService;
import com.myhome.server.api.service.FileServerPublicService;
import com.myhome.server.api.service.FileServerPublicServiceImpl;
import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.FileServerPublicEntity;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileServerController {
    /**
     * file server public
     * upload : O (Need to add feature : choice upload folder)
     * download : O
     * move : O
     * update : O
     * delete : O
     * */

    @Autowired
    FileServerPublicService service = new FileServerPublicServiceImpl();

    @GetMapping("/getPublicFileInfo/{path}") // get PublicFile info
    public ResponseEntity<FileServerPublicEntity> getPublicFileInfo(@PathVariable String path){
        FileServerPublicEntity fileServerPublicService = service.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }
    @GetMapping("/getPublicFilesInfo/{location}") // get PublicFiles info list
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFilesInfo(@PathVariable String location){
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
        Path path = Paths.get(dto.getLocation()+"/"+dto.getUuidName()+"_"+dto.getName()); // file path setting
        try{
            String contentType = Files.probeContentType(path); // content type setting
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(ContentDisposition
                    .builder("attachment") //builder type
                    .filename(dto.getName(), StandardCharsets.UTF_8) // filename setting by utf-8
                    .build());
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
            Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/uploadPublicFile") // upload files
    public ResponseEntity<List<String>> uploadPublicFileInfo(@RequestParam MultipartFile[] uploadFile, Model model)
    {
        List<FileServerPublicEntity> list = new ArrayList<>();
        for(MultipartFile file : uploadFile){
            if(!file.isEmpty()){
                try{
                    FileServerPublicEntity entity = new FileServerPublicEntity(
                            "testPath", // file path (need to change)
                            file.getOriginalFilename(), // file name
                            UUID.randomUUID().toString(), // file name to change UUID
                            file.getContentType(), // file type (need to check ex: txt file -> text/plan)
                            (float)file.getSize(), // file size(KB)
                            "testLocation" // file folder path (need to change)
                    );
                    list.add(entity);
                    File newFile = new File(entity.getUuidName()+"_"+entity.getName());
                    file.transferTo(newFile); // file name change
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        model.addAttribute("files", list);
        List<String> resultArr = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            if(service.save(list.get(i))){
                resultArr.add(list.get(i).getName());
            }
        }
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

    @Autowired
    FileServerPrivateService privateService;
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
        Path path = Paths.get(dto.getLocation()+"/"+dto.getUuidName()+"_"+dto.getName()); // file path setting
        try{
            String contentType = Files.probeContentType(path); // content type setting
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(ContentDisposition
                    .builder("attachment") //builder type
                    .filename(dto.getName(), StandardCharsets.UTF_8) // filename setting by utf-8
                    .build());
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
            Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/uploadPrivateFile/{owner}") // upload PrivateFiles
    public ResponseEntity<List<String>> uploadPrivateFileInfo(@RequestParam MultipartFile[] uploadFile, Model model, @PathVariable String owner)
    {
        List<FileServerPrivateEntity> list = new ArrayList<>();
        for(MultipartFile file : uploadFile){
            if(!file.isEmpty()){
                try{
                    FileServerPrivateEntity entity = new FileServerPrivateEntity(
                            "testPath", // file path (need to change)
                            file.getOriginalFilename(), // file name
                            UUID.randomUUID().toString(), // file name to change UUID
                            file.getContentType(), // file type (need to check ex: txt file -> text/plan)
                            (float)file.getSize(), // file size(KB),
                            owner,
                            "testLocation" // file folder path (need to change)
                    );
                    list.add(entity);
                    File newFile = new File(entity.getUuidName()+"_"+entity.getName());
                    file.transferTo(newFile); // file name change
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        model.addAttribute("files", list);
        List<String> resultArr = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            if(privateService.save(list.get(i))){
                resultArr.add(list.get(i).getName());
            }
        }
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
