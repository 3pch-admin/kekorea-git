package e3ps.erp.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;

import e3ps.bom.partlist.PartListData;
import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.PartListMasterProjectLink;
import e3ps.bom.partlist.service.PartlistHelper;
import e3ps.common.db.DBCPManager;
import e3ps.common.util.IBAUtils;
import e3ps.common.util.PageQueryUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.epm.service.EpmHelper;
import e3ps.erp.ErpConnectionPool;
import e3ps.erp.ErpSendHistory;
import e3ps.erp.dto.ErpDTO;
import e3ps.part.UnitBom;
import e3ps.part.UnitBomPartLink;
import e3ps.part.UnitSubPart;
import e3ps.part.service.PartHelper;
import e3ps.project.Project;
import e3ps.project.output.Output;
import e3ps.project.output.OutputDocumentLink;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.PagingQueryResult;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.services.ServiceFactory;
import wt.session.SessionHelper;
import wt.util.FileUtil;

public class ErpHelper {

	public static final boolean isOperation = true;

	public static final ErpService service = ServiceFactory.getService(ErpService.class);
	public static final ErpHelper manager = new ErpHelper();

	private static final BasicDataSource dataSource = ErpConnectionPool.getDataSource();

	public static final boolean isErpSend = true;

	/**
	 * ERP 물리 파일 전송 위치 변수
	 */
	private static final String erpOutputDir = File.separator + "\\Erp-app\\plm2";
	private static final String epmOutputDir = File.separator + "\\Erp-app\\plm";

	/**
	 * 캐시 처리
	 */
//	public static HashMap<String, Map<String, Object>> cacheManager = null;
//	public static HashMap<String, Map<String, Object>> validateCache = null;
//	public static HashMap<Integer, Map<String, Object>> unitCache = null;
	static {
//		if (cacheManager == null) {
//			cacheManager = new HashMap<>();
//		}
//
//		if (validateCache == null) {
//			validateCache = new HashMap<>();
//		}
//
//		if (unitCache == null) {
//			unitCache = new HashMap<>();
//		}
	}

	/**
	 * ERP 큐
	 */
	private static final String processQueueName = "sendToErpProcessQueue";
	private static final String className = "e3ps.erp.service.ErpHelper";
	private static final String methodName = "sendToErpFromQueue";

	/**
	 * YCODE 체크 수배표 등록시
	 */
	public Map<String, Object> validate(String partNo) throws Exception {
		StringBuffer sql = new StringBuffer();
		Map<String, Object> result = new HashMap<String, Object>();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			sql.append("SELECT *");
			sql.append(" FROM KEK_VDAITEM");
			sql.append(" WHERE ITEMNO='" + partNo.trim() + "' AND SMSATAUSNAME != '폐기'");
//			Map<String, Object> cacheData = validateCache.get(partNo);
//			if (cacheData == null || cacheData.get("check") == "NG") {

			con = dataSource.getConnection();
			st = con.createStatement();

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				result.put("check", "OK");
//					validateCache.put(partNo, result);
			} else {
				result.put("check", "NG");
//					validateCache.put(partNo, result);
			}
//			} else {
//				System.out.println("PART NO VALIDATE CACHE");
//				result = cacheData;
//			}
			ErpHelper.service.writeLog("수배표 YCODE 체크", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 YCODE 체크", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return result;
	}

	/**
	 * 수배표 UNITNAME 가져오기
	 */
	public Map<String, Object> getUnitName(int lotNo, String callLoc) throws Exception {
		Map<String, Object> result = new HashMap<>();
		StringBuffer sql = new StringBuffer();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {

//			Map<String, Object> cacheData = unitCache.get(lotNo);
			sql.append("SELECT LOTUNITNAME FROM KEK_VDALOTNO WHERE LOTNO='" + lotNo + "'");
//			if (cacheData == null) {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sql.toString());

			if (rs.next()) {
				result.put("unitName", (String) rs.getString(1));
//					unitCache.put(lotNo, result);
			} else {
				result.put("unitName", "ERP에 등록되지 않은 LOT NO입니다.");
			}
//			} else {
//				result = cacheData;
//			}

			ErpHelper.service.writeLog("LOT번호로 UNIT NAME 가져오기", sql.toString(), "", true, callLoc);
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("LOT번호로 UNIT NAME 가져오기", sql.toString(), e.toString(), false, callLoc);
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return result;
	}

	/**
	 * 수배표 부품정보 가져오기
	 */
	public Map<String, Object> getErpItemByPartNoAndQuantity(String partNo, int quantity) throws Exception {
		StringBuffer sql = new StringBuffer();
		Map<String, Object> result = new HashMap<>();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		ResultSet _rs = null;
		try {

//			String cacheKey = partNo + quantity;
//			Map<String, Object> cacheData = cacheManager.get(cacheKey);

			sql.append("SELECT ITEMSEQ, ITEMNAME, SPEC");
			sql.append(" FROM KEK_VDAITEM");
			sql.append(" WHERE ITEMNO='" + partNo.trim() + "' AND SMSATAUSNAME != '폐기'");
//			if (cacheData == null) {
			con = dataSource.getConnection();
			st = con.createStatement();

			rs = st.executeQuery(sql.toString());

			if (rs.next()) {
				int itemSeq = (int) rs.getInt(1);
				String itemName = (String) rs.getString(2);
				String spec = (String) rs.getString(3);

				StringBuffer sb = new StringBuffer();
				sb.append("EXEC KEK_SPLMBASEGETPRICE '" + itemSeq + "', '', '" + quantity + "'");
				_rs = st.executeQuery(sb.toString());

				String maker = "";
				String customer = "";
				String unit = "";
				String currency = "";
				int price = 0;
				double exchangeRate = 0;
				if (_rs.next()) {
					maker = (String) _rs.getString("makerName");
					customer = (String) _rs.getString("custName");
					unit = (String) _rs.getString("unitName");
					currency = (String) _rs.getString("currName");
					price = (int) _rs.getInt("price");
					System.out.println("rpcie=" + price);
					exchangeRate = (double) _rs.getDouble("exRate");

				}
				result.put("maker", maker);
				result.put("customer", customer);
				result.put("unit", unit);
				result.put("currency", currency);
				result.put("price", price);
				result.put("exchangeRate", exchangeRate);
				result.put("standard", spec);
				result.put("partName", itemName);
				result.put("won", quantity * price * exchangeRate);
				result.put("check", "OK");
				result.put("quantity", quantity);
//					cacheManager.put(cacheKey, result);
			} else {
				result.put("check", "NG");
//					cacheManager.put(cacheKey, result);
			}
//			} else {
//				System.out.println("수배표 등록 CACHE");
//				result = cacheData;
//			}
			ErpHelper.service.writeLog("수배표 부품 정보 가져오기 가져오기", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 부품 정보 가져오기 가져오기", sql.toString(), e.toString(), false, "수배표");
			if (con != null) {
				con.close();
			}

			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}

			if (_rs != null) {
				_rs.close();
			}
			throw e;
		} finally {
			if (con != null) {
				con.close();
			}

			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}

			if (_rs != null) {
				_rs.close();
			}
		}
		return result;
	}

	/**
	 * 규격으로 ERP 부품정보 가져오기
	 */
	public Map<String, Object> getErpItemBySpec(String spec, String callLoc) throws Exception {
		StringBuffer sql = new StringBuffer();
		Map<String, Object> result = new HashMap<String, Object>(); // json
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		ResultSet _rs = null;
		try {

//			con = dataSource.getConnection();
			con = DBCPManager.getConnection("erp");
			st = con.createStatement();

			sql.append("SELECT ITEMSEQ, ITEMNAME, SPEC, ITEMNO");
			sql.append(" FROM KEK_VDAITEM");
			sql.append(" WHERE SPEC='" + spec.trim() + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				int itemSeq = (int) rs.getInt(1);
				String itemName = (String) rs.getString(2);
				String itemNo = (String) rs.getString(4);

				StringBuffer sb = new StringBuffer();
				sb.append("EXEC KEK_SPLMBASEGETPRICE '" + itemSeq + "', '', '1'");

				_rs = st.executeQuery(sb.toString());

				String maker = "";
				String customer = "";
				String unit = "";
				String currency = "";
				int price = 0;
				if (_rs.next()) {
					maker = (String) _rs.getString("makerName");
					customer = (String) _rs.getString("custName");
					unit = (String) _rs.getString("unitName");
					currency = (String) _rs.getString("currName");
					price = (int) _rs.getInt("price");
				}

				result.put("itemName", itemName);
				result.put("itemNo", itemNo);
				result.put("maker", maker);
				result.put("customer", customer);
				result.put("unit", unit);
				result.put("price", price);
				result.put("currency", currency);
				System.out.println(result);
			}
			ErpHelper.service.writeLog("규격으로 ERP 부품 정보 가져오기 가져오기", sql.toString(), "", true, callLoc);
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("규격으로 ERP 부품 정보 가져오기 가져오기", sql.toString(), e.toString(), false, callLoc);
			if (con != null) {
				con.close();
			}

			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}

			if (_rs != null) {
				_rs.close();
			}
			throw e;
		} finally {
			if (con != null) {
				con.close();
			}

			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}

			if (_rs != null) {
				_rs.close();
			}
		}
		return result;
	}

	/**
	 * PDM에 등록된 데이터 YCODE로 ERP에서 추가 정보 가져오기
	 */
	public Map<String, Object> getErpItemByPartNo(String partNo, String callLoc) throws Exception {
		Map<String, Object> result = new HashMap<>();
		StringBuffer sql = new StringBuffer();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		ResultSet _rs = null;
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT ITEMSEQ, ITEMNAME, SPEC");
			sql.append(" FROM KEK_VDAITEM");
			sql.append(" WHERE ITEMNO='" + partNo.trim() + "' AND SMSATAUSNAME != '폐기'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				int itemSeq = (int) rs.getInt(1);
				String itemName = (String) rs.getString(2);
				String spec = (String) rs.getString(3);

				StringBuffer sb = new StringBuffer();
				sb.append("EXEC KEK_SPLMBASEGETPRICE '" + itemSeq + "', '', '1'");
				_rs = st.executeQuery(sb.toString());

				String maker = "";
				String customer = "";
				String unit = "";
				String currency = "";
				int price = 0;
				double exchangeRate = 0;
				if (_rs.next()) {
					maker = (String) _rs.getString("makerName");
					customer = (String) _rs.getString("custName");
					unit = (String) _rs.getString("unitName");
					currency = (String) _rs.getString("currName");
					price = (int) _rs.getInt("price");
//					System.out.println("rate=" + _rs.getShort("exRate"));
					exchangeRate = (double) _rs.getDouble("exRate");
				}
				result.put("maker", maker);
				result.put("customer", customer);
				result.put("unit", unit);
				result.put("currency", currency);
				result.put("price", price);
				result.put("exchangeRate", exchangeRate);
				result.put("standard", spec);
				result.put("partName", itemName);
			}
			ErpHelper.service.writeLog(" PDM에 등록된 데이터 YCODE로 ERP에서 추가 정보 가져오기 ", sql.toString(), "", true, callLoc);
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog(" PDM에 등록된 데이터 YCODE로 ERP에서 추가 정보 가져오기 ", sql.toString(), e.toString(), false,
					callLoc);
			if (con != null) {
				con.close();
			}

			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}

			if (_rs != null) {
				_rs.close();
			}
			throw e;
		} finally {
			if (con != null) {
				con.close();
			}

			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}

			if (_rs != null) {
				_rs.close();
			}
		}
		return result;
	}

	/**
	 * 산출물 ERP 전송
	 */
	public void sendToErp(WTDocument document) throws Exception {
		String errorQuery = "";
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {

			con = DBCPManager.getConnection("erp");
			st = con.createStatement();
			con = dataSource.getConnection();
//			st = con.createStatement();
//			con.setAutoCommit(false);

			QueryResult result = PersistenceHelper.manager.navigate(document, "output", OutputDocumentLink.class);
			while (result.hasMoreElements()) {
				Output output = (Output) result.nextElement();
				Project project = output.getProject();
				String loc = output.getLocation();

				System.out.println("loc=" + loc);

				// 산출물이 아니면 패스
				if (skip(loc)) {
					continue;
				}

				StringBuffer sql = new StringBuffer();

				sql.append("INSERT INTO KEK_TPJTOUTPUTRPTDO_IF (SEQ, STDNO, PJTSEQ, STDREPORTSEQ, REGDATE, USERID, ");
				sql.append("REMARK, CREATE_TIME)");

				int seq = getMaxSequence("KEK_TPJTOUTPUTRPTDO_IF", "산출물");
				sql.append(" VALUES('" + seq + "', ");

				String stdNo = document.getNumber();
				String projectType = project.getProjectType().getName();

				int stdReportSeq = 1;

				if (loc.contains("기계_작업지시서")) {
					stdReportSeq = 48;
					stdNo = "INS-" + stdNo;
				} else if(loc.contains("전기_작업지시서")) {
					stdReportSeq = 47;
					stdNo = "INS-" + stdNo;
				} else if(loc.contains("SW_지시서")) {
					stdReportSeq = 49;
					stdNo = "INS-" + stdNo;
				} else if (loc.contains("설비사양서")) {
					if (projectType.equals("개조")) {
						stdReportSeq = 53;
						stdNo = "REQ1-" + stdNo;
					} else if (projectType.equals("양산")) {
						stdReportSeq = 54;
						stdNo = "REQ2-" + stdNo;
					}
				} else if (loc.contains("장치 Check List") || loc.contains("장치 Check list")) {
					stdReportSeq = 140;
					stdNo = "REQ3-" + stdNo;
				}

				sql.append("'" + stdNo + "', ");

				String kekNumber = project.getKekNumber();
				Map<String, Object> pjtData = ErpHelper.manager.getPjtInfoByKekNumber(kekNumber);

				String pjtSeq = (String) pjtData.get("pjtSeq");

				if (StringUtils.isNull(pjtSeq)) {
					pjtSeq = "0";
				}

				sql.append("'" + Integer.parseInt(pjtSeq) + "', ");
				sql.append("'" + stdReportSeq + "', ");

				String regDate = erpDateStringFormat(document.getCreateTimestamp());
				sql.append("'" + regDate + "', ");

				String userId = document.getCreatorName();
				sql.append("'" + userId + "', ");

				String remark = StringUtils.replaceToValue(document.getDescription());
				sql.append("'" + remark + "', ");

				String createTime = new Timestamp(new Date().getTime()).toString().substring(0, 16);
				sql.append("'" + createTime + "');");

				errorQuery = sql.toString();

				st.executeUpdate(sql.toString());

				sendToErpFile(document, project);

				// 프로시저 실행
				StringBuffer sb = new StringBuffer();
				sb.append("EXEC KEK_SPLMOutputRptDOProc ");
				sb.append("'" + stdNo + "'");
//				st.executeUpdate(sb.toString());
				boolean isResult = st.execute(sb.toString());
				if (!isResult) {
					System.out.println("실패 했나?");
				}

				ErpHelper.service.writeLog("산출물 정보 ERP 전송", sql.toString(), "", true, "산출물");
			}

//			con.commit();

		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("산출물 정보 ERP 전송", errorQuery, e.toString(), false, "산출물");
			con.rollback();
			DBCPManager.freeConnection(con, st, rs);
//			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			DBCPManager.freeConnection(con, st, rs);
//			ErpConnectionPool.free(con, st, rs);
		}
	}

	/**
	 * 프로젝트 산출물 물리파일 ERP 전송
	 */
	private void sendToErpFile(WTDocument document, Project project) throws Exception {
		String errorQuery = "";
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {

			con = dataSource.getConnection();
			st = con.createStatement();
			con.setAutoCommit(false);

			StringBuffer sql = new StringBuffer();

			sql.append(
					"INSERT INTO KEK_TPJTOUTPUTRPTDOFILE_IF (SEQ, STDNO, RPTFILENAME, CREATE_TIME, FILEEXT, FILESIZE)");

			int seq = getMaxSequence("KEK_TPJTOUTPUTRPTDOFILE_IF", "산출물");
			sql.append(" VALUES('" + seq + "', ");

			String stdNo = document.getNumber();
			String loc = document.getLocation();
			String projectType = project.getProjectType().getName();

			if (loc.contains("작업지시서") || loc.contains("SW_지시서")) {
				stdNo = "INS-" + stdNo;
			} else if (loc.contains("설비사양서")) {
				if (projectType.equals("개조")) {
					stdNo = "REQ1-" + stdNo;
				} else if (projectType.equals("양산")) {
					stdNo = "REQ2-" + stdNo;
				}
			} else if (loc.contains("장치 Check List") || loc.contains("장치 Check list")) {
//				stdReportSeq = 140;
				stdNo = "REQ3-" + stdNo;
			}
			sql.append("'" + stdNo + "', ");

			ApplicationData data = null;
			QueryResult result = ContentHelper.service.getContentsByRole(document, ContentRoleType.PRIMARY);
			if (result.hasMoreElements()) {
				data = (ApplicationData) result.nextElement();
			}

			String rptFileName = data.getFileName();
			sql.append("'" + rptFileName + "', ");

			String createTime = new Timestamp(new Date().getTime()).toString().substring(0, 16);
			sql.append("'" + createTime + "', ");

			String ext = FileUtil.getExtension(rptFileName);
			sql.append("'" + ext + "', ");

			long fileSize = data != null ? data.getFileSize() : 0L;
			sql.append("'" + fileSize + "');");

			errorQuery = sql.toString();

			st.executeUpdate(sql.toString());

			ErpHelper.service.writeLog("산출물 물리파일 정보 ERP 전송", sql.toString(), "", true, "산출물");

			// 첨부 파일 전송...
			String dir = erpOutputDir;
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			if (data != null) {
				byte[] buffer = new byte[10240];
				InputStream is = ContentServerHelper.service.findLocalContentStream(data);
				File write = new File(directory + File.separator + data.getFileName());
				FileOutputStream fos = new FileOutputStream(write);
				int j = 0;
				while ((j = is.read(buffer, 0, 10240)) > 0) {
					fos.write(buffer, 0, j);
				}
				fos.close();
				is.close();
			}
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("산출물 물리파일 정보 ERP 전송", errorQuery, e.toString(), false, "산출물");
			con.rollback();
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
	}

	/**
	 * 산출물 여부 확인
	 */
	private boolean skip(String loc) {
		if (!loc.contains("장치 Check List") && !loc.contains("장치 Check list") && !loc.contains("작업지시서")
				&& !loc.contains("설비사양서") && !loc.contains("SW_지시서")) {
			return true;
		}

		return false;
	}

	/**
	 * 테이블 최대 Sequence + 1 값 반환
	 */
	public int getMaxSequence(String table, String callLoc) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT MAX(SEQ) FROM " + table;
		try {

			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1) + 1;
			}
			ErpHelper.service.writeLog("ERP 테이블 Sequence 값 + 1 반환", sql, "", true, callLoc);
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("ERP 테이블 Sequence 값 + 1 반환", sql, e.toString(), false, callLoc);
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * KEK 작번으로 ERP쪽 프로젝트 데이터 가져오기
	 */
	public Map<String, Object> getPjtInfoByKekNumber(String kekNumber) throws Exception {
		Map<String, Object> data = new HashMap<>();
		StringBuffer sql = new StringBuffer();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT PJTSEQ, PJTNAME, PJTNO FROM KEK_VPJTPROJECT WHERE PJTNO='" + kekNumber + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				int pjtSeq = (int) rs.getInt(1);
				String pjtName = (String) rs.getString(2);
				String pjtNo = (String) rs.getString(3);
				data.put("pjtSeq", String.valueOf(pjtSeq));
				data.put("pjtName", pjtName);
				data.put("pjtNo", pjtNo);
			}
			ErpHelper.service.writeLog("ERP 테이블 Sequence 값 + 1 반환", sql.toString(), "", true, "산출물");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("ERP 테이블 Sequence 값 + 1 반환", sql.toString(), e.toString(), false, "산출물");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return data;
	}

	/**
	 * ERP 날짜 전송 포맷
	 */
	private String erpDateStringFormat(Timestamp time) {
		return time.toString().substring(0, 10).replaceAll("-", "").replace("/", "");
	}

	/**
	 * 수배표 전송
	 */
	public void sendToErp(PartListMaster master) throws Exception {
		System.out.println("수배표 전송 START = " + new Timestamp(new Date().getTime()));
		String errorQuery = "";
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = DBCPManager.getConnection("erp");
//			con = dataSource.getConnection();
			st = con.createStatement();

//			con.setAutoCommit(false);
			// 작번 개수 만큼 전송
			QueryResult result = PersistenceHelper.manager.navigate(master, "project", PartListMasterProjectLink.class);

			if (result.size() == 0) {
				ErpConnectionPool.free(con, st, rs);
				return;
			}

			while (result.hasMoreElements()) {

				Project project = (Project) result.nextElement();
				String kekNumber = project.getKekNumber();
				String engType = master.getEngType();

				ArrayList<PartListData> list = PartlistHelper.manager.getPartListData(master);
				String disNo = "WANT_" + master.getNumber();

				System.out.println("수배표 전체 전송 개수 :  " + list.size());

				for (PartListData data : list) {
					StringBuffer sql = new StringBuffer();

					sql.append(
							"INSERT INTO KEK_TPJTBOM_IF (SEQ, DISNO, REGDATE, PJTSEQ, DESIGNTYPE, REMARKM, USERID, ");
					sql.append("ACCDATE, LOTSEQ, ITEMSEQ, MAKERSEQ, CUSTSEQ, UNITSEQ, CURRSEQ, QTY, EXRATE, ");
					sql.append("AMT, REMARK, UMSUPPLYTYPE, PRICE, CREATE_TIME, APPUSERID )");

					int seq = getMaxSequence("KEK_TPJTBOM_IF", "수배표");
					sql.append(" VALUES('" + seq + "', ");
					sql.append("'" + disNo + "', ");

					String accDate = erpDateStringFormat(data.getPartListDate());
					sql.append("'" + accDate.replaceAll("-", "") + "', ");

					Map<String, Object> pjtData = ErpHelper.manager.getPjtInfoByKekNumber(kekNumber);
					String pjtSeq = (String) pjtData.get("pjtSeq");
					sql.append("'" + StringUtils.replaceToValue(pjtSeq) + "', ");
//					sql.append("'" + pjtSeq + "', ");

					if (engType.contains("기계")) {
						engType = "기계";
					} else if (engType.contains("전기")) {
						engType = "전기";
					}

					sql.append("'" + getKekDesignType(engType + "설계") + "', ");

					String remark = StringUtils.replaceToValue(master.getDescription(), " ");
					sql.append("'" + remark + "', ");

					String userId = master.getCreatorName();
					sql.append("'" + userId + "', ");
					sql.append("'" + accDate + "', ");

					int lotNo = data.getLotNo();
					sql.append("'" + getKekLotSeq(String.valueOf(lotNo)) + "', ");

					String partNo = data.getPartNo();
					sql.append("'" + getKekItemSeq(StringUtils.replaceToValue(partNo)) + "', ");

					String makerName = data.getMaker();
					sql.append("'" + getKekMakerSeq(StringUtils.replaceToValue(makerName)) + "', ");

					String customer = data.getCustomer();
					sql.append("'" + getKekCustSeq(StringUtils.replaceToValue(customer)) + "', ");

					String unitName = data.getUnit();
					sql.append("'" + getKekUnitSeq(StringUtils.replaceToValue(unitName)) + "', ");

					String currency = data.getCurrency();
					sql.append("'" + getKekCurrencySeq(StringUtils.replaceToValue(currency)) + "', ");

					int qty = data.getQuantity();
					sql.append("'" + qty + "', ");

					double rate = data.getExchangeRate();

					BigDecimal bWon = new BigDecimal(data.getWon());

					sql.append("'" + rate + "', ");
					sql.append("'" + bWon.toString() + "', ");
					sql.append("'" + StringUtils.replaceToValue(data.getNote(), " ") + "', ");

					String classification = data.getClassification();
					sql.append("'" + getKekSupplySeq(StringUtils.replaceToValue(classification)) + "', ");
					sql.append("'" + data.getPrice() + "', ");
					sql.append("'" + accDate + "', ");

					String lastId = master.getLast();
					sql.append("'" + lastId + "');");

					errorQuery = sql.toString();

					// System.out.println("### erphelper send 수배표.=="+sql.toString());

					st.executeUpdate(sql.toString());
					ErpHelper.service.writeLog("수배표 ERP 전송", sql.toString(), "", true, "수배표");
				}

				StringBuffer sb = new StringBuffer();
				sb.append("EXEC KEK_SPLMBOMIF '" + disNo + "'");
				st.executeUpdate(sb.toString());
			}
//			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 ERP 전송", errorQuery, e.toString(), false, "수배표");
			con.rollback();
			DBCPManager.freeConnection(con, st, rs);
//			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			DBCPManager.freeConnection(con, st, rs);
//			ErpConnectionPool.free(con, st, rs);
		}
		System.out.println("수배표 전송 END = " + new Timestamp(new Date().getTime()));
	}

	/**
	 * ERP LOT NO SEQ 가져오기
	 */
	public int getKekLotSeq(String lotNo) throws Exception {
		StringBuffer sql = new StringBuffer();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT LOTSEQ FROM KEK_VDALOTNO WHERE LOTNO='" + lotNo + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 LOT SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 LOT SEQ", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * ERP 설계 타입 SEQ 가져오기
	 */
	public int getKekDesignType(String engType) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT DESIGNTYPE FROM KEK_VDADESIGNTYPE WHERE DESIGNTYPENAME='" + engType + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 설계타입 SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 설계타입 SEQ", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * ERP 품목 SEQ 가져오기
	 * 
	 * @param yCode
	 * @return
	 * @throws Exception
	 */
	public int getKekItemSeq(String partNo) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT ITEMSEQ");
			sql.append(" FROM KEK_VDAITEM");
			sql.append(" WHERE ITEMNO='" + partNo + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 설계타입 SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 설계타입 SEQ", sql.toString(), "", true, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * ERP 메이커 SEQ
	 */
	public int getKekMakerSeq(String makerName) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT MAKERSEQ FROM KEK_VDAMAKER WHERE MAKERNAME='" + makerName + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 메이커 SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 메이커 SEQ", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	public int getKekCustSeq(String customer) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT CUSTSEQ FROM KEK_VDAPURCUST WHERE CUSTNAME='" + customer + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 메이커 SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 메이커 SEQ", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * ERP 기준단위 SEQ
	 */
	public int getKekUnitSeq(String unitName) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT UNITSEQ FROM KEK_VDAUNIT WHERE UNITNAME='" + unitName + "'");
			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 기준단위 SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 기준단위 SEQ", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * ERP 통화 SEQ
	 */
	public int getKekCurrencySeq(String currency) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT CURRSEQ FROM KEK_VDACURR WHERE CURRNAME='" + currency + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 통화 SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 통화 SEQ", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * ERP 조달구분 SEQ
	 */
	public int getKekSupplySeq(String classification) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT UMSUPPLYTYPE FROM KEK_VDASUPPLYTYPE WHERE UMSUPPLYTYPENAME='" + classification + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				return (int) rs.getInt(1);
			}
			ErpHelper.service.writeLog("수배표 조달구분 SEQ", sql.toString(), "", true, "수배표");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("수배표 조달구분 SEQ", sql.toString(), e.toString(), false, "수배표");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return 0;
	}

	/**
	 * ERP 로 품목 전송 후 YCODE 리턴
	 */
	public String sendToErp(WTPart part) throws Exception {
		String partNo = null;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

//			con = dataSource.getConnection();
			con = DBCPManager.getConnection("erp");
			st = con.createStatement();

			int keyIdx = 1;
			sql.append(
					"INSERT INTO KEK_TDAItem_IF (SEQ, PART_NAME, PART_SPEC, UNITSEQ, MAKERSEQ, USERID, PRICE, CURRSEQ, CUSTSEQ, ");
			sql.append("CREATE_DATE,");
			String addName = "AddFileName" + keyIdx;
			String addExt = "AddFileExt" + keyIdx;
			String addSize = "AddFileSize" + keyIdx;
			sql.append("" + addName + ", " + addExt + ", " + addSize + ", ");
			sql.append("IsCode)");

			int seq = getMaxSequence("KEK_TDAITEM_IF", "제작사양서");
			sql.append(" VALUES('" + seq + "', ");

			String partName = IBAUtils.getStringValue(part, "NAME_OF_PARTS");
			String spec = IBAUtils.getStringValue(part, "DWG_NO");
			sql.append("'" + partName + "', ");
			sql.append("'" + spec + "', ");

			int unitSeq = getKekUnitSeq(IBAUtils.getStringValue(part, "STD_UNIT"));
			sql.append("'" + unitSeq + "', ");

			int makerSeq = getKekMakerSeq(IBAUtils.getStringValue(part, "MAKER"));
			sql.append("'" + makerSeq + "', ");

			WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
			String userId = user.getName();
			sql.append("'" + userId + "', ");

			int price = IBAUtils.getIntegerValue(part, "PRICE");
			sql.append("'" + price + "', ");

			int curSeq = getKekCurrencySeq(IBAUtils.getStringValue(part, "CURRNAME"));
			sql.append("'" + curSeq + "', ");

			int custSeq = getKekCustSeq(IBAUtils.getStringValue(part, "CUSTNAME"));
			sql.append("'" + custSeq + "', ");

			String createTime = new Timestamp(new Date().getTime()).toString();
			sql.append("'" + createTime + "', ");

			QueryResult result = ContentHelper.service.getContentsByRole(part, ContentRoleType.PRIMARY);
			ApplicationData data = null;
			if (result.hasMoreElements()) {
				data = (ApplicationData) result.nextElement();
			}

			String addFileName = "";
			String addFileExt = "";
			String size = "";

			if (data != null) {
				addFileName = data.getFileName();
				addFileExt = FileUtil.getExtension(addFileName);
				size = String.valueOf(data.getFileSize());
				sql.append("'" + addFileName + "', ");
				sql.append("'" + addFileExt + "', ");
				sql.append("'" + size + "', ");
			} else {
				sql.append("'" + addFileName + "', ");
				sql.append("'" + addFileExt + "', ");
				sql.append("'" + size + "', ");
			}
			String message = "Y";
			sql.append("'" + message + "');");

			System.out.println("query = " + sql.toString());

			st.executeUpdate(sql.toString());

			String dir = epmOutputDir + File.separator + spec;
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			if (data != null) {
				byte[] buffer = new byte[10240];
				InputStream is = ContentServerHelper.service.findLocalContentStream(data);
				File write = new File(directory + File.separator + data.getFileName());
				FileOutputStream fos = new FileOutputStream(write);
				int j = 0;
				while ((j = is.read(buffer, 0, 10240)) > 0) {
					fos.write(buffer, 0, j);
				}
				fos.close();
				is.close();
			}

			StringBuffer sb = new StringBuffer();
			sb.append("EXEC KEK_SPLMITEMIF");
			st.executeUpdate(sb.toString());

			partNo = savePartNo(part, spec);

			ErpHelper.service.writeLog("ERP로 품목정보 전송", sql.toString(), "", true, "품목등록");

		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("ERP로 품목정보 전송", sql.toString(), e.toString(), false, "품목등록");
//			ErpConnectionPool.free(con, st, rs);
			DBCPManager.freeConnection(con, st, rs);
			throw e;
		} finally {
			DBCPManager.freeConnection(con, st, rs);
//			ErpConnectionPool.free(con, st, rs);
		}
		return partNo;
	}

	/**
	 * PDM 품목, 도면 데이터 IBA값 설정
	 */
	private String savePartNo(WTPart part, String spec) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		String partNo = "";
		try {

			con = DBCPManager.getConnection("erp");
//			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT PART_NO FROM KEK_TDAItem_IF WHERE PART_SPEC='" + spec + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				partNo = (String) rs.getString(1);
			}

			if (!StringUtils.isNull(partNo)) {
				IBAUtils.deleteIBA(part, "PART_CODE", "s");
				IBAUtils.createIBA(part, "s", "PART_CODE", partNo);

				EPMDocument ee = PartHelper.manager.getEPMDocument(part);

				if (ee != null) {
					IBAUtils.deleteIBA(ee, "PART_CODE", "s");
					IBAUtils.createIBA(ee, "s", "PART_CODE", partNo);

					EPMDocument epm = EpmHelper.manager.getEPM2D(ee);

					if (epm != null) {
						IBAUtils.deleteIBA(epm, "PART_CODE", "s");
						IBAUtils.createIBA(epm, "s", "PART_CODE", partNo);
					}
				}
			}

			ErpHelper.service.writeLog("ERP 품목정보 조회후 IBA 세팅", sql.toString(), "", true, "품목등록");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("RP 품목정보 조회후 IBA 세팅", sql.toString(), e.toString(), false, "품목등록");
//			ErpConnectionPool.free(con, st, rs);
			DBCPManager.freeConnection(con, st, rs);
			throw e;
		} finally {
			DBCPManager.freeConnection(con, st, rs);
//			ErpConnectionPool.free(con, st, rs);
		}
		return partNo;
	}

	/**
	 * ERP 자재 전송
	 */
	public String sendToErpItem(WTPart part, WTDocument document) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		ArrayList<WTDocument> list = new ArrayList<>();
		String partNo = "";
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();
			con.setAutoCommit(false);

			int keyIdx = 1;

			sql.append(
					"INSERT INTO KEK_TDAITEM_IF (SEQ, PART_NAME, PART_SPEC, UNITSEQ, MAKERSEQ, USERID, PRICE, CURRSEQ, CUSTSEQ, ");
			sql.append("CREATE_DATE,");

//			QueryResult result = PersistenceHelper.manager.navigate(part, "document", WTDocumentWTPartLink.class);
//			while (result.hasMoreElements()) {
//				WTDocument doc = (WTDocument) result.nextElement();
//				list.add(doc);
//			}

			list.add(document);

			for (int i = 0; i < list.size(); i++) {
				String addName = "ADDFILENAME" + keyIdx;
				String addExt = "ADDFILEEXT" + keyIdx;
				String addSize = "ADDFILESIZE" + keyIdx;
				sql.append("" + addName + ", " + addExt + ", " + addSize + ", ");
				keyIdx++;
			}

			sql.append("ISCODE)");

			int seq = getMaxSequence("KEK_TDAITEM_IF", "제작사양서");
			sql.append(" VALUES('" + seq + "', ");

			String partName = IBAUtils.getStringValue(part, "NAME_OF_PARTS");
			String spec = IBAUtils.getStringValue(part, "DWG_NO");
			sql.append("'" + partName + "', ");
			sql.append("'" + spec + "', ");

			sql.append("'" + getKekUnitSeq(IBAUtils.getStringValue(part, "STD_UNIT")) + "', ");
			sql.append("'" + getKekMakerSeq(IBAUtils.getStringValue(part, "MAKER")) + "', ");

			WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
			sql.append("'" + user.getName() + "', ");

			int price = IBAUtils.getIntegerValue(part, "PRICE");
			sql.append("'" + price + "', ");
			sql.append("'" + getKekCurrencySeq(IBAUtils.getStringValue(part, "CURRNAME")) + "', ");
			sql.append("'" + getKekCustSeq(IBAUtils.getStringValue(part, "CUSTNAME")) + "', ");

			String createTime = new Timestamp(new Date().getTime()).toString();
			sql.append("'" + createTime + "', ");

			for (WTDocument doc : list) {
				ApplicationData data = null;
				QueryResult qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
				if (qr.hasMoreElements()) {
					data = (ApplicationData) qr.nextElement();
				}
				sql.append("'" + data.getFileName() + "', ");
				sql.append("'" + FileUtil.getExtension(data.getFileName()) + "', ");
				sql.append("'" + data.getFileSize() + "', ");
			}

			sql.append("'Y');");
			st.executeUpdate(sql.toString());

			String dir = epmOutputDir + File.separator + spec;
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			for (WTDocument doc : list) {
				QueryResult qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
				if (qr.hasMoreElements()) {
					ApplicationData data = (ApplicationData) qr.nextElement();
					byte[] buffer = new byte[10240];
					InputStream is = ContentServerHelper.service.findLocalContentStream(data);
					File write = new File(directory + File.separator + data.getFileName());
					FileOutputStream fos = new FileOutputStream(write);
					int j = 0;
					while ((j = is.read(buffer, 0, 10240)) > 0) {
						fos.write(buffer, 0, j);
					}
					fos.close();
					is.close();
				}
			}

			StringBuffer sb = new StringBuffer();
			sb.append("EXEC KEK_SPLMITEMIF");
			st.executeUpdate(sb.toString());

			partNo = savePartNo(part, spec);

			con.commit();

			ErpHelper.service.writeLog("ERP로 품목정보 전송", sql.toString(), "", true, "품목등록");
		} catch (Exception e) {
			e.printStackTrace();
			ErpHelper.service.writeLog("ERP로 품목정보 전송", sql.toString(), e.toString(), false, "품목등록");
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return partNo;
	}

	/**
	 * 큐를 이용한 수배표 ERP 전송
	 */
	public void postSendToErp(String oid) throws Exception {
		WTPrincipal principal = SessionHelper.manager.getPrincipal();
		ProcessingQueue queue = (ProcessingQueue) QueueHelper.manager.getQueue(processQueueName, ProcessingQueue.class);

		Hashtable<String, String> hash = new Hashtable<>();
		hash.put("oid", oid);

		Class[] argClasses = { Hashtable.class };
		Object[] argObjects = { hash };

		queue.addEntry(principal, methodName, className, argClasses, argObjects);
	}

	public void sendToErpFromQueue(HashMap<String, String> hash) throws Exception {

	}

	/**
	 * ERP 로그 리스트 검색 함수
	 */
	public Map<String, Object> list(Map<String, Object> params) throws Exception {
		System.out.println("ERP 로그 START = " + new Timestamp(new Date().getTime()));
		Map<String, Object> map = new HashMap<String, Object>();
		String name = (String) params.get("name");
		String resultMsg = (String) params.get("resultMsg");
		String sendQuery = (String) params.get("sendQuery");

		List<ErpDTO> list = new ArrayList<ErpDTO>();

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(ErpSendHistory.class, true);

		QuerySpecUtils.toLikeAnd(query, idx, ErpSendHistory.class, ErpSendHistory.NAME, name);
		QuerySpecUtils.toLikeAnd(query, idx, ErpSendHistory.class, ErpSendHistory.RESULT_MSG, resultMsg);
		QuerySpecUtils.toLikeAnd(query, idx, ErpSendHistory.class, ErpSendHistory.SEND_QUERY, sendQuery);

		QuerySpecUtils.toOrderBy(query, idx, ErpSendHistory.class, ErpSendHistory.CREATE_TIMESTAMP, true);

		PageQueryUtils pager = new PageQueryUtils(params, query);
		PagingQueryResult result = pager.find();
		while (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			ErpSendHistory erpSendHistory = (ErpSendHistory) obj[0];
			ErpDTO column = new ErpDTO(erpSendHistory);
			list.add(column);
		}

		map.put("list", list);
		map.put("sessionid", pager.getSessionId());
		map.put("curPage", pager.getCpage());
		System.out.println("ERP 로그 END = " + new Timestamp(new Date().getTime()));
		return map;
	}

	/**
	 * LOT NO로 UNIT NAME 가져오기
	 */
	public Map<String, Object> getLotNoByUnitName(String lotNo) throws Exception {
		Map<String, Object> result = new HashMap<>();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT LOTSEQ, LOTNO, LOTUNITNAME FROM KEK_VDALOTNO WHERE LOTNO='" + lotNo + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				String unitName = (String) rs.getString(3);
				result.put("unitName", unitName);
			}

		} catch (Exception e) {
			e.printStackTrace();
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return result;
	}

	/**
	 * YCODE 검증 함수
	 */
	public Map<String, Object> validateErpPartNumber(String partNumber) throws Exception {
		Map<String, Object> result = new HashMap<>();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT ITEMSEQ, ITEMNAME");
			sql.append(" FROM KEK_VDAITEM");
			sql.append(" WHERE ITEMNO='" + partNumber.trim() + "' AND SMSATAUSNAME != '폐기'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				result.put("check", true);
			} else {
				result.put("check", false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return result;
	}

	/**
	 * UNIT BOM 전송
	 */
	public void sendToUnitBom(UnitBom unitBom, ArrayList<UnitSubPart> list) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			StringBuffer sb = new StringBuffer();

			sb.append(
					"INSERT INTO KEK_TDAITEM_IF (SEQ, PART_NAME, PART_SPEC, UNITSEQ, MAKERSEQ, USERID,  CURRSEQ, CUSTSEQ, ");
			sb.append("CREATE_DATE, ISCODE)");

			int SEQS = getMaxSequence("KEK_TDAITEM_IF", "UNIT BOM 자재");

			sb.append(" VALUES('" + SEQS + "', ");

			String PART_NAMES = unitBom.getPartName().trim();
			String PART_SPECS = unitBom.getSpec().trim();

			sb.append("'" + PART_NAMES + "', ");
			sb.append("'" + PART_SPECS + "', ");

			sb.append("'" + getKekUnitSeq(unitBom.getUnit()) + "', ");
			sb.append("'" + getKekMakerSeq(unitBom.getMaker()) + "', ");

			WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
			String USERID = user.getName();
			sb.append("'" + USERID + "', ");

			sb.append("'" + getKekCurrencySeq(unitBom.getCurrency()) + "', ");
			sb.append("'" + getKekCustSeq(unitBom.getCustomer()) + "', ");

			String CREATE_DATE = new Timestamp(new Date().getTime()).toString();
			sb.append("'" + CREATE_DATE + "', ");

			String IsCode = "U";
			sb.append("'" + IsCode + "');");

			System.out.println("sendUnitBomToERP sql = " + sb.toString());

			st.executeUpdate(sb.toString());

			QueryResult qr = PersistenceHelper.manager.navigate(unitBom, "subPart", UnitBomPartLink.class, false);

			while (qr.hasMoreElements()) {
				UnitBomPartLink link = (UnitBomPartLink) qr.nextElement();
				UnitSubPart part = link.getSubPart();
				StringBuffer sql = new StringBuffer();

				sql.append(" INSERT INTO KEK_TPJTUNITBOM_IF (  ");
				sql.append(" SEQ, UNITPARTNO, PART_NAME, PART_SPEC, PART_NO, ");
				sql.append(" SUB_PART_NAME, SUB_PART_SPEC, SUB_PART_NO, QTY, UNITITEMSEQ, ");
				sql.append(" ITEMSEQ, SUBITEMSEQ, MAKERSEQ, UNITSEQ, CURRSEQ, ");
				sql.append(" PRICE, FULLPATH, CREATE_TIME");
				sql.append(")");

				int SEQ = getMaxSequence("KEK_TPJTUnitBOM_IF", "UNIT BOM");

				System.out.println("SEQ=" + SEQ);
				sql.append(" VALUES ('" + SEQ + "', ");

				String UNITPARTNO = unitBom.getSpec();
				sql.append("'" + UNITPARTNO + "', ");

				String PART_NAME = unitBom.getPartName();
				sql.append("'" + PART_NAME + "', ");

				String PART_SPEC = unitBom.getSpec();
				sql.append("'" + PART_SPEC + "', ");

				String PART_NO = unitBom.getPartNo();
				sql.append("'" + PART_NO + "', ");

				String SUB_PART_NAME = part.getPartName();
				sql.append("'" + SUB_PART_NAME + "', ");

				String SUB_PART_SPEC = part.getStandard();
				sql.append("'" + SUB_PART_SPEC + "', ");

				String SUB_PART_NO = part.getPartNo();
				sql.append("'" + SUB_PART_NO + "', ");

				String QTY = part.getQuantity();
				if (QTY == null || "".equals(QTY)) {
					QTY = "1";
				}
				sql.append("'" + QTY + "', ");

				String UnitItemSeq = part.getQuantity();
				if (UnitItemSeq == null || "".equals(UnitItemSeq)) {
					UnitItemSeq = "1";
				}
				sql.append("'" + UnitItemSeq + "', ");

				String ItemSeq = part.getQuantity();
				ItemSeq = "1";
				sql.append("'" + ItemSeq + "', ");

				String SubItemSeq = part.getQuantity();
				SubItemSeq = "1";
				sql.append("'" + SubItemSeq + "', ");
				sql.append("'" + getKekMakerSeq(part.getMaker()) + "', ");
				sql.append("'" + getKekUnitSeq(part.getUnit()) + "', ");
				sql.append("'" + getKekCurrencySeq(part.getCurrency()) + "', ");

				String PRICE = part.getPrice().replace(",", "");
				sql.append("'" + PRICE + "', ");

				String FullPath = PART_SPEC + File.separator + SUB_PART_SPEC;
				sql.append("'" + FullPath + "', ");

				CREATE_DATE = new Timestamp(new Date().getTime()).toString();
				sql.append("'" + CREATE_DATE + "');");

				System.out.println("sendUnitBomToERP sql = " + sql.toString());

				st.executeUpdate(sql.toString());

				StringBuffer exec = new StringBuffer();
				exec.append("EXEC KEK_SPLMUnitBOM '" + PART_NO + "'");
				st.executeUpdate(exec.toString());

				setUCode(PART_SPEC, unitBom);
			}

			StringBuffer sbs = new StringBuffer();
			sbs.append("EXEC KEK_SPLMItemIF");
			st.executeUpdate(sbs.toString());

		} catch (Exception e) {
			e.printStackTrace();
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
	}

	/**
	 * UNIT BOM 등록시 규격 체크
	 */
	public Map<String, Object> erpCheck(String spec) throws Exception {
		Map<String, Object> result = new HashMap<>();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {

			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT COUNT(*) FROM KEK_VDAITEM WHERE SPEC='" + spec + "'");

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				int i = Integer.parseInt(rs.getString(1));
				if (i > 0) {
					result.put("msg", "규격 " + spec + "의 중복값이 있습니다.");
					result.put("check", false);
				} else {
					result.put("msg", "중복확인 되었습니다.");
					result.put("check", true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return result;
	}

	private String setUCode(String PART_SPEC, UnitBom unitBom) throws Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String reValue = "";
		StringBuffer sql = new StringBuffer();
		try {
			con = dataSource.getConnection();
			st = con.createStatement();

			sql.append("SELECT PART_NO FROM KEK_TDAITEM_IF WHERE PART_SPEC='" + PART_SPEC + "'");

			String uCode = "";

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				uCode = (String) rs.getString(1);
			}

			if (!StringUtils.isNull(uCode)) {
				unitBom.setUCode(uCode);
				PersistenceHelper.manager.modify(unitBom);
			}
			reValue = uCode;
		} catch (Exception e) {
			e.printStackTrace();
			ErpConnectionPool.free(con, st, rs);
			throw e;
		} finally {
			ErpConnectionPool.free(con, st, rs);
		}
		return reValue;
	}
}