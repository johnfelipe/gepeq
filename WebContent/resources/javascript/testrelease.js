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

function addEvaluatorGroupStart() {
	readAvailableEvaluatorFilterGroupsHidden();
	readEvaluatorFilterGroupsHidden();
}

function acceptAddEvaluatorStart() {
	readEvaluatorFilterUsersIdsHidden();
	readEvaluatorFilterGroupsHidden();
}

function readEvaluatorFilterUsersIdsHidden() {
	if (getJQueryElementById('assessementAddressDialogForm:evaluatorFilterUsersIdsHidden').length!=0 &&
		getJQueryElementById('assessementAddressDialogForm:evaluatorFilterUsersList_target').length!=0) {
		
		setInputFieldValue('assessementAddressDialogForm:evaluatorFilterUsersIdsHidden',getInputFieldValue(
			'assessementAddressDialogForm:evaluatorFilterUsersList_target').replace(/\"/g,''));
	}
}

function readAvailableEvaluatorFilterGroupsHidden() {
	if (getJQueryElementById('assessementAddressDialogForm:availableEvaluatorFilterGroupsHidden').length!=0 &&
		getJQueryElementById('assessementAddressDialogForm:evaluatorFilterGroupsList_source').length!=0) {
		
		setInputFieldValue('assessementAddressDialogForm:availableEvaluatorFilterGroupsHidden',getInputFieldValue(
			'assessementAddressDialogForm:evaluatorFilterGroupsList_source').replace(/\"/g,''));
	}
}

function readEvaluatorFilterGroupsHidden() {
	if (getJQueryElementById('assessementAddressDialogForm:evaluatorFilterGroupsHidden').length!=0 &&
		getJQueryElementById('assessementAddressDialogForm:evaluatorFilterGroupsList_target').length!=0) {
		
		setInputFieldValue('assessementAddressDialogForm:evaluatorFilterGroupsHidden',getInputFieldValue(
			'assessementAddressDialogForm:evaluatorFilterGroupsList_target').replace(/\"/g,''));
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

function addSupportContactGroupStart() {
	readAvailableSupportContactFilterGroupsHidden();
	readSupportContactFilterGroupsHidden();
}

function acceptAddSupportContactStart() {
	readSupportContactFilterUsersIdsHidden();
	readSupportContactFilterGroupsHidden();
}

function readSupportContactFilterUsersIdsHidden() {
	if (getJQueryElementById('techSupportAddressDialogForm:supportContactFilterUsersIdsHidden').length!=0 &&
		getJQueryElementById(
		'techSupportAddressDialogForm:supportContactFilterUsersList_target').length!=0) {
		
		setInputFieldValue('techSupportAddressDialogForm:supportContactFilterUsersIdsHidden',getInputFieldValue(
			'techSupportAddressDialogForm:supportContactFilterUsersList_target').replace(/\"/g,''));
	}
}

function readAvailableSupportContactFilterGroupsHidden() {
	if (getJQueryElementById('techSupportAddressDialogForm:availableSupportContactFilterGroupsHidden').length!=0 &&
		getJQueryElementById('techSupportAddressDialogForm:supportContactFilterGroupsList_source').length!=0) {
		
		setInputFieldValue('techSupportAddressDialogForm:availableSupportContactFilterGroupsHidden',getInputFieldValue(
			'techSupportAddressDialogForm:supportContactFilterGroupsList_source').replace(/\"/g,''));
	}
}

function readSupportContactFilterGroupsHidden() {
	if (getJQueryElementById('techSupportAddressDialogForm:supportContactFilterGroupsHidden').length!=0 &&
		getJQueryElementById('techSupportAddressDialogForm:supportContactFilterGroupsList_target').length!=0) {
		
		setInputFieldValue('techSupportAddressDialogForm:supportContactFilterGroupsHidden',getInputFieldValue(
			'techSupportAddressDialogForm:supportContactFilterGroupsList_target').replace(/\"/g,''));
	}
}
