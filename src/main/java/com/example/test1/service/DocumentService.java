package com.example.test1.service;

import java.io.File;

import com.example.test1.entity.FolderAndFileResponseDTO;

import org.springframework.web.multipart.MultipartFile;

import com.example.test1.entity.FileSystemItemDTO;

public interface DocumentService {
  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder);
  public Boolean createFolder(String folderName, String parentFolderPath, FileSystemItemDTO rootFolder);
  public Boolean uploadFile(MultipartFile file, String parentFolderPath, FileSystemItemDTO rootFolder);
  public Boolean renameFileSystemItem(String fileUrl, String newName, FileSystemItemDTO rootFolder);
  public File downloadFileSystemItem(String fileUrl, FileSystemItemDTO rootFolder);
  public Boolean deleteFileSystemItem(String fileUrl, FileSystemItemDTO rootFolder);
}
