package e3ps.workspace.controller;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.epm.service.EpmHelper;
import e3ps.org.Department;
import e3ps.org.service.OrgHelper;
import e3ps.system.service.ErrorLogHelper;
import e3ps.workspace.ApprovalContract;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.dto.ApprovalContractDTO;
import e3ps.workspace.dto.ApprovalLineDTO;
import e3ps.workspace.service.WorkspaceHelper;
import net.sf.json.JSONArray;
import wt.fc.Persistable;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/workspace/**")
public class WorkspaceController extends BaseController {

	@Description(value = "검토함 리스트 페이지")
	@GetMapping(value = "/agree")
	public ModelAndView agree() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);

		model.addObject("before", before);
		model.addObject("end", end);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/workspace/agree-list.jsp");
		return model;
	}

	@Description(value = "검토함 조회 함수")
	@ResponseBody
	@PostMapping(value = "/agree")
	public Map<String, Object> agree(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.agree(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/agree", "검토함 조회 함수");
		}
		return result;
	}

	@Description(value = "결재함 리스트 페이지")
	@GetMapping(value = "/approval")
	public ModelAndView approval() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);

		model.addObject("before", before);
		model.addObject("end", end);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/workspace/approval-list.jsp");
		return model;
	}

	@Description(value = "결재함 조회 함수")
	@ResponseBody
	@PostMapping(value = "/approval")
	public Map<String, Object> approval(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.approval(params);
			System.out.println("poid는 대체 어디에 있는 건데? 왜 없는건데? : " + result);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/approval", "결재함 조회 함수");
		}
		return result;
	}

	@Description(value = "수신함 페이지")
	@GetMapping(value = "/receive")
	public ModelAndView receive() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);

		model.addObject("before", before);
		model.addObject("end", end);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/workspace/receive-list.jsp");
		return model;
	}

	@Description(value = "수신함 조회 함수")
	@ResponseBody
	@PostMapping(value = "/receive")
	public Map<String, Object> receive(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.receive(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/receive", "수신함 조회 함수");
		}
		return result;
	}

	@Description(value = "진행함 리스트 페이지")
	@GetMapping(value = "/progress")
	public ModelAndView progress() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/workspace/progress-list.jsp");
		return model;
	}

	@Description(value = "진행함 조회 함수")
	@PostMapping(value = "/progress")
	@ResponseBody
	public Map<String, Object> progress(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.progress(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/progress", "결재함 조회 함수");
		}
		return result;
	}

	@Description(value = "완료함 리스트 페이지")
	@GetMapping(value = "/complete")
	public ModelAndView complete() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);
		model.addObject("before", before);
		model.addObject("end", end);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/workspace/complete-list.jsp");
		return model;
	}

	@Description(value = "완료함 조회 함수")
	@ResponseBody
	@PostMapping(value = "/complete")
	public Map<String, Object> complete(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.complete(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/complete", "완료함 조회 함수");
		}
		return result;
	}

	@Description(value = "반려함 페이지")
	@GetMapping(value = "/reject")
	public ModelAndView reject() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/workspace/reject-list.jsp");
		return model;
	}

	@Description(value = "반려함 조회 함수")
	@ResponseBody
	@PostMapping(value = "/reject")
	public Map<String, Object> reject(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.reject(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/reject", "반려함 조회 함수");
		}
		return result;
	}

	@Description(value = "결재선 지정 팝업 페이지")
	@GetMapping(value = "/popup")
	public ModelAndView popup() throws Exception {
		ModelAndView model = new ModelAndView();
		Department department = OrgHelper.manager.getRoot();
		model.addObject("oid", department.getPersistInfo().getObjectIdentifier().getStringValue());
		model.setViewName("popup:/workspace/register-popup");
		return model;
	}

	@Description(value = "결재 정보 보기")
	@GetMapping(value = "/lineView")
	public ModelAndView lineView(@RequestParam String oid, @RequestParam String columnType, @RequestParam String poid)
			throws Exception {
		ModelAndView model = new ModelAndView();
		ApprovalLine approvalLine = (ApprovalLine) CommonUtils.getObject(oid);
		ApprovalLineDTO dto = new ApprovalLineDTO(approvalLine, columnType);
		Persistable per = (Persistable) CommonUtils.getObject(poid);
		model.addObject("per", per);
		model.addObject("dto", dto);
		model.addObject("oid", oid);
		model.setViewName("popup:/workspace/line-view");
		return model;
	}

	@Description(value = "결재 정보 보기")
	@GetMapping(value = "/masterView")
	public ModelAndView masterView(@RequestParam String oid, @RequestParam String columnType, @RequestParam String poid)
			throws Exception {
		ModelAndView model = new ModelAndView();
		ApprovalMaster master = (ApprovalMaster) CommonUtils.getObject(oid);
		ApprovalLineDTO dto = new ApprovalLineDTO(master, columnType);
		Persistable per = (Persistable) CommonUtils.getObject(poid);
		model.addObject("per", per);
		model.addObject("dto", dto);
		model.addObject("oid", oid);
		model.setViewName("popup:/workspace/master-view");
		return model;
	}

	@Description(value = "검토완료 함수")
	@ResponseBody
	@PostMapping(value = "/_agree")
	public Map<String, Object> _agree(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service._agree(params);
			int _agree = WorkspaceHelper.manager._agree();
			result.put("_agree", _agree);
			result.put("msg", AGREE_SUCCESS);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/_agree", "검토완료 함수");
		}
		return result;
	}

	@Description(value = "검토반려 함수")
	@ResponseBody
	@PostMapping(value = "/_unagree")
	public Map<String, Object> _unagree(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service._unagree(params);
			int _agree = WorkspaceHelper.manager._agree();
			result.put("_agree", _agree);
			result.put("msg", AGREE_REJECT);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/_unagree", "검토반려 함수");
		}
		return result;
	}

	@Description(value = "승인 함수")
	@ResponseBody
	@PostMapping(value = "/_approval")
	public Map<String, Object> _approval(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service._approval(params);
			int _approval = WorkspaceHelper.manager._approval();
			result.put("_approval", _approval);
			result.put("msg", APPROVAL_SUCCESS);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/_approval", "승인 함수");
		}
		return result;
	}

	@Description(value = "반려 함수")
	@ResponseBody
	@PostMapping(value = "/_reject")
	public Map<String, Object> _reject(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service._reject(params);
			int _reject = WorkspaceHelper.manager._reject();
			result.put("_reject", _reject);
			result.put("msg", REJECT_SUCCESS);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/_reject", "반려 함수");
		}
		return result;
	}

	@Description(value = "수신확인 함수")
	@ResponseBody
	@PostMapping(value = "/_receive")
	public Map<String, Object> _receive(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service._receive(params);
			int _receive = WorkspaceHelper.manager._receive();
			result.put("_receive", _receive);
			result.put("msg", RECEIVE_SUCCESS);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/_receive", "수신확인 함수");
		}
		return result;
	}

	@Description(value = "결재위임 함수")
	@ResponseBody
	@PostMapping(value = "/reassign")
	public Map<String, Object> reassign(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service.reassign(params);
			result.put("msg", "결재가 위임 되었습니다.");
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/reassign", "결재위임 함수");
		}
		return result;
	}

	@Description(value = "도면승인 일람표 다운로드")
	@GetMapping(value = "/print")
	public ResponseEntity<byte[]> print(@RequestParam String oid) throws Exception {
		ApprovalContract contract = (ApprovalContract) CommonUtils.getObject(oid);
		Workbook cover = WorkspaceHelper.manager.print(oid);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		cover.write(byteArrayOutputStream);

		byte[] bytes = byteArrayOutputStream.toByteArray();
		String name = URLEncoder.encode(contract.getName(), "UTF-8").replaceAll("\\+", "%20");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentLength(bytes.length);
		headers.setContentDispositionFormData("attachment", name + ".xlsx");

		return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
	}

	@Description(value = "개인결재선 조회 함수")
	@ResponseBody
	@PostMapping(value = "/loadLine")
	public Map<String, Object> loadLine(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.loadLine(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/loadLine", "개인결재선 조회 함수");
		}
		return result;
	}

	@Description(value = "개인결재선 저장 함수")
	@ResponseBody
	@PostMapping(value = "/save")
	public Map<String, Object> save(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			result = WorkspaceHelper.manager.validate(params);
			if ((boolean) result.get("validate")) {
				result.put("result", FAIL);
				return result;
			}

			WorkspaceHelper.service.save(params);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/save", "개인결재선 저장 함수");
		}
		return result;
	}

	@Description(value = "개인결재선 삭제 함수")
	@ResponseBody
	@GetMapping(value = "/delete")
	public Map<String, Object> delete(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service.delete(oid);
			result.put("result", SUCCESS);
			result.put("msg", DELETE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/delete", "개인결재선 삭제 함수");
		}
		return result;
	}

	@Description(value = "개인결재선 즐겨찾기 저장 함수")
	@ResponseBody
	@PostMapping(value = "/favorite")
	public Map<String, Object> favorite(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service.favorite(params);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/favorite", "개인결재선 즐겨찾기 저장 함수");
		}
		return result;
	}

	@Description(value = "개인결재선 즐겨찾기 불러오는 함수")
	@ResponseBody
	@PostMapping(value = "/loadFavorite")
	public Map<String, Object> loadFavorite(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.loadFavorite(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/loadFavorite", "개인결재선 즐겨찾기 불러오는 함수");
		}
		return result;
	}

	@Description(value = "개인결재선 불러오는 함수")
	@ResponseBody
	@GetMapping(value = "/loadFavorite")
	public Map<String, Object> loadFavorite(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.loadFavorite(oid);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/loadFavorite", "개인결재선 불러오는 함수");
		}
		return result;
	}

	@Description(value = "결재 초기화 함수")
	@ResponseBody
	@PostMapping(value = "/_reset")
	public Map<String, Object> _reset(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkspaceHelper.service._reset(params);
			result.put("msg", "결재가 초기화 되었습니다.");
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/_reset", "결재 초기화 함수");
		}
		return result;
	}

	@Description(value = "contract 리스트 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getValueMap("CUSTOMER");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getValueMap("MAK");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		model.addObject("customers", customers);
		model.addObject("projectTypes", projectTypes);
		model.addObject("maks", maks);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.setViewName("/extcore/jsp/workspace/contract-list.jsp");
		return model;
	}

	@Description(value = "contract 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/list", "contract 조회 함수");
		}
		return result;
	}

	@Description(value = "contract 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSuperviosr = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSuperviosr", isSuperviosr);
		model.setViewName("popup:/workspace/contract-create");
		return model;
	}

	@Description(value = "도면 결재 함수")
	@PostMapping(value = "/create")
	@ResponseBody
	public Map<String, Object> create(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			EpmHelper.service.register(params);
			result.put("result", SUCCESS);
			result.put("msg", REGISTER_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/workspace/create", "도면 결재 함수");
		}
		return result;
	}

	@Description(value = "contract 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		ApprovalContract appCon = (ApprovalContract) CommonUtils.getObject(oid);
		ApprovalContractDTO dto = new ApprovalContractDTO(appCon);
		ApprovalMaster appMaster = WorkspaceHelper.manager.getMaster(appCon);

		ArrayList<Map<String, Object>> contractDataList = WorkspaceHelper.manager.contractData(appCon);

		ArrayList<Map<String, Object>> contractEPMList = new ArrayList<>();
		ArrayList<Map<String, Object>> contractNumberRuleList = new ArrayList<>();

		for (Map<String, Object> mm : contractDataList) {
			if (((String) mm.get("oid")).indexOf("EPMDocument") > -1) {
				contractEPMList.add(mm);
			} else if (((String) mm.get("oid")).indexOf("NumberRule") > -1) {
				contractNumberRuleList.add(mm);
			}
		}

		boolean isAdmin = CommonUtils.isAdmin();
		model.addObject("isAdmin", isAdmin);
		model.addObject("oid", oid);
		model.addObject("dto", dto);
		model.addObject("contract", appCon);
		model.addObject("contractEPMList", JSONArray.fromObject(contractEPMList));
		model.addObject("contractNumberRuleList", JSONArray.fromObject(contractNumberRuleList));
		model.addObject("appMaster", appMaster);// model.addObject("history", history);
		model.setViewName("popup:/workspace/contract-view");
		return model;
	}

	@Description(value = "도면 결재 페이지")
	@GetMapping(value = "/update")
	public ModelAndView update(@RequestParam String oid, @RequestParam(required = false) String mode) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSuperviosr = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		ApprovalContract contract = (ApprovalContract) CommonUtils.getObject(oid);

		ApprovalMaster appMaster = WorkspaceHelper.manager.getMaster(contract);
		ArrayList<Map<String, Object>> contractDataList = WorkspaceHelper.manager.contractData(contract);

		ArrayList<Map<String, Object>> contractEPMList = new ArrayList<>();
		ArrayList<Map<String, Object>> contractNumberRuleList = new ArrayList<>();

		for (Map<String, Object> mm : contractDataList) {
			if (((String) mm.get("oid")).indexOf("EPMDocument") > -1) {
				contractEPMList.add(mm);
			} else if (((String) mm.get("oid")).indexOf("NumberRule") > -1) {
				contractNumberRuleList.add(mm);
			}
		}
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSuperviosr", isSuperviosr);
		model.addObject("contract", contract);
		model.addObject("contractEPMList", JSONArray.fromObject(contractEPMList));
		model.addObject("contractNumberRuleList", JSONArray.fromObject(contractNumberRuleList));
		model.addObject("appMaster", appMaster);
		model.setViewName("/extcore/jsp/workspace/contract-update.jsp");
		model.setViewName("popup:/workspace/contract-update");
		return model;
	}

	@Description(value = "도면 결재 함수")
	@PostMapping(value = "/update")
	@ResponseBody
	public Map<String, Object> update(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			EpmHelper.service.update(params);
			result.put("result", SUCCESS);
			result.put("msg", MODIFY_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/workspace/update", "도면 결재 수정 함수");
		}
		return result;
	}

	@Description(value = "문서 삭제 함수")
	@ResponseBody
	@GetMapping(value = "/deleteContract")
	public Map<String, Object> deleteContract(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			EpmHelper.service.delete(oid);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/deleteContract", "일괄결재 삭제");
		}
		return result;
	}

	@Description(value = "도번 승인요청서 다운로드")
	@ResponseBody
	@GetMapping(value = "/kekNumber")
	public Map<String, Object> kekNumber(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkspaceHelper.manager.kekNumber(oid);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workspace/kekNumber", "도번 승인요청서 다운로드");
		}
		return result;
	}
}
