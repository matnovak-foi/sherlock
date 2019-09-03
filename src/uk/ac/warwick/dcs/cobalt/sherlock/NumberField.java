package uk.ac.warwick.dcs.cobalt.sherlock;

import javax.swing.*;
import javax.swing.text.*;

/**
 * This enhanced JTextField only allows numbers to be inputted.
 */
public class NumberField
    extends JTextField {
  private int length = 1;
  public NumberField(int columns) {
    super(columns);
  } // NumberField

  public NumberField(int columns, int length) {
    super(columns);
    if (length > 1) {
      this.length = length;
    }
  }

  /**
   * Return the customised Document to use for this NumberField,
   * overriding the default one.
   * @return the documenent for this component.
   */
  protected javax.swing.text.Document createDefaultModel() {
    return new NumberDocument();
  } // createDefaultModel

  /**
   * The document model that only allows a 1-digit numbers to be entered.
   */
  protected class NumberDocument
      extends PlainDocument {
    public void insertString(int offs, String str, AttributeSet a) throws
        BadLocationException {
      if (getLength() == 0 && Character.isDigit(str.charAt(0))) {
        super.insertString(offs, str, a);
        return;
      }
      if (getLength() < length && Character.isDigit(str.charAt(0))) {
        super.insertString(offs, str, a);
      }

      /* this works for 3-digit numbers
         if (getLength() + str.length() < 4) {
         char[] source = str.toCharArray();
         char[] result = new char[source.length];
         int j = 0;
         for (int i = 0; i < result.length; i++)
         if (Character.isDigit(source[i]))
         result[j++] = source[i];
         super.insertString(offs, new String(result, 0, j), a);
         }*/
    }
  } // NumberDocument

} // NumberFiel