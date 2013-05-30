/* ©UNED */
function feedbackDisplaySummaryOnChangeStart() {
	if (window.feedbackSummaryPrevious && !isChecked(feedbackDisplaySummary))
		saveEditor(feedbackSummaryPrevious);
}

function feedbackDisplaySummaryOnChangeComplete() {
	if (window.feedbackSummaryPrevious && isChecked(feedbackDisplaySummary))
		refreshEditor(feedbackSummaryPrevious);
}

function feedbackDisplayScoresOnChangeStart() {
	if (window.feedbackScoresPrevious && !isChecked(feedbackDisplayScores))
		saveEditor(feedbackScoresPrevious);
}

function feedbackDisplayScoresOnChangeComplete() {
	if (window.feedbackScoresPrevious && isChecked(feedbackDisplayScores))
		refreshEditor(feedbackScoresPrevious);
}

function feedbackAccordionOnTabChangeStart() {
	if (feedbackAccordion.cfg.active==0) {
		if (window.feedbackScoresPrevious)
			saveEditors([feedbackScoresPrevious,feedbackAdvancedPrevious,feedbackAdvancedNext]);
		else
			saveEditors([feedbackAdvancedPrevious,feedbackAdvancedNext]);
	} else if (feedbackAccordion.cfg.active==1) {
		if (window.feedbackSummaryPrevious)
			saveEditors([feedbackSummaryPrevious,feedbackAdvancedPrevious,feedbackAdvancedNext]);
		else
			saveEditors([feedbackAdvancedPrevious,feedbackAdvancedNext]);
	} else if (feedbackAccordion.cfg.active==2) {
		if (window.feedbackSummaryPrevious) {
			if (window.feedbackScoresPrevious)
				saveEditors([feedbackScoresPrevious,feedbackSummaryPrevious]);
			else
				saveEditor(feedbackSummaryPrevious);
		} else if (window.feedbackScoresPrevious)
			saveEditor(feedbackScoresPrevious);
	}
}

function feedbackAccordionOnTabChangeComplete() {
	if (feedbackAccordion.cfg.active==0 && window.feedbackSummaryPrevious)
		refreshEditor(feedbackSummaryPrevious);
	else if (feedbackAccordion.cfg.active==1 && window.feedbackScoresPrevious)
		refreshEditor(feedbackScoresPrevious);
	else if (feedbackAccordion.cfg.active==2)
		refreshEditors([feedbackAdvancedPrevious,feedbackAdvancedNext]);
}

function testFormTabsOnTabChangeStart() {
	if (testFormTabs.cfg.selected==1) {
		if (window.preliminarySummaryText)
			saveEditor(preliminarySummaryText);
	} else {
		if (window.preliminarySummaryText)
			saveEditors([presentationText,preliminarySummaryText]);
		else
			saveEditor(presentationText);
	} 
	feedbackAccordionOnTabChangeStart();
}

function testFormTabsOnTabChangeComplete() {
	if (testFormTabs.cfg.selected==1) {
		refreshEditor(presentationText);
	} else if (testFormTabs.cfg.selected==3 || testFormTabs.cfg.selected==4) {
		if (window.preliminarySummaryText)
			refreshEditor(preliminarySummaryText);
		feedbackAccordionOnTabChangeComplete();
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
