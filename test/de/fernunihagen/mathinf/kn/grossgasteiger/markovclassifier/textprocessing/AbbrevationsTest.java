package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AbbrevationsTest {

	private final String sentenceToTest;
	private final boolean expectedResult;

	@Parameters(name = "{index}: Test({0})={1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "I am not ending with an abbrevation.", false }, { "I am ending with an abbrevation, it's Advermg.", true }, { "I am ending with an abbrevation, it's Advermg!", true }, { "I am ending with an abbrevation, it's Advermg?", true },
				{ "I am ending with an abbrevation, it's Advermg,", false }, { "I am ending with an abbrevation, it's Advermg:", false }, { "I am ending with an abbrevation, it's Advermg;", false }, { "I am ending with an abbrevation without full-stop, it's Advermg", true }, { "Advermg", true }, { "Advermg.", true },
				{ "abcAdvermg", false }, });
	}

	/**
	 * @param sentenceToTest
	 *            Sentence to test.
	 * @param endsWithAbbrevation
	 *            Expected result.
	 */
	public AbbrevationsTest(final String sentenceToTest, final boolean endsWithAbbrevation) {
		this.sentenceToTest = sentenceToTest;
		this.expectedResult = endsWithAbbrevation;
	}

	@Test
	public void testEndsWithAbbrevation() {
		final Abbrevations abbrevations = Abbrevations.getInstance();
		assertThat("Unexpected result for: " + sentenceToTest, abbrevations.endsWithAbbrevation(sentenceToTest), is(expectedResult));
	}

}
