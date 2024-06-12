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
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/api")
public class ApiController {
  @Autowired
  private DocumentServiceImpl documentService;
  private FileSystemItemDTO rootFolder = null;

  @GetMapping("/folders")
  public ResponseEntity<FileSystemItemDTO> getFolderList() {
    if(rootFolder == null){
      rootFolder = documentService.buildFolderStructure(documentService.getAllDocuments());
    }
    return new ResponseEntity<>(rootFolder, HttpStatus.OK);
  }

  @GetMapping("/folders/search")
  public ResponseEntity<FolderAndFileResponseDTO> getAllFolderPathByName(@RequestParam String folderName) {
    if(rootFolder == null){
      rootFolder = documentService.buildFolderStructure(documentService.getAllDocuments());
    }
    
    FolderAndFileResponseDTO path = documentService.searchAllFolderPathByName(folderName, rootFolder);
    
    return new ResponseEntity<>(path, HttpStatus.OK);
  }
  
}
