package com.example.test1.service;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.entity.FolderAndFileResponseDTO;

public interface FileOperationService {
  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder);
  public Boolean createFolder(String folderName, String parentFolderPath, FileSystemItemDTO rootFolder);
  public Boolean uploadFile(MultipartFile file, String parentFolderpath, FileSystemItemDTO rootFolder);
  public Boolean renameFileSystemItem(String fileUrl, String newName, FileSystemItemDTO rootFolder);
  public File ZipAndDownloadFileSystemItem(String fileUrl, FileSystemItemDTO rootFolder);
  public Boolean moveFileSystemItem(String sourceUrl, String destinationUrl, FileSystemItemDTO rootFolder);
  public Boolean copyFileSystemItem(String sourceUrl, String destinationUrl, FileSystemItemDTO rootFolder);
  public Boolean deleteFileSystemItem(String fileUrl, FileSystemItemDTO rootFolder);
}
