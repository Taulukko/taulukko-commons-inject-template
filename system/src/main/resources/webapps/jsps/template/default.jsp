<HTML>
	<HEAD>
		<Title>Template:: <inject selector="title"/></Title>	
		<SCRIPT>
			//head script 
			<inject selector="head script"/>
		</SCRIPT>
	</HEAD>
	<BODY>
		<P>Template Content Head</P>
		<inject selector="body"/>
		<P>Template Content Foot</P>
		<h2>SessionInfo:</h2>
		<P><%=request.getSession().getId() %><p>
	</BODY>
</HTML> 