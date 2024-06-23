package com.example.test1.service;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
      resultFolderList.add(
          new FileSystemItem(currentFolder.getParent().getName(), path, "folder", currentFolder.getParent().getSize()));
    }

    List<FileSystemItemDTO> subFolders = currentFolder.getChildFolders();
    for (FileSystemItemDTO subFolder : subFolders) {
      searchFolderPathByNameHelper(folderNameString, subFolder, path, resultFolderList, resultFileList);
    }

    for (FileSystemItem file : currentFolder.getChildFiles()) {
      if (file.getName().toLowerCase().contains(folderNameString)) {
        resultFileList
            .add(new FileSystemItem(file.getName(), path + "\\" + file.getName(), file.getType(), file.getSize()));
      }
    }
  }

  public Boolean createFolder(String folderName, String parentFolderPath, FileSystemItemDTO rootFolder) {
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

  public Boolean uploadFile(MultipartFile file, String parentFolderpath, FileSystemItemDTO rootFolder) {
    if (file == null || parentFolderpath == null || parentFolderpath.isEmpty()) {
      return false;
    }

    try {
      Path parentPath = Path.of(parentFolderpath);
      if (!Files.exists(parentPath) || !Files.isDirectory(parentPath)) {
        return false;
      }

      File newFile = new File(parentFolderpath + "\\" + file.getOriginalFilename());
      file.transferTo(newFile);
      return true;

    } catch (Exception e) {
      System.out.println("Error uploading file: ");
      e.printStackTrace();
      return false;
    }
  }

  public Boolean renameFileSystemItem(String fileUrl, String newName, FileSystemItemDTO rootFolder) {
    fileUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);

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

  public File downloadFileSystemItem(String fileUrl, FileSystemItemDTO rootFolder) {
    fileUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
    File file = new File(fileUrl);
    String zipFileName = file.getName() + ".zip";
    File zipFile = new File(file.getParent(), zipFileName);

    try (
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);) {
      downloadFileSystemItemHelper(file, file.getName(), zos);

    } catch (Exception e) {
      System.out.println("Error zipping file: " + fileUrl);
      e.printStackTrace();
      return null;
    }

    return zipFile;
  }

  private void downloadFileSystemItemHelper(File folder, String name, ZipOutputStream zos) {
    if (folder.isDirectory()) {
      File[] subFiles = folder.listFiles();
      if (subFiles == null || subFiles.length == 0) {
        try {
          zos.putNextEntry(new ZipEntry(name + "/"));
          zos.closeEntry();
        } catch (IOException e) {
          System.out.println("Error zipping folder: " + name);
          e.printStackTrace();
        }
      } else {
        for (File file : subFiles) {
          downloadFileSystemItemHelper(file, name + "\\" + file.getName(), zos);
        }
      }
    } else {
      try {
        zos.putNextEntry(new ZipEntry(name));
        Files.copy(folder.toPath(), zos);
        zos.closeEntry();
      } catch (Exception e) {
        System.out.println("Error zipping file: " + name);
        e.printStackTrace();
      }
    }
  }

  public Boolean moveFileSystemItem(String fileUrl, String destinationUrl, FileSystemItemDTO rootFolder) {
    fileUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
    destinationUrl = URLDecoder.decode(destinationUrl, StandardCharsets.UTF_8);

    if (fileUrl == null || fileUrl.isEmpty() || destinationUrl == null || destinationUrl.isEmpty()) {
      return false;
    }

    try {
      Path filePath = Path.of(fileUrl);
      Path destinationPath = Path.of(destinationUrl);
      if (!Files.exists(filePath) || !Files.exists(destinationPath)) {
        return false;
      }

      Path newFilePath = Path.of(destinationUrl + "\\" + filePath.getFileName());
      Files.move(filePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
      return true;

    } catch (DirectoryNotEmptyException e) {
      deleteFileSystemItem(fileUrl, rootFolder);
      moveFileSystemItem(fileUrl, destinationUrl, rootFolder);
      return true;

    } catch (Exception e) {
      System.out.println("Error moving file: " + fileUrl);
      e.printStackTrace();
      return false;
    }
  }

  public Boolean copyFileSystemItem(String fileUrl, String destinationUrl, FileSystemItemDTO rootFolder) {
    fileUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
    destinationUrl = URLDecoder.decode(destinationUrl, StandardCharsets.UTF_8);

    if (fileUrl == null || fileUrl.isEmpty() || destinationUrl == null || destinationUrl.isEmpty()) {
      return false;
    }

    try {
      Path filePath = Path.of(fileUrl);
      Path destinationPath = Path.of(destinationUrl);
      if (!Files.exists(filePath) || !Files.exists(destinationPath)) {
        return false;
      }

      Path newFilePath = Path.of(destinationUrl + "\\" + filePath.getFileName());
      if (Files.isDirectory(filePath)) {
        copyFileSystemItemHelper(filePath, newFilePath);
      } else {
        Files.copy(filePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
      }
      return true;

    } catch (Exception e) {
      System.out.println("Error copying file: " + fileUrl);
      e.printStackTrace();
      return false;
    }
  }

  public static void copyFileSystemItemHelper(Path source, Path target) throws IOException {
    Files.walk(source)
        .forEach(sourcePath -> {
          Path targetPath = target.resolve(source.relativize(sourcePath));
          try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException ex) {
            throw new RuntimeException("Error copying file: " + sourcePath.toString(), ex);
          }
        });
  }

  public Boolean deleteFileSystemItem(String fileUrl, FileSystemItemDTO rootFolder) {
    fileUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);

    if (fileUrl == null || fileUrl.isEmpty()) {
      return false;
    }

    try {
      Path filePath = Path.of(fileUrl);
      if (!Files.exists(filePath)) {
        return false;
      }

      return deleteFileSystemItemHelper(new File(fileUrl));

    } catch (Exception e) {
      System.out.println("Error deleting file: " + fileUrl);
      e.printStackTrace();
      return false;
    }
  }

  private Boolean deleteFileSystemItemHelper(File fileSystemItem) {
    if (fileSystemItem.isDirectory()) {
      File[] subFiles = fileSystemItem.listFiles();
      for (File subFile : subFiles) {
        deleteFileSystemItemHelper(subFile);
      }
    }

    return fileSystemItem.delete();
  }
}
