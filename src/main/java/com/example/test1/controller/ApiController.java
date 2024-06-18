package com.example.test1.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.test1.entity.FolderAndFileResponseDTO;
import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.service.DocumentServiceImpl;
import com.example.test1.service.FileScanServiceImpl;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@Controller
@RequestMapping("/api")
public class ApiController {
  private FileSystemItemDTO rootFolder = null;
  @Autowired
  private DocumentServiceImpl documentService;
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
  public ResponseEntity<FolderAndFileResponseDTO> getAllFolderPathByName(@RequestParam String folderName) {
    if(rootFolder == null){
      rootFolder = fileScanService.scanConfiguredFolder();
    }
    
    FolderAndFileResponseDTO path = documentService.searchAllFolderPathByName(folderName, rootFolder);
    
    return new ResponseEntity<>(path, HttpStatus.OK);
  }
  
  @PostMapping("/folders/create")
  public ResponseEntity<String> createFolder(@RequestParam String folderName, @RequestParam String parentFolderPath) {
    if(rootFolder == null){
      rootFolder = fileScanService.scanConfiguredFolder();
    }
    
    Boolean isCreated = documentService.createFolder(folderName, parentFolderPath, rootFolder);
    
    if(isCreated){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>("Folder created successfully", HttpStatus.OK);
    }else{
      return new ResponseEntity<>("Folder creation failed", HttpStatus.BAD_REQUEST);}
  }

  @PostMapping("/files/upload")
  public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file, @RequestParam String parentFolderPath) {
    Boolean isUploaded = documentService.uploadFile(file, parentFolderPath, rootFolder);

    if(isUploaded){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
    }else{
      return new ResponseEntity<>("File upload failed", HttpStatus.BAD_REQUEST);
    }
  }

  @PutMapping("/folders/rename")
  public ResponseEntity<Boolean> renameFileSystemItem(@RequestParam String newName, @RequestParam String fileUrl){
    Boolean isRenamed = documentService.renameFileSystemItem(fileUrl, newName, rootFolder);

    if(isRenamed){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>(true, HttpStatus.OK);
    }else{
      return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/folders/download")
  public ResponseEntity<StreamingResponseBody> downloadFileSystemItem(@RequestParam String fileUrl) {
    File zipFile = documentService.downloadFileSystemItem(fileUrl, rootFolder);

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

  @DeleteMapping("/folders/delete")
  public ResponseEntity<String> deleteFileSystemItem(@RequestParam String fileUrl) {
    Boolean isDeleted = documentService.deleteFileSystemItem(fileUrl, rootFolder);

    if(isDeleted){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>("File deleted successfully", HttpStatus.OK);
    }else{
      return new ResponseEntity<>("File deletion failed", HttpStatus.BAD_REQUEST);
    }
  }
}
