<%@page import="java.util.Calendar"%>
<%@page import="e3ps.project.schedule.KEKScheduleJobs"%>
<%@page import="wt.query.OrderBy"%>
<%@page import="wt.query.ClassAttribute"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="wt.query.SearchCondition"%>
<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.project.task.Task"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="e3ps.common.util.DateUtils"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="e3ps.project.dto.ProjectGateState"%>
<%@page import="e3ps.project.service.ProjectHelper"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.project.Project"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String oid = "e3ps.project.Project:141771980";
	Project project = (Project) CommonUtils.getObject(oid);

	// project.setGate1(gate1StateIcon(project));
	// PersistenceHelper.manager.modify(project);

	// KEKScheduleJobs.startBatch();

	Timestamp today = DateUtils.getCurrentTimestamp();
	QuerySpec query = new QuerySpec();
	int idx = query.appendClassList(Project.class, true);

	SearchCondition sc = null;

	Calendar ca = Calendar.getInstance();
	Timestamp start = DateUtils.getCurrentTimestamp();
	ca.setTime(start);
	ca.add(Calendar.MONTH, -6);
	Timestamp end = new Timestamp(ca.getTime().getTime());

	sc = new SearchCondition(Project.class, Project.P_DATE, SearchCondition.GREATER_THAN_OR_EQUAL, end);
	query.appendWhere(sc, new int[] { idx });

	query.appendAnd();
	sc = new SearchCondition(Project.class, Project.P_DATE, SearchCondition.LESS_THAN_OR_EQUAL, start);
	query.appendWhere(sc, new int[] { idx });

	ClassAttribute csa = new ClassAttribute(Project.class, Project.P_DATE);
	OrderBy by = new OrderBy(csa, true);
	query.appendOrderBy(by, new int[] { idx });

	QueryResult result = PersistenceHelper.manager.find(query);
	out.println(query);
	int cnt = 0;
	while (result.hasMoreElements()) {
		Object[] obj = (Object[]) result.nextElement();
		Project p = (Project) obj[0];
		System.out.println("p=" + p.getKekNumber() + "<br>");

		int gate1 = ProjectHelper.manager.gate1StateIcon(project);
		int gate2 = ProjectHelper.manager.gate2StateIcon(project);
		int gate3 = ProjectHelper.manager.gate3StateIcon(project);
		int gate4 = ProjectHelper.manager.gate4StateIcon(project);
		int gate5 = ProjectHelper.manager.gate5StateIcon(project);

		project.setGate1(gate1);
		project.setGate2(gate2);
		project.setGate3(gate3);
		project.setGate4(gate4);
		project.setGate5(gate5);

		int pro = ProjectHelper.manager.getKekProgress(project);

		project.setProgress(pro);
		PersistenceHelper.manager.modify(project);
	}
%>

<%!public static final String[] GATE1 = { "공정계획서", "사양체크리스트", "DR자료", "Risk Management" };

	public int gate1StateIcon(Project project) throws Exception {
		int gate1 = ProjectGateState.GATE_NO_START;
		// 태스크 이름으로 처리
		ArrayList<Task> list = new ArrayList<Task>();

		list = getterProjectTask(project, list);

		Timestamp end = null;
		Timestamp start = null;

		boolean isStart = false; // 시작 여부

		int totalProgress = 0;

		System.out.println("list=" + list.size());

		for (Task task : list) {

			String name = task.getName();
			System.out.println("name=" + name);
			for (String ss : GATE1) {

				if (name.equals(ss)) {
					totalProgress += task.getProgress();
					if (task.getState().equals("작업중") || task.getState().equals("완료됨")) {
						isStart = true;
						break;
					}

					Timestamp tStart = task.getPlanStartDate();
					Timestamp tEnd = task.getPlanEndDate();

					if (start == null || (start.getTime() < tStart.getTime())) {
						start = tStart;
					}

					if (tEnd != null) {
						if (end == null || (end.getTime() < tEnd.getTime())) {
							end = tEnd;
						}
					}
				}
			}
		}

		Timestamp today = DateUtils.getCurrentTimestamp();
		boolean isOverDay = false;
		boolean isCheckDay = false;
		if (end == null || today.getTime() > end.getTime()) {
			isOverDay = true;
		} else {
			// 실제 기간..
			int du = DateUtils.getDuration(start, end);
			BigDecimal counting = new BigDecimal(du);
			BigDecimal multi = new BigDecimal(0.2);

			BigDecimal result = counting.multiply(multi);
			int perDay = Math.round(result.floatValue()); // 2??

			int tdu = DateUtils.getDuration(end, DateUtils.getCurrentTimestamp()); // 1...
			if (tdu <= perDay) {
				isCheckDay = true;
			}
		}

		int per = totalProgress / GATE1.length;

		System.out.println("isStart1=" + isStart);
		System.out.println("totalProgress1=" + totalProgress);

		if (!isStart) {
			if (isOverDay) {
				gate1 = ProjectGateState.GATE_DELAY;
			} else {
				gate1 = ProjectGateState.GATE_NO_START;
			}
		} else {
			// 진행중이고 완료가 아닐대...
			if (isOverDay) {
				if (per != 100) {
					// 진행 오버
					gate1 = ProjectGateState.GATE_DELAY_PROGRESS;
				} else if (per >= 51 && per < 100) {
					gate1 = ProjectGateState.GATE_PROGRESS;
				} else if (per == 100) {
					gate1 = ProjectGateState.GATE_COMPLETE;
				}
			} else {
				// 진행 중이지만..

				if (isCheckDay) {
					if (per < 50) {
						// 주황색
						gate1 = ProjectGateState.GATE_DELAY_PROGRESS;
					} else if (per >= 51 && per < 100) {
						gate1 = ProjectGateState.GATE_PROGRESS;
					} else if (per == 100) {
						gate1 = ProjectGateState.GATE_COMPLETE;
					}
				} else {
					gate1 = ProjectGateState.GATE_PROGRESS;
				}
			}
		}
		return gate1;
	}%>

<%!public ArrayList<Task> getterProjectTask(Project project, ArrayList<Task> list) throws Exception {

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);
		long ids = project.getPersistInfo().getObjectIdentifier().getId();
		SearchCondition sc = new SearchCondition(Task.class, "projectReference.key.id", "=", ids);
		query.appendWhere(sc, new int[] { idx });
		query.appendAnd();

		sc = new SearchCondition(Task.class, "parentTaskReference.key.id", "=", 0L);
		query.appendWhere(sc, new int[] { idx });
		query.appendAnd();

		sc = new SearchCondition(Task.class, Task.DEPTH, "=", 2);
		query.appendWhere(sc, new int[] { idx });

		ClassAttribute ca = new ClassAttribute(Task.class, Task.SORT);
		OrderBy orderBy = new OrderBy(ca, false);
		query.appendOrderBy(orderBy, new int[] { idx });

		query.setAdvancedQueryEnabled(true);
		query.setDescendantQuery(false);

		QueryResult result = PersistenceHelper.manager.find(query);

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Task t = (Task) obj[0];
			list.add(t);
			ProjectHelper.manager.getterTasks(t, project, list);
		}
		return list;
	}%>