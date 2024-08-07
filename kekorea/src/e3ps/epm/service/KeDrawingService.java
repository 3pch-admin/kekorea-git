package e3ps.epm.service;

import java.util.HashMap;
import java.util.List;

import e3ps.epm.dto.KeDrawingDTO;
import wt.method.RemoteInterface;

@RemoteInterface
public interface KeDrawingService {

	/**
	 * 그리드에서 KE도면 등록,수정,삭제 하는 함수
	 */
	public abstract void save(HashMap<String, List<KeDrawingDTO>> dataMap) throws Exception;

	/**
	 * 그리드에서 KE도면 개정 하는 함수
	 */
	public abstract void revise(HashMap<String, List<KeDrawingDTO>> dataMap) throws Exception;
	
	/**
	 * 그리드에서 KE도면 개정 하는 함수
	 */
	public abstract void modify(HashMap<String, List<KeDrawingDTO>> dataMap) throws Exception;

}
