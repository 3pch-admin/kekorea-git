package e3ps.part.service;

import java.util.Map;

import e3ps.part.dto.PartDTO;
import wt.method.RemoteInterface;

@RemoteInterface
public interface PartService {

	/**
	 * 부품 일괄 등록(신규)
	 */
	public abstract Map<String, Object> bundle(Map<String, Object> params) throws Exception;

	/**
	 * 제작사양서 등록
	 */
	public abstract Map<String, Object> spec(Map<String, Object> params) throws Exception;

	/**
	 * 부품 수정
	 */
	public abstract void modify(Map<String, Object> params) throws Exception;

	/**
	 * 부품 일괄 등록
	 */
	public abstract void batch(Map<String, Object> params) throws Exception;

	/**
	 * 코드 채번
	 */
	public abstract void code(Map<String, Object> params) throws Exception;

	/**
	 * 부품 일괄 등록(PLM)
	 */
	public abstract void plm(Map<String, Object> params) throws Exception;
	
	/**
	 * 코드 생성 페이지
	 */
	public abstract void create(Map<String, Object> params) throws Exception;
}
