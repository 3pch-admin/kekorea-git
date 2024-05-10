package e3ps.korea.configSheet.beans;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import e3ps.common.util.AUIGridUtils;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.StringUtils;
import e3ps.korea.configSheet.ConfigSheet;
import e3ps.korea.configSheet.ConfigSheetProjectLink;
import e3ps.project.Project;
import lombok.Getter;
import lombok.Setter;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;

@Getter
@Setter
public class ConfigSheetDTO {

	private String oid;
	private String loid;
	private String poid;
	private String projectType_name;
	private String name;
	private String number;
	private String content;
	private String customer_name;
	private String install_name;
	private String mak_name;
	private String detail_name;
	private String kekNumber;
	private String keNumber;
	private String userId;
	private String description;
	private String state;
	private String model;
	private Timestamp pdate;
	private String pdate_txt;
	private String creator;
	private Timestamp createdDate;
	private String createdDate_txt;
	private String creatorId;
	private int version;
	private boolean latest;
	private ArrayList<String> dataFields = new ArrayList<>();

	private String totalKekNumber;
	private String totalKeNumber;
	
	// 변수용
	private ArrayList<String> secondarys = new ArrayList<>();
	private ArrayList<Map<String, String>> addRows = new ArrayList<>();
	private ArrayList<Map<String, String>> addRows9 = new ArrayList<>();
	private ArrayList<Map<String, String>> agreeRows = new ArrayList<>(); // 검토
	private ArrayList<Map<String, String>> approvalRows = new ArrayList<>(); // 결재
	private ArrayList<Map<String, String>> receiveRows = new ArrayList<>(); // 수신
	private int progress;

	private boolean isEdit = false;
	private boolean isRevise = false;
	private String primary;

	public ConfigSheetDTO() {

	}

	public ConfigSheetDTO(ConfigSheet conSheet) throws Exception {
		setOid(conSheet.getPersistInfo().getObjectIdentifier().getStringValue());
		setName(conSheet.getName());
		setNumber(conSheet.getNumber());
		setContent(StringUtils.replaceToValue(conSheet.getDescription()));
		setState(conSheet.getLifeCycleState().getDisplay());
		setCreator(conSheet.getCreatorFullName());
		setCreatedDate(conSheet.getCreateTimestamp());
		setCreatedDate_txt(CommonUtils.getPersistableTime(conSheet.getCreateTimestamp()));
		setCreatorId(conSheet.getCreatorName());
		setEdit(conSheet.getLifeCycleState().toString().equals("INWORK"));
		setRevise(conSheet.getLifeCycleState().toString().equals("APPROVED"));
		ArrayList<String> dFields = conSheet.getDataFields();
		Collections.sort(dFields);
		setDataFields(dFields);
		setVersion(conSheet.getVersion());
		setPrimary(AUIGridUtils.primaryTemplate(conSheet));
		
		QueryResult qr2 = PersistenceHelper.manager.navigate(conSheet, "project", ConfigSheetProjectLink.class);
		String reKekNumber = "";
		String reKeNumber = "";

		while (qr2.hasMoreElements()) {
			Project pp = (Project) qr2.nextElement();

			if (!"".equals(reKekNumber)) {
				reKekNumber += "," + pp.getKekNumber();
			} else {
				reKekNumber = pp.getKekNumber();
			}

			if (!"".equals(reKeNumber)) {
				reKeNumber += "," + pp.getKeNumber();
			} else {
				reKeNumber = pp.getKeNumber();
			}
		}
		setTotalKekNumber(reKekNumber);
		setTotalKeNumber(reKeNumber);
		
	}

	public ConfigSheetDTO(ConfigSheetProjectLink link) throws Exception {
		ConfigSheet configSheet = link.getConfigSheet();
		Project project = link.getProject();
		setOid(configSheet.getPersistInfo().getObjectIdentifier().getStringValue());
		setLoid(link.getPersistInfo().getObjectIdentifier().getStringValue());
		setPoid(project.getPersistInfo().getObjectIdentifier().getStringValue());
		setProjectType_name(project.getProjectType().getName());
		setName(configSheet.getName());
		setNumber(configSheet.getNumber());
		setContent(StringUtils.replaceToValue(configSheet.getDescription()));
		setCustomer_name(project.getCustomer() != null ? project.getCustomer().getName() : "");
		setInstall_name(project.getInstall() != null ? project.getInstall().getName() : "");
		setMak_name(project.getMak() != null ? project.getMak().getName() : "");
		setDetail_name(project.getDetail() != null ? project.getDetail().getName() : "");
		setKekNumber(project.getKekNumber());
		setKeNumber(project.getKeNumber());
		setUserId(project.getUserId());
		setDescription(StringUtils.replaceToValue(project.getDescription()));
		setModel(project.getModel());
		setPdate(project.getPDate());
		setPdate_txt(CommonUtils.getPersistableTime(project.getPDate()));
		setState(configSheet.getLifeCycleState().getDisplay());
		setCreator(configSheet.getCreatorFullName());
		setCreatedDate(configSheet.getCreateTimestamp());
		setCreatedDate_txt(CommonUtils.getPersistableTime(configSheet.getCreateTimestamp()));
		setCreatorId(configSheet.getCreatorName());
		setVersion(configSheet.getVersion());
		setLatest(configSheet.getLatest());
		setEdit(configSheet.getLifeCycleState().toString().equals("INWORK"));
		setRevise(configSheet.getLifeCycleState().toString().equals("APPROVED"));
		ArrayList<String> dFields = configSheet.getDataFields();
		Collections.sort(dFields);
		setDataFields(dFields);
		setPrimary(AUIGridUtils.primaryTemplate(configSheet));
	}
}
