package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.io.Serializable;

public final class AnalysisSettings implements Serializable {
	private static final long serialVersionUID = 20151202L;

	private final double minSig;
	private final double voidSig;
	private final double pseudoSig;

	public AnalysisSettings(final double sigMin, final double sigVoid, final double sigPseudo) {
		this.minSig = sigMin;
		this.voidSig = sigVoid;
		this.pseudoSig = sigPseudo == 0.0 ? 0.1 : sigPseudo;
	}

	public double minSig() {
		return minSig;
	}

	public double voidSig() {
		return voidSig;
	}

	public double pseudoSig() {
		return pseudoSig;
	}

	@Override
	public String toString() {
		return "AnalysisSettings [minSig=" + minSig + ", voidSig=" + voidSig + ", pseudoSig=" + pseudoSig + "]";
	}
}
