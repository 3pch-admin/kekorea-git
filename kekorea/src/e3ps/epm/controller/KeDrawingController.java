package e3ps.epm.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import e3ps.common.util.DateUtils;
import e3ps.epm.KeDrawing;
import e3ps.epm.dto.KeDrawingDTO;
import e3ps.epm.service.KeDrawingHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;

@Controller
@RequestMapping(value = "/keDrawing/**")
public class KeDrawingController extends BaseController {

	@Description(value = "KE 도면 리스트 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		WTUser sessionUser = CommonUtils.sessionUser();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		Timestamp time = new Timestamp(new Date().getTime());
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -2);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);

		
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("time", time);
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("before", before);
		model.addObject("end", end);
		model.setViewName("/extcore/jsp/epm/keDrawing-list.jsp");
		return model;
	}

	@Description(value = "KE 도면 리스트 가져 오는 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = KeDrawingHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "KE 등록 함수")
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

			ArrayList<KeDrawingDTO> addRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> add : addRows) {
				KeDrawingDTO dto = mapper.convertValue(add, KeDrawingDTO.class);
				addRow.add(dto);
			}

			ArrayList<KeDrawingDTO> editRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> edit : editRows) {
				KeDrawingDTO dto = mapper.convertValue(edit, KeDrawingDTO.class);
				editRow.add(dto);
			}

			ArrayList<KeDrawingDTO> removeRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> remove : removeRows) {
				KeDrawingDTO dto = mapper.convertValue(remove, KeDrawingDTO.class);
				removeRow.add(dto);
			}
			HashMap<String, List<KeDrawingDTO>> dataMap = new HashMap<>();
			dataMap.put("addRows", addRow); // 추가행
			dataMap.put("editRows", editRow); // 수정행
			dataMap.put("removeRows", removeRow); // 삭제행

			result = KeDrawingHelper.manager.isWorkOrder(removeRow);
			if ((boolean) result.get("workOrder")) {
				result.put("result", FAIL);
				return result;
			}

//			result = KeDrawingHelper.manager.numberValidate(addRow, editRow);
//			if ((boolean) result.get("isExist")) {
//				result.put("result", FAIL);
//				return result;
//			}
			
			result = KeDrawingHelper.manager.isValid(addRow, editRow);
			// true 중복있음
			if ((boolean) result.get("isExist")) {
				result.put("result", FAIL);
				return result;
			}

			KeDrawingHelper.service.save(dataMap);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "KE 도면 개정 페이지")
	@GetMapping(value = "/revise")
	public ModelAndView revise() throws Exception {
		ModelAndView model = new ModelAndView();
		model.setViewName("popup:/epm/keDrawing-revise");
		return model;
	}

	@Description(value = "KE 도면 개정")
	@ResponseBody
	@PostMapping(value = "/revise")
	public Map<String, Object> revise(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> addRows = params.get("addRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<KeDrawingDTO> addRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> add : addRows) {
				KeDrawingDTO dto = mapper.convertValue(add, KeDrawingDTO.class);
				addRow.add(dto);
			}

			HashMap<String, List<KeDrawingDTO>> dataMap = new HashMap<>();
			dataMap.put("addRows", addRow); // 추가행

			for (KeDrawingDTO dto : addRow) {
				KeDrawing keDrawing = (KeDrawing) CommonUtils.getObject(dto.getOid()); // 원본
				String keNumber = dto.getKeNumber(); // 변경 되는 값
				if (!keNumber.equals(keDrawing.getMaster().getKeNumber())) {
					result.put("result", FAIL);
					result.put("msg", "개정전 후의 도번이 일치 하지 않습니다.\n데이터를 확인 해주세요.");
					return result;
				}
			}

			KeDrawingHelper.service.revise(dataMap);
			result.put("msg", REVISE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "KE 도면 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		KeDrawing keDrawing = (KeDrawing) CommonUtils.getObject(oid);
		KeDrawingDTO dto = new KeDrawingDTO(keDrawing);
		JSONArray history = KeDrawingHelper.manager.history(keDrawing.getMaster());
		KeDrawing latest = KeDrawingHelper.manager.getLatest(keDrawing);
		model.addObject("latestVersion", latest.getVersion());
		model.addObject("loid", latest.getPersistInfo().getObjectIdentifier().getStringValue());
		model.addObject("history", history);
		model.addObject("dto", dto);
		model.setViewName("popup:/epm/keDrawing-view");
		return model;
	}

	@Description(value = "도면일람표 도면 뷰 페이지 생성 페이지")
	@GetMapping(value = "/viewByNumberAndRev")
	public ModelAndView viewByNumberAndRev(@RequestParam String number, @RequestParam String rev) throws Exception {
		ModelAndView model = new ModelAndView();
		KeDrawing keDrawing = KeDrawingHelper.manager.getKeDrawingByNumberAndRev(number, rev);
		KeDrawingDTO dto = new KeDrawingDTO(keDrawing);
		JSONArray history = KeDrawingHelper.manager.history(keDrawing.getMaster());
		KeDrawing latest = KeDrawingHelper.manager.getLatest(keDrawing);
		model.addObject("latestVersion", latest.getVersion());
		model.addObject("loid", latest.getPersistInfo().getObjectIdentifier().getStringValue());
		model.addObject("history", history);
		model.addObject("dto", dto);
		model.setViewName("popup:/epm/keDrawing-view");
		return model;
	}
	
	@Description(value = "KE 도면 등록 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create() throws Exception {
		ModelAndView model = new ModelAndView();
		model.setViewName("popup:/epm/keDrawing-create");
		return model;
	}
	
	
	@Description(value = "KE 도면 수정 페이지")
	@GetMapping(value = "/modify")
	public ModelAndView modify() throws Exception {
		ModelAndView model = new ModelAndView();
		model.setViewName("popup:/epm/keDrawing-modify");
		return model;
	}

	@Description(value = "KE 도면 수정")
	@ResponseBody
	@PostMapping(value = "/modify")
	public Map<String, Object> modify(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params) throws Exception {
		ArrayList<LinkedHashMap<String, Object>> addRows = params.get("addRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<KeDrawingDTO> addRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> add : addRows) {
				KeDrawingDTO dto = mapper.convertValue(add, KeDrawingDTO.class);
				addRow.add(dto);
			}

			HashMap<String, List<KeDrawingDTO>> dataMap = new HashMap<>();
			dataMap.put("addRows", addRow); // 추가행

			for (KeDrawingDTO dto : addRow) {
				KeDrawing keDrawing = (KeDrawing) CommonUtils.getObject(dto.getOid()); // 원본
				String keNumber = dto.getKeNumber(); // 변경 되는 값
				if (!keNumber.equals(keDrawing.getMaster().getKeNumber())) {
					result.put("result", FAIL);
					result.put("msg", "수정전 후의 도번이 일치 하지 않습니다.\n데이터를 확인 해주세요.");
					return result;
				}
			}

			KeDrawingHelper.service.modify(dataMap);
			result.put("msg", MODIFY_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}
	

}
