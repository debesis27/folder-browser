package com.example.test1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.test1.entity.FolderAndFileResponseDTO;
import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.service.DocumentServiceImpl;
import com.example.test1.service.FileScanServiceImpl;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/api")
public class ApiController {
  private FileSystemItemDTO rootFolder = null;
  @Autowired
  private DocumentServiceImpl documentService;

  @Autowired
  private FileScanServiceImpl fileScanService;

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
  
}
