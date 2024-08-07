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
import e3ps.part.dto.PartDTO;
import e3ps.part.service.PartHelper;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/part/**")
public class PartController extends BaseController {

	@Description(value = "부품 조회 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSuperviosr = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSuperviosr", isSuperviosr);
		model.setViewName("/extcore/jsp/part/part-list.jsp");
		return model;
	}

	@Description(value = "부품 조회(라이브러리) 페이지")
	@GetMapping(value = "/library")
	public ModelAndView library() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSuperviosr = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSuperviosr", isSuperviosr);
		model.setViewName("/extcore/jsp/part/part-library-list.jsp");
		return model;
	}

	@Description(value = "부품 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/part/list", "부품 조회 함수");
		}
		return result;
	}

	@Description(value = "부품 일괄 등록(신규) 리스트 페이지")
	@GetMapping(value = "/bundle")
	public ModelAndView bundle() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.setViewName("/extcore/jsp/part/part-bundle.jsp");
		return model;
	}

	@Description(value = "부품 일괄 등록(신규) 함수")
	@PostMapping(value = "/bundle")
	@ResponseBody
	public Map<String, Object> bundle(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartHelper.service.bundle(params);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/part/bundle", "부품 일괄 등록(신규) 함수");
		}
		return result;
	}

	@Description(value = "부품 일괄 등록 IBA PART_CODE 검증 함수")
	@GetMapping(value = "/bundleValidatorNumber")
	@ResponseBody
	public Map<String, Object> bundleValidatorNumber(@RequestParam String number) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartHelper.manager.bundleValidatorNumber(number);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/part/bundleValidatorNumber", "부품 일괄 등록 IBA PART_CODE 검증 함수");
		}
		return result;
	}

	@Description(value = "부품 일괄 등록 WTPART Number 검증 함수")
	@GetMapping(value = "/bundleValidatorSpec")
	@ResponseBody
	public Map<String, Object> bundleValidatorSpec(@RequestParam String spec) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartHelper.manager.bundleValidatorSpec(spec);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/part/bundleValidatorSpec", "부품 일괄 등록 WTPART Number 검증 함수");
		}
		return result;
	}

	@Description(value = "부품 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		WTPart part = (WTPart) CommonUtils.getObject(oid);
		PartDTO dto = new PartDTO(part);
		JSONArray versionHistory = PartHelper.manager.versionHistory(part);
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("dto", dto);
		model.addObject("versionHistory", versionHistory);
		model.setViewName("popup:/part/part-view");
		return model;
	}

	@Description(value = "제작사양서 등록 리스트 페이지")
	@GetMapping(value = "/spec")
	public ModelAndView spec() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.setViewName("/extcore/jsp/part/part-spec.jsp");
		return model;
	}

	@Description(value = "제작사양서 등록 함수")
	@PostMapping(value = "/spec")
	@ResponseBody
	public Map<String, Object> spec(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartHelper.service.spec(params);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/part/spec", "제작사양서 등록 함수");
		}
		return result;
	}

	@Description(value = "부품 추가 페이지")
	@GetMapping(value = "/popup")
	public ModelAndView popup(@RequestParam String method, @RequestParam String multi) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("method", method);
		model.addObject("multi", Boolean.parseBoolean(multi));
		model.setViewName("popup:/part/part-popup");
		return model;
	}

	@Description(value = "부품 수정 페이지")
	@GetMapping(value = "/modify")
	public ModelAndView modify(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		WTPart part = (WTPart) CommonUtils.getObject(oid);
		PartDTO dto = new PartDTO(part);
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("dto", dto);
		model.setViewName("popup:/part/part-modify");
		return model;
	}

	@Description(value = "부품 수정 함수")
	@ResponseBody
	@PostMapping(value = "/modify")
	public Map<String, Object> modify(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartHelper.service.modify(params);
			result.put("msg", MODIFY_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/part/modify", "부품 수정 함수");
		}
		return result;
	}

	@Description(value = "부품 일괄 등록 리스트 페이지")
	@GetMapping(value = "/batch")
	public ModelAndView batch() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.setViewName("/extcore/jsp/part/part-batch.jsp");
		return model;
	}

	@Description(value = "부품 일괄 등록 함수")
	@PostMapping(value = "/batch")
	@ResponseBody
	public Map<String, Object> batch(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartHelper.service.batch(params);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/part/batch", "부품 일괄 등록 함수");
		}
		return result;
	}

	@Description(value = "부품 일괄 등록 IBA PART_CODE 검증후 값 가져오기")
	@GetMapping(value = "/batchValidatorNumber")
	@ResponseBody
	public Map<String, Object> batchValidatorNumber(@RequestParam String number) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = PartHelper.manager.batchValidatorNumber(number);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/part/batchValidatorNumber",
					"부품 일괄 등록 IBA PART_CODE 검증후 값 가져오기");
		}
		return result;
	}

	@Description(value = "코드 채번 페이지")
	@GetMapping(value = "/code")
	public ModelAndView code() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.setViewName("/extcore/jsp/part/part-code.jsp");
		return model;
	}

	@Description(value = "코드 채번 함수")
	@PostMapping(value = "/code")
	@ResponseBody
	public Map<String, Object> code(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartHelper.service.code(params);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/part/code", "코드 채번 함수");
		}
		return result;
	}

	@Description(value = "부품 정보 페이지")
	@GetMapping(value = "/viewByNumber")
	public ModelAndView viewByNumber(@RequestParam String partNo) throws Exception {
		ModelAndView model = new ModelAndView();

		WTPart part = PartHelper.manager.getPartByNumber(partNo);
		if (part != null) {
			PartDTO dto = new PartDTO(part);
			JSONArray versionHistory = PartHelper.manager.versionHistory(part);
			model.addObject("dto", dto);
			model.addObject("versionHistory", versionHistory);
			boolean isAdmin = CommonUtils.isAdmin();
			boolean isSupervisor = CommonUtils.isSupervisor();
			model.addObject("isAdmin", isAdmin);
			model.addObject("isSupervisor", isSupervisor);
			model.setViewName("popup:/part/part-view-number");
		} else {
			model.setViewName("popup:/part/part-error");
		}
		return model;
	}

	@Description(value = "부품 정보 페이지")
	@GetMapping(value = "/viewByPartNo")
	public ModelAndView viewByPartNo(@RequestParam String partNo) throws Exception {
		ModelAndView model = new ModelAndView();

		System.out.println("partNo=" + partNo);

		WTPart part = PartHelper.manager.getPartByNumber(partNo.trim());
		System.out.println("part=" + part);

		if (part == null) {
			model.setViewName("popup:/part/part-error");
		} else {

			PartDTO dto = new PartDTO(part);
			JSONArray versionHistory = PartHelper.manager.versionHistory(part);
			boolean isAdmin = CommonUtils.isAdmin();
			boolean isSupervisor = CommonUtils.isSupervisor();
			model.addObject("isAdmin", isAdmin);
			model.addObject("isSupervisor", isSupervisor);
			model.addObject("dto", dto);
			model.addObject("versionHistory", versionHistory);
			model.setViewName("popup:/part/part-view");
		}
		return model;
	}

	@Description(value = "부품 일괄 등록(PLM) 리스트 페이지")
	@GetMapping(value = "/plm")
	public ModelAndView plm() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.setViewName("/extcore/jsp/part/part-plm.jsp");
		return model;
	}

	@Description(value = "부품 일괄 등록(PLM) 함수")
	@PostMapping(value = "/plm")
	@ResponseBody
	public Map<String, Object> plm(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartHelper.service.plm(params);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/part/plm", "부품 일괄 등록(PLM) 함수");
		}
		return result;
	}

	@Description(value = "라이브러리 추가 페이지")
	@GetMapping(value = "/popup-library")
	public ModelAndView library(@RequestParam String method, @RequestParam String multi) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("method", method);
		model.addObject("multi", Boolean.parseBoolean(multi));
		model.addObject("state", "APPROVED");
		model.setViewName("popup:/part/part-library-popup");
		return model;
	}

	@Description(value = "코드 생성 페이지 등록 함수")
	@ResponseBody
	@PostMapping(value = "/create")
	public Map<String, Object> create(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			PartHelper.service.create(params);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/part/create", "코드 생성 페이지 등록 함수");
		}
		return result;
	}

}
