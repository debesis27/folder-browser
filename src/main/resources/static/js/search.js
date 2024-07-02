import stateManager from "./stateManager.js";
import { openFolder } from "./fileBrowser.js";
import { debounce, getFileImageElement, findFolderByPath } from "./utils.js";

function searchFileSystemItems(query) {
  fetch("/api/folders/search?folderName=" + encodeURIComponent(query))
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
      $("#searchInput").val("");
      $("#search-results-container").empty();

      $(".main-content-container").removeClass("hide");
      $("#searchResults").addClass("hide");
      $("#logoOnSearchBar").removeClass("hide");
      $("#backButtonFromSearchResult").addClass("hide");

      stateManager.setState({ currentFolder: findFolderByPath(folder.url.split("\\").slice(0), stateManager.getState().rootFolder) });
      openFolder(stateManager.getState().currentFolder);
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

export function bindSearchEvents() {
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
    $(".main-content-container").addClass("hide");
    $("#searchResults").removeClass("hide");
    $("#logoOnSearchBar").addClass("hide");
    $("#backButtonFromSearchResult").removeClass("hide");
  });

  // Back button from Search click
  $("#backButtonFromSearchResult").click(function () {
    $("#searchInput").val("");
    $("#search-results-container").empty();

    $(".main-content-container").toggleClass("hide");
    $("#searchResults").toggleClass("hide");
    $("#logoOnSearchBar").toggleClass("hide");
    $("#backButtonFromSearchResult").toggleClass("hide");
  });
}

export {
  searchFileSystemItems
}
