let breadcrumbs = [];

function showBreadcrumbs() {
  const breadcrumbsDiv = $("#breadcrumbs");

  breadcrumbsDiv.empty();

  breadcrumbs.forEach(function(breadcrumb, index) {
    let span = $("<span></span>").text(breadcrumb.find("h3").text());

    span.click(function() {
      removeBreadCrumbTill(breadcrumb.find("h3").text());
      showBreadcrumbs();
    })

    breadcrumbsDiv.append(span);

    if (index < breadcrumbs.length - 1) {
      breadcrumbsDiv.append("&nbsp;>&nbsp;");
    }
  })
}

function openFolder(element) {
  element = $(element);
  addBreadCrumb(element);
  showBreadcrumbs();
}

function addBreadCrumb(element) {
  breadcrumbs.push(element);

  breadcrumbs.forEach(element => {
    console.log(element.find("h3").text());
  });
  console.log("----");

  let folderName = element.find("h3").text();

  if (folderName == "root") {
    element.toggleClass("collapse");
    element.next(".subfolders-div").toggleClass("collapse");
    return;
  }

  let grandparent = element.parent();
  let greatgrandparent = grandparent.parent();

  // Collapsing all the other folders except the selected one
  greatgrandparent.toggleClass("collapse");
  greatgrandparent.parent().children(".subfolder-iter").toggleClass("collapse");

  // Showing all the subfolders
  grandparent.children().each(function () {
    $(this).toggleClass("collapse");
  });

  adjustSubfolderPosition(element);
}

function adjustSubfolderPosition(element) {
  const subfoldersDiv = element.next(".subfolders-div");
  const offsetTop = element.offset().top;
  const offsetLeft = element.offset().left;

  subfoldersDiv.css({
    top: `${offsetTop}px`,
    left: `${offsetLeft}px`,
  });
}

function removeBreadCrumbTill(elementName) {
  for (let i = breadcrumbs.length - 1; i >= 0; i--) {
    if (breadcrumbs[i].find("h3").text() == elementName) {
      break;
    }
    removeBreadCrumb();
  }
}

function removeBreadCrumb() {
  if (breadcrumbs.length == 0) {
    return;
  }

  element = breadcrumbs.pop();

  breadcrumbs.forEach(element => {
    console.log(element.find("h3").text());
  });
  console.log("----");

  // root directory case
  if (breadcrumbs.length == 0) {
    element.next(".subfolders-div").toggleClass("collapse");
    element.toggleClass("collapse");
    return;
  }

  let grandparent = element.parent();
  let greatgrandparent = grandparent.parent();

  // Collapsing all the subfolders
  grandparent.children().each(function () {
    $(this).toggleClass("collapse");
  });

  // Showing all the folders
  greatgrandparent.toggleClass("collapse");
  greatgrandparent.parent().children(".subfolder-iter").toggleClass("collapse");
}

function goBack() {
  removeBreadCrumb();
  showBreadcrumbs();
}

$(window).resize(function() {
  // Adjust the position of all open subfolders on window resize
  breadcrumbs.forEach(element => {
    adjustSubfolderPosition(element);
  });
});
