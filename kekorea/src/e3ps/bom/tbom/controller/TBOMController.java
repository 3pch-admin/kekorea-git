package e3ps.bom.tbom.controller;

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
import e3ps.bom.tbom.TBOMMaster;
import e3ps.bom.tbom.dto.TBOMDTO;
import e3ps.bom.tbom.service.TBOMHelper;
import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.StringUtils;
import e3ps.project.Project;
import e3ps.project.task.Task;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/tbom/**")
public class TBOMController extends BaseController {

	@Description(value = "T-BOM 조회 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getValueMap("CUSTOMER");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getValueMap("MAK");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		model.addObject("customers", customers);
		model.addObject("projectTypes", projectTypes);
		model.addObject("maks", maks);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.setViewName("/extcore/jsp/bom/tbom/tbom-list.jsp");
		return model;
	}

	@Description(value = "T-BOM 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = TBOMHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/list", "T-BOM 조회 함수");
		}
		return result;
	}

	@Description(value = "T-BOM 등록")
	@GetMapping(value = "/create")
	public ModelAndView create(@RequestParam(required = false) String poid, @RequestParam(required = false) String toid)
			throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
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
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("popup:/bom/tbom/tbom-create");
		return model;
	}

	@Description(value = "T-BOM 등록 함수")
	@ResponseBody
	@PostMapping(value = "/create")
	public Map<String, Object> create(@RequestBody TBOMDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			TBOMHelper.service.create(dto);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/create", "T-BOM 등록 함수");
		}
		return result;
	}

	@Description(value = "부품 번호로 (KE OR EPM) 찾아오는 함수")
	@ResponseBody
	@GetMapping(value = "/getData")
	public Map<String, Object> getData(@RequestParam String number) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = TBOMHelper.manager.getData(number);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/getData", "부품 번호로 (KE OR EPM) 찾아오는 함수");
		}
		return result;
	}

	@Description(value = "T-BOM 그리드 저장 함수")
	@ResponseBody
	@PostMapping(value = "/save")
	public Map<String, Object> save(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			TBOMHelper.service.save(params);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/save", "T-BOM 그리드 저장 함수");
		}
		return result;
	}

	@Description(value = "T-BOM 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		TBOMMaster master = (TBOMMaster) CommonUtils.getObject(oid);
		TBOMDTO dto = new TBOMDTO(master);
		TBOMMaster latest = TBOMHelper.manager.getLatest(master);
		JSONArray history = TBOMHelper.manager.history(master);
		JSONArray list = TBOMHelper.manager.jsonAuiProject(oid);
		JSONArray data = TBOMHelper.manager.getData(master);
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("latestVersion", latest.getVersion());
		model.addObject("loid", latest.getPersistInfo().getObjectIdentifier().getStringValue());
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("sessionUser", sessionUser);
		model.addObject("data", data);
		model.addObject("list", list);
		model.addObject("dto", dto);
		model.addObject("history", history);
		model.setViewName("popup:/bom/tbom/tbom-view");
		return model;
	}

	@Description(value = "T-BOM 태스크 연결 제거 함수")
	@ResponseBody
	@PostMapping(value = "/disconnect")
	public Map<String, Object> disconnect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			TBOMHelper.service.disconnect(params);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/disconnect", "T-BOM 태스크 연결 제거 함수");
		}
		return result;
	}

	@Description(value = "T-BOM 팝업 조회 페이지")
	@GetMapping(value = "/popup")
	public ModelAndView popup(@RequestParam String method, @RequestParam String multi) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("method", method);
		model.addObject("multi", Boolean.parseBoolean(multi));
		model.setViewName("popup:/bom/tbom/tbom-popup");
		return model;
	}

	@Description(value = "T-BOM 비교 페이지")
	@GetMapping(value = "/compare")
	public ModelAndView compare(@RequestParam String oid, @RequestParam String compareArr) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();

		String[] compareOids = compareArr.split(",");
		ArrayList<Project> destList = new ArrayList<>(compareOids.length);
		for (String _oid : compareOids) {
			Project project = (Project) CommonUtils.getObject(_oid);
			destList.add(project);
		}

		Project p1 = (Project) CommonUtils.getObject(oid);
		ArrayList<Map<String, Object>> data = TBOMHelper.manager.compare(p1, destList);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("p1", p1);
		model.addObject("destList", destList);
		model.addObject("oid", oid);
		model.addObject("compareArr", compareArr);
		model.addObject("data", JSONArray.fromObject(data));
		model.setViewName("popup:/bom/tbom/tbom-compare");
		return model;
	}

	@Description(value = "T-BOM 수정 & 개정 페이지")
	@GetMapping(value = "/update")
	public ModelAndView update(@RequestParam String oid, @RequestParam String mode) throws Exception {
		ModelAndView model = new ModelAndView();
		TBOMMaster master = (TBOMMaster) CommonUtils.getObject(oid);
		TBOMDTO dto = new TBOMDTO(master);
		JSONArray list = TBOMHelper.manager.jsonAuiProject(oid);
		JSONArray data = TBOMHelper.manager.getData(master);
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isAdmin", isAdmin);
		WTUser sessionUser = CommonUtils.sessionUser();
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("list", list);
		model.addObject("data", data);
		model.addObject("dto", dto);
		model.addObject("mode", mode);
		model.setViewName("popup:/bom/tbom/tbom-update");
		return model;
	}

	@Description(value = "T-BOM 수정 함수")
	@PostMapping(value = "/modify")
	@ResponseBody
	public Map<String, Object> modify(@RequestBody TBOMDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			TBOMHelper.service.modify(dto);
			result.put("result", SUCCESS);
			result.put("msg", MODIFY_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/modify", "T-BOM 수정 함수");
		}
		return result;
	}

	@Description(value = "T-BOM 삭제 함수")
	@ResponseBody
	@GetMapping(value = "/delete")
	public Map<String, Object> delete(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			TBOMHelper.service.delete(oid);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/delete", "T-BOM 삭제 함수");
		}
		return result;
	}

	@Description(value = "T-BOM 개정 함수")
	@PostMapping(value = "/revise")
	@ResponseBody
	public Map<String, Object> revise(@RequestBody TBOMDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			TBOMHelper.service.revise(dto);
			result.put("result", SUCCESS);
			result.put("msg", REVISE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/revise", "T-BOM 개정 함수");
		}
		return result;
	}

	@Description(value = "산출물 태스크에서 T-BOM 연결 페이지")
	@GetMapping(value = "/connect")
	public ModelAndView connect(@RequestParam String poid, @RequestParam String toid) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("sessionUser", sessionUser);
		model.addObject("toid", toid);
		model.addObject("poid", poid);
		model.setViewName("popup:/bom/tbom/tbom-connect");
		return model;
	}

	@Description(value = "산출물 태스크 T-BOM 연결 함수")
	@PostMapping(value = "/connect")
	@ResponseBody
	public Map<String, Object> connect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = TBOMHelper.service.connect(params);

			if ((boolean) result.get("exist")) {
				result.put("result", FAIL);
				result.put("msg", "이미 해당 태스크와 연결된 T-BOM 입니다.");
				return result;
			}

			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/tbom/connect", "산출물 태스크 T-BOM 연결 함수");
		}
		return result;
	}
}
