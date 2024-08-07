package e3ps.doc.request.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import e3ps.admin.commonCode.CommonCode;
import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.common.content.service.CommonContentHelper;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.doc.request.RequestDocument;
import e3ps.doc.request.RequestDocumentProjectLink;
import e3ps.doc.request.dto.RequestDocumentDTO;
import e3ps.project.Project;
import e3ps.project.ProjectUserLink;
import e3ps.project.output.Output;
import e3ps.project.output.OutputDocumentLink;
import e3ps.project.service.ProjectHelper;
import e3ps.project.task.Task;
import e3ps.project.task.variable.TaskStateVariable;
import e3ps.project.task.variable.TaskTypeVariable;
import e3ps.project.template.Template;
import e3ps.project.template.service.TemplateHelper;
import e3ps.project.variable.ProjectStateVariable;
import e3ps.project.variable.ProjectUserTypeVariable;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.service.WorkspaceHelper;
import wt.clients.folder.FolderTaskLogic;
import wt.clients.vc.CheckInOutTaskLogic;
import wt.content.ApplicationData;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.DocumentType;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.org.OrganizationServicesHelper;
import wt.org.WTUser;
import wt.ownership.Ownership;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.services.StandardManager;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;

public class StandardRequestDocumentService extends StandardManager implements RequestDocumentService {

	public static StandardRequestDocumentService newStandardRequestDocumentService() throws WTException {
		StandardRequestDocumentService instance = new StandardRequestDocumentService();
		instance.initialize();
		return instance;
	}

	@Override
	public void save(HashMap<String, List<RequestDocumentDTO>> dataMap) throws Exception {
		List<RequestDocumentDTO> removeRows = dataMap.get("removeRows");
		Transaction trs = new Transaction();
		try {
			trs.start();

			for (RequestDocumentDTO dto : removeRows) {
				String oid = dto.getOid();
				RequestDocument requestDocument = (RequestDocument) CommonUtils.getObject(oid);

				QueryResult result = PersistenceHelper.manager.navigate(requestDocument, "project",
						RequestDocumentProjectLink.class, false);
				while (result.hasMoreElements()) {
					RequestDocumentProjectLink link = (RequestDocumentProjectLink) result.nextElement();
					PersistenceHelper.manager.delete(link);
				}

				PersistenceHelper.manager.delete(requestDocument);
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
	public Map<String, Object> create(RequestDocumentDTO dto) throws Exception {
		Map<String, Object> map = new HashMap<>();
		String name = dto.getName();
		String template = dto.getTemplate();
		String description = dto.getDescription();
		ArrayList<Map<String, String>> addRows = dto.getAddRows();
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		ArrayList<String> primarys = dto.getPrimarys();
		String toid = dto.getToid();
		String poid = dto.getPoid();
		boolean connect = dto.isConnect();
		// 태스크에서 바로 연결 시킬떄 생각..
		Transaction trs = new Transaction();
		try {
			trs.start();

			RequestDocument requestDocument = RequestDocument.newRequestDocument();
			String number = RequestDocumentHelper.manager.getNextNumber();
			requestDocument.setNumber(number);
			requestDocument.setName(name);
			requestDocument.setDescription(description);
			requestDocument.setOwnership(CommonUtils.sessionOwner());
			requestDocument.setDocType(DocumentType.toDocumentType("$$Request"));

			Folder folder = FolderTaskLogic.getFolder(RequestDocumentHelper.DEFAULT_ROOT,
					CommonUtils.getPDMLinkProductContainer());
			FolderHelper.assignLocation((FolderEntry) requestDocument, folder);

			PersistenceHelper.manager.save(requestDocument);

			for (int i = 0; i < primarys.size(); i++) {
				String cacheId = (String) primarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(requestDocument);
				if (i == 0) {
					applicationData.setRole(ContentRoleType.PRIMARY);
				} else {
					applicationData.setRole(ContentRoleType.SECONDARY);
				}
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(requestDocument, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);
//				QueryResult result = PersistenceHelper.manager.navigate(requestDocument, "project",
//						RequestDocumentProjectLink.class);
//				while (result.hasMoreElements()) {
//					Project p = (Project) result.nextElement();
//
//					if (p.getPersistInfo().getObjectIdentifier().getStringValue().equals(poid)) {
//						trs.rollback();
//						map.put("msg",
//								"해당 의뢰서가 작번 : " + p.getKekNumber() + "의 의뢰서 태스크에 연결이 되어있습니다.");
//						map.put("exist", true);
//						return map;
//					}
//				}
				Task task = ProjectHelper.manager.getTaskByName(project, "의뢰서");
				RequestDocumentProjectLink link = RequestDocumentProjectLink
						.newRequestDocumentProjectLink(requestDocument, project);
				PersistenceHelper.manager.save(link);

				Output output = Output.newOutput();
				output.setName(requestDocument.getName());
				output.setLocation(requestDocument.getLocation());
				output.setTask(task);
				output.setProject(project);
				output.setDocument(requestDocument);
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
				//------------------------------------------
				// 주석하고 다시 03:05
//				ProjectHelper.service.commit(project);
			}

			if (!connect) {
				// 연결 안함
				auiGridDataSave(requestDocument, template, addRows);
			} else {
				// 연결
				Project project = (Project) CommonUtils.getObject(poid);
				Task task = (Task) CommonUtils.getObject(toid);
				task.setState(TaskStateVariable.INWORK);
				task.setStartDate(new Timestamp(new Date().getTime()));
				task = (Task) PersistenceHelper.manager.modify(task);

				Output output = Output.newOutput();
				output.setName(requestDocument.getName());
				output.setLocation(requestDocument.getLocation());
				output.setTask(task);
				output.setProject(project);
				output.setDocument(requestDocument);
				output.setOwnership(CommonUtils.sessionOwner());
				output = (Output) PersistenceHelper.manager.save(output);

				RequestDocumentProjectLink link = RequestDocumentProjectLink
						.newRequestDocumentProjectLink(requestDocument, project);
				PersistenceHelper.manager.save(link);
			}

			// 결재시작
			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(requestDocument, agreeRows, approvalRows, receiveRows);
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
		return map;
	}

	private void auiGridDataSave(RequestDocument requestDocument, String template,
			ArrayList<Map<String, String>> addRows) throws Exception {
		Ownership ownership = CommonUtils.sessionOwner();
		Timestamp start = new Timestamp(new Date().getTime());
		Calendar ca = Calendar.getInstance();
		ca.setTime(start);
		ca.add(Calendar.DATE, 1);
		Timestamp end = new Timestamp(ca.getTime().getTime());
		try {

			for (Map<String, String> addRow : addRows) {
				String projectType_code = addRow.get("projectType_code");
				String customer_code = addRow.get("customer_code");
				String install_code = addRow.get("install_code");
				String mak_code = addRow.get("mak_code");
				String detail_code = addRow.get("detail_code");
				String kekNumber = addRow.get("kekNumber");
				String keNumber = addRow.get("keNumber");
				String userId = addRow.get("userId");
				String customDate = addRow.get("customDate");
				String description = addRow.get("description");
				String model = addRow.get("model");
				String pdate = addRow.get("pdate");
				String machine = addRow.get("machine");
				String elec = addRow.get("elec");
				String soft = addRow.get("soft");

				CommonCode projectTypeCode = null;
				CommonCode customerCode = null;
				CommonCode installCode = null;
				CommonCode makCode = null;
				CommonCode detailCode = null;

				Project project = Project.newProject();

				projectTypeCode = (CommonCode) CommonUtils.getObject(projectType_code);
				project.setProjectType(projectTypeCode);

				customerCode = CommonCodeHelper.manager.getCommonCode(customer_code, "CUSTOMER");
				project.setCustomer(customerCode);

				installCode = CommonCodeHelper.manager.getCommonCode(install_code, "INSTALL");
				project.setInstall(installCode);

				makCode = CommonCodeHelper.manager.getCommonCode(mak_code, "MAK");
				project.setMak(makCode);

				detailCode = CommonCodeHelper.manager.getCommonCode(detail_code, "MAK_DETAIL");
				project.setDetail(detailCode);

				project.setKekNumber(kekNumber);
				project.setKeNumber(keNumber);
				project.setUserId(userId);
				project.setCustomDate(DateUtils.convertDate(customDate));
				project.setDescription(description);
				project.setModel(model);
				project.setPDate(DateUtils.convertDate(pdate));
				project.setState(ProjectStateVariable.READY);
				project.setKekState(ProjectStateVariable.KEK_READY);
				project.setOwnership(ownership);
				project.setPlanStartDate(start);
				project.setPlanEndDate(end);

				project = (Project) PersistenceHelper.manager.save(project);

				WTUser machineUser = null;
				WTUser elecUser = null;
				WTUser softUser = null;

				if (!StringUtils.isNull(machine)) {
					machineUser = (WTUser) CommonUtils.getObject(machine);
					ProjectUserLink userLink = ProjectUserLink.newProjectUserLink(project, machineUser);
					userLink.setUserType(
							CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.MACHINE, "USER_TYPE"));
					PersistenceHelper.manager.save(userLink);
				}

				if (!StringUtils.isNull(elec)) {
					elecUser = (WTUser) CommonUtils.getObject(elec);
					ProjectUserLink userLink = ProjectUserLink.newProjectUserLink(project, elecUser);
					userLink.setUserType(
							CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.ELEC, "USER_TYPE"));
					PersistenceHelper.manager.save(userLink);
				}

				if (!StringUtils.isNull(soft)) {
					softUser = (WTUser) CommonUtils.getObject(soft);
					ProjectUserLink userLink = ProjectUserLink.newProjectUserLink(project, softUser);
					userLink.setUserType(
							CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.SOFT, "USER_TYPE"));
					PersistenceHelper.manager.save(userLink);
				}

				// 템플릿 선택여부
				if (!StringUtils.isNull(template)) {
					Template copy = (Template) CommonUtils.getObject(template);
					project.setTemplate(copy);
					project = (Project) PersistenceHelper.manager.modify(project);
					copyTasks(project, copy, requestDocument);

					WTUser pm = TemplateHelper.manager.getUserType(copy, "PM");
					if (pm != null) {
						ProjectUserLink link = ProjectUserLink.newProjectUserLink(project, pm);
						link.setUserType(
								CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.PM, "USER_TYPE"));
						PersistenceHelper.manager.save(link);
					} else {
						pm = OrganizationServicesHelper.manager.getAuthenticatedUser(ProjectHelper.PM_ID);
						ProjectUserLink link = ProjectUserLink.newProjectUserLink(project, pm);
						link.setUserType(
								CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.PM, "USER_TYPE"));
						PersistenceHelper.manager.save(link);
					}

					WTUser subPm = TemplateHelper.manager.getUserType(copy, "SUB_PM");
					if (subPm != null) {
						ProjectUserLink link = ProjectUserLink.newProjectUserLink(project, subPm);
						link.setUserType(
								CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.SUB_PM, "USER_TYPE"));
						PersistenceHelper.manager.save(link);
					} else {
						subPm = OrganizationServicesHelper.manager.getAuthenticatedUser(ProjectHelper.SUB_PM_ID);
						ProjectUserLink link = ProjectUserLink.newProjectUserLink(project, subPm);
						link.setUserType(
								CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.SUB_PM, "USER_TYPE"));
						PersistenceHelper.manager.save(link);
					}

				} else {

					// 템플릿 없을 경우..
					Task task = Task.newTask();

					task.setName("의뢰서");
					task.setDepth(1);
					task.setDescription("의뢰서 태스크");
					task.setSort(0);
					task.setProject(project);
					task.setTemplate(null);
					task.setPlanStartDate(start);
					task.setPlanEndDate(end);
					task.setStartDate(start);
					task.setEndDate(null);
					task.setDuration(1);
					task.setTaskType(CommonCodeHelper.manager.getCommonCode(TaskTypeVariable.NORMAL, "TASK_TYPE"));
					task.setOwnership(ownership);
					task.setProgress(0);
					task.setState(TaskStateVariable.INWORK);
					task.setAllocate(0);
					task = (Task) PersistenceHelper.manager.save(task);

					Output output = Output.newOutput();
					output.setName(requestDocument.getName());
					output.setLocation(requestDocument.getLocation());
					output.setTask(task);
					output.setProject(project);
					output.setDocument(requestDocument);
					output.setOwnership(ownership);
					output = (Output) PersistenceHelper.manager.save(output);

					WTUser pm = OrganizationServicesHelper.manager.getAuthenticatedUser(ProjectHelper.PM_ID);
					ProjectUserLink pmLink = ProjectUserLink.newProjectUserLink(project, pm);
					pmLink.setUserType(CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.PM, "USER_TYPE"));
					PersistenceHelper.manager.save(pmLink);

					WTUser subPm = OrganizationServicesHelper.manager.getAuthenticatedUser(ProjectHelper.SUB_PM_ID);
					ProjectUserLink subPmLink = ProjectUserLink.newProjectUserLink(project, subPm);
					subPmLink.setUserType(
							CommonCodeHelper.manager.getCommonCode(ProjectUserTypeVariable.SUB_PM, "USER_TYPE"));
					PersistenceHelper.manager.save(subPmLink);
				}

				RequestDocumentProjectLink link = RequestDocumentProjectLink
						.newRequestDocumentProjectLink(requestDocument, project);
				PersistenceHelper.manager.save(link);

				// 계산
				ProjectHelper.service.commit(project);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void copyTasks(Project project, Template copy, RequestDocument requestDocument) throws Exception {
		try {
			ArrayList<Task> list = TemplateHelper.manager.recurciveTask(copy);

			HashMap<Task, Task> parentMap = new HashMap<Task, Task>();

			for (int i = 0; i < list.size(); i++) {
				Task task = (Task) list.get(i);

				Task newTask = Task.newTask();
				newTask.setName(task.getName());
				newTask.setDescription(task.getDescription());
				newTask.setAllocate(task.getAllocate());
				newTask.setSort(task.getSort());
				newTask.setDepth(task.getDepth());
				newTask.setDuration(task.getDuration());

				if (task.getName().equals("의뢰서")) {
					newTask.setState(TaskStateVariable.INWORK);
					newTask.setStartDate(DateUtils.getCurrentTimestamp());
				} else {
					newTask.setState(TaskStateVariable.READY);
				}

				newTask.setTaskType(task.getTaskType());
				newTask.setOwnership(task.getOwnership());
				newTask.setUpdateUser(task.getUpdateUser());
				newTask.setProgress(0);
				newTask.setProject(project);
				newTask.setTemplate(null);

				// 프로젝트의 계획 시작일..
				Calendar ca = Calendar.getInstance();
				newTask.setPlanStartDate(project.getPlanStartDate());
				ca.add(Calendar.DATE, task.getDuration()); // 기간을추가..
				Timestamp end = new Timestamp(ca.getTime().getTime());
				newTask.setPlanEndDate(end);

				Task parent = (Task) parentMap.get(task.getParentTask());
				newTask.setParentTask(parent);
				newTask = (Task) PersistenceHelper.manager.save(newTask);

				parentMap.put(task, newTask);

				if (requestDocument != null && newTask.getName().equals("의뢰서")) {
					Output output = Output.newOutput();
					output.setName(requestDocument.getName());
					output.setLocation(requestDocument.getLocation());
					output.setTask(newTask);
					output.setProject(project);
					output.setDocument(requestDocument);
					output.setOwnership(CommonUtils.sessionOwner());
					output = (Output) PersistenceHelper.manager.save(output);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void delete(String oid) throws Exception {
		Transaction trs = new Transaction();
		try {
			trs.start();

			RequestDocument requestDocument = (RequestDocument) CommonUtils.getObject(oid);
			QueryResult qr = PersistenceHelper.manager.navigate(requestDocument, "project",
					RequestDocumentProjectLink.class, false);
			while (qr.hasMoreElements()) {
				RequestDocumentProjectLink link = (RequestDocumentProjectLink) qr.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			QueryResult result = PersistenceHelper.manager.navigate(requestDocument, "output", OutputDocumentLink.class,
					false);
			while (result.hasMoreElements()) {
				OutputDocumentLink link = (OutputDocumentLink) result.nextElement();
				Output output = link.getOutput();
				PersistenceHelper.manager.delete(output);
				PersistenceHelper.manager.delete(link);
			}

			PersistenceHelper.manager.delete(requestDocument);

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
				RequestDocument requestDocument = (RequestDocument) CommonUtils.getObject(oid);
				QuerySpec query = new QuerySpec();
				int idx = query.appendClassList(RequestDocumentProjectLink.class, true);
				QuerySpecUtils.toEqualsAnd(query, idx, RequestDocumentProjectLink.class, "roleAObjectRef.key.id",
						requestDocument);
				QuerySpecUtils.toEqualsAnd(query, idx, RequestDocumentProjectLink.class, "roleBObjectRef.key.id",
						project);
				QueryResult qr = PersistenceHelper.manager.find(query);
				while (qr.hasMoreElements()) {
					Object[] obj = (Object[]) qr.nextElement();
					RequestDocumentProjectLink link = (RequestDocumentProjectLink) obj[0];
					PersistenceHelper.manager.delete(link);
				}

				QueryResult result = PersistenceHelper.manager.navigate(requestDocument, "output",
						OutputDocumentLink.class);
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
		String poid = (String) params.get("poid");
		String toid = (String) params.get("toid");
		ArrayList<String> arr = (ArrayList<String>) params.get("arr");
		Transaction trs = new Transaction();
		try {
			trs.start();

			Task task = (Task) CommonUtils.getObject(toid);
			Project project = (Project) CommonUtils.getObject(poid);
			for (String oid : arr) {
				RequestDocument requestDocument = (RequestDocument) CommonUtils.getObject(oid);

				QueryResult result = PersistenceHelper.manager.navigate(requestDocument, "project",
						RequestDocumentProjectLink.class);
				while (result.hasMoreElements()) {
					Project p = (Project) result.nextElement();

					if (p.getPersistInfo().getObjectIdentifier().getStringValue().equals(poid)) {
						trs.rollback();
						map.put("msg",
								"해당 의뢰서가 작번 : " + p.getKekNumber() + "의 태스크 : " + task.getName() + "에 연결이 되어있습니다.");
						map.put("exist", true);
						return map;
					}
				}

				RequestDocumentProjectLink link = RequestDocumentProjectLink
						.newRequestDocumentProjectLink(requestDocument, project);
				PersistenceHelper.manager.save(link);

				Output output = Output.newOutput();
				output.setName(requestDocument.getName());
				output.setLocation(requestDocument.getLocation());
				output.setTask(task);
				output.setProject(project);
				output.setDocument(requestDocument);
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
	public void modify(RequestDocumentDTO dto) throws Exception {
		String oid = dto.getOid();
		String name = dto.getName();
		ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<String> secondarys = dto.getPrimarys();

//		ArrayList<Map<String, String>> addRows = dto.getAddRows();
//			ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();

		Transaction trs = new Transaction();
		boolean isApproval = approvalRows.size() > 0;
		ApprovalMaster mm = null;

		System.out.println("### req doc modify ==" + oid);

		try {
			trs.start();

			RequestDocument reqDoc = (RequestDocument) CommonUtils.getObject(oid);

			// 기존 연결 제거한다..
			QueryResult result = PersistenceHelper.manager.navigate(reqDoc, "project", RequestDocumentProjectLink.class,
					false);
			while (result.hasMoreElements()) {
				RequestDocumentProjectLink link = (RequestDocumentProjectLink) result.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			if (isApproval) {
				WorkspaceHelper.manager.deleteAllLines(reqDoc);
			} else {
				mm = WorkspaceHelper.manager.getMaster(reqDoc);
			}

			// 기존 산출물 연결도 제거 한다..
			QueryResult qr = PersistenceHelper.manager.navigate(reqDoc, "output", OutputDocumentLink.class, false);
			while (qr.hasMoreElements()) {
				OutputDocumentLink link = (OutputDocumentLink) qr.nextElement();
				Output output = link.getOutput();
				PersistenceHelper.manager.delete(output);
				PersistenceHelper.manager.delete(link);
			}

			Folder cFolder = CheckInOutTaskLogic.getCheckoutFolder();
			CheckoutLink clink = WorkInProgressHelper.service.checkout(reqDoc, cFolder, "회의록 수정 체크 아웃");
			RequestDocument newReqDoc = (RequestDocument) clink.getWorkingCopy();
			WTDocumentMaster master = (WTDocumentMaster) newReqDoc.getMaster();
			WTDocumentMasterIdentity identity = (WTDocumentMasterIdentity) master.getIdentificationObject();
			identity.setName(name);
			master = (WTDocumentMaster) IdentityHelper.service.changeIdentity(master, identity);

			CommonContentHelper.manager.clear(newReqDoc);

			for (int i = 0; secondarys != null && i < secondarys.size(); i++) {
				String cacheId = (String) secondarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(newReqDoc);
				applicationData.setRole(ContentRoleType.SECONDARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(newReqDoc, applicationData, vault.getPath());
			}

			WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
			String msg = user.getFullName() + " 사용자가 문서를 수정 하였습니다.";
			// 필요하면 수정 사유로 대체
			newReqDoc = (RequestDocument) WorkInProgressHelper.service.checkin(newReqDoc, msg);

			for (Map<String, String> addRow9 : addRows9) {
				String poid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(poid);

				Task t = ProjectHelper.manager.getTaskByName(project, "의뢰서");
				if (t == null) {
					throw new Exception(project.getKekNumber() + "작번에 의뢰서 태스크가 존재하지 않습니다.");
				}

				RequestDocumentProjectLink link = RequestDocumentProjectLink.newRequestDocumentProjectLink(newReqDoc,
						project);
				PersistenceHelper.manager.save(link);

				// 산출물
				Output output = Output.newOutput();
				output.setName(newReqDoc.getName());
				output.setLocation(newReqDoc.getLocation());
				output.setTask(t);
				output.setProject(project);
				output.setDocument(newReqDoc);
				output.setOwnership(CommonUtils.sessionOwner());
				output = (Output) PersistenceHelper.manager.save(output);

				// 태스크
				if (t.getStartDate() == null) {
					// 중복적으로 실제 시작일이 변경 되지 않게
					t.setStartDate(DateUtils.getCurrentTimestamp());
					t.setState(TaskStateVariable.INWORK);
				}

				t = (Task) PersistenceHelper.manager.modify(t);

				// 시작이 된 흔적이 없을 경우
				if (project.getStartDate() == null) {
					project.setStartDate(DateUtils.getCurrentTimestamp());
					project.setKekState(ProjectStateVariable.KEK_DESIGN_INWORK);
					project.setState(ProjectStateVariable.INWORK);
					project = (Project) PersistenceHelper.manager.modify(project);
				}
				ProjectHelper.service.calculation(project);
	            ProjectHelper.service.commit(project);
			}

			if (!isApproval) {
				if (mm != null) {
					String n = WorkspaceHelper.manager.getName(newReqDoc);
					mm.setPersist(newReqDoc);
					mm.setName(n);

					ArrayList<ApprovalLine> all = WorkspaceHelper.manager.getAllLines(mm);
					for (ApprovalLine line : all) {
						line.setName(n);
						PersistenceHelper.manager.modify(line);
					}
					PersistenceHelper.manager.modify(mm);
				}
			}

			// 결재시작
			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(newReqDoc, agreeRows, approvalRows, receiveRows);
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
