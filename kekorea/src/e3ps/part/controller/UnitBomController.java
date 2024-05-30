package e3ps.part.controller;

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

import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.part.UnitBom;
import e3ps.part.dto.UnitBomDTO;
import e3ps.part.service.UnitBomHelper;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/unit/**")
public class UnitBomController extends BaseController {

	@Description(value = "UNIT BOM 조회 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.setViewName("/extcore/jsp/bom/unit/unit-bom-list.jsp");
		return model;
	}

	@Description(value = "UNIT BOM 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = UnitBomHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/unit/list", "UNIT BOM 조회 함수");
		}
		return result;
	}

	@Description(value = "UNIT BOM 등록")
	@GetMapping(value = "/create")
	public ModelAndView create() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("popup:/bom/unit/unit-bom-create");
		return model;
	}

	@Description(value = "UNIT BOM 등록 함수")
	@ResponseBody
	@PostMapping(value = "/create")
	public Map<String, Object> create(@RequestBody UnitBomDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			UnitBomHelper.service.create(dto);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/unit/create", "UNIT BOM 등록 함수");
		}
		return result;
	}

	@Description(value = "UNIT BOM 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		UnitBom unitBom = (UnitBom) CommonUtils.getObject(oid);
		UnitBomDTO dto = new UnitBomDTO(unitBom);
		boolean isAdmin = CommonUtils.isAdmin();
		model.addObject("dto", dto);
		model.addObject("isAdmin", isAdmin);
		model.setViewName("popup:/bom/unit/unit-bom-view");
		return model;
	}

	@Description(value = "UNIT BOM 뷰 트리 호출 함수")
	@ResponseBody
	@PostMapping(value = "/view")
	public Map<String, Object> view(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			JSONArray list = UnitBomHelper.manager.tree(params);
			result.put("list", list);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/unit/create", "UNIT BOM 뷰 트리 호출 함수");
		}
		return result;
	}
}
