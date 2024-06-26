package e3ps.erp.controller;

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
import e3ps.erp.service.ErpHelper;
import e3ps.system.service.ErrorLogHelper;
import wt.org.WTUser;
import wt.session.SessionHelper;

@Controller
@RequestMapping(value = "/erp/**")
public class ErpController extends BaseController {

	@Description(value = "ERP 로그 리스트 페이지")
	@GetMapping(value = "/list")
	public ModelAndView list() throws Exception {
		ModelAndView model = new ModelAndView();
		boolean isAdmin = CommonUtils.isAdmin();
		boolean isSupervisor = CommonUtils.isSupervisor();
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		model.addObject("sessionUser", sessionUser);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("isAdmin", isAdmin);
		model.setViewName("/extcore/jsp/erp/erp-list.jsp");
		return model;
	}

	@Description(value = "ERP 로그 조회 함수")
	@ResponseBody
	@PostMapping(value = "/list")
	public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = ErpHelper.manager.list(params);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/erp/list", "ERP 로그 조회 함수");
		}
		return result;
	}

	@Description(value = "수배표 등록시 품목 정보 가져오기")
	@GetMapping(value = "/partListItemValue")
	@ResponseBody
	public Map<String, Object> partListItemValue(@RequestParam String partNo, @RequestParam String quantity)
			throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (ErpHelper.isOperation) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("partNo", partNo);
				params.put("quantity", quantity);
			}
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/erp/partListItemValue", "수배표 등록시 품목 정보 가져오기");
		}
		return result;
	}

	@Description(value = "수배표 YCODE 체크")
	@GetMapping(value = "/validate")
	@ResponseBody
	public Map<String, Object> validate(@RequestParam String partNo) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (ErpHelper.isOperation) {
				result = ErpHelper.manager.validate(partNo);
			}
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/erp/validate", "수배표 YCODE 체크");
		}
		return result;
	}

	@Description(value = "수배표 UNITNAME 가져오기")
	@GetMapping(value = "/getUnitName")
	@ResponseBody
	public Map<String, Object> getUnitName(@RequestParam int lotNo, @RequestParam String callLoc) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (ErpHelper.isOperation) {
				result = ErpHelper.manager.getUnitName(lotNo, callLoc);
			}
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/erp/getUnitName", "수배표 UNITNAME 가져오기");
		}
		return result;
	}

	@Description(value = "수배표 부품정보 가져오기")
	@GetMapping(value = "/getErpItemByPartNoAndQuantity")
	@ResponseBody
	public Map<String, Object> getErpItemByPartNoAndQuantity(@RequestParam String partNo, @RequestParam int quantity)
			throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (ErpHelper.isOperation) {
				result = ErpHelper.manager.getErpItemByPartNoAndQuantity(partNo, quantity);
			}
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/erp/getErpItemByPartNoAndQuantity", "수배표 부품정보 가져오기");
		}
		return result;
	}

	@Description(value = "규격으로 ERP 부품정보 가져오기")
	@GetMapping(value = "/getErpItemBySpec")
	@ResponseBody
	public Map<String, Object> getErpItemBySpec(@RequestParam String spec, @RequestParam String callLoc)
			throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (ErpHelper.isOperation) {
				result = ErpHelper.manager.getErpItemBySpec(spec, callLoc);
			}
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/erp/getErpItemBySpec", "규격으로 ERP 부품정보 가져오기");
		}
		return result;
	}

	@Description(value = "YCODE로 ERP 부품정보 가져오기")
	@GetMapping(value = "/getErpItemByPartNo")
	@ResponseBody
	public Map<String, Object> getErpItemByPartNo(@RequestParam String partNo, @RequestParam String callLoc)
			throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (ErpHelper.isOperation) {
				result = ErpHelper.manager.getErpItemByPartNo(partNo, callLoc);
			}
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", e.toString());
			result.put("result", FAIL);
			ErrorLogHelper.service.create(e.toString(), "/erp/getErpItemByPartNo", "YCODE로 ERP 부품정보 가져오기");
		}
		return result;
	}

	@Description(value = "LOT NO 로 UNIT NAME 조회 함수")
	@ResponseBody
	@GetMapping(value = "/getLotNoByUnitName")
	public Map<String, Object> getLotNoByUnitName(@RequestParam String lotNo) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = ErpHelper.manager.getLotNoByUnitName(lotNo);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/erp/getLotNoByUnitName", "LOT NO 로 UNIT NAME 조회 함수");
		}
		return result;
	}

	@Description(value = "YCODE 검증 함수")
	@ResponseBody
	@GetMapping(value = "/validateErpPartNumber")
	public Map<String, Object> validateErpPartNumber(@RequestParam String partNumber) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = ErpHelper.manager.validateErpPartNumber(partNumber);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/erp/validateErpPartNumber", "LOT 검증 함수");
		}
		return result;
	}

	@Description(value = "UNIT BOM 등록시 규격 중복 체크")
	@ResponseBody
	@GetMapping(value = "/erpCheck")
	public Map<String, Object> erpCheck(@RequestParam String spec) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = ErpHelper.manager.erpCheck(spec);
			result.put("result", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", FAIL);
			result.put("msg", e.toString());
			ErrorLogHelper.service.create(e.toString(), "/erp/erpCheck", "UNIT BOM 등록시 규격 중복 체크");
		}
		return result;
	}
}
