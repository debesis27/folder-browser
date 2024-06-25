import stateManager from "./stateManager.js";
import { getFileImageElement, findFolderByPath } from "./utils.js";

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
  let selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;
  let selectedFileSystemItemElement = stateManager.getState().selectedFileSystemItemElement;

  if (selectedFileSystemItem != null) {
    if (selectedFileSystemItemElement == element) {
      deselectFolder();
      return;
    }

    deselectFolder();
  }

  stateManager.setState({selectedFileSystemItem: item, selectedFileSystemItemElement: element});
  element.addClass("selected");
  $(".file-specific-tools").removeClass("hide");
}

function deselectFolder() {
  let selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;
  let selectedFileSystemItemElement = stateManager.getState().selectedFileSystemItemElement;

  if (!selectedFileSystemItem) return;

  selectedFileSystemItemElement.removeClass("selected");
  $(".file-specific-tools").addClass("hide");
  stateManager.setState({selectedFileSystemItem: null, selectedFileSystemItemElement: null});
}

// Deselect when clicking outside
document.addEventListener("click", function (event) {
  if (!event.target.closest('.folder') && !event.target.closest('.file-specific-tools')) {
    deselectFolder();
  }
});

$(".pop-breadcrumb").click(popBreadcrumb);

export {
  fetchAndShowFileBrowser,
  openFolder
}
