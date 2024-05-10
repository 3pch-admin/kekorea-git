<%@page import="wt.util.WTProperties"%>
<%@page import="java.io.OutputStream"%>
<%@page import="java.io.BufferedInputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileInputStream"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
response.reset();
//① 파일명 가져오기
String fileName = request.getParameter("fileName");
String originFileName = request.getParameter("originFileName");

// ② 경로 가져오기
String saveDir = WTProperties.getServerProperties().getProperty("wt.temp");
File file = new File(saveDir + "/" + fileName);
//System.out.println("파일명 : " + saveDir + "/" + fileName);

// ③ MIMETYPE 설정하기
String mimeType = getServletContext().getMimeType(file.toString());
if (mimeType == null) {
	response.setContentType("application/octet-stream");
}

// ④ 다운로드용 파일명을 설정
if (request.getHeader("user-agent").indexOf("MSIE") == -1) {
	originFileName = new String(originFileName.getBytes("UTF-8"), "8859_1");
} else {
	originFileName = new String(originFileName.getBytes("EUC-KR"), "8859_1"); // TODO 다국어
}

// ⑤ 무조건 다운로드하도록 설정
response.setHeader("Content-Disposition", "attachment;filename=\"" + originFileName + "\";");

BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));

out.clear();
out = pageContext.pushBody();

OutputStream outs = response.getOutputStream();
// ⑥ 요청된 파일을 읽어서 클라이언트쪽으로 저장한다.
ServletOutputStream servletOutputStream = response.getOutputStream();

byte b[] = new byte[1024];
int data = 0;

while ((data = (fileInputStream.read(b, 0, b.length))) != -1) {
	servletOutputStream.write(b, 0, data);
}

servletOutputStream.flush();
servletOutputStream.close();
fileInputStream.close();
%>

