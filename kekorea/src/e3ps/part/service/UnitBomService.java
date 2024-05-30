package e3ps.part.service;

import e3ps.part.dto.UnitBomDTO;
import wt.method.RemoteInterface;

@RemoteInterface
public interface UnitBomService {

	public abstract void create(UnitBomDTO dto) throws Exception;

}
