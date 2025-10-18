functionDefinition (modifiers1, name, parameters, modifiers2, returnType, body) ::= <%
<if(modifiers1)><modifiers1; separator=" "> <endif>
auto <name> <parameters><\ >
<if(modifiers2)><modifiers2; separator=" "> <endif>
<returnType> <body>
%>
