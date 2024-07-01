package com.example.test1.entity;

import java.util.List;

public class SearchResponseDTO {
  private List<FileSystemItem> folders;
  private List<FileSystemItem> files;

  public SearchResponseDTO(List<FileSystemItem> folders, List<FileSystemItem> files) {
    this.folders = folders;
    this.files = files;
  }

  public List<FileSystemItem> getFolders() {
    return folders;
  }

  public List<FileSystemItem> getFiles() {
    return files;
  }
}
