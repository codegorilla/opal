memberFunctionDefinition (
  modifiers1,
  className,
  name,
  parameters,
  cvQualifiers,
  refQualifiers,
  noexceptSpecifier,
  returnType,
  modifiers2,
  body
) ::= <%
<if(modifiers1)><modifiers1; separator=" "> <endif>
auto <className>::<name> <parameters><\ >
<if(cvQualifiers)><cvQualifiers; separator=" "> <endif>
<if(refQualifiers)><refQualifiers; separator=" "> <endif>
<if(noexceptSpecifier)><noexceptSpecifier> <endif>
<returnType><\ >
<if(modifiers2)><modifiers2; separator=" "> <endif>
<body>
%>
