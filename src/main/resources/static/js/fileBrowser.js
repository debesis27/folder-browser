import stateManager from "./stateManager.js";
import { getFileImageElement, findFolderByPath, formatBytes } from "./utils.js";

function fetchAndShowFileBrowser(currentFolderUrl = null) {
  fetch("/api/folders")
    .then(response => response.json())
    .then(data => {
      stateManager.setState({rootFolder: data});
      currentFolderUrl != null ? stateManager.setState({currentFolder: findFolderByPath(currentFolderUrl.split("\\"), data)}) : stateManager.setState({currentFolder: data});
      openFolder(stateManager.getState().currentFolder);
    })
    .catch(error => {
      console.error("Error fetching folders from api: ", error);
    });
}

function openFolder(folder) {
  stateManager.setState({currentFolder: folder});
  deselectFolder();
  renderFolderBrowser(folder);
  showBreadcrumbs(folder.parent.url);
  showCurrentFolderInfo(folder);
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
      window.open("/file?path=" + encodeURIComponent(file.url), "_blank");
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
      const folder = findFolderByPath(breadcrumbs.slice(0, index + 1), stateManager.getState().rootFolder);
      openFolder(folder);
    })

    breadcrumbsDiv.append(span);

    if (index < breadcrumbs.length - 1) {
      breadcrumbsDiv.append("&nbsp;<b>></b>&nbsp;");
    }
  })
}

function popBreadcrumb() {
  let currentFolder = stateManager.getState().currentFolder;
  let rootFolder = stateManager.getState().rootFolder;

  if (currentFolder.parent.url == rootFolder.parent.url) return;

  const folder = findFolderByPath(currentFolder.parent.url.split("\\").slice(0, -1), stateManager.getState().rootFolder);
  openFolder(folder);
}

function selectFolder(element, item) {
  let selectedFileSystemItemList = stateManager.getState().selectedFileSystemItemList;
  let selectedFileSystemItemElementList = stateManager.getState().selectedFileSystemItemElementList;

  if (selectedFileSystemItemList.length > 0) {
    if (selectedFileSystemItemList.includes(item)) {
      deselectFolder(item);
      return;
    }
  }

  selectedFileSystemItemList.push(item);
  selectedFileSystemItemElementList.push(element);

  if(selectedFileSystemItemList.length == 1 ){
    $("#folderInfoButton").removeClass("hide");
    $("#renameFolderButton").removeClass("hide");
  }else{
    $("#folderInfoButton").addClass("hide");
    $("#renameFolderButton").addClass("hide");
  }

  stateManager.setState({selectedFileSystemItemList: selectedFileSystemItemList, selectedFileSystemItemElementList: selectedFileSystemItemElementList});
  element.addClass("selected");
  $(".toolbar-right").removeClass("hide");
}

function deselectFolder(item = null) {
  let selectedFileSystemItemList = stateManager.getState().selectedFileSystemItemList;
  let selectedFileSystemItemElementList = stateManager.getState().selectedFileSystemItemElementList;

  if (selectedFileSystemItemList.length == 0) return;
  if(item == null || selectedFileSystemItemList.length == 1){
    selectedFileSystemItemElementList.forEach(element => {
      element.removeClass("selected");
    });
    $(".toolbar-right").addClass("hide");
    stateManager.setState({selectedFileSystemItemList: [], selectedFileSystemItemElementList: []});
  }else{
    let index = selectedFileSystemItemList.indexOf(item);
    selectedFileSystemItemElementList[index].removeClass("selected");
    selectedFileSystemItemList.splice(index, 1);
    selectedFileSystemItemElementList.splice(index, 1);

    if(selectedFileSystemItemList.length == 1){
      $("#folderInfoButton").removeClass("hide");
      $("#renameFolderButton").removeClass("hide");
    }else{
      $("#folderInfoButton").addClass("hide");
      $("#renameFolderButton").addClass("hide");
    }
    stateManager.setState({selectedFileSystemItemList: selectedFileSystemItemList, selectedFileSystemItemElementList: selectedFileSystemItemElementList});
  }
}

function showCurrentFolderInfo(currentFolder) {
  let element = $("#currentFolderInfo");
  element.empty();

  let heading = $("<h3></h3>").text("Folder Info");
  let size = $("<p></p>").text("Size: " + formatBytes(currentFolder.parent.size));
  let folders = $("<p></p>").text("Folders: " + currentFolder.childFolders.length);
  let files = $("<p></p>").text("Files: " + currentFolder.childFiles.length);

  element.append(heading, size, folders, files);
}

export function bindFileBrowserEvents() {
  // Deselect when clicking outside of the folder
  document.addEventListener("click", function (event) {
    if (!event.target.closest('.folder') && !event.target.closest('.toolbar-right')) {
      deselectFolder();
    }
  });

  $(".pop-breadcrumb").click(popBreadcrumb);

  $("#homeButton").click(function() {
    fetchAndShowFileBrowser();
  });
}

export {
  fetchAndShowFileBrowser,
  openFolder,
  deselectFolder
}
