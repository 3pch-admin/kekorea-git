package e3ps.bom.tbom.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import e3ps.bom.tbom.TBOMData;
import e3ps.bom.tbom.TBOMMaster;
import e3ps.bom.tbom.TBOMMasterDataLink;
import e3ps.bom.tbom.TBOMMasterProjectLink;
import e3ps.bom.tbom.dto.TBOMMasterDTO;
import e3ps.common.util.AUIGridUtils;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.part.KePart;
import e3ps.part.KePartMaster;
import e3ps.project.Project;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.query.ClassAttribute;
import wt.query.ColumnExpression;
import wt.query.ConstantExpression;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.util.WTAttributeNameIfc;

public class TBOMHelper {

	public static final TBOMHelper manager = new TBOMHelper();
	public static final TBOMService service = ServiceFactory.getService(TBOMService.class);

	/**
	 * T-BOM 조회
	 */
	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		String name = (String) params.get("name");
		String kekNumber = (String) params.get("kekNumber");
		String keNumber = (String) params.get("keNumber");
		String pdateFrom = (String) params.get("pdateFrom");
		String pdateTo = (String) params.get("pdateTo");
//		String customer_name = (String) params.get("customer_name");
//		String install_name = (String) params.get("install_name");
//		String projectType = (String) params.get("projectType");
//		String machineOid = (String) params.get("machineOid");
//		String elecOid = (String) params.get("elecOid");
//		String softOid = (String) params.get("softOid");
//		String mak_name = (String) params.get("mak_name");
//		String detail_name = (String) params.get("detail_name");
//		String description = (String) params.get("description");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");

		boolean latest = (boolean) params.get("latest");
		JSONArray list = new JSONArray();

		QuerySpec qs = new QuerySpec();
		int idx = qs.appendClassList(TBOMMaster.class, true);

		QuerySpecUtils.toLikeAnd(qs, idx, TBOMMaster.class, TBOMMaster.NAME, name);

		if (!StringUtils.isNull(kekNumber) || !StringUtils.isNull(keNumber) || !StringUtils.isNull(pdateFrom)
				|| !StringUtils.isNull(pdateTo)) {

			int idx_p = qs.appendClassList(Project.class, true);
			int idx_l = qs.appendClassList(TBOMMasterProjectLink.class, false);

			ClassAttribute ca1 = new ClassAttribute(TBOMMaster.class, "thePersistInfo.theObjectIdentifier.id");
			ClassAttribute ca3 = new ClassAttribute(TBOMMasterProjectLink.class, "roleAObjectRef.key.id");

			qs.appendWhere(new SearchCondition(ca1, "=", ca3), new int[] { idx, idx_l });

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}

			ca3 = new ClassAttribute(TBOMMasterProjectLink.class, "roleBObjectRef.key.id");
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

			System.out.println("######" + qs);
		}

		if (latest) {
			QuerySpecUtils.toBooleanAnd(qs, idx, TBOMMaster.class, TBOMMaster.LATEST, latest);
		}

		if (!StringUtils.isNull(creatorOid)) {
			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			SearchCondition sc = new SearchCondition(TBOMMaster.class, "creator.key.id", "=",
					CommonUtils.getOIDLongValue(creatorOid));
			qs.appendWhere(sc, new int[] { idx });
		}

		qs.setAdvancedQueryEnabled(true);
		qs.setDescendantQuery(false);

		PageQueryUtils pager = new PageQueryUtils(params, qs);
		PagingQueryResult qr = pager.find();

		while (qr.hasMoreElements()) {
			Object[] o = (Object[]) qr.nextElement();
			TBOMMaster tt = (TBOMMaster) o[0];

			TBOMMasterDTO td = new TBOMMasterDTO(tt);

			JSONObject node = new JSONObject();

			node.put("oid", tt.getPersistInfo().getObjectIdentifier().getStringValue());
			node.put("name", tt.getName());
			node.put("creator", td.getCreator());
			node.put("createdDate_txt", CommonUtils.getPersistableTime(tt.getCreateTimestamp()));
			node.put("modifiedDate_txt", CommonUtils.getPersistableTime(tt.getModifyTimestamp()));
			node.put("state", td.getState());
			node.put("kekNumber", td.getTotalKekNumber());
			node.put("keNumber", td.getTotalKeNumber());
			node.put("keNumber", td.getTotalKeNumber());
			node.put("version", td.getVersion());
			node.put("latest", td.isLatest());

			list.add(node);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());

		return map;
	}

	public String getNextNumber(String param) throws Exception {
		String preFix = DateUtils.getTodayString();
		String number = param + "-" + preFix + "-";
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TBOMMaster.class, true);

		SearchCondition sc = new SearchCondition(TBOMMaster.class, TBOMMaster.NUMBER, "LIKE",
				number.toUpperCase() + "%");
		query.appendWhere(sc, new int[] { idx });

		ClassAttribute attr = new ClassAttribute(TBOMMaster.class, TBOMMaster.NUMBER);
		OrderBy orderBy = new OrderBy(attr, true);
		query.appendOrderBy(orderBy, new int[] { idx });

		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TBOMMaster master = (TBOMMaster) obj[0];

			String s = master.getNumber().substring(master.getNumber().lastIndexOf("-") + 1);

			int ss = Integer.parseInt(s) + 1;
			DecimalFormat d = new DecimalFormat("0000");
			number += d.format(ss);
		} else {
			number += "0001";
		}
		return number;
	}

	/**
	 * T-BOM 등록시 가져올 KE 부품들
	 */
	public Map<String, Object> getData(String number) throws Exception {
		Map<String, Object> map = new HashMap<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(KePart.class, true);
		int idx_m = query.appendClassList(KePartMaster.class, true);
		QuerySpecUtils.toInnerJoin(query, KePart.class, KePartMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toBooleanAnd(query, idx, KePart.class, KePart.LATEST, true);
		QuerySpecUtils.toEqualsAnd(query, idx_m, KePartMaster.class, KePartMaster.KE_NUMBER, number);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			KePart kePart = (KePart) obj[0];
			KePartMaster master = (KePartMaster) obj[1];
			map.put("name", master.getName());
			map.put("keNumber", master.getKeNumber());
			map.put("code", master.getCode());
			map.put("model", master.getModel());
			map.put("lotNo", master.getLotNo());
			map.put("oid", kePart.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("ok", true);
		} else {
			map.put("keNumber", "서버에 존재하지 않는 KE 부품 번호 입니다.");
			map.put("ok", true);
		}
		return map;
	}

	/**
	 * T-BOM 관련 작번
	 */
	public JSONArray jsonAuiProject(String oid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		TBOMMaster master = (TBOMMaster) CommonUtils.getObject(oid);

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TBOMMaster.class, true);
		int idx_link = query.appendClassList(TBOMMasterProjectLink.class, true);
		QuerySpecUtils.toInnerJoin(query, TBOMMaster.class, TBOMMasterProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, TBOMMasterProjectLink.class, "roleAObjectRef.key.id", master);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TBOMMasterProjectLink link = (TBOMMasterProjectLink) obj[1];
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
	 * T-BOM 비교
	 */
	public ArrayList<Map<String, Object>> compare(Project p1, ArrayList<Project> destList) throws Exception {

		ArrayList<Map<String, Object>> list = integratedData(p1);
		ArrayList<Map<String, Object>> mergedList = new ArrayList<>();

		Map<String, Object> makList = new HashMap<>();
		Map<String, Object> customerList = new HashMap<>();
		Map<String, Object> keList = new HashMap<>();
		Map<String, Object> pdateList = new HashMap<>();

		makList.put("lotNo", "막종 / 막종상세");
		customerList.put("lotNo", "고객사 / 설치장소");
		keList.put("lotNo", "KE 작번");
		pdateList.put("lotNo", "발행일");

		makList.put("code", "막종 / 막종상세");
		customerList.put("code", "고객사 / 설치장소");
		keList.put("code", "KE 작번");
		pdateList.put("code", "발행일");

		makList.put("keNumber", "막종 / 막종상세");
		customerList.put("keNumber", "고객사 / 설치장소");
		keList.put("keNumber", "KE 작번");
		pdateList.put("keNumber", "발행일");

		destList.add(0, p1);
		for (int i = 0; i < destList.size(); i++) {
			Project project = (Project) destList.get(i);
			String oid = project.getPersistInfo().getObjectIdentifier().getStringValue();
			long id = project.getPersistInfo().getObjectIdentifier().getId();
			makList.put("id", id);
			customerList.put("id", id);
			keList.put("id", id);
			pdateList.put("id", id);
			makList.put("oid", oid);
			customerList.put("oid", oid);
			keList.put("oid", oid);
			pdateList.put("oid", oid);

			String mak = project.getMak() != null ? project.getMak().getName() : "";
			String detail = project.getDetail() != null ? project.getDetail().getName() : "";
			String customer = project.getCustomer() != null ? project.getCustomer().getName() : "";
			String install = project.getInstall() != null ? project.getInstall().getName() : "";

			makList.put("qty" + (i + 1), mak + " / " + detail);
			customerList.put("qty" + (i + 1), customer + " / " + install);
			keList.put("qty" + (i + 1), project.getKeNumber());
			pdateList.put("qty" + (i + 1), CommonUtils.getPersistableTime(project.getPDate()));
		}

		mergedList.add(makList);
		mergedList.add(customerList);
		mergedList.add(keList);
		mergedList.add(pdateList);

		destList.remove(0);
		// list1의 데이터를 먼저 추가
		for (Map<String, Object> data : list) {
			Map<String, Object> mergedData = new HashMap<>();
			mergedData.put("lotNo", data.get("lotNo"));
			mergedData.put("code", data.get("code"));
			mergedData.put("name", data.get("name"));
			mergedData.put("keNumber", data.get("keNumber"));
			mergedData.put("model", data.get("model"));
			mergedData.put("qty1", data.get("qty"));
			mergedData.put("unit", data.get("unit"));
			mergedData.put("provide", data.get("provide"));
			mergedData.put("discontinue", data.get("discontinue"));
			mergedData.put("oid", data.get("oid"));
			mergedData.put("moid", data.get("moid"));
			mergedList.add(mergedData);
		}

		// 전체 작번 START
		for (int i = 0; i < destList.size(); i++) {
			Project p2 = (Project) destList.get(i);

			ArrayList<Map<String, Object>> _list = integratedData(p2);

			for (Map<String, Object> data : _list) {
				String partNo = (String) data.get("keNumber");
				String lotNo = (String) data.get("lotNo");
				String key = partNo + "-" + lotNo;
				boolean isExist = false;

				// mergedList에 partNo가 동일한 데이터가 있는지 확인
				for (Map<String, Object> mergedData : mergedList) {
					String mergedPartNo = (String) mergedData.get("keNumber");
					String mergedLotNo = (String) mergedData.get("lotNo");
					String _key = mergedPartNo + "-" + mergedLotNo;

					if (key.equals(_key)) {
						// partNo가 동일한 데이터가 있으면 데이터를 업데이트하고 isExist를 true로 변경
						mergedData.put("qty" + (2 + i), data.get("qty"));
						isExist = true;
						break;
					}
				}

				if (!isExist) {
					// partNo가 동일한 데이터가 없으면 mergedList에 데이터를 추가
					Map<String, Object> mergedData = new HashMap<>();
					mergedData.put("lotNo", data.get("lotNo"));
					mergedData.put("name", data.get("name"));
					mergedData.put("code", data.get("code"));
					mergedData.put("keNumber", data.get("keNumber"));
					mergedData.put("model", data.get("model"));
					mergedData.put("qty" + (2 + i), data.get("qty"));
					mergedData.put("unit", data.get("unit"));
					mergedData.put("provide", data.get("provide"));
					mergedData.put("discontinue", data.get("discontinue"));
					mergedData.put("oid", data.get("oid"));
					mergedData.put("moid", data.get("moid"));
					mergedList.add(mergedData);
				}
			}
		} // 전체 작번 END...
		return mergedList;
	}

	/**
	 * 비교할 데이터 가져오기
	 */
	private ArrayList<Map<String, Object>> integratedData(Project project) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TBOMMaster.class, true);
		int idx_link = query.appendClassList(TBOMMasterProjectLink.class, false);
		int idx_p = query.appendClassList(Project.class, false);

		QuerySpecUtils.toBooleanAnd(query, idx, TBOMMaster.class, TBOMMaster.LATEST, true);
		QuerySpecUtils.toInnerJoin(query, TBOMMaster.class, TBOMMasterProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, Project.class, TBOMMasterProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_p, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, TBOMMasterProjectLink.class, "roleBObjectRef.key.id", project);

		QuerySpecUtils.toOrderBy(query, idx, TBOMMaster.class, TBOMMaster.CREATE_TIMESTAMP, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TBOMMaster master = (TBOMMaster) obj[0];

			QuerySpec _query = new QuerySpec();
			int _idx = _query.appendClassList(TBOMMaster.class, false);
			int _idx_link = _query.appendClassList(TBOMMasterDataLink.class, true);
			int idx_data = _query.appendClassList(TBOMData.class, false);
			QuerySpecUtils.toInnerJoin(_query, TBOMMaster.class, TBOMMasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleAObjectRef.key.id", _idx, _idx_link);
			QuerySpecUtils.toInnerJoin(_query, TBOMData.class, TBOMMasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleBObjectRef.key.id", idx_data, _idx_link);
			QuerySpecUtils.toEqualsAnd(_query, _idx_link, TBOMMasterDataLink.class, "roleAObjectRef.key.id", master);
			QuerySpecUtils.toOrderBy(_query, idx_data, TBOMData.class, TBOMData.SORT, false);
			QueryResult qr = PersistenceHelper.manager.find(_query);
			while (qr.hasMoreElements()) {
				Object[] oo = (Object[]) qr.nextElement();
				TBOMMasterDataLink link = (TBOMMasterDataLink) oo[0];
				TBOMData data = link.getData();
				Map<String, Object> map = new HashMap<>();
				map.put("oid", data.getKePart().getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("moid", master.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("lotNo", String.valueOf(data.getLotNo()));
				map.put("code", data.getKePart().getMaster().getCode());
				map.put("name", data.getKePart().getMaster().getName());
				map.put("model", data.getKePart().getMaster().getModel());
				map.put("keNumber", data.getKePart().getMaster().getKeNumber());
				map.put("qty", data.getQty());
				map.put("unit", data.getUnit());
				map.put("provide", data.getProvide());
				map.put("discontinue", data.getDiscontinue());
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * T-BOM 데이터 가져오기
	 */
	public JSONArray getData(TBOMMaster master) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TBOMMaster.class, false);
		int idx_link = query.appendClassList(TBOMMasterDataLink.class, true);
		int idx_data = query.appendClassList(TBOMData.class, false);
		QuerySpecUtils.toInnerJoin(query, TBOMMaster.class, TBOMMasterDataLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, TBOMData.class, TBOMMasterDataLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_data, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, TBOMMasterDataLink.class, "roleAObjectRef.key.id", master);
		QuerySpecUtils.toOrderBy(query, idx_data, TBOMData.class, TBOMData.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TBOMMasterDataLink link = (TBOMMasterDataLink) obj[0];
			TBOMData data = link.getData();
			Map<String, Object> map = new HashMap<>();
			map.put("oid", data.getKePart().getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("lotNo", String.valueOf(data.getLotNo()));
//			map.put("code", data.getKePart().getMaster().getCode());
			map.put("code", data.getCode());
			map.put("name", data.getKePart().getMaster().getName());
			map.put("model", data.getKePart().getMaster().getModel());
			map.put("keNumber", data.getKePart().getMaster().getKeNumber());
			map.put("qty", data.getQty());
			map.put("unit", data.getUnit());
			map.put("provide", data.getProvide());
			map.put("discontinue", data.getDiscontinue());
			map.put("ok", true);
			list.add(map);
		}

		return JSONArray.fromObject(list);
	}

	/**
	 * 프로젝트 T-BOM 탭
	 */
	public JSONArray tbomTab(String oid) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();

		Project project = (Project) CommonUtils.getObject(oid);

		QueryResult qr = PersistenceHelper.manager.navigate(project, "master", TBOMMasterProjectLink.class);
		while (qr.hasMoreElements()) {
			TBOMMaster master = (TBOMMaster) qr.nextElement();
			QuerySpec query = new QuerySpec();
			int idx = query.appendClassList(TBOMMaster.class, false);
			int idx_link = query.appendClassList(TBOMMasterDataLink.class, true);
			int idx_data = query.appendClassList(TBOMData.class, false);
			QuerySpecUtils.toInnerJoin(query, TBOMMaster.class, TBOMMasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleAObjectRef.key.id", idx, idx_link);
			QuerySpecUtils.toInnerJoin(query, TBOMData.class, TBOMMasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleBObjectRef.key.id", idx_data, idx_link);
			QuerySpecUtils.toEqualsAnd(query, idx_link, TBOMMasterDataLink.class, "roleAObjectRef.key.id", master);
			QuerySpecUtils.toOrderBy(query, idx_data, TBOMData.class, TBOMData.SORT, false);
			QueryResult result = PersistenceHelper.manager.find(query);
			while (result.hasMoreElements()) {
				Object[] obj = (Object[]) result.nextElement();
				TBOMMasterDataLink link = (TBOMMasterDataLink) obj[0];
				TBOMData data = link.getData();
				Map<String, Object> map = new HashMap<>();
				map.put("oid", data.getKePart().getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("lotNo", String.valueOf(data.getLotNo()));
				map.put("code", data.getKePart().getMaster().getCode());
				map.put("name", data.getKePart().getMaster().getName());
				map.put("model", data.getKePart().getMaster().getModel());
				map.put("keNumber", data.getKePart().getMaster().getKeNumber());
				map.put("qty", data.getQty());
				map.put("unit", data.getUnit());
				map.put("provide", data.getProvide());
				map.put("discontinue", data.getDiscontinue());
				list.add(map);
			}
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 최신버전 T-BOM
	 */
	public TBOMMaster getLatest(TBOMMaster master) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TBOMMaster.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, TBOMMaster.class, TBOMMaster.NUMBER, master.getNumber());
		QuerySpecUtils.toBooleanAnd(query, idx, TBOMMaster.class, TBOMMaster.LATEST, true);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TBOMMaster latest = (TBOMMaster) obj[0];
			return latest;
		}
		return null;
	}

	/**
	 * 버전정보
	 */
	public JSONArray history(TBOMMaster master) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TBOMMaster.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, TBOMMaster.class, TBOMMaster.NUMBER, master.getNumber());
		QuerySpecUtils.toOrderBy(query, idx, TBOMMaster.class, TBOMMaster.VERSION, true);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TBOMMaster mm = (TBOMMaster) obj[0];
			Map<String, Object> map = new HashMap();
			map.put("oid", mm.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("name", mm.getName());
			map.put("number", mm.getNumber());
			map.put("description", mm.getDescription());
			map.put("version", mm.getVersion());
			map.put("createdDate_txt", CommonUtils.getPersistableTime(mm.getCreateTimestamp()));
			map.put("creator", mm.getCreatorFullName());
			map.put("state", mm.getLifeCycleState().getDisplay());
			map.put("latest", mm.getLatest());
			map.put("secondarys", AUIGridUtils.secondaryTemplate(mm));
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 이전버전의 T-BOM
	 */
	public TBOMMaster getPreData(TBOMMaster master) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(TBOMMaster.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, TBOMMaster.class, TBOMMaster.NUMBER, master.getNumber());
		QuerySpecUtils.toEqualsAnd(query, idx, TBOMMaster.class, TBOMMaster.VERSION, master.getVersion() - 1);
		QuerySpecUtils.toBooleanAnd(query, idx, TBOMMaster.class, TBOMMaster.LATEST, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			TBOMMaster pre = (TBOMMaster) obj[0];
			return pre;
		}
		return null;
	}

	/**
	 * T-BOM 프로젝트 가져오기
	 */
	public JSONArray getProjects(TBOMMaster master) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QueryResult result = PersistenceHelper.manager.navigate(master, "project", TBOMMasterProjectLink.class);
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
}
