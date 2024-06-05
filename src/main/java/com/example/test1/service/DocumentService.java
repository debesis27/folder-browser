package com.example.test1.service;

import java.util.List;
import com.example.test1.entity.Document;
import com.example.test1.entity.FolderDTO;

public interface DocumentService {
  public List<Document> getAllDocuments();
  public FolderDTO buildFolderStructure(List<Document> documents);
}
