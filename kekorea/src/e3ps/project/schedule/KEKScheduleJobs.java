package e3ps.project.schedule;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import e3ps.common.util.DateUtils;
import e3ps.project.Project;
import e3ps.project.service.ProjectHelper;
import e3ps.project.task.Task;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;

public class KEKScheduleJobs {

	public static void startBatch() {
		try {
			System.out.println("START!! KEK SCHEDULE");
			startTask();
			System.out.println("END!! KEK SCHEDULE");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void startTask() throws Exception {
		Timestamp today = DateUtils.getCurrentTimestamp();
		try {
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

			int cnt = 0;
			while (result.hasMoreElements()) {
				Object[] obj = (Object[]) result.nextElement();
				Project project = (Project) obj[0];

				System.out.println("KEKNUMBER = " + project.getKekNumber());

				Timestamp planStartDate = project.getPlanStartDate();

				if (planStartDate != null && (DateUtils.convertStartDate(planStartDate.toString().substring(0, 10))
						.getTime() < today.getTime())) {

					if (project.getStartDate() == null) {
						project.setState("작업 중");
						project.setKekState("설계중");
						project.setStartDate(today);
					}

					if (project.getKekState() != null && project.getKekState().equals("준비")) {
						project.setKekState("설계중");
					}

					ArrayList<Task> list = new ArrayList<Task>();

					list = ProjectHelper.manager.getterProjectTask2(project, list);

					for (Task tt : list) {

						Timestamp tplanStartDate = tt.getPlanStartDate();

						if (tplanStartDate == null) {
							if (tt.getStartDate() == null) {
								tt.setStartDate(today);
								tt.setState("작업 중");
								PersistenceServerHelper.manager.update(tt);
							}
						}

						if (tplanStartDate != null
								&& (DateUtils.convertStartDate(tplanStartDate.toString().substring(0, 10))
										.getTime() < today.getTime())) {

							if (tt.getStartDate() == null) {
								tt.setStartDate(today);
								tt.setState("작업 중");
								PersistenceHelper.manager.modify(tt);
							}
						}
					}
					PersistenceServerHelper.manager.update(project);
					project = (Project) PersistenceHelper.manager.refresh(project);

//					ProjectHelper.manager.setProgress(project);
//
//					list = ProjectHelper.manager.getterProjectTask(project, list);
//
//					ProjectHelper.manager.setParentProgressSet(list);
//
//					ProjectHelper.manager.setProjectParentDate(list);
//
//					ProjectHelper.manager.setProjectDuration(project);

					if ("견적".equals(project.getPType())) {
						setQState(project);
					} else {
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
					}

					int pro = ProjectHelper.manager.getKekProgress(project);

					project.setProgress(pro);
					PersistenceHelper.manager.modify(project);

					setProgressCheck(project, pro);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setQState(Project project) throws Exception {
		String[] QTASK = ProjectHelper.QTASK;
		ArrayList<Task> list = new ArrayList<Task>();
		list = ProjectHelper.manager.getterProjectTask2(project, list);
		StringBuffer sb = new StringBuffer();
		for (Task tt : list) {

			String tname = tt.getName();

			if (tname.equals("의뢰서")) {
				continue;
			}

			if (!tname.equals(QTASK[0]) && !tname.equals(QTASK[1]) && !tname.equals(QTASK[2]) && !tname.equals(QTASK[3])
					&& !tname.equals(QTASK[4])) {
				continue;
			}

			if (tname.equals(QTASK[0])) {
				int qstate = ProjectHelper.manager.getQState(tt);
				project.setGate1(qstate);
			}

			if (tname.equals(QTASK[1])) {
				int qstate = ProjectHelper.manager.getQState(tt);
				project.setGate2(qstate);
			}

			if (tname.equals(QTASK[2])) {
				int qstate = ProjectHelper.manager.getQState(tt);
				project.setGate3(qstate);
			}

			if (tname.equals(QTASK[3])) {
				int qstate = ProjectHelper.manager.getQState(tt);
				project.setGate4(qstate);
			}

			if (tname.equals(QTASK[4])) {
				int qstate = ProjectHelper.manager.getQState(tt);
				project.setGate5(qstate);
			}

		}

		PersistenceHelper.manager.modify(project);
	}

	private static void setProgressCheck(Project project, int pro) throws Exception {
		// int progress = project.getProgress();
		int progress = pro;
		if (progress == 100) {
			project.setState("완료됨");

			if (project.getEndDate() == null) {
				project.setEndDate(DateUtils.getCurrentTimestamp());
			}

			if (project.getKekState() != null) {
				switch (project.getKekState()) {
				case "작업완료":
					break;
				default:
					project.setKekState("설계완료");
				}
			} else {
				project.setKekState("설계완료");
			}

			PersistenceHelper.manager.modify(project);

			project = (Project) PersistenceHelper.manager.refresh(project);
			ArrayList<Task> list = new ArrayList<Task>();
			list = ProjectHelper.manager.getterProjectTask(project, list);
			for (Task tt : list) {
				tt.setState("완료됨");
				if (tt.getEndDate() == null) {
					tt.setEndDate(DateUtils.getCurrentTimestamp());
				}

				if (tt.getStartDate() == null) {
					tt.setStartDate(DateUtils.getCurrentTimestamp());
				}

				tt.setProgress(100);
				tt = (Task) PersistenceHelper.manager.modify(tt);
			}
		}
	}
}