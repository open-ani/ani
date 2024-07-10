grammar BBCode;

//options { tokenVocab=BBCodeLexer; }

file: section EOF;

section : element+ ;

element: 
    b | i | u | s | code | 
    url | url_named | img | quote | size | mask | bgm_sticker | text_stiker | 
    plain;

plain: (TEXT | NUMBER | '(' | ')' | '[' | '/' | ']' | '=' | ',')+;

b: '[b]' content=section? '[/b]'; 
i: '[i]' content=section? '[/i]';
u: '[u]' content=section? '[/u]';
s: '[s]' content=section? '[/s]';
code: '[code]' content=section? '[/code]';
mask: '[mask]' content=section? '[/mask]';
quote: '[quote]' content=section? '[/quote]';
size: '[size=' value=NUMBER ']' content=section? '[/size]';

bgm_sticker: '(bgm' id=NUMBER ')';
text_stiker: 
      '(=A=)'
    | '(=w=)'
    | '(-w=)'
    | '(S_S)'
    | '(=v=)'
    | '(@_@)'
    | '(=W=)'
    | '(TAT)'
    | '(T_T)'
    | '(=\'=)'
    | '(=3=)'
    | '(= =\')'
    | '(=///=)'
    | '(=.,=)'
    | '(:P)'
    | '(LOL)';

url: '[url]' href=plain? '[/url]';
url_named: '[url=' href=attribute_value ']' content=section? '[/url]';
img: '[img]' content=section? '[/img]';

attribute_value: quoted=QUOTED | unquoted=TEXT;

// Lexer

NUMBER: [0-9]+ ;

QUOTED
   : '"' (ESC | SAFECODEPOINT)* '"'
   ;

// Not quoted
TEXT: ~[[\]()]+ ;

fragment ESC
   : '\\' (["\\/bfnrt])
   ;

fragment SAFECODEPOINT
   : ~ ["\\\u0000-\u001F]
   ;