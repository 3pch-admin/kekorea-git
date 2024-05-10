package e3ps.korea.configSheet.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import e3ps.admin.configSheetCode.ConfigSheetCode;
import e3ps.admin.configSheetCode.service.ConfigSheetCodeHelper;
import e3ps.common.content.service.CommonContentHelper;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
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
import e3ps.project.output.Output;
import e3ps.project.output.OutputDocumentLink;
import e3ps.project.service.ProjectHelper;
import e3ps.project.task.Task;
import e3ps.project.task.variable.TaskStateVariable;
import e3ps.project.variable.ProjectStateVariable;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.service.WorkspaceHelper;
import wt.clients.folder.FolderTaskLogic;
import wt.content.ApplicationData;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.services.StandardManager;
import wt.util.WTException;

public class StandardConfigSheetService extends StandardManager implements ConfigSheetService {

	private static final String taskName = "사양체크리스트";

	public static StandardConfigSheetService newStandardConfigSheetService() throws WTException {
		StandardConfigSheetService instance = new StandardConfigSheetService();
		instance.initialize();
		return instance;
	}

	@Override
	public void create(ConfigSheetDTO dto) throws Exception {
		String name = dto.getName();
		String description = dto.getDescription();
		ArrayList<String> secondarys = dto.getSecondarys();
		ArrayList<Map<String, String>> addRows = dto.getAddRows();
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		int progress = dto.getProgress();
		Transaction trs = new Transaction();
		try {
			trs.start();

			String number = ConfigSheetHelper.manager.getNextNumber();

			ConfigSheet configSheet = ConfigSheet.newConfigSheet();
			configSheet.setName(name);
			configSheet.setNumber(number);
			configSheet.setDescription(description != null ? description : name);
			configSheet.setLatest(true);
			configSheet.setVersion(0);

			Folder folder = FolderTaskLogic.getFolder("/Default/프로젝트/" + taskName,
					CommonUtils.getPDMLinkProductContainer());
			FolderHelper.assignLocation((FolderEntry) configSheet, folder);

			PersistenceHelper.manager.save(configSheet);

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(configSheet);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(configSheet, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				// 기계_수배표 전기_수배표
				Task t = ProjectHelper.manager.getTaskByName(project, taskName);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 " + taskName + " 태스크가 존재하지 않습니다.");
				}

				ConfigSheetProjectLink link = ConfigSheetProjectLink.newConfigSheetProjectLink(configSheet, project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(configSheet.getName());
				output.setLocation(configSheet.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(configSheet);
				output.setOwnership(CommonUtils.sessionOwner());
				output = (Output) PersistenceHelper.manager.save(output);

				// 태스크
				if (t.getStartDate() == null) {
					// 중복적으로 실제 시작일이 변경 되지 않게
					t.setStartDate(DateUtils.getCurrentTimestamp());
				}

				if (progress >= 100) {
					t.setEndDate(DateUtils.getCurrentTimestamp());
					t.setState(TaskStateVariable.COMPLETE);
					t.setProgress(100);
				} else {
					t.setState(TaskStateVariable.INWORK);
					t.setProgress(progress);
				}
				t = (Task) PersistenceHelper.manager.modify(t);

				// 시작이 된 흔적이 없을 경우
				if (project.getStartDate() == null) {
					project.setStartDate(DateUtils.getCurrentTimestamp());
					project.setKekState(ProjectStateVariable.KEK_DESIGN_INWORK);
					project.setState(ProjectStateVariable.INWORK);
					project = (Project) PersistenceHelper.manager.modify(project);
				}
//				ProjectHelper.service.calculation(project);
				ProjectHelper.service.commit(project);
			}

			ArrayList<String> dataFields = new ArrayList<>();
			int sort = 0;
			// sheet
			for (Map<String, String> addRow : addRows) {
				String category_code = addRow.get("category_code");
				String category_name = addRow.get("category_name");
				String item_code = addRow.get("item_code");
				String item_name = addRow.get("item_name");
				String note = addRow.get("note");
				String apply = addRow.get("apply");

				ConfigSheetVariable variable = ConfigSheetVariable.newConfigSheetVariable();

				System.out.println("category_name=" + category_name);

				variable.setCategory_name(category_name);
				variable.setCategory_code(category_code);
				variable.setItem_name(item_name);
				variable.setItem_code(item_code);
				variable.setNote(note);
				variable.setApply(apply);
				variable.setSort(sort);
				PersistenceHelper.manager.save(variable);

				int ss = 0;
				for (String key : addRow.keySet()) {
					if (key.contains("spec")) {
						if (!dataFields.contains(key)) {
							System.out.println("##addRow dataFields.add==" + key);
							dataFields.add(key);
						}
					}
				}
				Collections.sort(dataFields);
				int lastIndex = dataFields.size() - 1;
				for (int i = 0; i < dataFields.size(); i++) {
					String key = dataFields.get(i);
					ConfigSheetColumnData column = ConfigSheetColumnData.newConfigSheetColumnData();
					column.setDataField(key);
					column.setValue(addRow.get(key));

					PersistenceHelper.manager.save(column);

					ColumnVariableLink ll = ColumnVariableLink.newColumnVariableLink(column, variable);
					ll.setSort(ss);
					if (i == lastIndex) {
						ll.setLast(true);
					} else {
						ll.setLast(false);
					}
					PersistenceHelper.manager.save(ll);
					ss++;
				}

				ConfigSheetVariableLink link = ConfigSheetVariableLink.newConfigSheetVariableLink(configSheet,
						variable);
				link.setSort(sort);
				PersistenceHelper.manager.save(link);
				sort++;
			}

			configSheet.setDataFields(dataFields);
			PersistenceHelper.manager.modify(configSheet);

			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(configSheet, agreeRows, approvalRows, receiveRows);
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			CommonContentHelper.manager.clean();
			if (trs != null)
				trs.rollback();
		}

	}

	@Override
	public void save(HashMap<String, List<ConfigSheetDTO>> dataMap) throws Exception {
		List<ConfigSheetDTO> removeRows = dataMap.get("removeRows");
		Transaction trs = new Transaction();
		try {
			trs.start();

			for (ConfigSheetDTO dto : removeRows) {
				String oid = dto.getLoid();
				ConfigSheetProjectLink link = (ConfigSheetProjectLink) CommonUtils.getObject(oid);
				PersistenceHelper.manager.delete(link);
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
	}

	@Override
	public void delete(String oid) throws Exception {
		Transaction trs = new Transaction();
		try {
			trs.start();

			ConfigSheet configSheet = (ConfigSheet) CommonUtils.getObject(oid);

			QueryResult result = PersistenceHelper.manager.navigate(configSheet, "project",
					ConfigSheetProjectLink.class, false);
			while (result.hasMoreElements()) {
				ConfigSheetProjectLink link = (ConfigSheetProjectLink) result.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			result.reset();
			result = PersistenceHelper.manager.navigate(configSheet, "output", OutputDocumentLink.class);
			while (result.hasMoreElements()) {
				Output output = (Output) result.nextElement();
				PersistenceHelper.manager.delete(output);
			}

			result.reset();
			result = PersistenceHelper.manager.navigate(configSheet, "variable", ConfigSheetVariableLink.class, false);
			while (result.hasMoreElements()) {
				ConfigSheetVariableLink link = (ConfigSheetVariableLink) result.nextElement();
				ConfigSheetVariable variable = link.getVariable();

				QueryResult qr = PersistenceHelper.manager.navigate(variable, "column", ColumnVariableLink.class,
						false);
				while (qr.hasMoreElements()) {
					ColumnVariableLink ll = (ColumnVariableLink) qr.nextElement();
					ConfigSheetColumnData dd = ll.getColumn();
					PersistenceHelper.manager.delete(dd);
					PersistenceHelper.manager.delete(ll);
				}

				PersistenceHelper.manager.delete(variable);
				PersistenceHelper.manager.delete(link);
			}

			WorkspaceHelper.manager.deleteAllLines(configSheet);

			configSheet = (ConfigSheet) PersistenceHelper.manager.refresh(configSheet);

			PersistenceHelper.manager.delete(configSheet);

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
	}

	@Override
	public Map<String, Object> connect(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();
		String poid = (String) params.get("poid");
		String toid = (String) params.get("toid");
		ArrayList<String> arr = (ArrayList<String>) params.get("arr");
		Transaction trs = new Transaction();
		try {
			trs.start();

			Task task = (Task) CommonUtils.getObject(toid);
			Project project = (Project) CommonUtils.getObject(poid);
			for (String oid : arr) {
				ConfigSheet configSheet = (ConfigSheet) CommonUtils.getObject(oid);

				QueryResult result = PersistenceHelper.manager.navigate(configSheet, "project",
						ConfigSheetProjectLink.class);
				while (result.hasMoreElements()) {
					Project p = (Project) result.nextElement();

					if (p.getPersistInfo().getObjectIdentifier().getStringValue().equals(poid)) {
						trs.rollback();
						map.put("msg", "해당 CONFIG SHEET가 작번 : " + p.getKekNumber() + "의 태스크 : " + task.getName()
								+ "에 연결이 되어있습니다.");
						map.put("exist", true);
						return map;
					}
				}

				ConfigSheetProjectLink link = ConfigSheetProjectLink.newConfigSheetProjectLink(configSheet, project);
				PersistenceHelper.manager.save(link);

				Output output = Output.newOutput();
				output.setName(configSheet.getName());
				output.setLocation(configSheet.getLocation());
				output.setTask(task);
				output.setProject(project);
				output.setDocument(configSheet);
				output.setOwnership(CommonUtils.sessionOwner());
				PersistenceHelper.manager.save(output);

				// 의뢰서는 아에 다른 페이지에서 작동하므로 소스 간결 연결된 태스트 상태 변경
				// 추가적인 산출물 등록시 실제 시작일이 변경 안되도록 처리한다.
				if (task.getStartDate() == null) {
					task.setStartDate(new Timestamp(new Date().getTime()));
				}
				task.setState(TaskStateVariable.INWORK);
				PersistenceHelper.manager.modify(task);

				// 프로젝트 전체 진행율 조정
//				ProjectHelper.service.calculation(project);
				ProjectHelper.service.commit(project);
			}

			map.put("exist", false);

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
		return map;
	}

	@Override
	public void disconnect(Map<String, Object> params) throws Exception {
		ArrayList<String> arr = (ArrayList<String>) params.get("arr");
		String poid = (String) params.get("poid");
		Transaction trs = new Transaction();
		try {
			trs.start();
			Project project = (Project) CommonUtils.getObject(poid);
			for (String oid : arr) {

				Persistable per = (Persistable) CommonUtils.getObject(oid);
				if (per instanceof WTDocument) {
					WTDocument doc = (WTDocument) per;
					QueryResult result = PersistenceHelper.manager.navigate(doc, "output", OutputDocumentLink.class);
					while (result.hasMoreElements()) {
						Output output = (Output) result.nextElement();
						PersistenceHelper.manager.delete(output);
					}

				} else if (per instanceof ConfigSheet) {

					ConfigSheet configSheet = (ConfigSheet) CommonUtils.getObject(oid);

					QuerySpec query = new QuerySpec();
					int idx = query.appendClassList(ConfigSheetProjectLink.class, true);
					QuerySpecUtils.toEqualsAnd(query, idx, ConfigSheetProjectLink.class, "roleAObjectRef.key.id",
							configSheet);
					QuerySpecUtils.toEqualsAnd(query, idx, ConfigSheetProjectLink.class, "roleBObjectRef.key.id",
							project);
					QueryResult qr = PersistenceHelper.manager.find(query);
					while (qr.hasMoreElements()) {
						Object[] obj = (Object[]) qr.nextElement();
						ConfigSheetProjectLink link = (ConfigSheetProjectLink) obj[0];
						PersistenceHelper.manager.delete(link);
					}

					QueryResult result = PersistenceHelper.manager.navigate(configSheet, "output",
							OutputDocumentLink.class);
					while (result.hasMoreElements()) {
						Output output = (Output) result.nextElement();
						PersistenceHelper.manager.delete(output);
					}
				}
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
	}

	@Override
	public void modify(ConfigSheetDTO dto) throws Exception {
		String name = dto.getName();
		String description = dto.getDescription();
		ArrayList<String> secondarys = dto.getSecondarys();
		ArrayList<Map<String, String>> addRows = dto.getAddRows();
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		int progress = dto.getProgress();
		Transaction trs = new Transaction();
		boolean isApproval = approvalRows.size() > 0;
		ApprovalMaster mm = null;

		try {
			trs.start();

			ConfigSheet conSheet = (ConfigSheet) CommonUtils.getObject(dto.getOid());

//			String preNumber = pre.getNumber();
//			int preVersion = pre.getVersion();
//			Timestamp cTime = pre.getCreateTimestamp();

			QueryResult qr = PersistenceHelper.manager.navigate(conSheet, "output", OutputDocumentLink.class);
			while (qr.hasMoreElements()) {
				Output output = (Output) qr.nextElement();
				PersistenceHelper.manager.delete(output);
			}

			if (isApproval) {
				WorkspaceHelper.manager.deleteAllLines(conSheet);
			} else {
				mm = WorkspaceHelper.manager.getMaster(conSheet);
			}

			// PersistenceHelper.manager.delete(pre);

			// ConfigSheet config = ConfigSheet.newConfigSheet();
			conSheet.setName(name);
			conSheet.setDescription(description != null ? description : name);
			conSheet.setLatest(true);
			// conSheet = (ConfigSheet) PersistenceHelper.manager.modify(conSheet);

			// conSheet = (ConfigSheet) PersistenceHelper.manager.refresh(conSheet);

			/*
			 * Folder folder = FolderTaskLogic.getFolder("/Default/프로젝트/" + taskName,
			 * CommonUtils.getPDMLinkProductContainer());
			 * FolderHelper.service.changeFolder((FolderEntry) conSheet, folder);
			 */

			CommonContentHelper.manager.clear(conSheet);

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(conSheet);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(conSheet, applicationData, vault.getPath());
			}

			QueryResult qrP = PersistenceHelper.manager.navigate(conSheet, "project", ConfigSheetProjectLink.class,
					false);
			while (qrP.hasMoreElements()) {
				ConfigSheetProjectLink cpLink = (ConfigSheetProjectLink) qrP.nextElement();
				PersistenceHelper.manager.delete(cpLink);
			}

			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				// 기계_수배표 전기_수배표
				Task tt = ProjectHelper.manager.getTaskByName(project, taskName);
				if (tt == null) {
					throw new Exception(project.getKekNumber() + "작번에 " + taskName + " 태스크가 존재하지 않습니다.");
				}

				ConfigSheetProjectLink link = ConfigSheetProjectLink.newConfigSheetProjectLink(conSheet, project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(conSheet.getName());
				output.setLocation(conSheet.getLocation());
				output.setTask(tt);
				output.setProject(project);
				output.setDocument(conSheet);
				output.setOwnership(CommonUtils.sessionOwner());
				output = (Output) PersistenceHelper.manager.save(output);

				// 태스크
				if (tt.getStartDate() == null) {
					// 중복적으로 실제 시작일이 변경 되지 않게
					tt.setStartDate(DateUtils.getCurrentTimestamp());
				}

				if (progress >= 100) {
					tt.setEndDate(DateUtils.getCurrentTimestamp());
					tt.setState(TaskStateVariable.COMPLETE);
					tt.setProgress(100);
				} else {
					tt.setState(TaskStateVariable.INWORK);
					tt.setProgress(progress);
				}
				tt = (Task) PersistenceHelper.manager.modify(tt);

				// 시작이 된 흔적이 없을 경우
				if (project.getStartDate() == null) {
					project.setStartDate(DateUtils.getCurrentTimestamp());
					project.setKekState(ProjectStateVariable.KEK_DESIGN_INWORK);
					project.setState(ProjectStateVariable.INWORK);
					project = (Project) PersistenceHelper.manager.modify(project);
				}
//				ProjectHelper.service.calculation(project);
				ProjectHelper.service.commit(project);
			}

			// ConfigSheetVariableLink link =
			// ConfigSheetVariableLink.newConfigSheetVariableLink(conSheet, variable);

			QueryResult qrVal = PersistenceHelper.manager.navigate(conSheet, "variable", ConfigSheetVariableLink.class);

			while (qrVal.hasMoreElements()) {
				ConfigSheetVariable cv = (ConfigSheetVariable) qrVal.nextElement();
				PersistenceHelper.manager.delete(cv);
			}

			ArrayList<String> dataFields = new ArrayList<>();
			int sort = 0;
			// sheet
			for (Map<String, String> addRow : addRows) {
				String category_code = addRow.get("category_code");
				String category_name = addRow.get("category_name");
				String item_code = addRow.get("item_code");
				String item_name = addRow.get("item_name");
				String note = addRow.get("note");
				String apply = addRow.get("apply");

				ConfigSheetVariable variable = ConfigSheetVariable.newConfigSheetVariable();

				variable.setCategory_name(category_name);
				variable.setCategory_code(category_code);
				variable.setItem_name(item_name);
				variable.setItem_code(item_code);
				variable.setNote(note);
				variable.setApply(apply);
				variable.setSort(sort);
				PersistenceHelper.manager.save(variable);

				int ss = 0;
				for (String key : addRow.keySet()) {
					if (key.contains("spec")) {
						if (!dataFields.contains(key)) {
							System.out.println("##addRow dataFields.add==" + key);
							dataFields.add(key);
						}
					}
				}
				Collections.sort(dataFields);
				int lastIndex = dataFields.size() - 1;
				for (int i = 0; i < dataFields.size(); i++) {
					String key = dataFields.get(i);
					ConfigSheetColumnData column = ConfigSheetColumnData.newConfigSheetColumnData();
					column.setDataField(key);
					column.setValue(addRow.get(key));

					PersistenceHelper.manager.save(column);

					ColumnVariableLink ll = ColumnVariableLink.newColumnVariableLink(column, variable);
					ll.setSort(ss);
					if (i == lastIndex) {
						ll.setLast(true);
					} else {
						ll.setLast(false);
					}
					PersistenceHelper.manager.save(ll);
					ss++;
				}

				ConfigSheetVariableLink link = ConfigSheetVariableLink.newConfigSheetVariableLink(conSheet, variable);
				link.setSort(sort);
				PersistenceHelper.manager.save(link);
				sort++;
			}

			conSheet.setDataFields(dataFields);
			PersistenceHelper.manager.modify(conSheet);
			conSheet = (ConfigSheet) PersistenceHelper.manager.refresh(conSheet);

			// WorkspaceHelper.manager.deleteAllLines(conSheet);

			conSheet = (ConfigSheet) PersistenceHelper.manager.refresh(conSheet);
			if (!isApproval) {
				if (mm != null) {
					String n = WorkspaceHelper.manager.getName(conSheet);
					mm.setPersist(conSheet);
					mm.setName(n);

					ArrayList<ApprovalLine> all = WorkspaceHelper.manager.getAllLines(mm);
					for (ApprovalLine line : all) {
						line.setName(n);
						PersistenceHelper.manager.modify(line);
					}
					PersistenceHelper.manager.modify(mm);
				}
			}

			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(conSheet, agreeRows, approvalRows, receiveRows);
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			CommonContentHelper.manager.clean();
			if (trs != null)
				trs.rollback();
		}
	}

	@Override
	public void revise(ConfigSheetDTO dto) throws Exception {
		String name = dto.getName();
		String description = dto.getDescription();
		ArrayList<String> secondarys = dto.getSecondarys();
		ArrayList<Map<String, String>> addRows = dto.getAddRows();
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		int progress = dto.getProgress();
		Transaction trs = new Transaction();
		try {
			trs.start();

			ConfigSheet pre = (ConfigSheet) CommonUtils.getObject(dto.getOid());

			String preName = pre.getName();
			if (!preName.equals(dto.getName())) {
				pre.setName(dto.getName());
			}
			pre.setLatest(false);
			PersistenceHelper.manager.modify(pre);

			ConfigSheet configSheet = ConfigSheet.newConfigSheet();
			configSheet.setName(name);
			configSheet.setNumber(pre.getNumber());
			configSheet.setDescription(description != null ? description : name);
			configSheet.setLatest(true);
			configSheet.setVersion(pre.getVersion() + 1);

			Folder folder = FolderTaskLogic.getFolder("/Default/프로젝트/" + taskName,
					CommonUtils.getPDMLinkProductContainer());
			FolderHelper.assignLocation((FolderEntry) configSheet, folder);

			PersistenceHelper.manager.save(configSheet);

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(configSheet);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(configSheet, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				// 기계_수배표 전기_수배표
				Task t = ProjectHelper.manager.getTaskByName(project, taskName);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 " + taskName + " 태스크가 존재하지 않습니다.");
				}

				ConfigSheetProjectLink link = ConfigSheetProjectLink.newConfigSheetProjectLink(configSheet, project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(configSheet.getName());
				output.setLocation(configSheet.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(configSheet);
				output.setOwnership(CommonUtils.sessionOwner());
				output = (Output) PersistenceHelper.manager.save(output);

				// 태스크
				if (t.getStartDate() == null) {
					// 중복적으로 실제 시작일이 변경 되지 않게
					t.setStartDate(DateUtils.getCurrentTimestamp());
				}

				if (progress >= 100) {
					t.setEndDate(DateUtils.getCurrentTimestamp());
					t.setState(TaskStateVariable.COMPLETE);
					t.setProgress(100);
				} else {
					t.setState(TaskStateVariable.INWORK);
					t.setProgress(progress);
				}
				t = (Task) PersistenceHelper.manager.modify(t);

				// 시작이 된 흔적이 없을 경우
				if (project.getStartDate() == null) {
					project.setStartDate(DateUtils.getCurrentTimestamp());
					project.setKekState(ProjectStateVariable.KEK_DESIGN_INWORK);
					project.setState(ProjectStateVariable.INWORK);
					project = (Project) PersistenceHelper.manager.modify(project);
				}
//				ProjectHelper.service.calculation(project);
				ProjectHelper.service.commit(project);
			}

			ArrayList<String> dataFields = new ArrayList<>();
			int sort = 0;
			// sheet
			for (Map<String, String> addRow : addRows) {
				String category_code = addRow.get("category_code");
				String category_name = addRow.get("category_name");
				String item_code = addRow.get("item_code");
				String item_name = addRow.get("item_name");
				String note = addRow.get("note");
				String apply = addRow.get("apply");

				ConfigSheetVariable variable = ConfigSheetVariable.newConfigSheetVariable();

				variable.setCategory_name(category_name);
				variable.setCategory_code(category_code);
				variable.setItem_name(item_name);
				variable.setItem_code(item_code);
				variable.setNote(note);
				variable.setApply(apply);
				variable.setSort(sort);
				PersistenceHelper.manager.save(variable);

				int ss = 0;
				for (String key : addRow.keySet()) {
					if (key.contains("spec")) {
						if (!dataFields.contains(key)) {
							System.out.println("##addRow dataFields.add==" + key);
							dataFields.add(key);
						}
					}
				}
				Collections.sort(dataFields);
				int lastIndex = dataFields.size() - 1;
				for (int i = 0; i < dataFields.size(); i++) {
					String key = dataFields.get(i);
					ConfigSheetColumnData column = ConfigSheetColumnData.newConfigSheetColumnData();
					column.setDataField(key);
					column.setValue(addRow.get(key));

					PersistenceHelper.manager.save(column);

					ColumnVariableLink ll = ColumnVariableLink.newColumnVariableLink(column, variable);
					ll.setSort(ss);
					if (i == lastIndex) {
						ll.setLast(true);
					} else {
						ll.setLast(false);
					}
					PersistenceHelper.manager.save(ll);
					ss++;
				}

				ConfigSheetVariableLink link = ConfigSheetVariableLink.newConfigSheetVariableLink(configSheet,
						variable);
				link.setSort(sort);
				PersistenceHelper.manager.save(link);
				sort++;
			}

			configSheet.setDataFields(dataFields);
			PersistenceHelper.manager.modify(configSheet);
			configSheet = (ConfigSheet) PersistenceHelper.manager.refresh(configSheet);

			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(configSheet, agreeRows, approvalRows, receiveRows);
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			CommonContentHelper.manager.clean();
			if (trs != null)
				trs.rollback();
		}
	}
}
