functionDefinition (modifiers, name, parameters, returnType, body) ::= <%
<if(modifiers)><modifiers; separator=" "> <endif>
auto <name> <parameters> <returnType> <body>
%>
