package com.example.test1.entity;

import java.util.List;

public class FolderAndFileResponseDTO {
  private List<FileSystemItem> folders;
  private List<FileSystemItem> files;

  public FolderAndFileResponseDTO(List<FileSystemItem> folders, List<FileSystemItem> files) {
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
