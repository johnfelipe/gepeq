/* ©UNED */
/**
 * Handler for onkeypress that prevents for submitting if the enter key is pressed.
 */
function preventEnterSubmit(event) {
	event = event || window.event;
	if (event.keyCode == 13) {
		// normalize event target, so it looks the same for all browsers
		if (!event.target) {
			event.target = event.srcElement;
		}
		
		// don't do anything if the element handles the enter key on its own
		if (event.target.nodeName == 'A') {
			return;
		}
		if (event.target.nodeName == 'INPUT') {
			if (event.target.type == 'button' || event.target.type == 'submit') {
				if (strEndsWith(event.target.id, 'focusKeeper')) {
					// inside some Richfaces component such as rich:listShuttle
				} else {
					return;
				}
			}
		}
		if (event.target.nodeName =='TEXTAREA') {
			return;
		}
		
		// swallow event
		if (event.preventDefault) {
			// Firefox
			event.stopPropagation();
			event.preventDefault();
		} else {
			// IE
			event.cancelBubble = true;
			event.returnValue = false;
		}
	}
}

function getJQueryElementById(id) {
	return $(document.getElementById(id));
}

function submitForm(formId) {
	getJQueryElementById(formId).submit();
}

function centerDialog(dialogId) {
	var dialog = getJQueryElementById(dialogId);
    var dialogWidth = dialog.width();
    var dialogHeight = dialog.height();
    dialog.offset(
    {
    	top: f_clientHeight() / 2 - dialogHeight / 2 + f_scrollTop(), 
    	left: f_clientWidth() / 2 - dialogWidth / 2 + f_scrollLeft() 
    });
}

function getInputFieldValue(inputFieldId) {
	return getJQueryElementById(inputFieldId).attr('value');
}

function setInputFieldValue(inputFieldId,value) {
	getJQueryElementById(inputFieldId).attr('value',value);
}

function checkInputFieldValue(inputFieldId,value) {
	return getInputFieldValue(inputFieldId) == value;
}

function checkSetInputFieldValue(inputFieldId,value) {
	var inputField = getJQueryElementById(inputFieldId);
	var ok = inputField.attr('value') == value;
	if (!ok) {
		inputField.attr('value',value);
	}
	return ok; 
}

function encodeInputFieldUTF8(inputFieldId) {
	var inputField = getJQueryElementById(inputFieldId);
	inputField.attr('value',encodeUTF8(inputField.attr('value')));
}

function decodeInputFieldUTF8(inputFieldId) {
	var inputField = getJQueryElementById(inputFieldId);
	inputField.attr('value',decodeUTF8(inputField.attr('value')));
}

function encodeUTF8(s) {
	for (var c, i = -1, l = (s = s.split("")).length, o = String.fromCharCode; ++i < l;
		s[i] = (c = s[i].charCodeAt(0)) >= 127 ? o(0xc0 | (c >>> 6)) + o(0x80 | (c & 0x3f)) : s[i]);
	return s.join("");
}

function decodeUTF8(s) {
	for (var a, b, i = -1, l = (s = s.split("")).length, o = String.fromCharCode, c = "charCodeAt"; ++i < l;
		((a = s[i][c](0)) & 0x80) && (s[i] = (a & 0xfc) == 0xc0 && ((b = s[i + 1][c](0)) & 0xc0) == 0x80 ?
		o(((a & 0x03) << 6) + (b & 0x3f)) : o(128), s[++i] = ""));
	return s.join("");
}

function checkDecodeInputFieldsUTF8(checkDecodeUTF8FieldId,inputFieldsIds) {
	if (checkDecodeUTF8FieldId==null || checkSetInputFieldValue(checkDecodeUTF8FieldId,'true')) {
		var inputFieldsIdsArray=null;
		if (inputFieldsIds.indexOf(",")==-1) {
			inputFieldsIdsArray=inputFieldsIds.split(" ");
		} else {
			inputFieldsIdsArray=inputFieldsIds.split(",");
		}
		for (var i=0;i<inputFieldsIdsArray.length;i++) {
			decodeInputFieldUTF8(inputFieldsIdsArray[i]);
		}
	}
}

function f_clientWidth() {
    return f_filterResults (
        window.innerWidth ? window.innerWidth : 0,
        document.documentElement ? document.documentElement.clientWidth : 0,
        document.body ? document.body.clientWidth : 0
    );
}

function f_clientHeight() {
    return f_filterResults (
        window.innerHeight ? window.innerHeight : 0,
        document.documentElement ? document.documentElement.clientHeight : 0,
        document.body ? document.body.clientHeight : 0
    );
}

function f_scrollLeft() {
    return f_filterResults (
        window.pageXOffset ? window.pageXOffset : 0,
        document.documentElement ? document.documentElement.scrollLeft : 0,
        document.body ? document.body.scrollLeft : 0
    );
}

function f_scrollTop() {
    return f_filterResults (
        window.pageYOffset ? window.pageYOffset : 0,
        document.documentElement ? document.documentElement.scrollTop : 0,
        document.body ? document.body.scrollTop : 0
    );
}

function f_filterResults(n_win, n_docel, n_body) {
    var n_result = n_win ? n_win : 0;
    if (n_docel && (!n_result || (n_result > n_docel)))
        n_result = n_docel;
    return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
}

function refreshEditor(editor) {
	setTimeout(function() {
		if (editor.editor) {
			editor.editor.refresh();
		} else {
			editor.init();
		}
	},300);
}

function refreshEditors(editors) {
	setTimeout(function() {
		for (var i = 0; i < editors.length; i++) {
			if (editors[i].editor) {
				editors[i].editor.refresh();
			}
			else {
				editors[i].init();
			}
		}
	},300);
}

function saveEditor(editor) {
	if (editor.editor) {
		editor.saveHTML();
	}
}

function saveEditors(editors) {
	for (var i = 0; i < editors.length; i++) {
		if (editors[i].editor) {
			editors[i].saveHTML();
		}
	}
}

function isChecked(checkbox) {
	return checkbox.input.attr('checked')==='checked';
}

function clearDate(calendar) {
	calendar.setDate(null);
}
