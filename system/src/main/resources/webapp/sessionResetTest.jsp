<HTML>
<HEAD>
<Title>SessionTest</Title>
<SCRIPT></SCRIPT>
</HEAD>
<BODY>
	<h2>SessionTest:</h2>

	<P>Before:<%=request.getSession().getId()%></P>
	<%
		request.getSession().invalidate();
	%>
	<P>After:<%=request.getSession().getId()%></P>

</BODY>
</HTML>
