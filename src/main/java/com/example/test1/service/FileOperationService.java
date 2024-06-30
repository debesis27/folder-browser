package com.example.test1.service;

import java.io.File;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.entity.FolderAndFileResponseDTO;

public interface FileOperationService {
  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder);
  public Boolean createFolder(String folderName, String parentFolderPath);
  public Boolean uploadFile(MultipartFile file, String parentFolderpath);
  public Boolean renameFileSystemItem(String fileUrl, String newName);
  public File ZipAndDownloadFileSystemItem(List<String> fileUrlList);
  public List<String> moveFileSystemItem(List<String> sourceUrlList, String destinationUrl);
  public List<String> copyFileSystemItem(List<String> sourceUrlList, String destinationUrl);
  public List<String> deleteFileSystemItem(List<String> fileUrlList);
}
