<%@page import="wt.org.OrganizationServicesHelper"%>
<%@page import="wt.org.WTUser"%>
<%@page import="java.util.Date"%>
<%@page import="wt.fc.Persistable"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="e3ps.workspace.ApprovalMaster"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.workspace.ApprovalLine"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="e3ps.workspace.service.WorkspaceHelper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
System.out.println("START!");

String oid = "e3ps.workspace.ApprovalLine:148729325";
Timestamp completeTime = new Timestamp(new Date().getTime());
ApprovalLine line = (ApprovalLine) CommonUtils.getObject(oid);

ApprovalMaster master = line.getMaster();
System.out.println(master);
Persistable persist = master.getPersist();
System.out.println(persist);
line.setDescription("");
line.setCompleteTime(completeTime);
line.setState(WorkspaceHelper.STATE_AGREE_COMPLETE);
WTUser user = OrganizationServicesHelper.manager.getUser("20120413");
line.setCompleteUserID(user.getName());
System.out.println("???");
line = (ApprovalLine) PersistenceHelper.manager.modify(line);
System.out.println("???");
System.out.println(line);

// boolean isEndAgree = WorkspaceHelper.manager.isEndAgree(master);
// System.out.println(isEndAgree);
// if (isEndAgree) {


	System.out.println("???");
	ArrayList<ApprovalLine> approvalLines = WorkspaceHelper.manager.getApprovalLines(master);
	System.out.println("?!!!!");
	for (ApprovalLine approvalLine : approvalLines) {
		int sort = approvalLine.getSort();
		approvalLine.setSort(sort - 1);
		approvalLine = (ApprovalLine) PersistenceHelper.manager.modify(approvalLine);
		approvalLine = (ApprovalLine) PersistenceHelper.manager.refresh(approvalLine);

		if (approvalLine.getSort() == 0) {
			approvalLine.setStartTime(completeTime);
			approvalLine.setState(WorkspaceHelper.STATE_APPROVAL_APPROVING);
			PersistenceHelper.manager.modify(approvalLine);

			// 마스터 상태값도 변경
			master.setState(WorkspaceHelper.STATE_MASTER_APPROVAL_APPROVING);
			PersistenceHelper.manager.modify(master);
		}
	}
// }
out.println("END!");
%>