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

function getFileImageElement(fileType) {
  let extensionImage = fileExtensionImageMap[fileType] == undefined ? "file-document" : fileExtensionImageMap[fileType];
  let imageURL = "/images/file-extensions/" + extensionImage + ".svg";
  let image = $("<img>").attr("src", imageURL);

  return image;
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

function formatBytes(bytes, decimals = 2) {
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

function findFolderByPath(path, rootFolder) {
  let folder = rootFolder;

  for (let i = 1; i < path.length; i++) {
    if(path[i] == "") continue;
    folder = folder.childFolders.find(childFolder => childFolder.parent.name == path[i]);
  }

  return folder;
}

export {
  getFileImageElement,
  debounce,
  formatBytes,
  findFolderByPath
}
