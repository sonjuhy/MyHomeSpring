package com.myhome.server.api.controller;

import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.service.FileServerPublicService;
import com.myhome.server.api.service.FileServerPublicServiceImpl;
import com.myhome.server.db.entity.FileServerPublicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileServerController {
    /**
     * file server public
     *
     *
     * */

    @Autowired
    FileServerPublicService service = new FileServerPublicServiceImpl();

    @GetMapping("/getPublicFileInfo/{path}")
    public ResponseEntity<FileServerPublicEntity> getPublicFileInfo(@PathVariable String path){
        FileServerPublicEntity fileServerPublicService = service.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }
    @GetMapping("/getPublicFilesInfo/{location}")
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFilesInfo(@PathVariable String location){
        List<FileServerPublicEntity> list = service.findByLocation(location);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @PostMapping("/uploadPublicFile")
    public ResponseEntity<List<String>> uploadPublicFileInfo(@RequestParam MultipartFile[] uploadFile, Model model)
    {
        List<FileServerPublicEntity> list = new ArrayList<>();
        for(MultipartFile file : uploadFile){
            if(!file.isEmpty()){
                try{
                    FileServerPublicEntity entity = new FileServerPublicEntity(
                            "testPath",
                            file.getOriginalFilename(),
                            UUID.randomUUID().toString(),
                            file.getContentType(),
                            (float)file.getSize(),
                            "testLocation"
                    );
                    list.add(entity);
                    File newFile = new File(entity.getUuidName()+"_"+entity.getName());
                    file.transferTo(newFile);
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
        return new ResponseEntity<>(resultArr, HttpStatus.OK);
    }
    @PutMapping("/updatePublicFileInfo")
    public ResponseEntity<Integer> updatePublicFileInfo(@RequestBody FileServerPublicDto dto){
        int result = service.updateByFileServerPublicEntity(new FileServerPublicEntity(dto));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @DeleteMapping("/deletePublicFileInfo/{path}")
    public ResponseEntity<Long> deletePublicFileInfo(@PathVariable String path){
        System.out.println("delete path : " + path);
        long result = service.deleteByPath(path);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    /**
     * file server private
     * */
}
