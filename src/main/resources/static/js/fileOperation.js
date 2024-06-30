import stateManager from "./stateManager.js";
import { findFolderByPath, formatBytes } from "./utils.js";
import { openFileExplorerModel } from "./miniFileBrowser.js";
import { fetchAndShowFileBrowser, deselectFolder } from "./fileBrowser.js";

function showFolderInfo() {
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItemList[0];

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
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItemList[0];
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
  const selectedFileSystemItemList = stateManager.getState().selectedFileSystemItemList;
  const fileUrlList = [];

  for(let i = 0; i < selectedFileSystemItemList.length; i++) {
    let isFolder = selectedFileSystemItemList[i].parent != undefined;
    let fileUrl = isFolder ? selectedFileSystemItemList[i].parent.url : selectedFileSystemItemList[i].url;
    fileUrlList.push(fileUrl);
  }

  fetch("/api/folders/download", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(fileUrlList)
  })
  .then(response => {
    if (response.ok) return response.blob();
    throw new Error('Network response was not ok.');
  })
  .then(blob => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = "download.zip";
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
  })
  .catch(error => console.error('There was an error:', error));
}

function moveFolder(sourceUrlList, destinationUrl) {
  const currentFolder = stateManager.getState().currentFolder;

  fetch("/api/folders/move", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      sourceUrlList: sourceUrlList,
      destinationUrl: destinationUrl
    })
  })
    .then(response => response.json())
    .then(data => {
      if (data.length == 0) {
        fetchAndShowFileBrowser(currentFolder.parent.url);
        alert("File moved successfully");
      } else {
        alert("Some or all files failed to move. Please check the server logs for more information.");
        console.error("Failed to move files: \n", data);
      }
    })
    .catch(error => {
      console.error("Error moving file: ", error);
    });
}

function copyFolder(sourceUrlList, destinationUrl) {
  const currentFolder = stateManager.getState().currentFolder;

  fetch("/api/folders/copy", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      sourceUrlList: sourceUrlList,
      destinationUrl: destinationUrl
    })
  })
    .then(response => response.json())
    .then(data => {
      if (data.length == 0) {
        fetchAndShowFileBrowser(currentFolder.parent.url);
        alert("File copied successfully");
      } else {
        alert("Some or all files failed to copy. Please check the server logs for more information.");
        console.error("Failed to copy files: \n", data);
      }
    })
    .catch(error => {
      console.error("Error copying file: ", error);
    });
}

function deleteFolder() {
  const selectedFileSystemItemList = stateManager.getState().selectedFileSystemItemList;
  const currentFolder = stateManager.getState().currentFolder;

  const fileUrlList = [];
  for(let i = 0; i < selectedFileSystemItemList.length; i++) {
    let isFolder = selectedFileSystemItemList[i].parent != undefined;
    let fileUrl = isFolder ? selectedFileSystemItemList[i].parent.url : selectedFileSystemItemList[i].url;
    fileUrlList.push(fileUrl);
  }

  if (confirm("Are you sure you want to delete the selected items?")) {
    fetch("/api/folders/delete", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(fileUrlList)
    })
      .then(response => response.json())
      .then(data => {
        if (data.length == 0) {
          fetchAndShowFileBrowser(currentFolder.parent.url);
          alert("File deleted successfully");
        } else {
          alert("Some or all files failed to delete. Please check the server logs for more information.");
          console.error("Failed to delete files: \n", data);
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
  $("#uploadFolderButton").click(function() {
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
