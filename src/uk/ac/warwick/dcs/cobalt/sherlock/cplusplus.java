/* Generated By:JavaCC: Do not edit this line. cplusplus.java */
package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.Date;

class cplusplus implements cplusplusConstants {

  /**
   * The stream used to save the output file.
   * Added public so it can be closed
   */
  static public PrintStream save;

  /**
   * The new file containing the output of this tokeniser.
   */
  static File outputFile = null;

  /**
   * Keep track of the line number from the file read in. Used to print #line xxx in the outputFile.
   */
  static int lineNo = 1;

  static boolean print=false;
  static boolean seenNewLine=false;

  /**
   * Reinitialise all the variables, and get on with parsing the whatever's in the input stream.
   *
   * @param is - the input stream to parse.
   * @param f - the file to save the results of this tokeniser to.
   */
  static void ReInit(InputStream is, File f) {
    // Don't need to reinitialise the PrintStream, it's done for us.
    cplusplus.outputFile = f;
    cplusplus.lineNo=1;

    cplusplus.seenNewLine=false;
    cplusplus.print=false;

    cplusplus.ReInit(is);
  } // ReInit


  /**
   * Print the current line number into the new file.
   */
  static void printLineNo() {
    if (print) {
      save.println();
      save.print("#line "+lineNo);
      print = false;
    }
  } // printLine


  /**
   * Print the passed string into the new file.
   *
   * @param stringToPrint the string to print.
   */
  static void toPrint(String stringToPrint) {
    printLineNo();
    if (seenNewLine)
      save.println();
    seenNewLine = false;
    save.print(stringToPrint);
  } // toPrint


  public static void main(String args[]) throws ParseException {
    cplusplus parser = new cplusplus(System.in);
    parser.Input();
  }

  static final public void anyOldLines() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case singleLineComment:
      jj_consume_token(singleLineComment);
      break;
    case multiLineComment:
      jj_consume_token(multiLineComment);
      break;
    case hashLine:
      jj_consume_token(hashLine);
      jj_consume_token(INTEGER_LITERAL);
    seenNewLine=false;
    lineNo=Integer.parseInt(getToken(0).image);
    print=true;
    printLineNo();
      break;
    case LPAREN:
      jj_consume_token(LPAREN);
      break;
    case RPAREN:
      jj_consume_token(RPAREN);
      break;
    case LBRACE:
      jj_consume_token(LBRACE);
      break;
    case RBRACE:
      jj_consume_token(RBRACE);
      break;
    case LBRACKET:
      jj_consume_token(LBRACKET);
      break;
    case RBRACKET:
      jj_consume_token(RBRACKET);
      break;
    case SEMICOLON:
      jj_consume_token(SEMICOLON);
      break;
    case COMMA:
      jj_consume_token(COMMA);
      break;
    case DOT:
      jj_consume_token(DOT);
      break;
    case ASSIGN:
      jj_consume_token(ASSIGN);
      break;
    case GT:
      jj_consume_token(GT);
      break;
    case LT:
      jj_consume_token(LT);
      break;
    case BANG:
      jj_consume_token(BANG);
      break;
    case TILDE:
      jj_consume_token(TILDE);
      break;
    case COLON:
      jj_consume_token(COLON);
      break;
    case EQ:
      jj_consume_token(EQ);
      break;
    case LE:
      jj_consume_token(LE);
      break;
    case GE:
      jj_consume_token(GE);
      break;
    case NE:
      jj_consume_token(NE);
      break;
    case SC_OR:
      jj_consume_token(SC_OR);
      break;
    case SC_AND:
      jj_consume_token(SC_AND);
      break;
    case INCR:
      jj_consume_token(INCR);
      break;
    case DECR:
      jj_consume_token(DECR);
      break;
    case PLUS:
      jj_consume_token(PLUS);
      break;
    case MINUS:
      jj_consume_token(MINUS);
      break;
    case STAR:
      jj_consume_token(STAR);
      break;
    case SLASH:
      jj_consume_token(SLASH);
      break;
    case BIT_AND:
      jj_consume_token(BIT_AND);
      break;
    case BIT_OR:
      jj_consume_token(BIT_OR);
      break;
    case XOR:
      jj_consume_token(XOR);
      break;
    case REM:
      jj_consume_token(REM);
      break;
    case SHIFT:
      jj_consume_token(SHIFT);
      break;
    case PLUSASSIGN:
      jj_consume_token(PLUSASSIGN);
      break;
    case MINUSASSIGN:
      jj_consume_token(MINUSASSIGN);
      break;
    case STARASSIGN:
      jj_consume_token(STARASSIGN);
      break;
    case SLASHASSIGN:
      jj_consume_token(SLASHASSIGN);
      break;
    case ANDASSIGN:
      jj_consume_token(ANDASSIGN);
      break;
    case REMASSIGN:
      jj_consume_token(REMASSIGN);
      break;
    case SHIFTASSIGN:
      jj_consume_token(SHIFTASSIGN);
      break;
    case BREAK:
      jj_consume_token(BREAK);
      break;
    case CASE:
      jj_consume_token(CASE);
      break;
    case CATCH:
      jj_consume_token(CATCH);
      break;
    case CLASS:
      jj_consume_token(CLASS);
      break;
    case CONTINUE:
      jj_consume_token(CONTINUE);
      break;
    case _DEFAULT:
      jj_consume_token(_DEFAULT);
      break;
    case DO:
      jj_consume_token(DO);
      break;
    case ELSE:
      jj_consume_token(ELSE);
      break;
    case FALSE:
      jj_consume_token(FALSE);
      break;
    case FINALLY:
      jj_consume_token(FINALLY);
      break;
    case FLOAT:
      jj_consume_token(FLOAT);
      break;
    case FOR:
      jj_consume_token(FOR);
      break;
    case GOTO:
      jj_consume_token(GOTO);
      break;
    case IF:
      jj_consume_token(IF);
      break;
    case IMPLEMENTS:
      jj_consume_token(IMPLEMENTS);
      break;
    case IMPORT:
      jj_consume_token(IMPORT);
      break;
    case INT:
      jj_consume_token(INT);
      break;
    case INTERFACE:
      jj_consume_token(INTERFACE);
      break;
    case NATIVE:
      jj_consume_token(NATIVE);
      break;
    case NEW:
      jj_consume_token(NEW);
      break;
    case MODIFIERS:
      jj_consume_token(MODIFIERS);
      break;
    case NULL:
      jj_consume_token(NULL);
      break;
    case PACKAGE:
      jj_consume_token(PACKAGE);
      break;
    case RETURN:
      jj_consume_token(RETURN);
      break;
    case SHORT:
      jj_consume_token(SHORT);
      break;
    case SUPER:
      jj_consume_token(SUPER);
      break;
    case SWITCH:
      jj_consume_token(SWITCH);
      break;
    case THIS:
      jj_consume_token(THIS);
      break;
    case THROW:
      jj_consume_token(THROW);
      break;
    case TRUE:
      jj_consume_token(TRUE);
      break;
    case TRY:
      jj_consume_token(TRY);
      break;
    case TYPE:
      jj_consume_token(TYPE);
      break;
    case WHILE:
      jj_consume_token(WHILE);
      break;
    case INTEGER_LITERAL:
      jj_consume_token(INTEGER_LITERAL);
      break;
    case FLOATING_POINT_LITERAL:
      jj_consume_token(FLOATING_POINT_LITERAL);
      break;
    case CHARACTER_LITERAL:
      jj_consume_token(CHARACTER_LITERAL);
      break;
    case STRING_LITERAL:
      jj_consume_token(STRING_LITERAL);
      break;
    case IDENTIFIER:
      jj_consume_token(IDENTIFIER);
      break;
    case newLine:
      jj_consume_token(newLine);
      break;
    case others:
      jj_consume_token(others);
      if (seenNewLine)
        cplusplus.save.println();
      seenNewLine=false;
      cplusplus.toPrint(token.image);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  static final public void Input() throws ParseException {
  try    {
      if (outputFile!=null)
        cplusplus.save = new PrintStream(new FileOutputStream(outputFile));
      else
        save = System.out;
  } catch (IOException e) {
      Date day = new Date(System.currentTimeMillis());
      if (outputFile != null) {
        try {
            String file = new String
                (System.getProperty("user.home") + Settings.fileSep
                 + "sherlock.log");
            BufferedWriter bw = new BufferedWriter
                (new FileWriter(file, true));
            bw.write(day + "-" + "Cannot write to: "
                     + outputFile.getAbsolutePath());
            bw.newLine();
            bw.close();
        }
        catch (IOException e2) {
            if (outputFile != null)
                System.err.println(day + "-" + "Cannot write to: "
                                   + outputFile.getAbsolutePath());
        }
      }
      else System.err.println(day + "-" + "Cannot write to System.out");
      return;
  }
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case newLine:
      case hashLine:
      case singleLineComment:
      case multiLineComment:
      case GOTO:
      case TYPE:
      case BREAK:
      case CASE:
      case CATCH:
      case CLASS:
      case CONTINUE:
      case _DEFAULT:
      case DO:
      case ELSE:
      case FALSE:
      case FINALLY:
      case FLOAT:
      case FOR:
      case IF:
      case IMPLEMENTS:
      case IMPORT:
      case INT:
      case INTERFACE:
      case NATIVE:
      case NEW:
      case NULL:
      case PACKAGE:
      case RETURN:
      case SHORT:
      case SUPER:
      case SWITCH:
      case THIS:
      case THROW:
      case TRUE:
      case TRY:
      case WHILE:
      case MODIFIERS:
      case IDENTIFIER:
      case INTEGER_LITERAL:
      case FLOATING_POINT_LITERAL:
      case CHARACTER_LITERAL:
      case STRING_LITERAL:
      case LPAREN:
      case RPAREN:
      case LBRACE:
      case RBRACE:
      case LBRACKET:
      case RBRACKET:
      case SEMICOLON:
      case COMMA:
      case DOT:
      case ASSIGN:
      case GT:
      case LT:
      case BANG:
      case TILDE:
      case COLON:
      case EQ:
      case LE:
      case GE:
      case NE:
      case SC_OR:
      case SC_AND:
      case INCR:
      case DECR:
      case PLUS:
      case MINUS:
      case STAR:
      case SLASH:
      case BIT_AND:
      case BIT_OR:
      case XOR:
      case REM:
      case SHIFT:
      case PLUSASSIGN:
      case MINUSASSIGN:
      case STARASSIGN:
      case SLASHASSIGN:
      case ANDASSIGN:
      case REMASSIGN:
      case SHIFTASSIGN:
      case others:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      anyOldLines();
    }
    jj_consume_token(0);
  }

  static private boolean jj_initialized_once = false;
  static public cplusplusTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[2];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static private int[] jj_la1_3;
  static {
      jj_la1_0();
      jj_la1_1();
      jj_la1_2();
      jj_la1_3();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x779bf61e,0x779bf61e,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x138fc33f,0x138fc33f,};
   }
   private static void jj_la1_2() {
      jj_la1_2 = new int[] {0xffef9ffd,0xffef9ffd,};
   }
   private static void jj_la1_3() {
      jj_la1_3 = new int[] {0x5fc7f,0x5fc7f,};
   }

  public cplusplus(java.io.InputStream stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new cplusplusTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public cplusplus(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new cplusplusTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public cplusplus(cplusplusTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public void ReInit(cplusplusTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector jj_expentries = new java.util.Vector();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  static public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[115];
    for (int i = 0; i < 115; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 2; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
          if ((jj_la1_3[i] & (1<<j)) != 0) {
            la1tokens[96+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 115; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

}
