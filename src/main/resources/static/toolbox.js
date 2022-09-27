
function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}

function chooseTargetUser(event) {
    for(let cba of document.getElementsByClassName("choose-button-area-selected")) {
        cba.className = "choose-button-area"
    }
    event.currentTarget.className = "choose-button-area-selected"
    let title = event.currentTarget.title
    document.getElementById("targetUser").setAttribute("value", title)
    
    if(title === "other") {
        document.getElementById("otherTargetUserArea").style.display = "flex"
    } else {
        document.getElementById("otherTargetUserArea").style.display = "none"
    }

    updateRequestButtonState();
}

function toggleProductSelection(event) {
    let inputField = event.currentTarget.getElementsByTagName("input")[0];
    let productId = inputField.getAttribute("id");
    let subCategories = document.getElementById("category_"+productId);
    
    if(isBlank(inputField.getAttribute("value"))) {
        inputField.setAttribute("value", productId);
        event.currentTarget.className = "choose-tool-area-selected"
        if(subCategories !== null) {
            subCategories.style.removeProperty("display");
        }
    } else {
        inputField.removeAttribute("value");
        event.currentTarget.className = "choose-tool-area"
        if(subCategories !== null) {
            subCategories.style.display = "none";
        }
    }
    
    updateRequestButtonState();
    toggleProductCategoryAreaVisibility();
}

function toogleCategorySelection(event) {
    let inputField = event.currentTarget.getElementsByTagName("input")[0];
    let productCategoryCombinedKey = inputField.getAttribute("id");
    let categoryInputField = event.currentTarget.parentNode.getElementsByTagName("input")[0];

    for(let mode of event.currentTarget.parentNode.getElementsByClassName("sub-category-access-mode-selected")) {
        if(mode !== event.currentTarget) {
            mode.className = "sub-category-access-mode"
            mode.getElementsByTagName("input")[0].removeAttribute("value");
        }
    }
    
    if(isBlank(inputField.getAttribute("value"))) {
        inputField.setAttribute("value", productCategoryCombinedKey);
        categoryInputField.setAttribute("value", productCategoryCombinedKey);
        event.currentTarget.className = "sub-category-access-mode-selected"
    } else {
        inputField.removeAttribute("value");
        categoryInputField.removeAttribute("value");
        event.currentTarget.className = "sub-category-access-mode"
    }
    
    updateRequestButtonState();
}

function toggleProductCategoryAreaVisibility() {
    let subCategoryArea = document.getElementById("sub-category-stuff");
    let productSelected = (document.getElementsByClassName("choose-tool-area-selected").length > 0);
    
    if(productSelected <= 0) {
        subCategoryArea.style.display = "none";
    } else {
        subCategoryArea.style.removeProperty("display");
    }
}

function isAnyCommentFieldNotBlank() {
    for(let ti of $(".sub-category:visible input[type='text']")) { 
        if(!isBlank(ti.value)) {
            return true;
        } 
    }
    return false;
}

function isAnySubcategorySelected() {
    let categorySelected = (document.getElementsByClassName("sub-category-access-mode-selected").length > 0)
    let commentEntered = isAnyCommentFieldNotBlank();
    
    return (categorySelected || commentEntered);
}

function updateRequestButtonState() {
    let button = document.getElementById("request-button");
    let userSelected = isUserSelected();
    let productSelected = (document.getElementsByClassName("choose-tool-area-selected").length > 0);
    let categorySelected = isAnySubcategorySelected();
    
    if(userSelected && productSelected && categorySelected) {
        button.className = "request-button";
    } else {
        button.className = "request-button-disabled";
    }
}
function sendRequestForm(event) {
    let button = document.getElementById("request-button");
    if(button.className === "request-button-disabled") {
        event.preventDefault();
    }
}

function isUserSelected() {
    if(document.getElementsByClassName("choose-button-area-selected").length <= 0) {
        return false;
    }
    let userType = document.getElementById("targetUser").getAttribute("value");
    if(userType === "myself") {
        return true;
    }
    
    let targetUserMail = document.getElementById("targetUserMail").value;
    return isValidTargetUserMail(targetUserMail);
}

function validateTargetUserMail(event) {
    let targetMail = event.currentTarget.value;
    if(isValidTargetUserMail(targetMail)) {
        event.currentTarget.style.removeProperty("background-color");
    } else {
        event.currentTarget.style.setProperty("background-color", "#ffb199");
    }
    updateRequestButtonState();
}
function isValidTargetUserMail(targetMail) {
    return (/.+@yourcompany\.localdomain$/.test(targetMail) ||
        /.+@extern\.yourcompany\.localdomain$/.test(targetMail)) ;
}

function categoryCommentChanged(event) {
    updateRequestButtonState();
}

function toggleUserDetails(event) {
    let details = event.currentTarget.parentNode.getElementsByClassName("user-details")[0];
    
    // only allow one details-area to be visible
    for(let ud of document.getElementsByClassName("user-details")) {
        if(ud !== details && ud.style.visibility !== 'hidden') {
            ud.style.visibility = 'hidden';
        }
    }
    
    if(details.style.visibility !== 'visible') {
        details.style.visibility = 'visible';
    } else {
        details.style.visibility = 'hidden';
    }
}

function toggleMenu(event) {
    $("#menu-entries").toggle("fold", {}, 500);
}
