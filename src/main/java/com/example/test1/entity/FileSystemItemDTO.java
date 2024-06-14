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

  public void addChildFolder(FileSystemItemDTO newChildFolder){
    for(FileSystemItemDTO childfolder : this.childFolders){
      if(childfolder.parent.getName().equals(newChildFolder.getParent().getName())){
        return;
      }
    }

    this.childFolders.add(newChildFolder);
    return;
  }

  public List<FileSystemItem> getChildFiles(){
    return childFiles;
  }

  public void addChildFile(FileSystemItem newChildFile){
    for(FileSystemItem file : this.childFiles){
      if(file.getName().equals(newChildFile.getName())){
        System.out.println("File named: " + newChildFile.getName() + " already exists in folder named: " + this.parent.getName() + ". Skipping...");
        return;
      }
    }

    this.childFiles.add(newChildFile);
  }
}
