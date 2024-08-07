package e3ps.bom.partlist.controller;

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

import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.PartListMasterProjectLink;
import e3ps.bom.partlist.dto.PartListDTO;
import e3ps.bom.partlist.service.PartlistHelper;
import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.StringUtils;
import e3ps.org.Department;
import e3ps.org.People;
import e3ps.project.Project;
import e3ps.project.service.ProjectHelper;
import e3ps.project.task.Task;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/partlist/**")
public class PartlistController extends BaseController {

	@Description(value = "수배표 리스트 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.setViewName("/extcore/jsp/bom/partlist/partlist-list.jsp");
		return model;
	}

	@Description(value = "수배표 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartlistHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/partlist/list", "수배표 조회 함수");
		}
		return result;
	}

	@Description(value = "수배된 리스트 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid, @RequestParam(required = false) String tProg) throws Exception {
		ModelAndView model = new ModelAndView();
		Persistable per = (Persistable) CommonUtils.getObject(oid);

		PartListMaster master = null;
		PartListDTO dto = null;
		if (per instanceof PartListMasterProjectLink) {
			PartListMasterProjectLink link = (PartListMasterProjectLink) per;
			dto = new PartListDTO(link);
		} else if (per instanceof PartListMaster) {
			master = (PartListMaster) per;
			dto = new PartListDTO(master);
		}

		if (StringUtils.isNull(tProg)) {
			QueryResult qr = PersistenceHelper.manager.navigate(master, "project", PartListMasterProjectLink.class);
			String tt = master.getEngType();
			if (qr.hasMoreElements()) {
				Project project = (Project) qr.nextElement();
				Task parentTask = ProjectHelper.manager.getTaskByName(project, tt);
				Task t = ProjectHelper.manager.getTaskByParent(project, parentTask);
				tProg = String.valueOf(t.getProgress());
			}
		}

		JSONArray list = PartlistHelper.manager.getData(dto.getOid());
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSuperviosr = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSuperviosr", isSuperviosr);
		model.addObject("tProg", tProg);
		model.addObject("dto", dto);
		model.addObject("list", list);
		model.setViewName("popup:/bom/partlist/partlist-view");
		return model;
	}

	@Description(value = "수배표 비교 페이지 공통 함수")
	@GetMapping(value = "/compare")
	public ModelAndView compare(@RequestParam String oid, @RequestParam String compareArr, @RequestParam String invoke)
			throws Exception {
		ModelAndView model = new ModelAndView();
		Project p1 = (Project) CommonUtils.getObject(oid);
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSuperviosr = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		String[] compareOids = compareArr.split(",");
		ArrayList<Project> destList = new ArrayList<>(compareOids.length);
		for (String _oid : compareOids) {
			Project project = (Project) CommonUtils.getObject(_oid);
			destList.add(project);
		}
		ArrayList<Map<String, Object>> data = PartlistHelper.manager.compare(p1, destList, invoke);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSuperviosr", isSuperviosr);
		model.addObject("sessionUser", sessionUser);
		model.addObject("p1", p1);
		model.addObject("oid", oid);
		model.addObject("destList", destList);
		model.addObject("data", JSONArray.fromObject(data));
		model.setViewName("popup:/bom/partlist/partlist-compare");
		return model;
	}

	@Description(value = "수배표 팝업 조회 페이지")
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
		model.setViewName("popup:/bom/partlist/partlist-popup");
		return model;
	}

	@Description(value = "수배표 등록 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create(@RequestParam(required = false) String poid, @RequestParam(required = false) String toid)
			throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		People people = CommonUtils.sessionPeople();
		Department department = people.getDepartment();
		String engType = "";
		if (department.getCode().equals("MACHINE")) {
			engType = "기계";
		} else if (department.getCode().equals("ELEC")) {
			engType = "전기";
		}
		model.addObject("engType", engType);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		if (!StringUtils.isNull(poid)) {
			Project project = (Project) CommonUtils.getObject(poid);
			ArrayList<Map<String, String>> list = new ArrayList<>();
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
			list.add(map); // 기본 선택한 작번
			model.addObject("poid", poid);
			model.addObject("list", JSONArray.fromObject(list));
		}

		if (!StringUtils.isNull(toid)) {
			Task task = (Task) CommonUtils.getObject(toid);
			model.addObject("location", "/Default/프로젝트/" + task.getName());
			model.addObject("toid", toid);
		}
		model.setViewName("popup:/bom/partlist/partlist-create");
		return model;
	}

	@Description(value = "수배표 등록 함수")
	@ResponseBody
	@PostMapping(value = "/create")
	public Map<String, Object> create(@RequestBody PartListDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartlistHelper.service.create(dto);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/partlist/create", "수배표 등록 함수");
		}
		return result;
	}

	@Description(value = "수배표 수정 페이지")
	@GetMapping(value = "/modify")
	public ModelAndView modify(@RequestParam String oid, @RequestParam(required = false) String tProg)
			throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		PartListMaster master = (PartListMaster) CommonUtils.getObject(oid);
		PartListDTO dto = new PartListDTO(master);
		JSONArray list = PartlistHelper.manager.getData(dto.getOid());
		JSONArray data = PartlistHelper.manager.jsonAuiProject(dto.getOid());
		People people = CommonUtils.sessionPeople();
		Department department = people.getDepartment();
		String engType = "";
		if (department.getCode().equals("MACHINE")) {
			engType = "기계";
		} else if (department.getCode().equals("ELEC")) {
			engType = "전기";
		}

		if (StringUtils.isNull(tProg)) {
			QueryResult qr = PersistenceHelper.manager.navigate(master, "project", PartListMasterProjectLink.class);
			String tt = master.getEngType();
			if (qr.hasMoreElements()) {
				Project project = (Project) qr.nextElement();
				Task parentTask = ProjectHelper.manager.getTaskByName(project, tt);
				Task t = ProjectHelper.manager.getTaskByParent(project, parentTask);
				tProg = String.valueOf(t.getProgress());
			}
		}

		model.addObject("isSupervisor", isSupervisor);
		model.addObject("sessionUser", sessionUser);
		model.addObject("engType", engType);
		model.addObject("isAdmin", isAdmin);
		model.addObject("data", data);
		model.addObject("dto", dto);
		model.addObject("list", list);
		model.addObject("tProg", tProg);
		model.setViewName("popup:/bom/partlist/partlist-modify");
		return model;
	}

	@Description(value = "수배표 수정 함수")
	@ResponseBody
	@PostMapping(value = "/modify")
	public Map<String, Object> modify(@RequestBody PartListDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartlistHelper.service.modify(dto);
			result.put("msg", MODIFY_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/partlist/modify", "수배표 수정 함수");
		}
		return result;
	}

	@Description(value = "수배표 삭제 함수")
	@ResponseBody
	@GetMapping(value = "/delete")
	public Map<String, Object> delete(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartlistHelper.service.delete(oid);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/partlist/delete", "수배표 삭제 함수");
		}
		return result;
	}

	@Description(value = "수배표 태스크 연결 제거 함수")
	@ResponseBody
	@PostMapping(value = "/disconnect")
	public Map<String, Object> disconnect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartlistHelper.service.disconnect(params);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/partlist/disconnect", "수배표 태스크 연결 제거 함수");
		}
		return result;
	}

	@Description(value = "프로젝트에서 수배표 정보를 볼때")
	@GetMapping(value = "/moneyInfo")
	public ModelAndView moneyInfo(@RequestParam String oid, @RequestParam String invoke) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isSupervisor = CommonUtils.isSupervisor();
		JSONArray data = PartlistHelper.manager.partlistTab(oid, invoke);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("invoke", invoke);
		model.addObject("data", data);
		model.setViewName("popup:/bom/partlist/partlist-moneyInfo");
		return model;
	}

	@Description(value = "산출물 태스크에서 수배표 연결 페이지")
	@GetMapping(value = "/connect")
	public ModelAndView connect(@RequestParam String poid, @RequestParam String toid) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("toid", toid);
		model.addObject("poid", poid);
		model.setViewName("popup:/bom/partlist/partlist-connect");
		return model;
	}

	@Description(value = "수배표 태스크 연결 함수")
	@PostMapping(value = "/connect")
	@ResponseBody
	public Map<String, Object> connect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartlistHelper.service.connect(params);

			if ((boolean) result.get("exist")) {
				result.put("result", FAIL);
				result.put("msg", "이미 해당 태스크와 연결된 산출물 입니다.");
				return result;
			}

			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/partlist/connect", "수배표 태스크 연결 함수");
		}
		return result;
	}
}
