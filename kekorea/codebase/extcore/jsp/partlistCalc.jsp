<%@page import="e3ps.bom.partlist.PartListMaster"%>
<%@page import="e3ps.bom.partlist.PartListMasterProjectLink"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.project.Project"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String oid = "e3ps.project.Project:147936530";

Project pjt = (Project) CommonUtils.getObject(oid);

QueryResult qr = PersistenceHelper.manager.navigate(pjt, "master", PartListMasterProjectLink.class);
double mT = 0D;
double eT = 0D;
while (qr.hasMoreElements()) {
	PartListMaster p = (PartListMaster) qr.nextElement();
	String engType = p.getEngType();
	out.println(engType);
	if ("기계_1차_수배".equals(engType) || "기계_2차_수배".equals(engType)){
		out.println("ASD");
		mT += p.getTotalPrice();
	} else if ("전기_1차_수배".equals(engType) || "전기_2차_수배".equals(engType)){
		eT += p.getTotalPrice();
	}
}
pjt.setOutputMachinePrice(mT);
pjt.setOutputElecPrice(eT);
PersistenceHelper.manager.modify(pjt);
out.println("계산 종료..");
%>