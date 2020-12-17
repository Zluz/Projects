// Add custom text before the selection when copying text
//
// USAGE: slectionMod.init('custom text');
//

var selectionMod = (function() {

  var textBefore = '';

  var getSelection = function () {
    if (window.getSelection) {
      return window.getSelection();
    }
    return null;
  };

  var getSelectionNode = function (initialSelectionRange) {
    var selectionContent = initialSelectionRange.cloneContents();
    var tempNode = document.createElement("div");

    tempNode.appendChild(selectionContent);

    var html = tempNode.innerHTML;

    if (!html) {
      return null;
    }

    // Create the selection node
    var selectionNode = document.createElement("div");

    // Hide the selection node
    selectionNode.style.position = 'absolute';
    selectionNode.style.left = '-99999px';

    // Add modified selection content to the selection node
    html = textBefore + ': ' + html;
    selectionNode.innerHTML = html;

    return selectionNode
  };

  var restoreInitialSelection = function (selection, initialSelectionRange) {
    if (selection && initialSelectionRange) {
      selection.removeAllRanges();
      selection.addRange(initialSelectionRange);
    }
  };

  var cleanup = function (selection, containerNode, selectionNode, initialSelectionRange) {
    if (containerNode && selectionNode) {
      containerNode.removeChild(selectionNode);
    }
    restoreInitialSelection(selection, initialSelectionRange);
  };

  var onCopy = function () {
    var selection = getSelection();

    if (selection && selection.rangeCount === 1) {

      var initialSelectionRange = selection.getRangeAt(0);
      var selectionNode = getSelectionNode(initialSelectionRange);

      if (selectionNode) {

        // Get first ancestor element Node
        var containerNode = initialSelectionRange.commonAncestorContainer;

        while (containerNode.nodeType !== Node.ELEMENT_NODE) {
          containerNode = containerNode.parentNode;
        }

        // Add selection node to the DOM
        containerNode.appendChild(selectionNode);

        // Change selection to new node
        selection.selectAllChildren(selectionNode);

        window.setTimeout(
          function() {
            cleanup(selection, containerNode, selectionNode, initialSelectionRange)
          },
          10
        );

      }

    }

  };

  var addListeners = function () {
    $('body').on('copy', onCopy);
  };

  var init = function (text) {
    textBefore = text;

    if (textBefore) {
      addListeners();
    }
  };

  return {
    init: init
  }

})();
