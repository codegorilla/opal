memberFunctionDeclaration (
  accessSpecifier,
  modifiers1,
  name,
  parameters,
  cvQualifiers,
  refQualifiers,
  noexceptSpecifier,
  returnType,
  modifiers2
) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
<if(modifiers1)><modifiers1; separator=" "> <endif>
auto <name> <parameters><\ >
<if(cvQualifiers)><cvQualifiers; separator=" "> <endif>
<if(refQualifiers)><refQualifiers; separator=" "> <endif>
<if(noexceptSpecifier)><noexceptSpecifier> <endif>
<returnType>
<if(modifiers2)> <modifiers2; separator=" "><endif>;
%>
