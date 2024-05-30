package e3ps.workspace.service;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.aspose.cells.BorderType;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.Color;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Worksheet;

import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.service.PartlistHelper;
import e3ps.bom.tbom.TBOMMaster;
import e3ps.bom.tbom.service.TBOMHelper;
import e3ps.common.util.AUIGridUtils;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.IBAUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.doc.PRJDocument;
import e3ps.doc.meeting.Meeting;
import e3ps.doc.request.RequestDocument;
import e3ps.doc.request.service.RequestDocumentHelper;
import e3ps.epm.numberRule.NumberRule;
import e3ps.epm.workOrder.WorkOrder;
import e3ps.epm.workOrder.service.WorkOrderHelper;
import e3ps.korea.configSheet.ConfigSheet;
import e3ps.korea.configSheet.service.ConfigSheetHelper;
import e3ps.org.People;
import e3ps.org.dto.UserDTO;
import e3ps.part.service.PartHelper;
import e3ps.project.Project;
import e3ps.project.output.Output;
import e3ps.project.output.service.OutputHelper;
import e3ps.project.task.Task;
import e3ps.workspace.ApprovalContract;
import e3ps.workspace.ApprovalContractPersistableLink;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.ApprovalUserLine;
import e3ps.workspace.PersistableLineMasterLink;
import e3ps.workspace.dto.ApprovalLineDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.content.ContentHolder;
import wt.doc.WTDocument;
import wt.enterprise.Managed;
import wt.epm.EPMDocument;
import wt.fc.PagingQueryResult;
import wt.fc.PagingSessionHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.session.SessionHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTProperties;
import wt.vc.Versioned;

public class WorkspaceHelper {

	/**
	 * 결재 타입 상수 모음
	 */
	public static final String AGREE_LINE = "검토";
	public static final String APPROVAL_LINE = "결재";
	public static final String RECEIVE_LINE = "수신";
	public static final String SUBMIT_LINE = "기안";

	/**
	 * 결재마스터 상태값 상수 모음
	 */
	public static final String STATE_MASTER_APPROVAL_APPROVING = "승인중";
	public static final String STATE_MASTER_APPROVAL_COMPELTE = "결재완료";
	public static final String STATE_MASTER_AGREE_REJECT = "검토반려";
	public static final String STATE_MASTER_APPROVAL_REJECT = "반려";

	/**
	 * 기안 라인 상태
	 */
	public static final String STATE_SUBMIT_COMPLETE = "제출완료";

	/**
	 * 결재 라인 상태값 상수
	 */
	public static final String STATE_APPROVAL_READY = "대기중";
	public static final String STATE_APPROVAL_APPROVING = "승인중";
	public static final String STATE_APPROVAL_COMPLETE = "결재완료";
	public static final String STATE_APPROVAL_REJECT = "반려됨";

	/**
	 * 검토 라인 상태값 상수
	 */
	public static final String STATE_AGREE_READY = "검토중";
	public static final String STATE_AGREE_COMPLETE = "검토완료";
	public static final String STATE_AGREE_REJECT = "검토반려";

	/**
	 * 수신 라인 상태값 상수
	 */
	public static final String STATE_RECEIVE_READY = "수신확인중";
	public static final String STATE_RECEIVE_COMPLETE = "수신완료";

	/**
	 * 부재중 처리 상태값 상수
	 */
	public static final String STATE_LINE_ABSENCE = "부재중";

	/**
	 * 결재자 타입 상수 값
	 */
	public static final String WORKING_SUBMIT = "기안자";
	public static final String WORKING_APPROVAL = "승인자";
	public static final String WORKING_AGREE = "검토자";
	public static final String WORKING_RECEIVE = "수신자";

	/**
	 * ColumnData 구분 상수 값
	 */
	private static final String COLUMN_APPROVAL = "COLUMN_APPROVAL";
	private static final String COLUMN_AGREE = "COLUMN_AGREE";
	private static final String COLUMN_RECEIVE = "COLUMN_RECEIVE";
	private static final String COLUMN_COMPLETE = "COLUMN_COMPLETE";
	private static final String COLUMN_REJECT = "COLUMN_REJECT";
	private static final String COLUMN_PROGRESS = "COLUMN_PROGRESS";

	public static final WorkspaceService service = ServiceFactory.getService(WorkspaceService.class);
	public static final WorkspaceHelper manager = new WorkspaceHelper();

	public int getAppObjType(Persistable per) {
		int appObjType = 0;

		return appObjType;
	}

	/**
	 * 객체에 따른 결재제목 가져오기
	 */
	public String getName(Persistable per) {
		if (per instanceof WTDocument) {
			WTDocument document = (WTDocument) per;
			return document.getName();
		} else if (per instanceof WTPart) {
			WTPart part = (WTPart) per;
			return part.getName();
		} else if (per instanceof EPMDocument) {
			EPMDocument epm = (EPMDocument) per;
			return epm.getName();
		} else if (per instanceof ApprovalContract) {
			ApprovalContract contract = (ApprovalContract) per;
			return contract.getName();
		} else if (per instanceof Managed) {
			if (per instanceof PartListMaster) {
				PartListMaster master = (PartListMaster) per;
				return master.getName();
			} else if (per instanceof TBOMMaster) {
				TBOMMaster master = (TBOMMaster) per;
				return master.getName();
			} else if (per instanceof WorkOrder) {
				WorkOrder workOrder = (WorkOrder) per;
				return workOrder.getName();
			} else if (per instanceof ConfigSheet) {
				ConfigSheet configSheet = (ConfigSheet) per;
				return configSheet.getName();
			}
		}
		return "";
	}

	/**
	 * 검토함
	 */
	public Map<String, Object> agree(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<ApprovalLineDTO> list = new ArrayList<>();

		String approvalTitle = (String) params.get("approvalTitle"); // 결재 제목
		String submiterOid = (String) params.get("submiterOid"); // 작성자
		String receiveFrom = (String) params.get("receiveFrom");
		String receiveTo = (String) params.get("receiveTo");
		String state = (String) params.get("state");

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);

		// 쿼리문 작성
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_master = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_master);

		if ("검토완료".equals(state)) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_AGREE_COMPLETE);

			if (!StringUtils.isNull(receiveFrom) || !StringUtils.isNull(receiveTo)) {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						receiveFrom, receiveTo);
			} else {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						before, end);
			}
		} else if ("검토중".equals(state)) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_AGREE_READY);
		} else if ("검토반려".equals(state)) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_AGREE_REJECT);
			if (!StringUtils.isNull(receiveFrom) || !StringUtils.isNull(receiveTo)) {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						receiveFrom, receiveTo);
			} else {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						before, end);
			}
		} else if ("전체".equals(state)) {

			if (query.getConditionCount() > 0) {
				query.appendAnd();
			}

			query.appendOpenParen();

			SearchCondition sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=", STATE_AGREE_COMPLETE);
			query.appendWhere(sc, new int[] { idx });
			query.appendOr();

			sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=", STATE_AGREE_READY);
			query.appendWhere(sc, new int[] { idx });
			query.appendOr();

			sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=", STATE_AGREE_REJECT);
			query.appendWhere(sc, new int[] { idx });

			query.appendCloseParen();
			if (!StringUtils.isNull(receiveFrom) || !StringUtils.isNull(receiveTo)) {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						receiveFrom, receiveTo);
			} else {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						before, end);
			}
		}

		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, AGREE_LINE);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP, receiveFrom,
				receiveTo);
		QuerySpecUtils.toCreator(query, idx_master, ApprovalMaster.class, submiterOid);
		QuerySpecUtils.toLikeAnd(query, idx, ApprovalLine.class, ApprovalLine.NAME, approvalTitle);
		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {

		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "ownership.owner.key.id", sessionUser);
		}
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);

		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine approvalLine = (ApprovalLine) obj[0];
			ApprovalLineDTO column = new ApprovalLineDTO(approvalLine, COLUMN_AGREE);
			list.add(column);
		}
		map.put("size", list.size());
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 결재함
	 */
	public Map<String, Object> approval(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<ApprovalLineDTO> list = new ArrayList<>();
		String submiterOid = (String) params.get("submiterOid");
		String receiveFrom = (String) params.get("receiveFrom");
		String receiveTo = (String) params.get("receiveTo");
		String approvalTitle = (String) params.get("approvalTitle");
		boolean progress = (boolean) params.get("progress");

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, false);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);

		// 쿼리 수정할 예정

		if (!progress) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_APPROVAL_APPROVING);
		} else {
			if (query.getConditionCount() > 0)
				query.appendAnd();

			query.appendOpenParen();

			SearchCondition sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=",
					STATE_APPROVAL_COMPLETE);
			query.appendWhere(sc, new int[] { idx });
			query.appendOr();

			sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=", STATE_APPROVAL_APPROVING);
			query.appendWhere(sc, new int[] { idx });
			query.appendOr();

			sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=", STATE_APPROVAL_REJECT);
			query.appendWhere(sc, new int[] { idx });

			query.appendCloseParen();
			if (!StringUtils.isNull(receiveFrom) || !StringUtils.isNull(receiveTo)) {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						receiveFrom, receiveTo);
			} else {
				QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP,
						before, end);
			}
		}

		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, APPROVAL_LINE);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP, receiveFrom,
				receiveTo);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toCreator(query, idx, ApprovalLine.class,
					sessionUser.getPersistInfo().getObjectIdentifier().getStringValue());
		}

		QuerySpecUtils.toCreator(query, idx_m, ApprovalMaster.class, submiterOid);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP, receiveFrom,
				receiveTo);
		QuerySpecUtils.toLikeAnd(query, idx, ApprovalLine.class, ApprovalLine.NAME, approvalTitle);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine approvalLine = (ApprovalLine) obj[0];
			ApprovalLineDTO column = new ApprovalLineDTO(approvalLine, COLUMN_APPROVAL);
			list.add(column);
		}
		map.put("size", list.size());
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 수신함
	 */
	public Map<String, Object> receive(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<ApprovalLineDTO> list = new ArrayList<>();
		String approvalTitle = (String) params.get("approvalTitle");
		String receiveFrom = (String) params.get("receiveFrom");
		String receiveTo = (String) params.get("receiveTo");
		String submiterOid = (String) params.get("submiterOid");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_master = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_RECEIVE_READY);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, RECEIVE_LINE);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP, receiveFrom,
				receiveTo);
		QuerySpecUtils.toCreator(query, idx_master, ApprovalMaster.class, submiterOid);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "ownership.owner.key.id", sessionUser);
		}

		QuerySpecUtils.toLikeAnd(query, idx, ApprovalLine.class, ApprovalLine.NAME, approvalTitle);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine line = (ApprovalLine) obj[0];
			ApprovalLineDTO column = new ApprovalLineDTO(line, COLUMN_RECEIVE);
			list.add(column);
		}
		map.put("size", list.size());
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 진행함
	 */
	public Map<String, Object> progress(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<ApprovalLineDTO> list = new ArrayList<>();
		String approvalTitle = (String) params.get("approvalTitle");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalMaster.class, true);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, "ownership.owner.key.id", sessionUser);
		}

		if (query.getConditionCount() > 0) {
			query.appendAnd();
		}

		query.appendOpenParen();
		QuerySpecUtils.toEquals(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_APPROVAL_APPROVING);
		QuerySpecUtils.toEqualsOr(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_AGREE_READY);
		query.appendCloseParen();

		QuerySpecUtils.toLikeAnd(query, idx, ApprovalMaster.class, ApprovalMaster.NAME, approvalTitle);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalMaster.class, ApprovalMaster.START_TIME, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalMaster master = (ApprovalMaster) obj[0];
			ApprovalLineDTO column = new ApprovalLineDTO(master, COLUMN_PROGRESS);
			list.add(column);
		}
		map.put("size", list.size());
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 완료함
	 */
	public Map<String, Object> complete(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<ApprovalLineDTO> list = new ArrayList<>();
		String approvalTitle = (String) params.get("approvalTitle");
		String receiveFrom = (String) params.get("receiveFrom");
		String receiveTo = (String) params.get("receiveTo");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalMaster.class, true);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, "ownership.owner.key.id", sessionUser);
		}

		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, ApprovalMaster.STATE,
				STATE_MASTER_APPROVAL_COMPELTE);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, ApprovalMaster.class, ApprovalMaster.CREATE_TIMESTAMP,
				receiveFrom, receiveTo);
		QuerySpecUtils.toLikeAnd(query, idx, ApprovalMaster.class, ApprovalMaster.NAME, approvalTitle);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalMaster.class, ApprovalMaster.START_TIME, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalMaster master = (ApprovalMaster) obj[0];
			ApprovalLineDTO column = new ApprovalLineDTO(master, COLUMN_COMPLETE);
			list.add(column);
		}
		map.put("size", list.size());
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 반려함
	 */
	public Map<String, Object> reject(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<ApprovalLineDTO> list = new ArrayList<>();
		String approvalTitle = (String) params.get("approvalTitle");

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalMaster.class, true);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, "ownership.owner.key.id", sessionUser);
		}

		if (query.getConditionCount() > 0) {
			query.appendAnd();
		}

		query.appendOpenParen();
		QuerySpecUtils.toEquals(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_MASTER_AGREE_REJECT);
		QuerySpecUtils.toEqualsOr(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_MASTER_APPROVAL_REJECT);
		query.appendCloseParen();

		QuerySpecUtils.toLikeAnd(query, idx, ApprovalMaster.class, ApprovalMaster.NAME, approvalTitle);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalMaster.class, ApprovalMaster.START_TIME, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalMaster master = (ApprovalMaster) obj[0];
			ApprovalLineDTO column = new ApprovalLineDTO(master, COLUMN_REJECT);
			list.add(column);
		}
		map.put("size", list.size());
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 기안 라인 가져오기
	 */
	public ApprovalLine getSubmitLine(ApprovalMaster master) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "masterReference.key.id", master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.ROLE, WORKING_SUBMIT);
//		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, SUBMIT_LINE);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine line = (ApprovalLine) obj[0];
			return line;
		}
		return null;
	}

	/**
	 * 결재 라인 가져오기
	 */
	public ArrayList<ApprovalLine> getApprovalLines(ApprovalMaster master) throws Exception {
		ArrayList<ApprovalLine> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "masterReference.key.id", master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.ROLE, WORKING_APPROVAL);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, APPROVAL_LINE);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine line = (ApprovalLine) obj[0];
			list.add(line);
		}
		return list;
	}

	/**
	 * 검토 라인 가져오기
	 */
	public ArrayList<ApprovalLine> getAgreeLines(ApprovalMaster master) throws Exception {
		ArrayList<ApprovalLine> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "masterReference.key.id", master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.ROLE, WORKING_AGREE);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, AGREE_LINE);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine line = (ApprovalLine) obj[0];
			list.add(line);
		}
		return list;
	}

	/**
	 * 수신 라인 가져오기
	 */
	public ArrayList<ApprovalLine> getReceiveLines(ApprovalMaster master) throws Exception {
		ArrayList<ApprovalLine> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "masterReference.key.id", master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.ROLE, WORKING_RECEIVE);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, RECEIVE_LINE);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine line = (ApprovalLine) obj[0];
			list.add(line);
		}
		return list;
	}

	/**
	 * 결재 마스터 객체 가져오기
	 */
	public ApprovalMaster getMaster(Persistable per) throws Exception {
		QueryResult result = PersistenceHelper.manager.navigate(per, "lineMaster", PersistableLineMasterLink.class);
		if (result.hasMoreElements()) {
			return (ApprovalMaster) result.nextElement();
		}
		return null;
	}

	public ArrayList<ApprovalMaster> getMasterArray(Persistable per) throws Exception {
		ArrayList<ApprovalMaster> list = new ArrayList<>();

		QueryResult result = PersistenceHelper.manager.navigate(per, "lineMaster", PersistableLineMasterLink.class);

		while (result.hasMoreElements()) {
			list.add((ApprovalMaster) result.nextElement());
		}
		return list;
	}

	/**
	 * 결재 이력 그리드용
	 */
	public JSONArray jsonAuiHistory(String oid) throws Exception {
		Persistable per = (Persistable) CommonUtils.getObject(oid);
		ArrayList<Map<String, String>> list = new ArrayList<>();
		ApprovalMaster master = getMaster(per);

		if (master != null) {
			ApprovalLine submit = getSubmitLine(master);
			Map<String, String> data = new HashMap<>();
			data.put("type", submit.getType());
			data.put("role", submit.getRole());
			data.put("name", submit.getName());
			data.put("state", submit.getState());
			data.put("owner", submit.getOwnership().getOwner().getFullName());
			data.put("receiveDate_txt", submit.getStartTime().toString().substring(0, 16));
			data.put("completeDate_txt", submit.getCompleteTime().toString().substring(0, 16));
			data.put("description", submit.getDescription());
			list.add(data);

			ArrayList<ApprovalLine> agreeLines = getAgreeLines(master);
			for (ApprovalLine agreeLine : agreeLines) {
				Map<String, String> map = new HashMap<>();
				map.put("type", agreeLine.getType());
				map.put("role", agreeLine.getRole());
				map.put("name", agreeLine.getName());
				map.put("state", agreeLine.getState());
				map.put("owner", agreeLine.getOwnership().getOwner().getFullName());
				map.put("receiveDate_txt", agreeLine.getStartTime().toString().substring(0, 16));
				map.put("completeDate_txt",
						agreeLine.getCompleteTime() != null ? agreeLine.getCompleteTime().toString().substring(0, 16)
								: "");
				map.put("description", agreeLine.getDescription());
				list.add(map);
			}

			ArrayList<ApprovalLine> approvalLines = getApprovalLines(master);
			for (ApprovalLine approvalLine : approvalLines) {
				Map<String, String> map = new HashMap<>();
				map.put("type", approvalLine.getType());
				map.put("role", approvalLine.getRole());
				map.put("name", approvalLine.getName());
				map.put("state", approvalLine.getState());
				map.put("owner", approvalLine.getOwnership().getOwner().getFullName());
				map.put("receiveDate_txt",
						approvalLine.getStartTime() != null ? approvalLine.getStartTime().toString().substring(0, 16)
								: "");
				map.put("completeDate_txt",
						approvalLine.getCompleteTime() != null
								? approvalLine.getCompleteTime().toString().substring(0, 16)
								: "");
				map.put("description", approvalLine.getDescription());
				list.add(map);
			}

			ArrayList<ApprovalLine> receiveLines = getReceiveLines(master);
			for (ApprovalLine receiveLine : receiveLines) {
				Map<String, String> map = new HashMap<>();
				map.put("type", receiveLine.getType());
				map.put("role", receiveLine.getRole());
				map.put("name", receiveLine.getName());
				map.put("state", receiveLine.getState());
				map.put("owner", receiveLine.getOwnership().getOwner().getFullName());
				map.put("receiveDate_txt", receiveLine.getStartTime().toString().substring(0, 16));
				map.put("completeDate_txt",
						receiveLine.getCompleteTime() != null
								? receiveLine.getCompleteTime().toString().substring(0, 16)
								: "");
				map.put("description", receiveLine.getDescription());
				list.add(map);
			}
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 마스터와 관련된 모든 결재 라인 가져오기
	 */
	public ArrayList<ApprovalLine> getAllLines(ApprovalMaster master) throws Exception {
		ArrayList<ApprovalLine> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, false);
		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "masterReference.key.id", master);
		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine line = (ApprovalLine) obj[0];
			list.add(line);
		}
		return list;

	}

	/**
	 * 메인페이지에서 보여질 결재 리스트
	 */
	public JSONArray firstPageData(WTUser sessionUser) throws Exception {
		ArrayList<Map<String, String>> list = new ArrayList<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "ownership.owner.key.id", sessionUser);

		if (query.getConditionCount() > 0) {
			query.appendAnd();
		}

		query.appendOpenParen();
		SearchCondition sc = new SearchCondition(ApprovalLine.class, ApprovalLine.TYPE, "=", AGREE_LINE);
		query.appendWhere(sc, new int[] { idx });
		query.appendOr();
		sc = new SearchCondition(ApprovalLine.class, ApprovalLine.TYPE, "=", APPROVAL_LINE);
		query.appendWhere(sc, new int[] { idx });
		query.appendCloseParen();

		query.appendAnd();

		query.appendOpenParen();
		sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=", STATE_APPROVAL_APPROVING);
		query.appendWhere(sc, new int[] { idx });
		query.appendOr();
		sc = new SearchCondition(ApprovalLine.class, ApprovalLine.STATE, "=", STATE_AGREE_READY);
		query.appendWhere(sc, new int[] { idx });

		query.appendCloseParen();

		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.CREATE_TIMESTAMP, true);

		QueryResult result = PagingSessionHelper.openPagingSession(0, 50, query);
//		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine approvalLine = (ApprovalLine) obj[0];
			Map<String, String> map = new HashMap<>();
			map.put("name", approvalLine.getName());
			map.put("oid", approvalLine.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("poid",
					approvalLine.getMaster().getPersist() != null
							? approvalLine.getMaster().getPersist().getPersistInfo().getObjectIdentifier()
									.getStringValue()
							: "");
			map.put("createdDate_txt", CommonUtils.getPersistableTime(approvalLine.getCreateTimestamp(), 16));
			list.add(map);
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 마지막 검토 라인인지 확인
	 */
	public boolean isEndAgree(ApprovalMaster master) throws Exception {

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "masterReference.key.id", master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, AGREE_LINE);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_AGREE_READY);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 마지막 결재 라인인지 체크
	 */
	public boolean isEndApprovalLine(ApprovalMaster master, int sort) throws Exception {

		boolean isEndApprovalLine = true;
		ArrayList<ApprovalLine> list = getApprovalLines(master);
		for (ApprovalLine appLine : list) {
			int compare = appLine.getSort();
			if (sort <= compare) {
				isEndApprovalLine = false;
				break;
			}
		}
		return isEndApprovalLine;
	}

	/**
	 * 모든 결재 라인 삭제
	 */
	public void deleteAllLines(Persistable per) throws Exception {
		ArrayList<ApprovalMaster> masterArray = getMasterArray(per);

		for (ApprovalMaster master : masterArray) {
			if (master != null) {

				Object pbo = master.getPersist();
				if (pbo != null) {
					if (pbo instanceof Versioned) {
						LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) per, State.toState("INWORK"));
					}
					if (pbo instanceof Managed) {
						LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) per, State.toState("INWORK"));
					}
					if (pbo instanceof ApprovalContract) {
						ApprovalContract contract = (ApprovalContract) per;

						QueryResult result = PersistenceHelper.manager.navigate(contract, "persist",
								ApprovalContractPersistableLink.class);
						while (result.hasMoreElements()) {
							Persistable cont = (Persistable) result.nextElement();
							if (cont instanceof Versioned) {
								LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) cont,
										State.toState("INWORK"));
							}
							if (cont instanceof Managed) {
								LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) cont,
										State.toState("INWORK"));
							}
						}
					}
				}
				ApprovalLine submitLine = getSubmitLine(master);
				PersistenceHelper.manager.delete(submitLine);

				ArrayList<ApprovalLine> approvalLines = getApprovalLines(master);
				ArrayList<ApprovalLine> agreeLines = getAgreeLines(master);
				ArrayList<ApprovalLine> receiveLines = getReceiveLines(master);

				for (ApprovalLine line : approvalLines) {
					PersistenceHelper.manager.delete(line);
				}
				for (ApprovalLine line : agreeLines) {
					PersistenceHelper.manager.delete(line);
				}
				for (ApprovalLine line : receiveLines) {
					PersistenceHelper.manager.delete(line);
				}
				PersistenceHelper.manager.delete(master);
			}
		}
	}

	/**
	 * 지정된 결재선 불러오기
	 */
	public JSONArray loadAllLines(String oid) throws Exception {
		Persistable per = (Persistable) CommonUtils.getObject(oid);
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		ApprovalMaster master = getMaster(per);
		if (master != null) {
			ArrayList<ApprovalLine> approvalLines = getApprovalLines(master);
			ArrayList<ApprovalLine> agreeLines = getAgreeLines(master);
			ArrayList<ApprovalLine> receiveLines = getReceiveLines(master);

			for (ApprovalLine line : agreeLines) {
				WTUser wtUser = (WTUser) line.getOwnership().getOwner().getPrincipal();
				UserDTO dto = new UserDTO(wtUser);
				Map<String, Object> map = new HashMap<>();
				map.put("woid", wtUser.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("type", "검토");
				map.put("name", dto.getName());
				map.put("id", dto.getId());
				map.put("duty", dto.getDuty());
				map.put("department_name", dto.getDepartment_name());
				map.put("email", dto.getEmail());
				list.add(map);
			}

			int sort = 1;
			for (ApprovalLine line : approvalLines) {
				WTUser wtUser = (WTUser) line.getOwnership().getOwner().getPrincipal();
				UserDTO dto = new UserDTO(wtUser);
				Map<String, Object> map = new HashMap<>();
				map.put("woid", wtUser.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("sort", sort);
				map.put("type", "결재");
				map.put("name", dto.getName());
				map.put("id", dto.getId());
				map.put("duty", dto.getDuty());
				map.put("department_name", dto.getDepartment_name());
				map.put("email", dto.getEmail());
				list.add(map);
				sort++;
			}

			for (ApprovalLine line : receiveLines) {
				WTUser wtUser = (WTUser) line.getOwnership().getOwner().getPrincipal();
				UserDTO dto = new UserDTO(wtUser);
				Map<String, Object> map = new HashMap<>();
				map.put("woid", wtUser.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("type", "수신");
				map.put("name", dto.getName());
				map.put("id", dto.getId());
				map.put("duty", dto.getDuty());
				map.put("department_name", dto.getDepartment_name());
				map.put("email", dto.getEmail());
				list.add(map);
			}
		}
		return JSONArray.fromObject(list);
	}

	/**
	 * 일괄결재 prefix 가져오기
	 */
	public String prefix(Persistable per) {
		String prefix = "";
		if (per instanceof WTDocument) {
		} else if (per instanceof EPMDocument) {
			prefix = "도면";
		} else if (per instanceof WTPart) {
			prefix = "부품";
		} else if (per instanceof PartListMaster) {
			prefix = "수배표";
		}
		return prefix;
	}

	/**
	 * 결재 의견 가져오기
	 */
	public String getDescription(Persistable persistable) throws Exception {
		String description = "";
		if (persistable instanceof ApprovalContract) {
			ApprovalContract contract = (ApprovalContract) persistable;
			description = contract.getDescription();
		} else if (persistable instanceof PartListMaster) {
			PartListMaster m = (PartListMaster) persistable;
			description = m.getDescription();
		} else if (persistable instanceof WTDocument) {
			WTDocument m = (WTDocument) persistable;
			description = m.getDescription();
		} else if (persistable instanceof PRJDocument) {
			PRJDocument m = (PRJDocument) persistable;
			description = m.getDescription();
		}

		if (StringUtils.isNull(description)) {
			WTUser u = CommonUtils.sessionUser();
			description = u.getFullName() + " 사용자가 결재를 제출하였습니다.";
		}

		return description;
	}

	/**
	 * 일괄격채 데이터 가져오기
	 */
	public ArrayList<Map<String, Object>> contractData(ApprovalContract contract) throws Exception {
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		QueryResult result = PersistenceHelper.manager.navigate(contract, "persist",
				ApprovalContractPersistableLink.class);
		while (result.hasMoreElements()) {
			Persistable per = (Persistable) result.nextElement();
			Map<String, Object> map = new HashMap<>();

			if (per instanceof WTPart) {
				WTPart part = (WTPart) per;
				map.put("oid", part.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("name", part.getName());
//				map.put("dwg_no", IBAUtils.getStringValue(part, "DWG_NO"));
//				map.put("nameOfParts", IBAUtils.getStringValue(part, "NAME_OF_PARTS"));

				EPMDocument epm = PartHelper.manager.getEPMDocument(part);
				if ((epm != null && epm.getAuthoringApplication().getDisplay().equalsIgnoreCase("Creo"))) {
					map.put("nameOfParts", IBAUtils.getStringValue(part, "NAME_OF_PARTS"));
					map.put("dwg_no", IBAUtils.getStringValue(part, "DWG_NO"));
				} else if ((epm != null && epm.getAuthoringApplication().getDisplay().equals("AutoCAD"))) {
					map.put("nameOfParts",
							IBAUtils.getStringValue(part, "TITLE1") + " " + IBAUtils.getStringValue(part, "TITLE2"));
					map.put("dwg_no", IBAUtils.getStringValue(part, "DWG_No"));
				} else {
					map.put("nameOfParts", part.getName());
					map.put("dwg_no", part.getNumber());
				}

				map.put("state", part.getLifeCycleState().getDisplay());
				map.put("version", part.getVersionIdentifier().getSeries().getValue() + "."
						+ part.getIterationIdentifier().getSeries().getValue());
				map.put("creator", part.getCreatorFullName());
				map.put("modifier", part.getModifierFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(part.getCreateTimestamp()));

				boolean isLib = part.getContainer().getName().indexOf("LIBRARY") > -1;
				map.put("isLib", isLib);

				list.add(map);
			} else if (per instanceof EPMDocument) {
				EPMDocument epm = (EPMDocument) per;

				EPMDocument latest = (EPMDocument) CommonUtils.getLatestVersion(epm);

				map.put("oid", latest.getPersistInfo().getObjectIdentifier().getStringValue());
//				map.put("name", latest.getName());
				map.put("name", latest.getCADName());
				map.put("nameOfParts", IBAUtils.getStringValue(latest, "NAME_OF_PARTS"));
				map.put("dwgNo", IBAUtils.getStringValue(latest, "DWG_NO"));
				map.put("state", latest.getLifeCycleState().getDisplay());
				map.put("version", latest.getVersionIdentifier().getSeries().getValue() + "."
						+ latest.getIterationIdentifier().getSeries().getValue());
				map.put("creator", latest.getCreatorFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(latest.getCreateTimestamp()));
				list.add(map);
			} else if (per instanceof NumberRule) {
				NumberRule numberRule = (NumberRule) per;
				map.put("number", numberRule.getMaster().getNumber());
				map.put("size_txt",
						numberRule.getMaster().getSize() == null ? "" : numberRule.getMaster().getSize().getName());
				map.put("lotNo", String.valueOf(numberRule.getMaster().getLotNo()));
				map.put("unitName", numberRule.getMaster().getUnitName());
				map.put("name", numberRule.getMaster().getName());
				map.put("businessSector_txt",
						numberRule.getMaster().getSector() == null ? "" : numberRule.getMaster().getSector().getName());
				map.put("classificationWritingDepartments_txt", numberRule.getMaster().getDepartment() == null ? ""
						: numberRule.getMaster().getDepartment().getName());
				map.put("writtenDocuments_txt", numberRule.getMaster().getDocument() == null ? ""
						: numberRule.getMaster().getDocument().getName());
				map.put("version", String.valueOf(numberRule.getVersion()));
				map.put("state", numberRule.getState());
				map.put("creator", numberRule.getMaster().getOwnership().getOwner().getFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(numberRule.getMaster().getCreateTimestamp()));
				map.put("modifier", numberRule.getOwnership().getOwner().getFullName());
				map.put("modifiedDate_txt", CommonUtils.getPersistableTime(numberRule.getCreateTimestamp()));
				map.put("oid", numberRule.getPersistInfo().getObjectIdentifier().getStringValue());
				list.add(map);
			} else if (per instanceof WTDocument) {
				WTDocument document = (WTDocument) per;
				map.put("oid", document.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("name", document.getName());
				map.put("number", document.getNumber());
				map.put("state", document.getLifeCycleState().getDisplay());
				map.put("version", document.getVersionIdentifier().getSeries().getValue() + "."
						+ document.getIterationIdentifier().getSeries().getValue());
				map.put("creator", document.getCreatorFullName());
				map.put("createdDate_txt", CommonUtils.getPersistableTime(document.getCreateTimestamp()));
				list.add(map);
			} else if (per instanceof Output) {

				Output output = (Output) per;
				Project project = output.getProject();
				LifeCycleManaged lcm = output.getDocument();
				Task task = output.getTask();

				// 산출물 일괄결재
				map.put("poid", project.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("kekNumber", project.getKekNumber());
				map.put("toid", task.getPersistInfo().getObjectIdentifier().getStringValue());
				map.put("taskName", task.getName());
				map.put("contentHolder", (ContentHolder) lcm);
				if (lcm instanceof ConfigSheet) {
					ConfigSheet configSheet = (ConfigSheet) lcm;
					map.put("oid", configSheet.getPersistInfo().getObjectIdentifier().getStringValue());
					map.put("type", "CONFIG SHEET");
					map.put("name", configSheet.getName());
					map.put("state", configSheet.getLifeCycleState().getDisplay());
					map.put("creator", configSheet.getCreatorFullName());
					map.put("createdDate_txt", CommonUtils.getPersistableTime(configSheet.getCreateTimestamp()));
					map.put("version", String.valueOf(configSheet.getVersion()));
					map.put("primary", AUIGridUtils.primaryTemplate(configSheet));
					map.put("secondary", AUIGridUtils.secondaryTemplate(configSheet));
					// tbom
				} else if (lcm instanceof TBOMMaster) {
					TBOMMaster master = (TBOMMaster) lcm;
					map.put("oid", master.getPersistInfo().getObjectIdentifier().getStringValue());
					map.put("type", "T-BOM");
					map.put("name", master.getName());
					map.put("state", master.getLifeCycleState().getDisplay());
					map.put("creator", master.getCreatorFullName());
					map.put("createdDate_txt", CommonUtils.getPersistableTime(master.getCreateTimestamp()));
					map.put("version", String.valueOf(master.getVersion()));
					map.put("primary", AUIGridUtils.primaryTemplate(master));
					map.put("secondary", AUIGridUtils.secondaryTemplate(master));
					// workorder
				} else if (lcm instanceof WorkOrder) {
					WorkOrder workOrder = (WorkOrder) lcm;
					map.put("oid", workOrder.getPersistInfo().getObjectIdentifier().getStringValue());
					map.put("type", "도면일람표");
					map.put("name", workOrder.getName());
					map.put("state", workOrder.getLifeCycleState().getDisplay());
					map.put("creator", workOrder.getCreatorFullName());
					map.put("createdDate_txt", CommonUtils.getPersistableTime(workOrder.getCreateTimestamp()));
					map.put("version", String.valueOf(workOrder.getVersion()));
					map.put("primary", AUIGridUtils.primaryTemplate(workOrder));
					map.put("secondary", AUIGridUtils.secondaryTemplate(workOrder));
					// document
				} else if (lcm instanceof WTDocument) {
					WTDocument document = (WTDocument) lcm;
					map.put("oid", document.getPersistInfo().getObjectIdentifier().getStringValue());
					map.put("type", "산출물");
					map.put("name", document.getName());
					map.put("state", document.getLifeCycleState().getDisplay());
					map.put("creator", document.getCreatorFullName());
					map.put("createdDate_txt", CommonUtils.getPersistableTime(document.getCreateTimestamp()));
					map.put("version", document.getVersionIdentifier().getSeries().getValue());
					map.put("primary", AUIGridUtils.primaryTemplate(document));
					map.put("secondary", AUIGridUtils.secondaryTemplate(document));
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
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * 결재 내역들 개수 - 접속한 사용자
	 */
	public Map<String, Integer> count() throws Exception {

		int agree = _agree();
		int approval = _approval();
		int receive = _receive();
		int progress = _progress();
		int complete = _complete();
		int reject = _reject();

		Map<String, Integer> count = new HashMap<>();
		count.put("agree", agree);
		count.put("approval", approval);
		count.put("receive", receive);
		count.put("progress", progress);
		count.put("complete", complete);
		count.put("reject", reject);

		return count;
	}

	public int _reject() throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalMaster.class, true);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, "ownership.owner.key.id", sessionUser);
		}

		if (query.getConditionCount() > 0) {
			query.appendAnd();
		}

		query.appendOpenParen();
		QuerySpecUtils.toEquals(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_MASTER_AGREE_REJECT);
		QuerySpecUtils.toEqualsOr(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_MASTER_APPROVAL_REJECT);
		query.appendCloseParen();

		QuerySpecUtils.toOrderBy(query, idx, ApprovalMaster.class, ApprovalMaster.START_TIME, true);
		QueryResult result = PersistenceHelper.manager.find(query);
		return result.size();
	}

	public int _complete() throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalMaster.class, true);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, "ownership.owner.key.id", sessionUser);
		}

		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, ApprovalMaster.STATE,
				STATE_MASTER_APPROVAL_COMPELTE);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalMaster.class, ApprovalMaster.START_TIME, true);

		QueryResult result = PersistenceHelper.manager.find(query);
		return result.size();
	}

	public int _progress() throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalMaster.class, true);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalMaster.class, "ownership.owner.key.id", sessionUser);
		}

		if (query.getConditionCount() > 0) {
			query.appendAnd();
		}

		query.appendOpenParen();
		QuerySpecUtils.toEquals(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_APPROVAL_APPROVING);
		QuerySpecUtils.toEqualsOr(query, idx, ApprovalMaster.class, ApprovalMaster.STATE, STATE_AGREE_READY);
		query.appendCloseParen();

		QuerySpecUtils.toOrderBy(query, idx, ApprovalMaster.class, ApprovalMaster.START_TIME, true);

		QueryResult result = PersistenceHelper.manager.find(query);
		return result.size();
	}

	public int _receive() throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_master = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_RECEIVE_READY);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, RECEIVE_LINE);

		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "ownership.owner.key.id", sessionUser);
		}

		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, true);
		QueryResult result = PersistenceHelper.manager.find(query);
		return result.size();
	}

	public int _approval() throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_m = query.appendClassList(ApprovalMaster.class, false);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_m);

		// 쿼리 수정할 예정
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_APPROVAL_APPROVING);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, APPROVAL_LINE);

		WTUser sessionUser = CommonUtils.sessionUser();

//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toCreator(query, idx, ApprovalLine.class,
					sessionUser.getPersistInfo().getObjectIdentifier().getStringValue());
		}

		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, true);

		QueryResult result = PersistenceHelper.manager.find(query);
		return result.size();
	}

	public int _agree() throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_master = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_master);

		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.STATE, STATE_AGREE_READY);

		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, ApprovalLine.TYPE, AGREE_LINE);
		WTUser sessionUser = CommonUtils.sessionUser();
//		if (!CommonUtils.isAdmin()) {
		if (!"wcadmin".equals(sessionUser.getFullName())) {
//		if (!"wcadmin".equals(sessionUser.getFullName()) && !CommonUtils.isAdmin()) {
			QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "ownership.owner.key.id", sessionUser);
		}
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, true);
		QueryResult result = PersistenceHelper.manager.find(query);
		return result.size();
	}

	/**
	 * 도면 승인 일람표 생성
	 */
	public Workbook print(String oid) throws Exception {

		String numberRulePath = WTProperties.getLocalProperties().getProperty("wt.codebase.location") + File.separator
				+ "extcore" + File.separator + "excelTemplate" + File.separator + "NUMBERRULE-TEMPLATE.xlsx";
		FileInputStream fis = new FileInputStream(numberRulePath);
		Workbook workbook = new XSSFWorkbook(fis);
		Sheet sheet = workbook.getSheetAt(0);

		ApprovalContract contract = (ApprovalContract) CommonUtils.getObject(oid);
		QueryResult result = PersistenceHelper.manager.navigate(contract, "persist",
				ApprovalContractPersistableLink.class);

		// cell style
		CellStyle cellStyle = workbook.createCellStyle();
		// cell font
		Font font = workbook.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		// 정렬 설정
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		// 경계선 설정
		cellStyle.setBorderTop(BorderStyle.MEDIUM);
		cellStyle.setBorderBottom(BorderStyle.MEDIUM);
		cellStyle.setBorderLeft(BorderStyle.MEDIUM);
		cellStyle.setBorderRight(BorderStyle.MEDIUM);

		int rowNum = 1;
		int rowIndex = 14;
		while (result.hasMoreElements()) {
			Persistable per = (Persistable) result.nextElement();
			if (per instanceof NumberRule) {
				NumberRule numberRule = (NumberRule) per;

				String name = numberRule.getMaster().getName();
				String number = numberRule.getMaster().getNumber();

				Row row = sheet.createRow(rowIndex);

				CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex, 1, 3);
				CellRangeAddress region1 = new CellRangeAddress(rowIndex, rowIndex, 4, 6);
				CellRangeAddress region2 = new CellRangeAddress(rowIndex, rowIndex, 7, 9);
				CellRangeAddress region3 = new CellRangeAddress(rowIndex, rowIndex, 11, 14);

				sheet.addMergedRegion(region);
				sheet.addMergedRegion(region1);
				sheet.addMergedRegion(region2);
				sheet.addMergedRegion(region3);

				Cell cell = row.createCell(0);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(String.valueOf(rowNum));

				cell = row.createCell(1);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(numberRule.getMaster().getLotNo() + " / " + numberRule.getMaster().getUnitName());

				cell = row.createCell(2);
				cell.setCellStyle(cellStyle);
				cell = row.createCell(3);
				cell.setCellStyle(cellStyle);

				cell = row.createCell(4);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(name);

				cell = row.createCell(5);
				cell.setCellStyle(cellStyle);
				cell = row.createCell(6);
				cell.setCellStyle(cellStyle);

				cell = row.createCell(7);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(number);

				cell = row.createCell(8);
				cell.setCellStyle(cellStyle);
				cell = row.createCell(9);
				cell.setCellStyle(cellStyle);

				cell = row.createCell(10);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(String.valueOf(numberRule.getVersion()));

				cell = row.createCell(11);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(numberRule.getNote());
				cell = row.createCell(12);
				cell.setCellStyle(cellStyle);
				cell = row.createCell(13);
				cell.setCellStyle(cellStyle);
				cell = row.createCell(14);
				cell.setCellStyle(cellStyle);

				rowNum++;
				rowIndex++;
			}
		}
		return workbook;
	}

	/**
	 * 결재시 관련 작번
	 */
	public JSONArray getProjects(String oid) throws Exception {
		Persistable per = CommonUtils.getObject(oid);
		if (per instanceof TBOMMaster) {
			return TBOMHelper.manager.getProjects((TBOMMaster) per);
		} else if (per instanceof WorkOrder) {
			return WorkOrderHelper.manager.getProjects((WorkOrder) per);
		} else if (per instanceof PartListMaster) {
			return PartlistHelper.manager.getProjects((PartListMaster) per);
		} else if (per instanceof ConfigSheet) {
			return ConfigSheetHelper.manager.getProjects((ConfigSheet) per);
		} else if (per instanceof RequestDocument) {
			return RequestDocumentHelper.manager.getProjects((RequestDocument) per);
		} else if (per instanceof WTDocument) {
			return OutputHelper.manager.getProjects((WTDocument) per);
		}
		return null;
	}

	/**
	 * 개인결재선 조회 함수
	 */
	public Map<String, Object> loadLine(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name");
		Map<String, Object> result = new HashMap<>();
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		WTUser sessionUser = CommonUtils.sessionUser();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalUserLine.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalUserLine.class, "ownership.owner.key.id", sessionUser);
		QuerySpecUtils.toLikeAnd(query, idx, ApprovalUserLine.class, ApprovalUserLine.NAME, name);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalUserLine.class, ApprovalUserLine.NAME, false);
		QueryResult rs = PersistenceHelper.manager.find(query);
		while (rs.hasMoreElements()) {
			Object[] obj = (Object[]) rs.nextElement();
			ApprovalUserLine line = (ApprovalUserLine) obj[0];
			Map<String, Object> map = new HashMap<>();
			map.put("oid", line.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("name", line.getName());
			map.put("favorite", line.getFavorite());
			list.add(map);
		}
		result.put("list", list);
		return result;
	}

	/**
	 * 개인결재선 즐겨찾기 불러오는 함수
	 */
	public Map<String, Object> loadFavorite(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();

		WTUser sessionUser = CommonUtils.sessionUser();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalUserLine.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalUserLine.class, "ownership.owner.key.id", sessionUser);
		QuerySpecUtils.toBooleanAnd(query, idx, ApprovalUserLine.class, ApprovalUserLine.FAVORITE, true);
		QueryResult rs = PersistenceHelper.manager.find(query);

		ArrayList<UserDTO> approval = new ArrayList<>();
		ArrayList<UserDTO> agree = new ArrayList<>();
		ArrayList<UserDTO> receive = new ArrayList<>();

		if (rs.hasMoreElements()) {
			Object[] obj = (Object[]) rs.nextElement();
			ApprovalUserLine line = (ApprovalUserLine) obj[0];
			ArrayList<String> approvalList = (ArrayList<String>) line.getApprovalList();
			for (String oid : approvalList) {
				if (oid.indexOf("&") > -1) {
					int start = oid.indexOf("&");
					String s = oid.substring(0, start);
					People p = (People) CommonUtils.getObject(s);
					UserDTO dto = new UserDTO(p);
					approval.add(dto);
				} else {
					People p = (People) CommonUtils.getObject(oid);
					UserDTO dto = new UserDTO(p);
					approval.add(dto);
				}
			}
			ArrayList<String> agreeList = (ArrayList<String>) line.getAgreeList();
			for (String oid : agreeList) {
				if (oid.indexOf("&") > -1) {
					int start = oid.indexOf("&");
					String s = oid.substring(0, start);
					People p = (People) CommonUtils.getObject(s);
					UserDTO dto = new UserDTO(p);
					agree.add(dto);
				} else {
					People p = (People) CommonUtils.getObject(oid);
					UserDTO dto = new UserDTO(p);
					agree.add(dto);
				}
			}
			ArrayList<String> receiveList = (ArrayList<String>) line.getReceiveList();
			for (String oid : receiveList) {
				if (oid.indexOf("&") > -1) {
					int start = oid.indexOf("&");
					String s = oid.substring(0, start);
					People p = (People) CommonUtils.getObject(s);
					UserDTO dto = new UserDTO(p);
					receive.add(dto);
				} else {
					People p = (People) CommonUtils.getObject(oid);
					UserDTO dto = new UserDTO(p);
					receive.add(dto);
				}
			}
		}

		map.put("approval", approval);
		map.put("agree", agree);
		map.put("receive", receive);
		return map;
	}

	/**
	 * 개인결재선 불러오는 함수
	 */
	public Map<String, Object> loadFavorite(String _oid) throws Exception {
		Map<String, Object> map = new HashMap<>();
		ArrayList<UserDTO> approval = new ArrayList<>();
		ArrayList<UserDTO> agree = new ArrayList<>();
		ArrayList<UserDTO> receive = new ArrayList<>();
		ApprovalUserLine line = (ApprovalUserLine) CommonUtils.getObject(_oid);
		ArrayList<String> approvalList = (ArrayList<String>) line.getApprovalList();
		for (String oid : approvalList) {
			if (oid.indexOf("&") > -1) {
				int start = oid.indexOf("&");
				String s = oid.substring(0, start);
				People p = (People) CommonUtils.getObject(s);
				UserDTO dto = new UserDTO(p);
				approval.add(dto);
			} else {
				People p = (People) CommonUtils.getObject(oid);
				UserDTO dto = new UserDTO(p);
				approval.add(dto);
			}
		}
		ArrayList<String> agreeList = (ArrayList<String>) line.getAgreeList();
		for (String oid : agreeList) {
			if (oid.indexOf("&") > -1) {
				int start = oid.indexOf("&");
				String s = oid.substring(0, start);
				People p = (People) CommonUtils.getObject(s);
				UserDTO dto = new UserDTO(p);
				agree.add(dto);
			} else {
				People p = (People) CommonUtils.getObject(oid);
				UserDTO dto = new UserDTO(p);
				agree.add(dto);
			}
		}
		ArrayList<String> receiveList = (ArrayList<String>) line.getReceiveList();
		for (String oid : receiveList) {
			if (oid.indexOf("&") > -1) {
				int start = oid.indexOf("&");
				String s = oid.substring(0, start);
				People p = (People) CommonUtils.getObject(s);
				UserDTO dto = new UserDTO(p);
				receive.add(dto);
			} else {
				People p = (People) CommonUtils.getObject(oid);
				UserDTO dto = new UserDTO(p);
				receive.add(dto);
			}
		}
		map.put("approval", approval);
		map.put("agree", agree);
		map.put("receive", receive);
		return map;
	}

	/**
	 * 개인결재선 중복 체크
	 */
	public Map<String, Object> validate(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name");
		Map<String, Object> result = new HashMap<>();
		WTUser sessionUser = CommonUtils.sessionUser();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalUserLine.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalUserLine.class, ApprovalUserLine.NAME, name);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalUserLine.class, "ownership.owner.key.id", sessionUser);
		QueryResult qr = PersistenceHelper.manager.find(query);
		if (qr.size() > 0) {
			result.put("validate", true);
			result.put("msg", "중복된 개인결재선 이름입니다.");
			return result;
		}
		result.put("validate", false);
		return result;
	}

	public String getMasterAppType(Persistable per) throws Exception {
		String reValue = "일반";
		if (per instanceof WTDocument) {
			reValue = "문서";
		} else if (per instanceof WTPart) {
			reValue = "부품";
		} else if (per instanceof EPMDocument) {
			reValue = "도면";
		} else if (per instanceof ApprovalContract) {
			reValue = "일괄결재";
		} else if (per instanceof Managed) {
			if (per instanceof PartListMaster) {
				reValue = "수배표";
			} else if (per instanceof TBOMMaster) {
				reValue = "T-BOM";
			} else if (per instanceof WorkOrder) {
				reValue = "도면일람표";
			} else if (per instanceof ConfigSheet) {
				reValue = "CONFIG SHEET";
			} else if (per instanceof Meeting) {
				reValue = "회의록";
			}
		}
		return reValue;
	}

	public Map<String, ArrayList<ApprovalLine>> point(ApprovalMaster master) throws Exception {
		Map<String, ArrayList<ApprovalLine>> map = new HashMap<>();
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ApprovalLine.class, true);
		int idx_master = query.appendClassList(ApprovalMaster.class, true);

		QuerySpecUtils.toInnerJoin(query, ApprovalLine.class, ApprovalMaster.class, "masterReference.key.id",
				WTAttributeNameIfc.ID_NAME, idx, idx_master);
		QuerySpecUtils.toEqualsAnd(query, idx, ApprovalLine.class, "masterReference.key.id", master);
		QuerySpecUtils.toOrderBy(query, idx, ApprovalLine.class, ApprovalLine.START_TIME, false);
		QueryResult result = PersistenceHelper.manager.find(query);
		ArrayList<ApprovalLine> submit = new ArrayList<ApprovalLine>();
		ArrayList<ApprovalLine> approval = new ArrayList<ApprovalLine>();
		ArrayList<ApprovalLine> agree = new ArrayList<ApprovalLine>();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalLine line = (ApprovalLine) obj[0];
			String role = line.getRole();

			if (WORKING_SUBMIT.equals(role)) {
				submit.add(line);
			} else if (WORKING_AGREE.equals(role)) {
				agree.add(line);
			} else if (WORKING_APPROVAL.equals(role)) {
				approval.add(line);
			}
		}

		map.put("submit", submit);
		map.put("agree", agree);
		map.put("approval", approval);
		return map;
	}

	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();

		String creatorOid = (String) params.get("creatorOid");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");

		QuerySpec qs = new QuerySpec();
		int idx = qs.appendClassList(ApprovalContract.class, true);

		QuerySpecUtils.toCreator(qs, idx, ApprovalContract.class, creatorOid);
		QuerySpecUtils.toTimeGreaterAndLess(qs, idx, ApprovalContract.class, ApprovalContract.CREATE_TIMESTAMP,
				createdFrom, createdTo);
		QuerySpecUtils.toOrderBy(qs, idx, ApprovalContract.class, ApprovalContract.CREATE_TIMESTAMP, true);

//		int idxMaster = qs.appendClassList(ApprovalMaster.class, false);
//		int idxLine = qs.appendClassList(ApprovalLine.class, false);
//		
//		ClassAttribute ca = new ClassAttribute(ApprovalContract.class, "thePersistInfo.theObjectIdentifier.id");
//		ClassAttribute ca2 = new ClassAttribute(ApprovalMaster.class, "persistReference.key.id");
//		
//		if(qs.getConditionCount() > 0) {
//			qs.appendAnd();
//		}
//		qs.appendWhere(new SearchCondition(ca, "=", ca2), new int[] {idx, idxMaster});
//		
//		if(qs.getConditionCount() > 0) {
//			qs.appendAnd();
//		}
//		ClassAttribute ca3 = new ClassAttribute(ApprovalMaster.class, "thePersistInfo.theObjectIdentifier.id");
//		ClassAttribute ca4 = new ClassAttribute(ApprovalLine.class, "masterReference.key.id");
//		SearchCondition sc = new SearchCondition(ca3, "=", ca4);
//		sc.setOuterJoin(SearchCondition.LEFT_OUTER_JOIN);
//		qs.appendWhere(sc, new int[] {idxMaster, idxLine});
//		

		// qs.setAdvancedQueryEnabled(true);
		// qs.setDescendantQuery(false);

		PageQueryUtils pager = new PageQueryUtils(params, qs);
		PagingQueryResult result = pager.find();

		System.out.println(qs);

		JSONArray list = new JSONArray();

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ApprovalContract appCon = (ApprovalContract) obj[0];
			System.out.println(appCon);
			System.out.println(obj.length);
//			ApprovalMaster master = WorkspaceHelper.manager.getMaster(appCon);
//			
//			ArrayList<ApprovalLine> arr = WorkspaceHelper.manager.getAllLines(master);
//			System.out.println(arr.size());
//			if( arr.size() < 1) {
//				continue;
//			}
			System.out.println("================");
			JSONObject node = new JSONObject();

			node.put("oid", CommonUtils.getOIDString(appCon));
			node.put("name", appCon.getName());
			node.put("creator", appCon.getOwnership().getOwner().getFullName());
			node.put("createdDate_txt", CommonUtils.getPersistableTime(appCon.getCreateTimestamp()));

			list.add(node);

		}

		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	/**
	 * 도번 승인요청서 다운로드
	 */
	public Map<String, Object> kekNumber(String oid) throws Exception {
		Map<String, Object> map = new HashMap<>();

//		WTUser sessionUser = CommonUtils.sessionUser();

		ApprovalContract contract = (ApprovalContract) CommonUtils.getObject(oid);
		String wtHome = WTProperties.getServerProperties().getProperty("wt.home");
		String path = WTProperties.getServerProperties().getProperty("wt.temp");

		File orgFile = new File(wtHome + "/codebase/e3ps/workspace/dto/kekNumber-list.xlsx");

		File newFile = CommonUtils.copyFile(orgFile, new File(path + "/" + contract.getName() + "_도번승인 요청서.xlsx"));

		com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(newFile.getPath());
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("도번승인 요청서"); // 시트 이름

		Style topAndUnder = workbook.createStyle();
		topAndUnder.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		topAndUnder.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());

		Style top = workbook.createStyle();
		top.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());

		Style center = workbook.createStyle();
		center.setTextWrapped(true);
		center.setVerticalAlignment(TextAlignmentType.CENTER);
		center.setHorizontalAlignment(TextAlignmentType.CENTER);
		center.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		center.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		center.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		center.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

		Style under = workbook.createStyle();
		under.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());

		Style right = workbook.createStyle();
		right.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
		right.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());

		UserDTO dto = new UserDTO((WTUser) SessionHelper.manager.getPrincipal());

		// 날짜
		com.aspose.cells.Cell dateCell = worksheet.getCells().get(2, 12);
//		dateCell.setStyle(center);
		dateCell.putValue(DateUtils.getCurrentDateString("d"));

//		worksheet.getCells().get(2, 13).setStyle(topAndUnder);
//		worksheet.getCells().get(2, 14).setStyle(topAndUnder);

		// 부서
		com.aspose.cells.Cell deptCell = worksheet.getCells().get(3, 12);
//		deptCell.setStyle(center);
		deptCell.putValue(dto.getDepartment_name());

//		worksheet.getCells().get(4, 13).setStyle(under);
//		worksheet.getCells().get(4, 14).setStyle(under);
		// 이름
		com.aspose.cells.Cell userNameCell = worksheet.getCells().get(5, 12);
//		userNameCell.setStyle(center);
		userNameCell.putValue(dto.getName());

//		worksheet.getCells().get(5, 13).setStyle(under);
//		worksheet.getCells().get(5, 14).setStyle(under);

		ArrayList<Map<String, Object>> list = WorkspaceHelper.manager.contractData(contract);
		int rowIndex = 9;
		int index = 1;

		int rowCount = 0;
		int sheetIndex = 1;
		int count = list.size() / 25; // 3...

		Style empty = workbook.createStyle();
		empty.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.NONE, Color.getBlack());
		empty.setBorder(BorderType.TOP_BORDER, CellBorderType.NONE, Color.getBlack());
		empty.setBorder(BorderType.LEFT_BORDER, CellBorderType.NONE, Color.getBlack());
		empty.setBorder(BorderType.RIGHT_BORDER, CellBorderType.NONE, Color.getBlack());

		for (int i = 0; i < count; i++) {
			Worksheet currentSheet = workbook.getWorksheets().add("도번승인 요청서 " + count);
			currentSheet.copy(worksheet);

			currentSheet.getCells().get(6, 1).putValue("");
			currentSheet.getCells().get(6, 1).setStyle(empty);
			currentSheet.getCells().get(6, 2).setStyle(empty);

			currentSheet.getCells().get(6, 3).putValue("");
			currentSheet.getCells().get(6, 3).setStyle(empty);

			currentSheet.getCells().get(6, 4).putValue("");
			currentSheet.getCells().get(6, 4).setStyle(empty);
			currentSheet.getCells().get(6, 5).setStyle(empty);

			currentSheet.getCells().get(7, 1).setStyle(empty);
			currentSheet.getCells().get(7, 2).setStyle(empty);
			currentSheet.getCells().get(7, 3).setStyle(empty);
			currentSheet.getCells().get(7, 4).setStyle(empty);
			currentSheet.getCells().get(7, 5).setStyle(empty);
		}

		Worksheet currentSheet = worksheet; // 현재 작업 중인 시트를 추적하기 위한 변수

		for (Map<String, Object> dd : list) {
			String lotNo = (String) dd.get("lotNo");
			String unitName = (String) dd.get("unitName");
			String number = (String) dd.get("number");
			String name = (String) dd.get("name");
			String version = (String) dd.get("version");

			if (rowCount >= 25) {
				currentSheet = workbook.getWorksheets().get(sheetIndex);
//				currentSheet.autoFitRow(rowIndex);
//				currentSheet.autoFitColumn(2);
//				currentSheet.autoFitColumn(5);
				sheetIndex++;
				rowCount = 0;
				rowIndex = 9;
			}
//			currentSheet.autoFitRows(true);
			currentSheet.autoFitRow(rowIndex);

			com.aspose.cells.Cell rowCell = currentSheet.getCells().get(rowIndex, 1);
			rowCell.setStyle(center);
			rowCell.putValue(index);

			com.aspose.cells.Cell lotCell = currentSheet.getCells().get(rowIndex, 2);
			lotCell.setStyle(center);
			lotCell.putValue(lotNo + " / " + unitName);

			currentSheet.getCells().setRowHeight(rowIndex, lotCell.getHeightOfValue());

			// 3,4
			currentSheet.getCells().get(rowIndex, 3).setStyle(under);
			currentSheet.getCells().get(rowIndex, 4).setStyle(under);

			com.aspose.cells.Cell nameCell = currentSheet.getCells().get(rowIndex, 5);
			nameCell.setStyle(center);
			nameCell.putValue(name);

			currentSheet.getCells().setRowHeight(rowIndex, nameCell.getHeightOfValue());

			// 6,7,8
			currentSheet.getCells().get(rowIndex, 6).setStyle(under);
			currentSheet.getCells().get(rowIndex, 7).setStyle(under);
			currentSheet.getCells().get(rowIndex, 8).setStyle(under);

			com.aspose.cells.Cell numberCell = currentSheet.getCells().get(rowIndex, 9);
			numberCell.setStyle(center);
			numberCell.putValue(number);

			// 10 11
			currentSheet.getCells().get(rowIndex, 10).setStyle(under);

			com.aspose.cells.Cell revCell = currentSheet.getCells().get(rowIndex, 11);
			revCell.setStyle(center);
			revCell.putValue(version);

			currentSheet.getCells().get(rowIndex, 12).setStyle(under);
			currentSheet.getCells().get(rowIndex, 13).setStyle(center);
			currentSheet.getCells().get(rowIndex, 14).setStyle(right);

			index++;
			rowIndex++;
			rowCount++;
		}

		String fullPath = path + "/" + contract.getName() + "_도번승인 요청서.xlsx";
		workbook.save(fullPath);
		map.put("name", newFile.getName());
		return map;
	}
}
