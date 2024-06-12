package com.example.test1.service;

import java.util.List;
import com.example.test1.entity.Document;
import com.example.test1.entity.FolderAndFileResponseDTO;
import com.example.test1.entity.FileSystemItemDTO;

public interface DocumentService {
  public List<Document> getAllDocuments();
  public FileSystemItemDTO buildFolderStructure(List<Document> documents);
  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder);
}
