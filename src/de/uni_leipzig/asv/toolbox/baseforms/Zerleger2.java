package de.uni_leipzig.asv.toolbox.baseforms;

/** 
 * @title Prefixkompression - Demo fuer Training und Test von Prefixbaumklassifikatoren 
 * @author Christian Biemann 
 * @version 12.04.2003
 *
 * Aufruf: java LoadClass classify.tree testfile thresh
 * 
 *  Testfile hat Struktur WORT1 <tab> KLASSE1 <CRLF> WORT2 ...
 * neben Evaluierung mit verschiedneen thresholds aird Baum auch in "compr.tree" gespeichert

 */
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.uni_leipzig.asv.utils.Pretree;

public final class Zerleger2 {

	private final Pretree kompvvTree = new Pretree();

	private final Pretree kompvhTree = new Pretree();

	private final Pretree grfTree = new Pretree();

	static boolean d = !true; // debugguing

	private static String reverse(final String torev) {
		return new StringBuilder(torev).reverse().toString();
	}

	public List<String> kZerlegung(String aktwort) {
		if (Zerleger2.d) {
			System.out.print("grf: " + aktwort + "->");
		}
		aktwort = grundFormReduktion(aktwort);
		if (Zerleger2.d) {
			System.out.println(aktwort);
		}

		String zahlStrvv = "", zahlStrvh = "", suffixvv = "", suffixvh = "", vvteil1 = "", vhteil1 = "", vvteil2 = "", vhteil2 = "";
		int zahlvv = 0, zahlvh = 0;
		boolean vhOk, vvOk;

		if (Zerleger2.d) {
			System.out.println("Zerlege " + aktwort);
		}

		final String classvv = kompvvTree.classify(aktwort + "<");
		final String classvh = kompvhTree.classify(Zerleger2.reverse(aktwort) + "<");

		if (Zerleger2.d) {
			System.out.println("VV liefert " + classvv);
			System.out.println("VH liefert " + classvh);
		}

		final List<String> zervv = new ArrayList<>();
		final List<String> zervh = new ArrayList<>();
		zervv.add(aktwort);
		zervh.add(aktwort);
		vvOk = true;
		vhOk = true;
		if (classvv.equals("undecided")) {
			vvOk = false;
		}
		if (classvh.equals("undecided")) {
			vhOk = false;
		}

		if (vvOk) {
			for (int i = 0; i < classvv.length(); i++) {
				final char c = classvv.charAt(i);
				// System.out.println("Parse: "+c+" "+(int)c);
				if ((c < 58) && (c > 47)) {
					zahlStrvv += c;
				} else {
					suffixvv += c;
				}
			} // rof i
		}
		if (vhOk) {
			for (int i = 0; i < classvh.length(); i++) {
				final char c = classvh.charAt(i);
				// System.out.println("Parse: "+c+" "+(int)c);
				if ((c < 58) && (c > 47)) {
					zahlStrvh += c;
				} else {
					suffixvh += c;
				}
			} // rof i
		}

		if (vvOk) {
			zahlvv = new Integer(zahlStrvv).intValue();
		}
		if (vhOk) {
			zahlvh = new Integer(zahlStrvh).intValue();
		}

		if (vvOk) {
			if (zahlvv >= aktwort.length()) {
				vvOk = false;
			}
		}
		;
		if (vhOk) {
			if (zahlvh >= aktwort.length()) {
				vhOk = false;
			}
		}
		;

		if (vvOk) {
			for (int i = 0; i < suffixvv.length(); i++) {
				// if (d) System.out.println("VV matche "+suffixvv.charAt(i)+"
				// und "+aktwort.charAt(zahlvv+i));
				if (aktwort.length() > (zahlvv + i)) {
					if (suffixvv.charAt(i) != aktwort.charAt(zahlvv + i)) {
						vvOk = false;
					}
				} else {
					vvOk = false;
				}
			}
		}
		if (vhOk) {
			for (int i = 0; i < suffixvh.length(); i++) {
				if (suffixvh.charAt(i) != aktwort.charAt(zahlvh + 1 + i)) {
					vvOk = false;
				}
			}
		}

		// nun abschneiden durchf�hren
		if (vvOk) {
			zervv.remove(aktwort);
			vvteil1 = aktwort.substring(0, zahlvv);
			vvteil2 = aktwort.substring(zahlvv + suffixvv.length(), aktwort
					.length());
			zervv.add(vvteil1);
			zervv.add(vvteil2);
			if (Zerleger2.d) {
				System.out.println("VV zerlegt in " + vvteil1 + " " + vvteil2);
			}
			if (vvteil2.length() <= 3) {
				vvOk = false;
			}

		}
		if (vhOk) {
			zervh.remove(aktwort);
			vhteil1 = aktwort.substring(0, aktwort.length() - zahlvh);
			vhteil2 = aktwort.substring(aktwort.length()
					- (zahlvh + suffixvh.length()), aktwort.length());
			zervh.add(vhteil1);
			zervh.add(vhteil2);
			if (Zerleger2.d) {
				System.out.println("VH zerlegt in " + vhteil1 + " " + vhteil2);
			}

			if (vhteil1.length() <= 3) {
				vhOk = false;
			}

		}

		final List<String> retvec = new ArrayList<>();
		if (vvOk && vhOk) { // beide ok
			if (vvteil1.equals(vhteil1)) {
				retvec.add(vvteil1);
				if (vhteil2.length() < vvteil2.length()) {
					retvec.add(vhteil2);
				} else if (vhteil2.length() > vvteil2.length()) {
					retvec.add(vvteil2);
				}
			} else if ((vhteil1.length() - vvteil1.length()) < 3) {
				retvec.add(vvteil1);
				if (vhteil2.length() < vvteil2.length()) {
					retvec.add(vhteil2);
				} else if (vhteil2.length() > vvteil2.length()) {
					retvec.add(vvteil2);
				}
			}
			// sonst 3 teile
			else {
				retvec.add(vvteil1);
				retvec.add(aktwort.substring(vvteil1.length()
						+ suffixvv.length(), aktwort.length() - zahlvh));
				retvec.add(vhteil2);
			}
			if (vvteil2.equals(vhteil2)) {
				retvec.add(vvteil2);
			}

		} else if (vvOk && !vhOk) { // nur vvOK
			retvec.add(vvteil1);
			retvec.add(vvteil2);
		} else if (vhOk && !vvOk) { // nur vhOK
			retvec.add(vhteil1);
			retvec.add(vhteil2);
		} else { // keine Zerlegung gefunden -> lassen
			retvec.add(aktwort);
		}

		if (Zerleger2.d) {
			System.out.println("Ergebnis: " + retvec.toString());
		}

		if (retvec.size() > 1) {
			final List<String> retvec2 = new ArrayList<>();
			for (final String elem : retvec) {
				final List<String> zwischen = kZerlegung(elem);
				for (final String string : zwischen) {
					retvec2.add(string);
				}
			}
			return retvec2;
		} // rof if enum

		return retvec;
	} // end kZerlegung

	public String grundFormReduktion(final String wort) {
		String retwort = wort;
		String anweisungGrf = grfTree.classify(Zerleger2.reverse(wort));
		// System.out.println("Anweisung f�r "+wort+": "+anweisungGrf);
		if (!anweisungGrf.equals("undecided")) {
			final StringTokenizer kommatok = new StringTokenizer(
					anweisungGrf, ",");
			anweisungGrf = kommatok.nextToken(); // nehme bei
			// mehreren
			// nurerstes
			// parsing anweisung
			String zahlStr = new String();
			String suffix = new String();

			for (int i = 0; i < anweisungGrf.length(); i++) {
				final char c = anweisungGrf.charAt(i);
				// System.out.println("Parse: "+c+" "+(int)c);
				if ((c < 58) && (c > 47)) {
					zahlStr += c;
				} else {
					suffix += c;
				}
			} // rof i

			// System.out.println(anweisungGrf+"->"+zahlStr+"-"+suffix+"'");

			int cutpos = new Integer(zahlStr).intValue();
			if (cutpos > retwort.length()) {
				cutpos = retwort.length();
			}
			retwort = retwort.substring(0, retwort.length() - cutpos) + suffix;
		}
		return retwort;

	}

	public void init(final String kompvv, final String kompvh, final String gfred) {
		// Bäume initialisierung
		try {
			kompvvTree.load(kompvv);
			kompvvTree.setIgnoreCase(true);
			kompvvTree.setThresh(0.51);

			kompvhTree.load(kompvh);
			kompvhTree.setIgnoreCase(true); // Trainingsmenge in lowcase :(
			kompvhTree.setThresh(0.51); // weiss nicht?

			grfTree.load(gfred);
			grfTree.setIgnoreCase(true); // Trainingsmenge in lowcase :(
			grfTree.setThresh(0.46); // weiss nicht?

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void init(final InputStream kompvv, final InputStream kompvh, final InputStream gfred) {
		// Bäume initialisierung
		try {
			kompvvTree.load(kompvv);
			kompvvTree.setIgnoreCase(true);
			kompvvTree.setThresh(0.51);

			kompvhTree.load(kompvh);
			kompvhTree.setIgnoreCase(true); // Trainingsmenge in lowcase :(
			kompvhTree.setThresh(0.51); // weiss nicht?

			grfTree.load(gfred);
			grfTree.setIgnoreCase(true); // Trainingsmenge in lowcase :(
			grfTree.setThresh(0.46); // weiss nicht?

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
} // end class Zerleger

