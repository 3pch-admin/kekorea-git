package e3ps.epm.workOrder.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import com.aspose.cells.FileFormatType;

import e3ps.common.content.service.CommonContentHelper;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.ContentUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.epm.numberRule.NumberRule;
import e3ps.epm.workOrder.WorkOrder;
import e3ps.epm.workOrder.WorkOrderDataLink;
import e3ps.epm.workOrder.WorkOrderProjectLink;
import e3ps.epm.workOrder.dto.WorkOrderDTO;
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

public class StandardWorkOrderService extends StandardManager implements WorkOrderService {

	public static StandardWorkOrderService newStandardWorkOrderService() throws WTException {
		StandardWorkOrderService instance = new StandardWorkOrderService();
		instance.initialize();
		return instance;
	}

	@Override
	public void create(WorkOrderDTO dto) throws Exception {
		String toid = dto.getToid();
		String name = dto.getName();
		String description = dto.getDescription();
		int progress = dto.getProgress();
		String workOrderType = dto.getWorkOrderType();
		ArrayList<Map<String, Object>> addRows = dto.getAddRows(); // 도면 일람표
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		ArrayList<Map<String, String>> addRows11 = dto.getAddRows11();
//		String number = addRows11.get(0).get("number");
		ArrayList<String> secondarys = dto.getSecondarys();
		String location = "/Default/프로젝트/" + workOrderType + "_도면일람표";
		Transaction trs = new Transaction();
		try {
			trs.start();

//			String noid = addRows11.get(0).get("oid");
			NumberRule numberRule = (NumberRule) CommonUtils.getObject("e3ps.epm.numberRule.NumberRule:141174358");

			String n = WorkOrderHelper.manager.getNextNumber("WORKORDER");

			WorkOrder workOrder = WorkOrder.newWorkOrder();
			workOrder.setDescription(description);
			workOrder.setName(name);
			workOrder.setWorkOrderType(workOrderType);
			workOrder.setNumber(n);
			workOrder.setNumberRule(numberRule);
			workOrder.setVersion(0);
			workOrder.setLatest(true);

			Folder folder = FolderTaskLogic.getFolder(location, CommonUtils.getPDMLinkProductContainer());
			FolderHelper.assignLocation((FolderEntry) workOrder, folder);

			PersistenceHelper.manager.save(workOrder);

			int sort = 0;
			ArrayList<WorkOrderDataLink> list = new ArrayList<>();
			for (int i = 0; i < addRows.size(); i++) {
				Map<String, Object> addRow = addRows.get(i);
				String oid = (String) addRow.get("doid");
				int rev = (int) addRow.get("rev");
				int lotNo = (int) addRow.get("lotNo");
				String note = (String) addRow.get("note");
				Persistable persistable = (Persistable) CommonUtils.getObject(oid);
				WorkOrderDataLink link = WorkOrderDataLink.newWorkOrderDataLink(workOrder, persistable);
				link.setSort(sort);
				link.setLotNo(lotNo);
				link.setNote(note);
				link.setRev(rev);
				PersistenceHelper.manager.save(link);
				sort++;
				list.add(link);
			}

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(workOrder);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(workOrder, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				String taskName = "";
				if (!StringUtils.isNull(toid)) {
					Task task = (Task) CommonUtils.getObject(toid);
					taskName = task.getName();
				} else {
					if ("기계".equals(workOrderType)) {
						taskName = "기계_도면일람표";
					} else if ("전기".equals(workOrderType)) {
						taskName = "전기_도면일람표";
					}
				}

				// 기계_수배표 전기_수배표
				Task t = ProjectHelper.manager.getTaskByName(project, taskName);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 " + taskName + " 태스크가 존재하지 않습니다.");
				}

				WorkOrderProjectLink link = WorkOrderProjectLink.newWorkOrderProjectLink(workOrder, project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(workOrder.getName());
				output.setLocation(workOrder.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(workOrder);
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

			Workbook cover = WorkOrderHelper.manager.createWorkOrderCover(workOrder, list);
			File excelFile = ContentUtils.getTempFile(workOrder.getName() + "_표지.xlsx");
			FileOutputStream fos = new FileOutputStream(excelFile);
			cover.write(fos);

			ApplicationData data = ApplicationData.newApplicationData(workOrder);
			data.setRole(ContentRoleType.PRIMARY);
			PersistenceHelper.manager.save(data);
			ContentServerHelper.service.updateContent(workOrder, data, excelFile.getAbsolutePath());

			// pdf 표지
			File pdfFile = ContentUtils.getTempFile(workOrder.getName() + "_표지.pdf");
			com.aspose.cells.Workbook wb = new com.aspose.cells.Workbook(new FileInputStream(excelFile));
			FileOutputStream fospdf = new FileOutputStream(pdfFile);
			wb.save(fospdf, FileFormatType.PDF);

			ApplicationData dd = ApplicationData.newApplicationData(workOrder);
			dd.setRole(ContentRoleType.ADDITIONAL_FILES);
			PersistenceHelper.manager.save(dd);
			ContentServerHelper.service.updateContent(workOrder, dd, pdfFile.getAbsolutePath());

			WorkOrderHelper.manager.postAfterAction(workOrder.getPersistInfo().getObjectIdentifier().getStringValue());

			// 결재시작
			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(workOrder, agreeRows, approvalRows, receiveRows);
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
	public void modify(WorkOrderDTO dto) throws Exception {
		String name = dto.getName();
		String description = dto.getDescription();
		int progress = dto.getProgress();
		String workOrderType = dto.getWorkOrderType();
		ArrayList<Map<String, Object>> addRows = dto.getAddRows(); // 도면 일람표
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		ArrayList<String> secondarys = dto.getSecondarys();
		Transaction trs = new Transaction();
		try {
			trs.start();

			WorkOrder workOrder = (WorkOrder) CommonUtils.getObject(dto.getOid());
			workOrder.setName(name);
			workOrder.setDescription(description);
			workOrder.setWorkOrderType(workOrderType);
			PersistenceHelper.manager.modify(workOrder);

			// 기존 도면 일람표 링크 모두제거
			QueryResult qr = PersistenceHelper.manager.navigate(workOrder, "data", WorkOrderDataLink.class, false);
			while (qr.hasMoreElements()) {
				WorkOrderDataLink link = (WorkOrderDataLink) qr.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			int sort = 0;
			ArrayList<WorkOrderDataLink> list = new ArrayList<>();
			for (int i = 0; i < addRows.size(); i++) {
				Map<String, Object> addRow = addRows.get(i);
				String oid = (String) addRow.get("doid"); // 객체 링크 OID...
				int rev = (int) addRow.get("rev");
				int lotNo = (int) addRow.get("lotNo");
				String note = (String) addRow.get("note");
				Persistable persistable = (Persistable) CommonUtils.getObject(oid);
				WorkOrderDataLink link = WorkOrderDataLink.newWorkOrderDataLink(workOrder, persistable);
				link.setSort(sort);
				link.setLotNo(lotNo);
				link.setNote(note);
				link.setRev(rev);
				PersistenceHelper.manager.save(link);
				sort++;
				list.add(link);
			}

			// 표지 파일도 다시 생성해야함..
			CommonContentHelper.manager.clear(workOrder);

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(workOrder);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(workOrder, applicationData, vault.getPath());
			}

			// 기존 작번과 도면일람표 링크 제거
			QueryResult _qr = PersistenceHelper.manager.navigate(workOrder, "project", WorkOrderProjectLink.class,
					false);
			while (_qr.hasMoreElements()) {
				WorkOrderProjectLink link = (WorkOrderProjectLink) _qr.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			// 기존 산출물 링크도 제거 후 다시 연결
			QueryResult navi = PersistenceHelper.manager.navigate(workOrder, "output", OutputDocumentLink.class);
			while (navi.hasMoreElements()) {
				Output output = (Output) navi.nextElement();
				PersistenceHelper.manager.delete(output);
			}

			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				String taskName = "";
				if ("기계".equals(workOrderType)) {
					taskName = "기계_도면일람표";
				} else if ("전기".equals(workOrderType)) {
					taskName = "전기_도면일람표";
				}

				// 기계_수배표 전기_수배표
				Task t = ProjectHelper.manager.getTaskByName(project, taskName);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 " + taskName + " 태스크가 존재하지 않습니다.");
				}

				WorkOrderProjectLink link = WorkOrderProjectLink.newWorkOrderProjectLink(workOrder, project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(workOrder.getName());
				output.setLocation(workOrder.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(workOrder);
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

			Workbook cover = WorkOrderHelper.manager.createWorkOrderCover(workOrder, list);
			File excelFile = ContentUtils.getTempFile(workOrder.getName() + "_표지.xlsx");
			FileOutputStream fos = new FileOutputStream(excelFile);
			cover.write(fos);

			ApplicationData data = ApplicationData.newApplicationData(workOrder);
			data.setRole(ContentRoleType.PRIMARY);
			PersistenceHelper.manager.save(data);
			ContentServerHelper.service.updateContent(workOrder, data, excelFile.getAbsolutePath());

			// pdf 표지
			File pdfFile = ContentUtils.getTempFile(workOrder.getName() + "_표지.pdf");
			com.aspose.cells.Workbook wb = new com.aspose.cells.Workbook(new FileInputStream(excelFile));
			FileOutputStream fospdf = new FileOutputStream(pdfFile);
			wb.save(fospdf, FileFormatType.PDF);

			ApplicationData dd = ApplicationData.newApplicationData(workOrder);
			dd.setRole(ContentRoleType.ADDITIONAL_FILES);
			PersistenceHelper.manager.save(dd);
			ContentServerHelper.service.updateContent(workOrder, dd, pdfFile.getAbsolutePath());

			// PDF 병합
			WorkOrderHelper.manager.postAfterAction(workOrder.getPersistInfo().getObjectIdentifier().getStringValue());

			// 결재시작
			if (approvalRows.size() > 0) {
				WorkspaceHelper.manager.deleteAllLines(workOrder); // 기존결재 잇으면 삭제 후 작업
				WorkspaceHelper.service.register(workOrder, agreeRows, approvalRows, receiveRows);
			}
			boolean isApproval = approvalRows.size() > 0;
			if (!isApproval) {
				ApprovalMaster mm = WorkspaceHelper.manager.getMaster(workOrder);
				if (mm != null) {
					String n = WorkspaceHelper.manager.getName(workOrder);
					mm.setPersist(workOrder);
					mm.setName(n);

					ArrayList<ApprovalLine> all = WorkspaceHelper.manager.getAllLines(mm);
					for (ApprovalLine line : all) {
						line.setName(n);
						PersistenceHelper.manager.modify(line);
					}
					PersistenceHelper.manager.modify(mm);
				}
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
	public void delete(String oid) throws Exception {
		Transaction trs = new Transaction();
		try {
			trs.start();

			WorkOrder workOrder = (WorkOrder) CommonUtils.getObject(oid);

			QueryResult result = PersistenceHelper.manager.navigate(workOrder, "project", WorkOrderProjectLink.class,
					false);
			while (result.hasMoreElements()) {
				WorkOrderProjectLink link = (WorkOrderProjectLink) result.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			result.reset();
			result = PersistenceHelper.manager.navigate(workOrder, "data", WorkOrderDataLink.class, false);
			while (result.hasMoreElements()) {
				WorkOrderDataLink link = (WorkOrderDataLink) result.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			WorkOrder prev = WorkOrderHelper.manager.pre(workOrder);

			PersistenceHelper.manager.delete(workOrder);

			if (prev != null) {
				prev.setLatest(true);
				PersistenceHelper.manager.modify(prev);
			}

			// 버전 되돌려야함..

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
	public void revise(WorkOrderDTO dto) throws Exception {
		String name = dto.getName();
		String note = dto.getNote();
		String description = dto.getDescription();
		int progress = dto.getProgress();
		String workOrderType = dto.getWorkOrderType();
		ArrayList<Map<String, Object>> addRows = dto.getAddRows(); // 도면 일람표
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		ArrayList<String> secondarys = dto.getSecondarys();
		String location = "/Default/프로젝트/" + workOrderType + "_도면일람표";
		Transaction trs = new Transaction();
		try {
			trs.start();

			WorkOrder pre = (WorkOrder) CommonUtils.getObject(dto.getOid());
			String preName = pre.getName();
			if (!preName.equals(dto.getName())) {
				pre.setName(dto.getName());
			}
			pre.setLatest(false);
			PersistenceHelper.manager.modify(pre);

			WorkOrder workOrder = WorkOrder.newWorkOrder();
			workOrder.setDescription(description);
			workOrder.setName(name);
			workOrder.setWorkOrderType(workOrderType);
			workOrder.setNumber(pre.getNumber());
			workOrder.setNumberRule(pre.getNumberRule());
			workOrder.setVersion(pre.getVersion() + 1);
			workOrder.setLatest(true);
			workOrder.setNote(note);

			Folder folder = FolderTaskLogic.getFolder(location, CommonUtils.getPDMLinkProductContainer());
			FolderHelper.assignLocation((FolderEntry) workOrder, folder);

			PersistenceHelper.manager.save(workOrder);

			int sort = 0;
			ArrayList<WorkOrderDataLink> list = new ArrayList<>();
			for (int i = 0; i < addRows.size(); i++) {
				Map<String, Object> addRow = addRows.get(i);
				String oid = (String) addRow.get("doid");
				int rev = (int) addRow.get("rev");
				int lotNo = (int) addRow.get("lotNo");
				String n = (String) addRow.get("note");
				Persistable persistable = (Persistable) CommonUtils.getObject(oid);
				WorkOrderDataLink link = WorkOrderDataLink.newWorkOrderDataLink(workOrder, persistable);
				link.setSort(sort);
				link.setLotNo(lotNo);
				link.setNote(n);
				link.setRev(rev);
				PersistenceHelper.manager.save(link);
				sort++;
				list.add(link);
			}

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(workOrder);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(workOrder, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				String taskName = "";
				if ("기계".equals(workOrderType)) {
					taskName = "기계_도면일람표";
				} else if ("전기".equals(workOrderType)) {
					taskName = "전기_도면일람표";
				}

				// 기계_수배표 전기_수배표
				Task t = ProjectHelper.manager.getTaskByName(project, taskName);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 " + taskName + " 태스크가 존재하지 않습니다.");
				}

				WorkOrderProjectLink link = WorkOrderProjectLink.newWorkOrderProjectLink(workOrder, project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(workOrder.getName());
				output.setLocation(workOrder.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(workOrder);
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

			Workbook cover = WorkOrderHelper.manager.createWorkOrderCover(workOrder, list);
			File excelFile = ContentUtils.getTempFile(workOrder.getName() + "_표지.xlsx");
			FileOutputStream fos = new FileOutputStream(excelFile);
			cover.write(fos);

			ApplicationData data = ApplicationData.newApplicationData(workOrder);
			data.setRole(ContentRoleType.PRIMARY);
			PersistenceHelper.manager.save(data);
			ContentServerHelper.service.updateContent(workOrder, data, excelFile.getAbsolutePath());

			// pdf 표지
			File pdfFile = ContentUtils.getTempFile(workOrder.getName() + "_표지.pdf");
			com.aspose.cells.Workbook wb = new com.aspose.cells.Workbook(new FileInputStream(excelFile));
			FileOutputStream fospdf = new FileOutputStream(pdfFile);
			wb.save(fospdf, FileFormatType.PDF);

			ApplicationData dd = ApplicationData.newApplicationData(workOrder);
			dd.setRole(ContentRoleType.ADDITIONAL_FILES);
			PersistenceHelper.manager.save(dd);
			ContentServerHelper.service.updateContent(workOrder, dd, pdfFile.getAbsolutePath());

			WorkOrderHelper.manager.postAfterAction(workOrder.getPersistInfo().getObjectIdentifier().getStringValue());

			// 결재시작
			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(workOrder, agreeRows, approvalRows, receiveRows);
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
				WorkOrder workOrder = (WorkOrder) CommonUtils.getObject(oid);

				QueryResult result = PersistenceHelper.manager.navigate(workOrder, "project",
						WorkOrderProjectLink.class);
				while (result.hasMoreElements()) {
					Project p = (Project) result.nextElement();

					if (p.getPersistInfo().getObjectIdentifier().getStringValue().equals(poid)) {
						trs.rollback();
						map.put("msg",
								"해당 도면일람표가 작번 : " + p.getKekNumber() + "의 태스크 : " + task.getName() + "에 연결이 되어있습니다.");
						map.put("exist", true);
						return map;
					}
				}

				WorkOrderProjectLink link = WorkOrderProjectLink.newWorkOrderProjectLink(workOrder, project);
				PersistenceHelper.manager.save(link);

				Output output = Output.newOutput();
				output.setName(workOrder.getName());
				output.setLocation(workOrder.getLocation());
				output.setTask(task);
				output.setProject(project);
				output.setDocument(workOrder);
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
				WorkOrder workOrder = (WorkOrder) CommonUtils.getObject(oid);
				QuerySpec query = new QuerySpec();
				int idx = query.appendClassList(WorkOrderProjectLink.class, true);
				QuerySpecUtils.toEqualsAnd(query, idx, WorkOrderProjectLink.class, "roleAObjectRef.key.id", workOrder);
				QuerySpecUtils.toEqualsAnd(query, idx, WorkOrderProjectLink.class, "roleBObjectRef.key.id", project);
				QueryResult qr = PersistenceHelper.manager.find(query);
				while (qr.hasMoreElements()) {
					Object[] obj = (Object[]) qr.nextElement();
					WorkOrderProjectLink link = (WorkOrderProjectLink) obj[0];
					PersistenceHelper.manager.delete(link);
				}

				QueryResult result = PersistenceHelper.manager.navigate(workOrder, "output", OutputDocumentLink.class);
				while (result.hasMoreElements()) {
					Output output = (Output) result.nextElement();
					PersistenceHelper.manager.delete(output);
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
}
