functionDeclaration (modifiers, name, parameters, returnType) ::= <%
<if(modifiers)><modifiers; separator=" "> <endif>
auto <name> <parameters> <returnType>;
%>
