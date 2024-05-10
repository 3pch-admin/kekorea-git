<%@page import="e3ps.admin.configSheetCode.ConfigSheetCode"%>
<%@page import="e3ps.korea.configSheet.ConfigSheetVariable"%>
<%@page import="wt.content.ContentServerHelper"%>
<%@page import="wt.content.ApplicationData"%>
<%@page import="wt.content.ContentRoleType"%>
<%@page import="wt.content.ContentHelper"%>
<%@page import="wt.part.WTPart"%>
<%@page import="e3ps.common.util.QuerySpecUtils"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="wt.query.ClassAttribute"%>
<%@page import="wt.query.OrderBy"%>
<%@page import="e3ps.project.task.Task"%>
<%@page import="e3ps.admin.commonCode.service.CommonCodeHelper"%>
<%@page import="e3ps.project.Project"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="wt.query.SearchCondition"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="e3ps.loader.service.LoaderHelper"%>
<%@page import="e3ps.project.output.service.OutputHelper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%


out.println("<br>Start!!");
try{
	
	QuerySpec qs = new QuerySpec();
	
	int idx = qs.appendClassList(ConfigSheetVariable.class, true);
	QueryResult qr = PersistenceHelper.manager.find(qs);
	
	while(qr.hasMoreElements()){
		Object[] o = (Object[])qr.nextElement();
		ConfigSheetVariable cv = (ConfigSheetVariable)o[0];
		
		ConfigSheetCode cc =  cv.getCategory();
		ConfigSheetCode ii = cv.getItem();
		out.println("<br>###=="+cc+"=="+ii);
		
		 if( cc != null ){
			cv.setCategory_code(cc.getCode());
			cv.setCategory_name(cc.getName());
			 
		 }
		 
		 if( ii != null){
				cv.setItem_code(ii.getCode());
				cv.setItem_name(ii.getName());
			 }
		 PersistenceHelper.manager.modify(cv);
	}
	
	
}catch(Exception e){
	e.printStackTrace();
}


	out.println("<br>END!!");
%>

<%

System.out.println(query);

%>