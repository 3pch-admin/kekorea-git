package e3ps.korea.configSheet.beans;

import java.util.Comparator;

import e3ps.project.Project;

public class ConfigSheetComparator implements Comparator<Project> {

	public int compare(Project a, Project b) {
		return a.getKekNumber().compareTo(b.getKekNumber());
	}
}
