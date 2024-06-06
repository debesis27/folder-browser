package com.example.test1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.test1.entity.Document;
import com.example.test1.entity.FolderDTO;
import com.example.test1.repository.DocumentRepository;

@Service
public class DocumentServiceImpl implements DocumentService {
  @Autowired
  private DocumentRepository documentRepository;

  @Override
  public List<Document> getAllDocuments() {
    return documentRepository.findAll();
  }

  @Override
  public FolderDTO buildFolderStructure(List<Document> documents) {
    FolderDTO root = new FolderDTO("root");

    for(Document document : documents){
      if(document == null || document.getFile_url() == null || document.getFile_url().isEmpty()){
        continue;
      }

      boolean hasFile = false;
      String file = document.getFile_url();
      char[] fileChars = file.toCharArray();
      for(int i = fileChars.length-1; i >= 0; i--){
        if(fileChars[i] == '.'){
          hasFile = true;
          break;
        }
      }

      if(!hasFile){
        continue;
      }

      String[] folders = file.split("/");
      FolderDTO currentSubFolder = root;

      for(int i = 1; i < folders.length - 1; i++){
        if(folders[i].isEmpty()){
          continue;
        }
        currentSubFolder = currentSubFolder.addSubFolder(folders[i]);
      }

      currentSubFolder.addFile(folders[folders.length - 1], file);
    }

    return root;
  }
}
