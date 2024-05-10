<%@page import="e3ps.part.dto.UnitBomDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
UnitBomDTO dto = (UnitBomDTO) request.getAttribute("dto");
%>
<!-- AUIGrid -->
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				UNIT BOM 정보
			</div>
		</td>
		<td class="right">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>

<table class="view-table">
	<colgroup>
		<col width="150">
		<col width="500">
		<col width="150">
		<col width="500">
	</colgroup>
	<tr>
		<th class="lb">품명</th>
		<td class="indent5"><%=dto.getPartName()%></td>
		<th>기준단위</th>
		<td class="indent5"><%=dto.getUnit()%></td>
	</tr>
	<tr>
		<th class="lb">규격</th>
		<td class="indent5"><%=dto.getSpec()%></td>
		<th>메이커</th>
		<td class="indent5"><%=dto.getMaker() != null ? dto.getMaker() : ""%>
		</td>
	</tr>
	<tr>
		<th class="lb">통화</th>
		<td class="indent5"><%=dto.getCurrency()%></td>
		<th>기본구매처</th>
		<td class="indent5"><%=dto.getCustomer() != null ? dto.getCustomer() : ""%></td>
	</tr>
</table>

<br>

<div id="grid_wrap" style="height: 200px; border-top: 1px solid #3180c3;"></div>
<script type="text/javascript">
	let myGridID;
	const columns = [ {
		dataField : "partNumber",
		headerText : "부품번호",
		dataType : "string",
		width : 140,
	}, {
		dataField : "partName",
		headerText : "부품명",
		dataType : "string",
		width : 140,
	}, {
		dataField : "lotNo",
		headerText : "LOT NO",
		dataType : "string",
		width : 100,
	}, {
		dataField : "unitName",
		headerText : "UNIT NAME",
		dataType : "string",
		width : 140,
	}, {
		dataField : "spec",
		headerText : "규격",
		dataType : "string",
		width : 300,
	}, {
		dataField : "quantity",
		headerText : "수량",
		dataType : "numeric",
		postfix : "개",
		width : 70,
	}, {
		dataField : "maker",
		headerText : "제조사",
		dataType : "string",
		width : 100,
	}, {
		dataField : "customer",
		headerText : "거래처",
		dataType : "string",
		width : 120,
	}, {
		dataField : "unit",
		headerText : "단위",
		dataType : "string",
		width : 100,
	}, {
		dataField : "price",
		headerText : "단가",
		dataType : "numeric",
		formatString : "#,##0",
		postfix : "원",
		width : 70,
	}, {
		dataField : "currency",
		headerText : "화폐",
		dataType : "string",
		width : 70,
	}, {
		dataField : "won",
		headerText : "원화금액",
		dataType : "numeric",
		formatString : "#,##0",
		postfix : "원",
		width : 70,
	}, {
		dataField : "category",
		headerText : "조달구분",
		dataType : "string",
		width : 140,
	}, {
		dataField : "note",
		headerText : "비고",
		dataType : "string",
		width : 250,
	}, ]

	function createAUIGrid(columnLayout) {
		const props = {
			autoGridHeight : true,
			headerHeight : 30,
			showRowNumColumn : true,
			rowNumHeaderText : "번호",
			showAutoNoDataMessage : false,
			selectionMode : "multipleCells",
			displayTreeOpen : true
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		loadGridData();
// 		readyHandler();
	}
	
	function readyHandler() {
		AUIGrid.showItemsOnDepth(myGridID, 2);
	}

	function loadGridData() {
		const oid = document.getElementById("oid").value;
		const url = getCallUrl("/unit/view");
		const params = new Object();
		params.oid = oid;
		openLayer();
		call(url, params, function(data) {
			if (data.result) {
				const list = data.list;
				AUIGrid.setGridData(myGridID, list);
			}
			closeLayer();
		});
	}

	document.addEventListener("DOMContentLoaded", function() {
		createAUIGrid(columns);
		AUIGrid.resize(myGridID);
	})

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID);
	});
</script>