package e3ps.korea.cip.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import e3ps.admin.commonCode.CommonCode;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.korea.cip.Cip;
import e3ps.korea.cip.dto.CipDTO;
import e3ps.project.Project;
import net.sf.json.JSONArray;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.query.QuerySpec;
import wt.services.ServiceFactory;

public class CipHelper {

	public static final CipHelper manager = new CipHelper();
	public static final CipService service = ServiceFactory.getService(CipService.class);

	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		String item = (String) params.get("item");
		String improvements = (String) params.get("improvements");
		String improvement = (String) params.get("improvement");
		String apply = (String) params.get("apply");
		String mak = (String) params.get("mak");
		String detail = (String) params.get("detail");
		String install = (String) params.get("install");
		String customer = (String) params.get("customer");
		String creatorId = (String) params.get("creatorId");
		String createdFrom = (String) params.get("createdFrom");
		String createdTo = (String) params.get("createdTo");
		String note = (String) params.get("note");

		List<CipDTO> list = new ArrayList<CipDTO>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Cip.class, true);

		QuerySpecUtils.toLikeAnd(query, idx, Cip.class, Cip.ITEM, item);
		QuerySpecUtils.toLikeAnd(query, idx, Cip.class, Cip.IMPROVEMENTS, improvements);
		QuerySpecUtils.toLikeAnd(query, idx, Cip.class, Cip.IMPROVEMENT, improvement);
		QuerySpecUtils.toEqualsAnd(query, idx, Cip.class, Cip.APPLY, apply);
		QuerySpecUtils.toTimeGreaterAndLess(query, idx, Cip.class, Cip.CREATE_TIMESTAMP, createdFrom, createdTo);
		QuerySpecUtils.toLikeAnd(query, idx, Cip.class, Cip.NOTE, note);
		QuerySpecUtils.toCreator(query, idx, Cip.class, creatorId);
		
		if (!StringUtils.isNull(mak)) {
			CommonCode makCode = (CommonCode) CommonUtils.getObject(mak);
			QuerySpecUtils.toEqualsAnd(query, idx, Cip.class, "makReference.key.id",
					makCode.getPersistInfo().getObjectIdentifier().getId());
		}

		if (!StringUtils.isNull(detail)) {
			CommonCode detailCode = (CommonCode) CommonUtils.getObject(detail);
			QuerySpecUtils.toEqualsAnd(query, idx, Cip.class, "detailReference.key.id",
					detailCode.getPersistInfo().getObjectIdentifier().getId());
		}

		if (!StringUtils.isNull(customer)) {
			CommonCode customerCode = (CommonCode) CommonUtils.getObject(customer);
			QuerySpecUtils.toEqualsAnd(query, idx, Cip.class, "customerReference.key.id",
					customerCode.getPersistInfo().getObjectIdentifier().getId());
		}

		if (!StringUtils.isNull(install)) {
			CommonCode installCode = (CommonCode) CommonUtils.getObject(install);
			QuerySpecUtils.toEqualsAnd(query, idx, Cip.class, "installReference.key.id",
					installCode.getPersistInfo().getObjectIdentifier().getId());
		}

		QuerySpecUtils.toOrderBy(query, idx, Cip.class, Cip.CREATE_TIMESTAMP, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();

		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Cip cip = (Cip) obj[0];
			CipDTO column = new CipDTO(cip);
			list.add(column);
		}
		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		return map;
	}

	public JSONArray cipTab(String oid) throws Exception {
		ArrayList<CipDTO> list = new ArrayList<>();
		Project project = (Project) CommonUtils.getObject(oid);
		CommonCode mak = project.getMak();
		CommonCode detail = project.getDetail();
		CommonCode customer = project.getCustomer();
		CommonCode install = project.getInstall();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Cip.class, true);

		query.appendOpenParen();
		QuerySpecUtils.toEqualsAnd(query, idx, Cip.class, "makReference.key.id", mak);
		QuerySpecUtils.toEqualsOr(query, idx, Cip.class, "detailReference.key.id", detail);
		QuerySpecUtils.toEqualsOr(query, idx, Cip.class, "customerReference.key.id", customer);
		QuerySpecUtils.toEqualsOr(query, idx, Cip.class, "installReference.key.id", install);
		query.appendCloseParen();
		QuerySpecUtils.toOrderBy(query, idx, Cip.class, Cip.CREATE_TIMESTAMP, false);

		QueryResult result = PersistenceHelper.manager.find(query);
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			Cip cip = (Cip) obj[0];
			list.add(new CipDTO(cip));
		}
		return JSONArray.fromObject(list);
	}
}
