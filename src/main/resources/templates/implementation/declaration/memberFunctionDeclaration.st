memberFunctionDeclaration (accessSpecifier, name, parameters, cvQualifiers, returnType) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
auto <name> <parameters><\ >
<if(cvQualifiers)><cvQualifiers; separator=" "> <endif>
<returnType>;
%>
