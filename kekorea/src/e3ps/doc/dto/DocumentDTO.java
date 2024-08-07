package e3ps.doc.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

import e3ps.common.util.CommonUtils;
import e3ps.common.util.IBAUtils;
import e3ps.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import wt.doc.WTDocument;

@Getter
@Setter
public class DocumentDTO {

	private String oid;
	private String name;
	private String number;
	private String description;
	private String location;
	private String state;
	private String version;
	private String docType;
	private String creator;
	private Timestamp createdDate;
	private String createdDate_txt;
	private String modifier;
	private Timestamp modifiedDate;
	private String modifiedDate_txt;
	private String numberRule;
	private String numberRuleVersion;
//	private String revise;
	private boolean isLast = false;

	/**
	 * 첨부파일 변수
	 */
	private ArrayList<String> primarys = new ArrayList<>();

	/**
	 * 결재 변수
	 */
	private ArrayList<Map<String, String>> agreeRows = new ArrayList<>(); // 검토
	private ArrayList<Map<String, String>> approvalRows = new ArrayList<>(); // 결재
	private ArrayList<Map<String, String>> receiveRows = new ArrayList<>(); // 수신
	private boolean self;

	/**
	 * 부품 변수
	 */
	private ArrayList<Map<String, String>> addRows7 = new ArrayList<>();

	/**
	 * 도번 변수
	 */
	private ArrayList<Map<String, Object>> addRows11 = new ArrayList<>();

	public DocumentDTO() {

	}

	public DocumentDTO(WTDocument document) throws Exception {
		setOid(document.getPersistInfo().getObjectIdentifier().getStringValue());
		setName(document.getName());
		setNumber(document.getNumber());
		setDescription(StringUtils.replaceToValue(document.getDescription()));
		setLocation(document.getLocation());
		setState(document.getLifeCycleState().getDisplay());
		setVersion(CommonUtils.getFullVersion(document));
		setCreator(document.getCreatorFullName());
		setCreatedDate(document.getCreateTimestamp());
		setCreatedDate_txt(CommonUtils.getPersistableTime(document.getCreateTimestamp(), 16));
		setModifier(document.getModifierFullName());
		setModifiedDate(document.getModifyTimestamp());
		setModifiedDate_txt(CommonUtils.getPersistableTime(document.getModifyTimestamp(), 16));
		setDocType(document.getDocType().getDisplay());
		setNumberRule(IBAUtils.getStringValue(document, "NUMBER_RULE"));
		setNumberRuleVersion(IBAUtils.getStringValue(document, "NUMBER_RULE_VERSION"));
		setLast(CommonUtils.isLatestVersion(document));
	}
}
