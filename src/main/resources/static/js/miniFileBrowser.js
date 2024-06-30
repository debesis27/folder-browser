import stateManager from "./stateManager.js";
import { copyFolder, moveFolder } from "./fileOperation.js";
import { findFolderByPath } from "./utils.js";

function handleSelectDestinationFolderButtonClick() {
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItemList;
  const selectedDestinationFolder = stateManager.getState().selectedDestinationFolder;
  const isCopyOperation = stateManager.getState().isCopyOperation;

  if(selectedDestinationFolder == null) return;

  const destinationUrl = selectedDestinationFolder.parent.url;
  const sourceUrlList = [];

  for(let i = 0; i < selectedFileSystemItem.length; i++) {
    sourceUrlList.push(selectedFileSystemItem[i].parent != undefined ? selectedFileSystemItem[i].parent.url : selectedFileSystemItem[i].url);
  }

  isCopyOperation ? copyFolder(sourceUrlList, destinationUrl) : moveFolder(sourceUrlList, destinationUrl);

  $(".modal").addClass("hide");
  stateManager.setState({selectedDestinationFolder: null});
}

function openFileExplorerModel() {
  let rootFolder = stateManager.getState().rootFolder;

  renderFolderExplorer(rootFolder);
  stateManager.setState({selectedDestinationFolder: rootFolder});
  $(".modal").removeClass("hide");
}

function renderFolderExplorer(currentFolder) {
  let selectedFileSystemItem = stateManager.getState().selectedFileSystemItemList;
  let selectedDestinationFolder = stateManager.getState().selectedDestinationFolder;

  $("#miniFolderExplorer").empty();

  let goBackDiv = $("<div></div>").addClass("mini-folder");
  let goBackName = $("<h3></h3>").text("..");
  goBackDiv.append(goBackName);
  goBackDiv.click(function (event) {
    event.stopPropagation(); // Prevent the click from bubbling up
    selectedDestinationFolder = selectedDestinationFolder.parent.url == "D:" ? selectedDestinationFolder : findFolderByPath(selectedDestinationFolder.parent.url.split("\\").slice(0, -1), stateManager.getState().rootFolder);
    stateManager.setState({selectedDestinationFolder: selectedDestinationFolder});
    renderFolderExplorer(selectedDestinationFolder);
  })
  $("#miniFolderExplorer").append(goBackDiv);

  currentFolder.childFolders.forEach(childFolder => {
    if(selectedFileSystemItem.parent != null && childFolder.parent.url == selectedFileSystemItem.parent.url) return;

    let folderDiv = $("<div></div>").addClass("mini-folder");
    let folderName = $("<h3></h3>").text(childFolder.parent.name);

    folderDiv.append(folderName);
    folderDiv.click(function (event) {
      event.stopPropagation(); // Prevent the click from bubbling up
      selectedDestinationFolder = childFolder;
      stateManager.setState({selectedDestinationFolder: selectedDestinationFolder});
      renderFolderExplorer(childFolder);
    });

    $("#miniFolderExplorer").append(folderDiv);
  })
}

export function bindMiniFileBrowserEvents() {
  $(".close").on("click", function() {
    $(".modal").addClass("hide");
    stateManager.setState({selectedDestinationFolder: null});
  })
  
  $("#selectDestinationFolderButton").click(handleSelectDestinationFolderButtonClick);
}

export {
  openFileExplorerModel
}
