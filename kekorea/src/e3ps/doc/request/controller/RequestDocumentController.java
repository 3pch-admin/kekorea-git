package e3ps.doc.request.controller;

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
import e3ps.doc.request.RequestDocument;
import e3ps.doc.request.dto.RequestDocumentDTO;
import e3ps.doc.request.service.RequestDocumentHelper;
import e3ps.org.service.OrgHelper;
import e3ps.project.template.service.TemplateHelper;
import e3ps.system.service.ErrorLogHelper;
import net.sf.json.JSONArray;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/requestDocument/**")
public class RequestDocumentController extends BaseController {

	@Description(value = "의뢰서 리스트 페이지")
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
		model.setViewName("/extcore/jsp/document/request/requestDocument-list.jsp");
		return model;
	}

	@Description(value = "의뢰서 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = RequestDocumentHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/list", "의뢰서 조회 함수");
		}
		return result;
	}

	@Description(value = "의뢰서 그리드 저장")
	@PostMapping(value = "/save")
	@ResponseBody
	public Map<String, Object> save(@RequestBody Map<String, ArrayList<LinkedHashMap<String, Object>>> params)
			throws Exception {
		ArrayList<LinkedHashMap<String, Object>> removeRows = params.get("removeRows");
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList<RequestDocumentDTO> removeRow = new ArrayList<>();
			for (LinkedHashMap<String, Object> remove : removeRows) {
				RequestDocumentDTO dto = mapper.convertValue(remove, RequestDocumentDTO.class);
				removeRow.add(dto);
			}

			HashMap<String, List<RequestDocumentDTO>> dataMap = new HashMap<>();
			dataMap.put("removeRows", removeRow); // 삭제행

			RequestDocumentHelper.service.save(dataMap);
			result.put("result", SUCCESS);
			result.put("msg", SAVE_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/save", "의뢰서 그리드 저장 함수");
		}
		return result;
	}

	@Description(value = "의뢰서 등록 함수")
	@ResponseBody
	@PostMapping(value = "/create")
	public Map<String, Object> create(@RequestBody RequestDocumentDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

//			if (dto.isConnect()) {
//				Project project = (Project) CommonUtils.getObject(dto.getPoid());
//				QueryResult qr = PersistenceHelper.manager.navigate(project, "requestDocument",
//						RequestDocumentProjectLink.class);
//				if (qr.size() > 0) {
//					result.put("result", FAIL);
//					result.put("msg", "작번(" + project.getKekNumber() + ")과 연결된 의뢰서가 이미 존재합니다.");
//				}
//			}

			result = RequestDocumentHelper.service.create(dto);
			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/create", "의뢰서 등록 함수");
		}
		return result;
	}

	@Description(value = "의뢰서 등록 페이지")
	@GetMapping(value = "/create")
	public ModelAndView create(@RequestParam(required = false) String poid, @RequestParam(required = false) String toid)
			throws Exception {
		ModelAndView model = new ModelAndView();
		ArrayList<HashMap<String, String>> list = TemplateHelper.manager.getTemplateArrayMap();
		JSONArray elecs = OrgHelper.manager.getDepartmentUser("ELEC");
		JSONArray softs = OrgHelper.manager.getDepartmentUser("SOFT");
		JSONArray machines = OrgHelper.manager.getDepartmentUser("MACHINE");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getArrayKeyValueMap("MAK");
		ArrayList<Map<String, String>> details = CommonCodeHelper.manager.getArrayKeyValueMap("MAK_DETAIL");
		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getArrayKeyValueMap("CUSTOMER");
		ArrayList<Map<String, String>> installs = CommonCodeHelper.manager.getArrayKeyValueMap("INSTALL");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		model.addObject("maks", JSONArray.fromObject(maks));
		model.addObject("installs", JSONArray.fromObject(installs));
		model.addObject("customers", JSONArray.fromObject(customers));
		model.addObject("projectTypes", JSONArray.fromObject(projectTypes));
		model.addObject("elecs", elecs);
		model.addObject("softs", softs);
		model.addObject("details", JSONArray.fromObject(details));
		model.addObject("machines", machines);
		model.addObject("list", list);
		model.addObject("poid", poid);
		model.addObject("toid", toid);
		model.setViewName("popup:/document/request/requestDocument-create");
		return model;
	}

	@Description(value = "의뢰서 등록 검증")
	@ResponseBody
	@PostMapping(value = "/validate")
	public Map<String, Object> validate(@RequestBody Map<String, String> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = RequestDocumentHelper.manager.validate(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/validate", "의뢰서 등록 검증 함수");
		}
		return result;
	}

	@Description(value = "의뢰서 태스크 연결 제거 함수")
	@ResponseBody
	@PostMapping(value = "/disconnect")
	public Map<String, Object> disconnect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			RequestDocumentHelper.service.disconnect(params);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/disconnect", "의뢰서 태스크 연결 제거 함수");
		}
		return result;
	}

	@Description(value = "의뢰서 삭제 함수")
	@ResponseBody
	@GetMapping(value = "/delete")
	public Map<String, Object> delete(@RequestParam String oid) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			RequestDocumentHelper.service.delete(oid);
			result.put("msg", DELETE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/delete", "의뢰서 삭제 함수");
		}
		return result;
	}

	@Description(value = "의뢰서 정보 페이지")
	@GetMapping(value = "/view")
	public ModelAndView view(@RequestParam String oid) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		RequestDocument requestDocument = (RequestDocument) CommonUtils.getObject(oid);
		RequestDocumentDTO dto = new RequestDocumentDTO(requestDocument);
		model.addObject("isAdmin", isAdmin);
		model.addObject("dto", dto);
		model.setViewName("popup:/document/request/requestDocument-view");
		return model;
	}
	
	@Description(value = "의뢰서 수정 페이지")
	@GetMapping(value = "/modify")
	public ModelAndView modify(@RequestParam String oid) throws Exception {
		System.out.println("####111111111 modify");
		ModelAndView model = new ModelAndView();
		ArrayList<HashMap<String, String>> list = TemplateHelper.manager.getTemplateArrayMap();
		JSONArray elecs = OrgHelper.manager.getDepartmentUser("ELEC");
		JSONArray softs = OrgHelper.manager.getDepartmentUser("SOFT");
		JSONArray machines = OrgHelper.manager.getDepartmentUser("MACHINE");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getArrayKeyValueMap("MAK");
		ArrayList<Map<String, String>> details = CommonCodeHelper.manager.getArrayKeyValueMap("MAK_DETAIL");
		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getArrayKeyValueMap("CUSTOMER");
		ArrayList<Map<String, String>> installs = CommonCodeHelper.manager.getArrayKeyValueMap("INSTALL");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		boolean isAdmin = CommonUtils.isAdmin();
		RequestDocument requestDocument = (RequestDocument) CommonUtils.getObject(oid);
		RequestDocumentDTO dto = new RequestDocumentDTO(requestDocument);
		//String template = RequestDocumentHelper.manager.getProjectTemplate(oid);
		JSONArray projects = RequestDocumentHelper.manager.getProjects(requestDocument);
		
		//model.addObject("template", template);
		model.addObject("projects", projects);
		model.addObject("isAdmin", isAdmin);
		model.addObject("dto", dto);
		model.addObject("maks", JSONArray.fromObject(maks));
		model.addObject("installs", JSONArray.fromObject(installs));
		model.addObject("customers", JSONArray.fromObject(customers));
		model.addObject("projectTypes", JSONArray.fromObject(projectTypes));
		model.addObject("elecs", elecs);
		model.addObject("softs", softs);
		model.addObject("details", JSONArray.fromObject(details));
		model.addObject("machines", machines);
		model.addObject("list", list);
		model.setViewName("popup:/document/request/requestDocument-update");
		return model;
	}

	@Description(value = "산출물 수정")
	@ResponseBody
	@PostMapping(value = "/modify")
	public Map<String, Object> modify(@RequestBody RequestDocumentDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			RequestDocumentHelper.service.modify(dto);
			result.put("result", SUCCESS);
			result.put("msg", MODIFY_MSG);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/modify", "의뢰서 수정 함수");
		}
		return result;
	}

	@Description(value = "산출물 의뢰서 연결 페이지")
	@GetMapping(value = "/connect")
	public ModelAndView connect(@RequestParam String poid, @RequestParam String toid) throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("isAdmin", isAdmin);
		model.addObject("sessionUser", sessionUser);
		model.addObject("toid", toid);
		model.addObject("poid", poid);
		ArrayList<Map<String, String>> customers = CommonCodeHelper.manager.getValueMap("CUSTOMER");
		ArrayList<Map<String, String>> maks = CommonCodeHelper.manager.getValueMap("MAK");
		ArrayList<Map<String, String>> projectTypes = CommonCodeHelper.manager.getValueMap("PROJECT_TYPE");
		model.addObject("customers", customers);
		model.addObject("projectTypes", projectTypes);
		model.addObject("maks", maks);
		model.setViewName("popup:/document/request/requestDocument-connect");
		return model;
	}

	@Description(value = "의뢰서 태스크 연결 연결")
	@PostMapping(value = "/connect")
	@ResponseBody
	public Map<String, Object> connect(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = RequestDocumentHelper.service.connect(params);

			if ((boolean) result.get("exist")) {
				result.put("result", FAIL);
				result.put("msg", "이미 해당 태스크와 연결된 의뢰서 입니다.");
				return result;
			}

			result.put("msg", SAVE_MSG);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/requestDocument/connect", "의뢰서 태스크 연결 함수");
		}
		return result;
	}
}
