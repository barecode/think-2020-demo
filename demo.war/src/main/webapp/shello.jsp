<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="demo.war.SHelloUtils"%>
<!DOCTYPE html>
<html>
<head>
 <meta charset="UTF-8">
 <title>Hello Session JSP</title>
 <style>
 body {
  font-family: "IBM Plex Sans", sans-serif;
  font-size: 16px;
 }
 .topBar {
  background-image: linear-gradient(rgb(57, 53, 102) 0%, rgb(47, 38, 73) 100%);
  height: 40px;
  color: white;
  font-size: 30px;
  padding: 2px;
  padding-left: 20px;
 }
.leftBar {
  width: 200px;
  height: 100vh;
  float: left;
  border-right: 3px solid rgb(57,43,102);
 }
 .icText {
  font-size: 18px;
  font-weight: bold;
 }
 .icBlue {
  color: rgb(43,173,206);
 }
 .hidden {
  display: none
 }
 .detailedInfo {
  margin-top: 20px;
  font-size: 14px;
  min-width: 400px;">
 }
 td {
  padding-left: 20px;
 }
 </style>
</head>
<body>

<!-- TOP Context Bar -->
<div class="topBar">WebSphere &amp; Cloud Pak for Applications Demo</div>

<!-- LEFT div -->
<div class="leftBar">
  <div style="margin-top: 10px"><span class="icText">THINK <span class="icBlue">2020</span></span></div>
  <div style="margin-top: 15px;">TA Demo</div>
  <div>ND Qualities of Service</div>

  <div style="font-size: 12px; position: fixed; bottom: 50px">
  <p><b>Demonstration Tools</b></p>
  <form>
  <input type="hidden" name="invalidate" value="true">
  <input type="submit" value="Destroy Session" formaction="session-updater" formmethod="get">
  </form>
  </div>

  <div style="position: fixed; bottom: 10px">&copy;IBM 2020</div>
</div> <!-- left div -->

<!-- RIGHT/MAIN CONTENT div -->
<div style="width: 70%; float: left; padding-left: 10px;">
  <p>Hello! <%=demo.war.SHelloUtils.greeting(session)%></p>

  <div>
  <form action="">
  <label for="save">How are you feeling right now?</label>
  <input type="text" id="save" name="save">
  <input type="submit" value="Submit" formaction="session-updater" formmethod="get">
  </form>
  </div>
  <span style="font-size: 10px; font-style: italic; padding-left: 10px;">Enter a text value and click Submit. The value will be stored in the session to demonstrate session persistence.</span>

  <%
  final String requestHost = SHelloUtils.getRequestHost(request);
  final String localHost = SHelloUtils.getLocalHost(request);

  String localHostClass = "";
  String directClass = "";
  String indirectClass = "";
  if (localHost.equals(requestHost)) {
	  localHostClass = "hidden";
	  indirectClass = "hidden";
  } else {
	  directClass = "hidden";
  }
  %>

  <table class="detailedInfo">
    <caption style="margin-bottom: 5px; font-size: 18px;"><b>Request Details</b></caption>
    <tr><td><b>Request URL</b></td><td><%=request.getRequestURL()%></td></tr>
    <tr><td><b>Request Host</b></td><td><%=requestHost%></td></tr>
    <tr class="<%=localHostClass%>"><td><b>Actual Host</b></td><td><%=localHost%></td></tr>
    <tr><td><b>Server Name</b></td><td><%=SHelloUtils.getServerName()%></td></tr>
  </table>

  <div style="margin-top: 10px; margin-left: 20px; font-size: 14px;">
    <span>Access type:</span>
    <span class="<%=directClass%>">
      Direct<br/>
      The request was sent directly to the application server which processed the request.
    </span>
    <span class="<%=indirectClass%>" >
      Proxy / load balancer<br/>
      The request host does not match the actual host which processed the request.<br/>
      The request was proxied through IHS and processed by the indicated application server.<br/><br/>
      If the actual host or server name changes across multiple requests then the application is being hosted by a cluster.
    </span>
  </div>

  <table class="detailedInfo">
    <caption style="margin-bottom: 5px; font-size: 18px;"><b>Session Information</b></caption>
    <tr><td><b>ID</b></td><td><%=session.getId()%></td></tr>
    <tr><td><b>New?</b></td><td><%=session.isNew()%></td></tr>
    <tr><td><b>Created</b></td><td><%=session.getCreationTime()%></td></tr>
    <tr><td><b>Last Access</b></td><td><%=session.getLastAccessedTime()%></td></tr>
    <tr><td><b>Max Inactive Interval</b></td><td><%=session.getMaxInactiveInterval()%></td></tr>
  </table>

  <table class="detailedInfo">
    <caption style="margin-bottom: 5px; font-size: 18px;"><b>Session Attributes</b></caption>
    <tr><th><b>Attribute Name</b></th><th><b>Attribute Value</b></th></tr>
    <% Enumeration<String> attrs =session.getAttributeNames();
    if (!attrs.hasMoreElements()) {
    %>
    <tr><td><b>No stored attributes!</b></td></tr>
    <% } else {
		while (attrs.hasMoreElements()) {
			String attr = attrs.nextElement();
			out.println("<tr><td> " + attr + "</td><td> " + session.getAttribute(attr) + " </td></tr>");
        }
    } %>
  </table>

  <!-- Bottom footer -->
  <div style="position: fixed; bottom: 10px; font-size: 12px;">
  This is a demo page for THINK 2020.<br/>
  It displays details about the HTTP request, HTTP session and the application server which processed it.
  </div>
</div> <!-- right div -->
</body>
</html>
