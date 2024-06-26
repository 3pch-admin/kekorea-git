package e3ps.common.content.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;

import e3ps.common.util.ContentUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fv.Vault;
import wt.fv.uploadtocache.CacheDescriptor;
import wt.fv.uploadtocache.CachedContentDescriptor;
import wt.fv.uploadtocache.UploadToCacheHelper;
import wt.query.QuerySpec;
import wt.services.ServiceFactory;
import wt.util.EncodingConverter;
import wt.util.FileUtil;
import wt.util.WTAttributeNameIfc;
import wt.util.WTProperties;

public class CommonContentHelper {

	public static final CommonContentHelper manager = new CommonContentHelper();
	public static final CommonContentService service = ServiceFactory.getService(CommonContentService.class);

	private static String savePath = null;
	static {
		try {
			if (savePath == null) {
				savePath = WTProperties.getServerProperties().getProperty("wt.temp") + File.separator + "kekorea";
				File tempFolder = new File(savePath);
				if (!tempFolder.exists()) {
					tempFolder.mkdirs();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 첨부파일 목록 가져오기
	 */
	public JSONObject list(String oid, String roleType) throws Exception {
		JSONObject list = new JSONObject();
		if (!StringUtils.isNull(oid)) {
			ReferenceFactory rf = new ReferenceFactory();
			ContentHolder holder = (ContentHolder) rf.getReference(oid).getObject();

			if ("p".equalsIgnoreCase(roleType) || "primary".equalsIgnoreCase(roleType)) {
				JSONArray array = new JSONArray();
				QueryResult result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.PRIMARY);
				int idx = 0;
				if (result.hasMoreElements()) {
					ApplicationData data = (ApplicationData) result.nextElement();
					InputStream is = ContentServerHelper.service.findLocalContentStream(data);

					File file = new File(savePath + File.separator + data.getFileName());
					OutputStream outputStream = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						outputStream.write(buffer, 0, length);
					}
					is.close();
					outputStream.close();

					CacheDescriptor localCacheDescriptor = UploadToCacheHelper.service.getCacheDescriptor(1, true);
					long folderId = localCacheDescriptor.getFolderId();
					long streamId = localCacheDescriptor.getStreamIds()[0];

					InputStream[] streams = new InputStream[1];
					streams[0] = new FileInputStream(file);
					long[] fileSize = new long[1];
					fileSize[0] = file.length();

					CachedContentDescriptor ccd = new CachedContentDescriptor(streamId, folderId, fileSize[0], 0,
							file.getPath());

					JSONObject obj = new JSONObject();
					obj.put("_id_", "AX_P" + idx);
					obj.put("tagId", "AX_P" + idx);
					obj.put("name", data.getFileName());
					obj.put("fileSize", data.getFileSize());
					obj.put("uploadedPath", data.getUploadedFromPath());
					obj.put("roleType", roleType);
					obj.put("cacheId", ccd.getEncodedCCD());
					array.add(obj);

					streams[0].close();
					idx++;
				}

				result.reset();
				result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
				while (result.hasMoreElements()) {
					ApplicationData data = (ApplicationData) result.nextElement();
					JSONObject obj = new JSONObject();

					InputStream is = ContentServerHelper.service.findLocalContentStream(data);

					File file = new File(savePath + File.separator + data.getFileName());
					OutputStream outputStream = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						outputStream.write(buffer, 0, length);
					}
					outputStream.close();

					CacheDescriptor localCacheDescriptor = UploadToCacheHelper.service.getCacheDescriptor(1, true);
					long folderId = localCacheDescriptor.getFolderId();
					long streamId = localCacheDescriptor.getStreamIds()[0];

					InputStream[] streams = new InputStream[1];
					streams[0] = new FileInputStream(file);
					long[] fileSize = new long[1];
					fileSize[0] = file.length();

					CachedContentDescriptor ccd = new CachedContentDescriptor(streamId, folderId, fileSize[0], 0,
							file.getPath());

//					obj.put("_id_", UUID.randomUUID().toString());
//					obj.put("tagId", UUID.randomUUID().toString());
					obj.put("_id_", "AX_S" + idx);
					obj.put("tagId", "AX_S" + idx);
					obj.put("name", data.getFileName());
					obj.put("fileSize", data.getFileSize());
					obj.put("uploadedPath", data.getUploadedFromPath());
					obj.put("roleType", roleType);
					obj.put("cacheId", ccd.getEncodedCCD());
					array.add(obj);

					streams[0].close();
					idx++;
				}
				list.put("primaryFile", array);
			} else if ("s".equalsIgnoreCase(roleType) || "secondary".equalsIgnoreCase(roleType)) {
				JSONArray array = new JSONArray();
				QueryResult result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
				while (result.hasMoreElements()) {
					ApplicationData data = (ApplicationData) result.nextElement();
					JSONObject obj = new JSONObject();

					InputStream is = ContentServerHelper.service.findLocalContentStream(data);

					File file = new File(savePath + File.separator + data.getFileName());
					OutputStream outputStream = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						outputStream.write(buffer, 0, length);
					}
					outputStream.close();

					CacheDescriptor localCacheDescriptor = UploadToCacheHelper.service.getCacheDescriptor(1, true);
					long folderId = localCacheDescriptor.getFolderId();
					long streamId = localCacheDescriptor.getStreamIds()[0];

					InputStream[] streams = new InputStream[1];
					streams[0] = new FileInputStream(file);
					long[] fileSize = new long[1];
					fileSize[0] = file.length();

					CachedContentDescriptor ccd = new CachedContentDescriptor(streamId, folderId, fileSize[0], 0,
							file.getPath());

					obj.put("_id_", UUID.randomUUID().toString());
					obj.put("tagId", UUID.randomUUID().toString());
					obj.put("name", data.getFileName());
					obj.put("fileSize", data.getFileSize());
					obj.put("uploadedPath", data.getUploadedFromPath());
					obj.put("roleType", roleType);
					obj.put("cacheId", ccd.getEncodedCCD());
					array.add(obj);
					list.put("secondaryFile", array);

					streams[0].close();
				}
			}
		}
		return list;
	}

	/**
	 * 첨부 파일 업로드
	 */
	public JSONObject upload(HttpServletRequest request) throws Exception {

		int sizeLimit = (1024 * 1024 * 500);
		MultipartRequest multi = new MultipartRequest(request, savePath, sizeLimit, "UTF-8");
		// MultipartRequest multi = new MultipartRequest(request, savePath, sizeLimit,
		// "UTF-8", new DefaultFileRenamePolicy());

		String roleType = multi.getParameter("roleType");
		String origin = multi.getOriginalFileName(roleType);
		String name = multi.getFilesystemName(roleType);
		String type = multi.getContentType(roleType);

		String filePath = savePath + File.separator + name;
		File file = new File(filePath);
		System.out.println("### upload==" + file.exists() + "==" + filePath);
		if (file.exists()) {
			System.out.println("### 파일 있음...");
		} else {
			System.out.println("### 파일 없음...");
		}

		CacheDescriptor localCacheDescriptor = UploadToCacheHelper.service.getCacheDescriptor(1, true);
		UploadToCacheHelper.service.getCacheDescriptor(1, true);

		// file.exists();

		InputStream[] streams = new InputStream[1];
		streams[0] = new FileInputStream(file);
		long[] fileSize = new long[1];
		fileSize[0] = file.length();
		String[] paths = new String[1];
		paths[0] = file.getPath();

		CachedContentDescriptor ccd = CommonContentHelper.service.doUpload(localCacheDescriptor, file);
		JSONObject json = new JSONObject();
		json.put("icon", ContentUtils.getFileIcon(name));
		json.put("name", origin);
		json.put("type", type);
		json.put("saveName", name);
		json.put("fileSize", fileSize[0]);
		json.put("uploadedPath", filePath);
		json.put("roleType", roleType);
		json.put("tagId", UUID.randomUUID().toString());
		json.put("cacheId", ccd.getEncodedCCD());
		json.put("base64", ContentUtils.imageToBase64(file, FileUtil.getExtension(origin)));
		streams[0].close();
		return json;
	}

	/**
	 * 서버 로컬 파일볼트 가져오기
	 */
	public Vault getLocalVault(long id) throws Exception {
		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Vault.class, true);
		QuerySpecUtils.toEqualsAnd(query, idx, Vault.class, WTAttributeNameIfc.ID_NAME, id);
		QueryResult result = PersistenceHelper.manager.find(query);
		if (result.hasMoreElements()) {
			Object[] obj = (Object[]) result.nextElement();
			return (Vault) obj[0];
		}
		return null;
	}

	/**
	 * 파일볼트 캐쉬키로 File 가져오기
	 */
	public File getFileFromCacheId(String cacheId) throws Exception {
		File vault = null;
		if (!StringUtils.isNull(cacheId)) {
			String tmp = cacheId.split("/")[0];
			EncodingConverter localEncodingConverter = new EncodingConverter();
			String str = localEncodingConverter.decode(tmp);
			String[] arrayOfString = str.split(":");
			String cachePath = arrayOfString[arrayOfString.length - 2] + ":" + arrayOfString[arrayOfString.length - 1];
			vault = new File(cachePath);

			System.out.println("### getFileFromCacheId==" + vault.exists() + "==" + cachePath);

		}
		return vault;
	}

	/**
	 * 파일 템프 테이블 클리어
	 */
	public void clean() throws Exception {
//		File directory = new File(savePath);
//		File[] list = directory.listFiles();
//		for (File file : list) {
//			System.out.println("###content clean=="+file.getPath()+"=="+file.getName());
//			file.delete();
//		}
	}

	/**
	 * 모든 첨부파일 삭제
	 */
	public void clear(ContentHolder holder) throws Exception {
		QueryResult result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.PRIMARY);
		System.out.println("### clear=11=" + result.size());
		if (result.hasMoreElements()) {
			ContentItem item = (ContentItem) result.nextElement();
			System.out.println("### delete=11=" + item);
			ContentServerHelper.service.deleteContent(holder, item);
		}

		result.reset();
		result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
		System.out.println("### clear=22=" + result.size());
		while (result.hasMoreElements()) {
			ContentItem item = (ContentItem) result.nextElement();
			System.out.println("### delete=22=" + item);
			ContentServerHelper.service.deleteContent(holder, item);
		}

		result.reset();
		result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.THUMBNAIL);
		while (result.hasMoreElements()) {
			ContentItem item = (ContentItem) result.nextElement();
			ContentServerHelper.service.deleteContent(holder, item);
		}

		result.reset();
		result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.ADDITIONAL_FILES);
		while (result.hasMoreElements()) {
			ContentItem item = (ContentItem) result.nextElement();
			ContentServerHelper.service.deleteContent(holder, item);
		}
	}

	/**
	 * 주 첨부파일 모두 삭제
	 */
	public void clearP(ContentHolder holder) throws Exception {
		QueryResult result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.PRIMARY);
		if (result.hasMoreElements()) {
			ContentItem item = (ContentItem) result.nextElement();
			ContentServerHelper.service.deleteContent(holder, item);
		}
	}

	/**
	 * 첨부파일 모두 삭제
	 */
	public void clearS(ContentHolder holder) throws Exception {
		QueryResult result = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
		while (result.hasMoreElements()) {
			ContentItem item = (ContentItem) result.nextElement();
			ContentServerHelper.service.deleteContent(holder, item);
		}
	}
}
