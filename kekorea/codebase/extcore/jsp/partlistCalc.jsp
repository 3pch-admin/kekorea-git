<%@page import="e3ps.bom.partlist.PartListMaster"%>
<%@page import="e3ps.bom.partlist.PartListMasterProjectLink"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.project.Project"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String oid = "e3ps.project.Project:";

Project pjt = (Project) CommonUtils.getObject(oid);

QueryResult qr = PersistenceHelper.manager.navigate(pjt, "master", PartListMasterProjectLink.class, false);
double mT = 0D;
double eT = 0D;
while (qr.hasMoreElements()) {
	PartListMaster p = (PartListMaster) qr.nextElement();
	String engType = p.getEngType();
	if ("기계".equals(engType)) {
		mT = p.getTotalPrice();
	} else if ("전기".equals(engType)) {
		eT = p.getTotalPrice();
	}
}
pjt.setOutputMachinePrice(mT);
pjt.setOutputElecPrice(eT);
PersistenceHelper.manager.modify(ptj);
out.println("계산 종료..");
%>