package e3ps.doc.meeting.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

import e3ps.common.util.CommonUtils;
import e3ps.doc.meeting.Meeting;
import e3ps.doc.meeting.MeetingProjectLink;
import e3ps.project.Project;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;

@Getter
@Setter
@ToString
public class MeetingDTO {

	private String oid;
	private String poid;
	private String loid;
	private String name;
	private String tname;
	private String toid;
	private String number;
	private String projectType_name;
	private String content;
	private String customer_name;
	private String install_name;
	private String mak_name;
	private String detail_name;
	private String kekNumber;
	private String keNumber;
	private String userId;
	private String description;
	private String model;
	private Timestamp pdate;
	private String pdate_txt;
	private String state;
	private String creator;
	private Timestamp createdDate;
	private String createdDate_txt;

	private String totalKekNumber;
	private String totalKeNumber;

	// 변수 담기 용도
	private ArrayList<Map<String, String>> addRows9 = new ArrayList<>();
	private ArrayList<String> secondarys = new ArrayList<>();
	private ArrayList<Map<String, String>> agreeRows = new ArrayList<>(); // 검토
	private ArrayList<Map<String, String>> approvalRows = new ArrayList<>(); // 결재
	private ArrayList<Map<String, String>> receiveRows = new ArrayList<>(); // 수신
	private String tiny;

	public MeetingDTO() {

	}

	public MeetingDTO(Meeting mee) throws Exception {
		setOid(mee.getPersistInfo().getObjectIdentifier().getStringValue());
		setName(mee.getName());
		setNumber(mee.getNumber());
		setContent(mee.getContent());
		setState(mee.getLifeCycleState().getDisplay());
		setCreator(mee.getCreatorFullName());
		setCreatedDate(mee.getCreateTimestamp());
		setCreatedDate_txt(CommonUtils.getPersistableTime(mee.getCreateTimestamp()));

		if (mee.getTiny() != null) {
			setToid(mee.getTiny().getPersistInfo().getObjectIdentifier().getStringValue());
			setTname(mee.getTiny().getName());
		}

		QueryResult qr2 = PersistenceHelper.manager.navigate(mee, "project", MeetingProjectLink.class);
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

	public MeetingDTO(MeetingProjectLink link) throws Exception {
		Meeting meeting = link.getMeeting();
		Project project = link.getProject();
		setOid(meeting.getPersistInfo().getObjectIdentifier().getStringValue());
		setPoid(project.getPersistInfo().getObjectIdentifier().getStringValue());
		setLoid(link.getPersistInfo().getObjectIdentifier().getStringValue());
		setProjectType_name(project.getProjectType() == null ? "" : project.getProjectType().getName());
		setName(meeting.getName());
		setNumber(meeting.getNumber());
		setContent(meeting.getContent());
		setCustomer_name(project.getCustomer() == null ? "" : project.getCustomer().getName());
		setInstall_name(project.getInstall() == null ? "" : project.getInstall().getName());
		setMak_name(project.getMak() == null ? "" : project.getMak().getName());
		setDetail_name(project.getDetail() == null ? "" : project.getDetail().getName());
		setKekNumber(project.getKekNumber());
		setKeNumber(project.getKeNumber());
		setUserId(project.getUserId());
		setDescription(project.getDescription());
		setModel(project.getModel());
		setPdate(project.getPDate());
		setPdate_txt(CommonUtils.getPersistableTime(project.getPDate()));
		setState(meeting.getLifeCycleState().getDisplay());
		setCreator(meeting.getCreatorFullName());
		setCreatedDate(meeting.getCreateTimestamp());
		setCreatedDate_txt(CommonUtils.getPersistableTime(meeting.getCreateTimestamp()));

		if (meeting.getTiny() != null) {
			setToid(meeting.getTiny().getPersistInfo().getObjectIdentifier().getStringValue());
			setTname(meeting.getTiny().getName());
		}
	}
}