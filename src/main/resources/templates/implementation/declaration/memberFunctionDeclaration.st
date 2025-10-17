memberFunctionDeclaration (accessSpecifier, modifiers, name, parameters, cvQualifiers, refQualifiers, returnType) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
<if(modifiers)><modifiers; separator=" "> <endif>
auto <name> <parameters><\ >
<if(cvQualifiers)><cvQualifiers; separator=" "> <endif>
<if(refQualifiers)><refQualifiers; separator=" "> <endif>
<returnType>;
%>
