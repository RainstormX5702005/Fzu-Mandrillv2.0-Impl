grammar Mandrill;

COMMENT
	: '#' ~[\r\n]* '\r'? ('\n'|EOF) -> skip
	;

WS
	: [ \t\n\r]+ -> skip
	;

fragment
DIGIT
	: [0-9]
	;

fragment
NONDIGIT
	: [a-zA-Z_]
	;

If
    : 'if'
    ;

Else
    : 'else'
    ;

While
    : 'while'
    ;

Read
    : 'read'
    ;
Put
    : 'put'
    ;

Write
    : 'write'
    ;

Get
    : 'get'
    ;

Func
    : 'func'
    ;

Break
    : 'break'
    ;

Continue
    : 'continue'
    ;

Return
    : 'return'
    ;

Global
    : 'global'
    ;

Local
    : 'local'
    ;

Comma
    : ','
    ;

LeftParen: '(';

RightParen: ')';

LeftBracket: '[';

RightBracket: ']';

LeftBrace: '{';

RightBrace: '}';

Plus
    : '+'
    ;

Minus
    : '-'
    ;

Star
    : '*'
    ;

Slash
    : '/'
    ;

Semicolon
    : ';'
    ;

SingleQuota
    : '\''
    ;

Assign
    : '='
    ;

Equality
    : '=='
    ;

Inequality
    : '!='
    ;

LargeThan
    : '>'
    ;

LessThan
    : '<'
    ;

NoLessThan
    : '>='
    ;

NoMoreThan
    : '<='
    ;

Percentage
    : '%'
    ;

IntegerConstant
	: DIGIT+
	;

Identifier
	: NONDIGIT ( NONDIGIT| DIGIT )*
	;

StringConstant
    : '"' Schar* '"'
    ;

fragment Schar: ~ ["\\\r\n] | EscapeSequence;

CharacterConstant
    : '\'' CChar '\''
    ;

fragment CChar
    : ~['\\\r\n]
    | EscapeSequence
    ;

fragment EscapeSequence
    : SimpleEscapeSequence
    ;

fragment SimpleEscapeSequence
    : '\\' ['"?abfnrtv\\]
    ;

arraySuffix
    : '[' ']'
    ;

program
    : (functionDef | statement)* EOF
    ;

functionDef
    : 'func' arraySuffix? Identifier '(' parameterList? ')' stmtBlock
    ;

parameter
    : Identifier arraySuffix?
    ;

parameterList
    : parameter ( ',' parameter )*
    ;

stmtBlock
    : '{' statement* '}'
    ;

statement
    : assignStatement
    | loopStatement
    | conditionStatement
    | jumpStmt
    | declarationStmt
    | stmtBlock
    | emptyStmt
    ;

declarationStmt
    : scope=('global' | 'local') Identifier arraySuffix? ';'
    ;

jumpStmt
    : ('break' | 'continue' | 'return' expression) ';'
    ;

emptyStmt
    : ';'
    ;

assignStatement
    : lvalue '=' rvalue ';'
    | Identifier arraySuffix '=' rvalue ';'
    ;

lvalue
    : Write
    # printInteger
    | Put
    # printChar
    | Identifier ('[' expression ']')?
    # targetVariable
    ;

rvalue
    : expression
    ;

expression
    : IntegerConstant
    # intLiteral
    | CharacterConstant
    # charLiteral
    | StringConstant
    # stringLiteral
    | Identifier ('[' expression ']')?
    # sourceVariable
    | Identifier '(' argumentList? ')'
    # functionCall
    | Read
    # inputInt
    | Get
    # inputChat
    | '(' expression ')'
    # subgroupExpression
    | expression op=('*'|'/'|'%') expression
    # mulDivModExpression
    | expression op=('+'|'-') expression
    # addSubExpression
    | expression op=('<'|'>'|'>='|'<=') expression
    # comparingExpression
    | expression op=('=='|'!=') expression
    # equalityExpression
    ;

argumentList
    : expression ( ',' expression )*
    ;


loopStatement
    : While '(' expr=expression ')' stmtBlock
    ;

conditionStatement
    : If '(' expr=expression ')' thenStatement=stmtBlock (Else elseStatement=stmtBlock)?
    ;
