<%@page import="com.lowagie.text.Meta"%>
<%@page import="e3ps.doc.meeting.dto.MeetingTemplateDTO"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
MeetingTemplateDTO dto = (MeetingTemplateDTO) request.getAttribute("dto");
%>
<%@include file="/extcore/jsp/common/tinymce.jsp"%>
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				회의록 템플릿 정보
			</div>
		</td>
		<td class="right">
			<input type="button" value="수정" title="수정" class="blue" onclick="modify();">
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
		<th class="lb">회의록 템플릿 제목</th>
		<td class="indent5"><%=dto.getName()%></td>
	</tr>
	<tr>
		<th class="lb">회의록 양식</th>
		<td class="indent5">
			<textarea name="description" id="description" rows="8" readonly="readonly"><%=dto.getContent()%></textarea>
		</td>
	</tr>
</table>
<script type="text/javascript">
	function loadTinymce() {
		tinymce.init({
			selector : 'textarea',
			height : 800,
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

	document.addEventListener("DOMContentLoaded", function() {
		loadTinymce();
	});

	function modify() {
		const oid = document.getElementById("oid").value;
		const url = getCallUrl("/meeting/modify?oid=" + oid);
		document.location.href = url;
	}
</script>