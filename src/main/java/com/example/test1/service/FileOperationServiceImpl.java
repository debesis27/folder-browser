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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import com.example.test1.entity.FileSystemItem;
import com.example.test1.entity.FileSystemItemDTO;
import com.example.test1.entity.FolderAndFileResponseDTO;

@Service
public class FileOperationServiceImpl implements FileOperationService{
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
      Path parentPath = Path.of(parentFolderPath);
      if (!Files.exists(parentPath) || !Files.isDirectory(parentPath)) {
        return false;
      }

      Path newFolderPath = parentPath.resolve(folderName);
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
      Path parentPath = Path.of(parentFolderpath);

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
      Path filePath = Path.of(fileUrl);
      
      if (!Files.exists(filePath)) {
        return false;
      }

      Path newFilePath = Path.of(fileUrl.substring(0, fileUrl.lastIndexOf("\\")) + "\\" + newName);
      Files.move(filePath, newFilePath);
      return true;

    } catch (Exception e) {
      System.out.println("Error renaming file: " + fileUrl);
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public File ZipAndDownloadFileSystemItem(String fileUrl) {
    File file = new File(fileUrl);
    Path filePath = Path.of(fileUrl);
    String zipFileName = file.getName() + ".zip";
    File zipFile = new File(file.getParent(), zipFileName);

    try(
      FileOutputStream fos = new FileOutputStream(zipFile);
      ZipOutputStream zos = new ZipOutputStream(fos)
    ) {
      if(!Files.isDirectory(filePath)){
        zos.putNextEntry(new ZipEntry(file.getName()));
        Files.copy(filePath, zos);
        zos.closeEntry();
        return zipFile;
      }

      Files.walk(filePath).forEach(sourcePath -> {
        try {
          String ZipEntryname = filePath.getFileName().toString() + "/" + filePath.relativize(sourcePath).toString();
          if (Files.isDirectory(sourcePath)) {
            zos.putNextEntry(new ZipEntry(ZipEntryname + "/"));
            zos.closeEntry();
          } else {
            zos.putNextEntry(new ZipEntry(ZipEntryname));
            Files.copy(sourcePath, zos);
            zos.closeEntry();
          }
        } catch (IOException e) {
          System.out.println("Error zipping file: " + sourcePath.toString());
          e.printStackTrace();
        }
      });

    } catch (Exception e) {
      System.out.println("Error zipping file: " + fileUrl);
      e.printStackTrace();
      return null;
    }

    return zipFile;
  }

  @Override
  public Boolean moveFileSystemItem(String sourceUrl, String destinationUrl) {
    if (sourceUrl == null || sourceUrl.isEmpty() || destinationUrl == null || destinationUrl.isEmpty()) {
      return false;
    }

    try {
      Path sourcePath = Path.of(sourceUrl);
      Path destinationPath = Path.of(destinationUrl);
      if (!Files.exists(sourcePath) || !Files.exists(destinationPath)) {
        return false;
      }

      Path newFilePath = destinationPath.resolve(sourcePath.getFileName());
      
      if(Files.exists(newFilePath)){
        deleteFileSystemItem(newFilePath.toString());
      }

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

      return true;

    } catch (Exception e) {
      System.out.println("Error moving file: " + sourceUrl);
      e.printStackTrace();
      return false;
    }
  }

  public Boolean copyFileSystemItem(String sourceUrl, String destinationUrl) {
    if (sourceUrl == null || sourceUrl.isEmpty() || destinationUrl == null || destinationUrl.isEmpty()) {
      return false;
    }

    try {
      Path sourcePath = Path.of(sourceUrl);
      Path destinationPath = Path.of(destinationUrl);
      if (!Files.exists(sourcePath) || !Files.exists(destinationPath)) {
        return false;
      }

      Path newFilePath = destinationPath.resolve(sourcePath.getFileName());
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
      return true;

    } catch (Exception e) {
      System.out.println("Error copying file: " + sourceUrl);
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Boolean deleteFileSystemItem(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) {
      return false;
    }

    try {
      Path filePath = Path.of(fileUrl);
      if (!Files.exists(filePath)) {
        return false;
      }

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

      return true;

    } catch (Exception e) {
      System.out.println("Error deleting file: " + fileUrl);
      e.printStackTrace();
      return false;
    }
  }
}
