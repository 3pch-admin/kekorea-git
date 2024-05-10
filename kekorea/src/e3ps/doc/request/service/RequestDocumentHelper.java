package e3ps.doc.request.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import e3ps.admin.commonCode.CommonCode;
import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.PartListMasterProjectLink;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.doc.meeting.Meeting;
import e3ps.doc.request.RequestDocument;
import e3ps.doc.request.RequestDocumentProjectLink;
import e3ps.doc.request.dto.RequestDocumentDTO;
import e3ps.project.Project;
import e3ps.project.ProjectUserLink;
import e3ps.project.task.Task;
import e3ps.project.variable.ProjectUserTypeVariable;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.doc.WTDocumentMaster;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.org.WTUser;
import wt.query.ClassAttribute;
import wt.query.ColumnExpression;
import wt.query.ConstantExpression;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.util.WTAttributeNameIfc;
import wt.vc.VersionControlHelper;

public class RequestDocumentHelper {

	public static final RequestDocumentHelper manager = new RequestDocumentHelper();
	public static final RequestDocumentService service = ServiceFactory.getService(RequestDocumentService.class);

	/**
	 * 의뢰서 저장 폴더 변수
	 */
	public static final String DEFAULT_ROOT = "/Default/프로젝트/의뢰서";

	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		String name = (String) params.get("name");
		String kekNumber = (String) params.get("kekNumber");
		String keNumber = (String) params.get("keNumber");
		String pdateFrom = (String) params.get("pdateFrom");
		String pdateTo = (String) params.get("pdateTo");
		String customer_name = (String) params.get("customer_name");
		String install_name = (String) params.get("install_name");
		String projectType = (String) params.get("projectType");
		String machineOid = (String) params.get("machineOid");
		String elecOid = (String) params.get("elecOid");
		String softOid = (String) params.get("softOid");
		String mak_name = (String) params.get("mak_name");
		String detail_name = (String) params.get("detail_name");
		String description = (String) params.get("description");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");
		String state = (String) params.get("state");

		QuerySpec qs = new QuerySpec();
		int idx = qs.appendClassList(RequestDocument.class, true);
		QuerySpecUtils.toLatest(qs, idx, RequestDocument.class);

		QuerySpecUtils.toCreator(qs, idx, RequestDocument.class, creatorOid);
		QuerySpecUtils.toTimeGreaterAndLess(qs, idx, RequestDocument.class, RequestDocument.CREATE_TIMESTAMP,
				createdFrom, createdTo);
		QuerySpecUtils.toLikeAnd(qs, idx, RequestDocument.class, RequestDocument.NAME, name);
		QuerySpecUtils.toEqualsAnd(qs, idx, RequestDocument.class, RequestDocument.LIFE_CYCLE_STATE, state);
		QuerySpecUtils.toOrderBy(qs, idx, RequestDocument.class, Meeting.CREATE_TIMESTAMP, true);

		if (!StringUtils.isNull(kekNumber) || !StringUtils.isNull(keNumber) || !StringUtils.isNull(pdateFrom)
				|| !StringUtils.isNull(pdateTo)) {

			int idx_p = qs.appendClassList(Project.class, true);
			int idx_l = qs.appendClassList(RequestDocumentProjectLink.class, false);

			ClassAttribute ca1 = new ClassAttribute(RequestDocument.class, "thePersistInfo.theObjectIdentifier.id");
			ClassAttribute ca3 = new ClassAttribute(RequestDocumentProjectLink.class, "roleAObjectRef.key.id");
			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			qs.appendWhere(new SearchCondition(ca1, "=", ca3), new int[] { idx, idx_l });

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}

			ca3 = new ClassAttribute(RequestDocumentProjectLink.class, "roleBObjectRef.key.id");
			ca1 = new ClassAttribute(Project.class, "thePersistInfo.theObjectIdentifier.id");

			qs.appendWhere(new SearchCondition(ca3, "=", ca1), new int[] { idx_l, idx_p });

			if (!StringUtils.isNull(kekNumber)) {
				if (qs.getConditionCount() > 0) {
					qs.appendAnd();
				}
				qs.appendOpenParen();
				String[] s = kekNumber.split(",");
				if (s.length > 0) {
					for (int k = 0; k < s.length; k++) {
						String ss = s[k];
						if (k != 0) {
							if (qs.getConditionCount() > 0) {
								qs.appendOr();
							}
						}
						ClassAttribute ca = new ClassAttribute(Project.class, Project.KEK_NUMBER);
						ColumnExpression ce = ConstantExpression.newExpression("%" + ss.toUpperCase() + "%");
						SQLFunction function = SQLFunction.newSQLFunction(SQLFunction.UPPER, ca);
						SearchCondition sc = new SearchCondition(function, SearchCondition.LIKE, ce);
						qs.appendWhere(sc, new int[] { idx_p });
					}
				}
				qs.appendCloseParen();
			}

			if (!StringUtils.isNull(keNumber)) {
				if (qs.getConditionCount() > 0) {
					qs.appendAnd();
				}
				qs.appendWhere(
						new SearchCondition(Project.class, "keNumber", SearchCondition.LIKE, "%" + keNumber + "%"),
						new int[] { idx_p });
			}

			QuerySpecUtils.toTimeGreaterAndLess(qs, idx, Project.class, Project.P_DATE, createdFrom, createdTo);

		}

		qs.setAdvancedQueryEnabled(true);
		qs.setDescendantQuery(false);

		PageQueryUtils pager = new PageQueryUtils(params, qs);
		PagingQueryResult result = pager.find();

		JSONArray list = new JSONArray();

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			RequestDocument reqDoc = (RequestDocument) obj[0];

			JSONObject node = new JSONObject();

			RequestDocumentDTO dto = new RequestDocumentDTO(reqDoc);

			node.put("oid", reqDoc.getPersistInfo().getObjectIdentifier().getStringValue());
			node.put("name", reqDoc.getName());
			node.put("number", reqDoc.getNumber());
			node.put("version", VersionControlHelper.getIterationDisplayIdentifier(reqDoc).toString());

			node.put("state", dto.getState());
			node.put("modifier", dto.getModifier());
			node.put("creator", dto.getCreator());
			node.put("creatorId", reqDoc.getCreatorName());
			node.put("createdDate_txt", dto.getCreatedDate_txt());

			node.put("kekNumber", dto.getTotalKekNumber());
			node.put("keNumber", dto.getTotalKeNumber());

			list.add(node);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 의뢰서 프로젝트 가져오기
	 */
	public JSONArray getProjects(RequestDocument requestDocument) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QueryResult result = PersistenceHelper.manager.navigate(requestDocument, "project",
				RequestDocumentProjectLink.class);
		while (result.hasMoreElements()) {
			Project project = (Project) result.nextElement();
			Map<String, String> map = new HashMap<>();
			map.put("oid", project.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("kekNumber", project.getKekNumber());
			map.put("keNumber", project.getKeNumber());
			map.put("projectType", project.getProjectType().getName());
			map.put("customer", project.getCustomer() != null ? project.getCustomer().getName() : "");
			map.put("install", project.getInstall() != null ? project.getInstall().getName() : "");
			map.put("mak", project.getMak() != null ? project.getMak().getName() : "");
			map.put("detail", project.getDetail() != null ? project.getDetail().getName() : "");
			map.put("description", project.getDescription());
			map.put("pDate_txt", project.getPDate() != null ? project.getPDate().toString().substring(0, 10) : "");
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 의뢰시 작번 내용 입력시 검증
	 */
	public Map<String, Object> validate(Map<String, String> params) throws Exception {
		Map<String, Object> result = new HashMap<>();
		String kekNumber = params.get("kekNumber");
		String projectType_code = params.get("projectType_code");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Project.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, Project.class, Project.KEK_NUMBER, kekNumber);
		CommonCode projectTypeCode = (CommonCode) CommonUtils.getObject(projectType_code);
		QuerySpecUtils.toEqualsAnd(query, idx, Project.class, "projectTypeReference.key.id", projectTypeCode);
		QueryResult qr = PersistenceHelper.manager.find(query);
		result.put("validate", qr.size() > 0 ? true : false);
		return result;
	}

	/**
	 * 의뢰서 관련 작번 리스트
	 */
	public JSONArray jsonAuiProject(String oid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		RequestDocument master = (RequestDocument) CommonUtils.getObject(oid);
//		QueryResult result = PersistenceHelper.manager.navigate(master, "project", RequestDocumentProjectLink.class);
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(RequestDocument.class, true);
		int idx_link = query.appendClassList(RequestDocumentProjectLink.class, true);
		QuerySpecUtils.toInnerJoin(query, RequestDocument.class, RequestDocumentProjectLink.class,
				WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, RequestDocumentProjectLink.class, "roleAObjectRef.key.id", master);
		QueryResult result = PersistenceHelper.manager.find(query);
		System.out.println("soze=" + result.size());
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			RequestDocumentProjectLink link = (RequestDocumentProjectLink) obj[1];
			Project project = link.getProject();
			Map<String, String> map = new HashMap<>();
			map.put("oid", project.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("projectType_name", project.getProjectType() != null ? project.getProjectType().getName() : "");
			map.put("customer_name", project.getCustomer() != null ? project.getCustomer().getName() : "");
			map.put("install_name", project.getInstall() != null ? project.getInstall().getName() : "");
			map.put("mak_name", project.getMak() != null ? project.getMak().getName() : "");
			map.put("detail_name", project.getDetail() != null ? project.getDetail().getName() : "");
			map.put("kekNumber", project.getKekNumber());
			map.put("keNumber", project.getKeNumber());
			map.put("description", project.getDescription());
			map.put("pdate", project.getPDate() != null ? project.getPDate().toString().substring(0, 10) : "");
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 회의록 다음 번호
	 */
	public String getNextNumber() throws Exception {

		Calendar ca = Calendar.getInstance();
		int day = ca.get(Calendar.DATE);
		int month = ca.get(Calendar.MONTH) + 1;
		int year = ca.get(Calendar.YEAR);
		DecimalFormat df = new DecimalFormat("00");
		String number = "REQ-" + df.format(year) + df.format(month) + df.format(day) + "-";

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(WTDocumentMaster.class, true);

		QuerySpecUtils.toLikeRightAnd(query, idx, WTDocumentMaster.class, WTDocumentMaster.NUMBER, number);
		QuerySpecUtils.toOrderBy(query, idx, WTDocumentMaster.class, WTDocumentMaster.NUMBER, true);

		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			WTDocumentMaster document = (WTDocumentMaster) obj[0];

			String s = document.getNumber().substring(document.getNumber().lastIndexOf("-") + 1);

			int ss = Integer.parseInt(s) + 1;
			DecimalFormat d = new DecimalFormat("000");
			number += d.format(ss);
		} else {
			number += "001";
		}
		return number;
	}

	public String getProjectTemplate(String oid) throws Exception {

		RequestDocument master = (RequestDocument) CommonUtils.getObject(oid);

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(RequestDocument.class, true);
		int idx_link = query.appendClassList(RequestDocumentProjectLink.class, true);
		QuerySpecUtils.toInnerJoin(query, RequestDocument.class, RequestDocumentProjectLink.class,
				WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, RequestDocumentProjectLink.class, "roleAObjectRef.key.id", master);
		QueryResult result = PersistenceHelper.manager.find(query);
		Object[] obj = (Object[]) result.nextElement();
		RequestDocumentProjectLink link = (RequestDocumentProjectLink) obj[1];
		Project project = link.getProject();
		String template = project.getTemplate().toString();

		return template;
	}
}
