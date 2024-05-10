package e3ps.korea.configSheet.service;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import e3ps.admin.commonCode.CommonCode;
import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.admin.configSheetCode.ConfigSheetCode;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.korea.configSheet.ColumnVariableLink;
import e3ps.korea.configSheet.ConfigSheet;
import e3ps.korea.configSheet.ConfigSheetColumnData;
import e3ps.korea.configSheet.ConfigSheetProjectLink;
import e3ps.korea.configSheet.ConfigSheetVariable;
import e3ps.korea.configSheet.ConfigSheetVariableLink;
import e3ps.korea.configSheet.beans.ConfigSheetDTO;
import e3ps.project.Project;
import e3ps.project.ProjectUserLink;
import e3ps.project.variable.ProjectUserTypeVariable;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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

public class ConfigSheetHelper {

	public static final ConfigSheetHelper manager = new ConfigSheetHelper();
	public static final ConfigSheetService service = ServiceFactory.getService(ConfigSheetService.class);

	/**
	 * CONFIG SHEET 검색
	 */
	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();

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
		boolean latest = (boolean) params.get("latest");

		QuerySpec qs = new QuerySpec();
		int idx = qs.appendClassList(ConfigSheet.class, true);

		if (latest) {
			QuerySpecUtils.toBooleanAnd(qs, idx, ConfigSheet.class, ConfigSheet.LATEST, true);
		}

		QuerySpecUtils.toLikeAnd(qs, idx, ConfigSheet.class, ConfigSheet.NAME, name);
		QuerySpecUtils.toCreator(qs, idx, ConfigSheet.class, creatorOid);
		QuerySpecUtils.toTimeGreaterAndLess(qs, idx, ConfigSheet.class, ConfigSheet.CREATE_TIMESTAMP, createdFrom,
				createdTo);
		QuerySpecUtils.toOrderBy(qs, idx, ConfigSheet.class, ConfigSheet.CREATE_TIMESTAMP, true);

		if (!StringUtils.isNull(kekNumber) || !StringUtils.isNull(keNumber) || !StringUtils.isNull(pdateFrom)
				|| !StringUtils.isNull(pdateTo) || !StringUtils.isNull(customer_name)
				|| !StringUtils.isNull(install_name) || !StringUtils.isNull(projectType)
				|| !StringUtils.isNull(machineOid) || !StringUtils.isNull(elecOid) || !StringUtils.isNull(softOid)
				|| !StringUtils.isNull(mak_name) || !StringUtils.isNull(detail_name)
				|| !StringUtils.isNull(description)) {

			int idx_p = qs.appendClassList(Project.class, true);
			int idx_l = qs.appendClassList(ConfigSheetProjectLink.class, false);

			ClassAttribute ca1 = new ClassAttribute(ConfigSheet.class, "thePersistInfo.theObjectIdentifier.id");
			ClassAttribute ca3 = new ClassAttribute(ConfigSheetProjectLink.class, "roleAObjectRef.key.id");
			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			qs.appendWhere(new SearchCondition(ca1, "=", ca3), new int[] { idx, idx_l });

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}

			ca3 = new ClassAttribute(ConfigSheetProjectLink.class, "roleBObjectRef.key.id");
			ca1 = new ClassAttribute(Project.class, "thePersistInfo.theObjectIdentifier.id");

			qs.appendWhere(new SearchCondition(ca3, "=", ca1), new int[] { idx_l, idx_p });

//			QuerySpecUtils.toLikeAnd(qs, idx_p, Project.class, Project.KEK_NUMBER, kekNumber);

			if (!StringUtils.isNull(kekNumber)) {
				if (qs.getConditionCount() > 0) {
					qs.appendAnd();
				}
				qs.appendOpenParen();
				String[] s = kekNumber.split(",");
				if (s.length > 0) {
					System.out.println("l=" + s.length);
					for (int k = 0; k < s.length; k++) {
						String ss = s[k];
						if (k != 0) {
							if (qs.getConditionCount() > 0) {
								qs.appendOr();
							}
						}
						System.out.println("dddd=" + k);
						ClassAttribute ca = new ClassAttribute(Project.class, Project.KEK_NUMBER);
						ColumnExpression ce = ConstantExpression.newExpression("%" + ss.toUpperCase() + "%");
						SQLFunction function = SQLFunction.newSQLFunction(SQLFunction.UPPER, ca);
						SearchCondition sc = new SearchCondition(function, SearchCondition.LIKE, ce);
						qs.appendWhere(sc, new int[] { idx_p });
					}
				}
				qs.appendCloseParen();
			}

			QuerySpecUtils.toLikeAnd(qs, idx_p, Project.class, Project.KE_NUMBER, keNumber);
			QuerySpecUtils.toTimeGreaterAndLess(qs, idx, Project.class, Project.P_DATE, pdateFrom, pdateTo);

			if (!StringUtils.isNull(customer_name)) {
				CommonCode customerCode = (CommonCode) CommonUtils.getObject(customer_name);
				QuerySpecUtils.toEqualsAnd(qs, idx_p, Project.class, "customerReference.key.id",
						CommonUtils.getOIDLongValue(customerCode));
			}

			if (!StringUtils.isNull(install_name)) {
				CommonCode installCode = (CommonCode) CommonUtils.getObject(install_name);
				QuerySpecUtils.toEqualsAnd(qs, idx_p, Project.class, "installReference.key.id",
						CommonUtils.getOIDLongValue(installCode));
			}

			if (!StringUtils.isNull(projectType)) {
				CommonCode projectTypeCode = (CommonCode) CommonUtils.getObject(projectType);
				QuerySpecUtils.toEqualsAnd(qs, idx_p, Project.class, "projectTypeReference.key.id",
						CommonUtils.getOIDLongValue(projectTypeCode));
			}

			if (!StringUtils.isNull(machineOid) || !StringUtils.isNull(elecOid) || !StringUtils.isNull(softOid)) {
				int idx_plink = qs.appendClassList(ProjectUserLink.class, false);
				int idx_u = qs.appendClassList(WTUser.class, false);

				if (!StringUtils.isNull(machineOid)) {
					CommonCode machineCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.MACHINE,
							"USER_TYPE");

					QuerySpecUtils.toInnerJoin(qs, Project.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
							"roleAObjectRef.key.id", idx_p, idx_plink);
					QuerySpecUtils.toInnerJoin(qs, WTUser.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
							"roleBObjectRef.key.id", idx_u, idx_plink);
					QuerySpecUtils.toEqualsAnd(qs, idx_plink, ProjectUserLink.class, "roleBObjectRef.key.id",
							CommonUtils.getOIDLongValue(machineOid));
					QuerySpecUtils.toEqualsAnd(qs, idx_plink, ProjectUserLink.class, "userTypeReference.key.id",
							machineCode);
				}

				if (!StringUtils.isNull(elecOid)) {
					CommonCode elecCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.ELEC,
							"USER_TYPE");

					QuerySpecUtils.toInnerJoin(qs, Project.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
							"roleAObjectRef.key.id", idx_p, idx_plink);
					QuerySpecUtils.toInnerJoin(qs, WTUser.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
							"roleBObjectRef.key.id", idx_u, idx_plink);
					QuerySpecUtils.toEqualsAnd(qs, idx_plink, ProjectUserLink.class, "roleBObjectRef.key.id",
							CommonUtils.getOIDLongValue(elecOid));
					QuerySpecUtils.toEqualsAnd(qs, idx_plink, ProjectUserLink.class, "userTypeReference.key.id",
							elecCode);
				}

				if (!StringUtils.isNull(softOid)) {
					CommonCode softCode = CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.SOFT,
							"USER_TYPE");

					QuerySpecUtils.toInnerJoin(qs, Project.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
							"roleAObjectRef.key.id", idx_p, idx_plink);
					QuerySpecUtils.toInnerJoin(qs, WTUser.class, ProjectUserLink.class, WTAttributeNameIfc.ID_NAME,
							"roleBObjectRef.key.id", idx_u, idx_plink);
					QuerySpecUtils.toEqualsAnd(qs, idx_plink, ProjectUserLink.class, "roleBObjectRef.key.id",
							CommonUtils.getOIDLongValue(softOid));
					QuerySpecUtils.toEqualsAnd(qs, idx_plink, ProjectUserLink.class, "userTypeReference.key.id",
							softCode);
				}
			}

			if (!StringUtils.isNull(mak_name)) {
				CommonCode makCode = (CommonCode) CommonUtils.getObject(mak_name);
				QuerySpecUtils.toEqualsAnd(qs, idx_p, Project.class, "makReference.key.id",
						CommonUtils.getOIDLongValue(makCode));
			}

			if (!StringUtils.isNull(detail_name)) {
				CommonCode detailCode = (CommonCode) CommonUtils.getObject(detail_name);
				QuerySpecUtils.toEqualsAnd(qs, idx_p, Project.class, "detailReference.key.id",
						CommonUtils.getOIDLongValue(detailCode));
			}

			QuerySpecUtils.toLikeAnd(qs, idx_p, Project.class, Project.DESCRIPTION, description);

		}

		qs.setAdvancedQueryEnabled(true);
		qs.setDescendantQuery(false);

		PageQueryUtils pager = new PageQueryUtils(params, qs);
		PagingQueryResult result = pager.find();

		System.out.println(qs);

		JSONArray list = new JSONArray();

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ConfigSheet conSheet = (ConfigSheet) obj[0];

			ConfigSheetDTO dto = new ConfigSheetDTO(conSheet);

			JSONObject node = new JSONObject();

			node.put("oid", dto.getOid());
			node.put("name", dto.getName());
			node.put("number", dto.getNumber());
			node.put("version", dto.getVersion());
			node.put("latest", dto.getVersion());
			node.put("poid", dto.getPoid());
			node.put("projectType_name", dto.getProjectType_name());
			node.put("customer_name", dto.getCustomer_name());
			node.put("install_name", dto.getInstall_name());
			node.put("mak_name", dto.getMak_name());
			node.put("detail_name", dto.getDetail_name());
			node.put("userId", dto.getUserId());
			node.put("description", dto.getDescription());
			node.put("state", dto.getState());
			node.put("model", dto.getModel());
			node.put("pdate_txt", dto.getPdate_txt());
			node.put("creator", dto.getCreator());
			node.put("primary", dto.getPrimary());
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
	 * CONFIG SHEET 사양들 불러오기
	 */
	public JSONArray loadBaseGridData() throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();

		QuerySpec query = new QuerySpec();
		int idx_q = query.appendClassList(ConfigSheetCode.class, true);
		int idx_i = query.appendClassList(ConfigSheetCode.class, true);

		QuerySpecUtils.toEqualsAnd(query, idx_q, ConfigSheetCode.class, ConfigSheetCode.CODE_TYPE, "CATEGORY");
		query.appendAnd();
		SearchCondition sc = new SearchCondition(ConfigSheetCode.class, WTAttributeNameIfc.ID_NAME,
				ConfigSheetCode.class, "parentReference.key.id");
		sc.setFromIndicies(new int[] { idx_q, idx_i }, 0);
		sc.setOuterJoin(2);
		query.appendWhere(sc, new int[] { idx_q, idx_i });

		QuerySpecUtils.toOrderBy(query, idx_q, ConfigSheetCode.class, ConfigSheetCode.SORT, false);
		QuerySpecUtils.toOrderBy(query, idx_i, ConfigSheetCode.class, ConfigSheetCode.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		int sort = 0;
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ConfigSheetCode category = (ConfigSheetCode) obj[0];
			ConfigSheetCode item = (ConfigSheetCode) obj[1];
			Map<String, Object> map = new HashMap<>();
			map.put("category_code", category != null ? category.getCode() : "");
			map.put("category_name", category != null ? category.getName() : "");
			map.put("item_code", item != null ? item.getCode() : "");
			map.put("item_name", item != null ? item.getName() : "");

			map.put("sort", sort);
			sort++;
			list.add(map);
		}
//		return new org.json.JSONArray(list);
		return JSONArray.fromObject(list);
	}

	/**
	 * 등록된 CONFIG SHEET 정보 가져오기
	 */
	public JSONArray loadBaseGridData(String oid, boolean isView) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		ConfigSheet configSheet = (ConfigSheet) CommonUtils.getObject(oid);

		ArrayList<String> dataFields = configSheet.getDataFields();
		Collections.sort(dataFields);

		QuerySpec query = new QuerySpec();

		int idx_link = query.appendClassList(ConfigSheetVariableLink.class, true);

		QuerySpecUtils.toEqualsAnd(query, idx_link, ConfigSheetVariableLink.class, "roleAObjectRef.key.id",
				configSheet);
		QuerySpecUtils.toOrderBy(query, idx_link, ConfigSheetVariableLink.class, ConfigSheetVariableLink.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ConfigSheetVariableLink link = (ConfigSheetVariableLink) obj[0];
			ConfigSheetVariable variable = link.getVariable();
			ConfigSheetCode category = variable.getCategory();
			ConfigSheetCode item = variable.getItem();
			int sort = link.getSort();
			Map<String, Object> map = new HashMap<>();
			map.put("category_code", variable.getCategory_code());
			map.put("category_name", variable.getCategory_name());
			map.put("item_code", variable.getItem_code());
			map.put("item_name", variable.getItem_name());
			////////
			QuerySpec qs = new QuerySpec();
			int idx_l = qs.appendClassList(ColumnVariableLink.class, true);
			QuerySpecUtils.toEqualsAnd(qs, idx_l, ColumnVariableLink.class, "roleBObjectRef.key.id", variable);
			// QuerySpecUtils.toBooleanAnd(qs, idx_l, ColumnVariableLink.class,
			// ColumnVariableLink.LAST, true);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				Object[] oo = (Object[]) qr.nextElement();
				ColumnVariableLink ll = (ColumnVariableLink) oo[0];
				ConfigSheetColumnData column = (ConfigSheetColumnData) ll.getColumn();
				map.put(column.getDataField(), column.getValue());
			}

			/////////////////
//			QueryResult qr = PersistenceHelper.manager.navigate(variable, "column", ColumnVariableLink.class);
//			while (qr.hasMoreElements()) {
//				ConfigSheetColumnData column = (ConfigSheetColumnData) qr.nextElement();
//				map.put(column.getDataField(), column.getValue());
//			}

			map.put("note", variable.getNote());
			map.put("apply", variable.getApply());
			map.put("sort", sort);
			list.add(map);
		}

		return JSONArray.fromObject(list);
	}

	/**
	 * CONFIG SHEET 관련 작번
	 */
	public JSONArray jsonAuiProject(String oid) throws Exception {
		ConfigSheet configSheet = (ConfigSheet) CommonUtils.getObject(oid);
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QueryResult result = PersistenceHelper.manager.navigate(configSheet, "project", ConfigSheetProjectLink.class);
		while (result.hasMoreElements()) {
			Project project = (Project) result.nextElement();
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
	 * CONFIG SHEET 비교
	 */
	public ArrayList<Map<String, Object>> compare(Project p1, ArrayList<Project> destList,
			ArrayList<ConfigSheetCode> fixedList) throws Exception {
		System.out.println("CONFIG SHEET 비교 START = " + new Timestamp(new Date().getTime()));
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		destList.add(0, p1);

		Map<String, Object> makList = new HashMap<>();
		Map<String, Object> customerList = new HashMap<>();
		Map<String, Object> keList = new HashMap<>();
		Map<String, Object> pdateList = new HashMap<>();

		makList.put("category_name", "막종 / 막종상세");
		customerList.put("category_name", "고객사 / 설치장소");
		keList.put("category_name", "KE 작번");
		pdateList.put("category_name", "발행일");

		makList.put("item_name", "막종 / 막종상세");
		customerList.put("item_name", "고객사 / 설치장소");
		keList.put("item_name", "KE 작번");
		pdateList.put("item_name", "발행일");
		ArrayList<String> oidList = new ArrayList<>();
		for (Project pp : destList) {
			oidList.add(CommonUtils.getOIDString(pp));
		}

		for (int i = 0; i < destList.size(); i++) {
			Project project = (Project) destList.get(i);
			String oid = CommonUtils.getOIDString(project);
			long id = CommonUtils.getOIDLongValue(project);

			System.out.println("#### destList==oid==" + oid + "==" + project.getKekNumber() + "==" + project);

//			for(int j = 0; j < oidVec.size(); j++) {
//				String oo = oidVec.get(j);
//				makList.put(		"P" + (j + 1) + "oid", oo);
//				customerList.put(	"P" + (j + 1) + "oid", oo);
//				keList.put(			"P" + (j + 1) + "oid", oo);
//				pdateList.put(		"P" + (j + 1) + "oid", oo);
//			}

			makList.put("P" + i + "oid", oid);
			customerList.put("P" + i + "oid", oid);
			keList.put("P" + i + "oid", oid);
			pdateList.put("P" + i + "oid", oid);

			makList.put("P" + i + "id", id);
			customerList.put("P" + i + "id", id);
			keList.put("P" + i + "id", id);
			pdateList.put("P" + i + "id", id);

			makList.put("oidList", oidList);
			customerList.put("oidList", oidList);
			keList.put("oidList", oidList);
			pdateList.put("oidList", oidList);

			String mak = project.getMak() != null ? project.getMak().getName() : "";
			String detail = project.getDetail() != null ? project.getDetail().getName() : "";
			String customer = project.getCustomer() != null ? project.getCustomer().getName() : "";
			String install = project.getInstall() != null ? project.getInstall().getName() : "";

			makList.put("P" + i, "<a href='#'  onclick=javascript:popup('/Windchill/plm/project/info?oid=" + oid
					+ "');>" + mak + " / " + detail + "</a>");
			customerList.put("P" + i, "<a href='#'  onclick=javascript:popup('/Windchill/plm/project/info?oid=" + oid
					+ "');>" + customer + " / " + install + "</a>");
			keList.put("P" + i, "<a href='#'  onclick=javascript:popup('/Windchill/plm/project/info?oid=" + oid + "');>"
					+ project.getKeNumber() + "</a>");
			pdateList.put("P" + i, "<a href='#'  onclick=javascript:popup('/Windchill/plm/project/info?oid=" + oid
					+ "');>" + CommonUtils.getPersistableTime(project.getPDate()) + "</a>");
		}

		list.add(makList);
		list.add(customerList);
		list.add(keList);
		list.add(pdateList);

		
		ArrayList<Map<String,String>> defaultKey = new ArrayList<>();
	
		
		QueryResult qr = PersistenceHelper.manager.navigate(p1, "configSheet", ConfigSheetProjectLink.class);

		if (qr.hasMoreElements()) {
			ConfigSheet config = (ConfigSheet)qr.nextElement();
			
			
			QuerySpec query = new QuerySpec();
		
			int idx_link = query.appendClassList(ConfigSheetVariableLink.class, true);
		
			QuerySpecUtils.toEqualsAnd(query, idx_link, ConfigSheetVariableLink.class, "roleAObjectRef.key.id", config);
			QuerySpecUtils.toOrderBy(query, idx_link, ConfigSheetVariableLink.class, ConfigSheetVariableLink.SORT, false);
			QueryResult result = PersistenceHelper.manager.find(query);
			while (result.hasMoreElements()) {
				Object[] obj = (Object[]) result.nextElement();
				ConfigSheetVariableLink link = (ConfigSheetVariableLink) obj[0];
				ConfigSheetVariable variable = link.getVariable();
				ConfigSheetCode category = variable.getCategory();
				ConfigSheetCode item = variable.getItem();
				
				Map<String, Object> mergedList = new HashMap<>();
				
				
				
				mergedList.put("category_name", variable.getCategory_name());
				mergedList.put("category_code", variable.getCategory_code());
				mergedList.put("item_name", variable.getItem_name());
				mergedList.put("item_code", variable.getItem_code());
	
	
				for (int i = 0; i < destList.size(); i++) {
					Project project = (Project) destList.get(i);
					QueryResult qr1 = PersistenceHelper.manager.navigate(project, "configSheet", ConfigSheetProjectLink.class);
	
					System.out.println("### destList config .size==" + qr1.size());
					// 프로젝트 연관 컨피그 시트 찾기.ㅊㅊㅋㅌㅋ
					if (qr1.hasMoreElements()) {
						ConfigSheet configSheet = (ConfigSheet) qr1.nextElement();
						System.out.println("### configSheet==" + CommonUtils.getOIDLongValue(configSheet));
						QuerySpec _query = new QuerySpec();
	
						int idx_variable = _query.appendClassList(ConfigSheetVariable.class, true);
						int idx_sheet = _query.appendClassList(ConfigSheet.class, false);
						int idx_link2 = _query.appendClassList(ConfigSheetVariableLink.class, false);
	
						QuerySpecUtils.toInnerJoin(_query, ConfigSheetVariable.class, ConfigSheetVariableLink.class,
								WTAttributeNameIfc.ID_NAME, "roleBObjectRef.key.id", idx_variable, idx_link2);
						QuerySpecUtils.toInnerJoin(_query, ConfigSheetVariableLink.class, ConfigSheet.class,
								"roleAObjectRef.key.id", WTAttributeNameIfc.ID_NAME, idx_link2, idx_sheet);
						QuerySpecUtils.toEqualsAnd(_query, idx_link2, ConfigSheetVariableLink.class,
								"roleAObjectRef.key.id", configSheet);
						QuerySpecUtils.toEqualsAnd(_query, idx_variable, ConfigSheetVariable.class,
								ConfigSheetVariable.ITEM_NAME, variable.getItem_name());
						QuerySpecUtils.toOrderBy(_query, idx_link2, ConfigSheetVariableLink.class,
								ConfigSheetVariableLink.SORT, false);
						QueryResult _qr = PersistenceHelper.manager.find(_query);
						System.out.println("### _qr size==" + _qr.size());
	
						while (_qr.hasMoreElements()) {
							Object[] o = (Object[]) _qr.nextElement();
							ConfigSheetVariable variable2 = (ConfigSheetVariable) o[0];
	
							QuerySpec qs = new QuerySpec();
							int idx_l = qs.appendClassList(ColumnVariableLink.class, true);
							QuerySpecUtils.toEqualsAnd(qs, idx_l, ColumnVariableLink.class, "roleBObjectRef.key.id", variable2);
							QuerySpecUtils.toBooleanAnd(qs, idx_l, ColumnVariableLink.class, ColumnVariableLink.LAST, true);
	
							QueryResult rs = PersistenceHelper.manager.find(qs);
							System.out.println("rs size=" + rs.size());
							if (rs.hasMoreElements()) {
								Object[] oo = (Object[]) rs.nextElement();
								ColumnVariableLink ll = (ColumnVariableLink) oo[0];
								mergedList.put("P" + i, ll.getColumn().getValue());
							}
						}
					}
				}
				list.add(mergedList);
			}
		}
		destList.remove(0);
		System.out.println("CONFIG SHEET 비교 END = " + new Timestamp(new Date().getTime()));
		return list;
	}

	/**
	 * 선택한 작번 CONFIG SHEET 복사
	 */
	public ArrayList<Map<String, Object>> copyBaseData(ConfigSheet configSheet) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ConfigSheet.class, true);
		int idx_link = query.appendClassList(ConfigSheetVariableLink.class, true);
		QuerySpecUtils.toInnerJoin(query, ConfigSheet.class, ConfigSheetVariableLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, ConfigSheetVariableLink.class, "roleAObjectRef.key.id",
				configSheet);
		QuerySpecUtils.toOrderBy(query, idx_link, ConfigSheetVariableLink.class, ConfigSheetVariableLink.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ConfigSheetVariableLink link = (ConfigSheetVariableLink) obj[1];
			ConfigSheetVariable variable = link.getVariable();
			int sort = link.getSort();
			Map<String, Object> map = new HashMap<>();
			map.put("category_code", variable.getCategory_code());
			map.put("category_name", variable.getCategory_name());
			map.put("item_code", variable.getItem_code());
			map.put("item_name", variable.getItem_name());
			QueryResult qr = PersistenceHelper.manager.navigate(variable, "column", ColumnVariableLink.class);
			map.put("specSize", qr.size());
			while (qr.hasMoreElements()) {
				ConfigSheetColumnData column = (ConfigSheetColumnData) qr.nextElement();
				map.put(column.getDataField(), column.getValue() != null ? column.getValue() : "");
			}
			map.put("note", variable.getNote());
			map.put("apply", variable.getApply());
			map.put("sort", sort);
			
			list.add(map);
		}
		return list;
	}

	/**
	 * CONFIG SHEET 번호
	 */
	public String getNextNumber() throws Exception {

		Calendar ca = Calendar.getInstance();
		int month = ca.get(Calendar.MONTH) + 1;
		int year = ca.get(Calendar.YEAR);
		DecimalFormat df = new DecimalFormat("00");
		String number = "CS-" + df.format(year).substring(2) + df.format(month) + "-";

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ConfigSheet.class, true);

		QuerySpecUtils.toLikeRightAnd(query, idx, ConfigSheet.class, ConfigSheet.NUMBER, number);
		QuerySpecUtils.toOrderBy(query, idx, ConfigSheet.class, ConfigSheet.NUMBER, true);

		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ConfigSheet configSheet = (ConfigSheet) obj[0];

			String s = configSheet.getNumber().substring(configSheet.getNumber().lastIndexOf("-") + 1);

			int ss = Integer.parseInt(s) + 1;
			DecimalFormat d = new DecimalFormat("0000");
			number += d.format(ss);
		} else {
			number += "0001";
		}
		return number;
	}

	/**
	 * CONFIG SHEET 프로젝트 가져오기
	 */
	public JSONArray getProjects(ConfigSheet configSheet) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QueryResult result = PersistenceHelper.manager.navigate(configSheet, "project", ConfigSheetProjectLink.class);
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
	 * 최신 CONFIG SHEET
	 */
	public ConfigSheet getLatest(ConfigSheet configSheet) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ConfigSheet.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, ConfigSheet.class, ConfigSheet.NUMBER, configSheet.getNumber());
		QuerySpecUtils.toBooleanAnd(query, idx, ConfigSheet.class, ConfigSheet.LATEST, true);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ConfigSheet latest = (ConfigSheet) obj[0];
			return latest;
		}
		return null;
	}
	
	
}