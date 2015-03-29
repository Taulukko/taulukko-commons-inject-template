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
		<h2>Session Template:</h2>
		<P>Sessionid:<%=request.getSession().getId() %></p>
		<P>Random:<%=Math.round(Math.random() * 1000000)%></P>
	</BODY>
</HTML> 