<%@page import="wt.fc.QueryResult"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="wt.query.SearchCondition"%>
<%@page import="wt.query.ClassAttribute"%>
<%@page import="wt.query.QuerySpec"%>
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
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
Timestamp ss = new Timestamp(sdf.parse("2023-06-01").getTime());
QuerySpec query = new QuerySpec();
int idx = query.appendClassList(Project.class, true);

SearchCondition sc = new SearchCondition(Project.class, Project.CREATE_TIMESTAMP, SearchCondition.GREATER_THAN_OR_EQUAL,
		ss);
query.appendWhere(sc, new int[] { idx });
QueryResult qr = PersistenceHelper.manager.find(query);
int a=0;
while (qr.hasMoreElements()) {
	Object[] obj = (Object[]) qr.nextElement();
	Project project = (Project) obj[0];
	// String oid = "e3ps.project.Project:145667379";
	// String reference = "e3ps.project.template.Template:95023513";
	// Project project = (Project) CommonUtils.getObject(oid);
	Template template = project.getTemplate();
	System.out.println("진행중="+a);
	a++;
	if (template != null) {

		Timestamp start = project.getPlanStartDate();
		project.setPlanStartDate(start);

		Calendar eCa = Calendar.getInstance();
		eCa.setTimeInMillis(start.getTime());
		eCa.add(Calendar.DATE, template.getDuration());

		Timestamp end = new Timestamp(eCa.getTime().getTime());
		project.setPlanEndDate(end);
		project.setTemplate(template);

		project.setDuration(DateUtils.getDuration(start, end));

		project = (Project) PersistenceHelper.manager.modify(project);

		ArrayList<Task> list = TemplateHelper.manager.recurciveTask(template);
		ArrayList<Task> plist = ProjectHelper.manager.recurciveTask(project);
		for (int i = 0; i < list.size(); i++) {
	Task tTask = (Task) list.get(i);

	for (int j = 0; j < plist.size(); j++) {
		Task pTask = (Task) plist.get(j);

		if (tTask.getName().equals(pTask.getName())) {
// 			out.println("변경전 이름 = " + pTask.getName() + " / " + pTask.getPlanStartDate() + " / "
// 					+ pTask.getPlanEndDate() + "<br>");

			Timestamp tStart = pTask.getPlanStartDate();
			Calendar tCa = Calendar.getInstance();
			tCa.setTimeInMillis(tStart.getTime());
			tCa.add(Calendar.DATE, tTask.getDuration());

			Timestamp pEnd = new Timestamp(tCa.getTime().getTime());
			pTask.setDuration(tTask.getDuration());
			pTask.setPlanEndDate(pEnd);

			PersistenceHelper.manager.modify(pTask);
// 			out.println("<br>");
// 			out.println("변경후 이름 = " + pTask.getName() + " / " + pTask.getPlanStartDate() + " / "
// 					+ pTask.getPlanEndDate() + "<br>");
// 			out.println("<br>");
		}
	}
		}
	}
}
out.println("계산 종료!");
%>