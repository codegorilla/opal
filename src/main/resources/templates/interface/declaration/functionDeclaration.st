//auto <functionName> <functionParameters> <functionReturnType>;
//functionDeclaration (modifiers, functionName, functionParameters, functionReturnType) ::= <%

functionDeclaration (modifiers, name, parameters, returnType) ::= <%
<if(modifiers)><modifiers; separator=" "> <endif>
auto <name> <parameters> <returnType>;
%>
