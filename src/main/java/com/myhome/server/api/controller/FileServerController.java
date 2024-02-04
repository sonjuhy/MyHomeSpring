package com.myhome.server.api.controller;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.service.*;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
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
import java.util.Date;
import java.util.List;


@RestController()
@RequestMapping("/file")
public class FileServerController {

    private final static String TOPIC_CLOUD_LOG = "cloud-log-topic";

    @Autowired
    FileServerThumbNailService thumbNailService;

    @Autowired
    FileServerPublicService service;

    @Autowired
    FileServerPrivateService privateService;

    @Autowired
    FileServerCommonService commonService;

    @Autowired
    AuthService authService;

    @Autowired
    FileDefaultPathRepository defaultPathRepository;

    @Autowired
    LogComponent logComponent;

    @Autowired
    private Job publicCloudCheckJob;
    @Autowired
    private JobLauncher jobLauncher;

    /*
     * COMMON PART
     * check File(Public, Private both) : O
     * downloadThumbnail : O
     * checkThumbnail : O
     * getDefaultPath : O
     */
    @Operation(description = "Cloud 파일 전체 탐색 시작하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 완료"),
            @ApiResponse(responseCode = "500", description = "에러")
    })
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

    @Operation(description = "Cloud 파일 중 Public 에 해당하는 파일만 탐색 시작하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 완료"),
            @ApiResponse(responseCode = "500", description = "에러")
    })
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

    @GetMapping("/checkPublicFileStatusBatch")
    public ResponseEntity<String> checkPublicFileStatusBatch(){
        StringWriter sw = new StringWriter();
        try {
            Date date = new Date();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("publicCheck-"+date.getTime(), String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(publicCloudCheckJob, jobParameters);
        }
        catch (Exception e){
            e.printStackTrace(new PrintWriter(sw));
            return new ResponseEntity<>("publicFileCheck error : "+sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Public file check Success", HttpStatus.OK);
    }

    @Operation(description = "Cloud 파일 중 Private 에 해당하는 파일만 탐색 시작하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 완료"),
            @ApiResponse(responseCode = "500", description = "에러")
    })
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

    @Operation(description = "썸네일 다운로드 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 다운로드 혹은 파일이 없음"),
            @ApiResponse(responseCode = "500", description = "백엔드 에러")
    })
    @GetMapping("/downloadThumbNail/{uuid}/{accessToken}")
    public ResponseEntity<Resource> downloadThumbNail(@PathVariable String uuid, @PathVariable String accessToken){
        if(authService.validateAccessToken(accessToken)) {
            return commonService.downloadThumbNail(uuid);
        }
        else return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @Operation(description = "썸네일 파일 존재하는지 확인하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리. true : 존재, false : 없음")
    })
    @GetMapping("/checkThumbNailFile/{uuid}")
    public ResponseEntity<Boolean> checkThumbNailFileExist(@PathVariable String uuid){
        boolean result = thumbNailService.checkThumbNailExist(uuid);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Cloud 서비스에 기본 루트 받아오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "accessToken 정상 발급")
    })
    @GetMapping("/getDefaultPath")
    public ResponseEntity<List<FileDefaultPathEntity>> getDefaultPath(){
        List<FileDefaultPathEntity> list = defaultPathRepository.findAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    /*
     * mode specific
     * MB : total(mb), free(mb)
     * GB : total(gb), free(gb)
     * percent : free, usage percent
     * */
    @Operation(description = "mode 에 들어갈 값 : 결과값) MB : total(mb), free(mb) | GB : total(gb), free(gb) | percent : free, usage percent info")
    @GetMapping("/getStorageUsage/{mode}")
    public ResponseEntity<int[]> getStorageUsage(@PathVariable String mode){
        int[] usage = commonService.getStorageUsage(mode);
        return new ResponseEntity<>(usage, HttpStatus.OK);
    }

    /*
     * file server public
     * upload : O
     * download : O
     * move : O
     * update : O
     * delete : O
     * getList Page : O
     * getTrashList Page : O
     * */

    @Operation(description = "Public 파일 하나 정보 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 정보 정상 처리")
    })
    @GetMapping("/getPublicFileInfo") // get PublicFile info
    public ResponseEntity<FileServerPublicEntity> getPublicFileInfo(@RequestParam String path){
        FileServerPublicEntity fileServerPublicService = service.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }

    @Operation(description = "Public 파일 중 원하는 폴더 내부에 있는 파일 전부 정보 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 리스트 반환")
    })
    @GetMapping("/getPublicFileListInfo") // get PublicFiles info list
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFileListInfo(@RequestParam String location){
        List<FileServerPublicEntity> list = service.findByLocation(location, 0);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Public 파일 중 원하는 폴더 내부에 있는 파일을 원하는 갯수와 페이지로 나눠서 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 리스트 반환")
    })
    @GetMapping("/getPublicFileListInfoPage") // get PublicFiles info list
    public ResponseEntity<List<FileServerPublicEntity>> getPublicFileListInfoPage(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPublicEntity> list = service.findByLocationPage(location, 0, size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Public 파일을 원하는 폴더로 이동하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리턴 값이 0이면 정상 처리")
    })
    @GetMapping("/movePublicFileInfo")
    public ResponseEntity<Integer> movePublicFileInfo(@RequestParam(value="path") String path, @RequestParam(value="location") String location){
        int result = service.moveFile(path, location);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Public 파일 다운로드 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @PostMapping("/downloadPublicFile")
    public ResponseEntity<Resource> downloadPublicFile(@RequestBody FileServerPublicDto dto){
        return service.downloadFile(dto.getUuidName());
    }

    @Operation(description = "Public 파일 중 미디어(영상 등) 파일 다운로드 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리"),
            @ApiResponse(responseCode = "401", description = "권한 에러")
    })
    @CrossOrigin(origins = "*")
    @GetMapping("/downloadPublicMedia/{uuid}/{accessToken}")
    public ResponseEntity<Resource> downloadPublicMedia(@PathVariable String uuid, @PathVariable String accessToken){
        if(authService.validateAccessToken(accessToken)) return service.downloadPublicMedia(uuid);
        else return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @Operation(description = "Public 파일 중 미디어(영상) 파일 스트리밍 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리"),
            @ApiResponse(responseCode = "401", description = "권한 에러")
    })
    @CrossOrigin(origins = "*")
    @GetMapping("/streamingPublicVideo/{uuid}/{accessToken}")
    public ResponseEntity<ResourceRegion> streamingPublicVideo(@RequestHeader HttpHeaders httpHeaders, @PathVariable String uuid, @PathVariable String accessToken){
        if(authService.validateAccessToken(accessToken)) return service.streamingPublicVideo(httpHeaders, uuid);
        else return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @Operation(description = "Public 휴지통 파일 목록 중 원하는 폴더 기준으로 다 받아 오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @GetMapping("/getPublicTrashFileListInfo")
    public ResponseEntity<List<FileServerPublicTrashEntity>> getPublicTrashFileListInfo(@RequestParam String location){
        List<FileServerPublicTrashEntity> list = service.findByLocationTrash(location);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Public 휴지통 파일 목록 중 원하는 폴더에 원하는 갯수, 페이지로 받아오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @GetMapping("/getPublicTrashFileListInfoPage")
    public ResponseEntity<List<FileServerPublicTrashEntity>> getPublicTrashFileListInfoPage(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPublicTrashEntity> list = service.findByLocationPageTrash(location,  size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Public 에 파일 업로드 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @PostMapping("/uploadPublicFile") // upload files
    public ResponseEntity<List<String>> uploadPublicFile(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model)
    {
        System.out.println("uploadPublicFile : " + path);
        List<String> resultArr = service.uploadFiles(uploadFile, path, model);
        return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
    }

    @Operation(description = "Public 에 원하는 위치에 폴더 생성하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/mkdirPublic")
    public ResponseEntity<Void> mkdirPublic(@RequestParam String path){
        if(service.mkdir(path)){
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(description = "Public 파일 정보 업데이트 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @PutMapping("/updatePublicFileInfo")
    public ResponseEntity<Integer> updatePublicFileInfo(@RequestBody FileServerPublicDto dto){
        int result = service.updateByFileServerPublicEntity(new FileServerPublicEntity(dto));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Public 파일 중 휴지통에 있던 파일을 복원 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @PutMapping("/restorePublicFile")
    public ResponseEntity<Integer> restorePublic(@RequestParam String uuid){
        int result = service.restore(uuid);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Public 파일 중 휴지통으로 임시 삭제하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @DeleteMapping("/deletePublicFileToTrash")
    public ResponseEntity<Long> deletePublicFileTrash(@RequestParam String uuid){
        long result = service.moveTrash(uuid);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Public 휴지통에 있는 파일 완전 삭제 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @DeleteMapping("/deletePublicFileInfo/{path}")
    public ResponseEntity<Long> deletePublicFileInfo(@PathVariable String path){
        long result = service.deleteByPath(path); // delete file
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /*
     * file server private
     * upload : O
     * download : O
     * move : O
     * update : O
     * delete : O
     * getList Page : O
     * getTrashList Page : O
     * */

    @Operation(description = "Private 중 파일 하나 정보 불러오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @GetMapping("/getPrivateFileInfo") // get PrivateFile info
    public ResponseEntity<FileServerPrivateEntity> getPrivateFileInfo(@RequestParam String path){
        FileServerPrivateEntity fileServerPublicService = privateService.findByPath(path);
        return new ResponseEntity<>(fileServerPublicService, HttpStatus.OK);
    }

    @Operation(description = "Private 중 원하는 폴더 내에 있는 모든 파일 정보 불러오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @GetMapping("/getPrivateFileListInfo") // get PrivateFile info list
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateFileListInfo(@RequestParam String location){
        List<FileServerPrivateEntity> list = privateService.findByLocation(location, 0);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Private 중 원하는 폴더 내에 있는 파일을 원하는 갯수, 페이지로 정보 불러오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @GetMapping("/getPrivateFileListInfoPage") // get PrivateFile info list
    public ResponseEntity<List<FileServerPrivateEntity>> getPrivateFileListInfoPage(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPrivateEntity> list = privateService.findByLocationPage(location, 0, size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Private 파일 이동시키는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @GetMapping("/movePrivateFileInfo")
    public ResponseEntity<Integer> movePrivateFileInfo(@RequestParam String uuid, @RequestParam String location, @RequestParam String accessToken){
        int result = privateService.moveFile(uuid, location, accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Private 파일 다운로드 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @PostMapping("/downloadPrivateFile")
    public ResponseEntity<Resource> downloadPrivateFile(@RequestBody FileServerPrivateDto dto){
        return privateService.downloadFile(dto.getUuidName());
    }

    @Operation(description = "Private 파일 중 미디어 파일 받는(영상 스트리밍) API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @CrossOrigin(origins = "*")
    @GetMapping("/downloadPrivateMedia/{uuid}/{accessToken}")
    public ResponseEntity<Resource> downloadPrivateMedia(@PathVariable String uuid, @PathVariable String accessToken){
        if(authService.validateAccessToken(accessToken)) return privateService.downloadPrivateMedia(uuid);
        else return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @Operation(description = "Public 파일 중 미디어(영상) 파일 스트리밍 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리"),
            @ApiResponse(responseCode = "401", description = "권한 에러")
    })
    @CrossOrigin(origins = "*")
    @GetMapping("/streamingPrivateVideo/{uuid}/{accessToken}")
    public ResponseEntity<ResourceRegion> streamingPrivateVideo(@RequestHeader HttpHeaders httpHeaders, @PathVariable String uuid, @PathVariable String accessToken){
        if(authService.validateAccessToken(accessToken)) return privateService.streamingPrivateVideo(httpHeaders, uuid);
        else return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @Operation(description = "Private 의 휴지통(개인)에서 원하는 폴더 내 파일 리스트 불러오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @GetMapping("/getPrivateTrashFileListInfo")
    public ResponseEntity<List<FileServerPrivateTrashEntity>> getPrivateTrashFileListInfo(@RequestParam String location){
        List<FileServerPrivateTrashEntity> list = privateService.findByLocationTrash(location);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Private 의 휴지통(개인)에서 원하는 폴더 내 원하는 갯수, 페이지로 파일 리스트 불러오는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @GetMapping("/getPrivateTrashFileListInfoPage")
    public ResponseEntity<List<FileServerPrivateTrashEntity>> getPrivateTrashFileListInfoPage(@RequestParam String location, @RequestParam int size, @RequestParam int page){
        List<FileServerPrivateTrashEntity> list = privateService.findByLocationPageTrash(location,  size, page);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(description = "Private 에 파일 업로드 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리. 업로드 된 파일 이름들 리턴")
    })
    @PostMapping("/uploadPrivateFileInfo/{token}") // upload PrivateFiles
    public ResponseEntity<List<String>> uploadPrivateFileInfo(@RequestParam MultipartFile[] uploadFile, @RequestParam String path, Model model, @PathVariable String token)
    {
        List<String> resultArr = privateService.uploadFiles(uploadFile, path, token, model);
        return new ResponseEntity<>(resultArr, HttpStatus.OK); // return filename that success to insert file name in DB
    }

    @Operation(description = "Private 에 원하는 폴더 생성하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리"),
            @ApiResponse(responseCode = "500", description = "백엔드 에러")
    })
    @PostMapping("/mkdirPrivate")
    public ResponseEntity<Void> mkdirPrivate(@RequestParam String path, @RequestParam String token){
        if(privateService.mkdir(path, token)) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(description = "Private 파일 정보 업데이트 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @PutMapping("/updatePrivateFile")
    public ResponseEntity<Integer> updatePrivateFileInfo(@RequestBody FileServerPrivateDto dto){
        int result = privateService.updateByFileServerPrivateEntity(new FileServerPrivateEntity(dto));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Private 휴지통에 있는 파일 복구하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 처리")
    })
    @PutMapping("/restorePrivateFile")
    public ResponseEntity<Integer> restorePrivateFile(@RequestParam(value="uuid") String uuid, @RequestParam(value="accessToken") String accessToken){
        int result = privateService.restore(uuid, accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Private 개인 휴지통으로 임시 삭제하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @DeleteMapping("/deletePrivateFileToTrash")
    public ResponseEntity<Long> deletePrivateFileTrash(@RequestParam(value="uuid") String uuid, @RequestParam(value="accessToken") String accessToken){
        long result = privateService.moveTrash(uuid, accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "Private 개인 휴지통에 있는 파일 중 완전 삭제하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 값이 0이면 정상 처리")
    })
    @DeleteMapping("/deletePrivateFileInfo")
    public ResponseEntity<Long> deletePrivateFileInfo(@RequestParam(value="path") String path, @RequestParam(value="accessToken") String accessToken){
        long result = privateService.deleteByPath(path, accessToken); // delete file
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
