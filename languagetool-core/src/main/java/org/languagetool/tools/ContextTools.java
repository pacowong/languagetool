/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.tools;

/**
 * Helper class to mark errors in text.
 */
public class ContextTools {

  private int contextSize = 40;
  private boolean escapeHtml = true;
  private String errorMarkerStart = "<b><font bgcolor=\"#ff8b8b\">";
  private String errorMarkerEnd = "</font></b>";

  public ContextTools() {
  }

  public String getContext(final int fromPos, final int toPos, final String contents) {
    final String text = contents.replace('\n', ' ');
    // calculate context region, i.e. extend the both ends by contextSize:
    int startContent = fromPos - contextSize;
    String prefix = "...";
    String postfix = "...";
    String markerPrefix = "   ";
    if (startContent < 0) {
      prefix = "";
      markerPrefix = "";
      startContent = 0;
    }
    int endContent = toPos + contextSize;
    final int textLength = text.length();
    if (endContent > textLength) {
      postfix = "";
      endContent = textLength;
    }
    final StringBuilder marker = getMarker(fromPos, toPos, textLength + prefix.length());
    // now build context string plus marker:
    final StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    sb.append(text.substring(startContent, endContent));
    final String markerStr = markerPrefix
        + marker.substring(startContent, endContent); // prefix+text length
    sb.append(postfix);
    final int startMark = markerStr.indexOf('^');
    final int endMark = markerStr.lastIndexOf('^');
    String result = sb.toString();
    if (escapeHtml) {
      final String escapedErrorPart = StringTools.escapeHTML(result.substring(startMark, endMark + 1))
              .replace(" ", "&nbsp;");   // make sure whitespace errors are visible
      result = StringTools.escapeHTML(result.substring(0, startMark))
          + errorMarkerStart
          + escapedErrorPart
          + errorMarkerEnd + StringTools.escapeHTML(result.substring(endMark + 1));
    } else {
      result = result.substring(0, startMark) + errorMarkerStart
          + result.substring(startMark, endMark + 1) + errorMarkerEnd
          + result.substring(endMark + 1);
    }
    return result;
  }

  /**
   * Get a plain text context that uses {@code ^} characters in a new line as a marker of the
   * given string region. Ignores {@link #setEscapeHtml(boolean)}.
   * @since 2.3
   */
  public String getPlainTextContext(int fromPos, int toPos, final String contents) {
    // mark the string from fromPos to toPos-1 and save it in a marker string
	  final String text = contents.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
	  final int textLength = text.length();
	  
	  // check the input range
	  fromPos = Math.max(0,fromPos);
	  toPos = Math.min(toPos,textLength);
	  
	  // construct the marked string
	  StringBuilder sbTruncated = new StringBuilder(), sbMarkers = new StringBuilder();
	  
	  // starting part
	  int startContent = Math.max(0,fromPos-contextSize);
	  if (startContent > 0){
		  sbTruncated.append("..."); // Prefix
		  sbMarkers.append("   ");
	  }

	  // middle part with markers
	  sbMarkers.append(repeatChar(' ', fromPos-startContent));
	  sbMarkers.append(repeatChar('^', toPos-fromPos));
	  
	  int endContent = Math.min(toPos+contextSize,textLength);
	  sbTruncated.append(text.substring(startContent, endContent));
	  
	  // ending part
	  if (endContent < contents.length()-1){
		  sbTruncated.append("..."); // Suffix
	  }
	  
	  if (endContent > toPos){
		sbMarkers.append(repeatChar(' ', endContent-toPos));
	  }

    // now build context string plus marker:
    return sbTruncated.toString() + '\n' + sbMarkers.toString();
  }

  /**
   * Set the string used to mark the beginning of an error, e.g. {@code <span class="error">}
   */
  public void setErrorMarkerStart(String errorMarkerStart) {
    this.errorMarkerStart = errorMarkerStart;
  }

  /**
   * Set the string used to mark the end of an error, e.g. {@code </span>}
   */
  public void setErrorMarkerEnd(String errorMarkerEnd) {
    this.errorMarkerEnd = errorMarkerEnd;
  }

  /**
   * The context size of the error. This many characters of the original text will be used
   * from the left and from the right context of the error.
   */
  public void setContextSize(int contextSize) {
    this.contextSize = contextSize;
  }

  /**
   * Whether HTML special characters should be escaped.
   */
  public void setEscapeHtml(boolean escapeHtml) {
    this.escapeHtml = escapeHtml;
  }

  private StringBuilder getMarker(int fromPos, int toPos, int textLength) {
    // make "^" marker. inefficient but robust implementation:
    final StringBuilder marker = new StringBuilder(textLength);
    for (int i = 0; i < textLength; i++) {
      if (i >= fromPos && i < toPos) {
        marker.append('^');
      } else {
        marker.append(' ');
      }
    }
    return marker;
  }
  
  private String repeatChar(char c, int n){
    char[] repeat = new char[number];
    Arrays.fill(repeat, c);
    return new String(repeat);
  }
}
