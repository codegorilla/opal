functionDefinition (modifiers, name, parameters, noexceptSpecifier, returnType, body) ::= <%
<if(modifiers)><modifiers; separator=" "> <endif>
auto <name> <parameters><\ >
<if(noexceptSpecifier)><noexceptSpecifier> <endif>
<returnType> <body>
%>
