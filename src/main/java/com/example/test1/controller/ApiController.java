package com.example.test1.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.test1.entity.SearchResponseDTO;
import com.example.test1.entity.CopyOrMoveFileSystemItemDTO;
import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.service.FileOperationServiceImpl;
import com.example.test1.service.FileScanServiceImpl;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@Controller
@RequestMapping("/api")
public class ApiController {
  private FileSystemItemDTO rootFolder = null;
  @Autowired
  private FileOperationServiceImpl fileOperationService;
  @Autowired
  private FileScanServiceImpl fileScanService;

  private void setRootFolder(FileSystemItemDTO rootFolder){
    this.rootFolder = rootFolder;
  }

  @GetMapping("/folders")
  public ResponseEntity<FileSystemItemDTO> getFolderList() {
    if(rootFolder == null){
      rootFolder = fileScanService.scanConfiguredFolder();
    }
    return new ResponseEntity<>(rootFolder, HttpStatus.OK);
  }

  @GetMapping("/folders/search")
  public ResponseEntity<SearchResponseDTO> getAllFolderPathByName(@RequestParam String folderName) {
    if(rootFolder == null){
      rootFolder = fileScanService.scanConfiguredFolder();
    }

    SearchResponseDTO path = fileOperationService.searchAllFolderPathByName(folderName, rootFolder);
    
    return new ResponseEntity<>(path, HttpStatus.OK);
  }
  
  @PostMapping("/folders/create")
  public ResponseEntity<Boolean> createFolder(@RequestParam String folderName, @RequestParam String parentFolderPath) {
    if(rootFolder == null){
      rootFolder = fileScanService.scanConfiguredFolder();
    }
    
    Boolean isCreated = fileOperationService.createFolder(folderName, parentFolderPath);
    
    if(isCreated){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>(true, HttpStatus.OK);
    }else{
      return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);}
  }

  @PostMapping("/files/upload")
  public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files, @RequestParam String parentFolderPath) {
    List<String> failedFileList = fileOperationService.uploadFiles(files, parentFolderPath);

    if(failedFileList.isEmpty()){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>(failedFileList, HttpStatus.OK);
    }else{
      return new ResponseEntity<>(failedFileList, HttpStatus.BAD_REQUEST);
    }
  }

  @PutMapping("/folders/rename")
  public ResponseEntity<Boolean> renameFileSystemItem(@RequestParam String newName, @RequestParam String fileUrl){
    Boolean isRenamed = fileOperationService.renameFileSystemItem(fileUrl, newName);

    if(isRenamed){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>(true, HttpStatus.OK);
    }else{
      return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/folders/download")
  public ResponseEntity<StreamingResponseBody> downloadFileSystemItem(@RequestBody List<String> fileUrlList) {
    File zipFile = fileOperationService.ZipAndDownloadFileSystemItem(fileUrlList);

    if(zipFile == null){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zipFile.getName());
    headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

    StreamingResponseBody stream = outputStream -> {
      try(FileInputStream inputStream = new FileInputStream(zipFile)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      } finally {
        zipFile.delete();
      }
    };

    return ResponseEntity.ok()
      .headers(headers)
      .contentLength(zipFile.length())
      .body(stream);
  }

  @PutMapping("/folders/move")
  public ResponseEntity<List<String>> moveFileSystemItem(@RequestBody CopyOrMoveFileSystemItemDTO moveFileSystemItemDTO) {
    List<String> failedFileList = fileOperationService.moveFileSystemItem(moveFileSystemItemDTO.getSourceUrlList(), moveFileSystemItemDTO.getDestinationUrl());

    if(failedFileList.isEmpty()){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>(failedFileList, HttpStatus.OK);
    }else{
      return new ResponseEntity<>(failedFileList, HttpStatus.BAD_REQUEST);
    }
  }

  @PutMapping("/folders/copy")
  public ResponseEntity<List<String>> copyFileSystemItem(@RequestBody CopyOrMoveFileSystemItemDTO copyFileSystemItemDTO) {
    List<String> failedFileList = fileOperationService.copyFileSystemItem(copyFileSystemItemDTO.getSourceUrlList(), copyFileSystemItemDTO.getDestinationUrl());

    if(failedFileList.isEmpty()){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>(failedFileList, HttpStatus.OK);
    }else{
      return new ResponseEntity<>(failedFileList, HttpStatus.BAD_REQUEST);
    }
  }

  @DeleteMapping("/folders/delete")
  public ResponseEntity<List<String>> deleteFileSystemItem(@RequestBody List<String> fileUrlList) {
    List<String> failedFileList = fileOperationService.deleteFileSystemItem(fileUrlList);

    if(failedFileList.isEmpty()){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>(failedFileList, HttpStatus.OK);
    }else{
      return new ResponseEntity<>(failedFileList, HttpStatus.BAD_REQUEST);
    }
  }
}
