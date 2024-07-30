package e3ps.bom.partlist.service;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import e3ps.bom.partlist.MasterDataLink;
import e3ps.bom.partlist.PartListData;
import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.PartListMasterProjectLink;
import e3ps.bom.partlist.dto.PartListDTO;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.project.Project;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.query.ClassAttribute;
import wt.query.ColumnExpression;
import wt.query.ConstantExpression;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.util.WTAttributeNameIfc;

public class PartlistHelper {

	public static final PartlistService service = ServiceFactory.getService(PartlistService.class);
	public static final PartlistHelper manager = new PartlistHelper();

	/**
	 * 수배표 가져오는 함수
	 */
	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		String name = (String) params.get("partlistName");
		String state = (String) params.get("state");
		String kekNumber = (String) params.get("kekNumber");
		String keNumber = (String) params.get("keNumber");
//		String description = (String) params.get("description");
		String engType = (String) params.get("engType");
		String pdateFrom = (String) params.get("pdateFrom");
		String pdateTo = (String) params.get("pdateTo");
//		String customer_name = (String) params.get("customer_name");
//		String install_name = (String) params.get("install_name");
//		String mak_name = (String) params.get("mak_name");
//		String detail_name = (String) params.get("detail_name");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");

		QuerySpec qs = new QuerySpec();
		int idx = qs.appendClassList(PartListMaster.class, true);

		QuerySpecUtils.toLikeAnd(qs, idx, PartListMaster.class, PartListMaster.NAME, name);
		QuerySpecUtils.toEqualsAnd(qs, idx, PartListMaster.class, PartListMaster.ENG_TYPE, engType);
		QuerySpecUtils.toEqualsAnd(qs, idx, PartListMaster.class, PartListMaster.LIFE_CYCLE_STATE, state);

		if (!StringUtils.isNull(creatorOid)) {
			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			SearchCondition sc = new SearchCondition(PartListMaster.class, "creator.key.id", "=",
					CommonUtils.getOIDLongValue(creatorOid));
			qs.appendWhere(sc, new int[] { idx });
		}

		if (!StringUtils.isNull(kekNumber) || !StringUtils.isNull(keNumber) || !StringUtils.isNull(pdateFrom)
				|| !StringUtils.isNull(pdateTo)) {

			int idx_p = qs.appendClassList(Project.class, true);
			int idx_l = qs.appendClassList(PartListMasterProjectLink.class, false);

			ClassAttribute ca1 = new ClassAttribute(PartListMaster.class, "thePersistInfo.theObjectIdentifier.id");
			ClassAttribute ca3 = new ClassAttribute(PartListMasterProjectLink.class, "roleAObjectRef.key.id");

			qs.appendWhere(new SearchCondition(ca1, "=", ca3), new int[] { idx, idx_l });

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}

			ca3 = new ClassAttribute(PartListMasterProjectLink.class, "roleBObjectRef.key.id");
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

		QuerySpecUtils.toTimeGreaterAndLess(qs, idx, PartListMaster.class, PartListMaster.CREATE_TIMESTAMP, createdFrom,
				createdTo);
		QuerySpecUtils.toOrderBy(qs, idx, PartListMaster.class, PartListMaster.CREATE_TIMESTAMP, true);

		qs.setAdvancedQueryEnabled(true);
		qs.setDescendantQuery(false);

		PageQueryUtils pager = new PageQueryUtils(params, qs);
		PagingQueryResult result = pager.find();

		JSONArray list = new JSONArray();
		while (result.hasMoreElements()) {

			Object[] obj = (Object[]) result.nextElement();
			PartListMaster plMaster = (PartListMaster) obj[0];

			PartListDTO dto = new PartListDTO(plMaster);

			JSONObject node = new JSONObject();

			node.put("oid", plMaster.getPersistInfo().getObjectIdentifier().getStringValue());
			node.put("name", plMaster.getName());
			node.put("number", plMaster.getNumber());
			node.put("eng", plMaster.getEngType());
			node.put("kekNumber", dto.getTotalKekNumber());
			node.put("keNumber", dto.getTotalKeNumber());
			node.put("state", dto.getState());
			node.put("creator", dto.getCreator());
			node.put("createdDate_txt", dto.getCreatedDate_txt());
			node.put("modifiedDate_txt", dto.getModifiedDate_txt());

			list.add(node);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 수배된 데이터들 ArrayList 로 가져오기
	 */
	public ArrayList<PartListData> getPartListData(String oid) throws Exception {
		PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);
		return getPartListData(master);
	}

	/**
	 * 수배된 데이터들 ArrayList 로 가져오기
	 */
	public ArrayList<PartListData> getPartListData(PartListMaster master) throws Exception {
		ArrayList<PartListData> list = new ArrayList<PartListData>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(MasterDataLink.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, MasterDataLink.class, "roleAObjectRef.key.id", master);
		QuerySpecUtils.toOrderBy(query, idx, MasterDataLink.class, MasterDataLink.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			MasterDataLink link = (MasterDataLink) obj[0];
			list.add(link.getData());
		}
		return list;
	}

	/**
	 * 수배표된 데이터들을 JSONArray 형태로 가져오는 함수
	 */
	public JSONArray getData(String oid) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(MasterDataLink.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, MasterDataLink.class, "roleAObjectRef.key.id",
				master.getPersistInfo().getObjectIdentifier().getId());
		QuerySpecUtils.toOrderBy(query, idx, MasterDataLink.class, MasterDataLink.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			MasterDataLink link = (MasterDataLink) obj[0];
			PartListData data = link.getData();

//			WTPart wtPart = PartHelper.manager.getWTPart(data.getPartNo());

//			String s = data.getPartNo();

			Map<String, Object> map = new HashMap<>();
			map.put("check", "OK");
			map.put("lotNo", data.getLotNo());
			map.put("unitName", data.getUnitName());
			map.put("partNo", data.getPartNo());
			map.put("partName", data.getPartName());
			map.put("standard", data.getStandard());
			map.put("maker", data.getMaker());
			map.put("customer", data.getCustomer());
			map.put("quantity", data.getQuantity());
			map.put("unit", data.getUnit());
			map.put("price", data.getPrice());
			map.put("currency", data.getCurrency());
			map.put("won", data.getWon());
			map.put("partListDate_txt", CommonUtils.getPersistableTime(data.getPartListDate()));
			map.put("exchangeRate", data.getExchangeRate());
			map.put("referDrawing", data.getReferDrawing());
			map.put("classification", data.getClassification());
			map.put("note", data.getNote());

//				int _idx = qs.appendClassList(WTPart.class, true);
//			QuerySpecUtils.queryEqualsNumber(qs, WTPart.class, _idx, s);
//			QuerySpecUtils.toOrderBy(qs, _idx, WTPart.class, WTPart.CREATE_TIMESTAMP, true);
//			QueryResult qr = PersistenceHelper.manager.find(qs);
//			System.out.println("==" + qr.size());
//			if (qr.hasMoreElements()) {
//				Object[] o = (Object[]) qr.nextElement();
//				WTPart pp = (WTPart) o[0];
//				map.put("poid", pp.getPersistInfo().getObjectIdentifier().getStringValue());
//			}		QuerySpec qs = new QuerySpec();

			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 수배표된 데이터들을 ArrayList<Map<String, Object>> 형태로 가져오는 함수
	 */
	public ArrayList<Map<String, Object>> getArrayMap(String oid) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(MasterDataLink.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, MasterDataLink.class, "roleAObjectRef.key.id",
				master.getPersistInfo().getObjectIdentifier().getId());
		QuerySpecUtils.toOrderBy(query, idx, MasterDataLink.class, MasterDataLink.SORT, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			MasterDataLink link = (MasterDataLink) obj[0];
			PartListData data = link.getData();

			Map<String, Object> map = new HashMap<>();
			map.put("oid", data.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("lotNo", data.getLotNo());
			map.put("unitName", data.getUnitName());
			map.put("partNo", data.getPartNo());
			map.put("partName", data.getPartName());
			map.put("standard", data.getStandard());
			map.put("maker", data.getMaker());
			map.put("customer", data.getCustomer());
			map.put("quantity", data.getQuantity());
			map.put("unit", data.getUnit());
			map.put("price", data.getPrice());
			map.put("currency", data.getCurrency());
			map.put("won", data.getWon());
			map.put("partListDate", data.getPartListDate());
			map.put("exchangeRate", data.getExchangeRate());
			map.put("referDrawing", data.getReferDrawing());
			map.put("classification", data.getClassification());
			map.put("note", data.getNote());
			list.add(map);
		}
		return list;
	}

	/**
	 * 수배표 관련 작번 리스트
	 */
	public JSONArray jsonAuiProject(String oid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(PartListMaster.class, true);
		int idx_link = query.appendClassList(PartListMasterProjectLink.class, true);
		QuerySpecUtils.toInnerJoin(query, PartListMaster.class, PartListMasterProjectLink.class,
				WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, PartListMasterProjectLink.class, "roleAObjectRef.key.id", master);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			PartListMasterProjectLink link = (PartListMasterProjectLink) obj[1];
			Project project = link.getProject();
			Map<String, String> map = new HashMap<>();
			map.put("oid", project.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("projectType_name", project.getProjectType() != null ? project.getProjectType().getName() : "");
			map.put("customer_name", project.getCustomer() != null ? project.getCustomer().getName() : "");
			map.put("mak_name", project.getMak() != null ? project.getMak().getName() : "");
			map.put("install_name", project.getInstall() != null ? project.getInstall().getName() : "");
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
	 * 수배표 비교
	 */
	public ArrayList<Map<String, Object>> compare(Project p1, ArrayList<Project> destList, String invoke)
			throws Exception {

		long start = System.currentTimeMillis() / 1000;
		System.out.println("수배표 비교 START = " + start);
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		ArrayList<Map<String, Object>> mergedList = new ArrayList<>();
		String[] t = null;
		if ("a-t".equals(invoke)) {
			t = new String[] { "기계_1차_수배", "기계_2차_수배", "전기_1차_수배", "전기_2차_수배" };
			list = integratedData(p1, t);
		} else if ("a".equals(invoke)) {
			t = new String[] { "기계_1차_수배", "기계_2차_수배", "전기_1차_수배", "전기_2차_수배" };
			list = integratedData(p1, t);
		} else if ("m".equals(invoke)) {
			t = new String[] { "기계_1차_수배", "기계_2차_수배" };
			list = integratedData(p1, t);
		} else if ("e".equals(invoke)) {
			t = new String[] { "전기_1차_수배", "전기_2차_수배" };
			list = integratedData(p1, t);
		}

		Map<String, Object> makList = new HashMap<>();
		Map<String, Object> customerList = new HashMap<>();
		Map<String, Object> keList = new HashMap<>();
		Map<String, Object> pdateList = new HashMap<>();

		makList.put("engType", "막종 / 막종상세");
		customerList.put("engType", "고객사 / 설치장소");
		keList.put("engType", "KE 작번");
		pdateList.put("engType", "발행일");

		makList.put("lotNo", "막종 / 막종상세");
		customerList.put("lotNo", "고객사 / 설치장소");
		keList.put("lotNo", "KE 작번");
		pdateList.put("lotNo", "발행일");

		makList.put("unitName", "막종 / 막종상세");
		customerList.put("unitName", "고객사 / 설치장소");
		keList.put("unitName", "KE 작번");
		pdateList.put("unitName", "발행일");

		makList.put("partNo", "막종 / 막종상세");
		customerList.put("partNo", "고객사 / 설치장소");
		keList.put("partNo", "KE 작번");
		pdateList.put("partNo", "발행일");

		destList.add(0, p1);
		for (int i = 0; i < destList.size(); i++) {
			Project project = (Project) destList.get(i);
			String oid = project.getPersistInfo().getObjectIdentifier().getStringValue();
			makList.put("oid", oid);
			customerList.put("oid", oid);
			keList.put("oid", oid);
			pdateList.put("oid", oid);

			String mak = project.getMak() != null ? project.getMak().getName() : "";
			String detail = project.getDetail() != null ? project.getDetail().getName() : "";
			String customer = project.getCustomer() != null ? project.getCustomer().getName() : "";
			String install = project.getInstall() != null ? project.getInstall().getName() : "";

			makList.put("quantity" + (i + 1), mak + " / " + detail);
			customerList.put("quantity" + (i + 1), customer + " / " + install);
			keList.put("quantity" + (i + 1), project.getKeNumber());
			pdateList.put("quantity" + (i + 1), CommonUtils.getPersistableTime(project.getPDate()));
		}

		mergedList.add(makList);
		mergedList.add(customerList);
		mergedList.add(keList);
		mergedList.add(pdateList);

		destList.remove(0);

		// list1의 데이터를 먼저 추가
		for (Map<String, Object> data : list) {
			Map<String, Object> mergedData = new HashMap<>();
			mergedData.put("oid", data.get("oid"));
			mergedData.put("engType", data.get("engType"));
			mergedData.put("lotNo", data.get("lotNo"));
			mergedData.put("unitName", data.get("unitName"));
			mergedData.put("partNo", data.get("partNo"));
			mergedData.put("partName", data.get("partName"));
			mergedData.put("standard", data.get("standard"));
			mergedData.put("maker", data.get("maker"));
			mergedData.put("customer", data.get("customer"));
			mergedData.put("quantity1", data.get("quantity"));
			mergedData.put("unit", data.get("unit"));
			mergedData.put("price", data.get("price"));
			mergedData.put("currency", data.get("currency"));
			mergedData.put("won", data.get("won"));
			mergedData.put("partListDate_txt", data.get("partListDate_txt"));
			mergedData.put("exchangeRate", data.get("exchangeRate"));
			mergedData.put("referDrawing", data.get("referDrawing"));
			mergedData.put("classification", data.get("classification"));
			mergedData.put("note", data.get("note"));
			mergedList.add(mergedData);
		}

		// 전체 작번 START
		for (int i = 0; i < destList.size(); i++) {
			Project p2 = (Project) destList.get(i);
			ArrayList<Map<String, Object>> _list = integratedData(p2, t);
			for (Map<String, Object> data : _list) {
				String partNo = (String) data.get("partNo");
				String lotNo = (String) data.get("lotNo");
				String key = partNo + "-" + lotNo;
//			String key = partNo;
				boolean isExist = false;

				// mergedList에 partNo가 동일한 데이터가 있는지 확인
				for (Map<String, Object> mergedData : mergedList) {
					String mergedPartNo = (String) mergedData.get("partNo");
					String mergedLotNo = (String) mergedData.get("lotNo");
					String _key = mergedPartNo + "-" + mergedLotNo;

					if (key.equals(_key)) {
						// partNo가 동일한 데이터가 있으면 데이터를 업데이트하고 isExist를 true로 변경
						mergedData.put("quantity" + (2 + i), data.get("quantity"));
						isExist = true;
						break;
					}
				}

				if (!isExist) {
					// partNo가 동일한 데이터가 없으면 mergedList에 데이터를 추가
					Map<String, Object> mergedData = new HashMap<>();
					mergedData.put("oid", data.get("oid"));
					mergedData.put("engType", data.get("engType"));
					mergedData.put("lotNo", data.get("lotNo"));
					mergedData.put("unitName", data.get("unitName"));
					mergedData.put("partNo", data.get("partNo"));
					mergedData.put("partName", data.get("partName"));
					mergedData.put("standard", data.get("standard"));
					mergedData.put("maker", data.get("maker"));
					mergedData.put("customer", data.get("customer"));
					mergedData.put("model", data.get("model"));
					mergedData.put("quantity" + (2 + i), data.get("quantity"));
					mergedData.put("unit", data.get("unit"));
					mergedData.put("price", data.get("price"));
					mergedData.put("currency", data.get("currency"));
					mergedData.put("won", data.get("won"));
					mergedData.put("partListDate_txt", data.get("partListDate_txt"));
					mergedData.put("exchangeRate", data.get("exchangeRate"));
					mergedData.put("referDrawing", data.get("referDrawing"));
					mergedData.put("classification", data.get("classification"));
					mergedData.put("note", data.get("note"));
					mergedList.add(mergedData);
				}
			}
		}
		long end = System.currentTimeMillis() / 1000;
		System.out.println("수배표 비교 END = " + end + ", 걸린 시간 = " + (end - start));
		return mergedList;
	}

	/**
	 * 수배표 비교 데이터
	 */
	public ArrayList<Map<String, Object>> integratedData(Project project, String[] t) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(PartListMaster.class, true);
		int idx_link = query.appendClassList(PartListMasterProjectLink.class, false);
		int idx_p = query.appendClassList(Project.class, false);

		QuerySpecUtils.toInnerJoin(query, PartListMaster.class, PartListMasterProjectLink.class,
				WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", idx, idx_link);
		QuerySpecUtils.toInnerJoin(query, Project.class, PartListMasterProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx_p, idx_link);
		QuerySpecUtils.toEqualsAnd(query, idx_link, PartListMasterProjectLink.class, "roleBObjectRef.key.id", project);
		if (t != null && t.length > 0) {
			QuerySpecUtils.toIn(query, idx, PartListMaster.class, PartListMaster.ENG_TYPE, t);
		}
		QuerySpecUtils.toOrderBy(query, idx, PartListMaster.class, PartListMaster.CREATE_TIMESTAMP, false);

		ArrayList<String> comp = new ArrayList<>();

		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			PartListMaster master = (PartListMaster) obj[0];

			QuerySpec _query = new QuerySpec();
			int _idx = _query.appendClassList(PartListMaster.class, false);
			int _idx_link = _query.appendClassList(MasterDataLink.class, true);
			int idx_data = _query.appendClassList(PartListData.class, false);
			QuerySpecUtils.toInnerJoin(_query, PartListMaster.class, MasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleAObjectRef.key.id", _idx, _idx_link);
			QuerySpecUtils.toInnerJoin(_query, PartListData.class, MasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleBObjectRef.key.id", idx_data, _idx_link);
			QuerySpecUtils.toEqualsAnd(_query, _idx_link, MasterDataLink.class, "roleAObjectRef.key.id", master);
			QuerySpecUtils.toOrderBy(_query, idx_data, PartListData.class, PartListData.SORT, false);
			QueryResult qr = PersistenceHelper.manager.find(_query);
			while (qr.hasMoreElements()) {
				Object[] oo = (Object[]) qr.nextElement();
				MasterDataLink link = (MasterDataLink) oo[0];
				PartListData data = link.getData();
				WTPart part = data.getWtPart();

				String key = data.getPartNo() + "-" + data.getLotNo();

				if (!comp.contains(key)) {

					for (int i = 0; i < list.size(); i++) {
						Map<String, Object> mm = (Map<String, Object>) list.get(i);
						String k = (String) mm.get(key);

						if (!StringUtils.isNull(k)) {
							int qu = (int) mm.get("quantity");
							mm.put("quantity", qu + data.getQuantity());

							list.remove(i);
							list.add(mm);
						}
					}
					comp.add(key);
				} else {
					Map<String, Object> map = new HashMap<>();
					map.put("oid", part != null ? part.getPersistInfo().getObjectIdentifier().getStringValue() : "");
					map.put("engType", master.getEngType());
					map.put("lotNo", String.valueOf(data.getLotNo()));
					map.put("unitName", data.getUnitName());
					map.put("partNo", data.getPartNo());
					map.put("partName", data.getPartName());
					map.put("standard", data.getStandard());
					map.put("maker", data.getMaker());
					map.put("customer", data.getCustomer());
					map.put("quantity", data.getQuantity());
					map.put("unit", data.getUnit());
					map.put("price", data.getPrice());
					map.put("currency", data.getCurrency());
					map.put("won", data.getWon());
					map.put("partListDate_txt", CommonUtils.getPersistableTime(data.getPartListDate()));
					map.put("exchangeRate", data.getExchangeRate());
					map.put("referDrawing", data.getReferDrawing());
					map.put("classification", data.getClassification());
					map.put("note", data.getNote());
					map.put("key", key);
				}

				list.add(map);
			}
		}

		for (Map<String, Object> mm : list) {

		}

		return list;
	}

	/**
	 * 결재 정보창에서 볼 수배표 데이터
	 */
	public JSONArray jsonAuiWorkSpaceData(String oid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);
		QueryResult result = PersistenceHelper.manager.navigate(master, "project", PartListMasterProjectLink.class,
				false);
		while (result.hasMoreElements()) {
			PartListMasterProjectLink link = (PartListMasterProjectLink) result.nextElement();
			Project project = link.getProject();
			Map<String, String> map = new HashMap<>();
			map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("name", master.getName());
			map.put("state", master.getLifeCycleState().getDisplay());
			map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getModifyTimestamp()));
			map.put("creator", master.getCreatorFullName());
			map.put("projectType_name", project.getProjectType() != null ? project.getProjectType().getName() : "");
			map.put("kekNumber", project.getKekNumber());
			map.put("keNumber", project.getKeNumber());
			map.put("customer_name", project.getCustomer() != null ? project.getCustomer().getName() : "");
			map.put("mak_name", project.getMak() != null ? project.getMak().getName() : "");
			map.put("detail_name", project.getDetail() != null ? project.getDetail().getName() : "");
			map.put("description", project.getDescription());
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 프로젝트 수배표 탭
	 */
	public JSONArray partlistTab(String oid, String invoke) throws Exception {
		System.out.println("탭이 출력되나?");
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		Project project = (Project) CommonUtils.getObject(oid);
		String[] t = null;

		if ("a".equals(invoke)) {
			t = new String[] { "기계_1차_수배", "기계_2차_수배", "전기_1차_수배", "전기_2차_수배" };
		} else if ("m".equals(invoke)) {
			t = new String[] { "기계_1차_수배", "기계_2차_수배" };
		} else if ("e".equals(invoke)) {
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
		if (t != null && t.length > 0) {
			QuerySpecUtils.toIn(query, idx, PartListMaster.class, PartListMaster.ENG_TYPE, t);
		}
		QuerySpecUtils.toOrderBy(query, idx, PartListMaster.class, PartListMaster.CREATE_TIMESTAMP, false);

		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			PartListMaster master = (PartListMaster) obj[0];

			QuerySpec _query = new QuerySpec();
			int _idx = _query.appendClassList(PartListMaster.class, false);
			int _idx_link = _query.appendClassList(MasterDataLink.class, true);
			int idx_data = _query.appendClassList(PartListData.class, false);
			QuerySpecUtils.toInnerJoin(_query, PartListMaster.class, MasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleAObjectRef.key.id", _idx, _idx_link);
			QuerySpecUtils.toInnerJoin(_query, PartListData.class, MasterDataLink.class, WTAttributeNameIfc.ID_NAME,
					"roleBObjectRef.key.id", idx_data, _idx_link);
			QuerySpecUtils.toEqualsAnd(_query, _idx_link, MasterDataLink.class, "roleAObjectRef.key.id", master);
			QuerySpecUtils.toOrderBy(_query, idx_data, PartListData.class, PartListData.SORT, false);
			QueryResult qr = PersistenceHelper.manager.find(_query);
			while (qr.hasMoreElements()) {
				Object[] oo = (Object[]) qr.nextElement();
				MasterDataLink link = (MasterDataLink) oo[0];
				PartListData data = link.getData();
				Map<String, Object> map = new HashMap<>();
				map.put("engType", master.getEngType());
				map.put("lotNo", String.valueOf(data.getLotNo()));
				map.put("unitName", data.getUnitName());
				map.put("partNo", data.getPartNo());
				map.put("partName", data.getPartName());
				map.put("standard", data.getStandard());
				map.put("maker", data.getMaker());
				map.put("customer", data.getCustomer());
				map.put("quantity", data.getQuantity());
				map.put("unit", data.getUnit());
				map.put("price", data.getPrice());
				map.put("currency", data.getCurrency());
				map.put("won", data.getWon());
				map.put("partListDate_txt", CommonUtils.getPersistableTime(data.getPartListDate()));
				map.put("exchangeRate", data.getExchangeRate());
				map.put("referDrawing", data.getReferDrawing());
				map.put("classification", data.getClassification());
				map.put("note", data.getNote());
				list.add(map);
			}
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 수배표 프로젝트 가져오기
	 */
	public JSONArray getProjects(PartListMaster mm) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QueryResult result = PersistenceHelper.manager.navigate(mm, "project", PartListMasterProjectLink.class);
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
	 * 수배표 번호
	 */
	public String getNextNumber() throws Exception {

		Calendar ca = Calendar.getInstance();
		int month = ca.get(Calendar.MONTH) + 1;
		int year = ca.get(Calendar.YEAR);
		DecimalFormat df = new DecimalFormat("00");
		String number = "PP-" + df.format(year).substring(2) + df.format(month) + "-";

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(PartListMaster.class, true);

		QuerySpecUtils.toLikeRightAnd(query, idx, PartListMaster.class, PartListMaster.NUMBER, number);
		QuerySpecUtils.toOrderBy(query, idx, PartListMaster.class, PartListMaster.NUMBER, true);

		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			PartListMaster master = (PartListMaster) obj[0];

			String s = master.getNumber().substring(master.getNumber().lastIndexOf("-") + 1);

			int ss = Integer.parseInt(s) + 1;
			DecimalFormat d = new DecimalFormat("0000");
			number += d.format(ss);
		} else {
			number += "0001";
		}
		return number;
	}
}
