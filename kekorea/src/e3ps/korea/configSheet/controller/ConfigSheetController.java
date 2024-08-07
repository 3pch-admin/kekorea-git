package e3ps.korea.configSheet.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.admin.configSheetCode.ConfigSheetCode;
import e3ps.admin.configSheetCode.service.ConfigSheetCodeHelper;
import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.StringUtils;
import e3ps.korea.configSheet.ConfigSheet;
import e3ps.korea.configSheet.ConfigSheetProjectLink;
import e3ps.korea.configSheet.beans.ConfigSheetComparator;
import e3ps.korea.configSheet.beans.ConfigSheetDTO;
import e3ps.korea.configSheet.service.ConfigSheetHelper;
import e3ps.org.service.OrgHelper;
import e3ps.project.Project;
import e3ps.project.task.Task;
import e3ps.project.template.service.TemplateHelper;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/configSheet/**")
public class ConfigSheetController extends BaseController {

	@Description(value = "CONFIG SHEET 리스트 페이지")
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
		model.setViewName("/extcore/jsp/korea/configSheet/configSheet-list.jsp");
		return model;
	}

	@Description(value = "CONFIG SHEET 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = ConfigSheetHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/configSheet/list", "CONFIG SHEET 조회 함수");
		}
		return result;
	}

	@Description(value = "CONFIG SHEET 등록 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create(@RequestParam(required = false) String poid, @RequestParam(required = false) String toid)
			throws Exception {
		ModelAndView model = new ModelAndView();
		JSONArray categorys = ConfigSheetCodeHelper.manager.parseJson("CATEGORY");
		JSONArray baseData = ConfigSheetHelper.manager.loadBaseGridData();
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
			model.addObject("kekNumber", project.getKekNumber());
		}
		model.addObject("baseData", baseData);
		model.addObject("categorys", categorys);
		model.setViewName("popup:/korea/configSheet/configSheet-create");
		return model;
	}

	@Description(value = "CONFIG SHEET 등록 함수")
	@PostMapping(value = "/create")
	@ResponseBody
	public Map<String, Object> create(@RequestBody ConfigSheetDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ArrayList<Map<String, String>> addRows9 = dto.getAddRows9();

			boolean isValidate = false;
			for (Map<String, String> addRow9 : addRows9) {
				String oid = addRow9.get("oid");
				Project project = (Project) CommonUtils.getObject(oid);

				QueryResult rs = PersistenceHelper.manager.navigate(project, "configSheet",
						ConfigSheetProjectLink.class);

				if (rs.size() > 0) {
					isValidate = true;
					break;
				}
			}

			if (isValidate) {
				result.put("msg", "이미 해당 작번엔 CONFIG SHEET가 등록 되어있습니다.");
				result.put("result", FAIL);
				return result;
			}

			ConfigSheetHelper.service.create(dto);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/configSheet/create", "CONFIG SHEET 등록 함수");
		}
		return result;
	}

	@Description(value = "CONFIG SHEET 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		ConfigSheet configSheet = (ConfigSheet) CommonUtils.getObject(oid);
		ConfigSheet latest = ConfigSheetHelper.manager.getLatest(configSheet);
		JSONArray data = ConfigSheetHelper.manager.loadBaseGridData(oid, true);
		ConfigSheetDTO dto = new ConfigSheetDTO(configSheet);
		boolean isAdmin = CommonUtils.isAdmin();
		model.addObject("latestVersion", latest.getVersion());
		model.addObject("loid", latest.getPersistInfo().getObjectIdentifier().getStringValue());
		model.addObject("isAdmin", isAdmin);
		model.addObject("oid", oid);
		model.addObject("data", data);
		model.addObject("dto", dto);
//		model.addObject("history", history);
		model.setViewName("popup:/korea/configSheet/configSheet-view");
		return model;
	}

	@Description(value = "CONIFG SHEET 비교 페이지")
	@GetMapping(value = "/compare")
	public ModelAndView compare(@RequestParam String oid, @RequestParam String compareArr) throws Exception {

		System.out.println("controller /compare!!!!!!!!!!!!!!");
		
		ModelAndView model = new ModelAndView("jsonView");
		Project p1 = (Project) CommonUtils.getObject(oid);

		String[] compareOids = compareArr.split(",");
		ArrayList<Project> destList = new ArrayList<>(compareOids.length);
		for (String _oid : compareOids) {
			Project project = (Project) CommonUtils.getObject(_oid);
			destList.add(project);
			System.out.println("## before=="+project.getKekNumber());
		}
		Collections.sort(destList, new ConfigSheetComparator());
		for (Project pp : destList) {
			System.out.println("## after=="+pp.getKekNumber());
		}
		
		ArrayList<ConfigSheetCode> fixedList = ConfigSheetCodeHelper.manager.getConfigSheetCode("CATEGORY");
		
		//fixedList 에 p1꺼 담기.
		
		
		ArrayList<Map<String, Object>> data = ConfigSheetHelper.manager.compare(p1, destList, fixedList);

		model.addObject("p1", p1);
		model.addObject("oid", oid);
		model.addObject("fixedList", fixedList);
		model.addObject("destList", destList); // 최초 선택 데이터 제거
		model.addObject("data", JSONArray.fromObject(data));
		model.setViewName("popup:/korea/configSheet/configSheet-compare");
		return model;
	}

	@Description(value = "CONFIG SHEET 복사할 작번 추가 페이지")
	@GetMapping(value = "/copy")
	public ModelAndView copy(@RequestParam String method, @RequestParam String multi) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -4);
		Timestamp date = new Timestamp(calendar.getTime().getTime());
		String before = date.toString().substring(0, 10);
		String end = DateUtils.getCurrentTimestamp().toString().substring(0, 10);

		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getValueMap("CUSTOMER");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getValueMap("MAK");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		ArrayList<HashMap<String, String>> list = TemplateHelper.manager.getTemplateArrayMap();

		JSONArray elecs = OrgHelper.manager.getDepartmentUser("ELEC");
		JSONArray softs = OrgHelper.manager.getDepartmentUser("SOFT");
		JSONArray machines = OrgHelper.manager.getDepartmentUser("MACHINE");

		model.addObject("elecs", elecs);
		model.addObject("softs", softs);
		model.addObject("machines", machines);
		model.addObject("list", list);
		model.addObject("customers", customers);
		model.addObject("projectTypes", projectTypes);
		model.addObject("maks", maks);
		model.addObject("before", before);
		model.addObject("end", end);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("method", method);
		model.addObject("multi", Boolean.parseBoolean(multi));
		model.setViewName("popup:/korea/configSheet/configSheet-copy");
		return model;
	}

	@Description(value = "CONFIG SHEET 복사 함수")
	@PostMapping(value = "/copy")
	@ResponseBody
	public Map<String, Object> copy(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		String oid = (String) params.get("oid");
		try {

			ConfigSheet configSheet = null;
			Project project = (Project) CommonUtils.getObject(oid);
			QueryResult qr = PersistenceHelper.manager.navigate(project, "configSheet", ConfigSheetProjectLink.class);
			if (qr.size() == 0) {
				result.put("result", FAIL);
				result.put("msg", "작번에 연결된 CONFIG SHEET가 존재하지 않습니다.");
				return result;
			}

			if (qr.hasMoreElements()) {
				configSheet = (ConfigSheet) qr.nextElement();
				ArrayList<Map<String, Object>> list = ConfigSheetHelper.manager.copyBaseData(configSheet);
				result.put("list", list);
				result.put("result", SUCCESS);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/configSheet/copy", "CONFIG SHEET 복사 함수");
		}
		return result;
	}

	@Description(value = "CONFIG SHEET 그리드 저장 함수")
	@PostMapping(value = "/save")
	@ResponseBody
	public Map<String, Object> save(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> removeRows = params.get("removeRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ArrayList<ConfigSheetDTO> removeRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> remove : removeRows) {
				ConfigSheetDTO dto = mapper.convertValue(remove, ConfigSheetDTO.class);
				removeRow.add(dto);
			}

			HashMap<String, List<ConfigSheetDTO>> dataMap = new HashMap<>();
			dataMap.put("removeRows", removeRow); // 삭제행

			ConfigSheetHelper.service.save(dataMap);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/configSheet/save", "CONFIG SHEET 그리드 저장 함수");
		}
		return result;
	}

	@Description(value = "CONFIG SHEET 삭제 함수")
	@GetMapping(value = "/delete")
	@ResponseBody
	public Map<String, Object> delete(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ConfigSheetHelper.service.delete(oid);
			result.put("result", SUCCESS);
			result.put("msg", DELETE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/configSheet/delete", "CONFIG SHEET 삭제 함수");
		}
		return result;
	}

	@Description(value = "산출물 태스크에서 CONFIG SHEET 연결 페이지")
	@GetMapping(value = "/connect")
	public ModelAndView connect(@RequestParam String poid, @RequestParam String toid) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		
		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getValueMap("CUSTOMER");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getValueMap("MAK");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		model.addObject("customers", customers);
		model.addObject("projectTypes", projectTypes);
		model.addObject("maks", maks);
		
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("toid", toid);
		model.addObject("poid", poid);
		model.setViewName("popup:/korea/configSheet/configSheet-connect");
		return model;
	}

	@Description(value = "CONFIG SHEET 태스크 연결 함수")
	@PostMapping(value = "/connect")
	@ResponseBody
	public Map<String, Object> connect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = ConfigSheetHelper.service.connect(params);

			if ((boolean) result.get("exist")) {
				result.put("result", FAIL);
				result.put("msg", "이미 해당 태스크와 연결된 CONFIG SHEET 입니다.");
				return result;
			}

			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/configSheet/connect", "CONFIG SHEET 태스크 연결 함수");
		}
		return result;
	}

	@Description(value = "CONFIG SHEET 태스크 연결 제거 함수")
	@ResponseBody
	@PostMapping(value = "/disconnect")
	public Map<String, Object> disconnect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ConfigSheetHelper.service.disconnect(params);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/configSheet/disconnect", "CONFIG SHEET 태스크 연결 제거 함수");
		}
		return result;
	}

	@Description(value = "CONFIF SHEET 수정 & 개정 페이지")
	@GetMapping(value = "/update")
	public ModelAndView update(@RequestParam String oid, @RequestParam String mode) throws Exception {
		ModelAndView model = new ModelAndView();
		ConfigSheet configSheet = (ConfigSheet) CommonUtils.getObject(oid);
		JSONArray categorys = ConfigSheetCodeHelper.manager.parseJson("CATEGORY");
		JSONArray data = ConfigSheetHelper.manager.loadBaseGridData(oid, false);
		ConfigSheetDTO dto = new ConfigSheetDTO(configSheet);
		boolean isAdmin = CommonUtils.isAdmin();
		model.addObject("categorys", categorys);
		model.addObject("isAdmin", isAdmin);
		model.addObject("oid", oid);
		model.addObject("data", data);
		model.addObject("dto", dto);
		model.addObject("mode", mode);
		model.setViewName("popup:/korea/configSheet/configSheet-update");
		return model;
	}

	@Description(value = "CONFIF SHEET 수정 함수")
	@PostMapping(value = "/modify")
	@ResponseBody
	public Map<String, Object> modify(@RequestBody ConfigSheetDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ConfigSheetHelper.service.modify(dto);
			result.put("result", SUCCESS);
			result.put("msg", MODIFY_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/configSheet/modify", "CONFIG SHEET 수정 함수");
		}
		return result;
	}

	@Description(value = "CONFIF SHEET 개정 함수")
	@PostMapping(value = "/revise")
	@ResponseBody
	public Map<String, Object> revise(@RequestBody ConfigSheetDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ConfigSheetHelper.service.revise(dto);
			result.put("result", SUCCESS);
			result.put("msg", REVISE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/configSheet/revise", "CONFIG SHEET 개정 함수");
		}
		return result;
	}
}
