<%@page import="java.util.Map"%>
<%@page import="java.util.Vector"%>
<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.common.util.ContentUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
// 주 첨부 파일은 무조건 있어야 한다.
String oid = request.getParameter("oid");
Map<String, Object> thumbnail = ContentUtils.getThumbnail(oid);
if (thumbnail != null) {
%>
<div>
	<a href="<%=thumbnail.get("url")%>">
		<span style="position: relative; bottom: 2px;"><%=thumbnail.get("name")%></span>
		<img src="<%=thumbnail.get("fileIcon")%>" style="position: relative; top: 1px;">
	</a>
</div>
<%
} else {
%>
<font color="red">
	<b>등록된 병합PDF가 없습니다.</b>
</font>
<%
}
%>