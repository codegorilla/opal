memberFunctionDeclaration (accessSpecifier, functionName, functionParameters, functionReturnType) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
auto <functionName> <functionParameters> <functionReturnType>;
%>
