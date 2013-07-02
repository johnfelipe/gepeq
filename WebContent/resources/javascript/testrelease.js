/* ©UNED */
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

function addAdminGroupStart() {
	readAvailableAdminGroupsHidden();
	readAdminGroupsToAddHidden();
}

function acceptAddAdminGroupsStart() {
	readAdminGroupsToAddHidden();
}

function readAvailableAdminGroupsHidden() {
	if (getJQueryElementById('adminGroupsDialogForm:availableAdminGroupsHidden').length!=0 &&
		getJQueryElementById('adminGroupsDialogForm:adminGroupsPickList_source').length!=0) {
		
		setInputFieldValue('adminGroupsDialogForm:availableAdminGroupsHidden',getInputFieldValue(
			'adminGroupsDialogForm:adminGroupsPickList_source').replace(/\"/g,''));
	}
}

function readAdminGroupsToAddHidden() {
	if (getJQueryElementById('adminGroupsDialogForm:adminGroupsToAddHidden').length!=0 &&
		getJQueryElementById('adminGroupsDialogForm:adminGroupsPickList_target').length!=0) {
		
		setInputFieldValue('adminGroupsDialogForm:adminGroupsToAddHidden',getInputFieldValue(
			'adminGroupsDialogForm:adminGroupsPickList_target').replace(/\"/g,''));
	}
}

function changeEvaluatorFilterTypeStart() {
	readEvaluatorFilterUsersIdsHidden();
}

function changeEvaluatorFilterSubtypeStart() {
	readEvaluatorFilterUsersIdsHidden();
}

function applyEvaluatorFilterUsersFilterStart() {
	readEvaluatorFilterUsersIdsHidden();
}

function acceptAddEvaluatorStart() {
	readEvaluatorFilterUsersIdsHidden();
}

function readEvaluatorFilterUsersIdsHidden() {
	if (getJQueryElementById('assessementAddressDialogForm:evaluatorFilterUsersIdsHidden').length!=0 &&
		getJQueryElementById('assessementAddressDialogForm:evaluatorFilterUsersList_target').length!=0) {
		
		setInputFieldValue('assessementAddressDialogForm:evaluatorFilterUsersIdsHidden',getInputFieldValue(
			'assessementAddressDialogForm:evaluatorFilterUsersList_target').replace(/\"/g,''));
	}
}

function changeSupportContactFilterTypeStart() {
	readSupportContactFilterUsersIdsHidden();
}

function changeSupportContactFilterSubtypeStart() {
	readSupportContactFilterUsersIdsHidden();
}

function applySupportContactFilterUsersFilterStart() {
	readSupportContactFilterUsersIdsHidden();
}

function acceptAddSupportContactStart() {
	readSupportContactFilterUsersIdsHidden();
}

function readSupportContactFilterUsersIdsHidden() {
	if (getJQueryElementById('techSupportAddressDialogForm:supportContactFilterUsersIdsHidden').length!=0 &&
		getJQueryElementById(
		'techSupportAddressDialogForm:supportContactFilterUsersList_target').length!=0) {
		
		setInputFieldValue('techSupportAddressDialogForm:supportContactFilterUsersIdsHidden',getInputFieldValue(
			'techSupportAddressDialogForm:supportContactFilterUsersList_target').replace(/\"/g,''));
	}
}
