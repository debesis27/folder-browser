import { fetchAndShowFileBrowser } from "./fileBrowser.js";
import { bindFileBrowserEvents } from "./fileBrowser.js";
import { bindMiniFileBrowserEvents } from "./miniFileBrowser.js";
import { bindFileOperationEvents } from "./fileOperation.js";
import { bindSearchEvents } from "./search.js";

document.addEventListener("DOMContentLoaded", function () {
  fetchAndShowFileBrowser();
});

$(document).ready(function() {
  bindFileBrowserEvents();
  bindMiniFileBrowserEvents();
  bindFileOperationEvents();
  bindSearchEvents();
})
