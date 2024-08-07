package e3ps.project.issue.controller;

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

import e3ps.admin.commonCode.service.CommonCodeHelper;
import e3ps.common.controller.BaseController;
import e3ps.common.util.CommonUtils;
import e3ps.project.Project;
import e3ps.project.issue.Issue;
import e3ps.project.issue.IssueProjectLink;
import e3ps.project.issue.beans.IssueDTO;
import e3ps.project.issue.service.IssueHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/issue/**")
public class IssueController extends BaseController {

	@Description(value = "특이사항 리스트 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getValueMap("MAK");
		model.addObject("maks", maks);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("sessionUser", sessionUser);
		model.setViewName("/extcore/jsp/project/issue/issue-list.jsp");
		return model;
	}

	@Description(value = "특이사항 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = IssueHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "특이사항 뷰 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		IssueProjectLink link = (IssueProjectLink) CommonUtils.getObject(oid);
		IssueDTO dto = new IssueDTO(link);
		boolean isAdmin = CommonUtils.isAdmin();
		model.addObject("isAdmin", isAdmin);
		model.addObject("dto", dto);
		model.setViewName("popup:/project/issue/issue-view");
		return model;
	}

	@Description(value = "특이사항 등록")
	@PostMapping(value = "/save")
	@ResponseBody
	public Map<String, Object> save(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> removeRows = params.get("removeRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<IssueDTO> removeRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> remove : removeRows) {
				IssueDTO dto = mapper.convertValue(remove, IssueDTO.class);
				removeRow.add(dto);
			}

			HashMap<String, List<IssueDTO>> dataMap = new HashMap<>();
			dataMap.put("removeRows", removeRow); // 삭제행

			IssueHelper.service.save(dataMap);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "이슈 등록 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		Project project = (Project) CommonUtils.getObject(oid);
		ArrayList<Map<String, String>> list = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		map.put("oid", project.getPersistInfo().getObjectIdentifier().getStringValue());
		map.put("projectType_name", project.getProjectType()==null?"":project.getProjectType().getName());
		map.put("customer_name", project.getCustomer()==null?"":project.getCustomer().getName());
		map.put("mak_name", project.getMak()==null?"":project.getMak().getName());
		map.put("detail_name", project.getDetail()==null?"":project.getDetail().getName());
		map.put("install_name", project.getInstall()==null?"":project.getInstall().getName());
		map.put("kekNumber", project.getKekNumber());
		map.put("keNumber", project.getKeNumber());
		map.put("description", project.getDescription());
		list.add(map); // 기본 선택한 작번
		model.addObject("list", JSONArray.fromObject(list));
		model.addObject("oid", oid);
		model.setViewName("popup:/project/issue/issue-create");
		return model;
	}

	@Description(value = "이슈 등록")
	@PostMapping(value = "/create")
	@ResponseBody
	public Map<String, Object> create(@RequestBody IssueDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			IssueHelper.service.create(dto);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "이슈 삭제")
	@GetMapping(value = "/delete")
	@ResponseBody
	public Map<String, Object> delete(String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			IssueHelper.service.delete(oid);
			result.put("result", SUCCESS);
			result.put("msg", DELETE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}

	@Description(value = "특이사항 수정 페이지")
	@GetMapping(value = "/modify")
	public ModelAndView modify(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		Issue issue = (Issue) CommonUtils.getObject(oid);
		IssueDTO dto = new IssueDTO(issue);
		boolean isAdmin = CommonUtils.isAdmin();
		model.addObject("isAdmin", isAdmin);
		model.addObject("dto", dto);
		model.setViewName("popup:/project/issue/issue-modify");
		return model;
	}

	@Description(value = "이슈 수정")
	@PostMapping(value = "/modify")
	@ResponseBody
	public Map<String, Object> modify(@RequestBody IssueDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			IssueHelper.service.modify(dto);
			result.put("result", SUCCESS);
			result.put("msg", MODIFY_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
		}
		return result;
	}
}
