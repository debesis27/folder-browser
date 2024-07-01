package com.example.test1.service;

import java.io.File;
import java.nio.file.Path;

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
  public String getRootFolderUrl() {
    return fileBrowserConfig.getDirectory();
  }

  @Override
  public Path getPathFromFolderUrl(String path){
    Path rootPath = Path.of(getRootFolderUrl()).toAbsolutePath().normalize();
    if(path.length() < 5) return rootPath;
    path = path.substring(5);                                     // Remove root\ from the path
    path = rootPath.resolve(path).toString();
    return Path.of(path).toAbsolutePath().normalize();
  }

  @Override
  public FileSystemItemDTO scanConfiguredFolder() {
    String rootPath = getRootFolderUrl() + "/";
    return scanFolder(rootPath);
  }

  @Override
  public FileSystemItemDTO scanFolder(String folderPath) {
    File rootFolder = new File(folderPath);

    if(!rootFolder.exists() || !rootFolder.isDirectory()){
      throw new IllegalArgumentException("Invalid folder path: " + folderPath);
    }

    FileSystemItemDTO rootFolderDto = scanFolderHelper(rootFolder);
    rootFolderDto.getParent().setUrl("root");
    return rootFolderDto;
  }

  private FileSystemItemDTO scanFolderHelper(File directory){
    Path rootFolderPath = Path.of(getRootFolderUrl()).toAbsolutePath().normalize();
    Path currentFolderPath = directory.toPath().toAbsolutePath().normalize();

    long directorySize = 0;
    String name = directory.getName();
    Path relativePath = rootFolderPath.relativize(currentFolderPath);
    if(currentFolderPath.equals(rootFolderPath)){
      name = "root";
      relativePath = Path.of("");
    }

    FileSystemItemDTO rootFolderDto = new FileSystemItemDTO(new FileSystemItem(
      name, "root\\" + relativePath.toString(), "folder", directory.length()
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
            String fileURL = "root\\" + rootFolderPath.relativize(file.toPath().toAbsolutePath().normalize()).toString();
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
