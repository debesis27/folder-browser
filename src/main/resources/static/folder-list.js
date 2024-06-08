function toggleFolder(element) {
  let $element = $(element);
  let $icon = $element.find('svg');

  // Toggle the collapse class
  $element.next('ul').toggleClass('show');

  // Toggle the folder icon
  if ($icon.hasClass('fa-folder')) {
      $icon.removeClass('fa-folder').addClass('fa-folder-open');
  } else {
      $icon.removeClass('fa-folder-open').addClass('fa-folder');
  }
}

console.log("Document is ready");
