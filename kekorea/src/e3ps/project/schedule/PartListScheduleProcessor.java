package e3ps.project.schedule;

import java.util.Properties;

import com.ptc.wvs.server.schedule.Schedulable;
import com.ptc.wvs.server.schedule.ScheduledJobProcessor;

public class PartListScheduleProcessor extends ScheduledJobProcessor {

	public PartListScheduleProcessor(String var1) {
		super(var1);
	}

	@Override
	protected void doScheduleJob(Schedulable var1, boolean var2, String var3, Properties var4) {
		if("KEK_PROJECT_PARTLIST_BATCH".equals(var1.getIdentifier())) {
			System.out.println("KEK PARTLIST BATCH JOB START!");
			PartListScheduleJobs.startBatch();
			System.out.println("KEK PARTLIST BATCH JOB END!");
		}
	}
}
