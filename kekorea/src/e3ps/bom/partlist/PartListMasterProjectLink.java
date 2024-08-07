package e3ps.bom.partlist;

import com.ptc.windchill.annotations.metadata.GenAsBinaryLink;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;
import com.ptc.windchill.annotations.metadata.GeneratedRole;

import e3ps.project.Project;
import wt.fc.ObjectToObjectLink;
import wt.util.WTException;

@GenAsBinaryLink(superClass = ObjectToObjectLink.class,

		roleA = @GeneratedRole(name = "master", type = PartListMaster.class),

		roleB = @GeneratedRole(name = "project", type = Project.class),

		properties = {

				@GeneratedProperty(name = "sort", type = Integer.class, javaDoc = "정렬", initialValue = "1")

		}

)

public class PartListMasterProjectLink extends _PartListMasterProjectLink {

	static final long serialVersionUID = 1;

	public static PartListMasterProjectLink newPartListMasterProjectLink(PartListMaster partListMaster, Project project)
			throws WTException {
		PartListMasterProjectLink instance = new PartListMasterProjectLink();
		instance.initialize(partListMaster, project);
		return instance;
	}
}
