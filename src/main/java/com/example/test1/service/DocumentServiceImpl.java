package com.example.test1.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.test1.entity.Document;
import com.example.test1.entity.FileSystemItem;
import com.example.test1.entity.FolderAndFileResponseDTO;
import com.example.test1.entity.FileSystemItemDTO;
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
  public FileSystemItemDTO buildFolderStructure(List<Document> documents) {
    FileSystemItem rootFolder = new FileSystemItem("root", "root", "folder");
    FileSystemItemDTO root = new FileSystemItemDTO(rootFolder);

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
      FileSystemItemDTO currentSubFolder = root;
      String folderUrl = "root";

      for(int i = 1; i < folders.length - 1; i++){
        if(folders[i].isEmpty()){
          continue;
        }
        folderUrl += "/" + folders[i];
        currentSubFolder = currentSubFolder.addChildFolder(folders[i], folderUrl);
      }

      currentSubFolder.addFile(folders[folders.length - 1], "root" + file, getFileExtension(folders[folders.length - 1]));
    }

    return root;
  }

  private String getFileExtension (String fileName) {
    String extension = "";
    int i = fileName.lastIndexOf('.');
    if (i > 0) {
      extension = fileName.substring(i+1);
    }
    return extension;
  }

  @Override
  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder) {
    List<FileSystemItem> resultFolderList = new ArrayList<>();
    List<FileSystemItem> resultFileList = new ArrayList<>();

    if(folderNameString == null || folderNameString.isEmpty()){
      return null;
    }

    searchFolderPathByNameHelper(folderNameString, rootFolder, "", resultFolderList, resultFileList);
    return new FolderAndFileResponseDTO(resultFolderList, resultFileList);
  }

  private void searchFolderPathByNameHelper(String folderNameString, FileSystemItemDTO currentFolder, String path, List<FileSystemItem> resultFolderList, List<FileSystemItem> resultFileList) {
    path += "/" + currentFolder.getParent().getName();

    if(currentFolder.getParent().getName().contains(folderNameString)){
      resultFolderList.add(new FileSystemItem(currentFolder.getParent().getName(), path, "folder"));
    }

    List<FileSystemItemDTO> subFolders = currentFolder.getChildFolders();
    for(FileSystemItemDTO subFolder : subFolders){
      searchFolderPathByNameHelper(folderNameString, subFolder, path, resultFolderList, resultFileList);
    }

    for(FileSystemItem file : currentFolder.getChildFiles()){
      if(file.getName().contains(folderNameString)){
        resultFileList.add(new FileSystemItem(file.getName(), path + "/" + file.getName(), file.getType()));
      }
    }
  }
}
