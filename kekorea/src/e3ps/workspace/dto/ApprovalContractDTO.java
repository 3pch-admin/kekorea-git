package e3ps.workspace.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

import e3ps.common.util.CommonUtils;
import e3ps.workspace.ApprovalContract;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.service.WorkspaceHelper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalContractDTO {

	private String oid;
	private String name;
	private String creator;
	private String submiter;
	private String description;
	private Timestamp completeTime;
	private Timestamp createdDate;
	private String moid;
	private ArrayList<Map<String, String>> keDrawingRows = new ArrayList<>(); // 작번
	private ArrayList<Map<String, String>> kekDrawingRows = new ArrayList<>(); // 작번
	private ArrayList<Map<String, Object>> epmRows = new ArrayList<>(); // 작번
	private ArrayList<Map<String, Object>> numberRuleRows = new ArrayList<>(); // 작번
	private ArrayList<Map<String, String>> agreeRows = new ArrayList<>(); // 검토
	private ArrayList<Map<String, String>> approvalRows = new ArrayList<>(); // 결재
	private ArrayList<Map<String, String>> receiveRows = new ArrayList<>(); // 수신
	public ApprovalContractDTO() {

	}

	public ApprovalContractDTO(ApprovalContract appCon) throws Exception {
		setOid(CommonUtils.getOIDString(appCon));
		
		setName(appCon.getName());
		setCreator(appCon.getOwnership().getOwner().getFullName());
		ApprovalMaster appMaster  = WorkspaceHelper.manager.getMaster(appCon);
		
		ApprovalLine submitLine = WorkspaceHelper.manager.getSubmitLine(appMaster);
		setSubmiter(submitLine.getOwnership().getOwner().getFullName());
		setDescription(appCon.getDescription()==null?"":appCon.getDescription());
		setCompleteTime( appCon.getCompleteTime());
		setCreatedDate(appCon.getCreateTimestamp());
		setMoid(CommonUtils.getOIDString(appMaster));
		
		ArrayList<Map<String, Object>> list = WorkspaceHelper.manager.contractData(appCon);
		if ("EPMDOCUMENT".equals(appCon.getContractType())) {
			for (Map<String, Object> map : list) {
				if (((String) map.get("oid")).indexOf("EPMDocument") > -1) {
					epmRows.add(map);
				}else if (((String) map.get("oid")).indexOf("NumberRule") > -1) {
					numberRuleRows.add(map);
				}
			}
			setEpmRows(epmRows);
			setNumberRuleRows(numberRuleRows);
		}
		
		
	}

}
