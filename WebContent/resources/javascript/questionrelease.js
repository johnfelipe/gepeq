function addUserGroupStart() {
	readAvailableUserGroupsHidden();
	readUserGroupsToAddHidden();
}

function acceptAddUserGroupsStart() {
	readUserGroupsToAddHidden();
}

function readAvailableUserGroupsHidden() {
	if (getJQueryElementById('userGroupsDialogForm:availableUserGroupsHidden').length!=0 &&
		getJQueryElementById('userGroupsDialogForm:userGroupsPickList_source').length!=0) {
		
		setInputFieldValue('userGroupsDialogForm:availableUserGroupsHidden',getInputFieldValue(
			'userGroupsDialogForm:userGroupsPickList_source').replace(/\"/g,''));
	}
}

function readUserGroupsToAddHidden() {
	if (getJQueryElementById('userGroupsDialogForm:userGroupsToAddHidden').length!=0 &&
		getJQueryElementById('userGroupsDialogForm:userGroupsPickList_target').length!=0) {
		
		setInputFieldValue('userGroupsDialogForm:userGroupsToAddHidden',getInputFieldValue(
			'userGroupsDialogForm:userGroupsPickList_target').replace(/\"/g,''));
	}
}
