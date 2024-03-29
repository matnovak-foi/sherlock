/* Generated By:JavaCC: Do not edit this line. NaturalParser.java */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.util.Date;

/**
 * Parses a source-code file and places all the comments into sentence objects
 * which are in turn held by a document object.
 */
public class NaturalParser implements NaturalParserConstants {

  /**
   * The stream used to save the output file.
   */
  static ObjectOutputStream save;

  static Document currentDoc = new Document();
  static Sentence currentSentence;
  static String[] sentEnders = {".","?","!",";",":"};

  /**
   * The new file containing the output of this tokeniser.
   */
  static File outputFile = null;

  /**
   * Keep track of the line number from the file read in. Used to print #line xxx in the outputFile.
   */
  static int lineNo = 1;

  static String docName = "";
  static boolean justStartedSentence = false;

  /**
   * Reinitialise all the variables, and get on with parsing the whatever's in the input stream.
   *
   * @param is - the input stream to parse.
   * @param f - the file to save the results of this tokeniser to.
   */
  public static void ReInit(InputStream is, File f, String docName) {
    NaturalParser.currentDoc = new Document();
    NaturalParser.currentSentence = null;
    NaturalParser.outputFile = f;
    NaturalParser.lineNo = 1;
    NaturalParser.docName = docName;
    //NaturalParser.ReInit(is);
  } // ReInit

  public void setOutput(File f, String docName){
    NaturalParser.currentDoc = new Document();
    NaturalParser.currentSentence = null;
    NaturalParser.outputFile = f;
    NaturalParser.lineNo = 1;
    NaturalParser.docName = docName;
  }


  public static void main(String args[]) throws ParseException {
    NaturalParser parser = new NaturalParser(System.in);
    parser.Input();
    Document doc = NaturalParser.currentDoc;
    System.out.println(doc);
  }

/**
 * Handle any tokens during the input.
 */
  final public void anyOldLines() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case newLine:
      jj_consume_token(newLine);
      lineNo++;
      break;
    case number:
      jj_consume_token(number);
      break;
    case word:
      jj_consume_token(word);
      break;
    case space:
      jj_consume_token(space);
      break;
    case character_literal:
      jj_consume_token(character_literal);
      break;
    case string_literal:
      jj_consume_token(string_literal);
      break;
    case others:
      jj_consume_token(others);
      break;
    case sentEnd:
      jj_consume_token(sentEnd);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void Input() throws ParseException {
  try {
    if (outputFile!= null)
      save = new ObjectOutputStream(
                             new FileOutputStream(outputFile));
    else{
      File f = new File(System.getProperty("user.home"), "testing");
      save = new ObjectOutputStream(
                             new FileOutputStream(f));
    }
  } catch (IOException e) {
      Date day = new Date(System.currentTimeMillis());
      if (outputFile != null) {
        try {
            String file = new String
                (System.getProperty("user.home") + File.pathSeparator
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
      case sentEnd:
      case number:
      case word:
      case space:
      case others:
      case character_literal:
      case string_literal:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      anyOldLines();
    }
    jj_consume_token(0);
    // Tidy the document object
    if(NaturalParser.currentSentence != null && !NaturalParser.justStartedSentence)
      NaturalParser.currentDoc.endSentence(token.endLine,
        token.endColumn);
    NaturalParser.currentDoc.finishedParsing();
    NaturalParser.currentDoc.setFileName(NaturalParser.docName);
    try{
      // Write it to the output file.
      save.writeObject(NaturalParser.currentDoc);

      // Finish up.
      save.flush();
      save.close();
    } catch(IOException ioe){
      System.err.println("Error writing to file "+NaturalParser.outputFile);
    }
  }

  public NaturalParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[2];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x1ec06,0x1ec06,};
   }

  public NaturalParser(java.io.InputStream stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new NaturalParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public NaturalParser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new NaturalParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public NaturalParser(NaturalParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  public void ReInit(NaturalParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
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

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[17];
    for (int i = 0; i < 17; i++) {
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
        }
      }
    }
    for (int i = 0; i < 17; i++) {
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

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
