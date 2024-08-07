package e3ps.project.output.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import e3ps.admin.commonCode.CommonCode;
import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.tbom.TBOMMaster;
import e3ps.common.util.AUIGridUtils;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.FolderUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.doc.E3PSDocumentMaster;
import e3ps.doc.PRJDocument;
import e3ps.epm.workOrder.WorkOrder;
import e3ps.korea.configSheet.ConfigSheet;
import e3ps.project.Project;
import e3ps.project.output.Output;
import e3ps.project.output.OutputDocumentLink;
import e3ps.project.output.OutputProjectLink;
import e3ps.project.output.dto.OutputDTO;
import e3ps.project.task.Task;
import net.sf.json.JSONArray;
import wt.clients.folder.FolderTaskLogic;
import wt.content.ContentHolder;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.enterprise.RevisionControlled;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.IteratedFolderMemberLink;
import wt.lifecycle.LifeCycleManaged;
import wt.query.ClassAttribute;
import wt.query.ColumnExpression;
import wt.query.ConstantExpression;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.util.WTAttributeNameIfc;

public class OutputHelper {

	public static final OutputHelper manager = new OutputHelper();
	public static final OutputService service = ServiceFactory.getService(OutputService.class);

	/**
	 * 산출물 경로
	 */
	public static final String OUTPUT_NEW_ROOT = "/Default/프로젝트";
	public static final String OUTPUT_OLD_ROOT = "/Default/문서/프로젝트";

	/**
	 * 산출물 검색
	 */
	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();
		ArrayList<OutputDTO> list = new ArrayList<>();

		String name = (String) params.get("name");
		String number = (String) params.get("number");
		String content = (String) params.get("content");
		String kekNumber = (String) params.get("kekNumber");
		String keNumber = (String) params.get("keNumber");
		String description = (String) params.get("description");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");
		String state = (String) params.get("state");
		boolean latest = (boolean) params.get("latest");
		String oid = (String) params.get("oid"); // 폴더 OID
		String type = (String) params.get("type");
		String mak = (String) params.get("mak");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(WTDocument.class, true);
		int idx_m = query.appendClassList(WTDocumentMaster.class, false);

		query.setAdvancedQueryEnabled(true);
		query.setDescendantQuery(false);

		QuerySpecUtils.toCI(query, idx, WTDocument.class);
		QuerySpecUtils.toInnerJoin(query, WTDocument.class, WTDocumentMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);

		QuerySpecUtils.toLikeAnd(query, idx, WTDocument.class, WTDocument.NAME, name);
		QuerySpecUtils.toLikeAnd(query, idx, WTDocument.class, WTDocument.NUMBER, number);
		QuerySpecUtils.toLikeAnd(query, idx, WTDocument.class, WTDocument.DESCRIPTION, content);

		if (!StringUtils.isNull(kekNumber)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
//			QuerySpecUtils.toLikeAnd(query, idx_p, Project.class, Project.KEK_NUMBER, kekNumber);

			if (!StringUtils.isNull(kekNumber)) {
				if (query.getConditionCount() > 0) {
					query.appendAnd();
				}
				query.appendOpenParen();
				String[] s = kekNumber.split(",");
				if (s.length > 0) {
					for (int k = 0; k < s.length; k++) {
						String ss = s[k];
						if (k != 0) {
							if (query.getConditionCount() > 0) {
								query.appendOr();
							}
						}
						ClassAttribute ca = new ClassAttribute(Project.class, Project.KEK_NUMBER);
						ColumnExpression ce = ConstantExpression.newExpression("%" + ss.toUpperCase() + "%");
						SQLFunction function = SQLFunction.newSQLFunction(SQLFunction.UPPER, ca);
						sc = new SearchCondition(function, SearchCondition.LIKE, ce);
						query.appendWhere(sc, new int[] { idx });
					}
				}
				query.appendCloseParen();
			}
		}

		if (!StringUtils.isNull(keNumber)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
			QuerySpecUtils.toLikeAnd(query, idx_p, Project.class, Project.KE_NUMBER, keNumber);
		}

		if (!StringUtils.isNull(description)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
			QuerySpecUtils.toLikeAnd(query, idx_p, Project.class, Project.DESCRIPTION, description);
		}

		if (!StringUtils.isNull(mak)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
			CommonCode makCode = (CommonCode) CommonUtils.getObject(mak);
			QuerySpecUtils.toEqualsAnd(query, idx_p, Project.class, "makReference.key.id", makCode);
		}

		Folder folder = null;
		if (!StringUtils.isNull(oid)) {
			folder = (Folder) CommonUtils.getObject(oid);
		} else {
			if ("new".equalsIgnoreCase(type)) {
				folder = FolderTaskLogic.getFolder(OUTPUT_NEW_ROOT, CommonUtils.getPDMLinkProductContainer());
			} else if ("old".equalsIgnoreCase(type)) {
				folder = FolderTaskLogic.getFolder(OUTPUT_OLD_ROOT, CommonUtils.getPDMLinkProductContainer());
			}
		}

		if (folder != null) {
			if (query.getConditionCount() > 0) {
				query.appendAnd();
			}
			int f_idx = query.appendClassList(IteratedFolderMemberLink.class, false);
			ClassAttribute fca = new ClassAttribute(IteratedFolderMemberLink.class, "roleBObjectRef.key.branchId");
			SearchCondition fsc = new SearchCondition(fca, "=",
					new ClassAttribute(WTDocument.class, "iterationInfo.branchId"));
			fsc.setFromIndicies(new int[] { f_idx, idx }, 0);
			fsc.setOuterJoin(0);
			query.appendWhere(fsc, new int[] { f_idx, idx });
			query.appendAnd();

			query.appendOpenParen();
			long fid = folder.getPersistInfo().getObjectIdentifier().getId();
			query.appendWhere(new SearchCondition(IteratedFolderMemberLink.class, "roleAObjectRef.key.id", "=", fid),
					new int[] { f_idx });

			ArrayList<Folder> folders = FolderUtils.recurciveFolder(folder, new ArrayList<Folder>());
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
			QuerySpecUtils.toLatest(query, idx, WTDocument.class);
		}

		QuerySpecUtils.creatorQuery(query, idx, WTDocument.class, creatorOid);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, WTDocument.class, WTDocument.CREATE_TIMESTAMP, createdFrom,
				createdTo);
		QuerySpecUtils.toState(query, idx, WTDocument.class, state);

		QuerySpecUtils.toOrderBy(query, idx, WTDocument.class, WTDocument.MODIFY_TIMESTAMP, true);

		System.out.println("qer=" + query);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			WTDocument output = (WTDocument) obj[0];
			OutputDTO dto = new OutputDTO(output);
			list.add(dto);
		}

		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 산출물과 연결된 프로젝트
	 */
	public JSONArray jsonAuiProject(String oid) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		Output output = (Output) CommonUtils.getObject(oid);
		QueryResult result = PersistenceHelper.manager.navigate(output, "project", OutputProjectLink.class);
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
	 * 산출물 문서 번호
	 */
	public String getNextNumber() throws Exception {

		Calendar ca = Calendar.getInstance();
//		int day = ca.get(Calendar.DATE);
		int month = ca.get(Calendar.MONTH) + 1;
		int year = ca.get(Calendar.YEAR);
		DecimalFormat df = new DecimalFormat("00");
		String number = "PJ-" + df.format(year).substring(2) + df.format(month) + "-";

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
			DecimalFormat d = new DecimalFormat("0000");
			number += d.format(ss);
		} else {
			number += "0001";
		}
		return number;
	}

	/**
	 * 산출물과 관련된 작번들
	 */
	public JSONArray getProjects(WTDocument document) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QueryResult result = PersistenceHelper.manager.navigate(document, "output", OutputDocumentLink.class);
		while (result.hasMoreElements()) {
			Output output = (Output) result.nextElement();

			QueryResult qr = PersistenceHelper.manager.navigate(output, "project", OutputProjectLink.class);
			while (qr.hasMoreElements()) {
				Project project = (Project) qr.nextElement();
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
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 작번 모든 산출물 리스트
	 */
	public JSONArray outputTab(String oid) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();

		Project project = (Project) CommonUtils.getObject(oid);
		QueryResult result = PersistenceHelper.manager.navigate(project, "output", OutputProjectLink.class);
		while (result.hasMoreElements()) {
			Output output = (Output) result.nextElement();
			Task task = output.getTask();

			if (task == null) {
				continue;
			}

			LifeCycleManaged lcm = output.getDocument();
			// 산출물 일괄결재
			Map<String, Object> map = new HashMap<>();
			map.put("poid", project.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("kekNumber", project.getKekNumber());
			map.put("toid", task.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("taskName", task.getName());
			if (lcm == null) {
				E3PSDocumentMaster mm = output.getMaster();
				if (mm != null) {
					RevisionControlled rc = CommonUtils.getLatestObject(mm);
					map.put("oid", rc.getPersistInfo().getObjectIdentifier().getStringValue());
					map.put("type", "산출물(구)");
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

				map.put("loid", lcm != null ? lcm.getPersistInfo().getObjectIdentifier().getStringValue() : "");
				if (lcm instanceof ConfigSheet) {
					ConfigSheet configSheet = (ConfigSheet) lcm;
					if (configSheet.getLatest()) {
						map.put("oid", configSheet.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("type", "CONFIG SHEET");
						map.put("name", configSheet.getName());
						map.put("state", configSheet.getLifeCycleState().getDisplay());
						map.put("creator", configSheet.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(configSheet.getCreateTimestamp()));
						map.put("version", String.valueOf(configSheet.getVersion()));
						map.put("primary", AUIGridUtils.primaryTemplate(configSheet));
						map.put("secondary", AUIGridUtils.secondaryTemplate(configSheet));
					} else {
						continue;
					}
					// tbom
				} else if (lcm instanceof TBOMMaster) {
					TBOMMaster master = (TBOMMaster) lcm;
					if (master.getLatest()) {
						map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("type", "T-BOM");
						map.put("name", master.getName());
						map.put("state", master.getLifeCycleState().getDisplay());
						map.put("creator", master.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
						map.put("version", String.valueOf(master.getVersion()));
						map.put("primary", AUIGridUtils.primaryTemplate(master));
						map.put("secondary", AUIGridUtils.secondaryTemplate(master));
					} else {
						continue;
					}
					// workorder
				} else if (lcm instanceof WorkOrder) {
					WorkOrder workOrder = (WorkOrder) lcm;
					if (workOrder.getLatest()) {
						map.put("oid", workOrder.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("type", "도면일람표");
						map.put("name", workOrder.getName());
						map.put("state", workOrder.getLifeCycleState().getDisplay());
						map.put("creator", workOrder.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(workOrder.getCreateTimestamp()));
						map.put("version", String.valueOf(workOrder.getVersion()));
						map.put("primary", AUIGridUtils.primaryTemplate(workOrder));
						map.put("secondary", AUIGridUtils.secondaryTemplate(workOrder));
					} else {
						continue;
					}
					// document
				} else if (lcm instanceof WTDocument) {
					WTDocument document = (WTDocument) lcm;
					if (CommonUtils.isLatestVersion(document)) {
						map.put("oid", document.getPersistInfo().getObjectIdentifier().getStringValue());
						map.put("type", "산출물");
						map.put("name", document.getName());
						map.put("state", document.getLifeCycleState().getDisplay());
						map.put("creator", document.getCreatorFullName());
						map.put("createdDate_txt", CommonUtils.getPersistableTime(document.getCreateTimestamp()));
						map.put("version", document.getVersionIdentifier().getSeries().getValue() + "."
								+ document.getIterationIdentifier().getSeries().getValue());
						map.put("primary", AUIGridUtils.primaryTemplate(document));
						map.put("secondary", AUIGridUtils.secondaryTemplate(document));
					} else {
						continue;
					}
					// partlist
				} else if (lcm instanceof PartListMaster) {
					PartListMaster master = (PartListMaster) lcm;
					map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
					map.put("type", "수배표");
					map.put("name", master.getName());
					map.put("state", master.getLifeCycleState().getDisplay());
					map.put("creator", master.getCreatorFullName());
					map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
					map.put("version", master.getVersion());
					map.put("primary", AUIGridUtils.primaryTemplate(master));
					map.put("secondary", AUIGridUtils.secondaryTemplate(master));
				}
			}
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	public Map<String, Object> old(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();
		ArrayList<OutputDTO> list = new ArrayList<>();

		String name = (String) params.get("name");
		String number = (String) params.get("number");
		String content = (String) params.get("content");
		String kekNumber = (String) params.get("kekNumber");
		String keNumber = (String) params.get("keNumber");
		String description = (String) params.get("description");
		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");
		String state = (String) params.get("state");
		boolean latest = (boolean) params.get("latest");
		String oid = (String) params.get("oid"); // 폴더 OID
//		String type = (String) params.get("type");
		String mak = (String) params.get("mak");

		QuerySpec query = new QuerySpec();

		int idx = query.appendClassList(PRJDocument.class, true);
		int idx_m = query.appendClassList(E3PSDocumentMaster.class, false);

		QuerySpecUtils.toCI(query, idx, PRJDocument.class);
		QuerySpecUtils.toInnerJoin(query, PRJDocument.class, E3PSDocumentMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);

//		sc = new SearchCondition(PRJDocument.class, "masterReference.key.id", E3PSDocumentMaster.class,
//				"thePersistInfo.theObjectIdentifier.id");
//		query.appendWhere(sc, new int[] { idx, master });

		QuerySpecUtils.toLikeAnd(query, idx, PRJDocument.class, PRJDocument.NAME, name);
		QuerySpecUtils.toLikeAnd(query, idx, PRJDocument.class, PRJDocument.NUMBER, number);
		QuerySpecUtils.toLikeAnd(query, idx, PRJDocument.class, PRJDocument.DESCRIPTION, content);

		if (!StringUtils.isNull(kekNumber)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
			QuerySpecUtils.toLikeAnd(query, idx_p, Project.class, Project.KEK_NUMBER, kekNumber);
		}

		if (!StringUtils.isNull(keNumber)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
			QuerySpecUtils.toLikeAnd(query, idx_p, Project.class, Project.KE_NUMBER, keNumber);
		}

		if (!StringUtils.isNull(description)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
			QuerySpecUtils.toLikeAnd(query, idx_p, Project.class, Project.DESCRIPTION, description);
		}

		if (!StringUtils.isNull(mak)) {
			int idx_olink = query.appendClassList(OutputDocumentLink.class, false);
			int idx_plink = query.appendClassList(OutputProjectLink.class, false);
			int idx_o = query.appendClassList(Output.class, false);
			int idx_p = query.appendClassList(Project.class, true);

			query.appendOpenParen();

			ClassAttribute roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			ClassAttribute roleBca = new ClassAttribute(WTDocument.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition sc = new SearchCondition(
					new ClassAttribute(OutputDocumentLink.class, "roleAObjectRef.key.id"), "=", roleAca);
			query.appendWhere(sc, new int[] { idx_olink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputDocumentLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_olink, idx });
			query.appendAnd();

			roleAca = new ClassAttribute(Output.class, WTAttributeNameIfc.ID_NAME);
			roleBca = new ClassAttribute(Project.class, WTAttributeNameIfc.ID_NAME);

			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleAObjectRef.key.id"), "=",
					roleAca);
			query.appendWhere(sc, new int[] { idx_plink, idx_o });
			query.appendAnd();
			sc = new SearchCondition(new ClassAttribute(OutputProjectLink.class, "roleBObjectRef.key.id"), "=",
					roleBca);
			query.appendWhere(sc, new int[] { idx_plink, idx_p });

			query.appendCloseParen();
			CommonCode makCode = (CommonCode) CommonUtils.getObject(mak);
			QuerySpecUtils.toEqualsAnd(query, idx_p, Project.class, "makReference.key.id", makCode);
		}

		Folder folder = null;
		if (!StringUtils.isNull(oid)) {
			folder = (Folder) CommonUtils.getObject(oid);
		} else {
			folder = FolderTaskLogic.getFolder(OUTPUT_OLD_ROOT, CommonUtils.getPDMLinkProductContainer());
		}

		if (folder != null) {
			if (query.getConditionCount() > 0)
				query.appendAnd();
			int f_idx = query.appendClassList(IteratedFolderMemberLink.class, false);
			ClassAttribute fca = new ClassAttribute(IteratedFolderMemberLink.class, "roleBObjectRef.key.branchId");
			SearchCondition fsc = new SearchCondition(fca, "=",
					new ClassAttribute(PRJDocument.class, "iterationInfo.branchId"));
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
			QuerySpecUtils.toLatest(query, idx, PRJDocument.class);
		}

		QuerySpecUtils.creatorQuery(query, idx, PRJDocument.class, creatorOid);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, PRJDocument.class, PRJDocument.CREATE_TIMESTAMP, createdFrom,
				createdTo);
		QuerySpecUtils.toState(query, idx, PRJDocument.class, state);

		QuerySpecUtils.toOrderBy(query, idx, PRJDocument.class, PRJDocument.MODIFY_TIMESTAMP, true);

		query.setAdvancedQueryEnabled(true);
		query.setDescendantQuery(false);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			PRJDocument document = (PRJDocument) obj[0];
			OutputDTO dto = new OutputDTO(document);
			list.add(dto);
		}

		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}
}
