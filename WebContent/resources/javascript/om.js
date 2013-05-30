/* ©UNED */
// Abre el servlet de om para construir y visualizar una pregunta 
/**
 * View a question on an Openmark Test Navigator test environment to try it
 */
function viewQuestion(xhr, status, args) {
	var url = args.url;
	if (url != 'error') {
		var packageName = args.packageName;
	    window.open(url + packageName + '/?restart', '_blank');
	}
}
// Abre un test en OM 
/**
 * View a test on an Openmark Test Navigator test environment to try it
 */
function viewTest(xhr, status, args) {
	var url = args.url;
	if (url != 'error') {
		var testName = args.testName;
	    window.open(url + '!preview/' + testName + '/?restart', '_blank');
	}
}

/**
 * View a published question on the Openmark Test Navigator production environment
 */
function viewQuestionRelease(xhr, status, args) {
	var url = args.url;
	if (url != 'error') {
		var packageName = args.packageName;
		window.open(url + packageName + '/', '_blank');
	}
}

/**
 * View a published test on the Openmark Test Navigator production environment
 */
function viewTestRelease(xhr, status, args) {
	var url = args.url;
	if (url != 'error') {
		var testName = args.testName;
		var version = args.version;
		window.open(url + testName + version + '/', '_blank');
	}
}
