<%@page import="e3ps.project.service.ProjectHelper"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.project.Project"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.math.RoundingMode"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="e3ps.project.dto.ProjectDTO"%>
<%@page import="e3ps.project.template.dto.TemplateDTO"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	ProjectDTO dto = (ProjectDTO) request.getAttribute("dto");
	Project project = (Project) request.getAttribute("project");
	boolean isAdmin = (boolean) request.getAttribute("isAdmin");
	WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
	ArrayList<Project> list = (ArrayList<Project>) request.getAttribute("list");
	int eeProgress = (int) request.getAttribute("eProgress");
	int mmProgress = (int) request.getAttribute("mProgress");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<style type="text/css">
select {
	-webkit-border-radius: 2px 5px 5px;
	-moz-border-radius: 2px 5px 5px;
	border-radius: 2px 5px 5px;
	font-size: 12px;
	height: 26px;
	line-height: 26px;
	background: #fff;
	margin: 0 auto;
	box-sizing: content-box;
	border: 1px solid #a6a6a6;
	padding: 0;
	vertical-align: middle;
}
</style>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<%@include file="/extcore/jsp/common/highchart.jsp"%>
</head>
<body style="margin: 0px 0px 0px 5px;">
	<form>
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
		<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
		<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
		<div id="tabs">
			<ul>
				<li>
					<a href="#tabs-1">기본정보</a>
				</li>
				<li>
					<a href="#tabs-2">산출물</a>
				</li>
				<li>
					<a href="#tabs-3">특이사항</a>
				</li>
				<li>
					<a href="#tabs-4">기계 수배표</a>
				</li>
				<li>
					<a href="#tabs-5">전기 수배표</a>
				</li>
				<li>
					<a href="#tabs-6">T-BOM</a>
				</li>
				<li>
					<a href="#tabs-7">통합 수배표</a>
				</li>
				<li>
					<a href="#tabs-8">CIP</a>
				</li>
				<li>
					<a href="#tabs-9">도면 일람표</a>
				</li>
				<li>
					<a href="#tabs-10">CONFIG SHEET</a>
				</li>
				<li>
					<a href="#tabs-11">회의록</a>
				</li>
				<li>
					<a href="#tabs-12">이력관리</a>
				</li>												
			</ul>
			<div id="tabs-1">
				<table class="view-table">
					<%
						// not ruswjr
						if (!dto.isEstimate()) {
					%>
					<colgroup>
						<col width="140">
						<col width="140">
						<col width="140">
						<col width="140">
						<col width="140">
						<col width="140">
						<col width="30">
						<col width="140">
						<col width="140">
					</colgroup>
					<tr>
						<th class="lb rb">KEK 작번</th>
						<th class="rb">거래처</th>
						<th class="rb">설치장소</th>
						<th class="rb">모델</th>
						<th class="rb">발행일</th>
						<th class="rb">요구 납기일</th>
						<td rowspan="4" class="tb-none bb-none" style="width: 30px;">&nbsp;</td>
						<th class="rb" rowspan="2">진행률</th>
						<td rowspan="2" class="center"><%//=dto.getProgress()%>
							<%=ProjectHelper.manager.getKekProgress(project)%>%
						</td>
					</tr>
					<tr>
						<td class="center"><%=dto.getKekNumber()%></td>
						<td class="center"><%=dto.getCustomer_name()%></td>
						<td class="center"><%=dto.getInstall_name()%></td>
						<td class="center"><%=dto.getModel()%></td>
						<td class="center"><%=dto.getPdate_txt()%></td>
						<td class="center"><%=dto.getCustomDate_txt()%></td>
					</tr>
					<tr>
						<th class="lb rb">KE 작번</th>
						<th class="rb">USER ID</th>
						<th class="rb">작번 유형</th>
						<th class="rb">막종 / 막종상세</th>
						<th class="rb" colspan="2">작업 내용</th>
						<th class="rb">기계</th>
						<td class="center"><%=mmProgress%>%
						</td>
					</tr>
					<tr>
						<td class="center"><%=dto.getKeNumber()%></td>
						<td class="center"><%=dto.getUserId()%></td>
						<td class="center"><%=dto.getProjectType_name()%></td>
						<td class="center"><%=dto.getMak_name()%>
							/
							<%=dto.getDetail_name()%></td>
						<td class="center" colspan="2"><%=dto.getDescription()%></td>
						<th>전기</th>
						<td class="center"><%=eeProgress%>%
						</td>
					</tr>
					<%
						} else {
					%>
					<colgroup>
						<col width="140">
						<col width="140">
						<col width="140">
						<col width="140">
						<col width="140">
						<col width="30">
						<col width="140">
						<col width="140">
					</colgroup>
					<tr>
						<th class="lb rb">KEK 작번</th>
						<th class="rb">거래처</th>
						<th class="rb">설치장소</th>
						<th class="rb">발행일</th>
						<th class="rb">요구 납기일</th>
						<td rowspan="4" class="tb-none bb-none" style="width: 30px;">&nbsp;</td>
						<th rowspan="4">진행률</th>
						<td rowspan="4" class="center"><%=dto.getProgress()%>
							<%
								//=ProjectHelper.manager.getKekProgress(project)
							%>%
						</td>
					</tr>
					<tr>
						<td class="center"><%=dto.getKekNumber()%></td>
						<td class="center"><%=dto.getCustomer_name()%></td>
						<td class="center"><%=dto.getInstall_name()%></td>
						<td class="center"><%=dto.getPdate_txt()%></td>
						<td class="center"><%=dto.getCustomDate_txt()%></td>
					</tr>
					<tr>
						<th class="lb rb">KE 작번</th>
						<th class="rb">USER ID</th>
						<th class="rb">작번 유형</th>
						<th class="rb" colspan="2">작업 내용</th>
					</tr>
					<tr>
						<td class="center"><%=dto.getKeNumber()%></td>
						<td class="center"><%=dto.getUserId()%></td>
						<td class="center"><%=dto.getProjectType_name()%></td>
						<td class="indent5" colspan="2"><%=dto.getDescription()%></td>
					</tr>
					<%
						}
					%>
				</table>

				<div class="info-header">
					<img src="/Windchill/extcore/images/header.png">
					작번 상세 정보
				</div>

				<table class="view-table">
					<tr>
						<th class="lb rb">총기간[공수](일)</th>
						<th class="rb">계획 시작일</th>
						<th class="rb">계획 종료일</th>
						<th class="rb">실제 시작일</th>
						<th class="rb">실제 종료일</th>
					</tr>
					<tr>
						<td class="center"><%=dto.getDuration()%>[
							<font color="red"><%=dto.getHoliday()%></font>
							]일
						</td>
						<td class="center"><%=dto.getPlanStartDate_txt()%></td>
						<td class="center"><%=dto.getPlanEndDate_txt()%></td>
						<td class="center"><%=dto.getStartDate_txt()%></td>
						<td class="center"><%=dto.getEndDate_txt()%></td>
					</tr>
					<tr>
						<th class="lb rb">
							총괄 책임자&nbsp;
							<img src="/Windchill/extcore/images/edit.gif" class="edit" onclick="edit();">
						</th>
						<th class="rb">
							세부일정 책임자&nbsp;
							<img src="/Windchill/extcore/images/edit.gif" class="edit" onclick="edit();">
						</th>
						<th class="rb">
							기계&nbsp;
							<img src="/Windchill/extcore/images/edit.gif" class="edit" onclick="edit();">
						</th>
						<th class="rb">
							전기&nbsp;
							<img src="/Windchill/extcore/images/edit.gif" class="edit" onclick="edit();">
						</th>
						<th class="rb">
							SOFT&nbsp;
							<img src="/Windchill/extcore/images/edit.gif" class="edit" onclick="edit();">
						</th>
					</tr>
					<tr>
						<td class="center"><%=dto.getPm()%></td>
						<td class="center"><%=dto.getSubPm()%></td>
						<td class="center"><%=dto.getMachine_name() != null ? dto.getMachine_name() : "지정안됨"%></td>
						<td class="center"><%=dto.getElec_name() != null ? dto.getElec_name() : "지정안됨"%></td>
						<td class="center"><%=dto.getSoft_name() != null ? dto.getSoft_name() : "지정안됨"%></td>
					</tr>
					<%
						String outputTotal = String.format("%,.0f", dto.getOutputTotalPrice());
						String inputTotal = String.format("%,.0f", dto.getTotalPrice());
						BigDecimal outputTotalCounting = new BigDecimal(dto.getOutputTotalPrice());
						BigDecimal inputTotalPriceCounting = new BigDecimal(dto.getTotalPrice());

						int tPgoress = 0;
						if (inputTotalPriceCounting.intValue() != 0) {
							BigDecimal result = outputTotalCounting.divide(inputTotalPriceCounting, 2, RoundingMode.FLOOR);
							tPgoress = (int) (result.doubleValue() * 100);
						}

						String outputMachine = String.format("%,.0f", dto.getOutputMachinePrice());
						String inputOutputMachine = String.format("%,.0f", dto.getMachinePrice());
						BigDecimal outputMachineCounting = new BigDecimal(dto.getOutputMachinePrice());
						BigDecimal inputOutputMachineCounting = new BigDecimal(dto.getMachinePrice());

						int mProgress = 0;
						if (inputOutputMachineCounting.intValue() != 0) {
							BigDecimal result = outputMachineCounting.divide(inputOutputMachineCounting, 2, RoundingMode.FLOOR);
							mProgress = (int) (result.doubleValue() * 100);
						}

						String outputElec = String.format("%,.0f", dto.getOutputElecPrice());
						String inputOutputElec = String.format("%,.0f", dto.getElecPrice());
						BigDecimal outputElecCounting = new BigDecimal(dto.getOutputElecPrice());
						BigDecimal inputOutputElecCounting = new BigDecimal(dto.getElecPrice());

						int eProgress = 0;
						if (inputOutputElecCounting.intValue() != 0) {
							BigDecimal result = outputElecCounting.divide(inputOutputElecCounting, 2, RoundingMode.FLOOR);
							eProgress = (int) (result.doubleValue() * 100);
						}
					%>
					<tr>
						<th class="lb rb">작번상태</th>
						<th class="rb">진행상태</th>
						<%
							if (!dto.isEstimate()) {
						%>
						<th class="rb">작번 TOTAL 금액</th>
						<th class="rb">
							기계 TOTAL 금액&nbsp;
							<img src="/Windchill/extcore/images/edit.gif" class="edit" onclick="money('<%=inputOutputMachine%>', 'm');">
						</th>
						<th class="rb">
							전기 TOTAL 금액&nbsp;
							<img src="/Windchill/extcore/images/edit.gif" class="edit" onclick="money('<%=inputOutputElec%>', 'e');">
						</th>
						<%
							} else {
						%>
						<th class="rb" colspan="3">&nbsp;</th>
						<%
							}
						%>
					</tr>
					<tr>
						<td class="center">
							<select name="kekState" id="kekState" class="width-100">
								<option value="" <%if ("".equals(dto.getKekState())) {%> selected="selected" <%}%>>선택</option>
								<option value="준비" <%if ("준비".equals(dto.getKekState())) {%> selected="selected" <%}%>>준비</option>
								<option value="설계중" <%if ("설계중".equals(dto.getKekState())) {%> selected="selected" <%}%>>설계중</option>
								<option value="설계완료" <%if ("설계완료".equals(dto.getKekState())) {%> selected="selected" <%}%>>설계완료</option>
								<option value="작업완료" <%if ("작업완료".equals(dto.getKekState())) {%> selected="selected" <%}%>>작업완료</option>
								<option value="중단됨" <%if ("중단됨".equals(dto.getKekState())) {%> selected="selected" <%}%>>중단됨</option>
								<option value="취소" <%if ("취소".equals(dto.getKekState())) {%> selected="selected" <%}%>>취소</option>
							</select>
						</td>
						<td class="center"><%=dto.getState()%></td>
						<%
							if (!dto.isEstimate()) {
						%>
						<td class="rb center">
							<font color="blue">
								<b>
									<%=outputTotal%>원
								</b>
							</font>
							/
							<font color="red">
								<b><%=inputTotal%>원
								</b>
							</font>
							/
							<%=tPgoress%>%
						</td>
						<td class="rb center">
							<font color="blue">
								<b>
									<a href="javascript:moneyInfo('m');"><%=outputMachine%>원
									</a>
								</b>
							</font>
							/
							<font color="red">
								<b><%=inputOutputMachine%>원
								</b>
							</font>
							/
							<%=mProgress%>%
						</td>
						<td class="rb center">
							<font color="blue">
								<b>
									<a href="javascript:moneyInfo('e');"><%=outputElec%>원
									</a>
								</b>
							</font>
							/
							<font color="red">
								<b><%=inputOutputElec%>원
								</b>
							</font>
							/
							<%=eProgress%>%
						</td>
						<%
							} else {
						%>
						<td colspan="3">&nbsp;</td>
						<%
							}
						%>
					</tr>
				</table>

				<div class="info-header">
					<img src="/Windchill/extcore/images/header.png">
					참조 작번 정보
				</div>


				<table class="view-table">
					<tr>
						<th class="lb rb">KEK 작번</th>
						<th class="rb">작번유형</th>
						<th class="rb">작업내용</th>
						<th class="rb">막종 / 막종상세</th>
						<th class="rb">거래처 / 설치장소</th>
						<th class="rb">발행일</th>
						<th class="rb">요청납기일</th>
					</tr>
					<%
						for (Project p : list) {
					%>
					<tr>
						<td class="center"><%=p.getKekNumber()%></td>
						<td class="center"><%=p.getProjectType().getName()%></td>
						<td class="center"><%=p.getDescription()%></td>
						<td class="center"><%=p.getMak() != null ? p.getMak().getName() : ""%>
							/
							<%=p.getDetail() != null ? p.getDetail().getName() : ""%></td>
						<td class="center"><%=p.getCustomer().getName()%>
							/
							<%=p.getInstall() != null ? p.getInstall().getName() : ""%></td>
						<td class="center"><%=CommonUtils.getPersistableTime(p.getPDate())%></td>
						<td class="center"><%=CommonUtils.getPersistableTime(p.getCustomDate())%></td>
					</tr>
					<%
						}
						if (list.size() == 0) {
					%>
					<tr>
						<td class="center" colspan="7">
							<font color="red">
								<b>참조 작번이 없습니다.</b>
							</font>
						</td>
					</tr>
					<%
						}
					%>
				</table>

				<br>
				<div id="_chart" style="height: 340px;"></div>
				<script type="text/javascript">
					Highcharts.chart('_chart', {
						chart : {
							type : 'column'
						},
						title : {
							text : '작번 TOTAL 금액 차트(수배표/입력)',
						},
						subtitle : {
							text : "<%=dto.getKekNumber()%> / <%=dto.getKeNumber()%>",
						},
						tooltip : {
							headerFormat : '<span style="font-size:10px">{point.key}</span><table>',
							pointFormat : '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' + '<td style="padding:0"><b>{point.y:.1f} mm</b></td></tr>',
							footerFormat : '</table>',
							shared : true,
							useHTML : true
						},
						xAxis : {
							categories : [ '작번 TOTAL 금액(수배표)', '작번 TOTAL 금액(입력)' ],
							crosshair : true
						},
						yAxis : {
							min : 0,
							title : {
								text : '원'
							}
						},
						plotOptions : {
							column : {
								pointPadding : 0.2,
								borderWidth : 0
							}
						},
						tooltip : {
							headerFormat : '<span style="font-size:10px">{point.key}</span><table>',
							pointFormat : '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' + '<td style="padding:0"><b>{point.y:%,.0f} 원</b></td></tr>',
							footerFormat : '</table>',
							shared : true,
							useHTML : true
						},
						series : [ {
							name : '작번 TOTAL 금액',
							data : [
				<%=dto.getOutputTotalPrice()%>
					,
				<%=dto.getTotalPrice()%>
					]
						}, {
							name : '기계 TOTAL 금액',
							data : [
				<%=dto.getOutputMachinePrice()%>
					,
				<%=dto.getMachinePrice()%>
					]
						}, {
							name : '전기 TOTAL 금액',
							data : [
				<%=dto.getOutputElecPrice()%>
					,
				<%=dto.getElecPrice()%>
					]
						} ]
					});
				</script>
			</div>
			<div id="tabs-2">
				<iframe style="height: 800px;" src="/Windchill/plm/project/outputTab?oid=<%=dto.getOid()%>"></iframe>
			</div>
			<div id="tabs-3">
				<iframe style="height: 800px;" src="/Windchill/plm/project/issueTab?oid=<%=dto.getOid()%>"></iframe>
			</div>
			<div id="tabs-4">
				<iframe style="height: 800px;" src="/Windchill/plm/project/partlistTab?oid=<%=dto.getOid()%>&invoke=m"></iframe>
			</div>
			<div id="tabs-5">
				<iframe style="height: 800px;" src="/Windchill/plm/project/partlistTab?oid=<%=dto.getOid()%>&invoke=e"></iframe>
			</div>
			<div id="tabs-6">
				<iframe style="height: 800px;" src="/Windchill/plm/project/tbomTab?oid=<%=dto.getOid()%>"></iframe>
			</div>
			<div id="tabs-7">
				<iframe style="height: 800px;" src="/Windchill/plm/project/partlistTab?oid=<%=dto.getOid()%>&invoke=a"></iframe>
			</div>
			<div id="tabs-8">
				<iframe style="height: 800px;" src="/Windchill/plm/project/cipTab?oid=<%=dto.getOid()%>&invoke=a"></iframe>
			</div>
			<div id="tabs-9">
				<iframe style="height: 800px;" src="/Windchill/plm/project/workOrderTab?oid=<%=dto.getOid()%>&invoke=a"></iframe>
			</div>
			<div id="tabs-10">
				<iframe style="height: 800px;" src="/Windchill/plm/project/configSheetTab?oid=<%=dto.getOid()%>"></iframe>
			</div>
			<div id="tabs-11">
				<iframe style="height: 800px;" src="/Windchill/plm/project/meetingTab?oid=<%=dto.getOid()%>"></iframe>
			</div>
			<div id="tabs-12">
				<iframe style="height: 800px;" src="/Windchill/plm/project/historyTab?oid=<%=dto.getOid()%>"></iframe>
			</div>				
		</div>
		<script type="text/javascript">
		var projectKey = '<%= project%>';
		console.log("project :"+projectKey);
		
			function money(money, type) {
				const oid = document.getElementById("oid").value;
				const url = getCallUrl("/project/money?oid=" + oid + "&money=" + money + "&type=" + type);
				popup(url, 500, 300);
			}

			function edit() {
				const oid = document.getElementById("oid").value;
				const url = getCallUrl("/project/editUser?oid=" + oid);
				popup(url, 500, 300);
			}

			function moneyInfo(invoke) {
				const oid = document.getElementById("oid").value;
				const url = getCallUrl("/partlist/moneyInfo?oid=" + oid + "&invoke=" + invoke);
				popup(url);
			}

			$("#kekState").change(function() {
				const value = $(this).val();
				if (value === "") {
					return false;
				}
				if (!confirm("작번상태를 변경하시겠습니까?")) {
					return false;
				}
				const oid = document.getElementById("oid").value;
				const url = getCallUrl("/project/state");
				const params = {
					oid : oid,
					kekState : value
				};
				parent.parent.openLayer();
				call(url, params, function(data) {
					alert(data.msg);
					if (data.result) {
						// 						document.location.reload();
						parent.parent.document.location.reload();
					} else {
						parent.parent.closeLayer();
					}
				})
			})

			document.addEventListener("DOMContentLoaded", function() {
				$("#tabs").tabs();
				selectbox("kekState");
				// 태스크 트리가 늦게 로딩된다...
				// 				parent.parent.closeLayer();

			})
		</script>
	</form>
</body>
</html>