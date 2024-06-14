package com.example.test1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.test1.service.FileScanServiceImpl;

@Controller
@RequestMapping("/")
public class WebController {

  @Autowired
  private FileScanServiceImpl fileScanService;

  @GetMapping("/folders")
  public String getFolderStructure(Model model) {
    model.addAttribute("rootFolder", fileScanService.scanConfiguredFolder());
    return "folder-list";
  }

  @GetMapping("/folders-grid")
  public String getFolderStructureGrid() {
    return "folder-grid-view";
  }
}
