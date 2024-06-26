package com.example.test1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebController {

  @GetMapping("/")
  public String redirectToFolderStructureGrid() {
    return "redirect:/folders-grid";
  }

  @GetMapping("/folders-grid")
  public String getFolderStructureGrid() {
    return "folder-grid-view";
  }
}
