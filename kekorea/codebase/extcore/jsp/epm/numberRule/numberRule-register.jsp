<%@page import="java.sql.Timestamp"%>
<%@page import="wt.org.WTUser"%>
<%@page import="java.util.HashMap"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<%
	
%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				KEK 도번 결재
			</div>
		</td>
		<td class="right">
			<input type="button" value="결재" title="결재" class="blue" onclick="save();">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>
<table class="create-table">
	<colgroup>
		<col width="130">
		<col width="*">
	</colgroup>
	<tr>
		<th class="req lb">결재 제목</th>
		<td class="indent5">
			<input type="text" name="name" id="name" class="width-700">
		</td>
	</tr>
	<tr>
		<th class="req lb">결재 의견</th>
		<td class="indent5">
			<textarea id="description" name="description" rows="5"></textarea>
		</td>
	</tr>
	<tr>
		<th class="req lb">결재</th>
		<td>
			<jsp:include page="/extcore/jsp/common/approval-register.jsp">
				<jsp:param value="" name="oid" />
				<jsp:param value="create" name="mode" />
			</jsp:include>
		</td>
	</tr>
</table>
<br>
<div id="grid_wrap" style="height: 60px; border-top: 1px solid #3180c3;"></div>
<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
<script type="text/javascript">
	let myGridID;
	const data = window.data;
	function _layout() {
		return [ {
			dataField : "number",
			headerText : "도면번호",
			dataType : "string",
			width : 100,
			editable : false,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "lotNo",
			headerText : "LOT",
			dataType : "numeric",
			width : 80,
			formatString : "###0",
			editRenderer : {
				type : "InputEditRenderer",
				onlyNumeric : true,
				maxlength : 4,
			},
			filter : {
				showIcon : true,
				inline : true,
				displayFormatValues : true
			},
		}, {
			dataField : "unitName",
			headerText : "UNIT NAME",
			dataType : "string",
			width : 200,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "name",
			headerText : "도번명",
			dataType : "string",
			width : 250,
			style : "aui-left",
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "businessSector_txt",
			headerText : "사업부문",
			dataType : "string",
			width : 200,
			editable : false,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "drawingCompany_txt",
			headerText : "도면생성회사",
			dataType : "string",
			width : 150,
			editable : false,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "classificationWritingDepartments_txt",
			headerText : "작성부서구분",
			dataType : "string",
			width : 150,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "writtenDocuments_txt",
			headerText : "작성문서구분",
			dataType : "string",
			width : 275,
			filter : {
				showIcon : true,
				inline : true
			}
		} ]
	}

	function createAUIGrid(columnLayout) {
		const props = {
			headerHeight : 30,
			showRowNumColumn : true,
			rowNumHeaderText : "번호",
			showAutoNoDataMessage : false,
			enableFilter : true,
			selectionMode : "multipleCells",
			enableMovingColumn : true,
			showInlineFilter : false,
			useContextMenu : true,
			enableRightDownFocus : true,
			filterLayerWidth : 320,
			filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
			editable : true,
			fixedColumnCount : 1,
			autoGridHeight : true
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		AUIGrid.setGridData(myGridID, data);
	}

	function save() {
		const addRows8 = AUIGrid.getAddedRowItems(myGridID8); // 결재
		const name = document.getElementById("name").value;
		const description = document.getElementById("description").value;
		if (name === "") {
			alert("결재의견을 입력하세요.");
			return false;
		}

		if (addRows8.length === 0) {
			alert("결재선을 지정하세요.");
			_register();
			return false;
		}
		if (!confirm("도번결재를 등록하시겠습니까?")) {
			return false;
		}
		const data = AUIGrid.getGridData(myGridID);
		const params = new Object();
		params.name = name;
		params.description = description;
		params.data = data;
		toRegister(params, addRows8);
		const url = getCallUrl("/numberRule/register");
		openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
				opener.loadGridData();
				self.close();
			}
			closeLayer();
		})
	}

	document.addEventListener("DOMContentLoaded", function() {
		toFocus("name");
		const columns = loadColumnLayout("numberRule-list");
		const contenxtHeader = genColumnHtml(columns);
		createAUIGrid(columns);
		AUIGrid.resize(myGridID);
		createAUIGrid8(columns8);
		AUIGrid.resize(myGridID8);
	});

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID);
		AUIGrid.resize(myGridID8);
	});
</script>