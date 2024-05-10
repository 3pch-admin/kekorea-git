package e3ps.part.service;

import java.util.ArrayList;
import java.util.Map;

import e3ps.erp.service.ErpHelper;
import e3ps.part.UnitBom;
import e3ps.part.UnitBomPartLink;
import e3ps.part.UnitSubPart;
import e3ps.part.dto.UnitBomDTO;
import wt.fc.PersistenceHelper;
import wt.pom.Transaction;
import wt.services.StandardManager;
import wt.util.WTException;

public class StandardUnitBomService extends StandardManager implements UnitBomService {

	public static StandardUnitBomService newStandardUnitBomService() throws WTException {
		StandardUnitBomService instance = new StandardUnitBomService();
		instance.initialize();
		return instance;
	}

	@Override
	public void create(UnitBomDTO dto) throws Exception {
		Transaction trs = new Transaction();
		try {
			trs.start();

			UnitBom unitBom = UnitBom.newUnitBom();
			unitBom.setPartName(dto.getPartName());
			// unitBom.setPartNo(upartNo);
			unitBom.setSpec(dto.getSpec());
			unitBom.setUnit(dto.getUnit());
			unitBom.setMaker(dto.getMaker());
			unitBom.setCustomer(dto.getCustomer());
			unitBom.setCurrency(dto.getCurrency());
			unitBom.setPrice(dto.getPrice());
			PersistenceHelper.manager.save(unitBom);

			int totalPrice = 0;
			ArrayList<UnitSubPart> list = new ArrayList<>();
			ArrayList<Map<String, Object>> data = dto.getData();
			for (Map<String, Object> dd : data) {

				int lotNo = (int) dd.get("lotNo");
				String unitName = (String) dd.get("unitName");
				String partNumber = (String) dd.get("partNumber");
				String partName = (String) dd.get("partName");
				String unit = (String) dd.get("unit");
				String maker = (String) dd.get("maker");
				String customer = (String) dd.get("customer");
				int quantity = (int) dd.get("quantity");
				String currency = (String) dd.get("currency");
				String ref = (String) dd.get("ref");
				String rate = (String) dd.get("rate");
				String note = (String) dd.get("note");
				String category = (String) dd.get("category");
				int price = (int) dd.get("price");
				int won = (int) dd.get("won");

				System.out.println(dd);

				UnitSubPart sub = UnitSubPart.newUnitSubPart();

				sub.setLotNo(String.valueOf(lotNo));
				sub.setUnitName(unitName);
				sub.setPartNo(partNumber);
				sub.setPartName(partName);
				sub.setStandard(unit);
				sub.setMaker(maker);
				sub.setCustomer(customer);
				sub.setQuantity(String.valueOf(quantity));
				sub.setUnit(unit);
				sub.setPrice(String.valueOf(price));
				sub.setCurrency(currency);
				sub.setWon((double) won);
				sub.setExchangeRate(rate);
				sub.setReferDrawing(ref);
				sub.setClassification(category);
				sub.setNote(note);
				sub = (UnitSubPart) PersistenceHelper.manager.save(sub);

				totalPrice += (int) won;

				PersistenceHelper.manager.save(sub);

				UnitBomPartLink link = UnitBomPartLink.newUnitBomPartLink(unitBom, sub);
				PersistenceHelper.manager.save(link);

				list.add(sub);
			}

			unitBom.setPrice(String.valueOf(totalPrice));
			PersistenceHelper.manager.modify(unitBom);

			ErpHelper.manager.sendToUnitBom(unitBom, list);

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}

	}

}
