<%@page import="e3ps.project.service.ProjectHelper"%>
<%@page import="e3ps.common.util.QuerySpecUtils"%>
<%@page import="e3ps.project.Project"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="e3ps.project.task.Task"%>
<%@page import="wt.query.QuerySpec"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	QuerySpec query = new QuerySpec();
	int idx = query.appendClassList(Project.class, true);
	QuerySpecUtils.toTimeGreaterThan(query, idx, Project.class, Project.CREATE_TIMESTAMP, "2023-07-01");
	QueryResult qr = PersistenceHelper.manager.find(query);
	System.out.println("qr=" + qr.size());
	while (qr.hasMoreElements()) {
		Object[] obj = (Object[]) qr.nextElement();
		Project p = (Project) obj[0];
		ProjectHelper.service.commit(p);
		System.out.println(p.getKekNumber() + " TIME = " +p.getCreateTimestamp());
	}
	System.out.println("END!");
%>