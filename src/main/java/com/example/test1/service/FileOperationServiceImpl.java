package com.example.test1.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.test1.entity.FileSystemItem;
import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.entity.FolderAndFileResponseDTO;

@Service
public class FileOperationServiceImpl implements FileOperationService{
  @Autowired
  private FileScanService fileScanService;
  
  @Override
  public FolderAndFileResponseDTO searchAllFolderPathByName(String folderNameString, FileSystemItemDTO rootFolder) {
    List<FileSystemItem> resultFolderList = new ArrayList<>();
    List<FileSystemItem> resultFileList = new ArrayList<>();

    if (folderNameString == null || folderNameString.isEmpty()) {
      return null;
    }

    folderNameString = folderNameString.toLowerCase();
    searchFolderPathByNameHelper(folderNameString, rootFolder, "", resultFolderList, resultFileList);
    return new FolderAndFileResponseDTO(resultFolderList, resultFileList);
  }

  private void searchFolderPathByNameHelper(
    String folderNameString,
    FileSystemItemDTO currentFolder,
    String path,
    List<FileSystemItem> resultFolderList,
    List<FileSystemItem> resultFileList
    ) {
    if (path.isEmpty()) {
      path = currentFolder.getParent().getName();
    } else {
      path += "\\" + currentFolder.getParent().getName();
    }

    if (currentFolder.getParent().getName().toLowerCase().contains(folderNameString)) {
      resultFolderList.add(new FileSystemItem(currentFolder.getParent().getName(), path, "folder", currentFolder.getParent().getSize()));
    }

    List<FileSystemItemDTO> subFolders = currentFolder.getChildFolders();
    for (FileSystemItemDTO subFolder : subFolders) {
      searchFolderPathByNameHelper(folderNameString, subFolder, path, resultFolderList, resultFileList);
    }

    for (FileSystemItem file : currentFolder.getChildFiles()) {
      if (file.getName().toLowerCase().contains(folderNameString)) {
        resultFileList.add(new FileSystemItem(file.getName(), path + "\\" + file.getName(), file.getType(), file.getSize()));
      }
    }
  }

  @Override
  public Boolean createFolder(String folderName, String parentFolderPath) {
    if (folderName == null || folderName.isEmpty() || parentFolderPath == null || parentFolderPath.isEmpty()) {
      return false;
    }

    try {
      Path newFolderPath = fileScanService.getPathFromFolderUrl(parentFolderPath).resolve(folderName);
      Path parentPath = fileScanService.getPathFromFolderUrl(parentFolderPath);
      if (!Files.exists(parentPath) || !Files.isDirectory(parentPath)) {
        return false;
      }

      Files.createDirectory(newFolderPath, new FileAttribute<?>[0]);
      return true;
    } catch (Exception e) {
      System.out.println("Error creating folder: " + folderName);
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Boolean uploadFile(MultipartFile file, String parentFolderpath) {
    if (file == null || parentFolderpath == null || parentFolderpath.isEmpty()) {
      return false;
    }

    try {
      Path parentPath = fileScanService.getPathFromFolderUrl(parentFolderpath);

      if (!Files.exists(parentPath) || !Files.isDirectory(parentPath)) {
        return false;
      }
      
      file.transferTo(parentPath.resolve(file.getOriginalFilename()).toFile());
      return true;

    } catch (Exception e) {
      System.out.println("Error uploading file: ");
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Boolean renameFileSystemItem(String fileUrl, String newName) {
    if (fileUrl == null || fileUrl.isEmpty() || newName == null || newName.isEmpty()) {
      return false;
    }

    try {
      Path filePath = fileScanService.getPathFromFolderUrl(fileUrl);
      
      if (!Files.exists(filePath)) {
        return false;
      }

      Path newFilePath = fileScanService.getPathFromFolderUrl(fileUrl.substring(0, fileUrl.lastIndexOf("\\")) + "\\" + newName);
      Files.move(filePath, newFilePath);
      return true;

    } catch (Exception e) {
      System.out.println("Error renaming file: " + fileUrl);
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public File ZipAndDownloadFileSystemItem(List<String> fileUrlList) {
    String zipFileName = "download.zip";
    File zipFile = new File(fileScanService.getPathFromFolderUrl(fileUrlList.get(0)).toFile().getParent(), zipFileName);

    try (
      FileOutputStream fos = new FileOutputStream(zipFile);
      ZipOutputStream zos = new ZipOutputStream(fos)
    ) {
      for (String fileUrl : fileUrlList) {
        Path filePath = fileScanService.getPathFromFolderUrl(fileUrl);
        File file = filePath.toFile();

        if (!Files.isDirectory(filePath)) {
          zos.putNextEntry(new ZipEntry(file.getName()));
          Files.copy(filePath, zos);
          zos.closeEntry();
        }else{
          Files.walk(filePath).forEach(sourcePath -> {
            try {
              String zipEntryName = filePath.getFileName().toString() + "/" + filePath.relativize(sourcePath).toString();
              if (Files.isDirectory(sourcePath)) {
                zos.putNextEntry(new ZipEntry(zipEntryName + "/"));
                zos.closeEntry();
              } else {
                zos.putNextEntry(new ZipEntry(zipEntryName));
                Files.copy(sourcePath, zos);
                zos.closeEntry();
              }
            } catch (IOException e) {
                System.out.println("Error zipping file: " + sourcePath.toString());
                e.printStackTrace();
            }
          });
        }
      }
    } catch (Exception e) {
        System.out.println("Error zipping file: " + fileUrlList);
        e.printStackTrace();
    }

    return zipFile;
  }

  @Override
  public List<String> moveFileSystemItem(List<String> sourceUrlList, String destinationUrl) {
    if (sourceUrlList == null || sourceUrlList.isEmpty() || destinationUrl == null || destinationUrl.isEmpty()) {
      List<String> error = new ArrayList<>();
      error.add("Error: sourceUrlList or destinationUrl is empty");
      return error;
    }

    Path destinationPath = fileScanService.getPathFromFolderUrl(destinationUrl);
    if(!Files.exists(destinationPath) || !Files.isDirectory(destinationPath)){
      List<String> error = new ArrayList<>();
      error.add("Error: destinationUrl is not a valid directory");
      return error;
    }

    List<String> failedFileNameList = new CopyOnWriteArrayList<>();
    sourceUrlList.forEach(sourceUrl -> {
      Path sourcePath = fileScanService.getPathFromFolderUrl(sourceUrl);
      if (!Files.exists(sourcePath)) {
        failedFileNameList.add("No file for URL: " + sourceUrl);
        return;
      }
  
      Path newFilePath = destinationPath.resolve(sourcePath.getFileName());
      if(Files.exists(newFilePath)){
        List<String> temporaryList = new ArrayList<>();
        temporaryList.add(newFilePath.toString());
        deleteFileSystemItem(temporaryList);
      }

      try {
        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetDir = newFilePath.resolve(sourcePath.relativize(dir));
            Files.createDirectories(targetDir);
            return FileVisitResult.CONTINUE;
          }
  
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path targetFile = newFilePath.resolve(sourcePath.relativize(file));
            Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
          }
  
          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            } else {
              throw exc;
            }
          }
        });
      } catch (Exception e) {
        System.out.println("Error moving file: " + sourceUrlList);
        failedFileNameList.add(sourcePath.getFileName().toString());
        e.printStackTrace();
      }
    });

    return failedFileNameList;
  }

  public List<String> copyFileSystemItem(List<String> sourceUrlList, String destinationUrl) {
    if (sourceUrlList == null || sourceUrlList.isEmpty() || destinationUrl == null || destinationUrl.isEmpty()) {
      List<String> error = new ArrayList<>();
      error.add("Error: sourceUrlList or destinationUrl is empty");
      return error;
    }

    Path destinationPath = fileScanService.getPathFromFolderUrl(destinationUrl);
    if(!Files.exists(destinationPath) || !Files.isDirectory(destinationPath)){
      List<String> error = new ArrayList<>();
      error.add("Error: destinationUrl is not a valid directory");
      return error;
    }

    List<String> failedFileNameList = new CopyOnWriteArrayList<>();
    sourceUrlList.forEach(sourceUrl -> {
      Path sourcePath = fileScanService.getPathFromFolderUrl(sourceUrl);
      if (!Files.exists(sourcePath)) {
        failedFileNameList.add("No file for URL: " + sourceUrl);
        return;
      }
  
      Path newFilePath = destinationPath.resolve(sourcePath.getFileName());
      
      try {
        if (Files.isDirectory(sourcePath)) {
          Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
              Path targetDir = newFilePath.resolve(sourcePath.relativize(dir));
              try {
                Files.copy(dir, targetDir);
              } catch (FileAlreadyExistsException e) {
                if(!Files.isDirectory(newFilePath)){
                  throw e;
                }
              }
  
              return FileVisitResult.CONTINUE;
            }
  
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              Path targetFile = newFilePath.resolve(sourcePath.relativize(file));
              Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
  
              return FileVisitResult.CONTINUE;
            }
  
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("Error visiting file: " + file.toString());
                exc.printStackTrace();
                return FileVisitResult.CONTINUE;
            }
    
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    System.err.println("Error visiting directory: " + dir.toString());
                    exc.printStackTrace();
                }
                return FileVisitResult.CONTINUE;
            }
          });
        } else {
          Files.copy(sourcePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
      }catch (Exception e) {
        failedFileNameList.add(sourcePath.getFileName().toString());
        System.out.println("Error copying file: " + sourceUrlList);
        e.printStackTrace();
      }
    });

    return failedFileNameList;
  }

  @Override
  public List<String> deleteFileSystemItem(List<String> fileUrlList) {
    if (fileUrlList == null || fileUrlList.isEmpty()) {
      List<String> error = new ArrayList<>();
      error.add("Error: fileUrlList is empty");
      return error;
    }

    List<String> failedFileNameList = new CopyOnWriteArrayList<>();
    fileUrlList.forEach(fileUrl -> {
      Path filePath = fileScanService.getPathFromFolderUrl(fileUrl);
      if (!Files.exists(filePath)) {
        failedFileNameList.add("No file for URL: " + fileUrl);
        return;
      }

      try {
        Files.walkFileTree(filePath, new SimpleFileVisitor<Path>(){
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }
  
          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
  
          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.out.println("Error deleting file: " + file.toString());
            exc.printStackTrace();
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (Exception e) {
        failedFileNameList.add(filePath.getFileName().toString());
        System.out.println("Error deleting file: " + fileUrlList);
        e.printStackTrace();
      }
    });

    return failedFileNameList;
  }
}
