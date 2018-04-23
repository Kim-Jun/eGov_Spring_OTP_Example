<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>OTP Sample</title>
</head>
<body>
당신의 키는 → ${encodedKey } 입니다. <br>
당신의 바코드 주소는 → ${url } 입니다. <br><br>
 
<form action="<%=request.getContextPath() %>/egovAuthPro.do" method="get">
    <img src="${url }" id="img"/><br><br>
    code : <input type="text" name="user_code"><br>
    <input type="hidden" name="encodedKey" value="${encodedKey }" readonly="readonly"><br>
    <input type="submit" value="전송!">
</form>
</body>
</html>