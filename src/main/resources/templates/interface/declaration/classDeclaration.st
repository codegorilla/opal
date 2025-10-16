classDeclaration (modifiers, name, extendsClause, body) ::= <%
class <name><\ >
<if(modifiers)><modifiers; separator=" "> <endif>
<extendsClause><\ >
<body>;
%>
