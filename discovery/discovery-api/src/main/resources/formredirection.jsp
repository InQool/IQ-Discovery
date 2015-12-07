<%--
<?xml version="1.0" encoding="UTF-8"?>
<%@page contentType="text/html; charset=UTF-8" import="java.util.Map" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>OpenID HTML FORM Redirection</title>
    <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />    
</head>
<body onload="document.forms['openid-form-redirection'].submit();"> 
    <form name="openid-form-redirection" action="${targetURL}" method="post" accept-charset="utf-8">
        <c:forEach var="parameter" items="${message}">
	        <input type="hidden" name="${parameter.key}" value="${parameter.value}"/>
        </c:forEach>
        
        <noscript>
        	Probíhá přesměrování...<br/>
        	Pokud nedošlo k automatickému přesměrování pokračujte prosím kliknutím na tlačítko "Pokračovat"
        	<input type="submit" name="continueButton" value="Pokračovat"/>
		</noscript>
    </form>
</body>
</html>
--%>
