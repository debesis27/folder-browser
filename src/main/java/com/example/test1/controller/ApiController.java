package com.example.test1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.test1.entity.FolderAndFileResponseDTO;
import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.service.DocumentServiceImpl;
import com.example.test1.service.FileScanServiceImpl;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


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

  @DeleteMapping("/folders/delete")
  public ResponseEntity<String> deleteFile(@RequestParam String fileUrl) {
    Boolean isDeleted = documentService.deleteFileSystemItem(fileUrl, rootFolder);

    if(isDeleted){
      setRootFolder(fileScanService.scanConfiguredFolder());
      return new ResponseEntity<>("File deleted successfully", HttpStatus.OK);
    }else{
      return new ResponseEntity<>("File deletion failed", HttpStatus.BAD_REQUEST);
    }
  }
}
