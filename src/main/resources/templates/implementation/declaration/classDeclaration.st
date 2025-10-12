classDeclaration (
  classModifiers,
  className,
  baseClause,
  classBody
) ::= <%
export class <className><\ >
<baseClause>
<if(classModifiers)><classModifiers; separator=" "> <endif>
<classBody>;
%>
