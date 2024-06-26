<%@page import="e3ps.erp.ErpConnectionPool"%>
<%@page import="org.apache.commons.dbcp2.BasicDataSource"%>
<%@page import="e3ps.project.service.ProjectHelper"%>
<%@page import="e3ps.project.template.service.TemplateHelper"%>
<%@page import="e3ps.project.task.Task"%>
<%@page import="java.util.ArrayList"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="java.util.Calendar"%>
<%@page import="e3ps.common.util.DateUtils"%>
<%@page import="e3ps.project.Project"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.project.template.Template"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
String URL = "jdbc:sqlserver://192.168.1.61:1433;databasename=KEK";
String USERNAME = "plm_e3ps";
String PASSWORD = "proe2015!";
int MAX_TOTAL = 100; // 최대 생성 가능한 Connection 수
int MAX_IDLE = 50; // 최대 유휴 Connection 수
int MIN_IDLE = 10; // 최소 유휴 Connection 수

BasicDataSource dataSource = new BasicDataSource();
dataSource.setDriverClassName(DRIVER);
dataSource.setUrl(URL);
dataSource.setUsername(USERNAME);
dataSource.setPassword(PASSWORD);
dataSource.setMaxTotal(MAX_TOTAL);
dataSource.setMaxIdle(MAX_IDLE);
dataSource.setMinIdle(MIN_IDLE);
out.println(dataSource.getConnection());

// String oid = "e3ps.project.Project:145667379";
// String reference = "e3ps.project.template.Template:95023513";

// Project project = (Project) CommonUtils.getObject(oid);
// Template template = (Template) CommonUtils.getObject(reference);

// Timestamp start = project.getPlanStartDate();
// project.setPlanStartDate(start);

// Calendar eCa = Calendar.getInstance();
// eCa.setTimeInMillis(start.getTime());
// eCa.add(Calendar.DATE, template.getDuration());

// Timestamp end = new Timestamp(eCa.getTime().getTime());
// project.setPlanEndDate(end);
// project.setTemplate(template);

// project.setDuration(DateUtils.getDuration(start, end));

// project = (Project) PersistenceHelper.manager.modify(project);

// ArrayList<Task> list = TemplateHelper.manager.recurciveTask(template);
// ArrayList<Task> plist = ProjectHelper.manager.recurciveTask(project);
// for (int i = 0; i < list.size(); i++) {
// 	Task tTask = (Task) list.get(i);

// 	for (int j = 0; j < plist.size(); j++) {
// 		Task pTask = (Task) plist.get(j);

// 		if (tTask.getName().equals(pTask.getName())) {
// 	out.println("변경전 이름 = " + pTask.getName() + " / " + pTask.getPlanStartDate() + " / "
// 			+ pTask.getPlanEndDate() + "<br>");

// 	Timestamp tStart = pTask.getPlanStartDate();
// 	Calendar tCa = Calendar.getInstance();
// 	tCa.setTimeInMillis(tStart.getTime());
// 	tCa.add(Calendar.DATE, tTask.getDuration());

// 	Timestamp pEnd = new Timestamp(tCa.getTime().getTime());
// 	pTask.setDuration(tTask.getDuration());
// 	pTask.setPlanEndDate(pEnd);

// 	PersistenceHelper.manager.modify(pTask);
// 	out.println("<br>");
// 	out.println("변경후 이름 = " + pTask.getName() + " / " + pTask.getPlanStartDate() + " / "
// 			+ pTask.getPlanEndDate() + "<br>");
// 	out.println("<br>");
// 		}

// 	}
// }
out.println("계산 종료!");
%>