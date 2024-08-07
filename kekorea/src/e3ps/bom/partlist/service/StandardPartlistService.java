
package e3ps.bom.partlist.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import e3ps.bom.partlist.MasterDataLink;
import e3ps.bom.partlist.PartListData;
import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.PartListMasterProjectLink;
import e3ps.bom.partlist.dto.PartListDTO;
import e3ps.common.content.service.CommonContentHelper;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.org.Department;
import e3ps.org.People;
import e3ps.project.Project;
import e3ps.project.output.Output;
import e3ps.project.output.OutputDocumentLink;
import e3ps.project.service.ProjectHelper;
import e3ps.project.task.Task;
import e3ps.project.task.variable.TaskStateVariable;
import e3ps.project.variable.ProjectStateVariable;
import e3ps.workspace.service.WorkspaceHelper;
import wt.clients.folder.FolderTaskLogic;
import wt.content.ApplicationData;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.services.StandardManager;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;

public class StandardPartlistService extends StandardManager implements PartlistService {

	/**
	 * 캐시 처리
	 */
	public static HashMap<String, WTPart> partCache = null;
	static {
		if (partCache == null) {
			partCache = new HashMap<>();
		}
	}

	public static StandardPartlistService newStandardPartlistService() throws WTException {
		StandardPartlistService instance = new StandardPartlistService();
		instance.initialize();
		return instance;
	}

	@Override
	public void delete(String oid) throws Exception {
		Transaction trs = new Transaction();
		try {
			trs.start();

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
				PersistenceHelper.manager.delete(link);
			}

			QuerySpec _query = new QuerySpec();
			int _idx = _query.appendClassList(PartListMaster.class, true);
			int idx_link = _query.appendClassList(PartListMasterProjectLink.class, true);
			QuerySpecUtils.toInnerJoin(_query, PartListMaster.class, PartListMasterProjectLink.class,
					WTAttributeNameIfc.ID_NAME, "roleAObjectRef.key.id", _idx, idx_link);
			QuerySpecUtils.toEqualsAnd(_query, idx_link, PartListMasterProjectLink.class, "roleAObjectRef.key.id",
					master.getPersistInfo().getObjectIdentifier().getId());
			QueryResult qr = PersistenceHelper.manager.find(_query);
			while (qr.hasMoreElements()) {
				Object[] obj = (Object[]) qr.nextElement();
				PartListMasterProjectLink link = (PartListMasterProjectLink) obj[1];
				PersistenceHelper.manager.delete(link);
			}

			// 결재 이력 삭제해ㅑ할거..
			WorkspaceHelper.manager.deleteAllLines(master);

			master = (PartListMaster) PersistenceHelper.manager.refresh(master);

			PersistenceHelper.manager.delete(master);

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
	public void create(PartListDTO dto) throws Exception {
		String name = dto.getName();
		String engType = dto.getEngType();
		String description = dto.getDescription();
		ArrayList<String> secondarys = dto.getSecondarys();
		ArrayList<Map<String, Object>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, Object>> addRows = dto.getAddRows();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		String toid = dto.getToid();
		int progress = dto.getProgress();

		String location = "/Default/프로젝트/" + engType + "_수배표";

		Transaction trs = new Transaction();
		try {
			trs.start();

			String number = PartlistHelper.manager.getNextNumber();
			PartListMaster master = PartListMaster.newPartListMaster();
			master.setNumber(number);
			master.setName(name);
			master.setEngType(engType); // 먼저 저장을 하고 후에 변경을 해야한다.. 필수값 에러 ..
			master.setDescription(description);
			master.setOwnership(CommonUtils.sessionOwner());

			// 위치는 기계 수배표 전기 수배표로 몰빵..
			Folder folder = FolderTaskLogic.getFolder(location, CommonUtils.getPDMLinkProductContainer());
			FolderHelper.assignLocation((FolderEntry) master, folder);

			master = (PartListMaster) PersistenceHelper.manager.save(master);

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(master);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(master, applicationData, vault.getPath());
			}

			double totalPrice = 0D;
			int sort = 0;
			for (Map<String, Object> addRow : addRows) {
				// 수배표 데이터..
				PartListData data = PartListData.newPartListData();

				int lotNo = (int) addRow.get("lotNo");
				String unitName = (String) addRow.get("unitName");
				String partNo = (String) addRow.get("partNo");
				String partName = (String) addRow.get("partName");
				String standard = (String) addRow.get("standard");
				String maker = (String) addRow.get("maker");
				String customer = (String) addRow.get("customer");
				int quantity = (int) addRow.get("quantity");
				String unit = (String) addRow.get("unit");
				int price = (int) addRow.get("price");
				String currency = (String) addRow.get("currency");
				Object won = (Object) addRow.get("won");
				Object exchangeRate = (Object) addRow.get("exchangeRate");
				String referDrawing = (String) addRow.get("referDrawing");
				String classification = (String) addRow.get("classification");
				String note = (String) addRow.get("note");

				System.out.println("문제 발생 체크 = " + partNo + " 행 = " + sort);

				WTPart part = partCache.get(partNo);
				if (part == null) {
					QuerySpec query = new QuerySpec();
					int idx = query.appendClassList(WTPart.class, true);
					int idx_m = query.appendClassList(WTPartMaster.class, false);
					QuerySpecUtils.toCI(query, idx, WTPart.class);
					QuerySpecUtils.toInnerJoin(query, WTPart.class, WTPartMaster.class, "masterReference.key.id",
							WTAttributeNameIfc.ID_NAME, idx, idx_m);

					QuerySpecUtils.toLatest(query, idx, WTPart.class);
					QuerySpecUtils.toIBAEqualsAnd(query, WTPart.class, idx, "PART_CODE", partNo);
					QueryResult result = PersistenceHelper.manager.find(query);
					if (result.hasMoreElements()) {
						Object[] obj = (Object[]) result.nextElement();
						part = (WTPart) obj[0];
					}
				}

				data.setLotNo(lotNo);
				data.setUnitName(unitName);
				data.setPartNo(partNo);
				data.setPartName(partName);
				data.setStandard(standard);
				data.setMaker(maker);
				data.setCustomer(customer);
				data.setQuantity(quantity);
				data.setUnit(unit);
				data.setPrice(price);
				data.setCurrency(currency);
//				data.setWon(won);

				if (won instanceof Double) {
					double value = (double) won;
					data.setWon(value);
				} else {
					Integer value = (Integer) won;
					Double dd = Double.valueOf(value);
					data.setWon(dd);
				}

				// part connect
				data.setWtPart(part);

				if (exchangeRate instanceof Double) {
					double value = (double) exchangeRate;
					data.setExchangeRate(value);
				} else {
					Integer value = (Integer) exchangeRate;
					Double dd = Double.valueOf(value);
					data.setExchangeRate(dd);
				}

				data.setReferDrawing(referDrawing);
				data.setClassification(classification);
				data.setNote(note);
				data.setPartListDate(new Timestamp(new Date().getTime()));
				data.setSort(sort);
				PersistenceHelper.manager.save(data);

				MasterDataLink link = MasterDataLink.newMasterDataLink(master, data);
				link.setSort(sort);
				PersistenceHelper.manager.save(link);
				sort++;

				totalPrice += data.getWon();

			}

			for (Map<String, Object> addRow9 : addRows9) {
				String oid = (String) addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				if ("기계".equals(engType) || "기계_1차_수배".equals(engType) || "기계_2차_수배".equals(engType)) {
					double outputMachinePrice = project.getOutputMachinePrice() != null
							? project.getOutputMachinePrice()
							: 0D;
					outputMachinePrice += totalPrice;
					project.setOutputMachinePrice(outputMachinePrice);
				} else if ("전기".equals(engType) || "전기_1차_수배".equals(engType) || "전기_2차_수배".equals(engType)) {
					double outputElecPrice = project.getOutputElecPrice() != null ? project.getOutputElecPrice() : 0D;
					outputElecPrice += totalPrice;
					project.setOutputElecPrice(outputElecPrice);
				}
				PersistenceHelper.manager.modify(project);

				String taskName = "";
				if (!StringUtils.isNull(toid)) {
					Task task = (Task) CommonUtils.getObject(toid);
					taskName = task.getName();
				} else {
					if ("기계".equals(engType)) {
						taskName = "기계_수배표";
					} else if ("전기".equals(engType)) {
						taskName = "전기_수배표";
					}
				}

				// 기계_수배표 전기_수배표
				Task parentTask = ProjectHelper.manager.getTaskByName(project, taskName);
				// 1차수배 2차수배
				Task t = ProjectHelper.manager.getTaskByParent(project, parentTask);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 태스크(1차_수배, 2차_수배)가 존재하지 않습니다.");
				}

				master.setEngType(engType + "_" + t.getName());
				PartListMasterProjectLink link = PartListMasterProjectLink.newPartListMasterProjectLink(master,
						project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(master.getName());
				output.setLocation(master.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(master);
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

				// 무조건 부모는 존재 한다
				Task pTask = t.getParentTask();
				if (pTask.getStartDate() == null) {
					pTask.setStartDate(DateUtils.getCurrentTimestamp());
					pTask.setState(TaskStateVariable.INWORK);
				}
				PersistenceHelper.manager.modify(pTask);

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

			master.setTotalPrice(totalPrice);
			PersistenceHelper.manager.modify(master);

			// 결재시작
			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(master, agreeRows, approvalRows, receiveRows);
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
	public void modify(PartListDTO dto) throws Exception {
		String oid = dto.getOid();
		String name = dto.getName();
//		String engType = dto.getEngType();
		String description = dto.getDescription();
		ArrayList<String> secondarys = dto.getSecondarys();
		ArrayList<Map<String, Object>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, Object>> addRows = dto.getAddRows();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		String toid = dto.getToid();
		int progress = dto.getProgress();
		Transaction trs = new Transaction();
		try {
			trs.start();

			People people = CommonUtils.sessionPeople();
			Department department = people.getDepartment();
			String engType = "";
			if (department.getCode().equals("MACHINE")) {
				engType = "기계";
			} else if (department.getCode().equals("ELEC")) {
				engType = "전기";
			}

			PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);

			// 기존 결재선 삭제
			WorkspaceHelper.manager.deleteAllLines(master);

			master = (PartListMaster) PersistenceHelper.manager.refresh(master);

			System.out.println("name=" + name);

			master.setName(name);
			master.setDescription(description);
			master = (PartListMaster) PersistenceHelper.manager.modify(master);

			// 산출물로 연결된거 삭제
			QueryResult _qr = PersistenceHelper.manager.navigate(master, "output", OutputDocumentLink.class, false);
			while (_qr.hasMoreElements()) {
				OutputDocumentLink link = (OutputDocumentLink) _qr.nextElement();
				Output output = link.getOutput();
				PersistenceHelper.manager.delete(output);
			}

			// 관련 작번 삭제
			QueryResult qr = PersistenceHelper.manager.navigate(master, "project", PartListMasterProjectLink.class,
					false);
			while (qr.hasMoreElements()) {
				PartListMasterProjectLink link = (PartListMasterProjectLink) qr.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			ArrayList<PartListData> dataList = PartlistHelper.manager.getPartListData(oid);
			for (PartListData data : dataList) {
				PersistenceHelper.manager.delete(data);
			}

			// 수배표 내용 수정
			double totalPrice = 0D;
			int sort = 0;
			for (Map<String, Object> addRow : addRows) {
				// 수배표 데이터..
				PartListData data = PartListData.newPartListData();

				int lotNo = (int) addRow.get("lotNo");
				String unitName = (String) addRow.get("unitName");
				String partNo = (String) addRow.get("partNo");
				String partName = (String) addRow.get("partName");
				String standard = (String) addRow.get("standard");
				String maker = (String) addRow.get("maker");
				String customer = (String) addRow.get("customer");
				int quantity = (int) addRow.get("quantity");
				String unit = (String) addRow.get("unit");
				int price = (int) addRow.get("price");
				String currency = (String) addRow.get("currency");
				Object won = (Object) addRow.get("won");
				Object exchangeRate = (Object) addRow.get("exchangeRate");
				String referDrawing = (String) addRow.get("referDrawing");
				String classification = (String) addRow.get("classification");
				String note = (String) addRow.get("note");

				System.out.println("문제 발생 체크 = " + partNo + " 행 = " + sort);

				data.setLotNo(lotNo);
				data.setUnitName(unitName);
				data.setPartNo(partNo);
				data.setPartName(partName);
				data.setStandard(standard);
				data.setMaker(maker);
				data.setCustomer(customer);
				data.setQuantity(quantity);
				data.setUnit(unit);
				data.setPrice(price);
				data.setCurrency(currency);
//				data.setWon(won);

				if (won instanceof Double) {
					double value = (double) won;
					data.setWon(value);
				} else {
					Integer value = (Integer) won;
					Double dd = Double.valueOf(value);
					data.setWon(dd);
				}

				if (exchangeRate instanceof Double) {
					double value = (double) exchangeRate;
					data.setExchangeRate(value);
				} else {
					Integer value = (Integer) exchangeRate;
					Double dd = Double.valueOf(value);
					data.setExchangeRate(dd);
				}

				data.setReferDrawing(referDrawing);
				data.setClassification(classification);
				data.setNote(note);
				data.setPartListDate(new Timestamp(new Date().getTime()));
				data.setSort(sort);
				PersistenceHelper.manager.save(data);

				MasterDataLink link = MasterDataLink.newMasterDataLink(master, data);
				link.setSort(sort);
				PersistenceHelper.manager.save(link);
				sort++;

				totalPrice += data.getWon();
			}

			for (Map<String, Object> addRow9 : addRows9) {
				Project project = (Project) CommonUtils.getObject((String) addRow9.get("oid"));

				if ("기계".equals(engType)) {
					double outputMachinePrice = project.getOutputMachinePrice() != null
							? project.getOutputMachinePrice()
							: 0D;
					outputMachinePrice += totalPrice;
					project.setOutputMachinePrice(outputMachinePrice);
				} else if ("전기".equals(engType)) {
					double outputElecPrice = project.getOutputElecPrice() != null ? project.getOutputElecPrice() : 0D;
					outputElecPrice += totalPrice;
					project.setOutputElecPrice(outputElecPrice);
				}
				PersistenceHelper.manager.modify(project);

				String taskName = "";
				if (!StringUtils.isNull(toid)) {
					Task task = (Task) CommonUtils.getObject(toid);
					taskName = task.getName();
				} else {
					if ("기계".equals(engType)) {
						taskName = "기계_수배표";
					} else if ("전기".equals(engType)) {
						taskName = "전기_수배표";
					}
				}

				// 기계_수배표 전기_수배표
				Task parentTask = ProjectHelper.manager.getTaskByName(project, taskName);
				// 1차수배 2차수배
				Task t = ProjectHelper.manager.getTaskByParent(project, parentTask);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 태스크(1차_수배, 2차_수배)가 존재하지 않습니다.");
				}

				master.setEngType(engType + "_" + t.getName());
				PartListMasterProjectLink link = PartListMasterProjectLink.newPartListMasterProjectLink(master,
						project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(master.getName());
				output.setLocation(master.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(master);
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

				// 무조건 부모는 존재 한다
				Task pTask = t.getParentTask();
				if (pTask.getStartDate() == null) {
					pTask.setStartDate(DateUtils.getCurrentTimestamp());
					pTask.setState(TaskStateVariable.INWORK);
				}
				PersistenceHelper.manager.modify(pTask);

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

			master.setTotalPrice(totalPrice);
			PersistenceHelper.manager.modify(master);

			CommonContentHelper.manager.clear(master);

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(master);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(master, applicationData, vault.getPath());
			}

			// 결재시작
			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(master, agreeRows, approvalRows, receiveRows);
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
	public void disconnect(Map<String, Object> params) throws Exception {
		ArrayList<String> arr = (ArrayList<String>) params.get("arr");
		String poid = (String) params.get("poid");
		Transaction trs = new Transaction();
		try {
			trs.start();

			Project project = (Project) CommonUtils.getObject(poid);
			for (String oid : arr) {
				PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);
				QuerySpec query = new QuerySpec();
				int idx = query.appendClassList(PartListMasterProjectLink.class, true);
				QuerySpecUtils.toEqualsAnd(query, idx, PartListMasterProjectLink.class, "roleAObjectRef.key.id",
						master);
				QuerySpecUtils.toEqualsAnd(query, idx, PartListMasterProjectLink.class, "roleBObjectRef.key.id",
						project);
				QueryResult qr = PersistenceHelper.manager.find(query);
				while (qr.hasMoreElements()) {
					Object[] obj = (Object[]) qr.nextElement();
					PartListMasterProjectLink link = (PartListMasterProjectLink) obj[0];
					PersistenceHelper.manager.delete(link);
				}

				QueryResult result = PersistenceHelper.manager.navigate(master, "output", OutputDocumentLink.class);
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

	@Override
	public Map<String, Object> connect(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();
		String toid = (String) params.get("toid");
		String poid = (String) params.get("poid");
		ArrayList<String> arr = (ArrayList<String>) params.get("arr");
		Transaction trs = new Transaction();
		try {
			trs.start();

			Task task = (Task) CommonUtils.getObject(toid);
			Project project = (Project) CommonUtils.getObject(poid);
			for (String oid : arr) {
				PartListMaster mm = (PartListMaster) CommonUtils.getObject(oid);
				QueryResult result = PersistenceHelper.manager.navigate(mm, "project", PartListMasterProjectLink.class);
				while (result.hasMoreElements()) {
					Project p = (Project) result.nextElement();

					if (p.getPersistInfo().getObjectIdentifier().getStringValue().equals(oid)) {
						map.put("msg",
								"해당 산출물이 작번 : " + p.getKekNumber() + "의 태스크 : " + task.getName() + "에 연결이 되어있습니다.");
						map.put("exist", true);
						return map;
					}
				}

				String engType = "";
				double totalPrice = 0D;
				if (task.getName().indexOf("기계") > -1) {
					double outputMachinePrice = project.getOutputMachinePrice() != null
							? project.getOutputMachinePrice()
							: 0D;
					outputMachinePrice += totalPrice;
					project.setOutputMachinePrice(outputMachinePrice);
					engType = "기계";
				} else if (task.getName().indexOf("전기") > -1) {
					double outputElecPrice = project.getOutputElecPrice() != null ? project.getOutputElecPrice() : 0D;
					outputElecPrice += totalPrice;
					project.setOutputElecPrice(outputElecPrice);
					engType = "전기";
				}
				PersistenceHelper.manager.modify(project);

				Task parentTask = ProjectHelper.manager.getTaskByName(project, task.getName());
				// 1차수배 2차수배
				Task t = ProjectHelper.manager.getTaskByParent(project, parentTask);
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 태스크(1차_수배, 2차_수배)가 존재하지 않습니다.");
				}

				mm.setEngType(engType + "_" + t.getName());
				PartListMasterProjectLink link = PartListMasterProjectLink.newPartListMasterProjectLink(mm, project);
				PersistenceHelper.manager.save(link);

				Output output = Output.newOutput();
				output.setName(mm.getName());
				output.setLocation(mm.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(mm);
				output.setOwnership(CommonUtils.sessionOwner());
				PersistenceHelper.manager.save(output);

				// 태스크
				if (t.getStartDate() == null) {
					// 중복적으로 실제 시작일이 변경 되지 않게
					t.setStartDate(DateUtils.getCurrentTimestamp());
				}

				t.setState(TaskStateVariable.INWORK);
				t = (Task) PersistenceHelper.manager.modify(t);

				// 무조건 부모는 존재 한다
				Task pTask = t.getParentTask();
				if (pTask.getStartDate() == null) {
					pTask.setStartDate(DateUtils.getCurrentTimestamp());
					pTask.setState(TaskStateVariable.INWORK);
				}
				PersistenceHelper.manager.modify(pTask);

				// 시작이 된 흔적이 없을 경우
				if (project.getStartDate() == null) {
					project.setStartDate(DateUtils.getCurrentTimestamp());
					project.setKekState(ProjectStateVariable.KEK_DESIGN_INWORK);
					project.setState(ProjectStateVariable.INWORK);
					project = (Project) PersistenceHelper.manager.modify(project);
				}

//				ProjectHelper.service.calculation(project);
				ProjectHelper.service.commit(project);

				mm.setTotalPrice(totalPrice);
				PersistenceHelper.manager.modify(mm);

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
}
