grammar Calculator;

query : expression EOF;

expression
   :  LPAREN inner=expression RPAREN # Parentheses
   |  digit=expression POW exponent=expression # Pow
   |  LOG (LPAREN root=SCIENTIFIC_NUMBER RPAREN) (LPAREN exponent=expression RPAREN) # Log
   |  (COS | SIN) LPAREN inner=expression RPAREN # CosOrSin
   |  SQRT LPAREN inner=expression RPAREN # Sqrt
   |  base=expression FACTORIAL # Factorial
   |  left=expression (MULT | DIV)  right=expression # MultOrDiv
   |  left=expression (PLUS | MINUS) right=expression # PlusOrMinus
   |  (PLUS | MINUS)? SCIENTIFIC_NUMBER # Digit
   ;

SCIENTIFIC_NUMBER: NUMBER (E SIGN? UNSIGNED_INTEGER)?;

fragment NUMBER : UNSIGNED_INTEGER + ('.' UNSIGNED_INTEGER +)?;
fragment UNSIGNED_INTEGER : ('0' .. '9')+;
fragment E : 'E' | 'e';
fragment SIGN : ('+' | '-');

LOG: 'log';
SIN: 'sin';
COS: 'cos';
SQRT: 'sqrt';

FACTORIAL : '!';
LPAREN : '(';
RPAREN : ')';
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV : '/';
POINT : '.';
POW : '^';

WS : [ \r\n\t] + -> channel(HIDDEN);