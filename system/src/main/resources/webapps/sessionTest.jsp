<HTML>
	<HEAD>
		<Title>SessionTest</Title>
		<SCRIPT></SCRIPT>
	</HEAD>
	<BODY>
		<h2>SessionTest:</h2>
		<P><%=request.getSession().getId() %><p>
		<%
			request.getSession().invalidate();
		%>
	</BODY>
</HTML> 