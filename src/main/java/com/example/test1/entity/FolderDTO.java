package com.example.test1.entity;

import java.util.ArrayList;
import java.util.List;

public class FolderDTO {
  private String name;
  private List<FolderDTO> subFolders = new ArrayList<>();
  private List<String> files = new ArrayList<>();

  public FolderDTO(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<FolderDTO> getSubFolders(){
    return subFolders;
  }

  public FolderDTO addSubFolder(String subfolderName){
    for(FolderDTO subfolder : this.subFolders){
      if(subfolder.getName().equals(subfolderName)){
        return subfolder;
      }
    }

    FolderDTO newSubFolder = new FolderDTO(subfolderName);
    this.subFolders.add(newSubFolder);
    return newSubFolder;
  }

  public List<String> getFiles(){
    return files;
  }

  public void addFile(String fileName){
    for(String file : this.files){
      if(file.equals(fileName)){
        System.out.println("File named: " + fileName + " already exists in folder named: " + this.name + ". Skipping...");
        return;
      }
    }

    this.files.add(fileName);
  }
}
