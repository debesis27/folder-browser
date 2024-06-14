package com.example.test1.service;

import com.example.test1.entity.FileSystemItemDTO;

public interface FileScanService {
  public FileSystemItemDTO scanConfiguredFolder();
  public FileSystemItemDTO scanFolder(String folderPath);
}
