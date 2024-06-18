let fileExtensionImageMap = {
  "folder": "folder",
  "doc": "file-word",
  "docx": "file-word",
  "pdf": "file-pdf",
  "xls": "file-excel",
  "xlsx": "file-excel",
  "csv": "file-excel",
  "ppt": "file-powerpoint",
  "pptx": "file-powerpoint",
  "jpg": "file-image",
  "jpeg": "file-image",
  "png": "file-image",
  "gif": "file-image",
  "bmp": "file-image",
  "svg": "file-image",
  "mp3": "file-audio",
  "wav": "file-audio",
  "mp4": "file-video",
  "avi": "file-video",
  "mkv": "file-video",
  "mov": "file-video",
  "zip": "file-archive",
  "rar": "file-archive",
  "tar": "file-archive",
  "gz": "file-archive",
  "7z": "file-archive",
  "html": "file-code",
  "css": "file-code",
  "js": "file-code",
  "java": "file-code",
  "c": "file-code",
  "cpp": "file-code",
  "py": "file-code",
  "php": "file-code",
  "sql": "file-code",
  "json": "file-code",
  "xml": "file-code",
  "exe": "file-system",
  "dll": "file-system",
  "sys": "file-system",
  "iso": "file-system",
  "bin": "file-system",
  "bat": "file-system",
  "sh": "file-system",
  "cmd": "file-system",
}

let rootFolder;
let currentFolder;
let selectedFileSystemItem = null;
let selectedFileSystemItemElement = null;

document.addEventListener("DOMContentLoaded", function () {
  fetch("/api/folders")
    .then(response => response.json())
    .then(data => {
      rootFolder = data;
      currentFolder = rootFolder;
      openFolder(rootFolder);
    })
    .catch(error => {
      console.error("Error fetching folders from api: ", error);
    });

  // Deselect when clicking outside
  document.addEventListener("click", function (event) {
    if (!event.target.closest('.folder') && !event.target.closest('.file-specific-tools')) {
      deselectFolder();
    }
  });

  // Search bar input change
  $("#searchInput").on("input", debounce(function () {
    const query = $(this).val().trim();
    if (query) {
      searchFileSystemItems(query);
    } else {
      $("#search-results-container").empty();
    }
  }, 400));

  // Search bar click
  $("#searchInput").click(function () {
    $(".toolbar-right").addClass("hide");
    $(".folder-grid-view-container").addClass("hide");
    $("#searchResults").removeClass("hide");
    $("#logoOnSearchBar").addClass("hide");
    $("#backButtonFromSearchResult").removeClass("hide");
  });

  // Back button from Search click
  $("#backButtonFromSearchResult").click(function () {
    toggleSearchResults();
  });
})

function toggleSearchResults() {
  $("#searchInput").val("");
  $("#search-results-container").empty();

  $(".toolbar-right").toggleClass("hide");
  $(".folder-grid-view-container").toggleClass("hide");
  $("#searchResults").toggleClass("hide");
  $("#logoOnSearchBar").toggleClass("hide");
  $("#backButtonFromSearchResult").toggleClass("hide");
}

function debounce(func, delay) {
  let timeout;
  return function () {
    const context = this;
    const args = arguments;
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(context, args), delay);
  }
}

function openFolder(folder) {
  currentFolder = folder;
  renderFolderBrowser(folder);
  showBreadcrumbs(folder.parent.url);
}

function renderFolderBrowser(folder) {
  const folderBrowser = $("#folder-container");
  folderBrowser.empty();

  folder.childFolders.forEach(childFolder => {
    let folderDiv = $("<div></div>").addClass("folder");
    let folderName = $("<h3></h3>").text(childFolder.parent.name);
    let folderImage = $("<img>").attr("src", "/images/file-extensions/folder.svg");

    folderDiv.append(folderImage);
    folderDiv.append(folderName);
    folderDiv.click(function (event) {
      event.stopPropagation(); // Prevent the click from bubbling up
      selectFolder(folderDiv, childFolder);
    });
    folderDiv.dblclick(function () {
      openFolder(childFolder);
    })

    folderBrowser.append(folderDiv);
  })

  folder.childFiles.forEach(file => {
    let fileDiv = $("<div></div>").addClass("folder");
    let fileName = $("<h3></h3>").text(file.name);
    let fileImage = getFileImageElement(file.type);

    fileDiv.append(fileImage);
    fileDiv.append(fileName);
    fileDiv.click(function (event) {
      event.stopPropagation(); // Prevent the click from bubbling up
      selectFolder(fileDiv, file);
    });
    fileDiv.dblclick(function () {
      window.open("/file?path=" + file.url, "_blank");
    })

    folderBrowser.append(fileDiv);
  })
}

function showBreadcrumbs(folderUrl) {
  const breadcrumbsDiv = $("#breadcrumbs");
  breadcrumbsDiv.empty();

  let breadcrumbs = folderUrl.split("\\");
  breadcrumbs = breadcrumbs.slice(0, breadcrumbs.length);

  breadcrumbs.forEach(function (breadcrumb, index) {
    let span = $("<span></span>").text(breadcrumb);

    span.click(function () {
      const folder = findFolderByPath(breadcrumbs.slice(0, index + 1));
      openFolder(folder);
    })

    breadcrumbsDiv.append(span);

    if (index < breadcrumbs.length - 1) {
      breadcrumbsDiv.append("&nbsp;<b>></b>&nbsp;");
    }
  })
}

function findFolderByPath(path) {
  let folder = rootFolder;

  for (let i = 1; i < path.length; i++) {
    if(path[i] == "") continue;
    folder = folder.childFolders.find(childFolder => childFolder.parent.name == path[i]);
  }

  return folder;
}

function goBack() {
  if (currentFolder.parent.url == rootFolder.parent.url) return;

  const folder = findFolderByPath(currentFolder.parent.url.split("\\").slice(0, -1));
  openFolder(folder);
}

function getFileImageElement(fileType) {
  let extensionImage = fileExtensionImageMap[fileType] == undefined ? "file-document" : fileExtensionImageMap[fileType];
  let imageURL = "/images/file-extensions/" + extensionImage + ".svg";
  let image = $("<img>").attr("src", imageURL);

  return image;
}

function selectFolder(element, item) {
  if (selectedFileSystemItem != null) {
    if (selectedFileSystemItemElement == element) {
      deselectFolder();
      return;
    }

    deselectFolder();
  }

  selectedFileSystemItem = item;
  selectedFileSystemItemElement = element;
  element.addClass("selected");
  $(".file-specific-tools").removeClass("hide");
}

function deselectFolder() {
  if (!selectedFileSystemItem) return;

  selectedFileSystemItemElement.removeClass("selected");
  $(".file-specific-tools").addClass("hide");
  selectedFileSystemItem = null;
  selectedFileSystemItemElement = null;
}

function renameFolder() {
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
      .then(data => {
        if (data) {
          fetch("/api/folders")
            .then(response => response.json())
            .then(data => {
              rootFolder = data;
              currentFolder = findFolderByPath(currentFolder.parent.url.split("\\"));
              openFolder(currentFolder);
              alert("File renamed successfully");
            })
            .catch(error => {
              console.error("Error fetching folders from api: ", error);
            });
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
  let isFolder = selectedFileSystemItem.parent != undefined;
  let fileUrl = isFolder ? selectedFileSystemItem.parent.url : selectedFileSystemItem.url;
  window.open("/api/folders/download?fileUrl=" + encodeURIComponent(fileUrl), "_blank");
}

function moveFolder() {
  // Implement move functionality
}

function copyFolder() {
  // Implement copy functionality
}

function deleteFolder() {
  const isFolder = selectedFileSystemItem.parent != undefined;
  let fileUrl = isFolder ? selectedFileSystemItem.parent.url : selectedFileSystemItem.url;

  if (confirm("Are you sure you want to delete this " + (isFolder ? "folder" : "file") + "?")) {
    fetch("/api/folders/delete?fileUrl=" + encodeURIComponent(fileUrl), {
      method: "DELETE"
    })
      .then(data => {
        if (data) {
          fetch("/api/folders")
            .then(response => response.json())
            .then(data => {
              rootFolder = data;
              currentFolder = findFolderByPath(currentFolder.parent.url.split("\\"));
              openFolder(currentFolder);
              alert("File deleted successfully");
            })
            .catch(error => {
              console.error("Error fetching folders from api: ", error);
            });
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
      .then(data => {
        if (data) {
          fetch("/api/folders")
            .then(response => response.json())
            .then(data => {
              rootFolder = data;
              if(currentFolder != rootFolder) currentFolder.parent.url = currentFolder.parent.url.concat("\\" + folderName);
              else currentFolder.parent.url = currentFolder.parent.url.concat(folderName);
              currentFolder = findFolderByPath(currentFolder.parent.url.split("\\"));
              openFolder(currentFolder);
              alert("Folder created successfully");
            })
            .catch(error => {
              console.error("Error fetching folders from api: ", error);
            });
        } else {
          alert("Failed to create folder");
        }
      })
      .catch(error => {
        console.error("Error creating folder: ", error);
      });
  }
}

$("#uploadButton").on("click", function() {
  $("#fileUploadInput").click();
})

$("#fileUploadInput").on("change", function() {
  if(this.files && this.files.length > 0) {
    uploadFile(this.files[0]);
  }
})

function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("parentFolderPath", currentFolder.parent.url);

  fetch("/api/files/upload", {
    method: "POST",
    body: formData
  })
    .then(data => {
      if(data){
        fetch("/api/folders")
          .then(response => response.json())
          .then(data => {
            rootFolder = data;
            currentFolder = findFolderByPath(currentFolder.parent.url.split("\\"));
            openFolder(currentFolder);
            alert("File uploaded successfully");
          })
          .catch(error => {
            console.error("Error fetching folders from api: ", error);
          });
      }else{
        alert("Failed to upload file");
      }
    })
    .catch(error => {
      console.error("Error uploading file: ", error);
    })
}

function searchFileSystemItems(query) {
  fetch("/api/folders/search?folderName=" + query)
    .then(response => response.json())
    .then(data => {
      displaySearchResults(data);
    })
    .catch(error => {
      console.error("Error fetching search results from api: ", error);
    });
}

function displaySearchResults(results) {
  const searchResultsContainer = $("#search-results-container");
  searchResultsContainer.empty();

  results.folders.forEach(folder => {
    let searchResultItem = getSearchResultItemElement(folder, function () {
      currentFolder = findFolderByPath(folder.url.split("\\").slice(0));
      toggleSearchResults();
      openFolder(currentFolder);
    });
    searchResultsContainer.append(searchResultItem);
  })

  results.files.forEach(file => {
    let searchResultItem = getSearchResultItemElement(file, function () {
      window.open("/file?path=" + encodeURIComponent(file.url), "_blank");
    });
    searchResultsContainer.append(searchResultItem);
  })
}

function getSearchResultItemElement(fileSystemItem, onClickFunction) {
  let resultItem = $("<div></div>").addClass("search-result-item");
  let resultItemName = $("<span></span>").text(fileSystemItem.name).addClass("result-name");
  let resultItemImage = getFileImageElement(fileSystemItem.type);
  let resultItemPath = $("<span></span>").text(fileSystemItem.url).addClass("result-path");

  resultItem.append(resultItemImage, resultItemName, resultItemPath);
  resultItem.click(function () {
    onClickFunction();
  });
  return resultItem;
}
