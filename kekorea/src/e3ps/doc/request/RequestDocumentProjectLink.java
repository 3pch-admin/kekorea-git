package e3ps.doc.request;

import com.ptc.windchill.annotations.metadata.GenAsBinaryLink;
import com.ptc.windchill.annotations.metadata.GeneratedRole;

import e3ps.project.Project;
import wt.fc.ObjectToObjectLink;
import wt.util.WTException;

@GenAsBinaryLink(superClass = ObjectToObjectLink.class,

		roleA = @GeneratedRole(name = "requestDocument", type = RequestDocument.class),

		roleB = @GeneratedRole(name = "project", type = Project.class)

)

public class RequestDocumentProjectLink extends _RequestDocumentProjectLink {

	static final long serialVersionUID = 1;

	public static RequestDocumentProjectLink newRequestDocumentProjectLink(RequestDocument requestDocument,
			Project project) throws WTException {
		RequestDocumentProjectLink instance = new RequestDocumentProjectLink();
		instance.initialize(requestDocument, project);
		return instance;
	}
}
