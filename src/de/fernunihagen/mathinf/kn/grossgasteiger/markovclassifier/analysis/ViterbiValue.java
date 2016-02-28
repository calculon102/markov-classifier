package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.io.Serializable;
import java.math.BigDecimal;

public class ViterbiValue implements Serializable {
	private static final long serialVersionUID = 20151129L;
	
	private final String className;
	private BigDecimal viterbiValue;

	public ViterbiValue(final String className) {
		this.className = className;
		this.viterbiValue = BigDecimal.ZERO;
	}

	public String getClassName() {
		return className;
	}

	public BigDecimal getViterbiValue() {
		return viterbiValue;
	}

	public void setViterbiValue(final BigDecimal newViterbiValue) {
		this.viterbiValue = newViterbiValue;
	}

	@Override
	public String toString() {
		return "ViterbiValue [" + className + ", " + viterbiValue + "]";
	}
}
