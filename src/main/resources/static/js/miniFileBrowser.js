import stateManager from "./stateManager.js";
import { copyFolder, moveFolder } from "./fileOperation.js";

function handleSelectDestinationFolderButtonClick() {
  const selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;
  const selectedDestinationFolder = stateManager.getState().selectedDestinationFolder;
  const isCopyOperation = stateManager.getState().isCopyOperation;

  if(selectedDestinationFolder == null) return;

  const sourceUrl = selectedFileSystemItem.parent != undefined ? selectedFileSystemItem.parent.url : selectedFileSystemItem.url;
  const destinationUrl = selectedDestinationFolder.parent.url;

  isCopyOperation ? copyFolder(sourceUrl, destinationUrl) : moveFolder(sourceUrl, destinationUrl);

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
  let selectedFileSystemItem = stateManager.getState().selectedFileSystemItem;
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

$(".close").on("click", function() {
  $(".modal").addClass("hide");
  stateManager.setState({selectedDestinationFolder: null});
})

$("#selectDestinationFolderButton").click(handleSelectDestinationFolderButtonClick);

export {
  handleSelectDestinationFolderButtonClick,
  openFileExplorerModel
}
