package e3ps.doc.meeting.service;

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
import e3ps.doc.meeting.MeetingProjectLink;
import e3ps.doc.meeting.MeetingTemplate;
import e3ps.doc.meeting.dto.MeetingDTO;
import e3ps.doc.meeting.dto.MeetingTemplateDTO;
import e3ps.project.Project;
import e3ps.project.ProjectUserLink;
import e3ps.project.variable.ProjectUserTypeVariable;
import e3ps.workspace.notice.Notice;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.doc.WTDocument;
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

public class MeetingHelper {

	public static final MeetingHelper manager = new MeetingHelper();
	public static final MeetingService service = ServiceFactory.getService(MeetingService.class);

	// 회의록이 저장되어지는 폴더 위치 상수
	public static final String LOCATION = "/Default/프로젝트/회의록";

	/**
	 * 회의록 템플릿 조회
	 */
	public Map<String, Object> template(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<MeetingTemplateDTO> list = new ArrayList<>();

		String name = (String) params.get("name");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(MeetingTemplate.class, true);

		QuerySpecUtils.toLikeAnd(query, idx, MeetingTemplate.class, MeetingTemplate.NAME, name);
		QuerySpecUtils.toTimeGreaterEqualsThan(query, idx, MeetingTemplate.class, MeetingTemplate.CREATE_TIMESTAMP,
				createdFrom);
		QuerySpecUtils.toTimeLessEqualsThan(query, idx, MeetingTemplate.class, MeetingTemplate.CREATE_TIMESTAMP,
				createdTo);
		QuerySpecUtils.toCreator(query, idx, MeetingTemplate.class, creatorOid);
		QuerySpecUtils.toBooleanAnd(query, idx, MeetingTemplate.class, MeetingTemplate.ENABLE, true);
		QuerySpecUtils.toOrderBy(query, idx, MeetingTemplate.class, MeetingTemplate.NAME, false);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			MeetingTemplate meetingTemplate = (MeetingTemplate) obj[0];
			MeetingTemplateDTO column = new MeetingTemplateDTO(meetingTemplate);
			list.add(column);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 회의록 조회
	 */
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
		String content = (String) params.get("content");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");

		QuerySpec qs = new QuerySpec();
		int idx = qs.appendClassList(Meeting.class, true);

		QuerySpecUtils.toLatest(qs, idx, Meeting.class);
		QuerySpecUtils.toLikeAnd(qs, idx, Meeting.class, Meeting.NAME, name);
		QuerySpecUtils.toLikeAnd(qs, idx, Meeting.class, Meeting.CONTENT, content);
		QuerySpecUtils.toCreator(qs, idx, Meeting.class, creatorOid);
		QuerySpecUtils.toTimeGreaterAndLess(qs, idx, Meeting.class, Meeting.CREATE_TIMESTAMP, createdFrom, createdTo);
		QuerySpecUtils.toOrderBy(qs, idx, Meeting.class, Meeting.CREATE_TIMESTAMP, true);

		if (!StringUtils.isNull(kekNumber) || !StringUtils.isNull(keNumber) || !StringUtils.isNull(pdateFrom)
				|| !StringUtils.isNull(pdateTo)) {

			int idx_p = qs.appendClassList(Project.class, true);
			int idx_l = qs.appendClassList(MeetingProjectLink.class, false);

			ClassAttribute ca1 = new ClassAttribute(Meeting.class, "thePersistInfo.theObjectIdentifier.id");
			ClassAttribute ca3 = new ClassAttribute(MeetingProjectLink.class, "roleAObjectRef.key.id");
			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			qs.appendWhere(new SearchCondition(ca1, "=", ca3), new int[] { idx, idx_l });

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}

			ca3 = new ClassAttribute(MeetingProjectLink.class, "roleBObjectRef.key.id");
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
			Meeting mee = (Meeting) obj[0];

			MeetingDTO dto = new MeetingDTO(mee);

			JSONObject node = new JSONObject();

			node.put("oid", mee.getPersistInfo().getObjectIdentifier().getStringValue());
			node.put("version", mee.getVersionIdentifier().getSeries().getValue() + "."
					+ mee.getIterationIdentifier().getSeries().getValue());
			node.put("name", dto.getName());
			node.put("state", dto.getState());
			node.put("creator", dto.getCreator());
			node.put("creatorId", mee.getOwnership().getOwner().getName());
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

	public ArrayList<Map<String, String>> getMeetingTemplateMap() throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(MeetingTemplate.class, true);
		QuerySpecUtils.toBooleanAnd(query, idx, MeetingTemplate.class, MeetingTemplate.ENABLE, true);
		QuerySpecUtils.toOrderBy(query, idx, MeetingTemplate.class, MeetingTemplate.NAME, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			MeetingTemplate meetingTemplate = (MeetingTemplate) obj[0];
			Map<String, String> map = new HashMap<>();
			map.put("oid", meetingTemplate.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("name", meetingTemplate.getName());
			list.add(map);
		}
		return list;
	}

	public String getContent(String oid) throws Exception {
		MeetingTemplate meetingTemplate = (MeetingTemplate) CommonUtils.getObject(oid);
		return meetingTemplate.getContent();
	}

	public JSONArray jsonAuiProject(String oid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		Meeting meeting = (Meeting) CommonUtils.getObject(oid);

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Meeting.class, true);
		int idx_link = query.appendClassList(MeetingProjectLink.class, true);
		QuerySpecUtils.toInnerJoin(query, Meeting.class, MeetingProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, MeetingProjectLink.class, "roleAObjectRef.key.id", meeting);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			MeetingProjectLink link = (MeetingProjectLink) obj[1];
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
		String number = "ME-" + df.format(year) + df.format(month) + df.format(day) + "-";

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
}
