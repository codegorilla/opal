classDeclaration (
  classModifiers,
  className,
  classExtendsClause,
  classBody
) ::= <%
class <className><\ >
<classExtendsClause><\ >
<if(classModifiers)><classModifiers; separator=" "> <endif>
<classBody>;
%>

