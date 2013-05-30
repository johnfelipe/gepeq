<%@page isErrorPage="true" %>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<!-- ©UNED -->
<html>
<head></head>
    <body>
        <jsp:forward page="/pages/error.jsf">
            <jsp:param name="errorCode" value="PAGE_NON_EXIST_ERROR" />
            <jsp:param name="plainMessage" value="The page you are trying to access does not exist." />
        </jsp:forward>
    </body>
</html>