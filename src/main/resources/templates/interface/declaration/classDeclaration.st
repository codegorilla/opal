classDeclaration (
  classModifiers,
  className,
  baseClause,
  classBody
) ::= <%
export class <className><\ >
<if(classModifiers)><classModifiers; separator=" "> <endif>
<baseClause> <classBody>;
%>
