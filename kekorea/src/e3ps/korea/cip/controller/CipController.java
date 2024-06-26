package e3ps.korea.cip.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.korea.cip.dto.CipDTO;
import e3ps.korea.cip.service.CipHelper;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/cip/**")
public class CipController extends BaseController {

	@Description(value = "CIP 조회 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		JSONArray maks = CommonCodeHelper.manager.parseJson("MAK");
		JSONArray customers = CommonCodeHelper.manager.parseJson("CUSTOMER");

		ArrayList<Map<String, String>> customer_list = CommonCodeHelper.manager.getValueMap("CUSTOMER");
		ArrayList<Map<String, String>> mak_list = CommonCodeHelper.manager.getValueMap("MAK");

		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();

		model.addObject("isSupervisor", isSupervisor);
		model.addObject("mak_list", mak_list);
		model.addObject("customer_list", customer_list);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("maks", maks);
		model.addObject("customers", customers);
		model.setViewName("/extcore/jsp/korea/cip/cip-list.jsp");
		return model;
	}

	@Description(value = "CIP 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = CipHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/cip/list", "CIP 조회 함수");
		}
		return result;
	}

	@Description(value = "CIP 그리드 저장 함수")
	@PostMapping(value = "/save")
	@ResponseBody
	public Map<String, Object> save(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> addRows = params.get("addRows");
		ArrayList<LinkedHashMap<String, Object>> editRows = params.get("editRows");
		ArrayList<LinkedHashMap<String, Object>> removeRows = params.get("removeRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<CipDTO> addRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> add : addRows) {
				CipDTO dto = mapper.convertValue(add, CipDTO.class);
				addRow.add(dto);
			}

			ArrayList<CipDTO> editRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> edit : editRows) {
				CipDTO dto = mapper.convertValue(edit, CipDTO.class);
				editRow.add(dto);
			}

			ArrayList<CipDTO> removeRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> remove : removeRows) {
				CipDTO dto = mapper.convertValue(remove, CipDTO.class);
				removeRow.add(dto);
			}

			HashMap<String, List<CipDTO>> dataMap = new HashMap<>();
			dataMap.put("addRows", addRow); // 삭제행
			dataMap.put("editRows", editRow); // 삭제행
			dataMap.put("removeRows", removeRow); // 삭제행

			CipHelper.service.save(dataMap);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/cip/save", "CIP 그리드 저장 함수");
		}
		return result;
	}
}
