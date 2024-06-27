package com.example.test1.service;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.entity.FolderAndFileResponseDTO;

public interface FileOperationService {
  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder);
  public Boolean createFolder(String folderName, String parentFolderPath);
  public Boolean uploadFile(MultipartFile file, String parentFolderpath);
  public Boolean renameFileSystemItem(String fileUrl, String newName);
  public File ZipAndDownloadFileSystemItem(String fileUrl);
  public Boolean moveFileSystemItem(String sourceUrl, String destinationUrl);
  public Boolean copyFileSystemItem(String sourceUrl, String destinationUrl);
  public Boolean deleteFileSystemItem(String fileUrl);
}
