<%@page import="e3ps.erp.service.ErpHelper"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="wt.doc.WTDocument"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String oid = "wt.doc.WTDocument:143903687";
	WTDocument doc = (WTDocument) CommonUtils.getObject(oid);
	ErpHelper.manager.sendToErp(doc);
%>