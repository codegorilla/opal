memberFunctionDeclaration (accessSpecifier, modifiers, name, parameters, cvQualifiers, returnType) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
<if(modifiers)><modifiers; separator=" "> <endif>
auto <name> <parameters><\ >
<if(cvQualifiers)><cvQualifiers; separator=" "> <endif>
<returnType>;
%>
