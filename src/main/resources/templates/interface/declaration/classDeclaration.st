classDeclaration (modifiers, name, extendsClause, body) ::= <%
class <name><\ >
<if(modifiers)><modifiers> <endif>
<extendsClause><\ >
<body>;
%>
