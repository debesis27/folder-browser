package com.example.test1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.test1.service.DocumentServiceImpl;

@Controller
@RequestMapping("/")
public class DocumentController {
  @Autowired
  private DocumentServiceImpl documentService;

  @GetMapping("/files")
  public String getAllFiles(Model model) {
    model.addAttribute("files", documentService.getAllDocuments());
    return "document-list";
  }

  @GetMapping("/folders")
  public String getFolderStructure(Model model) {
    model.addAttribute("rootFolder", documentService.buildFolderStructure(documentService.getAllDocuments()));
    return "folder-list";
  }

  @GetMapping("/folders-grid")
  public String getFolderStructureGrid(){
    return "folder-grid-view";
  }
}
