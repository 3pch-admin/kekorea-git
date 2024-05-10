<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.doc.meeting.Meeting"%>
<%@page import="e3ps.doc.meeting.dto.MeetingDTO"%>
<%@page import="com.lowagie.text.Meta"%>
<%@page import="e3ps.doc.meeting.dto.MeetingTemplateDTO"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	boolean isAdmin = (boolean) request.getAttribute("isAdmin");
	MeetingDTO dto = (MeetingDTO) request.getAttribute("dto");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<%@include file="/extcore/jsp/common/tinymce.jsp"%>
</head>
<body onload="loadTinymce();">
	<form>
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="oid" id="oid" value="<%=dto != null ? dto.getOid() : ""%>">

		<table class="view-table">
			<colgroup>
				<col style="width: 10%;">
				<col style="width: 40%;">
				<col style="width: 10%;">
				<col style="width: 40%;">
			</colgroup>
			<%
				if(dto != null) {
			%>
			<tr>
				<th class="lb">회의록 제목</th>
				<td class="indent5"><%=dto.getName()%></td>
				<th>회의록 템플릿</th>
				<td class="indent5"><%=dto.getTname() != null ? dto.getTname() : ""%></td>
			</tr>
			<tr>
				<th class="lb">내용</th>
				<td class="indent5" colspan="3">
					<textarea rows="5" readonly="readonly"><%=dto.getContent()%></textarea>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5" colspan="3">
					<jsp:include page="/extcore/jsp/common/secondary-view.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
					</jsp:include>
				</td>
			</tr>
			<%
				} else {
			%>
			<tr>
				<td class="lb center" colspan="4"><font color="red"><b>해당 작번에 등록된 회의록이 없습니다.</b></font></td>
			</tr>			
			<%
				}
			%>
		</table>
		<script type="text/javascript">
		
		function loadTinymce() {
			tinymce.init({
				selector : 'textarea',
				height : 600,
				menubar : false,
				statusbar : false,
				language : 'ko_KR',
				toolbar : false,
				readonly : true,
				setup : function(editor) {
					editor.on('init', function() {
						const content = editor.getContent();
						editor.setContent(content);
					});
				}
			});
		}
		</script>
	</form>
</body>
</html>