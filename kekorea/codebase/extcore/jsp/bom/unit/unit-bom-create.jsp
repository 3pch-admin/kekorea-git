<%@page import="java.util.Date"%>
<%@page import="e3ps.common.util.DateUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String dd = DateUtils.getDateString(new Date(), "date");
%>
<!-- AUIGrid -->
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<input type="hidden" name="check" id="check" value="false">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				UNIT BOM 등록
			</div>
		</td>
		<td class="right">
			<input type="button" value="등록" title="등록" class="blue" onclick="create();">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>
<table class="create-table">
	<colgroup>
		<col width="150">
		<col width="600">
		<col width="150">
		<col width="600">
	</colgroup>
	<tr>
		<th class="req lb">품명</th>
		<td class="indent5">
			<input type="text" name="partName" id="partName" class="width-400">
		</td>
		<th class="req">기준단위</th>
		<td class="indent5">
			<input type="text" name="unit" id="unit" class="width-200">
		</td>
	</tr>
	<tr>
		<th class="req lb">규격</th>
		<td class="indent5">
			<input type="text" name="spec" id="spec" class="width-400">
			<input type="button" value="중복 체크" title="중복 체크" onclick="erpCheck();" class="red">
		</td>
		<th>메이커</th>
		<td class="indent5">
			<input type="text" name="maker" id="maker" class="width-200">
		</td>
	</tr>
	<tr>
		<th class="req lb">통화</th>
		<td class="indent5">
			<input type="text" name="currency" id="currency" class="width-400">
		</td>
		<th>기본구매처</th>
		<td class="indent5">
			<input type="text" name="customer" id="customer" class="width-200">
		</td>
	</tr>
</table>

<table class="button-table">
	<tr>
		<td class="left">
			<input type="button" value="행 추가(이후)" title="행 추가(이후)" class="orange" onclick="addRow();">
			<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">
		</td>
	</tr>
</table>

<div id="grid_wrap" style="height: 575px; border-top: 1px solid #3180c3;"></div>
<script type="text/javascript">
	let myGridID;
	const columns = [ {
		dataField : "check",
		headerText : "체크",
		dataType : "string",
		editable : false
	}, {
		dataField : "lotNo",
		headerText : "LOT_NO",
		dataType : "numeric",
		width : 100,
		editRenderer : {
			type : "InputEditRenderer",
			onlyNumeric : true,
		},
	}, {
		dataField : "unitName",
		headerText : "UNIT NAME",
		dataType : "string",
		width : 130
	}, {
		dataField : "partNumber",
		headerText : "부품번호",
		dataType : "string",
		width : 120
	}, {
		dataField : "partName",
		headerText : "부품명",
		dataType : "string",
		width : 250,
		editable : false
	}, {
		dataField : "spec",
		headerText : "규격",
		dataType : "string",
		width : 300,
		editable : false
	}, {
		dataField : "maker",
		headerText : "MAKER",
		dataType : "string",
		width : 100
	}, {
		dataField : "customer",
		headerText : "거래처",
		dataType : "string",
		width : 100
	}, {
		dataField : "quantity",
		headerText : "수량",
		dataType : "numeric",
		dataType : "numeric",
		editRenderer : {
			type : "InputEditRenderer",
			onlyNumeric : true,
		},
		width : 80
	}, {
		dataField : "unit",
		headerText : "단위",
		dataType : "string",
		width : 80,
		editable : false
	}, {
		dataField : "price",
		headerText : "단가",
		dataType : "numeric",
		width : 100,
		formatString : "#,##0",
		editable : false
	}, {
		dataField : "currency",
		headerText : "화폐",
		dataType : "string",
		width : 80,
		editable : false
	}, {
		dataField : "won",
		headerText : "원화금액",
		dataType : "numeric",
		width : 120,
		formatString : "#,##0",
		editable : false
	}, {
		dataField : "wantedDate",
		headerText : "수배일자",
		dataType : "string",
		width : 120,
		editable : false
	}, {
		dataField : "rate",
		headerText : "환율",
		dataType : "string",
		width : 80,
		editable : false
	}, {
		dataField : "ref",
		headerText : "참고도면",
		dataType : "string",
		width : 100,
	}, {
		dataField : "category",
		headerText : "조달구분",
		dataType : "string",
		width : 100
	}, {
		dataField : "note",
		headerText : "비고",
		dataType : "string",
		width : 150,
	} ];

	function createAUIGrid(columnLayout) {
		const props = {
			editable : true,
			showStateColumn : true,
			headerHeight : 30,
			showRowCheckColumn : true,
			showRowNumColumn : true,
			rowNumHeaderText : "번호",
			showAutoNoDataMessage : false,
			selectionMode : "multipleCells",
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		readyHandler();
		AUIGrid.bind(myGridID, "keyDown", auiKeyDownHandler);
		AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
	}

	function auiCellEditEndHandler(event) {
		const dataField = event.dataField;
		const item = event.item;
		const rowIndex = event.rowIndex;
		if (dataField === "lotNo") {
			const lotNo = item.lotNo;
			const url = getCallUrl("/erp/getLotNoByUnitName?lotNo=" + lotNo);
			call(url, null, function(data) {
				if (data.result) {
					const unitName = data.unitName;
					item.unitName = unitName;
					AUIGrid.updateRow(myGridID, item, rowIndex);
				}
			}, "GET");
		} else if (dataField === "partNumber") {
			const partNumber = item.partNumber;
			const url = getCallUrl("/erp/validateErpPartNumber?partNumber=" + partNumber); // ycode
			call(url, null, function(data) {
				if (data.result) {
					const check = data.check;
					if (check) {
						item.check = "OK";
					} else {
						item.check = "NG";
					}
					AUIGrid.updateRow(myGridID, item, rowIndex);
				} else {
					item.check = "NG";
					AUIGrid.updateRow(myGridID, item, rowIndex);
				}
			}, "GET");
		}

		if (dataField === "partNumber" || dataField === "quantity") {
			const partNumber = item.partNumber;
			let quantity = item.quantity;
			
			if(isNull(partNumber)) {
				return false;
			}
			
			if(isNull(quantity)) {
				quantity = 1;
			}
			
			const url = getCallUrl("/erp/getErpItemByPartNoAndQuantity?partNo=" + partNumber + "&quantity=" + quantity);
			call(url, null, function(data) {
				console.log(data);
				if (data.result) {
					const newItem = {
						unit : data.unit,
						rate : data.rate,
						spec : data.standard,
						price : data.price,
						maker : data.maker,
						customer : data.customer,
						currency : data.currency,
						won : data.won,
						wantedDate : "<%=dd%>",
						partName : data.partName,
						quantity : data.quantity
					};
					AUIGrid.updateRow(myGridID, newItem, rowIndex);
				}
			}, "GET");
		}
	}

	function readyHandler() {
		const data = new Object();
		data.wantedDate = "<%=dd%>";
		AUIGrid.addRow(myGridID, data);
	}

	function auiKeyDownHandler(event) {
		if (event.keyCode == 13) { // 엔터 키
			var selectedItems = AUIGrid.getSelectedItems(event.pid);
			var rowIndex = selectedItems[0].rowIndex;
			if (rowIndex === AUIGrid.getRowCount(event.pid) - 1) { // 마지막 행인지 여부
				const item = new Object();
				AUIGrid.addRow(myGridID, item, "last");
				return false; // 엔터 키의 기본 행위 안함.
			}
		}
		return true; // 기본 행위 유지
	}

	function deleteRow() {
		const checked = AUIGrid.getCheckedRowItems(myGridID);
		if (checked.length === 0) {
			alert("삭제할 행을 선택하세요.");
			return false;
		}
		AUIGrid.removeCheckedRows(myGridID);
	};

	function addRow() {
		const item = new Object();
		item.wantedDate = "<%=dd%>";
		AUIGrid.addRow(myGridID, item, "last");
	}

	function create() {
		const partName = document.getElementById("partName");
		const unit = document.getElementById("unit");
		const spec = document.getElementById("spec");
		const currency = document.getElementById("currency");
 		const check = document.getElementById("check").value;
 		
 		if(check === "false") {
 			alert("규격 중복 체크를 진행하세요.");
 			return false;
 		}

		if (partName.value === "") {
			alert("품명을 입력하세요.");
			partName.focus();
			return false;
		}

		if (unit.value === "") {
			alert("품명을 입력하세요.");
			unit.focus();
			return false;
		}

		if (spec.value === "") {
			alert("기준단위 입력하세요.");
			spec.focus();
			return false;
		}

		if (currency.value === "") {
			alert("통화를 입력하세요.");
			currency.focus();
			return false;
		}

		const data = AUIGrid.getGridData(myGridID);

		for (let i = 0; i < data.length; i++) {
			const item = data[i];
			const rowIndex = AUIGrid.rowIdToIndex(myGridID, item._$uid);
			if (isNull(item.check) || item.check === "NG") {
				AUIGrid.showToastMessage(myGridID, rowIndex, 0, "ERP 검증을 진행 하세요.");
				return false;
			}

			if (isNull(item.lotNo)) {
				AUIGrid.showToastMessage(myGridID, rowIndex, 1, "LOT NO를 입력하세요.");
				return false;
			}

			if (isNull(item.partNumber)) {
				AUIGrid.showToastMessage(myGridID, rowIndex, 3, "부품번호를 입력하세요.");
				return false;
			}
		}

		if (!confirm("등록 하시겠습니까?")) {
			return false;
		}

		const url = getCallUrl("/unit/create");
		const params = new Object();
		params.partName = partName.value;
		params.unit = unit.value;
		params.spec = spec.value;
		params.currency = currency.value;
		params.customer = customer.value;
		params.maker = maker.value;
		params.data = data;
		console.log(params);
		openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
				opener.loadGridData();
				self.close();
			} else {
				closeLayer();
			}
		});
	}

	function erpCheck() {
		const spec = document.getElementById("spec");
		if(spec.value === "") {
			alert("규격을 입력하세요.");
			spec.focus();
			return false;
		}
		const url = getCallUrl("/erp/erpCheck?spec=" + spec.value);
		openLayer();
		call(url, null, function(data) {
			alert(data.msg);
			if(data.result) {
				const check = data.check;
				document.getElementById("check").value = check;
			}
			closeLayer();
		}, "GET");
	}
	

	const input = document.getElementById("spec");
	input.onkeypress = function() {
		const check = document.getElementById("check").value = "false";
	}
	document.addEventListener("DOMContentLoaded", function() {
		setTimeout(function() {
			toFocus("partName");
		}, 500);
		createAUIGrid(columns);
		AUIGrid.resize(myGridID);
	})

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID);
	});
</script>