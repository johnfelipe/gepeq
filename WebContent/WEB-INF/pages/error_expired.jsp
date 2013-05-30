<%@page isErrorPage="true" %>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<!-- ©UNED -->
<html>
<head></head>
    <body>
        <jsp:forward page="/pages/error.jsf">
            <jsp:param name="errorCode" value="SESSION_EXPIRED_ERROR" />
            <jsp:param name="plainMessage" value="Your session has expired. You must login into the system again." />
        </jsp:forward>
    </body>
</html>