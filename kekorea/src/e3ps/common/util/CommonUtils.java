package e3ps.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Enumeration;

import e3ps.doc.meeting.Meeting;
import e3ps.doc.request.RequestDocument;
import e3ps.org.People;
import e3ps.org.PeopleWTUserLink;
import e3ps.project.output.Output;
import wt.access.NotAuthorizedException;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTValuedHashMap;
import wt.iba.definition.IBADefinitionException;
import wt.inf.container.WTContainerRef;
import wt.inf.library.WTLibrary;
import wt.introspection.ClassInfo;
import wt.introspection.WTIntrospector;
import wt.org.WTUser;
import wt.ownership.Ownership;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.query.KeywordExpression;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.VersionReference;
import wt.vc.Versioned;
import wt.vc.views.ViewManageable;

public class CommonUtils {

	private static ReferenceFactory rf = null;
	private static final String ADMIN_GROUP = "Administrators";

	private CommonUtils() {

	}

	public static RevisionControlled getLatestObject(Master master) throws WTException {
		return getLatestObject(master, null);
	}

	public static RevisionControlled getLatestObject(Master master, String _viewName) throws WTException {
		RevisionControlled rc = null;
		QueryResult qr = wt.vc.VersionControlHelper.service.allVersionsOf(master);

		while (qr.hasMoreElements()) {
			RevisionControlled obj = ((RevisionControlled) qr.nextElement());

			if (_viewName != null) {
				if (!_viewName.equals(((ViewManageable) obj).getViewName()))
					continue;
			}

			if (rc == null
					|| obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries())) {
				rc = obj;
			}
		}
		if (rc != null)
			return (RevisionControlled) VersionControlHelper.getLatestIteration(rc, false);
		else
			return rc;
	}

	/**
	 * 최고 관리자 확인 함수
	 */
	public static boolean isSupervisor() throws Exception {
		WTUser user = sessionUser();
		return isSupervisor(user);
	}

	/**
	 * 최고 관리자 확인 함수
	 */
	private static boolean isSupervisor(WTUser user) throws Exception {
		String id = user.getName();
		if ("Administrator".equals(id)) {
			return true;
		}
		return false;
	}

	/**
	 * 관리자 그룹인지 확인 하는 함수
	 */
	public static boolean isAdmin() throws Exception {
		return isMember(ADMIN_GROUP);
	}

	/**
	 * 관리자 인지 확인 하는 함수
	 */
	public static boolean isMember(String group) throws Exception {
		WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
		return isMember(group, user);
	}

	/**
	 * 관리자 인지 확인 하는 함수
	 */
	public static boolean isMember(String group, WTUser user) throws Exception {
		Enumeration en = user.parentGroupNames();
		while (en.hasMoreElements()) {
			String st = (String) en.nextElement();
			if (st.equals(group)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 제품 컨테이너 가져오기
	 */
	public static WTContainerRef getPDMLinkProductContainer() throws Exception {
		QuerySpec query = new QuerySpec(PDMLinkProduct.class);
		SearchCondition sc = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, "=", "Commonspace");
		query.appendWhere(sc, new int[] { 0 });
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			PDMLinkProduct pdmLinkProduct = (PDMLinkProduct) result.nextElement();
			return WTContainerRef.newWTContainerRef(pdmLinkProduct);
		}
		return null;
	}

	/**
	 * 라이브러리 컨테이너 가져오기
	 */
	public static WTContainerRef getWTLibraryContainer() throws Exception {
		QuerySpec query = new QuerySpec(WTLibrary.class);
		SearchCondition sc = new SearchCondition(WTLibrary.class, WTLibrary.NAME, "=", "LIBRARY");
		query.appendWhere(sc, new int[] { 0 });
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			WTLibrary wtLibrary = (WTLibrary) result.nextElement();
			return WTContainerRef.newWTContainerRef(wtLibrary);
		}
		return null;
	}

	/**
	 * 최신 버전의 객체 인지 확인 하는 함수
	 * 
	 * @param oid : 최신 버전을 확인할 객체의 OID
	 * @return boolean
	 * @throws Exception
	 */
	public static boolean isLatestVersion(String oid) throws Exception {
		ReferenceFactory rf = new ReferenceFactory();
		Persistable persistable = (Persistable) rf.getReference(oid).getObject();
		return isLatestVersion(persistable);
	}

	/**
	 * 최신 버전의 객체 인지 확인 하는 함수
	 * 
	 * @param persistable : 최신 버전을 확인할 객체
	 * @return boolean
	 * @throws Exception
	 */
	public static boolean isLatestVersion(Persistable persistable) throws Exception {
		WTHashSet set = new WTHashSet();
		set.add(persistable);

		WTValuedHashMap map = (WTValuedHashMap) VersionControlHelper.service.getLatestRevisions(set);
		VersionReference reference = (VersionReference) map.get(persistable);

		boolean isLatestRevision = reference.references(persistable);
		boolean isLatestIteration = VersionControlHelper.isLatestIteration((Iterated) persistable);

		return (isLatestIteration && isLatestRevision);
	}

	/**
	 * 최신 버전의 객체를 가져오는 함수
	 * 
	 * @param oid : 최신 버전의 객체를 가져올 객체의 OID
	 * @return Persistable
	 * @throws Exception
	 */
	public static Persistable getLatestVersion(String oid) throws Exception {
		ReferenceFactory rf = new ReferenceFactory();
		Persistable persistable = (Persistable) rf.getReference(oid).getObject();
		WTHashSet set = new WTHashSet();
		set.add(persistable);

		WTValuedHashMap map = (WTValuedHashMap) VersionControlHelper.service.getLatestRevisions(set);
		VersionReference reference = (VersionReference) map.get(persistable);
		return reference.getObject();
	}

	/**
	 * 최신 버전의 객체를 가져오는 함수
	 * 
	 * @param persistable : 최신 버전의 객체를 가져올 객체
	 * @return Persistable
	 * @throws Exception
	 */
	public static Persistable getLatestVersion(Persistable persistable) throws Exception {
		WTHashSet set = new WTHashSet();
		set.add(persistable);

		WTValuedHashMap map = (WTValuedHashMap) VersionControlHelper.service.getLatestRevisions(set);
		VersionReference reference = (VersionReference) map.get(persistable);
		return reference.getObject();
	}

	/**
	 * 객체를 가져오는 함수
	 */
	public static Persistable getObject(String oid) throws Exception {
		if (oid != null && oid.length() > 0) {
			if (rf == null) {
				rf = new ReferenceFactory();
			}
			return rf.getReference(oid).getObject();
		}
		return null;
	}

	/**
	 * 객체의 버전을 가져오는 함수
	 */
	public static String getVersion(RevisionControlled rc) throws Exception {
		return rc.getVersionIdentifier().getSeries().getValue();
	}

	/**
	 * 객체의 이터레이션을 가져오는 함수
	 * 
	 * @param rc : 이터레이션을 가져올 객체
	 * @return String
	 * @throws Exception
	 */
	public static String getIteration(RevisionControlled rc) throws Exception {
		return rc.getIterationIdentifier().getSeries().getValue();
	}

	/**
	 * 객체의 버전+이터레이션을 가져오는 함수
	 * 
	 * @param rc : 버전+이터레이션을 가져올 객체
	 * @return String
	 * @throws Exception
	 */
	public static String getFullVersion(RevisionControlled rc) throws Exception {
		return rc.getVersionIdentifier().getSeries().getValue() + "."
				+ rc.getIterationIdentifier().getSeries().getValue();
	}

	/**
	 * 작성일, 수정일 등 시간에 대한 값을 String 형태로 변경하는 함수
	 */
	public static String getPersistableTime(Timestamp time) throws Exception {
		if (time == null) {
			return "";
		}
		return getPersistableTime(time, 10);
	}

	/**
	 * 작성일, 수정일 등 시간에 대한 값을 String 형태로 변경하는 함수
	 */
	public static String getPersistableTime(Timestamp time, int index) throws Exception {
		if (time == null) {
			return "";
		}
		return time.toString().substring(0, index);
	}

	/**
	 * 접속한 사용자의 Ownership 객체를 가져오는 함수
	 */
	public static Ownership sessionOwner() throws Exception {
		return Ownership.newOwnership(SessionHelper.manager.getPrincipal());
	}

	/**
	 * 접속한 사용자의 WTUser 객체를 가져오는 함수
	 */
	public static WTUser sessionUser() throws Exception {
		return (WTUser) SessionHelper.manager.getPrincipal();
	}

	/**
	 * 접속한 사용자의 PEOPEL 객체 리턴
	 */
	public static People sessionPeople() throws Exception {
		WTUser sessionUser = sessionUser();
		QueryResult result = PersistenceHelper.manager.navigate(sessionUser, "people", PeopleWTUserLink.class);
		if (result.hasMoreElements()) {
			People people = (People) result.nextElement();
			return people;
		}
		return null;
	}

	/**
	 * 접속한 사용자가 RevisionControlled 객체의 작성자인지 확인 하는 함수
	 */
	public static boolean isCreator(RevisionControlled rc) throws Exception {
		WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
		return isCreator(rc, sessionUser);
	}

	/**
	 * WTUser 객체가 RevisionControlled 객체의 작성자인지 확인 하는 함수
	 */
	public static boolean isCreator(RevisionControlled rc, WTUser sessionUser) throws Exception {
		return rc.getCreatorName().equals(sessionUser.getName());
	}

	public boolean isLastVersion(Versioned versioned) {
		try {

			Class cls = null;

			if (versioned instanceof WTPart) {
				cls = WTPart.class;
			} else if (versioned instanceof EPMDocument) {
				cls = EPMDocument.class;
			} else if (versioned instanceof WTDocument) {
				cls = WTDocument.class;
			} else if (versioned instanceof RequestDocument) {
				cls = WTDocument.class;
			} else if (versioned instanceof Meeting) {
				cls = WTDocument.class;
			} else if (versioned instanceof Output) {
				cls = WTDocument.class;
			} else {
				return false;
			}
			long longOid = 0;// CommonUtils.getOIDLongValue(versioned);

			QuerySpec qs = new QuerySpec();

			int idx = qs.appendClassList(cls, true);

			// 최신 이터레이션
			qs.appendWhere(VersionControlHelper.getSearchCondition(cls, true), new int[] { idx });

			// 최신 버젼
			addLastVersionCondition(qs, cls, idx);

			qs.appendAnd();
			qs.appendWhere(
					new SearchCondition(cls, "thePersistInfo.theObjectIdentifier.id", SearchCondition.EQUAL, longOid),
					new int[] { idx });

			QueryResult rt = PersistenceHelper.manager.find(qs);

			if (rt.size() > 0)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static void addLastVersionCondition(QuerySpec _query, Class _target, int _idx) throws IBADefinitionException,
			NotAuthorizedException, RemoteException, WTException, QueryException, WTPropertyVetoException {
		ClassInfo var2 = WTIntrospector.getClassInfo(_target);
		String tableName = var2.getDatabaseInfo().getBaseTableInfo().getTablename();
		// VERSIONSORTIDA2VERSIONINFO
		String columnName = var2.getDatabaseInfo().getBaseTableInfo()
				.getColumnDescriptor("versionInfo.identifier.versionSortId").getColumnName();
		// System.out.println("#######@@@@@@@@>>>>>>>>>tableName:"+tableName);
		// System.out.println("#######@@@@@@@@>>>>>>>>>columnName:"+columnName);
		if (_query.getConditionCount() > 0) {
			_query.appendAnd();
		}
		_query.appendWhere(new SearchCondition(
				new KeywordExpression(_query.getFromClause().getAliasAt(_idx) + "." + columnName), "=",
				new KeywordExpression("(SELECT max(" + columnName + ") FROM " + tableName + " WHERE "
						+ _query.getFromClause().getAliasAt(_idx) + ".IDA3MASTERREFERENCE = IDA3MASTERREFERENCE)")));
		/*
		 * AttributeDefDefaultView aview =
		 * IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(
		 * "LatestVersionFlag"); if (aview != null) { if (_query.getConditionCount() >
		 * 0) _query.appendAnd();
		 * 
		 * int idx = _query.appendClassList(StringValue.class, false); SearchCondition
		 * sc = new SearchCondition(new ClassAttribute(StringValue.class,
		 * "theIBAHolderReference.key.id"), "=", new ClassAttribute( _target,
		 * "thePersistInfo.theObjectIdentifier.id")); sc.setFromIndicies(new int[] {
		 * idx, _idx }, 0); sc.setOuterJoin(0); _query.appendWhere(sc, new int[] { idx,
		 * _idx }); _query.appendAnd(); sc = new SearchCondition(StringValue.class,
		 * "definitionReference.key.id", "=", aview.getObjectID().getId());
		 * _query.appendWhere(sc, new int[] { idx }); _query.appendAnd(); sc = new
		 * SearchCondition(StringValue.class, "value", "=", "TRUE");
		 * _query.appendWhere(sc, new int[] { idx }); }
		 */
	}

	public static long getOIDLongValue(String oid) {
		String tempoid = oid;
		tempoid = tempoid.substring(tempoid.lastIndexOf(":") + 1);
		return Long.parseLong(tempoid);
	}

	public static long getOIDLongValue(Persistable per) {
		String tempoid = getOIDString(per);
		tempoid = tempoid.substring(tempoid.lastIndexOf(":") + 1);
		return Long.parseLong(tempoid);
	}

	public static String getOIDString(Persistable per) {
		if (per == null)
			return null;
		return PersistenceHelper.getObjectIdentifier(per).getStringValue();
	}

	// 파일복사
	public static File copyFile(File source, File dest) {
		long startTime = System.currentTimeMillis();

		int count = 0;
		long totalSize = 0;
		byte[] b = new byte[128];

		FileInputStream in = null;
		FileOutputStream out = null;
		// 성능향상을 위한 버퍼 스트림 사용
		BufferedInputStream bin = null;
		BufferedOutputStream bout = null;
		try {
			in = new FileInputStream(source);
			bin = new BufferedInputStream(in);

			out = new FileOutputStream(dest);
			bout = new BufferedOutputStream(out);
			while ((count = bin.read(b)) != -1) {
				bout.write(b, 0, count);
				totalSize += count;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {// 스트림 close 필수
			try {
				if (bout != null) {
					bout.close();
				}
				if (out != null) {
					out.close();
				}
				if (bin != null) {
					bin.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException r) {
				// System.out.println("close 도중 에러 발생.");
			}
		}
		return dest;
	}
}