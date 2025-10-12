classDeclaration (
  classModifiers,
  className,
  classExtendsClause,
  classBody
) ::= <%
export class <className><\ >
<if(classModifiers)><classModifiers; separator=" "> <endif>
<classExtendsClause> <classBody>;
%>
