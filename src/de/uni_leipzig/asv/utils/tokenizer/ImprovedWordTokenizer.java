/*
 * DefaultWordTokenizer.java
 *
 * Created on 16. März 2006, 19:55
 *
 * Diese Klasse ist im Rahmen meiner Diplomarbeit zum Thema
 * "fensterbasierte Kookkurrenzen" entstanden. Die Rechte an dieser
 * Klasse liegen beim Lehrstuhl für
 *
 *      Automatische Sprachverarbeitung
 *      Augustusplatz 10/11
 *      04109 Leipzig
 *
 *      http://www.asv.informatik.uni-leipzig.de
 *
 *      Betreuer    : Prof. Gerhard Heyer, Dipl.-Inf. Stefan Bordag
 *      Konzeption  : Dipl.-Inf. Stefan Bordag, Marco Büchler
 *      Entwicklung : Marco Büchler
 *
 *
 */

package de.uni_leipzig.asv.utils.tokenizer;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author development
 */
public class ImprovedWordTokenizer extends AbstractWordTokenizerImpl {

	String strSentenceNumber = null;

	boolean boolReplaceNumbers = false;

	/** Creates a new instance of DefaultWordTokenizer */
	public ImprovedWordTokenizer() {
	}

	@Override
	public String execute(String strLine) {
		ArrayList<int[]> objWhitespacePositions = getWhitespacePositions(strLine);
		// ConfigurationContainer.println( "Satz: " + strLine );
		Iterator<int[]> objIter = objWhitespacePositions.iterator();

		int intStart = 0;
		int intEnd = 0;
		StringBuffer objBuffer = new StringBuffer();

		while (objIter.hasNext()) {
			intEnd = (objIter.next())[0];

			if (intEnd - intStart > 0) {
				String strWord = strLine.substring(intStart, intEnd);
				// ConfigurationContainer.println( "start: " + strWord );
				objBuffer.append(processPrefix(strWord));
				objBuffer.append(" ");
			}

			intStart = intEnd + 1;
		}

		return objBuffer.toString().trim();
	}

	protected String processPrefix(String strWord) {
		if (strWord == null) {
			return "";
		}
		if (strWord.length() == 0) {
			return strWord;
		}
		if (strWord.length() == 1) {
			return processSingleNumber(strWord);
		}

		StringBuffer objBuffer = new StringBuffer();
		int length = EXPLIZIT_CUT.length;
		int intOffSet = 0;

		for (int i = 0; i < length; i++) {
			if (strWord.startsWith(EXPLIZIT_CUT[i], intOffSet)) {
				intOffSet++;
				i = 0;
			}
		}

		if (intOffSet > 0) {
			for (int i = 0; i < intOffSet; i++) {
				objBuffer.append(strWord.charAt(i));
				objBuffer.append(" ");
			}

			// ConfigurationContainer.println( "hasPrefix: " +
			// strWord.substring(intOffSet) );
			objBuffer.append(processSuffix(strWord.substring(intOffSet)));
			objBuffer.append(" ");
		} else {
			// ConfigurationContainer.println( "!hasPrefix: " + strWord );
			objBuffer.append(processSuffix(strWord));
			objBuffer.append(" ");
		}

		return objBuffer.toString().trim();
	}

	protected String processSuffix(String strWord) {
		if (strWord.length() == 1) {
			return processSingleNumber(strWord);
		}

		StringBuffer objBuffer = new StringBuffer();
		int length = EXPLIZIT_CUT.length;
		int intOffSet = 0;

		int i = 0;
		String strTMPWord = new String(strWord);
		for (i = 0; i < length; i++) {
			if (strTMPWord.endsWith(EXPLIZIT_CUT[i])) {
				intOffSet++;
				i = 0;
				strTMPWord = strTMPWord.substring(0, strTMPWord.length() - 1);
				// ConfigurationContainer.println( "hasSuffix0: " +
				// strWord.substring(0,strWord.length()-1) );
			}
		}

		if (intOffSet > 0) {
			// ConfigurationContainer.println( "hasSuffix: " +
			// strWord.substring(0,strWord.length()-intOffSet) );
			objBuffer.append(processDotAsPrefix(strWord.substring(0, strWord
					.length()
					- intOffSet)));
			objBuffer.append(" ");

			int intStart = strWord.length() - intOffSet;
			for (int j = intStart; j < strWord.length(); j++) {
				objBuffer.append(strWord.charAt(j));
				objBuffer.append(" ");
			}
		} else {
			// ConfigurationContainer.println( "!hasSuffix: " + strWord );
			objBuffer.append(processDotAsPrefix(strWord));
			objBuffer.append(" ");
		}

		return objBuffer.toString().trim();
	}

	protected boolean isSequenceOfDot(String strWord) {
		int length = strWord.length();

		for (int i = 0; i < length; i++) {
			if (strWord.charAt(i) != '.') {
				// ConfigurationContainer.println( "is not Seq of ." );
				return false;
			}
		}

		// ConfigurationContainer.println( "is Seq of ." );
		return true;
	}

	protected String processDotAsPrefix(String strWord) {
		if (strWord.length() == 1 || isSequenceOfDot(strWord)) {
			return processSingleNumber(strWord);
		}

		if (!strWord.startsWith(".")) {
			// ConfigurationContainer.println( "not starts .: " + strWord );
			StringBuffer objBuffer = new StringBuffer();
			objBuffer.append(processDotAsSuffix(strWord));
			objBuffer.append(" ");
			return objBuffer.toString().trim();
		}

		// ConfigurationContainer.println( "starts .: " + strWord.substring(1)
		// );
		StringBuffer objBuffer = new StringBuffer();
		objBuffer.append(".");
		objBuffer.append(" ");
		objBuffer.append(processDotAsSuffix(strWord.substring(1)));

		return objBuffer.toString().trim();
	}

	protected int detectSequenceOfDot(String strWord) {
		int length = strWord.length();
		int returnLength = 0;
		for (int i = length - 1; i >= 0; i--) {
			if (strWord.charAt(i) != '.') {
				return returnLength;
			}
			returnLength++;
		}
		return 0;
	}

	protected String processDotAsSuffix(String strWord) {
		if (strWord.length() == 1 || objAbbrevList.contains(strWord)) {
			// ConfigurationContainer.println( "isAbbrev or not relevant: " +
			// strWord );
			return processSingleNumber(strWord);
		}

		// word not ends with .
		if (!strWord.endsWith(".")) {
			// ConfigurationContainer.println( "!!isAbbr: " +
			// strWord.substring(0,strWord.length()-1) );
			StringBuffer objBuffer = new StringBuffer();
			objBuffer.append(processSuffix2(strWord));
			objBuffer.append(" ");
			return objBuffer.toString().trim();
		}

		// word ends with .

		int intDotSeqSize = detectSequenceOfDot(strWord);

		if (intDotSeqSize > 1) {
			// ConfigurationContainer.println( "isDotSeq: " +
			// strWord.substring(0,strWord.length()-intDotSeqSize) );
			StringBuffer objBuffer = new StringBuffer();
			objBuffer.append(processSuffix2(strWord.substring(0, strWord
					.length()
					- intDotSeqSize)));
			objBuffer.append(" ");

			for (int i = 0; i < intDotSeqSize; i++) {
				objBuffer.append(".");
			}

			return objBuffer.toString().trim();
		}

		if (needsNumberSplit(strWord)) {
			// ConfigurationContainer.println( "!!!!isAbbr: " +
			// strWord.substring(0,strWord.length()-1) );
			StringBuffer objBuffer = new StringBuffer();
			objBuffer.append(processSuffix2(strWord.substring(0, strWord
					.length() - 1)));
			objBuffer.append(" .");
			return objBuffer.toString();
		}

		if (isNumber(strWord)) {
			// ConfigurationContainer.println( "!isAbbr: " +
			// strWord.substring(0,strWord.length()) );
			StringBuffer objBuffer = new StringBuffer();
			objBuffer.append(processSuffix2(strWord.substring(0, strWord
					.length())));
			// objBuffer.append(".");
			return objBuffer.toString();
		}

		// ConfigurationContainer.println( "!!!!!!!!!!!!!isAbbr: " +
		// strWord.substring(0,strWord.length()-1) );
		StringBuffer objBuffer = new StringBuffer();
		objBuffer.append(processSuffix2(strWord.substring(0,
				strWord.length() - 1)));
		objBuffer.append(" .");

		return objBuffer.toString();
	}

	protected String processSuffix2(String strWord) {
		if (strWord.length() == 1) {
			return processSingleNumber(strWord);
		}

		StringBuffer objBuffer = new StringBuffer();
		int length = EXPLIZIT_CUT.length;
		int intOffSet = 0;

		int i = 0;
		String strTMPWord = new String(strWord);
		for (i = 0; i < length; i++) {
			if (strTMPWord.endsWith(EXPLIZIT_CUT[i])) {
				intOffSet++;
				i = 0;
				strTMPWord = strTMPWord.substring(0, strTMPWord.length() - 1);
				// ConfigurationContainer.println( "hasSuffix0: " +
				// strWord.substring(0,strWord.length()-1) );
			}
		}

		if (intOffSet > 0) {
			// ConfigurationContainer.println( "hasSuffix: " +
			// strWord.substring(0,strWord.length()-intOffSet) );
			objBuffer.append(processInfixApostrophe(strWord.substring(0,
					strWord.length() - intOffSet)));
			objBuffer.append(" ");

			int intStart = strWord.length() - intOffSet;
			for (int j = intStart; j < strWord.length(); j++) {
				objBuffer.append(strWord.charAt(j));
				objBuffer.append(" ");
			}
		} else {
			// ConfigurationContainer.println( "!hasSuffix: " + strWord );
			objBuffer.append(processInfixApostrophe(strWord));
			objBuffer.append(" ");
		}

		return objBuffer.toString().trim();
	}

	protected boolean needsSplit(String strWord) {
		int length = strWord.length();

		for (int i = 0; i < length; i++) {
			if (!(Character.isLetter(strWord.charAt(i)) || (Character
					.getType(strWord.charAt(i)) == Character.OTHER_PUNCTUATION))) {
				return false;
			}
		}

		return true;
	}

	protected boolean needsNumberSplit(String strWord) {
		int length = strWord.length();

		for (int i = 0; i < length; i++) {
			if (!(Character.isDigit(strWord.charAt(i)) || (Character
					.getType(strWord.charAt(i)) == Character.OTHER_PUNCTUATION))) {
				// ConfigurationContainer.println( "!needsNUmberSplit1: " +
				// strWord);
				return false;
			}
		}

		/*
		 * if( strWord.endsWith(".") && strWord.indexOf(',') != -1 &&
		 * strWord.indexOf(':') != -1 ){ //ConfigurationContainer.println(
		 * "needsNUmberSplit: " + strWord); return true; }
		 */

		// ConfigurationContainer.println( "!needsNUmberSplit2: " + strWord);
		return false;
	}

	protected boolean isNumber(String strWord) {
		int length = strWord.length();

		for (int i = 0; i < length; i++) {
			if (!(Character.isDigit(strWord.charAt(i))
					|| (Character.getType(strWord.charAt(i)) == Character.OTHER_PUNCTUATION) || strWord
					.charAt(i) == '-')) {
				// ConfigurationContainer.println( "!needsNUmberSplit1: " +
				// strWord);
				return false;
			}
		}

		return true;
	}

	protected String processSingleNumber(String strWord) {
		if (strWord == null) {
			return null;
		}
		if (strWord.length() == 0) {
			return strWord;
		}
		if (boolReplaceNumbers && Character.isDigit(strWord.charAt(0))) {
			return "%N%";
		}

		return strWord;
	}

	/*
	 * protected String processInfixApostrophe( String strWord ){ if(
	 * strWord.length() == 1 ){ return strWord; }
	 * 
	 * int intPos = strWord.indexOf("'");
	 * 
	 * if( intPos >=0 && needsSplit(strWord) ){ StringBuffer objBuffer = new
	 * StringBuffer(); objBuffer.append(
	 * processInfixComma(strWord.substring(0,intPos)) ); objBuffer.append( " ' " );
	 * objBuffer.append(
	 * processInfixComma(strWord.substring(intPos+1,strWord.length())) ); return
	 * objBuffer.toString(); }
	 * 
	 * return processInfixComma(strWord); }
	 */

	protected String processInfixApostrophe(String strWord) {
		if (strWord.length() == 1) {
			return processSingleNumber(strWord);
		}

		int intPos = strWord.indexOf("'");

		if (intPos >= 0 && needsSplit(strWord)) {
			// ConfigurationContainer.println( "split comma: " + strWord );
			StringBuffer objBuffer = new StringBuffer();
			objBuffer.append(processNumber(strWord.substring(0, intPos)));
			objBuffer.append(" ' ");
			objBuffer.append(processNumber(strWord.substring(intPos + 1,
					strWord.length())));
			return objBuffer.toString();
		}

		// ConfigurationContainer.println( "split comma2: " + strWord );
		return processNumber(strWord);
	}

	public String processNumber(String strWord) {
		// ConfigurationContainer.println( "processNumber: " +
		// boolReplaceNumbers + "\t" + isNumber(strWord) );
		if (boolReplaceNumbers && isNumber(strWord)) {
			return "%N%";
		}

		return strWord;
	}

	public void setBoolReplaceNumbers(boolean boolReplaceNumbers) {
		this.boolReplaceNumbers = boolReplaceNumbers;
	}
}
