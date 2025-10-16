classDeclaration (
  classModifiers,
  className,
  classExtendsClause,
  classBody
) ::= <%
class <className><\ >
<if(classModifiers)><classModifiers; separator=" "> <endif>
<classExtendsClause> <classBody>;
%>
