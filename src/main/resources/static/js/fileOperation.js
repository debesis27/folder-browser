import stateManager from "./stateManager.js";
import { findFolderByPath, formatBytes } from "./utils.js";
import { openFileExplorerModel } from "./miniFileBrowser.js";
import { fetchAndShowFileBrowser } from "./fileBrowser.js";

function showFolderInfo() {
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;

  const isFolder = selectedFileSystemItem.parent != undefined;
  const folderOrFileText = isFolder ? "Folder" : "File";
  const name = isFolder ? selectedFileSystemItem.parent.name : selectedFileSystemItem.name;
  const url = isFolder ? selectedFileSystemItem.parent.url : decodeURIComponent(selectedFileSystemItem.url);
  const type = isFolder ? "Folder" : selectedFileSystemItem.type;
  const size = isFolder ? formatBytes(selectedFileSystemItem.parent.size) : formatBytes(selectedFileSystemItem.size);

  $("#folderInfo").empty();

  let folderName = $("<h3></h3>").text(folderOrFileText + " name: " + name);
  let folederUrl = $("<p></p>").text(folderOrFileText + " url: " + url);
  let folderType = $("<p></p>").text(folderOrFileText + " type: " + type);
  let folderSize = $("<p></p>").text(folderOrFileText + " size: " + size);

  $("#folderInfo").append(folderName, folederUrl, folderType, folderSize);

  $("#folderInfoModal").children().children("h2").text(folderOrFileText + " Info");
  $("#folderInfoModal").removeClass("hide");
}

function renameFolder() {
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;
  const currentFolder = stateManager.getState().currentFolder;

  const isFolder = selectedFileSystemItem.parent != undefined;
  let fileUrl = isFolder ? selectedFileSystemItem.parent.url : selectedFileSystemItem.url;
  let fileExtension = isFolder ? "" : selectedFileSystemItem.name.substring(selectedFileSystemItem.name.lastIndexOf("."), selectedFileSystemItem.name.length);
  let oldName = isFolder ? selectedFileSystemItem.parent.name : selectedFileSystemItem.name.substring(0, selectedFileSystemItem.name.lastIndexOf("."));
  let newName = prompt("Enter new " + (isFolder ? "folder" : "file") + " name:", oldName);

  if (newName) {
    fetch("/api/folders/rename", {
      method: "PUT",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body: new URLSearchParams({
        fileUrl: fileUrl,
        newName: newName + (isFolder ? "" : fileExtension)
      })
    })
      .then(response => response.json())
      .then(response => {
        if (response) {
          fetchAndShowFileBrowser(currentFolder.parent.url);
          alert("File renamed successfully");
        } else {
          alert("Failed to rename file");
        }
      })
      .catch(error => {
        console.error("Error renaming file: ", error);
      });
  }
}

function downloadFolder() {
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;

  let isFolder = selectedFileSystemItem.parent != undefined;
  let fileUrl = isFolder ? selectedFileSystemItem.parent.url : selectedFileSystemItem.url;
  window.open("/api/folders/download?fileUrl=" + encodeURIComponent(fileUrl), "_blank");
}

function moveFolder(sourceUrl, destinationUrl) {
  const currentFolder = stateManager.getState().currentFolder;

  fetch("/api/folders/move", {
    method: "PUT",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({
      sourceUrl: sourceUrl,
      destinationUrl: destinationUrl
    })
  })
    .then(response => response.json())
    .then(data => {
      if (data) {
        fetchAndShowFileBrowser(currentFolder.parent.url);
        alert("File moved successfully");
      } else {
        alert("Failed to move file");
      }
    })
    .catch(error => {
      console.error("Error moving file: ", error);
    });
}

function copyFolder(sourceUrl, destinationUrl) {
  const currentFolder = stateManager.getState().currentFolder;

  fetch("/api/folders/copy", {
    method: "PUT",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({
      sourceUrl: sourceUrl,
      destinationUrl: destinationUrl
    })
  })
    .then(response => response.json())
    .then(data => {
      if (data) {
        fetchAndShowFileBrowser(currentFolder.parent.url);
        alert("File copied successfully");
      } else {
        alert("Failed to copy file");
      }
    })
    .catch(error => {
      console.error("Error copying file: ", error);
    });
}

function deleteFolder() {
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;
  const currentFolder = stateManager.getState().currentFolder;

  const isFolder = selectedFileSystemItem.parent != undefined;
  let fileUrl = isFolder ? selectedFileSystemItem.parent.url : selectedFileSystemItem.url;

  if (confirm("Are you sure you want to delete this " + (isFolder ? "folder" : "file") + "?")) {
    fetch("/api/folders/delete?fileUrl=" + encodeURIComponent(fileUrl), {
      method: "DELETE"
    })
      .then(response => response.json())
      .then(data => {
        if (data) {
          fetchAndShowFileBrowser(currentFolder.parent.url);
          alert("File deleted successfully");
        } else {
          alert("Failed to delete file");
        }
      })
      .catch(error => {
        console.error("Error deleting file: ", error);
      });
  }
}

function createFolder() {
  const currentFolder = stateManager.getState().currentFolder;

  const folderName = prompt("Enter folder name:");

  if (folderName) {
    fetch("/api/folders/create", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body: new URLSearchParams({
        folderName: folderName,
        parentFolderPath: currentFolder.parent.url
      })
    })
      .then(response => response.json())
      .then(data => {
        if (data) {
          fetchAndShowFileBrowser(currentFolder.parent.url);
          alert("Folder created successfully");
        } else {
          alert("Failed to create folder");
        }
      })
      .catch(error => {
        console.error("Error creating folder: ", error);
      });
  }
}

function uploadFile(file) {
  const currentFolder = stateManager.getState().currentFolder;

  const formData = new FormData();
  formData.append("file", file);
  formData.append("parentFolderPath", currentFolder.parent.url);

  fetch("/api/files/upload", {
    method: "POST",
    body: formData
  })
    .then(response => response.json())
    .then(data => {
      if(data){
        fetchAndShowFileBrowser(currentFolder.parent.url);
        alert("File uploaded successfully");
      }else{
        alert("Failed to upload file");
      }
    })
    .catch(error => {
      console.error("Error uploading file: ", error);
    })
}

export function bindFileOperationEvents() {
  $("#createFolderButton").click(createFolder);
  $("#uploadFileButton").click(function() {
    $("#fileUploadInput").click();
  });
  $("#folderInfoButton").click(showFolderInfo);
  $("#renameFolderButton").click(renameFolder);
  $("#downloadFolderButton").click(downloadFolder);
  $("#moveFolderButton").click(function(){
    stateManager.setState({isCopyOperation: false});
    openFileExplorerModel();
  });
  $("#copyFolderButton").click(function() {
    stateManager.setState({isCopyOperation: true});
    openFileExplorerModel();
  });
  $("#deleteFolderButton").click(deleteFolder);

  $("#fileUploadInput").on("change", function() {
    if(this.files && this.files.length > 0) {
      uploadFile(this.files[0]);
    }
  })
}

export {
  showFolderInfo,
  renameFolder,
  downloadFolder,
  moveFolder,
  copyFolder,
  deleteFolder,
  createFolder,
  uploadFile
}
