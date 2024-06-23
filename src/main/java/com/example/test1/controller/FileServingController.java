package com.example.test1.controller;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class FileServingController {
  @GetMapping("/file")
  public ResponseEntity<Resource> serveFile(@RequestParam String path) {
    try {
      Path filePath = Paths.get(path).toAbsolutePath().normalize();
      Resource resource = new FileSystemResource(filePath);

      if(!resource.exists()){
        return ResponseEntity.notFound().build();
      }
      
      String contentType = Files.probeContentType(filePath);
      if(contentType == null){
        contentType = "application/octet-stream";
      }

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");

      return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(contentType))
        .headers(headers)
        .body(resource);
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
}
