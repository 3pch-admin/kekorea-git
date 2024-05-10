package e3ps.bom.tbom.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

import e3ps.bom.tbom.TBOMMaster;
import e3ps.bom.tbom.TBOMMasterProjectLink;
import e3ps.common.util.CommonUtils;
import e3ps.project.Project;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TBOMDTO {

	private String oid;
	private String loid;
	private String poid;
	private String projectType_code;
	private String projectType_name;
	private String projectType_oid;
	private String name;
	private String number;
	private boolean latest;
	private int version;
	private String mak_code;
	private String mak_name;
	private String mak_oid;
	private String detail_code;
	private String detail_name;
	private String detail_oid;
	private String kekNumber;
	private String keNumber;
	private String userId;
	private String description;
	private String customer_code;
	private String customer_name;
	private String customer_oid;
	private String install_code;
	private String install_name;
	private String install_oid;
	private Timestamp pdate;
	private String pdate_txt;
	private String model;
	private String creator;
	private Timestamp createdDate;
	private String createdDate_txt;
	private Timestamp modifiedDate;
	private String modifiedDate_txt;
	private String state;
	private String content;
	private String note;

	// 변수용
	private ArrayList<Map<String, Object>> addRows = new ArrayList<>(); // 도면 일람표
	private ArrayList<Map<String, String>> addRows9 = new ArrayList<>(); // 작번
	private ArrayList<Map<String, String>> agreeRows = new ArrayList<>(); // 검토
	private ArrayList<Map<String, String>> approvalRows = new ArrayList<>(); // 결재
	private ArrayList<Map<String, String>> receiveRows = new ArrayList<>(); // 수신
	private ArrayList<String> secondarys = new ArrayList<>();
	private String toid;

	private boolean isEdit = false;
	private boolean isRevise = false;

	public TBOMDTO() {

	}

	public TBOMDTO(TBOMMaster master) throws Exception {
		setOid(master.getPersistInfo().getObjectIdentifier().getStringValue());
		setName(master.getName());
		setNumber(master.getNumber());
		setVersion(master.getVersion());
		setLatest(true);
		setContent(master.getDescription());
		setState(master.getLifeCycleState().getDisplay());
		setCreator(master.getCreatorFullName());
		setCreatedDate(master.getCreateTimestamp());
		setCreatedDate_txt(CommonUtils.getPersistableTime(master.getCreateTimestamp()));
		setModifiedDate(master.getModifyTimestamp());
		setModifiedDate_txt(CommonUtils.getPersistableTime(master.getModifyTimestamp()));
		setEdit(master.getLifeCycleState().toString().equals("INWORK"));
		setRevise(master.getLifeCycleState().toString().equals("APPROVED"));
		setNote(master.getNote());
	}

	public TBOMDTO(TBOMMasterProjectLink link) throws Exception {
		TBOMMaster master = link.getMaster();
		Project project = link.getProject();
		setOid(master.getPersistInfo().getObjectIdentifier().getStringValue());
		setLoid(link.getPersistInfo().getObjectIdentifier().getStringValue());
		setPoid(project.getPersistInfo().getObjectIdentifier().getStringValue());
		setName(master.getName());
		setNumber(master.getNumber());
		setVersion(master.getVersion());
		setLatest(true);
		setContent(master.getDescription());
		if (project.getProjectType() != null) {
			setProjectType_code(project.getProjectType().getCode());
			setProjectType_name(project.getProjectType().getName());
			setProjectType_oid(project.getProjectType().getPersistInfo().getObjectIdentifier().getStringValue());
		}
		if (project.getMak() != null) {
			setMak_code(project.getMak().getCode());
			setMak_name(project.getMak().getName());
			setMak_oid(project.getMak().getPersistInfo().getObjectIdentifier().getStringValue());
		}
		if (project.getDetail() != null) {
			setDetail_code(project.getDetail().getCode());
			setDetail_name(project.getDetail().getName());
			setDetail_oid(project.getDetail().getPersistInfo().getObjectIdentifier().getStringValue());
		}
		setKekNumber(project.getKekNumber());
		setKeNumber(project.getKeNumber());
		setUserId(project.getUserId());
		setDescription(project.getDescription());
		if (project.getCustomer() != null) {
			setCustomer_code(project.getCustomer().getCode());
			setCustomer_name(project.getCustomer().getName());
			setCustomer_oid(project.getCustomer().getPersistInfo().getObjectIdentifier().getStringValue());
		}
		if (project.getInstall() != null) {
			setInstall_code(project.getInstall().getCode());
			setInstall_name(project.getInstall().getName());
			setInstall_oid(project.getInstall().getPersistInfo().getObjectIdentifier().getStringValue());
		}
		if (project.getPDate() != null) {
			setPdate(project.getPDate());
			setPdate_txt(CommonUtils.getPersistableTime(project.getPDate()));
		}
		setModel(project.getModel());
		setState(master.getLifeCycleState().getDisplay());
		setCreator(master.getCreatorFullName());
		setCreatedDate(master.getCreateTimestamp());
		setCreatedDate_txt(CommonUtils.getPersistableTime(master.getCreateTimestamp()));
		setModifiedDate(master.getModifyTimestamp());
		setModifiedDate_txt(CommonUtils.getPersistableTime(master.getModifyTimestamp()));
		setEdit(master.getLifeCycleState().toString().equals("INWORK"));
		setRevise(master.getLifeCycleState().toString().equals("APPROVED"));
		setNote(master.getNote());
	}
}
