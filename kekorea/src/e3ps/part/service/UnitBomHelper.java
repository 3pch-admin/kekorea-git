package e3ps.part.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import e3ps.common.util.CommonUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.part.UnitBom;
import e3ps.part.UnitBomPartLink;
import e3ps.part.UnitSubPart;
import e3ps.part.dto.UnitBomDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.query.ClassAttribute;
import wt.query.KeywordExpression;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.SubSelectExpression;
import wt.services.ServiceFactory;

public class UnitBomHelper {

	public static final UnitBomHelper manager = new UnitBomHelper();
	public static final UnitBomService service = ServiceFactory.getService(UnitBomService.class);

	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();
		ArrayList<UnitBomDTO> list = new ArrayList<>();
		String uCode = (String) params.get("uCode");
		String yCode = (String) params.get("yCode");
		String uSpec = (String) params.get("uSpec");
		String ySpec = (String) params.get("ySpec");
		String uPartName = (String) params.get("uPartName");
		String yPartName = (String) params.get("yPartName");

		QuerySpec query = new QuerySpec();
//		SearchCondition sc = null;
//		ClassAttribute ca = null;

		int idx = query.appendClassList(UnitBom.class, true);
		query.setAdvancedQueryEnabled(true);

		QuerySpecUtils.toLikeAnd(query, idx, UnitBom.class, UnitBom.U_CODE, uCode);
		QuerySpecUtils.toLikeAnd(query, idx, UnitBom.class, UnitBom.SPEC, uSpec);
		QuerySpecUtils.toLikeAnd(query, idx, UnitBom.class, UnitBom.PART_NAME, uPartName);

		if (!StringUtils.isNull(yCode) || !StringUtils.isNull(ySpec) || !StringUtils.isNull(yPartName)) {
			QuerySpec subQs = searchSubQuery(yCode, ySpec, yPartName);
			SubSelectExpression subfrom = new SubSelectExpression(subQs);
			subfrom.setFromAlias(new String[] { "C0" }, 0);

			int subIndex = query.appendFrom(subfrom);

			if (query.getConditionCount() > 0)
				query.appendAnd();

			SearchCondition sc2 = new SearchCondition(
					new ClassAttribute(UnitBom.class, "thePersistInfo.theObjectIdentifier.id"), "=",
					new KeywordExpression(query.getFromClause().getAliasAt(subIndex) + ".IDA3A5"));

			sc2.setFromIndicies(new int[] { idx, subIndex }, 0);
			sc2.setOuterJoin(0);
			query.appendWhere(sc2, new int[] { idx, subIndex });
		}

		QuerySpecUtils.toOrderBy(query, idx, UnitBom.class, UnitBom.CREATE_TIMESTAMP, true);

		System.out.println(query);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			UnitBom unit = (UnitBom) obj[0];
			UnitBomDTO column = new UnitBomDTO(unit);
			list.add(column);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	private QuerySpec searchSubQuery(String yCode, String ySpec, String yPartName) throws Exception {
		QuerySpec subQs = new QuerySpec();
		int idx = subQs.appendClassList(UnitSubPart.class, false);
		int link_idx = subQs.appendClassList(UnitBomPartLink.class, false);

		subQs.setDistinct(true);
		subQs.setAdvancedQueryEnabled(true);
		subQs.appendSelect(new ClassAttribute(UnitBomPartLink.class, "roleAObjectRef.key.id"), false);

		SearchCondition sc = new SearchCondition(
				new ClassAttribute(UnitSubPart.class, "thePersistInfo.theObjectIdentifier.id"), "=",
				new ClassAttribute(UnitBomPartLink.class, "roleBObjectRef.key.id"));
		sc.setOuterJoin(0);
		subQs.appendWhere(sc, new int[] { idx, link_idx });

		if (!StringUtils.isNull(yCode)) {
			if (subQs.getConditionCount() > 0)
				subQs.appendAnd();

			SearchCondition sc2 = new SearchCondition(UnitSubPart.class, UnitSubPart.PART_NO, SearchCondition.LIKE,
					"%" + yCode.toUpperCase() + "%");
			subQs.appendWhere(sc2, new int[] { idx });
		}

		if (!StringUtils.isNull(ySpec)) {
			if (subQs.getConditionCount() > 0)
				subQs.appendAnd();

			SearchCondition sc2 = new SearchCondition(UnitSubPart.class, UnitSubPart.STANDARD, SearchCondition.LIKE,
					"%" + ySpec.toUpperCase() + "%");
			subQs.appendWhere(sc2, new int[] { idx });
		}

		if (!StringUtils.isNull(yPartName)) {
			if (subQs.getConditionCount() > 0)
				subQs.appendAnd();

			SearchCondition sc2 = new SearchCondition(UnitSubPart.class, UnitSubPart.PART_NAME, SearchCondition.LIKE,
					"%" + yPartName.toUpperCase() + "%");
			subQs.appendWhere(sc2, new int[] { idx });
		}

		return subQs;
	}

	/**
	 * UNIT BOM 트리
	 */
	public JSONArray tree(Map<String, Object> params) throws Exception {
		String oid = (String) params.get("oid");
		UnitBom unitBom = (UnitBom) CommonUtils.getObject(oid);

		JSONArray jsonArray = new JSONArray();
		JSONObject rootNode = new JSONObject();
		rootNode.put("oid", unitBom.getPersistInfo().getObjectIdentifier().getStringValue());
		rootNode.put("partNumber", unitBom.getUCode());
		rootNode.put("partName", unitBom.getPartName());
		rootNode.put("spec", unitBom.getSpec());
		rootNode.put("maker", unitBom.getMaker());
		rootNode.put("unit", unitBom.getUnit());
		rootNode.put("customer", unitBom.getCustomer());

		JSONArray jsonChildren = new JSONArray();

		QueryResult qr = PersistenceHelper.manager.navigate(unitBom, "subPart", UnitBomPartLink.class, false);
		while (qr.hasMoreElements()) {
			UnitBomPartLink link = (UnitBomPartLink) qr.nextElement();
			UnitSubPart sp = link.getSubPart();
			JSONObject node = new JSONObject();
			node.put("oid", sp.getPersistInfo().getObjectIdentifier().getStringValue());
			node.put("partNumber", sp.getPartNo());
			node.put("partName", sp.getPartName());
			node.put("spec", sp.getStandard());
			node.put("quantity", sp.getQuantity());
			node.put("maker", sp.getMaker());
			node.put("unit", sp.getUnit());
			node.put("customer", sp.getCustomer());
			node.put("lotNo", sp.getLotNo());
			node.put("unitName", sp.getUnitName());
			node.put("unit", sp.getUnit());
			node.put("price", sp.getPrice());
			node.put("currency", sp.getCurrency());
			node.put("won", sp.getWon());
			node.put("category", sp.getClassification());
			node.put("note", sp.getNote());
			jsonChildren.add(node);
		}
		rootNode.put("children", jsonChildren);

		jsonArray.add(rootNode);
		return jsonArray;

	}
}
