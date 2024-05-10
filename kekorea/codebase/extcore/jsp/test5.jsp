<%@page import="wt.fc.QueryResult"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="e3ps.common.util.QuerySpecUtils"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="e3ps.workspace.ApprovalMaster"%>
<%@page import="e3ps.loader.numberRule.NumberRuleLoader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<%


try{
	
	String before = "2023-09-01";
	String end = "2023-09-28";
	

	QuerySpec qs = new QuerySpec();
	
	int idx = qs.appendClassList(ApprovalMaster.class, true);
	
	QuerySpecUtils.toTimeGreaterAndLess(qs, idx, ApprovalMaster.class, ApprovalMaster.CREATE_TIMESTAMP, before, end);
	
	QueryResult qr = PersistenceHelper.manager.find(qs);
	
	while(qr.hasMoreElements()){
		Object[] o = (Object[])qr.nextElement();
		
		ApprovalMaster am = (ApprovalMaster)
		
	}
	
	
	
	
	
}catch(Exception e){
	e.printStackTrace();
}


%>

