package e3ps.project.template.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import e3ps.admin.commonCode.CommonCode;
import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.project.task.ParentTaskChildTaskLink;
import e3ps.project.task.TargetTaskSourceTaskLink;
import e3ps.project.task.Task;
import e3ps.project.task.service.TaskHelper;
import e3ps.project.template.Template;
import e3ps.project.template.TemplateUserLink;
import e3ps.project.template.dto.TemplateDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.org.WTUser;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;

public class TemplateHelper {

	public static final TemplateHelper manager = new TemplateHelper();
	public static final TemplateService service = ServiceFactory.getService(TemplateService.class);

	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		List<TemplateDTO> list = new ArrayList<TemplateDTO>();

		String name = (String) params.get("name");
		String duration = (String) params.get("duration");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");
		String modifierOid = (String) params.get("modifierOid");
		String modifiedFrom = (String) params.get("modifiedFrom");
		String modifiedTo = (String) params.get("modifiedTo");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Template.class, true);

		QuerySpecUtils.toLikeAnd(query, idx, Template.class, Template.NAME, name);
		QuerySpecUtils.toLikeAnd(query, idx, Template.class, Template.DURATION, duration);
		QuerySpecUtils.toCreator(query, idx, Template.class, creatorOid);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, Template.class, Template.CREATE_TIMESTAMP, createdFrom,
				createdTo);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, Template.class, Template.MODIFY_TIMESTAMP, modifiedFrom,
				modifiedTo);
		QuerySpecUtils.toBooleanAnd(query, idx, Template.class, Template.ENABLE, true);

		if (!StringUtils.isNull(modifierOid)) {
			if (query.getConditionCount() > 0) {
				query.appendAnd();
			}
			WTUser user = (WTUser) CommonUtils.getObject(modifierOid);
			SearchCondition sc = new SearchCondition(Template.class, "updateUser.owner.key.id", "=",
					user.getPersistInfo().getObjectIdentifier().getId());
			query.appendWhere(sc, new int[] { idx });
		}

		QuerySpecUtils.toOrderBy(query, idx, Template.class, Template.CREATE_TIMESTAMP, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Template template = (Template) obj[0];
			TemplateDTO column = new TemplateDTO(template);
			list.add(column);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	public ArrayList<HashMap<String, String>> getTemplateArrayMap() throws Exception {
		ArrayList<HashMap<String, String>> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Template.class, true);
		QuerySpecUtils.toBooleanAnd(query, idx, Template.class, Template.ENABLE, true);
		QuerySpecUtils.toOrderBy(query, idx, Template.class, Template.CREATE_TIMESTAMP, true);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			HashMap<String, String> map = new HashMap<>();
			Template template = (Template) obj[0];
			map.put("key", template.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("value", template.getName());
			list.add(map);
		}
		return list;
	}

	/**
	 * 템플릿 트리 가져오기
	 */
	public JSONArray load(String oid) throws Exception {
		Template template = (Template) CommonUtils.getObject(oid);
		JSONArray list = new JSONArray();
		JSONObject node = new JSONObject();
		node.put("oid", template.getPersistInfo().getObjectIdentifier().getStringValue());
		node.put("name", template.getName());
		node.put("description", template.getDescription());
		node.put("duration", template.getDuration());
		node.put("isNew", false);
		node.put("allocate", 0);
		node.put("taskType", "");
		node.put("type", "template");

		JSONArray childrens = new JSONArray();
		ArrayList<Task> taskList = TaskHelper.manager.getTemplateTasks(template);
		for (Task task : taskList) {
			JSONObject children = new JSONObject();
			children.put("oid", task.getPersistInfo().getObjectIdentifier().getStringValue());
			children.put("name", task.getName());
			children.put("description", task.getDescription());
			children.put("duration", task.getDuration());
			children.put("isNew", false);
			children.put("allocate", task.getAllocate() != null ? task.getAllocate() : 0);
			children.put("taskType", task.getTaskType() != null ? task.getTaskType().getCode() : "NORMAL");
			children.put("type", "task");
			load(children, template, task);
			childrens.add(children);
		}
		node.put("children", childrens);
		list.add(node);
		return list;
	}

	/**
	 * 템플릿 관련 태스트 가져오기
	 */
	private void load(JSONObject node, Template template, Task parentTask) throws Exception {
		JSONArray childrens = new JSONArray();
		ArrayList<Task> taskList = TaskHelper.manager.getTemplateTasks(template, parentTask);
		for (Task task : taskList) {
			JSONObject children = new JSONObject();
			children.put("oid", task.getPersistInfo().getObjectIdentifier().getStringValue());
			children.put("name", task.getName());
			children.put("description", task.getDescription());
			children.put("duration", task.getDuration());
			children.put("isNew", false);
			children.put("allocate", task.getAllocate() != null ? task.getAllocate() : 0);
			children.put("taskType", task.getTaskType() != null ? task.getTaskType().getCode() : "NORMAL");
			children.put("type", "task");
			load(children, template, task);
			childrens.add(children);
		}
		node.put("children", childrens);
	}

	/**
	 * 템플릿 유저 가져오기
	 */
	public WTUser getUserType(Template template, String code) throws Exception {
		CommonCode userTypeCode = CommonCodeHelper.manager.getCommonCode(code, "USER_TYPE");
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TemplateUserLink.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, TemplateUserLink.class, "roleAObjectRef.key.id", template);
		QuerySpecUtils.toEqualsAnd(query, idx, TemplateUserLink.class, "userTypeReference.key.id", userTypeCode);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TemplateUserLink link = (TemplateUserLink) obj[0];
			return link.getUser();
		}
		return null;
	}

	public ArrayList<Task> getSourceOrTarget(Task task, String reference) throws Exception {
		Template template = task.getTemplate();
		ArrayList<Task> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TargetTaskSourceTaskLink.class, true);

		if ("source".equals(reference)) {
			QuerySpecUtils.toEqualsAnd(query, idx, TargetTaskSourceTaskLink.class, "roleBObjectRef.key.id", task);
		} else if ("target".equals(reference)) {
			QuerySpecUtils.toEqualsAnd(query, idx, TargetTaskSourceTaskLink.class, "roleAObjectRef.key.id", task);
		}
		QuerySpecUtils.toEqualsAnd(query, idx, TargetTaskSourceTaskLink.class, "templateReference.key.id", template);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TargetTaskSourceTaskLink link = (TargetTaskSourceTaskLink) obj[0];
			if ("source".equals(reference)) {
				list.add(link.getSourceTask());
			} else if ("target".equals(reference)) {
				list.add(link.getTargetTask());
			}
		}
		return list;
	}

	/**
	 * 모든 템플릿 태스크 가져오기
	 */
	public ArrayList<Task> recurciveTask(Template template) throws Exception {
		ArrayList<Task> list = new ArrayList<Task>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);

		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "templateReference.key.id",
				template.getPersistInfo().getObjectIdentifier().getId());
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "parentTaskReference.key.id", 0L);
		QuerySpecUtils.toOrderBy(query, idx, Task.class, Task.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Task task = (Task) obj[0];
			list.add(task);
			recurciveTask(template, task, list);
		}
		return list;
	}

	/**
	 * 모든 템플릿 태스크 재귀함수
	 */
	private void recurciveTask(Template template, Task parentTask, ArrayList<Task> list) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);

		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "templateReference.key.id",
				template.getPersistInfo().getObjectIdentifier().getId());
		QuerySpecUtils.toEqualsAnd(query, idx, Task.class, "parentTaskReference.key.id", parentTask);
		QuerySpecUtils.toOrderBy(query, idx, Task.class, Task.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Task task = (Task) obj[0];
			list.add(task);
			recurciveTask(template, task, list);
		}
	}

	/**
	 * @메소드명 :
	 * @최초 작성자 :
	 * @최초 작성일 : 2024. 07. 12
	 * @설명 :
	 */
	public String loadGanttTemplate(Map<String, Object> param) throws Exception {
		String oid = (String) param.get("oid");
		ReferenceFactory rf = new ReferenceFactory();
		Template template = (Template) rf.getReference(oid).getObject();

		ArrayList<Task> list = new ArrayList<Task>();

		list = TemplateHelper.manager.getterTemplateTask(template, list);
		// list = ProjectHelper.manager.getterProjectNonSchduleTask(project, list);

		// 프로젝트 추가

		StringBuffer gantt = new StringBuffer();

		gantt.append("{\"data\": [");

		// project
		gantt.append("{");

		gantt.append("\"id\": \"" + template.getPersistInfo().getObjectIdentifier().getStringValue() + "\",");
		gantt.append("\"type\": \"project\",");
		gantt.append("\"isNew\": \"false\",");
		gantt.append("\"start_date\": \"" + DateUtils.formatTime(template.getPlanStartDate()) + "\",");
		gantt.append("\"end_date\": \"" + DateUtils.formatTime(template.getPlanEndDate()) + "\",");

//		gantt.append("\"state\": \"" + state + "\",");
		gantt.append("\"text\": \"" + template.getName() + "\",");

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
				gantt.append("\"allocate\": \"" + tt.getAllocate() + "\",");

				float tprogress = (float) tt.getProgress() / 100;
				gantt.append("\"progress\": \"" + StringUtils.numberFormat(tprogress, "#.##") + "\",");
				gantt.append(
						"\"duration\": \"" + DateUtils.getDuration(tt.getPlanStartDate(), tt.getPlanEndDate()) + "\",");
				if ((list.size() - 1) == i) {
					if (StringUtils.isNull(tt.getParentTask())) {
						gantt.append("\"parent\": \"" + template.getPersistInfo().getObjectIdentifier().getStringValue()
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
						gantt.append("\"parent\": \"" + template.getPersistInfo().getObjectIdentifier().getStringValue()
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

	/**
	 * @메소드명 :
	 * @최초 작성자 :
	 * @최초 작성일 : 2024. 07. 12
	 * @설명 :
	 */
	private ArrayList<Task> getterTemplateTask(Template template, ArrayList<Task> list) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);
		long ids = template.getPersistInfo().getObjectIdentifier().getId();
		SearchCondition sc = new SearchCondition(Task.class, "templateReference.key.id", "=", ids);
		query.appendWhere(sc, new int[] { idx });
		query.appendAnd();

		sc = new SearchCondition(Task.class, "parentTaskReference.key.id", "=", 0L);
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
			getterTasks(t, template, list);
		}
		return list;
	}

	public void getterTasks(Task parentTask, Template template, ArrayList<Task> list) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Task.class, true);

		long ids = parentTask.getPersistInfo().getObjectIdentifier().getId();
		long pids = template.getPersistInfo().getObjectIdentifier().getId();

		SearchCondition sc = new SearchCondition(Task.class, "parentTaskReference.key.id", "=", ids);
		query.appendWhere(sc, new int[] { idx });
		query.appendAnd();

		sc = new SearchCondition(Task.class, "templateReference.key.id", "=", pids);
		query.appendWhere(sc, new int[] { idx });

		ClassAttribute ca = new ClassAttribute(Task.class, Task.SORT);
		OrderBy orderBy = new OrderBy(ca, false);
		query.appendOrderBy(orderBy, new int[] { idx });

		QueryResult result = PersistenceHelper.manager.find(query);

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Task t = (Task) obj[0];
			list.add(t);
			getterTasks(t, template, list);
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

}
