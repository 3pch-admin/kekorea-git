package e3ps.epm.workOrder;

import com.ptc.windchill.annotations.metadata.Cardinality;
import com.ptc.windchill.annotations.metadata.ColumnProperties;
import com.ptc.windchill.annotations.metadata.ForeignKeyRole;
import com.ptc.windchill.annotations.metadata.GenAsPersistable;
import com.ptc.windchill.annotations.metadata.GeneratedForeignKey;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;
import com.ptc.windchill.annotations.metadata.MyRole;
import com.ptc.windchill.annotations.metadata.PropertyConstraints;

import e3ps.epm.numberRule.NumberRule;
import wt.content.ContentHolder;
import wt.enterprise.Managed;
import wt.util.WTException;

@GenAsPersistable(superClass = Managed.class, interfaces = { ContentHolder.class },

		properties = {

				@GeneratedProperty(name = "version", type = Integer.class, javaDoc = "버전", initialValue = "1", constraints = @PropertyConstraints(required = true)),

				@GeneratedProperty(name = "latest", type = Boolean.class, javaDoc = "최신여부", initialValue = "true", constraints = @PropertyConstraints(required = true)),

				@GeneratedProperty(name = "description", type = String.class, javaDoc = "설명", constraints = @PropertyConstraints(upperLimit = 2000)),

				@GeneratedProperty(name = "note", type = String.class, javaDoc = "개정사유", constraints = @PropertyConstraints(upperLimit = 2000)),

				@GeneratedProperty(name = "name", type = String.class, javaDoc = "도면일람표 명", constraints = @PropertyConstraints(required = true)),

				@GeneratedProperty(name = "number", type = String.class, constraints = @PropertyConstraints(required = true), columnProperties = @ColumnProperties(columnName = "WorkOrderNumber")),

				@GeneratedProperty(name = "workOrderType", type = String.class, javaDoc = "도면일람표 타입", constraints = @PropertyConstraints(required = true)),

				@GeneratedProperty(name = "approved", type = String.class),

				@GeneratedProperty(name = "judged", type = String.class),

				@GeneratedProperty(name = "checekd", type = String.class)

		},

		foreignKeys = {

				@GeneratedForeignKey(name = "WorkOrderNumberRuleLink",

						foreignKeyRole = @ForeignKeyRole(name = "numberRule", type = NumberRule.class,

								constraints = @PropertyConstraints(required = true)),

						myRole = @MyRole(name = "workOrder", cardinality = Cardinality.ONE)

				) }

)
public class WorkOrder extends _WorkOrder {

	static final long serialVersionUID = 1;

	public static WorkOrder newWorkOrder() throws WTException {
		WorkOrder instance = new WorkOrder();
		instance.initialize();
		return instance;
	}

}
