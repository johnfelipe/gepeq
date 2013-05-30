<%@page isErrorPage="true" %>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<!-- ©UNED -->
<html>
<head></head>
    <body>
        <jsp:forward page="/pages/error.jsf">
            <jsp:param name="errorCode" value="UNKNOWN_ERROR" />
            <jsp:param name="plainMessage" value="There was an unexpected error." />
        </jsp:forward>
    </body>
</html>