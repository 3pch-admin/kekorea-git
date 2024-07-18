<%@page import="java.util.Map"%>
<%@page import="e3ps.org.dto.UserDTO"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@page import="wt.session.SessionHelper"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
UserDTO data = (UserDTO) request.getAttribute("data");
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
Map<String, Integer> count = (Map<String, Integer>) request.getAttribute("count");
// System.out.println(count);
%>
<nav class="navbar-default navbar-static-side" role="navigation">
	<div class="sidebar-collapse">
		<ul class="nav metismenu" id="side-menu">
			<li class="nav-header">
				<div class="dropdown profile-element">
					<a href="javascript:index();">
						<span class="block m-t-xs font-bold"><%=data.getName()%></span>
						<span class="text-muted text-xs block">
							<font color="white"><%=data.getDepartment_name()%>-<%=data.getDuty() != null ? data.getDuty() : "지정안됨"%></font>
						</span>
					</a>
				</div>
			</li>
			<li>
				<a href="#">
					<i class="fa fa-envelope"></i>
					<span class="nav-label">나의 업무</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
							<a onclick="tabbar('공지사항', '/notice/list', 'tab1');">공지사항</a>
<!-- 						<a onclick="moveToPage(this, '/notice/list', '> 나의 업무 > 공지사항');">공지사항</a> -->
					</li>
					<li>
						<a onclick="tabbar('검토함', '/workspace/agree', 'tab2');">검토함
<!-- 						<a onclick="moveToPage(this, '/workspace/agree', '> 나의 업무 > 검토함');"> -->
							<span class="label label-info float-right">
								<span id="_agree"><%=count.get("agree")%></span>
							</span>
						</a>
					</li>
					<li>
						<a onclick="tabbar('결재함', '/workspace/approval', 'tab3');">결재함
<!-- 						<a onclick="moveToPage(this, '/workspace/approval', '> 나의 업무 > 결재함');"> -->
							<span class="label label-info float-right">
								<span id="_approval"><%=count.get("approval")%></span>
							</span>
						</a>
					</li>
					<li>
						<a onclick="tabbar('수신함', '/workspace/receive', 'tab4');">수신함
<!-- 						<a onclick="moveToPage(this, '/workspace/receive', '> 나의 업무 > 수신함');"> -->
							
							<span class="label label-info float-right">
								<span id="_receive"><%=count.get("receive")%></span>
							</span>
						</a>
					</li>
					<li>
						<a onclick="tabbar('진행함', '/workspace/progress', 'tab5');">진행함
<!-- 						<a onclick="moveToPage(this, '/workspace/progress', '> 나의 업무 > 진행함');"> -->
							<span class="label label-info float-right">
								<span id="_progress"><%=count.get("progress")%></span>
							</span>
						</a>
					</li>
					<li>
						<a onclick="tabbar('완료함', '/workspace/complete', 'tab6');">완료함
<!-- 						<a onclick="moveToPage(this, '/workspace/complete', '> 나의 업무 > 완료함');"> -->
							<span class="label label-info float-right">
								<span id="_complete"><%=count.get("complete")%></span>
							</span>
						</a>
					</li>
					<li>
						<a onclick="tabbar('반려함', '/workspace/reject', 'tab7');">반려함
<!-- 						<a onclick="moveToPage(this, '/workspace/reject', '> 나의 업무 > 반려함');"> -->
							<span class="label label-info float-right">
								<span id="_reject"><%=count.get("reject")%></span>
							</span>
						</a>
					</li>
					<li>
						<a onclick="tabbar('조직도', '/org/organization', 'tab8');">조직도</a>
<!-- 						<a onclick="moveToPage(this, '/org/organization', '> 나의 업무 > 조직도');">조직도</a> -->
					</li>
					<li>
						<a onclick="javascript:_popup('/org/password');">비밀번호 변경</a>
					</li>
				</ul>
			</li>
			<li>
				<a href="metrics.html">
					<i class="fa fa-pie-chart"></i>
					<span class="nav-label">작번 관리</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
						<a onclick="tabbar('작번 조회', '/project/list', 'tab9');">작번 조회</a>
<!-- 						<a onclick="moveToPage(this, '/project/list', '> 작번 관리 > 작번 조회');">작번 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('나의 작번 조회', '/project/my', 'tab10');">나의 작번</a>
<!-- 						<a onclick="moveToPage(this, '/project/my', '> 작번 관리 > 나의 작번');">나의 작번</a> -->
					</li>
					<li>
						<a onclick="tabbar('템플릿 조회', '/template/list', 'tab11');">템플릿 조회</a>
<!-- 						<a onclick="moveToPage(this, '/template/list', '> 작번 관리 > 템플릿 조회');">템플릿 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('특이사항 조회', '/issue/list', 'tab12');">특이사항 조회</a>
<!-- 						<a onclick="moveToPage(this, '/issue/list', '> 작번 관리 > 특이사항 조회');">특이사항 조회</a> -->
					</li>
				</ul>
			</li>
			<li>
				<a href="#">
					<i class="fa fa-edit"></i>
					<span class="nav-label">도면 관리</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
						<a onclick="tabbar('KE 도면 조회', '/keDrawing/list', 'tab13');">KE 도면 조회</a>
<!-- 						<a onclick="moveToPage(this, '/keDrawing/list', '> 도면 관리 > KE 도면 조회');">KE 도면 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('KEK 도번 조회', '/numberRule/list', 'tab14');">KEK 도번 조회</a>
<!-- 						<a onclick="moveToPage(this, '/numberRule/list', '> 도면 관리 > KEK 도번 조회');">KEK 도번 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('KEK 도면 조회', '/epm/list', 'tab15');">KEK 도면 조회</a>
<!-- 						<a onclick="moveToPage(this, '/epm/list', '> 도면 관리 > KEK 도면 조회');">KEK 도면 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('도면일람표 조회', '/workOrder/list', 'tab16');">도면일람표 조회</a>
<!-- 						<a onclick="moveToPage(this, '/workOrder/list', '> 도면 관리 > 도면일람표 조회');">도면일람표 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('도면일람표 조회', '/workspace/list', 'tab17');">도면 결재 조회</a>
<!-- 						<a onclick="moveToPage(this, '/workspace/list', '> 도면 관리 > 도면 결재 조회')">도면 결재 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('도면 출력', '/epm/print', 'tab18');">도면 출력</a>
<!-- 						<a onclick="moveToPage(this, '/epm/print', '> 도면 관리 > 도면 출력')">도면 출력</a> -->
					</li>
				</ul>
			</li>
			<li>
				<a href="#">
					<i class="fa fa-desktop"></i>
					<span class="nav-label">부품 관리</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
						<a onclick="tabbar('부품 조회', '/part/list', 'tab19');">부품 조회</a>
<!-- 						<a onclick="moveToPage(this, '/part/list', '> 부품 관리 > 부품 조회');">부품 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('코드 생성', '/part/code', 'tab20');">코드 생성</a>
<!-- 						<a onclick="moveToPage(this, '/part/code', '> 부품 관리 > 코드 생성');">코드 생성</a> -->
					</li>
					<li>
						<a onclick="tabbar('부품 일괄 등록 (신규)', '/part/bundle', 'tab21');">부품 일괄 등록 (신규)</a>
<!-- 						<a onclick="moveToPage(this, '/part/bundle', '> 부품 관리 > 부품 일괄 등록 (신규)');">부품 일괄 등록 (신규)</a> -->
					</li>
					<li>
						<a onclick="tabbar('부품 일괄 등록 (첨부파일)', '/part/batch', 'tab22');">부품 일괄 등록 (첨부파일)</a>
<!-- 						<a onclick="moveToPage(this, '/part/batch', '> 부품 관리 > 부품 일괄 등록(첨부파일)');">부품 일괄 등록(첨부파일)</a> -->
					</li>
					<li>
						<a onclick="tabbar('부품 일괄 등록 (PLM)', '/part/plm', 'tab23');">부품 일괄 등록 (PLM)</a>
<!-- 						<a onclick="moveToPage(this, '/part/plm', '> 부품 관리 > 부품 일괄 등록(PLM)');">부품 일괄 등록(PLM)</a> -->
					</li>
					<li>
						<a onclick="tabbar('제작사양서 등록', '/part/spec', 'tab24');">제작사양서 등록</a>
<!-- 						<a onclick="moveToPage(this, '/part/spec', '> 부품 관리 > 제작사양서 등록');">제작사양서 등록</a> -->
					</li>
					<!-- 					<li> -->
					<!-- 						<a href="contacts_2.html">제작사양서 등록</a> -->
					<!-- 					</li> -->
					<!-- 					<li> -->
					<!-- 						<a href="projects.html">UNIT BOM 조회</a> -->
					<!-- 					</li> -->
					<li>
						<a onclick="tabbar('KE 부품 조회', '/kePart/list', 'tab25');">KE 부품 조회</a>
<!-- 						<a onclick="moveToPage(this, '/kePart/list', '> 부품 관리 > KE 부품 조회');">KE 부품 조회</a> -->
					</li>
					<!-- 					<li> -->
					<!-- 						<a href="project_detail.html">UNIT BOM 등록</a> -->
					<!-- 					</li> -->
					<!-- 					<li> -->
					<!-- 						<a href="activity_stream.html">EPLAN 결재</a> -->
					<!-- 					</li> -->
				</ul>
			</li>
			<li>
				<a href="#">
					<i class="fa fa-files-o"></i>
					<span class="nav-label">문서 관리</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
						<a onclick="tabbar('문서 조회', '/doc/list', 'tab26');">문서 조회</a>
<!-- 						<a onclick="moveToPage(this, '/doc/list', '> 문서 관리 > 문서 조회');">문서 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('산출물 조회', '/output/list', 'tab27');">산출물 조회</a>
<!-- 						<a onclick="moveToPage(this, '/output/list', '> 문서 관리 > 산출물 조회');">산출물 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('의뢰서 조회', '/requestDocument/list', 'tab28');">의뢰서 조회</a>
<!-- 						<a onclick="moveToPage(this, '/requestDocument/list', '> 문서 관리 > 의뢰서 조회');">의뢰서 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('회의록 조회', '/meeting/list', 'tab29');">회의록 조회</a>
<!-- 						<a onclick="moveToPage(this, '/meeting/list', '> 문서 관리 > 회의록 조회');">회의록 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('문서 결재', '/doc/register', 'tab30');">문서 결재</a>
<!-- 						<a onclick="moveToPage(this, '/doc/register', '> 문서 관리 > 문서 결재');">문서 결재</a> -->
					</li>
				</ul>
			</li>
			<li>
				<a href="#">
					<i class="fa fa-files-o"></i>
					<span class="nav-label">BOM 관리</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
						<a onclick="tabbar('수배표 조회', '/partlist/list', 'tab31');">수배표 조회</a>
<!-- 						<a onclick="moveToPage(this, '/partlist/list', '> BOM 관리 > 수배표 조회');">수배표 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('T-BOM 조회', '/tbom/list', 'tab32');">T-BOM 조회</a>
<!-- 						<a onclick="moveToPage(this, '/tbom/list', '> BOM 관리 > T-BOM 조회');">T-BOM 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('UNIT BOM 조회', '/unit/list', 'tab33');">UNIT BOM 조회</a>
<!-- 						<a onclick="moveToPage(this, '/unit/list', '> BOM 관리 > UNIT BOM 조회');">UNIT BOM 조회</a> -->
					</li>
				</ul>
			</li>
			<li>
				<a href="#">
					<i class="fa fa-sitemap"></i>
					<span class="nav-label">한국 생산</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
						<a onclick="tabbar('한국 생산', '/korea/list', 'tab34');">한국 생산</a>
<!-- 						<a onclick="moveToPage(this, '/korea/list', '> 한국 생산 > 한국 생산');">한국 생산</a> -->
					</li>
					<li>
						<a onclick="tabbar('CONFIG SHEET 조회', '/configSheet/list', 'tab35');">CONFIG SHEET 조회</a>
<!-- 						<a onclick="moveToPage(this, '/configSheet/list', '> 한국 생산 > CONFIG SHEET 조회');">CONFIG SHEET 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('CIP 조회', '/cip/list', 'tab36');">CIP 조회</a>
<!-- 						<a onclick="moveToPage(this, '/cip/list', '> 한국 생산 > CIP 조회');">CIP 조회</a> -->
					</li>
					<li>
						<a onclick="tabbar('이력 관리 조회', '/history/list', 'tab37');">이력 관리 조회</a>
<!-- 						<a onclick="moveToPage(this, '/history/list', '> 한국 생산 > 이력 관리 조회');">이력 관리 조회</a> -->
					</li>
				</ul>
			</li>
			<li>
				<a href="#">
					<i class="fa fa-sitemap"></i>
					<span class="nav-label">시스템 로그</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
						<a onclick="tabbar('ERP 로그 조회', '/erp/list', 'tab38');">ERP 로그 조회</a>
<!-- 						<a onclick="moveToPage(this, '/erp/list', '> ERP 로그 > ERP 로그');">ERP 로그</a> -->
					</li>
					<li>
						<a onclick="tabbar('에러 로그 조회', '/system/list', 'tab39');">에러 로그 조회</a>
<!-- 						<a onclick="moveToPage(this, '/system/list', '> 에러 로그 > 에러 로그');">에러 로그</a> -->
					</li>
				</ul>
			</li>
			<%
			if (isAdmin) {
			%>
			<li>
				<a href="css_animation.html">
					<i class="fa fa-magic"></i>
					<span class="nav-label">관리자</span>
					<span class="fa arrow"></span>
				</a>
				<ul class="nav nav-second-level collapse">
					<li>
							<a onclick="tabbar('코드 관리 조회', '/commonCode/list', 'tab40');">코드 관리</a>
<!-- 						<a onclick="moveToPage(this, '/commonCode/list', '> 관리자 > 코드 관리');">코드 관리</a> -->
					</li>
					<li>
						<a onclick="tabbar('이력 관리 컬럼 조회', '/specCode/list', 'tab41');">이력 관리 컬럼</a>
<!-- 						<a onclick="moveToPage(this, '/specCode/list', '> 관리자 > 이력 관리 컬럼');">이력 관리 컬럼</a> -->
					</li>
					<li>
						<a onclick="tabbar('CONFIG SHEET 카테고리 조회', '/configSheetCode/list', 'tab42');">CONFIG SHEET 카테고리</a>
<!-- 						<a onclick="moveToPage(this, '/configSheetCode/list', '> 관리자 > CONFIG SHEET 카테고리');">CONFIG SHEET 카테고리</a> -->
					</li>
					<li>
						<a onclick="tabbar('KEK 도번 관리 조회', '/numberRuleCode/list', 'tab43');">KEK 도번 관리</a>
<!-- 						<a onclick="moveToPage(this, '/numberRuleCode/list', '> 관리자 > KEK 도번 관리');">KEK 도번 관리</a> -->
					</li>
					<li>
						<a onclick="tabbar('회의록 템플릿 조회', '/meeting/template', 'tab44');">회의록 템플릿</a>
<!-- 						<a onclick="moveToPage(this, '/meeting/template', '> 관리자 > 회의록 템플릿');">회의록 템플릿</a> -->
					</li>
				</ul>
			</li>
			<%
			}
			%>
		</ul>
	</div>
</nav>
<script type="text/javascript">
	// 페이지 이동
	function tabbar(text, url, tabId) {
		const iframe = document.getElementById("content");
		const iframeWindow = iframe.contentWindow;
		iframeWindow.createNewTab(text, "/Windchill/plm" + url, tabId);
	}
</script>