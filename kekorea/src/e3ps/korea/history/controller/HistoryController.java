package e3ps.korea.history.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
import e3ps.admin.specCode.SpecCode;
import e3ps.admin.specCode.service.SpecCodeHelper;
import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.korea.history.service.HistoryHelper;
import e3ps.project.Project;
import e3ps.project.template.service.TemplateHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/history/**")
public class HistoryController extends BaseController {

	@Description(value = "이력관리 리스트 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		ArrayList<Map<String, String>> headers = SpecCodeHelper.manager.getArrayKeyValueMap("SPEC");
		Map<String, ArrayList<Map<String, String>>> list = SpecCodeHelper.manager.getOptionList();
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
		ArrayList<HashMap<String, String>> templates = TemplateHelper.manager.getTemplateArrayMap();

		model.addObject("templates", templates);
		model.addObject("customers", customers);
		model.addObject("projectTypes", projectTypes);
		model.addObject("maks", maks);
		model.addObject("before", before);
		model.addObject("end", end);
		model.addObject("sessionUser", sessionUser);
		model.addObject("isAdmin", isAdmin);
		model.addObject("list", list);
		model.addObject("headers", headers);
		model.setViewName("/extcore/jsp/korea/history/history-list.jsp");
		return model;
	}

	@Description(value = "이력관리 리스트 목록 가져오는 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = HistoryHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "이력 관리 등록 함수")
	@ResponseBody
	@PostMapping(value = "/save")
	public Map<String, Object> save(@RequestBody Map<String, ArrayList<Map<String, String>>> params) throws Exception {
		Map<String, Object> result = new HashMap<>();
		try {
			HistoryHelper.service.save(params);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}
	
	@Description(value = "이력 관리 등록 함수")
	@ResponseBody
	@PostMapping(value = "/dataSave")
	public Map<String, Object> dataSave(@RequestBody Map<String, ArrayList<Map<String, String>>> params) throws Exception {
		Map<String, Object> result = new HashMap<>();
		try {
			HistoryHelper.service.dataSave(params);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}
	
	
	@Description(value = "이력관리 상세정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
//		Persistable per = CommonUtils.getObject(oid);
		ArrayList<Map<String, String>> headers = SpecCodeHelper.manager.getArrayKeyValueMap("SPEC");
		Map<String, ArrayList<Map<String, String>>> list = SpecCodeHelper.manager.getOptionList();
//		Map map = new HashMap();

//		History link = (History) per;
		
		JSONArray data = HistoryHelper.manager.view(oid);
		model.addObject("data", data);
		model.addObject("list", list);
		model.addObject("headers", headers);
		model.setViewName("popup:/korea/history/history-view");
		return model;
	}
	

	@Description(value = "이력관리 비교 페이지 공통 함수")
	@GetMapping(value = "/compare")
	public ModelAndView compare(@RequestParam String oid, @RequestParam String compareArr) throws Exception {
		ModelAndView model = new ModelAndView();
		Project p1 = (Project) CommonUtils.getObject(oid);
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		String[] compareOids = compareArr.split(",");
		ArrayList<Project> destList = new ArrayList<>(compareOids.length);
		for (String _oid : compareOids) {
			Project project = (Project) CommonUtils.getObject(_oid);
			destList.add(project);
		}

		ArrayList<SpecCode> fixedList = SpecCodeHelper.manager.getSpecCode();
		ArrayList<Map<String, Object>> data = HistoryHelper.manager.compare(p1, destList, fixedList);
		model.addObject("sessionUser", sessionUser);
		model.addObject("p1", p1);
		model.addObject("oid", oid);
		model.addObject("fixedList", fixedList);
		model.addObject("destList", destList);
		model.addObject("data", JSONArray.fromObject(data));
		model.setViewName("popup:/korea/history/history-compare");
		return model;
	}
}
