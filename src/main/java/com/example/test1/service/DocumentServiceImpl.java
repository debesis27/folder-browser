package com.example.test1.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.test1.entity.FileSystemItem;
import com.example.test1.entity.FolderAndFileResponseDTO;
import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.repository.DocumentRepository;

@Service
public class DocumentServiceImpl implements DocumentService {
  @Autowired
  private DocumentRepository documentRepository;

  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder) {
    List<FileSystemItem> resultFolderList = new ArrayList<>();
    List<FileSystemItem> resultFileList = new ArrayList<>();

    if(folderNameString == null || folderNameString.isEmpty()){
      return null;
    }

    folderNameString = folderNameString.toLowerCase();
    searchFolderPathByNameHelper(folderNameString, rootFolder, "", resultFolderList, resultFileList);
    return new FolderAndFileResponseDTO(resultFolderList, resultFileList);
  }

  private void searchFolderPathByNameHelper(String folderNameString, FileSystemItemDTO currentFolder, String path, List<FileSystemItem> resultFolderList, List<FileSystemItem> resultFileList) {
    if(path.isEmpty()){
      path = currentFolder.getParent().getName();
    }else{
      path += "\\" + currentFolder.getParent().getName();
    }

    if(currentFolder.getParent().getName().toLowerCase().contains(folderNameString)){
      resultFolderList.add(new FileSystemItem(currentFolder.getParent().getName(), path, "folder"));
    }

    List<FileSystemItemDTO> subFolders = currentFolder.getChildFolders();
    for(FileSystemItemDTO subFolder : subFolders){
      searchFolderPathByNameHelper(folderNameString, subFolder, path, resultFolderList, resultFileList);
    }

    for(FileSystemItem file : currentFolder.getChildFiles()){
      if(file.getName().toLowerCase().contains(folderNameString)){
        resultFileList.add(new FileSystemItem(file.getName(), path + "\\" + file.getName(), file.getType()));
      }
    }
  }
}
