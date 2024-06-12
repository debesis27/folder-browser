package com.example.test1.entity;

import java.util.ArrayList;
import java.util.List;

public class FileSystemItemDTO {
  private FileSystemItem parent = new FileSystemItem();
  private List<FileSystemItemDTO> childFolders = new ArrayList<>();
  private List<FileSystemItem> childFiles = new ArrayList<>();

  public FileSystemItemDTO(FileSystemItem parent){
    this.parent = parent;
  }

  public FileSystemItem getParent() {
    return parent;
  }

  public void setParent(FileSystemItem parent) {
    this.parent = parent;
  }

  public List<FileSystemItemDTO> getChildFolders(){
    return childFolders;
  }

  public FileSystemItemDTO addChildFolder(String subfolderName, String subfolderUrl){
    for(FileSystemItemDTO subfolder : this.childFolders){
      if(subfolder.parent.getName().equals(subfolderName)){
        return subfolder;
      }
    }

    FileSystemItem newSubFolder = new FileSystemItem(subfolderName, subfolderUrl, "folder");
    FileSystemItemDTO newChildFolder = new FileSystemItemDTO(newSubFolder);
    this.childFolders.add(newChildFolder);
    return newChildFolder;
  }

  public List<FileSystemItem> getChildFiles(){
    return childFiles;
  }

  public void addFile(String fileName, String fileUrl, String fileType){
    for(FileSystemItem file : this.childFiles){
      if(file.getName().equals(fileName)){
        System.out.println("File named: " + fileName + " already exists in folder named: " + this.parent.getName() + ". Skipping...");
        return;
      }
    }

    FileSystemItem newFile = new FileSystemItem(fileName, fileUrl, fileType);
    this.childFiles.add(newFile);
  }
}
