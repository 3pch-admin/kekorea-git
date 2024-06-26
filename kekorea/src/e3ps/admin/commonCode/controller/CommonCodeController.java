package e3ps.admin.commonCode.controller;

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

import e3ps.admin.commonCode.CommonCodeType;
import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.common.controller.BaseController;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;

@Controller
@RequestMapping(value = "/commonCode/**")
public class CommonCodeController extends BaseController {

	@Description(value = "코드 관리 리스트 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		CommonCodeType[] codeTypes = CommonCodeType.getCommonCodeTypeSet();
		JSONArray jsonList = CommonCodeHelper.manager.parseJson();
		model.addObject("jsonList", jsonList);
		model.addObject("codeTypes", codeTypes);
		model.setViewName("/extcore/jsp/admin/commonCode/commonCode-list.jsp");
		return model;
	}

	@Description(value = "코드 관리 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = CommonCodeHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/commonCode/list", "코드 관리 조회 함수");
		}
		return result;
	}

	@Description(value = "코드 관리 등록 함수")
	@ResponseBody
	@PostMapping(value = "/create")
	public Map<String, Object> create(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			CommonCodeHelper.service.create(params);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/commonCode/create", "코드 관리 등록 함수");
		}
		return result;
	}

	@Description(value = "코드의 자식 코드 가져오는 함수")
	@ResponseBody
	@GetMapping(value = "/getChildrens")
	public Map<String, Object> getChildrens(@RequestParam String parentCode, @RequestParam String codeType)
			throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ArrayList<Map<String, Object>> childrens = CommonCodeHelper.manager.getChildrens(parentCode, codeType);
			result.put("list", childrens);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/commonCode/getChildrens", "코드의 자식 코드 가져오는 함수");
		}
		return result;
	}

	@Description(value = "코드의 자식 코드 가져오는 함수 AXISJ")
	@ResponseBody
	@PostMapping(value = "/getChildrens")
	public Map<String, Object> getChildrens(@RequestParam String parentOid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ArrayList<Map<String, String>> childrens = CommonCodeHelper.manager.getChildrens(parentOid);
			result.put("list", childrens);
			result.put("result", "ok"); // AXISJ 전용 성공 값
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/commonCode/getChildrens", "코드의 자식 코드 가져오는 함수 AXISJ");
		}
		return result;
	}

	@Description(value = "코드타입 트리 구조 가져오는 함수")
	@PostMapping(value = "/loadTree")
	@ResponseBody
	public Map<String, Object> loadTree(@RequestBody Map<String, String> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			JSONArray list = CommonCodeHelper.manager.loadTree(params);
			result.put("list", list);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/commonCode/loadTree", "코드타입 트리 구조 가져오는 함수");
		}
		return result;
	}

	@Description(value = "상위 코드 검색 함수")
	@ResponseBody
	@PostMapping(value = "/remoter")
	public Map<String, Object> remoter(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ArrayList<Map<String, Object>> list = CommonCodeHelper.manager.remoter(params);
			result.put("list", list);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/commonCode/remoter", "상위 코드 검색 함수");
		}
		return result;
	}
}
