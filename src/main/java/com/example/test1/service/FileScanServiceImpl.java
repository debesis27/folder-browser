package com.example.test1.service;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.test1.config.FileBrowserConfig;
import com.example.test1.entity.FileSystemItem;
import com.example.test1.entity.FileSystemItemDTO;

@Service
public class FileScanServiceImpl implements FileScanService{
  @Autowired
  private FileBrowserConfig fileBrowserConfig;

  @Override
  public FileSystemItemDTO scanConfiguredFolder() {
    String rootPath = fileBrowserConfig.getDirectory() + "/";
    return scanFolder(rootPath);
  }

  @Override
  public FileSystemItemDTO scanFolder(String folderPath) {
    System.out.println("Scanning folder: " + folderPath);
    File rootFolder = new File(folderPath);
    if(!rootFolder.exists() || !rootFolder.isDirectory()){
      throw new IllegalArgumentException("Invalid folder path: " + folderPath);
    }

    FileSystemItemDTO rootFolderDto = scanFolderHelper(rootFolder);
    return rootFolderDto;
  }

  private FileSystemItemDTO scanFolderHelper(File directory){
    long directorySize = 0;
    String name = directory.getName();
    String path = directory.toPath().toString();
    if(name.isEmpty()) {
      name = directory.getPath();
      name = name.substring(0, name.length() - 1);
      path = path.substring(0, path.length() - 1);
    }

    FileSystemItemDTO rootFolderDto = new FileSystemItemDTO(new FileSystemItem(
      name, path, "folder", directory.length()
    ));

    File[] files = directory.listFiles();
    if(files != null){
      for(File file : files){
        if(file.isHidden() || file.getName().startsWith(".")) continue;
          if(file.isDirectory()){
            FileSystemItemDTO childFolderDto = scanFolderHelper(file);
            rootFolderDto.addChildFolder(childFolderDto);
            directorySize += childFolderDto.getParent().getSize();
          }else{
            String fileExtension = StringUtils.getFilenameExtension(file.getName());
            String fileURL = file.getAbsolutePath();
            FileSystemItem childFile = new FileSystemItem(
              file.getName(), fileURL, fileExtension, file.length()
            );
            rootFolderDto.addChildFile(childFile);
            directorySize += file.length();
          }
      }
    }
    
    rootFolderDto.getParent().setSize(directorySize + rootFolderDto.getParent().getSize());
    return rootFolderDto;
  }
}
