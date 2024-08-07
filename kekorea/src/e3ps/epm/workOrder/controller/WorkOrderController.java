package e3ps.epm.workOrder.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Description;
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
import e3ps.common.util.StringUtils;
import e3ps.epm.service.KeDrawingHelper;
import e3ps.epm.workOrder.WorkOrder;
import e3ps.epm.workOrder.dto.WorkOrderDTO;
import e3ps.epm.workOrder.service.WorkOrderHelper;
import e3ps.org.Department;
import e3ps.org.People;
import e3ps.project.Project;
import e3ps.project.task.Task;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/workOrder/**")
public class WorkOrderController extends BaseController {

	@Description(value = "도면일람표 삭제 함수")
	@ResponseBody
	@GetMapping(value = "/delete")
	public Map<String, Object> delete(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkOrderHelper.service.delete(oid);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workOrder/delete", "도면일람표 삭제 함수");
		}
		return result;
	}

	@Description(value = "도면일람표 리스트 페이지")
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
		model.setViewName("/extcore/jsp/epm/workOrder/workOrder-list.jsp");
		return model;
	}

	@Description(value = "도면일람표 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			System.out.println("START");
			result = WorkOrderHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workOrder/list", "도면일람표 조회 함수");
		}
		return result;
	}

	@Description(value = "도면일람표 등록 함수")
	@ResponseBody
	@PostMapping(value = "/create")
	public Map<String, Object> create(@RequestBody WorkOrderDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
			ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
			if (approvalRows.size() > 1) {
				result.put("result", FAIL);
				result.put("msg", "도면일람표의 결재자는 1명이사이 될 수 없습니다.");
				return result;
			}

			if (agreeRows.size() > 2) {
				result.put("result", FAIL);
				result.put("msg", "도면일람표의 검토자는 2명이상이 될 수 없습니다.");
				return result;
			}

			WorkOrderHelper.service.create(dto);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workOrder/create", "도면일람표 등록 함수");
		}
		return result;
	}

	@Description(value = "도면일람표 생성 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create(@RequestParam(required = false) String poid, @RequestParam(required = false) String toid)
			throws Exception {
		ModelAndView model = new ModelAndView();
		People people = CommonUtils.sessionPeople();
		Department department = people.getDepartment();
		String workOrderType = "";
		if (department.getCode().equals("MACHINE")) {
			workOrderType = "기계";
		} else if (department.getCode().equals("ELEC")) {
			workOrderType = "전기";
		}
		if (!StringUtils.isNull(poid) && !StringUtils.isNull(toid)) {
			Project project = (Project) CommonUtils.getObject(poid);
			ArrayList<Map<String, String>> data = new ArrayList<>();
			Map<String, String> map = new HashMap<>();
			map.put("oid", project.getPersistInfo().getObjectIdentifier().getStringValue());
			map.put("projectType_name", project.getProjectType().getName());
			map.put("customer_name", project.getCustomer() != null ? project.getCustomer().getName() : "");
			map.put("mak_name", project.getMak() != null ? project.getMak().getName() : "");
			map.put("detail_name", project.getDetail() != null ? project.getDetail().getName() : "");
			map.put("install_name", project.getInstall() != null ? project.getInstall().getName() : "");
			map.put("kekNumber", project.getKekNumber());
			map.put("keNumber", project.getKeNumber());
			map.put("description", project.getDescription());
			data.add(map); // 기본 선택한 작번

			Task task = (Task) CommonUtils.getObject(toid);
			model.addObject("location", "/Default/프로젝트/" + task.getName());
			model.addObject("toid", toid);
			model.addObject("poid", poid);
			model.addObject("data", JSONArray.fromObject(data));
		}
		model.addObject("workOrderType", workOrderType);
		model.setViewName("popup:/epm/workOrder/workOrder-create");
		return model;
	}

	@Description(value = "도면일람표에 첨부할 도면들 찾아오는(KEK, KE) 함수")
	@ResponseBody
	@GetMapping(value = "/getData")
	public Map<String, Object> getData(@RequestParam String number) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkOrderHelper.manager.getData(number);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workOrder/getData", "도면일람표에 첨부할 도면들 찾아오는(KEK, KE) 함수");
		}
		return result;
	}

	@Description(value = "도면일람표 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		WorkOrder workOrder = (WorkOrder) CommonUtils.getObject(oid);
		WorkOrder latest = WorkOrderHelper.manager.getLatest(workOrder);
		JSONArray history = WorkOrderHelper.manager.history(workOrder);
		WorkOrderDTO dto = new WorkOrderDTO(workOrder);
		JSONArray list = KeDrawingHelper.manager.getData(workOrder);
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = CommonUtils.sessionUser();
		model.addObject("latestVersion", latest.getVersion());
		model.addObject("loid", latest.getPersistInfo().getObjectIdentifier().getStringValue());
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("history", history);
		model.addObject("list", list);
		model.addObject("dto", dto);
		model.setViewName("popup:/epm/workOrder/workOrder-view");
		return model;
	}

	@Description(value = "도면일람표 비교 페이지 공통 함수")
	@GetMapping(value = "/compare")
	public ModelAndView compare(@RequestParam String oid, @RequestParam String compareArr) throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		Project p1 = (Project) CommonUtils.getObject(oid);
		String[] compareOids = compareArr.split(",");
		ArrayList<Project> destList = new ArrayList<>(compareOids.length);
		for (String _oid : compareOids) {
			Project project = (Project) CommonUtils.getObject(_oid);
			destList.add(project);
		}

		ArrayList<Map<String, Object>> data = WorkOrderHelper.manager.compare(p1, destList);
		model.addObject("sessionUser", sessionUser);
		model.addObject("p1", p1);
		model.addObject("oid", oid);
		model.addObject("destList", destList);
		model.addObject("data", JSONArray.fromObject(data));
		model.setViewName("popup:/epm/workOrder/workOrder-compare");
		return model;
	}

	@Description(value = "도면일람표 수정 & 개정 페이지")
	@GetMapping(value = "/update")
	public ModelAndView update(@RequestParam String oid, @RequestParam String mode) throws Exception {
		ModelAndView model = new ModelAndView();
		WorkOrder workOrder = (WorkOrder) CommonUtils.getObject(oid);
		WorkOrderDTO dto = new WorkOrderDTO(workOrder);
		JSONArray list = KeDrawingHelper.manager.getData(workOrder);
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = CommonUtils.sessionUser();
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("list", list);
		model.addObject("dto", dto);
		model.addObject("mode", mode);
		model.setViewName("popup:/epm/workOrder/workOrder-update");
		return model;
	}

	@Description(value = "도면일람표 수정 함수")
	@PostMapping(value = "/modify")
	@ResponseBody
	public Map<String, Object> modify(@RequestBody WorkOrderDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
			ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
			if (approvalRows.size() > 1) {
				result.put("result", FAIL);
				result.put("msg", "도면일람표의 결재자는 1명이사이 될 수 없습니다.");
				return result;
			}

			if (agreeRows.size() > 2) {
				result.put("result", FAIL);
				result.put("msg", "도면일람표의 검토자는 2명이상이 될 수 없습니다.");
				return result;
			}
			WorkOrderHelper.service.modify(dto);
			result.put("result", SUCCESS);
			result.put("msg", MODIFY_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/workOrder/modify", "도면일람표 수정 함수");
		}
		return result;
	}

	@Description(value = "도면일람표 개정 함수")
	@PostMapping(value = "/revise")
	@ResponseBody
	public Map<String, Object> revise(@RequestBody WorkOrderDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
			ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
			if (approvalRows.size() > 1) {
				result.put("result", FAIL);
				result.put("msg", "도면일람표의 결재자는 1명이상이 될 수 없습니다.");
				return result;
			}

			if (agreeRows.size() > 2) {
				result.put("result", FAIL);
				result.put("msg", "도면일람표의 검토자는 2명이상이 될 수 없습니다.");
				return result;
			}
			WorkOrderHelper.service.revise(dto);
			result.put("result", SUCCESS);
			result.put("msg", REVISE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/workOrder/revise", "도면일람표 개정 함수");
		}
		return result;
	}

	@Description(value = "산출물 도면일람표 연결 페이지")
	@GetMapping(value = "/connect")
	public ModelAndView connect(@RequestParam String poid, @RequestParam String toid) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getValueMap("CUSTOMER");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getValueMap("MAK");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("customers", customers);
		model.addObject("projectTypes", projectTypes);
		model.addObject("maks", maks);
		model.addObject("toid", toid);
		model.addObject("poid", poid);
		model.setViewName("popup:/epm/workOrder/workOrder-connect");
		return model;
	}

	@Description(value = "도면일람표 태스크 연결 함수")
	@PostMapping(value = "/connect")
	@ResponseBody
	public Map<String, Object> connect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = WorkOrderHelper.service.connect(params);

			if ((boolean) result.get("exist")) {
				result.put("result", FAIL);
				result.put("msg", "이미 해당 태스크와 연결된 도면일람표 입니다.");
				return result;
			}

			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/workOrder/connect", "도면일람표 태스크 연결 함수");
		}
		return result;
	}

	@Description(value = "도면일람표 태스트 연결 제거 함수")
	@ResponseBody
	@PostMapping(value = "/disconnect")
	public Map<String, Object> disconnect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			WorkOrderHelper.service.disconnect(params);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}
}
