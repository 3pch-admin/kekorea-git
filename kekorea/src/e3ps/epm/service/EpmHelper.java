
package e3ps.epm.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import e3ps.common.util.CommonUtils;
import e3ps.common.util.FolderUtils;
import e3ps.common.util.IBAUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.epm.dto.EpmDTO;
import e3ps.epm.numberRule.NumberRule;
import e3ps.epm.numberRule.service.NumberRuleHelper;
import e3ps.epm.workOrder.WorkOrder;
import e3ps.epm.workOrder.WorkOrderDataLink;
import e3ps.epm.workOrder.WorkOrderProjectLink;
import e3ps.project.Project;
import net.sf.json.JSONArray;
import wt.clients.folder.FolderTaskLogic;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.build.EPMBuildHistory;
import wt.epm.build.EPMBuildRule;
import wt.epm.structure.EPMReferenceLink;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.IteratedFolderMemberLink;
import wt.part.WTPart;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.util.WTAttributeNameIfc;
import wt.vc.VersionControlHelper;

public class EpmHelper {

	/**
	 * 도면 저장 기본위치 제품 - 라이브러리 동일
	 */
	public static final String DEFAULT_ROOT = "/Default/도면";

	/**
	 * 제품, 라이브러리 컨데이터 구분 변수
	 */
	public static final String PRODUCT_CONTAINER = "PRODUCT";
	public static final String LIBRARY_CONTAINER = "LIBRARY";

	public static final EpmService service = ServiceFactory.getService(EpmService.class);
	public static final EpmHelper manager = new EpmHelper();

	public WTPart getPart(EPMDocument epm) throws Exception {
		WTPart part = null;
		if (epm == null) {
			return part;
		}

		QueryResult result = null;
		if (VersionControlHelper.isLatestIteration(epm)) {
			result = PersistenceHelper.manager.navigate(epm, "buildTarget", EPMBuildRule.class);
		} else {
			result = PersistenceHelper.manager.navigate(epm, "built", EPMBuildHistory.class);
		}

		while (result.hasMoreElements()) {
			part = (WTPart) result.nextElement();
		}
		return part;
	}

	public boolean isNumber(String number) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(EPMDocumentMaster.class, true);
		SearchCondition sc = new SearchCondition(EPMDocumentMaster.class, EPMDocumentMaster.NUMBER, "=",
				number.toUpperCase().trim());
		query.appendWhere(sc, new int[] { idx });
		QueryResult result = PersistenceHelper.manager.find(query);
		boolean isNumber = result.size() > 0 ? true : false;
		return isNumber;
	}

	public EPMDocument getEPM2D(EPMDocument ee) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(EPMReferenceLink.class, true);

		EPMDocumentMaster master = (EPMDocumentMaster) ee.getMaster();
		long id = master.getPersistInfo().getObjectIdentifier().getId();

		SearchCondition sc = new SearchCondition(EPMReferenceLink.class, "roleBObjectRef.key.id", "=", id);
		query.appendWhere(sc, new int[] { idx });
		query.appendAnd();

		sc = new SearchCondition(EPMReferenceLink.class, EPMReferenceLink.REFERENCE_TYPE, "=", "DRAWING");
		query.appendWhere(sc, new int[] { idx });

		EPMDocument epm2d = null;
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			EPMReferenceLink link = (EPMReferenceLink) obj[0];
			epm2d = link.getReferencedBy();
		}
		return epm2d;
	}

	/**
	 * 도면 검색
	 */
	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		System.out.println("검색 START = " + new Timestamp(new Date().getTime()));
		Map<String, Object> map = new HashMap<String, Object>();
		List<EpmDTO> list = new ArrayList<EpmDTO>();

		String fileName = (String) params.get("fileName");
		String partCode = (String) params.get("partCode");
		String number = (String) params.get("number");
		String partName = (String) params.get("partName");
		String cadType = (String) params.get("cadType");
		String material = (String) params.get("material");
		String remark = (String) params.get("remark");
		String reference = (String) params.get("reference");
		String state = (String) params.get("state");
		boolean latest = (boolean) params.get("latest");
		String oid = (String) params.get("oid"); // 폴더 OID
		String container = (String) params.get("container");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");
		String modifierOid = (String) params.get("modifierOid");
		QuerySpec query = new QuerySpec();

		int idx = query.appendClassList(EPMDocument.class, true);
		int idx_m = query.appendClassList(EPMDocumentMaster.class, false);
//
//		if (!StringUtils.isNull(fileName)) {
//			String[] s = fileName.split(",");
//			if (s.length > 0) {
//				for (int i = 0; i < s.length; i++) {
//					String ss = s[i];
////				for (String ss : s) {
////					QuerySpecUtils.toLikeOr(query, idx, Project.class, Project.KEK_NUMBER, ss);
////					QuerySpecUtils.toLikeOr(query, idx, EPMDocument.class, EPMDocument.CADNAME, ss);
//
//					ClassAttribute ca = new ClassAttribute(EPMDocument.class, EPMDocument.CADNAME);
//					ColumnExpression ce = ConstantExpression.newExpression("%" + ss.toUpperCase() + "%");
//					SQLFunction function = SQLFunction.newSQLFunction(SQLFunction.UPPER, ca);
//					SearchCondition sc = new SearchCondition(function, SearchCondition.LIKE, ce);
//					query.appendWhere(sc, new int[] { idx });
//
//					if (i != s.length - 1) {
//						if (query.getConditionCount() > 0) {
//							query.appendOr();
//						}
//					} else {
//						query.appendCloseParen();
//					}
//				}
//			}
//		}

		QuerySpecUtils.toCI(query, idx, EPMDocument.class);
		QuerySpecUtils.toInnerJoin(query, EPMDocument.class, EPMDocumentMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);

		QuerySpecUtils.toTimeGreaterAndLess(query, idx, EPMDocument.class, EPMDocument.CREATE_TIMESTAMP, createdFrom,
				createdTo);

		// 캐드파일명
		QuerySpecUtils.toLikeOr(query, idx, EPMDocument.class, EPMDocument.CADNAME, fileName);
		QuerySpecUtils.toIBALikeAnd(query, EPMDocument.class, idx, "PART_CODE", partCode);

		// 국제 전용 IBA 프로이 오토 캐드 검색용

//		if (!StringUtils.isNull(number)) {
//			String[] s = number.split(",");
//			if (s.length > 0) {
//				for (String ss : s) {
//					if (query.getConditionCount() > 0) {
//						query.appendOr();
//					}
////					QuerySpecUtils.toLikeOr(query, idx, Project.class, Project.KEK_NUMBER, ss);
//		QuerySpecUtils.queryLikeNumber(query, EPMDocument.class, idx, number);
//				}
//			}
//		}

		QuerySpecUtils.queryLikeNumber(query, EPMDocument.class, idx, number);
		QuerySpecUtils.queryLikeName(query, EPMDocument.class, idx, partName);

		// 캐드타입
		QuerySpecUtils.toEqualsAnd(query, idx, EPMDocument.class, EPMDocument.DOC_TYPE, cadType);

		QuerySpecUtils.toIBALikeAnd(query, EPMDocument.class, idx, "MATERIAL", material);
		QuerySpecUtils.toIBALikeAnd(query, EPMDocument.class, idx, "REMARKS", remark);
		QuerySpecUtils.toIBALikeAnd(query, EPMDocument.class, idx, "REF_NO", reference);
		QuerySpecUtils.toState(query, idx, EPMDocument.class, state);
		QuerySpecUtils.creatorQuery(query, idx, EPMDocument.class, creatorOid);
		QuerySpecUtils.modifierQuery(query, idx, EPMDocument.class, modifierOid);

		Folder folder = null;
		if (!StringUtils.isNull(oid)) {
			folder = (Folder) CommonUtils.getObject(oid);
		} else {
			if (PRODUCT_CONTAINER.equals(container)) {
				folder = FolderTaskLogic.getFolder(DEFAULT_ROOT, CommonUtils.getPDMLinkProductContainer());
			} else if (LIBRARY_CONTAINER.equalsIgnoreCase(container)) {
				folder = FolderTaskLogic.getFolder(DEFAULT_ROOT, CommonUtils.getWTLibraryContainer());
			}
		}

		if (folder != null) {
			if (query.getConditionCount() > 0) {
				query.appendAnd();
			}
			int f_idx = query.appendClassList(IteratedFolderMemberLink.class, false);
			ClassAttribute fca = new ClassAttribute(IteratedFolderMemberLink.class, "roleBObjectRef.key.branchId");
			SearchCondition fsc = new SearchCondition(fca, "=",
					new ClassAttribute(EPMDocument.class, "iterationInfo.branchId"));
			fsc.setFromIndicies(new int[] { f_idx, idx }, 0);
			fsc.setOuterJoin(0);
			query.appendWhere(fsc, new int[] { f_idx, idx });
			query.appendAnd();

			query.appendOpenParen();
			long fid = folder.getPersistInfo().getObjectIdentifier().getId();
			query.appendWhere(new SearchCondition(IteratedFolderMemberLink.class, "roleAObjectRef.key.id", "=", fid),
					new int[] { f_idx });

			ArrayList<Folder> folders = FolderUtils.getSubFolders(folder, new ArrayList<Folder>());
			for (int i = 0; i < folders.size(); i++) {
				Folder sub = (Folder) folders.get(i);
				query.appendOr();
				long sfid = sub.getPersistInfo().getObjectIdentifier().getId();
				query.appendWhere(
						new SearchCondition(IteratedFolderMemberLink.class, "roleAObjectRef.key.id", "=", sfid),
						new int[] { f_idx });
			}
			query.appendCloseParen();
		}

		if (latest) {
			QuerySpecUtils.toLatest(query, idx, EPMDocument.class);
		}

		System.out.println(query);

		QuerySpecUtils.toOrderBy(query, idx, EPMDocument.class, EPMDocument.MODIFY_TIMESTAMP, true);
		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			EPMDocument epm = (EPMDocument) obj[0];
			EpmDTO column = new EpmDTO(epm);
			list.add(column);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		System.out.println("검색 END = " + new Timestamp(new Date().getTime()));
		return map;
	}

	/**
	 * 도면 버전 이력
	 */
	public JSONArray versionHistory(EPMDocument epm) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		QueryResult result = VersionControlHelper.service.allIterationsOf(epm.getMaster());
		while (result.hasMoreElements()) {
			Map<String, Object> map = new HashMap<>();
			EPMDocument dd = (EPMDocument) result.nextElement();
			map.put("oid", dd.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("number", dd.getNumber());
			map.put("name", dd.getName());
			map.put("version", CommonUtils.getFullVersion(dd));
			map.put("creator", dd.getCreatorFullName());
			map.put("createdDate_txt", CommonUtils.getPersistableTime(dd.getCreateTimestamp()));
			map.put("modifier", dd.getModifierName());
			map.put("modifiedDate_txt", dd.getModifierFullName());
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 관련 작번 도면과 연결된 - 도면일람표에 사용된 도면들..
	 */
	public JSONArray jsonAuiProject(String oid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		EPMDocument epm = (EPMDocument) CommonUtils.getObject(oid);

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Project.class, true);
		int idx_l = query.appendClassList(WorkOrderProjectLink.class, false);
		int idx_w = query.appendClassList(WorkOrder.class, false);
		int idx_d = query.appendClassList(WorkOrderDataLink.class, false);

		QuerySpecUtils.toInnerJoin(query, Project.class, WorkOrderProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleBObjectRef.key.id", idx, idx_l);
		QuerySpecUtils.toInnerJoin(query, WorkOrder.class, WorkOrderProjectLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx_w, idx_l);
		QuerySpecUtils.toInnerJoin(query, WorkOrder.class, WorkOrderDataLink.class, WTAttributeNameIfc.ID_NAME,
				"roleAObjectRef.key.id", idx_w, idx_d);
		QuerySpecUtils.toEqualsAnd(query, idx_d, WorkOrderDataLink.class, "roleBObjectRef.key.id", epm);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Project project = (Project) obj[0];
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
	 * 도면 결재시 도면 추가 - 없다면 FAIL 있으면 TRUE
	 */
	public Map<String, Object> append(Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<>();
		ArrayList<Map<String, Object>> arr = (ArrayList<Map<String, Object>>) params.get("arr");

		ArrayList<Map<String, Object>> list1 = new ArrayList<>();
		ArrayList<Map<String, Object>> list2 = new ArrayList<>();

		for (Map<String, Object> map : arr) {
			String oid = (String) map.get("oid");

			Map<String, Object> map1 = new HashMap<>();
			Map<String, Object> map2 = new HashMap<>();

			EPMDocument epm = (EPMDocument) CommonUtils.getObject(oid);
			String authoringApplication = epm.getAuthoringApplication().getDisplay();
			String dwgNo = "";
			String nameOfParts = "";
			if (authoringApplication.equalsIgnoreCase("CREO")) {
				dwgNo = IBAUtils.getStringValue(epm, "DWG_NO");
				nameOfParts = IBAUtils.getStringValue(epm, "NAME_OF_PARTS");
			} else if (authoringApplication.equalsIgnoreCase("AUTOCAD")) {
				dwgNo = IBAUtils.getStringValue(epm, "DWG_No");
				nameOfParts = IBAUtils.getStringValue(epm, "TITLE1") + " " + IBAUtils.getStringValue(epm, "TITLE2");
			}
			String version = epm.getVersionIdentifier().getSeries().getValue(); // 리비전으로만 체크한다..

			NumberRule numberRule = NumberRuleHelper.manager.numberRuleForNumberAndVersion(dwgNo, version);
			if (numberRule != null) {
				map2.put("number", numberRule.getMaster().getNumber());
				map2.put("size_txt",
						numberRule.getMaster().getSize() == null ? "" : numberRule.getMaster().getSize().getName());
				map2.put("lotNo", numberRule.getMaster().getLotNo());
				map2.put("unitName", numberRule.getMaster().getUnitName());
				map2.put("name", numberRule.getMaster().getName());
				map2.put("businessSector_txt",
						numberRule.getMaster().getSector() == null ? "" : numberRule.getMaster().getSector().getName());
				map2.put("classificationWritingDepartments_txt", numberRule.getMaster().getDepartment() == null ? ""
						: numberRule.getMaster().getDepartment().getName());
				map2.put("writtenDocuments_txt", numberRule.getMaster().getDocument() == null ? ""
						: numberRule.getMaster().getDocument().getName());
				map2.put("version", numberRule.getVersion());
				map2.put("state", numberRule.getState());
				map2.put("creator", numberRule.getMaster().getOwnership().getOwner().getFullName());
				map2.put("createdDate_txt",
						CommonUtils.getPersistableTime(numberRule.getMaster().getCreateTimestamp()));
				map2.put("modifier", numberRule.getOwnership().getOwner().getFullName());
				map2.put("modifiedDate_txt", CommonUtils.getPersistableTime(numberRule.getCreateTimestamp()));
				map2.put("oid", numberRule.getPersistInfo().getObjectIdentifier().getStringValue());
				map2.put("eoid", epm.getPersistInfo().getObjectIdentifier().getStringValue());
				list2.add(map2);
			}

			map1.put("name", epm.getName());
			map1.put("dwg_no", dwgNo);
			map1.put("name_of_parts", nameOfParts);
			map1.put("version", version);
			map1.put("state", epm.getLifeCycleState().getDisplay());
			map1.put("creator", epm.getCreatorFullName());
			map1.put("createdDate_txt", CommonUtils.getPersistableTime(epm.getCreateTimestamp()));
			map1.put("oid", oid);
			list1.add(map1);
		}

		result.put("list1", list1);
		result.put("list2", list2);

		return result;

	}

	/**
	 * ERP 전송 함수 (2D)전용
	 */
	public void sendToErp(Map<String, Object> params) throws Exception {
		// TODO Auto-generated method stub

	}
}
