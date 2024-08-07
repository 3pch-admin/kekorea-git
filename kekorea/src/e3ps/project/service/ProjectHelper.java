package e3ps.project.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import e3ps.admin.commonCode.CommonCode;
import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.PartListMasterProjectLink;
import e3ps.bom.partlist.service.PartlistHelper;
import e3ps.bom.tbom.TBOMMaster;
import e3ps.bom.tbom.service.TBOMHelper;
import e3ps.common.util.AUIGridUtils;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.doc.E3PSDocumentMaster;
import e3ps.doc.PRJDocument;
import e3ps.doc.meeting.Meeting;
import e3ps.doc.meeting.service.MeetingHelper;
import e3ps.doc.request.RequestDocument;
import e3ps.doc.request.service.RequestDocumentHelper;
import e3ps.doc.service.DocumentHelper;
import e3ps.epm.KeDrawing;
import e3ps.epm.service.EpmHelper;
import e3ps.epm.service.KeDrawingHelper;
import e3ps.epm.workOrder.WorkOrder;
import e3ps.epm.workOrder.service.WorkOrderHelper;
import e3ps.korea.configSheet.ConfigSheet;
import e3ps.korea.configSheet.service.ConfigSheetHelper;
import e3ps.part.KePart;
import e3ps.part.service.KePartHelper;
import e3ps.project.Project;
import e3ps.project.ProjectUserLink;
import e3ps.project.dto.ProjectDTO;
import e3ps.project.dto.ProjectGateState;
import e3ps.project.issue.Issue;
import e3ps.project.issue.service.IssueHelper;
import e3ps.project.output.Output;
import e3ps.project.output.OutputProjectLink;
import e3ps.project.output.OutputTaskLink;
import e3ps.project.output.service.OutputHelper;
import e3ps.project.task.ParentTaskChildTaskLink;
import e3ps.project.task.TargetTaskSourceTaskLink;
import e3ps.project.task.Task;
import e3ps.project.task.service.TaskHelper;
import e3ps.project.task.variable.TaskStateVariable;
import e3ps.project.template.Template;
import e3ps.project.variable.ProjectStateVariable;
import e3ps.project.variable.ProjectUserTypeVariable;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.content.ContentHolder;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.PagingQueryResult;
import wt.fc.PagingSessionHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.lifecycle.LifeCycleManaged;
import wt.org.WTUser;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.util.WTAttributeNameIfc;

public class ProjectHelper {

	public final static String PM_ID = "yspark";
	public final static String SUB_PM_ID = "19940009";

	public static final String STAND = "준비중";
	public static final String INWORK = "작업 중";
	public static final String DELAY = "지연됨";
	public static final String COMPLETE = "완료됨";
	public static final String STOP = "중단됨";

	public static final String[] GATE1 = { "공정계획서", "사양체크리스트", "DR자료", "Risk Management" };
	public static final String[] GATE2 = { "1차_수배", "1차_수배" };
	public static final String[] GATE3 = { "전기_제작사양서", "기계_제작사양서" };
	public static final String[] GATE4 = { "2차_수배", "2차_수배" };
	public static final String[] GATE5 = { "전기_작업지시서", "기계_작업지시서", "설비사양서" };
	public static final String[] QTASK = { "사양체크리스트", "Risk Management", "견적설계검토서", "DR자료", "고객제출용자료" };

	public static final String REQ_TASK_NAME = "의뢰서";

	public static final ProjectService service = ServiceFactory.getService(ProjectService.class);
	public static final ProjectHelper manager = new ProjectHelper();

	/**
	 * 전기 진행율 할당율 계산
	 */
	public int getElecAllocateProgress(Project project) throws Exception {
		int progress = 0;

		ArrayList<Task> list = new ArrayList<Task>();
//		list = recurciveTask(project, 2);
		list = recurciveTask(project);

		int sumAllocate = 0;
		int sumProgress = 0;
		// 모든 태스크 수집

		for (Task task : list) {

			String taskType = task.getTaskType().getName();

			if ("전기".equals(taskType)) {

				int allocate = task.getAllocate() != null ? task.getAllocate() : 0;
				int tprogress = task.getProgress() != null ? task.getProgress() : 0;

				sumProgress += (allocate * tprogress);

				sumAllocate += allocate;

				// 기계 진행율 = SUM(기계Task 각각의 할당률xTask 진행률)/SUM(기계Task 각각의 할당률)
			}

		}

		if (sumAllocate != 0) {
			progress = sumProgress / sumAllocate;
		}
		return progress;
	}

	/**
	 * 기계 진행율 할당율 계산
	 */
	public int getMachineAllocateProgress(Project project) throws Exception {
		int progress = 0;

		ArrayList<Task> list = new ArrayList<Task>();

//		list = recurciveTask(project, 2);

		list = recurciveTask(project);

		int sumAllocate = 0;
		int sumProgress = 0;
		// 모든 태스크 수집

		for (Task task : list) {

			String taskType = task.getTaskType().getName();

			if ("기계".equals(taskType)) {

				int allocate = task.getAllocate() != null ? task.getAllocate() : 0;
				int tprogress = task.getProgress() != null ? task.getProgress() : 0;

				sumProgress += (allocate * tprogress);

				sumAllocate += allocate;

				// 기계 진행율 = SUM(기계Task 각각의 할당률xTask 진행률)/SUM(기계Task 각각의 할당률)
			}

		}

		if (sumAllocate != 0) {
			progress = sumProgress / sumAllocate;
		}
		return progress;
	}

	/**
	 * 프로젝트 담당자가져오기
	 */
	public WTUser getUserType(Project project, String userType) throws Exception {
		CommonCode userTypeCode = CommonCodeHelper.manager.getCommonCode(userType, "USER_TYPE");
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ProjectUserLink.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, ProjectUserLink.class, "roleAObjectRef.key.id", project);
		QuerySpecUtils.toEqualsAnd(query, idx, ProjectUserLink.class, "userTypeReference.key.id", userTypeCode);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ProjectUserLink link = (ProjectUserLink) obj[0];
			return link.getUser();
		}
		return null;
	}

	/**
	 * 진행율 계산
	 */
	public static double getPreferComp(Task task) throws Exception {
		double preferComp = 0;
		Timestamp today = DateUtils.getCurrentTimestamp();
		String cdate = DateUtils.getDateString(today, "date");
		String sdate = DateUtils.getDateString(task.getPlanStartDate(), "date");
		String edate = DateUtils.getDateString(task.getPlanEndDate(), "date");

		if (cdate.compareTo(sdate) < 0) {
			preferComp = 0;
		} else if (cdate.compareTo(edate) >= 0) {
			preferComp = 100;
		} else {
			double du = DateUtils.getPlanDurationHoliday(task.getPlanStartDate(), today);
			double planDuration = DateUtils.getPlanDurationHoliday(task.getPlanStartDate(), task.getPlanEndDate());

			if (planDuration == 0) {
				preferComp = 0;
			} else {
				preferComp = (du / planDuration) * 100;
			}
		}
		return preferComp;
	}

	/**
	 * 계획 대비 진행율 계산
	 */
	public static double getPreferComp(Project project) throws Exception {
		double preferComp = 0;

		Timestamp today = DateUtils.getCurrentTimestamp();
		String cdate = DateUtils.getDateString(today, "date");
		String sdate = DateUtils.getDateString(project.getPlanStartDate(), "date");
		String edate = DateUtils.getDateString(project.getPlanEndDate(), "date");

		if (cdate.compareTo(sdate) < 0) {
			preferComp = 0;
		} else if (cdate.compareTo(edate) >= 0) {
			preferComp = 100;
		} else {
			double du = DateUtils.getPlanDurationHoliday(project.getPlanStartDate(), today);
			double planDuration = DateUtils.getPlanDurationHoliday(project.getPlanStartDate(),
					project.getPlanEndDate());

			if (planDuration == 0) {
				preferComp = 0;
			} else {
				preferComp = (du / planDuration) * 100;
			}
		}

		return preferComp;
	}

	/**
	 * 리스트 프로젝트 진행 바 표시
	 */
	public String getProjectStateBar(Project project) throws Exception {

		int progress = ProjectHelper.manager.getKekProgress(project);
		int comp = (int) ProjectHelper.getPreferComp(project);

		int gap = progress - comp;
		String bar = "";

		if (COMPLETE.equals(project.getState())) {
			bar += "<img title='프로젝트 완료 되었습니다.' class='pos3' src='/Windchill/jsp/images/state_green_bar.gif'>";
			bar += "<img title='프로젝트 완료 되었습니다.' class='pos3' src='/Windchill/jsp/images/state_green_bar.gif'>";
			bar += "<img title='프로젝트 완료 되었습니다.' class='pos3' src='/Windchill/jsp/images/state_green_bar.gif'>";
		} else {

			if (progress != 100) {

				// 프로젝트 시작
				if (!"준비".equals(project.getKekState())) {
					if (gap < 0 && gap >= -30) {
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
					} else if (gap < -30) {
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
					} else if (gap == 0) {
						bar += "<img class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
					} else if (gap > 0) {
						bar += "<img title='프로젝트 정상진행 중 입니다.' class='pos3' src='/Windchill/jsp/images/state_blue_bar.gif'>";
						bar += "<img title='프로젝트 정상진행 중 입니다.' class='pos3' src='/Windchill/jsp/images/state_blue_bar.gif'>";
						bar += "<img title='프로젝트 정상진행 중 입니다.' class='pos3' src='/Windchill/jsp/images/state_blue_bar.gif'>";
						bar += "<img title='프로젝트 정상진행 중 입니다.' class='pos3' src='/Windchill/jsp/images/state_blue_bar.gif'>";
						bar += "<img title='프로젝트 정상진행 중 입니다.' class='pos3' src='/Windchill/jsp/images/state_blue_bar.gif'>";
					}
				} else {
					if (gap > 0) {
						bar += "<img title='프로젝트 시작전 입니다.' class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img title='프로젝트 시작전 입니다.' class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img title='프로젝트 시작전 입니다.' class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img title='프로젝트 시작전 입니다.' class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
						bar += "<img title='프로젝트 시작전 입니다.' class='pos3' src='/Windchill/jsp/images/state_blank_bar.gif'>";
					} else if (gap < 0 && gap >= -30) {
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_yellow_bar.gif'>";
					} else if (gap < -30) {
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
						bar += "<img title='프로젝트 지연되고 있습니다.' class='pos3' src='/Windchill/jsp/images/state_red_bar.gif'>";
					}
				}
			} else if (progress == 100) {
				bar += "<img title='프로젝트 완료 되었습니다.' class='pos3' src='/Windchill/jsp/images/state_green_bar.gif'>";
				bar += "<img title='프로젝트 완료 되었습니다.' class='pos3' src='/Windchill/jsp/images/state_green_bar.gif'>";
				bar += "<img title='프로젝트 완료 되었습니다.' class='pos3' src='/Windchill/jsp/images/state_green_bar.gif'>";
			}
		}
		return bar;
	}

	/**
	 * 트리 태스크 아이콘
	 */
	public String getStateIcon(int type) throws Exception {
		if (type == 0) {
			return "<img title='태스크 시작전 입니다.' style='position: relative; top: 3px;' src='/Windchill/extcore/images/project/state_blank_bar.png'>";
		} else if (type == 1) {
			return "<img title='태스크가 계획 종료일 보다 초과 되서 진행 중 입니다.' style='position: relative; top: 3px;' class='' src='/Windchill/extcore/images/project/state_red_bar.png'>";
		} else if (type == 2) {
			return "<img title='태스크가 진행 중 입니다.' style='position: relative; top: 3px;' src='/Windchill/extcore/images/project/state_yellow_bar.png'>";
		} else if (type == 3) {
			return "<img title='태스크 지연 입니다.' style='position: relative; top: 3px;' src='/Windchill/extcore/images/project/state_orange_bar.png'>";
		} else if (type == 4) {
			return "<img title='태스크 완료 입니다.' style='position: relative; top: 3px;' src='/Windchill/extcore/images/project/state_green_bar.png'>";
		}
		return "";
	}

	/**
	 * 작번 조회 함수
	 */
	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		System.out.println("작번검색 START = " + new Timestamp(new Date().getTime()));
		Map<String, Object> map = new HashMap<String, Object>();
		List<ProjectDTO> list = new ArrayList<ProjectDTO>();

		String kekNumber = (String) params.get("kekNumber");
		String keNumber = (String) params.get("keNumber");
		String pdateFrom = (String) params.get("pdateFrom");
		String pdateTo = (String) params.get("pdateTo");
		String userId = (String) params.get("userId");
		String kekState = (String) params.get("kekState");
		String model = (String) params.get("model");
		String customer_name = (String) params.get("customer_name");
		String install_name = (String) params.get("install_name");
		String projectType = (String) params.get("projectType");
		String machineOid = (String) params.get("machineOid");
		String elecOid = (String) params.get("elecOid");
		String softOid = (String) params.get("softOid");
		String mak_name = (String) params.get("mak_name");
		String detail_name = (String) params.get("detail_name");
		String template = (String) params.get("template");
		String description = (String) params.get("description");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Project.class, true);

		if (!StringUtils.isNull(kekNumber)) {
			String[] s = kekNumber.split(",");
			if (s.length > 0) {
				for (String ss : s) {
					QuerySpecUtils.toLikeOr(query, idx, Project.class, Project.KEK_NUMBER, ss);
				}
			}
		}

//		QuerySpecUtils.toLikeAnd(query, idx, Project.class, Project.KEK_NUMBER, kekNumber);
		QuerySpecUtils.toLikeAnd(query, idx, Project.class, Project.KE_NUMBER, keNumber);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, Project.class, Project.P_DATE, pdateFrom, pdateTo);
		QuerySpecUtils.toLikeAnd(query, idx, Project.class, Project.USER_ID, userId);
		QuerySpecUtils.toLikeAnd(query, idx, Project.class, Project.KEK_STATE, kekState);
		QuerySpecUtils.toLikeAnd(query, idx, Project.class, Project.MODEL, model);

		if (!StringUtils.isNull(customer_name)) {
			CommonCode customerCode = (CommonCode) CommonUtils.getObject(customer_name);
			QuerySpecUtils.toEqualsAnd(query, idx, Project.class, "customerReference.key.id", customerCode);
		}

		if (!StringUtils.isNull(install_name)) {
			CommonCode installCode = (CommonCode) CommonUtils.getObject(install_name);
			QuerySpecUtils.toEqualsAnd(query, idx, Project.class, "installReference.key.id", installCode);
		}

		if (!StringUtils.isNull(projectType)) {
			CommonCode projectTypeCode = (CommonCode) CommonUtils.getObject(projectType);
			QuerySpecUtils.toEqualsAnd(query, idx, Project.class, "projectTypeReference.key.id", projectTypeCode);
		}

		if (!StringUtils.isNull(machineOid)) {
			WTUser machine = (WTUser) CommonUtils.getObject(machineOid);
			CommonCode machineCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.MACHINE,
					"USER_TYPE");
			int idx_plink = query.appendClassList(ProjectUserLink.class, false);
			int idx_u = query.appendClassList(WTUser.class, false);

			QuerySpecUtils.toInnerJoin(query, Project.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
					"roleAObjectRef.key.id", idx, idx_plink);
			QuerySpecUtils.toInnerJoin(query, WTUser.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
					"roleBObjectRef.key.id", idx_u, idx_plink);
			QuerySpecUtils.toEqualsAnd(query, idx_plink, ProjectUserLink.class, "roleBObjectRef.key.id", machine);
			QuerySpecUtils.toEqualsAnd(query, idx_plink, ProjectUserLink.class, "userTypeReference.key.id",
					machineCode);
		}

		if (!StringUtils.isNull(elecOid)) {
			WTUser elec = (WTUser) CommonUtils.getObject(elecOid);
			CommonCode elecCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.ELEC, "USER_TYPE");
			int idx_plink = query.appendClassList(ProjectUserLink.class, false);
			int idx_u = query.appendClassList(WTUser.class, false);

			QuerySpecUtils.toInnerJoin(query, Project.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
					"roleAObjectRef.key.id", idx, idx_plink);
			QuerySpecUtils.toInnerJoin(query, WTUser.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
					"roleBObjectRef.key.id", idx_u, idx_plink);
			QuerySpecUtils.toEqualsAnd(query, idx_plink, ProjectUserLink.class, "roleBObjectRef.key.id", elec);
			QuerySpecUtils.toEqualsAnd(query, idx_plink, ProjectUserLink.class, "userTypeReference.key.id", elecCode);
		}

		if (!StringUtils.isNull(softOid)) {
			WTUser soft = (WTUser) CommonUtils.getObject(softOid);
			CommonCode softCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.SOFT, "USER_TYPE");
			int idx_plink = query.appendClassList(ProjectUserLink.class, false);
			int idx_u = query.appendClassList(WTUser.class, false);

			QuerySpecUtils.toInnerJoin(query, Project.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
					"roleAObjectRef.key.id", idx, idx_plink);
			QuerySpecUtils.toInnerJoin(query, WTUser.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
					"roleBObjectRef.key.id", idx_u, idx_plink);
			QuerySpecUtils.toEqualsAnd(query, idx_plink, ProjectUserLink.class, "roleBObjectRef.key.id", soft);
			QuerySpecUtils.toEqualsAnd(query, idx_plink, ProjectUserLink.class, "userTypeReference.key.id", softCode);
		}

		if (!StringUtils.isNull(mak_name)) {
			CommonCode makCode = (CommonCode) CommonUtils.getObject(mak_name);
			QuerySpecUtils.toEqualsAnd(query, idx, Project.class, "makReference.key.id", makCode);
		}

		if (!StringUtils.isNull(detail_name)) {
			CommonCode detailCode = (CommonCode) CommonUtils.getObject(detail_name);
			QuerySpecUtils.toEqualsAnd(query, idx, Project.class, "detailReference.key.id", detailCode);
		}

		if (!StringUtils.isNull(template)) {
			Template t = (Template) CommonUtils.getObject(template);
			QuerySpecUtils.toEqualsAnd(query, idx, Project.class, "templateReference.key.id", t);
		}

		QuerySpecUtils.toLikeAnd(query, idx, Project.class, Project.DESCRIPTION, description);
		QuerySpecUtils.toOrderBy(query, idx, Project.class, Project.P_DATE, true);

		System.out.println("query=" + query);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Project project = (Project) obj[0];
			ProjectDTO column = new ProjectDTO(project);
			list.add(column);
		}

		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		System.out.println("작번검색 END = " + new Timestamp(new Date().getTime()));
		return map;
	}

	/**
	 * 프로젝트 트리
	 */
	public JSONArray load(String oid) throws Exception {
		Project project = (Project) CommonUtils.getObject(oid);
		JSONArray list = new JSONArray();
		JSONObject node = new JSONObject();
		node.put("oid", project.getPersistInfo().getObjectIdentifier().getStringValue());
		node.put("name", project.getKekNumber());
		node.put("description", project.getDescription());
		node.put("duration", project.getDuration());
		node.put("allocate", 0);
		node.put("taskType", project.getProjectType().getName());
		node.put("treeIcon", getTreeIcon(project));
		node.put("type", "project");
		node.put("isNew", false);

		JSONArray childrens = new JSONArray();
		ArrayList<Task> taskList = TaskHelper.manager.getProjectTasks(project);
		for (Task task : taskList) {
			JSONObject children = new JSONObject();
			children.put("oid", task.getPersistInfo().getObjectIdentifier().getStringValue());
			children.put("name", task.getName());
			children.put("description", task.getDescription());
			children.put("duration", task.getDuration());
			children.put("allocate", task.getAllocate() != null ? task.getAllocate() : 0);
			children.put("taskType", task.getTaskType().getCode());
			children.put("type", "task");
			children.put("isNew", false);
			children.put("treeIcon", getTreeIcon(task));
			load(children, project, task);
			childrens.add(children);
		}
		node.put("children", childrens);
		list.add(node);
		return list;
	}

	/**
	 * 프로젝트 태스크 트리 아이콘
	 */
	private Object getTreeIcon(Task task) {
		// 준비중, 진행중, 완료됨, 지연됨
		String icon = "/Windchill/extcore/images/task_ready.gif";

		String state = task.getState();
		if (TaskStateVariable.COMPLETE.equals(state)) {
			return "/Windchill/extcore/images/task_complete.gif";
		} else if (TaskStateVariable.READY.equals(state)) {
			return "/Windchill/extcore/images/task_ready.gif";
		} else {

			Timestamp today = DateUtils.getCurrentTimestamp();

			int du = DateUtils.getDuration(task.getPlanStartDate(), task.getPlanEndDate());
			BigDecimal counting = new BigDecimal(du);
			BigDecimal multi = new BigDecimal(0.2);

			BigDecimal result = counting.multiply(multi);
			int perDay = Math.round(result.floatValue()); // 2??

			int tdu = DateUtils.getDuration(task.getPlanEndDate(), DateUtils.getCurrentTimestamp()); // 1...

			if (task.getPlanEndDate() != null && today.getTime() > task.getPlanEndDate().getTime()) {
				return "/Windchill/extcore/images/task_delay.gif";
			} else {
				if (tdu <= perDay) {
					if (task.getProgress() < 50) {
						return "/Windchill/extcore/images/task_orange.gif";
					} else if (task.getProgress() >= 51 && task.getProgress() < 100) {
						return "/Windchill/extcore/images/task_yellow.gif";
					} else if (task.getProgress() == 100) {
						return "/Windchill/extcore/images/task_complete.gif";
					}
				} else {
					return "/Windchill/extcore/images/task_yellow.gif";
				}
			}
		}
		return icon;
	}

	/**
	 * 프로젝트 트리 아이콘 경로
	 */
	private String getTreeIcon(Project project) {
		// 준비중, 진행중, 완료됨, 지연됨
		String icon = "/Windchill/extcore/images/task_ready.gif";

		String state = project.getState();

		if (ProjectStateVariable.COMPLETE.equals(state)) {
			return "/Windchill/extcore/images/task_complete.gif";
		} else if (ProjectStateVariable.READY.equals(state)) {
			return "/Windchill/extcore/images/task_ready.gif";
		} else {
			Timestamp today = DateUtils.getCurrentTimestamp();
			int du = DateUtils.getDuration(project.getPlanStartDate(), project.getPlanEndDate());
			BigDecimal counting = new BigDecimal(du);
			BigDecimal multi = new BigDecimal(0.2);

			BigDecimal result = counting.multiply(multi);
			int perDay = Math.round(result.floatValue()); // 2??

			int tdu = DateUtils.getDuration(project.getPlanEndDate(), DateUtils.getCurrentTimestamp()); // 1...

			if (today.getTime() > project.getPlanEndDate().getTime()) {
				return "/Windchill/extcore/images/task_delay.gif";
			} else {
				if (tdu <= perDay) {
					if (project.getProgress() < 50) {
						return "/Windchill/extcore/images/task_orange.gif";
					} else if (project.getProgress() >= 51 && project.getProgress() < 100) {
						return "/Windchill/extcore/images/task_yellow.gif";
					} else if (project.getProgress() == 100) {
						return "/Windchill/extcore/images/task_complete.gif";
					}
				} else {
					return "/Windchill/extcore/images/task_yellow.gif";
				}
			}
		}
		return icon;
	}

	/**
	 * 프로젝트 태스크 트리
	 */
	private void load(JSONObject node, Project project, Task parentTask) throws Exception {
		JSONArray childrens = new JSONArray();
		ArrayList<Task> taskList = TaskHelper.manager.getProjectTasks(project, parentTask);
		for (Task task : taskList) {
			JSONObject children = new JSONObject();
			children.put("oid", task.getPersistInfo().getObjectIdentifier().getStringValue());
			children.put("name", task.getName());
			children.put("description", task.getDescription());
			children.put("duration", task.getDuration());
			children.put("allocate", task.getAllocate() != null ? task.getAllocate() : 0);
			children.put("taskType", task.getTaskType().getCode());
			children.put("type", "task");
			children.put("isNew", false);
			children.put("treeIcon", getTreeIcon(task));
			load(children, project, task);
			childrens.add(children);
		}
		node.put("children", childrens);
	}

	/**
	 * 프로젝트 참조 작번
	 */
	public JSONArray referenceTab(String oid) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		Project project = (Project) CommonUtils.getObject(oid);
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Project.class, true);
		QuerySpecUtils.toLikeRightAnd(query, idx, Project.class, Project.KEK_NUMBER, project.getKekNumber() + "%");
		QuerySpecUtils.toNotEqualsAnd(query, idx, Project.class, "projectTypeReference.key.id",
				project.getProjectType());
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Project p = (Project) obj[0];
			Map<String, Object> map = new HashMap<>();
			map.put("kekNumber", p.getKekNumber());
			map.put("projectType_name", p.getProjectType().getName());
			map.put("mak_name", p.getMak().getName());
			map.put("detail_name", p.getDetail().getName());
			map.put("customer_name", p.getCustomer().getName());
			map.put("install_name", p.getInstall().getName());
			map.put("description", p.getDescription());
			map.put("pdate_txt", CommonUtils.getPersistableTime(p.getPDate()));
			map.put("customDate_txt", CommonUtils.getPersistableTime(p.getCustomDate()));
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 수배표 전체 리스트 (기계, 전기 구분)
	 */
	public JSONArray jsonAuiPartlist(String oid, String toid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		Project project = (Project) CommonUtils.getObject(oid);
		Task task = (Task) CommonUtils.getObject(toid);
		String[] t = null;
		if (task.getName().equals("기계_수배표")) {
			t = new String[] { "기계_1차_수배", "기계_2차_수배" };
		} else if (task.getName().equals("전기_수배표")) {
			t = new String[] { "전기_1차_수배", "전기_2차_수배" };
		}

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(PartListMaster.class, true);
		int idx_link = query.appendClassList(PartListMasterProjectLink.class, false);
		int idx_p = query.appendClassList(Project.class, false);

		QuerySpecUtils.toInnerJoin(query, PartListMaster.class, PartListMasterProjectLink.class,
				WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, Project.class, PartListMasterProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_p, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, PartListMasterProjectLink.class, "roleBObjectRef.key.id", project);
		QuerySpecUtils.toIn(query, idx, PartListMaster.class, PartListMaster.ENG_TYPE, t);
		QuerySpecUtils.toOrderBy(query, idx, PartListMaster.class, PartListMaster.CREATE_TIMESTAMP, false);

		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			PartListMaster master = (PartListMaster) obj[0];
			Map<String, String> map = new HashMap<>();
			map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("name", master.getName());
			map.put("number", master.getNumber());
			map.put("state", master.getLifeCycleState().getDisplay());
			map.put("creator", master.getCreatorFullName());
			map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
			map.put("secondary", AUIGridUtils.secondaryTemplate(master));
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 작번 기본 산출물
	 */
	public JSONArray jsonAuiOutput(String poid, String toid) throws Exception {
		return jsonAuiOutput(poid, toid, true);
	}

	/**
	 * 태스크 이름이 일치하는 태스크가 있는지 검색 프로젝트 내에서
	 */
	public Task getTaskByName(Project project, String name) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "projectReference.key.id", project);
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, Task.NAME, name);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			return (Task) obj[0];
		}
		return null;
	}

	/**
	 * 국제에 맞는 KEK 진행율 가져오기 계산식 들어감
	 */
	public int getKekProgress(Project project) throws Exception {
		int kekProgress = 0;
//		String[] taskTypes = new String[] { "COMMON", "MACHINE", "ELEC", "SOFT" };
//		ArrayList<Task> list = recurciveTask(project, taskTypes);
		ArrayList<Task> list = recurciveTask(project);

		int sumAllocate = 0;
		int sumProgress = 0;
		for (Task task : list) {

			int allocate = task.getAllocate() != null ? task.getAllocate() : 0;
			int tprogress = task.getProgress() != null ? task.getProgress() : 0;
			System.out.println("=======allocate =" + allocate);
			System.out.println("=======tprogress =" + tprogress);
			sumProgress += (allocate * tprogress);
			System.out.println("=======sumProgress =" + sumProgress);
			sumAllocate += allocate;
			System.out.println("=======sumAllocate  =" + sumAllocate);
		}

		if (sumAllocate != 0) {
			kekProgress = sumProgress / sumAllocate;
			System.out.println("=======kekProgress  =" + kekProgress);
		}
		return kekProgress;
	}

	/**
	 * 진행율 위한 태스크 수집 함수
	 */
	public ArrayList<Task> recurciveTask(Project project, int depth) throws Exception {
		ArrayList<Task> list = new ArrayList<Task>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);

		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "projectReference.key.id",
				project.getPersistInfo().getObjectIdentifier().getId());
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "parentTaskReference.key.id", 0L);
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, Task.DEPTH, depth);

		QuerySpecUtils.toOrderBy(query, idx, Task.class, Task.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		System.out.println("========recurciveTask 905line result =" + result);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Task task = (Task) obj[0];
			list.add(task);
		}
		return list;
	}

	/**
	 * 태스크 가져오기 재귀함수
	 */
	public ArrayList<Task> recurciveTask(Project project) throws Exception {
		return recurciveTask(project, null);
	}

	/**
	 * 모든 프로젝트 태스크 재귀함수 태스크 타입 조건 추가
	 */
	public ArrayList<Task> recurciveTask(Project project, String[] taskTypes) throws Exception {
		ArrayList<Task> list = new ArrayList<Task>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);

		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "projectReference.key.id",
				project.getPersistInfo().getObjectIdentifier().getId());
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "parentTaskReference.key.id", 0L);

		if (taskTypes != null) {
			long[] ids = new long[taskTypes.length];
			for (int i = 0; i < taskTypes.length; i++) {
				String taskType = taskTypes[i];
				CommonCode taskTypeCode = CommonCodeHelper.manager.getCommonCode(taskType, "TASK_TYPE");
				ids[i] = taskTypeCode.getPersistInfo().getObjectIdentifier().getId();
			}
			QuerySpecUtils.toIn(query, idx, Task.class, "taskTypeReference.key.id", ids);
		}

		QuerySpecUtils.toOrderBy(query, idx, Task.class, Task.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Task task = (Task) obj[0];
			list.add(task);
			recurciveTask(project, task, list, taskTypes);
		}
		return list;
	}

	/**
	 * 모든 프로젝트 태스크 재귀함수 태스크 타입 조건 추가
	 */
	private void recurciveTask(Project project, Task parentTask, ArrayList<Task> list, String[] taskTypes)
			throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);

		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "projectReference.key.id",
				project.getPersistInfo().getObjectIdentifier().getId());
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "parentTaskReference.key.id", parentTask);
		if (taskTypes != null) {
			long[] ids = new long[taskTypes.length];
			for (int i = 0; i < taskTypes.length; i++) {
				String taskType = taskTypes[i];
				CommonCode taskTypeCode = CommonCodeHelper.manager.getCommonCode(taskType, "TASK_TYPE");
				ids[i] = taskTypeCode.getPersistInfo().getObjectIdentifier().getId();
			}
			QuerySpecUtils.toIn(query, idx, Task.class, "taskTypeReference.key.id", ids);
		}

		QuerySpecUtils.toOrderBy(query, idx, Task.class, Task.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Task task = (Task) obj[0];
			list.add(task);
			recurciveTask(project, task, list, taskTypes);
		}
	}

	/**
	 * 부모 태스크와 연관된 자식 태스크 가져오기.. 수배표에서만 사용용도
	 */
	public Task getTaskByParent(Project project, Task parentTask) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "projectReference.key.id", project);
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "parentTaskReference.key.id", parentTask);
		QuerySpecUtils.toOrderBy(query, idx, Task.class, Task.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		Task task = null;
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			task = (Task) obj[0];
			// 기존 태스크랑 연결된 수배표가 있다면 패스 시킨다. // 0개 1차수배 2차수배 1개이후 쿼리대로 2차 수배만 계속 리턴
			QueryResult qr = PersistenceHelper.manager.navigate(task, "output", OutputTaskLink.class);
			if (qr.size() == 0) {
				break;
			}
		}
		return task;
	}

	/**
	 * 프로젝트 태스크 전체 진행율 계산
	 */
	public void calculation(Project project) throws Exception {
		ArrayList<Task> list = recurciveTask(project);

		for (Task tt : list) {
			QuerySpec query = new QuerySpec();
			int idx = query.appendClassList(Task.class, true);
			QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "parentTaskReference.key.id", tt);
			QuerySpecUtils.toOrderBy(query, idx, Task.class, Task.SORT, false);
			QueryResult result = PersistenceHelper.manager.find(query);
			int sum = 0;
			while (result.hasMoreElements()) {
				Object[] obj = (Object[]) result.nextElement();
				Task task = (Task) obj[0];
				sum += task.getProgress();
			}

			if (result.size() != 0 && sum != 0) {

				int comp = sum / result.size();
				tt.setProgress(comp);

				if (tt.getStartDate() == null) {
					tt.setStartDate(DateUtils.getCurrentTimestamp());
				}

				tt.setState(TaskStateVariable.INWORK);

				if (comp == 100) {
					// 실제 완료일이 매번 변경안되도록
					if (tt.getEndDate() == null) {
						tt.setEndDate(DateUtils.getCurrentTimestamp());
					}
					// 어차피 100일 경우 무조건 완료
					tt.setState(TaskStateVariable.COMPLETE);
				}
				PersistenceHelper.manager.modify(tt);
			}
		}

	}

	/**
	 * 기계 전기 진행율
	 */
	public int getTaskProgress(Project project, String taskType) throws Exception {
		int progress = 0;
		ArrayList<Task> list = recurciveTask(project, new String[] { taskType });
		int sumAllocate = 0;
		int sumProgress = 0;
		// 모든 태스크 수집
		for (Task task : list) {
			int allocate = task.getAllocate() != null ? task.getAllocate() : 0;
			int tprogress = task.getProgress() != null ? task.getProgress() : 0;
			sumProgress += (allocate * tprogress);
			sumAllocate += allocate;
			// 기계 진행율 = SUM(기계Task 각각의 할당률xTask 진행률)/SUM(기계Task 각각의 할당률)
		}
		if (sumAllocate != 0) {
			progress = sumProgress / sumAllocate;
		}
		return progress;
	}

	/**
	 * 객체와 연관된 작번을 가져오는 함수 객체로 구분해서 처리
	 */
	public JSONArray jsonAuiProject(String oid) throws Exception {
		Persistable per = CommonUtils.getObject(oid);

		// 작업 지시서
		if (per instanceof WorkOrder) {
			return WorkOrderHelper.manager.jsonAuiProject(oid);
			// 수배표
		} else if (per instanceof PartListMaster) {
			return PartlistHelper.manager.jsonAuiProject(oid);
			// 회의록
		} else if (per instanceof Meeting) {
			return MeetingHelper.manager.jsonAuiProject(oid);
			// TBOM
		} else if (per instanceof TBOMMaster) {
			return TBOMHelper.manager.jsonAuiProject(oid);
			// 의뢰서
		} else if (per instanceof RequestDocument) {
			return RequestDocumentHelper.manager.jsonAuiProject(oid);
			// 도면
		} else if (per instanceof EPMDocument) {
			return EpmHelper.manager.jsonAuiProject(oid);
		} else if (per instanceof Output) {
			// 산출물
			return OutputHelper.manager.jsonAuiProject(oid);
		} else if (per instanceof WTDocument) {
			// 문서
			return DocumentHelper.manager.jsonAuiProject(oid);
		} else if (per instanceof ConfigSheet) {
			// CONFIG SHEET
			return ConfigSheetHelper.manager.jsonAuiProject(oid);
		} else if (per instanceof Issue) {
			// 특이사항
			return IssueHelper.manager.jsonAuiProject(oid);
		} else if (per instanceof PRJDocument) {
			// 문서
			return DocumentHelper.manager.jsonAuiProject(oid);
		}
		return new JSONArray();
	}

	/**
	 * 특정 객체가 사용된 작번 확인
	 */
	public JSONArray jsonAuiReferenceProject(String oid) throws Exception {
		Persistable per = CommonUtils.getObject(oid);

		if (per instanceof KePart) {
			return KePartHelper.manager.jsonAuiReferenceProject(oid);
		} else if (per instanceof KeDrawing) {
			return KeDrawingHelper.manager.jsonAuiReferenceProject(oid);
		}
		return new JSONArray();
	}

	/**
	 * 참조 작번
	 */
	public ArrayList<Project> getReferenceBy(Project project) throws Exception {
		ArrayList<Project> list = new ArrayList<Project>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Project.class, true);
		QuerySpecUtils.toLikeRightAnd(query, idx, Project.class, Project.KEK_NUMBER, project.getKekNumber());
		QuerySpecUtils.toNotEqualsAnd(query, idx, Project.class, "projectTypeReference.key.id", project);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Project p = (Project) obj[0];
			list.add(p);
		}
		return list;
	}

	/**
	 * 메인페이지 나의 작번 리스트
	 */
	public JSONArray firstPageData(WTUser sessionUser) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Project.class, true);
		int idx_link = query.appendClassList(ProjectUserLink.class, false);
		int idx_u = query.appendClassList(WTUser.class, false);

		QuerySpecUtils.toInnerJoin(query, Project.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, WTUser.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_u, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, ProjectUserLink.class, "roleBObjectRef.key.id", sessionUser);

		query.appendAnd();
		query.appendOpenParen();

		CommonCode machineCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.MACHINE, "USER_TYPE");
		SearchCondition sc = new SearchCondition(ProjectUserLink.class, "userTypeReference.key.id", "=",
				machineCode.getPersistInfo().getObjectIdentifier().getId());
		query.appendWhere(sc, new int[] { idx_link });
		query.appendOr();

		CommonCode elecCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.ELEC, "USER_TYPE");
		sc = new SearchCondition(ProjectUserLink.class, "userTypeReference.key.id", "=",
				elecCode.getPersistInfo().getObjectIdentifier().getId());
		query.appendWhere(sc, new int[] { idx_link });
		query.appendOr();

		CommonCode softCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.SOFT, "USER_TYPE");
		sc = new SearchCondition(ProjectUserLink.class, "userTypeReference.key.id", "=",
				softCode.getPersistInfo().getObjectIdentifier().getId());
		query.appendWhere(sc, new int[] { idx_link });

		query.appendCloseParen();

		QuerySpecUtils.toOrderBy(query, idx, Project.class, Project.P_DATE, true);

		QueryResult result = PagingSessionHelper.openPagingSession(0, 30, query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Project project = (Project) obj[0];

			Map<String, String> map = new HashMap<>();
			map.put("oid", project.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("kekNumber", project.getKekNumber());
			map.put("keNumber", project.getKeNumber());
			map.put("mak", project.getMak() != null ? project.getMak().getName() : "");
			map.put("detail", project.getDetail() != null ? project.getDetail().getName() : "");
			map.put("customer", project.getCustomer() != null ? project.getCustomer().getName() : "");
			map.put("install", project.getInstall() != null ? project.getInstall().getName() : "");
			map.put("description", project.getDescription());
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 작번 선택후 결재받을 내가 작성한 모든 산출물
	 */
	public JSONArray registerData(Project project) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		// configsheet
		WTUser sessionUser = CommonUtils.sessionUser();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Output.class, true);
		int idx_link = query.appendClassList(OutputProjectLink.class, false);
		int idx_p = query.appendClassList(Project.class, false);

		QuerySpecUtils.toInnerJoin(query, Output.class, OutputProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, Project.class, OutputProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_p, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, OutputProjectLink.class, "roleBObjectRef.key.id", project);
		QuerySpecUtils.toCreator(query, idx, Output.class,
				sessionUser.getPersistInfo().getObjectIdentifier().getStringValue());

		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Output output = (Output) obj[0];
			Task task = output.getTask();
			LifeCycleManaged lcm = output.getDocument();

			// 작업 중 상태만
			if (!lcm.getLifeCycleState().toString().equals("INWORK")) {
				continue;
			}

			Map<String, Object> map = new HashMap<>();
			// configsheet
			map.put("taskName", task.getName());
			map.put("oid", output.getPersistInfo().getObjectIdentifier().getStringValue());
			if (lcm instanceof ConfigSheet) {
				ConfigSheet configSheet = (ConfigSheet) lcm;
//				map.put("oid", configSheet.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("type", "CONFIG SHEET");
				map.put("name", configSheet.getName());
				map.put("state", configSheet.getLifeCycleState().getDisplay());
				map.put("creator", configSheet.getCreatorFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(configSheet.getCreateTimestamp()));
				map.put("version", configSheet.getVersion());
				map.put("primary", AUIGridUtils.primaryTemplate(configSheet));
				map.put("secondary", AUIGridUtils.secondaryTemplate(configSheet));
				// tbom
			} else if (lcm instanceof TBOMMaster) {
				TBOMMaster master = (TBOMMaster) lcm;
//				map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("type", "T-BOM");
				map.put("name", master.getName());
				map.put("state", master.getLifeCycleState().getDisplay());
				map.put("creator", master.getCreatorFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
				map.put("version", master.getVersion());
				map.put("primary", AUIGridUtils.primaryTemplate(master));
				map.put("secondary", AUIGridUtils.secondaryTemplate(master));
				// workorder
			} else if (lcm instanceof WorkOrder) {
				WorkOrder workOrder = (WorkOrder) lcm;
//				map.put("oid", workOrder.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("type", "도면일람표");
				map.put("name", workOrder.getName());
				map.put("state", workOrder.getLifeCycleState().getDisplay());
				map.put("creator", workOrder.getCreatorFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(workOrder.getCreateTimestamp()));
				map.put("version", workOrder.getVersion());
				map.put("primary", AUIGridUtils.primaryTemplate(workOrder));
				map.put("secondary", AUIGridUtils.secondaryTemplate(workOrder));
				// document
			} else if (lcm instanceof WTDocument) {
				WTDocument document = (WTDocument) lcm;
//				map.put("oid", document.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("type", "산출물");
				map.put("name", document.getName());
				map.put("state", document.getLifeCycleState().getDisplay());
				map.put("creator", document.getCreatorFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(document.getCreateTimestamp()));
				map.put("version", document.getVersionIdentifier().getSeries().getValue());
				map.put("primary", AUIGridUtils.primaryTemplate(document));
				map.put("secondary", AUIGridUtils.secondaryTemplate(document));
				// partlist
			} else if (lcm instanceof PartListMaster) {
				PartListMaster master = (PartListMaster) lcm;
//				map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("type", "수배표");
				map.put("name", master.getName());
				map.put("state", master.getLifeCycleState().getDisplay());
				map.put("creator", master.getCreatorFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
				map.put("version", master.getVersion());
				map.put("primary", AUIGridUtils.primaryTemplate(master));
				map.put("secondary", AUIGridUtils.secondaryTemplate(master));
			}

			list.add(map);
		}

		return JSONArray.fromObject(list);
	}

	public String loadGanttProject(Map<String, Object> param) throws Exception {
		String oid = (String) param.get("oid");
		ReferenceFactory rf = new ReferenceFactory();
		Project project = (Project) rf.getReference(oid).getObject();

		ArrayList<Task> list = new ArrayList<Task>();

		list = ProjectHelper.manager.getterProjectTask(project, list);
		// list = ProjectHelper.manager.getterProjectNonSchduleTask(project, list);

		// 프로젝트 추가

		StringBuffer gantt = new StringBuffer();

		gantt.append("{\"data\": [");

		// project
		gantt.append("{");

		gantt.append("\"id\": \"" + project.getPersistInfo().getObjectIdentifier().getStringValue() + "\",");
		gantt.append("\"type\": \"project\",");
		gantt.append("\"isNew\": \"false\",");
		gantt.append("\"start_date\": \"" + DateUtils.formatTime(project.getPlanStartDate()) + "\",");
		gantt.append("\"end_date\": \"" + DateUtils.formatTime(project.getPlanEndDate()) + "\",");
		gantt.append("\"real_start_date\": \"" + DateUtils.formatTime(project.getStartDate()) + "\",");
		gantt.append("\"real_end_date\": \"" + DateUtils.formatTime(project.getEndDate()) + "\",");
		gantt.append(
				"\"duration\": " + DateUtils.getDuration(project.getPlanStartDate(), project.getPlanEndDate()) + ",");

		int state = ProjectHelper.manager.ganttState(project);
		// state
		gantt.append("\"state\": \"" + state + "\",");
		gantt.append("\"text\": \"" + project.getKekNumber() + "-" + project.getProjectType().getName() + "\",");
//		gantt.append("\"text\": \"" + project.getKekNumber() + "-" + project.getPType() + "\",");

		float progress = (float) getKekProgress(project) / 100;

		gantt.append("\"progress\": \"" + StringUtils.numberFormat(progress, "#.##") + "\",");
		gantt.append("\"taskType\": \"\",");
		gantt.append("\"parent\": \"0\",");

		System.out.println("list=" + list.size());

		if (list.size() == 0) {
			gantt.append("\"open\": false");
			gantt.append("}");
		} else if (list.size() > 0) {
			gantt.append("\"open\": true");
			gantt.append("},");

			for (int i = 0; i < list.size(); i++) {
				Task tt = (Task) list.get(i);

				gantt.append("{");
				gantt.append("\"id\": \"" + tt.getPersistInfo().getObjectIdentifier().getStringValue() + "\",");
				gantt.append("\"text\": \"" + tt.getName() + "\",");

				boolean hasChild = false;
				QueryResult result = PersistenceHelper.manager.navigate(tt, "childTask", ParentTaskChildTaskLink.class);
				if (result.size() > 0) {
					hasChild = true;
				}

				if (hasChild) {
					gantt.append("\"type\": \"project\",");
				} else {
					gantt.append("\"type\": \"task\",");
				}

				gantt.append("\"isNew\": \"false\",");
				gantt.append("\"start_date\": \"" + DateUtils.formatTime(tt.getPlanStartDate()) + "\",");
				gantt.append("\"end_date\": \"" + DateUtils.formatTime(tt.getPlanEndDate()) + "\",");
				gantt.append("\"real_start_date\": \"" + DateUtils.formatTime(tt.getStartDate()) + "\",");
				gantt.append("\"real_end_date\": \"" + DateUtils.formatTime(tt.getEndDate()) + "\",");
				gantt.append("\"taskType\": \"" + tt.getTaskType().getName() + "\",");
				int tstate = ProjectHelper.manager.ganttState(tt);
				gantt.append("\"state\": \"" + tstate + "\",");
				gantt.append("\"allocate\": \"" + tt.getAllocate() + "\",");

				float tprogress = (float) tt.getProgress() / 100;
				gantt.append("\"progress\": \"" + StringUtils.numberFormat(tprogress, "#.##") + "\",");
				gantt.append(
						"\"duration\": \"" + DateUtils.getDuration(tt.getPlanStartDate(), tt.getPlanEndDate()) + "\",");
				if ((list.size() - 1) == i) {
					if (StringUtils.isNull(tt.getParentTask())) {
						gantt.append("\"parent\": \"" + project.getPersistInfo().getObjectIdentifier().getStringValue()
								+ "\",");
						gantt.append("\"open\": true");
					} else {
						gantt.append("\"parent\": \""
								+ tt.getParentTask().getPersistInfo().getObjectIdentifier().getStringValue() + "\",");
						gantt.append("\"open\": true");
					}
					gantt.append("}");
				} else {
					if (StringUtils.isNull(tt.getParentTask())) {
						gantt.append("\"parent\": \"" + project.getPersistInfo().getObjectIdentifier().getStringValue()
								+ "\",");
						gantt.append("\"open\": true");
					} else {
						gantt.append("\"parent\": \""
								+ tt.getParentTask().getPersistInfo().getObjectIdentifier().getStringValue() + "\",");
						gantt.append("\"open\": true");
					}
					gantt.append("},");
				}
			}
		}

		gantt.append("],");

		gantt.append("\"links\": [");

		ArrayList<TargetTaskSourceTaskLink> linkList = getAllTargetList(list);

		for (int i = 0; i < linkList.size(); i++) {
			TargetTaskSourceTaskLink link = (TargetTaskSourceTaskLink) linkList.get(i);
			gantt.append("{");
			gantt.append("\"id\": \"" + link.getPersistInfo().getObjectIdentifier().getStringValue() + "\",");
			gantt.append("\"source\": \"" + link.getTargetTask().getPersistInfo().getObjectIdentifier().getStringValue()
					+ "\",");
			gantt.append("\"target\": \"" + link.getSourceTask().getPersistInfo().getObjectIdentifier().getStringValue()
					+ "\",");

			gantt.append("\"lag\": \"" + link.getLag() + "\",");
			gantt.append("\"type\": \"0\",");

			if ((linkList.size() - 1) == i) {
				gantt.append("}");
			} else {
				gantt.append("},");
			}
		}

		gantt.append("]");
		gantt.append("}");

		return gantt.toString();
	}

	public ArrayList<Task> getterProjectTask2(Project project, ArrayList<Task> list) throws Exception {

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
			getterTasks(t, project, list);
		}
		return list;
	}

	public ArrayList<Task> getterProjectTask(Project project, ArrayList<Task> list) throws Exception {

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);
		long ids = project.getPersistInfo().getObjectIdentifier().getId();
		SearchCondition sc = new SearchCondition(Task.class, "projectReference.key.id", "=", ids);
		query.appendWhere(sc, new int[] { idx });
		query.appendAnd();

		sc = new SearchCondition(Task.class, "parentTaskReference.key.id", "=", 0L);
		query.appendWhere(sc, new int[] { idx });
//		query.appendAnd();
//
//		sc = new SearchCondition(Task.class, Task.DEPTH, "=", 1);
//		query.appendWhere(sc, new int[] { idx });

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
			getterTasks(t, project, list);
		}
		return list;
	}

	public void getterTasks(Task parentTask, Project project, ArrayList<Task> list) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);

		long ids = parentTask.getPersistInfo().getObjectIdentifier().getId();
		long pids = project.getPersistInfo().getObjectIdentifier().getId();

		SearchCondition sc = new SearchCondition(Task.class, "parentTaskReference.key.id", "=", ids);
		query.appendWhere(sc, new int[] { idx });
		query.appendAnd();

		sc = new SearchCondition(Task.class, "projectReference.key.id", "=", pids);
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
			getterTasks(t, project, list);
		}
	}

	private ArrayList<TargetTaskSourceTaskLink> getAllTargetList(ArrayList<Task> list) throws Exception {
		ArrayList<TargetTaskSourceTaskLink> lists = new ArrayList<TargetTaskSourceTaskLink>();
		for (int i = 0; i < list.size(); i++) {
			Task tt = (Task) list.get(i);

			QueryResult result = PersistenceHelper.manager.navigate(tt, "targetTask", TargetTaskSourceTaskLink.class,
					false);

			while (result.hasMoreElements()) {
				TargetTaskSourceTaskLink link = (TargetTaskSourceTaskLink) result.nextElement();
				lists.add(link);
			}
		}
		return lists;
	}

	private int ganttState(Project project) throws Exception {
		int ganttState = 4; // 준비중
		String state = project.getState();

		if ("COMPLETE".equals(state)) {
			ganttState = 5;
		} else {
			int comp = (int) ProjectHelper.getPreferComp(project);
			int gap = project.getProgress() - comp;
			if (gap < 0 && gap >= -30) {
				ganttState = 1;
			} else if (gap < -30) {
				ganttState = 0;
			} else if (gap > 0) {
				ganttState = 2;
			}
		}
		return ganttState;
	}

	private int ganttState(Task tt) throws Exception {
		boolean isNormalTask = tt.getTaskType().toString().equals("일반") || tt.getAllocate() == 0;
		int ganttState = 4; // 준비중
		String state = tt.getState();
		if (isNormalTask) {
			QueryResult result = PersistenceHelper.manager.navigate(tt, "output", OutputTaskLink.class);
			if (result.size() > 0 || tt.getProgress() == 100) {
				ganttState = 5;
			} else if (tt.getProgress() != 100) {
				ganttState = 4;
			}
		} else {

			if ("COMPLETE".equals(state)) {
				ganttState = 5;
			} else {
				int comp = (int) ProjectHelper.getPreferComp(tt);
				int gap = tt.getProgress() - comp;
				if (gap < 0 && gap >= -30) {
					ganttState = 1;
				} else if (gap < -30) {
					ganttState = 0;
				} else if (gap > 0) {
					ganttState = 2;
				}
			}
		}
		return ganttState;
	}

	public int gate1StateIcon(Project project) throws Exception {
		int gate1 = ProjectGateState.GATE_NO_START;
		// 태스크 이름으로 처리
		ArrayList<Task> list = new ArrayList<Task>();

		list = getterProjectTask2(project, list);

		Timestamp end = null;
		Timestamp start = null;

		boolean isStart = false; // 시작 여부

		int totalProgress = 0;

		for (Task task : list) {

			String name = task.getName();

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
	}

	public int gate2StateIcon(Project project) throws Exception {
		int gate2 = ProjectGateState.GATE_NO_START;
		// 태스크 이름으로 처리
		ArrayList<Task> list = new ArrayList<Task>();

		list = getterProjectTask2(project, list);

		Timestamp end = null;
		Timestamp start = null;

		boolean isStart = false; // 시작 여부

		int totalProgress = 0;

		for (Task task : list) {

			String name = task.getName();

			for (String ss : GATE2) {

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
			int perDay = result.intValue(); // 2??

			int tdu = DateUtils.getDuration(end, DateUtils.getCurrentTimestamp()); // 1...

			if (tdu <= perDay) {
				isCheckDay = true;
			}
		}

		int per = totalProgress / GATE2.length;
		System.out.println("isStart2=" + isStart);
		System.out.println("totalProgress2=" + totalProgress);
		if (!isStart) {
			if (isOverDay) {
				gate2 = ProjectGateState.GATE_DELAY;
			} else {
				gate2 = ProjectGateState.GATE_NO_START;
			}
		} else {
			// 진행중이고 완료가 아닐대...
			if (isOverDay) {
				if (per != 100) {
					gate2 = ProjectGateState.GATE_DELAY_PROGRESS;
				} else if (per >= 51 && per < 100) {
					gate2 = ProjectGateState.GATE_PROGRESS;
				} else if (per == 100) {
					gate2 = ProjectGateState.GATE_COMPLETE;
				}
			} else {
				// 진행 중이지만..

				if (isCheckDay) {
					if (per < 50) {
						gate2 = ProjectGateState.GATE_DELAY_PROGRESS;
					} else if (per >= 51 && per < 100) {
						gate2 = ProjectGateState.GATE_PROGRESS;
					} else if (per == 100) {
						gate2 = ProjectGateState.GATE_COMPLETE;
					}
				} else {
					gate2 = ProjectGateState.GATE_PROGRESS;
				}
			}
		}
		return gate2;
	}

	public int gate3StateIcon(Project project) throws Exception {
		int gate3 = ProjectGateState.GATE_NO_START;
		// 태스크 이름으로 처리
		ArrayList<Task> list = new ArrayList<Task>();

		list = getterProjectTask2(project, list);

		Timestamp end = null;
		Timestamp start = null;

		boolean isStart = false; // 시작 여부

		int totalProgress = 0;

		for (Task task : list) {

			String name = task.getName();

			for (String ss : GATE3) {

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
			int perDay = result.intValue(); // 2??

			int tdu = DateUtils.getDuration(end, DateUtils.getCurrentTimestamp()); // 1...

			if (tdu <= perDay) {
				isCheckDay = true;
			}
		}
		int per = totalProgress / GATE3.length;
		System.out.println("isStart3=" + isStart);
		System.out.println("totalProgress3=" + totalProgress);
		if (!isStart) {
			if (isOverDay) {
				gate3 = ProjectGateState.GATE_DELAY;
			} else {
				gate3 = ProjectGateState.GATE_NO_START;
			}
		} else {
			// 진행중이고 완료가 아닐대...
			if (isOverDay) {
				if (per != 100) {
					gate3 = ProjectGateState.GATE_DELAY_PROGRESS;
				} else if (per >= 51 && per < 100) {
					gate3 = ProjectGateState.GATE_PROGRESS;
				} else if (per == 100) {
					gate3 = ProjectGateState.GATE_COMPLETE;
				}
			} else {
				// 진행 중이지만..

				if (isCheckDay) {
					if (per < 50) {
						gate3 = ProjectGateState.GATE_DELAY_PROGRESS;
					} else if (per >= 51 && per < 100) {
						gate3 = ProjectGateState.GATE_PROGRESS;
					} else if (per == 100) {
						gate3 = ProjectGateState.GATE_COMPLETE;
					}
				} else {
					gate3 = ProjectGateState.GATE_PROGRESS;
				}
			}
		}
		return gate3;
	}

	public int gate4StateIcon(Project project) throws Exception {
		int gate4 = ProjectGateState.GATE_NO_START;
//		StringBuffer sb = new StringBuffer();

		// 태스크 이름으로 처리
		ArrayList<Task> list = new ArrayList<Task>();

		list = getterProjectTask2(project, list);

		Timestamp end = null;
		Timestamp start = null;

		boolean isStart = false; // 시작 여부

		int totalProgress = 0;

		for (Task task : list) {

			String name = task.getName();

			for (String ss : GATE4) {

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
			int perDay = result.intValue(); // 2??

			int tdu = DateUtils.getDuration(end, DateUtils.getCurrentTimestamp()); // 1...

			if (tdu <= perDay) {
				isCheckDay = true;
			}
		}
		int per = totalProgress / GATE4.length;
		System.out.println("isStart4=" + isStart);
		System.out.println("totalProgress4=" + totalProgress);
		if (!isStart) {
			if (isOverDay) {
				gate4 = ProjectGateState.GATE_DELAY;
			} else {
				gate4 = ProjectGateState.GATE_NO_START;
			}
		} else {
			// 진행중이고 완료가 아닐대...
			if (isOverDay) {
				if (per != 100) {
					gate4 = ProjectGateState.GATE_DELAY_PROGRESS;
				} else if (per >= 51 && per < 100) {
					gate4 = ProjectGateState.GATE_PROGRESS;
				} else if (per == 100) {
					gate4 = ProjectGateState.GATE_COMPLETE;
				}
			} else {
				// 진행 중이지만..

				if (isCheckDay) {
					if (per < 50) {
						gate4 = ProjectGateState.GATE_DELAY_PROGRESS;
					} else if (per >= 51 && per < 100) {
						gate4 = ProjectGateState.GATE_PROGRESS;
					} else if (per == 100) {
						gate4 = ProjectGateState.GATE_COMPLETE;
					}
				} else {
					gate4 = ProjectGateState.GATE_PROGRESS;
				}
			}
		}
		return gate4;
	}

	public int gate5StateIcon(Project project) throws Exception {
		int gate5 = ProjectGateState.GATE_NO_START;
//		StringBuffer sb = new StringBuffer();

		// 태스크 이름으로 처리
		ArrayList<Task> list = new ArrayList<Task>();

		list = getterProjectTask2(project, list);

		Timestamp end = null;
		Timestamp start = null;

		boolean isStart = false; // 시작 여부

		int totalProgress = 0;

		for (Task task : list) {

			String name = task.getName();
			System.out.println("gate5=" + name);
			for (String ss : GATE5) {

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
			int perDay = result.intValue(); // 2??

			int tdu = DateUtils.getDuration(end, DateUtils.getCurrentTimestamp()); // 1...

			if (tdu <= perDay) {
				isCheckDay = true;
			}
		}
		int per = totalProgress / GATE5.length;
		System.out.println("isStart5=" + isStart);
		System.out.println("totalProgress5=" + totalProgress);
		if (!isStart) {
			if (isOverDay) {
				gate5 = ProjectGateState.GATE_DELAY;
			} else {
				gate5 = ProjectGateState.GATE_NO_START;
			}
		} else {
			// 진행중이고 완료가 아닐대...
			if (isOverDay) {
				if (per != 100) {
					gate5 = ProjectGateState.GATE_DELAY_PROGRESS;
				} else if (per >= 51 && per < 100) {
					gate5 = ProjectGateState.GATE_DELAY_PROGRESS;
				} else if (per == 100) {
					gate5 = ProjectGateState.GATE_COMPLETE;
				}
			} else {
				// 진행 중이지만..

				if (isCheckDay) {
					if (per < 50) {
						gate5 = ProjectGateState.GATE_DELAY_PROGRESS;
					} else if (per >= 51 && per < 100) {
						gate5 = ProjectGateState.GATE_PROGRESS;
					} else if (per == 100) {
						gate5 = ProjectGateState.GATE_COMPLETE;
					}
				} else {
					gate5 = ProjectGateState.GATE_PROGRESS;
				}
			}
		}
		return gate5;
	}

	public int getQState(Task tt) throws Exception {
		int qstate = ProjectGateState.GATE_NO_START;

		if (tt.getState() != null) {
			// 완료됨
			if (tt.getState().equals("완료됨")) {
				qstate = ProjectGateState.GATE_COMPLETE;
			} else if (tt.getState().equals("준비중")) {
				qstate = ProjectGateState.GATE_NO_START;
			} else {

				Timestamp today = DateUtils.getCurrentTimestamp();
				int du = DateUtils.getDuration(tt.getPlanStartDate(), tt.getPlanEndDate());
				BigDecimal counting = new BigDecimal(du);
				BigDecimal multi = new BigDecimal(0.2);

				BigDecimal result = counting.multiply(multi);
				int perDay = Math.round(result.floatValue()); // 기간의 20%
				int tdu = DateUtils.getDuration(tt.getPlanEndDate(), DateUtils.getCurrentTimestamp()); // 1...

				if (tt.getPlanEndDate() != null && today.getTime() > tt.getPlanEndDate().getTime()) {
					qstate = ProjectGateState.GATE_DELAY;
				} else {
					// 오늘 기준으로 작아 졌을떄 체크
					if (tdu <= perDay) {
						if (tt.getProgress() < 50) {
							qstate = ProjectGateState.GATE_DELAY_PROGRESS;
						} else if (tt.getProgress() >= 51 && tt.getProgress() < 100) {
							qstate = ProjectGateState.GATE_PROGRESS;
						} else if (tt.getProgress() == 100) {
							qstate = ProjectGateState.GATE_COMPLETE;
						}
					} else {
						qstate = ProjectGateState.GATE_PROGRESS;
					}
				}
			}
		}
		return qstate;
	}

	public JSONArray jsonAuiOutput(String poid, String toid, boolean _latest) throws Exception {
		// true = latest
		Project project = (Project) CommonUtils.getObject(poid);
		Task task = (Task) CommonUtils.getObject(toid);
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Output.class, true); // ... inner join 필요 없을..
		QuerySpecUtils.toEqualsAnd(query, idx, Output.class, "taskReference.key.id", task);
		QuerySpecUtils.toEqualsAnd(query, idx, Output.class, "projectReference.key.id", project);
		QuerySpecUtils.toOrderBy(query, idx, Output.class, Output.CREATE_TIMESTAMP, true);

//		 QuerySpecUtils.toLatest(query, idx, Output.class);

		QueryResult result = PersistenceHelper.manager.find(query);
		System.out.println("_latest=" + _latest);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Output output = (Output) obj[0];
			LifeCycleManaged lcm = output.getDocument();

			Map<String, String> map = new HashMap<>();
			map.put("ooid", output.getPersistInfo().getObjectIdentifier().getStringValue());

			if (lcm == null) {
				E3PSDocumentMaster mm = output.getMaster();
				if (mm != null) {
					RevisionControlled rc = CommonUtils.getLatestObject(mm);
					map.put("oid", rc.getPersistInfo().getObjectIdentifier().getStringValue());
//					map.put("type", "산출물(구)");
					map.put("name", rc.getName());
					map.put("state", rc.getLifeCycleState().getDisplay());
					map.put("creator", rc.getCreatorFullName());
					map.put("createdDate_txt", CommonUtils.getPersistableTime(rc.getCreateTimestamp()));
					map.put("version", rc.getVersionIdentifier().getSeries().getValue() + "."
							+ rc.getIterationIdentifier().getSeries().getValue());
					map.put("primary", AUIGridUtils.primaryTemplate((ContentHolder) rc));
					map.put("secondary", AUIGridUtils.secondaryTemplate((ContentHolder) rc));
				}
			} else {

				if (lcm instanceof WTDocument) {
					WTDocument document = (WTDocument) lcm;

					if (_latest) {
						if (CommonUtils.isLatestVersion(lcm)) {
							map.put("oid", document.getPersistInfo().getObjectIdentifier().getStringValue());
							map.put("name", document.getName());
							map.put("version", document.getVersionIdentifier().getSeries().getValue() + "."
									+ document.getIterationIdentifier().getSeries().getValue());
							map.put("state", document.getLifeCycleState().getDisplay());
							map.put("creator", document.getCreatorFullName());
							map.put("createdDate_txt", CommonUtils.getPersistableTime(document.getCreateTimestamp()));
							map.put("primary", AUIGridUtils.primaryTemplate(document));
							map.put("secondary", AUIGridUtils.secondaryTemplate(document));
						} else {
							continue;
						}
					} else {
						map.put("oid", document.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("name", document.getName());
						map.put("version", document.getVersionIdentifier().getSeries().getValue() + "."
								+ document.getIterationIdentifier().getSeries().getValue());
						map.put("state", document.getLifeCycleState().getDisplay());
						map.put("creator", document.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(document.getCreateTimestamp()));
						map.put("primary", AUIGridUtils.primaryTemplate(document));
						map.put("secondary", AUIGridUtils.secondaryTemplate(document));
					}
				} else if (lcm instanceof Meeting) {
					if (_latest) {
						if (CommonUtils.isLatestVersion(lcm)) {
							Meeting meeting = (Meeting) lcm;
							map.put("oid", meeting.getPersistInfo().getObjectIdentifier().getStringValue());
							map.put("name", meeting.getName());
							map.put("state", meeting.getLifeCycleState().getDisplay());
							map.put("creator", meeting.getCreatorFullName());
							map.put("createdDate_txt", CommonUtils.getPersistableTime(meeting.getCreateTimestamp()));
							map.put("primary", AUIGridUtils.primaryTemplate(meeting));
							map.put("secondary", AUIGridUtils.secondaryTemplate(meeting));
						} else {
							continue;
						}
					} else {
						Meeting meeting = (Meeting) lcm;
						map.put("oid", meeting.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("name", meeting.getName());
						map.put("state", meeting.getLifeCycleState().getDisplay());
						map.put("creator", meeting.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(meeting.getCreateTimestamp()));
						map.put("primary", AUIGridUtils.primaryTemplate(meeting));
						map.put("secondary", AUIGridUtils.secondaryTemplate(meeting));
					}
				} else if (lcm instanceof PartListMaster) {
					PartListMaster master = (PartListMaster) lcm;
					map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
					map.put("name", master.getName());
					map.put("state", master.getLifeCycleState().getDisplay());
					map.put("creator", master.getCreatorFullName());
					map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
					map.put("primary", AUIGridUtils.primaryTemplate(master));
					map.put("secondary", AUIGridUtils.secondaryTemplate(master));
				} else if (lcm instanceof RequestDocument) {
					if (_latest) {
						if (CommonUtils.isLatestVersion(lcm)) {
							RequestDocument requestDocument = (RequestDocument) lcm;
							map.put("oid", requestDocument.getPersistInfo().getObjectIdentifier().getStringValue());
							map.put("name", requestDocument.getName());
							map.put("state", requestDocument.getLifeCycleState().getDisplay());
							map.put("creator", requestDocument.getCreatorFullName());
							map.put("createdDate_txt",
									CommonUtils.getPersistableTime(requestDocument.getCreateTimestamp()));
							map.put("primary", AUIGridUtils.primaryTemplate(requestDocument));
							map.put("secondary", AUIGridUtils.secondaryTemplate(requestDocument));
						} else {
							continue;
						}
					} else {
						RequestDocument requestDocument = (RequestDocument) lcm;
						map.put("oid", requestDocument.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("name", requestDocument.getName());
						map.put("state", requestDocument.getLifeCycleState().getDisplay());
						map.put("creator", requestDocument.getCreatorFullName());
						map.put("createdDate_txt",
								CommonUtils.getPersistableTime(requestDocument.getCreateTimestamp()));
						map.put("primary", AUIGridUtils.primaryTemplate(requestDocument));
						map.put("secondary", AUIGridUtils.secondaryTemplate(requestDocument));
					}
				} else if (lcm instanceof WorkOrder) {
					if (_latest) {
						WorkOrder workOrder = (WorkOrder) lcm;
						if (workOrder.getLatest()) {
							map.put("oid", workOrder.getPersistInfo().getObjectIdentifier().getStringValue());
							map.put("name", workOrder.getName());
							map.put("state", workOrder.getLifeCycleState().getDisplay());
							map.put("creator", workOrder.getCreatorFullName());
							map.put("createdDate_txt", CommonUtils.getPersistableTime(workOrder.getCreateTimestamp()));
							map.put("primary", AUIGridUtils.primaryTemplate(workOrder));
							map.put("secondary", AUIGridUtils.secondaryTemplate(workOrder));
						} else {
							continue;
						}
					} else {
						WorkOrder workOrder = (WorkOrder) lcm;
						map.put("oid", workOrder.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("name", workOrder.getName());
						map.put("state", workOrder.getLifeCycleState().getDisplay());
						map.put("creator", workOrder.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(workOrder.getCreateTimestamp()));
						map.put("primary", AUIGridUtils.primaryTemplate(workOrder));
						map.put("secondary", AUIGridUtils.secondaryTemplate(workOrder));
					}
				} else if (lcm instanceof TBOMMaster) {
					if (_latest) {
						TBOMMaster master = (TBOMMaster) lcm;
						if (master.getLatest()) {
							map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
							map.put("name", master.getName());
							map.put("state", master.getLifeCycleState().getDisplay());
							map.put("creator", master.getCreatorFullName());
							map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
							map.put("primary", AUIGridUtils.primaryTemplate(master));
							map.put("secondary", AUIGridUtils.secondaryTemplate(master));
						} else {
							continue;
						}
					} else {
						TBOMMaster master = (TBOMMaster) lcm;
						map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("name", master.getName());
						map.put("state", master.getLifeCycleState().getDisplay());
						map.put("creator", master.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
						map.put("primary", AUIGridUtils.primaryTemplate(master));
						map.put("secondary", AUIGridUtils.secondaryTemplate(master));
					}
				} else if (lcm instanceof ConfigSheet) {
					if (_latest) {
						ConfigSheet configSheet = (ConfigSheet) lcm;
						if (configSheet.getLatest()) {
							map.put("oid", configSheet.getPersistInfo().getObjectIdentifier().getStringValue());
							map.put("name", configSheet.getName());
							map.put("state", configSheet.getLifeCycleState().getDisplay());
							map.put("creator", configSheet.getCreatorFullName());
							map.put("createdDate_txt",
									CommonUtils.getPersistableTime(configSheet.getCreateTimestamp()));
							map.put("primary", AUIGridUtils.primaryTemplate(configSheet));
							map.put("secondary", AUIGridUtils.secondaryTemplate(configSheet));
						} else {
							continue;
						}
					} else {
						ConfigSheet configSheet = (ConfigSheet) lcm;
						map.put("oid", configSheet.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("name", configSheet.getName());
						map.put("state", configSheet.getLifeCycleState().getDisplay());
						map.put("creator", configSheet.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(configSheet.getCreateTimestamp()));
						map.put("primary", AUIGridUtils.primaryTemplate(configSheet));
						map.put("secondary", AUIGridUtils.secondaryTemplate(configSheet));
					}
				}
			}
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * @메소드명 : 
	 * @최초 작성자 : 
	 * @최초 작성일 : 2024. 06. 24
	 * @설명 :
	 */
	public String getFirstMDate(Project p) throws Exception {
		String str = "";
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(PartListMaster.class, true);
		int idx_link = query.appendClassList(PartListMasterProjectLink.class, false);
		int idx_p = query.appendClassList(Project.class, false);

		QuerySpecUtils.toInnerJoin(query, PartListMaster.class, PartListMasterProjectLink.class,
				WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, Project.class, PartListMasterProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_p, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, PartListMasterProjectLink.class, "roleBObjectRef.key.id", p);
		QuerySpecUtils.toEqualsAnd(query, idx, PartListMaster.class, PartListMaster.ENG_TYPE, "기계_1차_수배");
		QuerySpecUtils.toOrderBy(query, idx, PartListMaster.class, PartListMaster.CREATE_TIMESTAMP, false);
		QueryResult qr = PersistenceHelper.manager.find(query);
		if (qr.hasMoreElements()) {
			Object[] obj = (Object[]) qr.nextElement();
			PartListMaster pm = (PartListMaster) obj[0];
			str = pm.getCreateTimestamp().toString().substring(0, 10);
		}
		return str;
	}

	/**
	 * 
	 * @메소드명 : 
	 * @최초 작성자 : 
	 * @최초 작성일 : 2024. 06. 24
	 * @설명 :
	 */
	public String getFirstEDate(Project p) throws Exception {
		String str = "";
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(PartListMaster.class, true);
		int idx_link = query.appendClassList(PartListMasterProjectLink.class, false);
		int idx_p = query.appendClassList(Project.class, false);

		QuerySpecUtils.toInnerJoin(query, PartListMaster.class, PartListMasterProjectLink.class,
				WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, Project.class, PartListMasterProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_p, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, PartListMasterProjectLink.class, "roleBObjectRef.key.id", p);
		QuerySpecUtils.toEqualsAnd(query, idx, PartListMaster.class, PartListMaster.ENG_TYPE, "전기_1차_수배");
		QuerySpecUtils.toOrderBy(query, idx, PartListMaster.class, PartListMaster.CREATE_TIMESTAMP, false);
		QueryResult qr = PersistenceHelper.manager.find(query);
		if (qr.hasMoreElements()) {
			Object[] obj = (Object[]) qr.nextElement();
			PartListMaster pm = (PartListMaster) obj[0];
			str = pm.getCreateTimestamp().toString().substring(0, 10);
		}
		return str;
	}
}
