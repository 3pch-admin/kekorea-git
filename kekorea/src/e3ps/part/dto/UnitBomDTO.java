package e3ps.part.dto;

import java.util.ArrayList;
import java.util.Map;

import e3ps.part.UnitBom;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitBomDTO {

	private String oid;
	private String ucode;
	private String partNo;
	private String partName;
	private String spec;
	private String unit;
	private String maker;
	private String customer;
	private String currency;
	private String price;

	private ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	public UnitBomDTO() {

	}

	public UnitBomDTO(UnitBom unitBom) throws Exception {
		setOid(unitBom.getPersistInfo().getObjectIdentifier().getStringValue());
		setUcode(unitBom.getUCode());
		setPartNo(unitBom.getPartNo());
		setPartName(unitBom.getPartName());
		setSpec(unitBom.getSpec());
		setUnit(unitBom.getUnit());
		setMaker(unitBom.getMaker());
		setCustomer(unitBom.getCustomer());
		setCurrency(unitBom.getCurrency());
		setPrice(unitBom.getPrice());
	}
}
