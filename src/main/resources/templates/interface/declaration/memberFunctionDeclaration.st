memberFunctionDeclaration (
  accessSpecifier,
  modifiers1,
  name,
  parameters,
  cvQualifiers,
  refQualifiers,
  modifiers2,
  returnType
) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
<if(modifiers1)><modifiers1; separator=" "> <endif>
auto <name> <parameters><\ >
<if(cvQualifiers)><cvQualifiers; separator=" "> <endif>
<if(refQualifiers)><refQualifiers; separator=" "> <endif>
<if(modifiers2)><modifiers2; separator=" "> <endif>
<returnType>;
%>
