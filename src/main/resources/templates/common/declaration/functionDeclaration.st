functionDeclaration (modifiers, name, parameters, noexceptSpecifier, returnType) ::= <%
<if(modifiers)><modifiers; separator=" "> <endif>
auto <name> <parameters><\ >
<if(noexceptSpecifier)><noexceptSpecifier> <endif>
<returnType>;
%>
