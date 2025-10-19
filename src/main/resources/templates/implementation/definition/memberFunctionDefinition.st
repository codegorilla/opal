memberFunctionDefinition (
  modifiers1,
  className,
  name,
  parameters,
  cvQualifiers,
  refQualifiers,
  modifiers2,
  returnType,
  body
) ::= <%
<if(modifiers1)><modifiers1; separator=" "> <endif>
auto <className>::<name> <parameters><\ >
<if(cvQualifiers)><cvQualifiers; separator=" "> <endif>
<if(refQualifiers)><refQualifiers; separator=" "> <endif>
<if(modifiers2)><modifiers2; separator=" "> <endif>
<returnType> <body>
%>
