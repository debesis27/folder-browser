let breadcrumbs = [];
let fileExtensionImageMap = {
  "doc" : "file-word",
  "docx" : "file-word",
  "pdf" : "file-pdf",
  "xls" : "file-excel",
  "xlsx" : "file-excel",
  "csv" : "file-excel",
  "ppt" : "file-powerpoint",
  "pptx" : "file-powerpoint",
  "jpg" : "file-image",
  "jpeg" : "file-image",
  "png" : "file-image",
  "gif" : "file-image",
  "bmp" : "file-image",
  "svg" : "file-image",
  "mp3" : "file-audio",
  "wav" : "file-audio",
  "mp4" : "file-video",
  "avi" : "file-video",
  "mkv" : "file-video",
  "mov" : "file-video",
  "zip" : "file-archive",
  "rar" : "file-archive",
  "tar" : "file-archive",
  "gz" : "file-archive",
  "7z" : "file-archive",
  "html" : "file-code",
  "css" : "file-code",
  "js" : "file-code",
  "java" : "file-code",
  "c" : "file-code",
  "cpp" : "file-code",
  "py" : "file-code",
  "php" : "file-code",
  "sql" : "file-code",
  "json" : "file-code",
  "xml" : "file-code",
  "exe" : "file-system",
  "dll" : "file-system",
  "sys" : "file-system",
  "iso" : "file-system",
  "bin" : "file-system",
  "bat" : "file-system",
  "sh" : "file-system",
  "cmd" : "file-system",
}

let rootFolder;

document.addEventListener("DOMContentLoaded", function() {
  fetch("/api/folders")
    .then(response => response.json())
    .then(data => {
      rootFolder = data;
      openFolder(rootFolder);
    })
    .catch(error => {
      console.error("Error fetching folders from api: ", error);
    });
})

function openFolder(folder) {
  renderFolderBrowser(folder);
  addBreadCrumb(folder.name);
  showBreadcrumbs();
}

function renderFolderBrowser(folder) {
  const folderBrowser = $("#folder-container");
  folderBrowser.empty();

  folder.subFolders.forEach(subFolder => {
    let folderDiv = $("<div></div>").addClass("folder");
    let folderName = $("<h3></h3>").text(subFolder.name);
    let folderImage = $("<img>").attr("src", "/images/file-extensions/folder.svg");

    folderDiv.append(folderImage);
    folderDiv.append(folderName);
    folderDiv.click(function() {
      openFolder(subFolder);
    })
    
    folderBrowser.append(folderDiv);
  })

  folder.files.forEach(file => {
    let fileDiv = $("<div></div>").addClass("folder");
    let fileName = $("<h3></h3>").text(file.name);
    let fileImage = getFileImageElementFromName(file.name);

    fileDiv.append(fileImage);
    fileDiv.append(fileName);
    fileDiv.click(function() {
      window.open(file.url, "_blank");
    })

    folderBrowser.append(fileDiv);
  })
}

function addBreadCrumb(folderName) {
  breadcrumbs.push(folderName);
}

function showBreadcrumbs() {
  const breadcrumbsDiv = $("#breadcrumbs");

  breadcrumbsDiv.empty();

  breadcrumbs.forEach(function(breadcrumb, index) {
    let span = $("<span></span>").text(breadcrumb);

    span.click(function() {
      const folder = removeBreadCrumbTill(index + 1);
      renderFolderBrowser(folder);
      showBreadcrumbs();
    })

    breadcrumbsDiv.append(span);

    if (index < breadcrumbs.length - 1) {
      breadcrumbsDiv.append("&nbsp;<b>></b>&nbsp;");
    }
  })
}

function findFolderByPath(path) {
  let folder = rootFolder;

  for(let i = 1; i < path.length; i++) {
    folder = folder.subFolders.find(subFolder => subFolder.name == path[i]);
  }

  return folder;
}

function removeBreadCrumbTill(index) {
  breadcrumbs = breadcrumbs.slice(0, index);
  return findFolderByPath(breadcrumbs);
}

function goBack() {
  if (breadcrumbs.length == 1) {
    return;
  }

  folder = removeBreadCrumbTill(breadcrumbs.length - 1);
  renderFolderBrowser(folder);
  showBreadcrumbs();
}

function getFileImageElementFromName(fileName) {
  let extension = fileName.split('.').pop().trim();
  let extensionImage = getFileImage(extension);
  let imageURL = "/images/file-extensions/" + extensionImage + ".svg";
  let image = $("<img>").attr("src", imageURL);

  return image;
}

function getFileImage(extension) {
  let image = fileExtensionImageMap[extension] == undefined ? "file-document" : fileExtensionImageMap[extension];
  return image;
}

window.onload = function() {
  let files = document.getElementsByClassName("file-iter");
  for (let i = 0; i < files.length; i++) {
    addFileImage(files[i]);
  }
}
