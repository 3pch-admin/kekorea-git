package e3ps.part.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.ContentUtils;
import e3ps.part.KePart;
import e3ps.part.KePartMaster;
import e3ps.part.dto.KePartDTO;
import e3ps.part.service.KePartHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;

@Controller
@RequestMapping(value = "/kePart/**")
public class KePartController extends BaseController {

	@Description(value = "KE 부품 조회 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = CommonUtils.sessionUser();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/part/kePart-list.jsp");
		return model;
	}

	@Description(value = "KE 부품 조회")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = KePartHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", false);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "KE 부품 등록 함수")
	@ResponseBody
	@PostMapping(value = "/save")
	public Map<String, Object> save(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> addRows = params.get("addRows");
		ArrayList<LinkedHashMap<String, Object>> editRows = params.get("editRows");
		ArrayList<LinkedHashMap<String, Object>> removeRows = params.get("removeRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<KePartDTO> addRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> add : addRows) {
				KePartDTO dto = mapper.convertValue(add, KePartDTO.class);
				addRow.add(dto);
			}

			ArrayList<KePartDTO> editRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> edit : editRows) {
				KePartDTO dto = mapper.convertValue(edit, KePartDTO.class);
				editRow.add(dto);
			}

			ArrayList<KePartDTO> removeRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> remove : removeRows) {
				KePartDTO dto = mapper.convertValue(remove, KePartDTO.class);
				removeRow.add(dto);
			}
			HashMap<String, List<KePartDTO>> dataMap = new HashMap<>();
			dataMap.put("addRows", addRow);
			dataMap.put("editRows", editRow);
			dataMap.put("removeRows", removeRow);

			result = KePartHelper.manager.isTBOM(removeRow);
			if ((boolean) result.get("tbom")) {
				result.put("result", FAIL);
				return result;
			}

			result = KePartHelper.manager.isValid(addRow, editRow);
			if ((boolean) result.get("isExist")) {
				result.put("result", FAIL);
				return result;
			}

			KePartHelper.service.save(dataMap);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "KE 부품 개정 페이지")
	@GetMapping(value = "/revise")
	public ModelAndView revise() throws Exception {
		ModelAndView model = new ModelAndView();
		model.setViewName("popup:/part/kePart-revise");
		return model;
	}

	@Description(value = "KE 부품 개정")
	@ResponseBody
	@PostMapping(value = "/revise")
	public Map<String, Object> revise(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> addRows = params.get("addRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<KePartDTO> addRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> add : addRows) {
				KePartDTO dto = mapper.convertValue(add, KePartDTO.class);
				addRow.add(dto);
			}

			HashMap<String, List<KePartDTO>> dataMap = new HashMap<>();
			dataMap.put("addRows", addRow);

			for (KePartDTO dto : addRow) {
				KePart kePart = (KePart) CommonUtils.getObject(dto.getOid());
				String keNumber = dto.getKeNumber();
				if (!keNumber.equals(kePart.getMaster().getKeNumber())) {
					result.put("result", FAIL);
					result.put("msg", "개정전 후의 품번이 일치 하지 않습니다.\n데이터를 확인 해주세요.");
					return result;
				}
			}

			KePartHelper.service.revise(dataMap);
			result.put("msg", REVISE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}
	
	@Description(value = "KE 부품 수정 페이지")
	@GetMapping(value = "/modify")
	public ModelAndView modify() throws Exception {
		ModelAndView model = new ModelAndView();
		model.setViewName("popup:/part/kePart-modify");
		return model;
	}
	
	@Description(value = "KE 부품 수정")
	@ResponseBody
	@PostMapping(value = "/modify")
	public Map<String, Object> modify(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> addRows = params.get("addRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<KePartDTO> addRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> add : addRows) {
				KePartDTO dto = mapper.convertValue(add, KePartDTO.class);
				addRow.add(dto);
			}

			HashMap<String, List<KePartDTO>> dataMap = new HashMap<>();
			dataMap.put("addRows", addRow);

			for (KePartDTO dto : addRow) {
				KePart kePart = (KePart) CommonUtils.getObject(dto.getOid());
				String keNumber = dto.getKeNumber();
				if (!keNumber.equals(kePart.getMaster().getKeNumber())) {
					result.put("result", FAIL);
					result.put("msg", "수정전 후의 품번이 일치 하지 않습니다.\n데이터를 확인 해주세요.");
					return result;
				}
			}

			KePartHelper.service.modify(dataMap);
			result.put("msg", MODIFY_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}
	
	

	@Description(value = "KE 부품 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		KePart kePart = (KePart) CommonUtils.getObject(oid);
		KePartDTO dto = new KePartDTO(kePart);
		Map<String, Object> primary = ContentUtils.getPrimary(dto.getOid());
		JSONArray list = KePartHelper.manager.history(kePart.getMaster());
		model.addObject("list", list);
		model.addObject("primarys", primary);
		model.addObject("dto", dto);
		model.setViewName("popup:/part/kePart-view");
		return model;
	}

	@Description(value = "KE 부품 이력정보 페이지")
	@GetMapping(value = "/history")
	public ModelAndView history(@RequestParam String moid) throws Exception {
		ModelAndView model = new ModelAndView();
		KePartMaster master = (KePartMaster) CommonUtils.getObject(moid);
		JSONArray list = KePartHelper.manager.history(master);
		model.addObject("list", list);
		model.setViewName("popup:/part/kePart-history");
		return model;
	}

	@Description(value = "KE 부품 등록 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create() throws Exception {
		ModelAndView model = new ModelAndView();
		model.setViewName("popup:/part/kePart-create");
		return model;
	}
}
