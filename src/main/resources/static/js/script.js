let breadcrumbs = [];

function openFolder(element) {
  element = $(element);
  addBreadCrumb(element);
}

function addBreadCrumb(element) {
  breadcrumbs.push(element);
  
  breadcrumbs.forEach(element => {
    console.log(element.find("h3").text());
  });
  console.log("----");

  let folderName = element.find("h3").text();

  if (folderName == "root") {
    element.parent().toggleClass("collapse");
    element.parent().next(".subfolders-div").toggleClass("collapse");

    return;
  }

  let grandparent = element.parent().parent();
  let greatgrandparent = grandparent.parent();

  //Collapsing all the other folders except the selected one
  greatgrandparent.toggleClass("collapse");
  greatgrandparent.parent().children(".subfolder-iter").toggleClass("collapse");

  //Showing all the subfolders
  grandparent.children().each(function () {
    $(this).toggleClass("collapse");
  })
}

function removeBreadCrumbTill(elementName) {
  for(let i = breadcrumbs.length - 1; i >= 0; i--) {
    if(breadcrumbs[i].find("h3").text() == elementName) {
      break;
    }
    removeBreadCrumb();
  }
}

function removeBreadCrumb() {
  if(breadcrumbs.length == 0) {
    return;
  }

  element = breadcrumbs.pop();

  breadcrumbs.forEach(element => {
    console.log(element.find("h3").text());
  });
  console.log("----");

  // root directory case
  if(breadcrumbs.length == 0) {
    element.parent().next(".subfolders-div").toggleClass("collapse");
    element.parent().toggleClass("collapse");

    return;
  }

  let grandparent = element.parent().parent();
  let greatgrandparent = grandparent.parent();

  //Collapsing all the subfolders
  grandparent.children().each(function() {
    $(this).toggleClass("collapse");
  })

  //Showing all the folders
  greatgrandparent.toggleClass("collapse");
  greatgrandparent.parent().children(".subfolder-iter").toggleClass("collapse");

}

function goBack() {
  removeBreadCrumb();
}
