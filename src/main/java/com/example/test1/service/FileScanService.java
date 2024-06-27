package com.example.test1.service;

import java.nio.file.Path;

import com.example.test1.entity.FileSystemItemDTO;

public interface FileScanService {
  public String getRootFolderUrl();
  public Path getPathFromFolderUrl(String folderUrl);
  public FileSystemItemDTO scanConfiguredFolder();
  public FileSystemItemDTO scanFolder(String folderPath);
}
