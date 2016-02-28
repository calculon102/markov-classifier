package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BaseFormConverterTest {

	private final String wordToTest;
	private final List<String> expectedResultZerlegung;
	private final String expectedResultBaseformOnly;

	@Parameters(name = "{index}: Test({0})={1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ "Baumschulen", Arrays.asList("baum", "schule"), "Baumschule" },
				{ "Bäume", Arrays.asList("baum"), "Baum" },
				{ "Treetables", Arrays.asList("treetable"), "Treetable" }, // Hmpf. Isn't that great...
				{ "bathrobe", Arrays.asList("bath", "robe"), "bathrobe" },
				{ "earrings", Arrays.asList("earring"), "earring" },
				{ "thunderstorm", Arrays.asList("thunder", "torm"), "thunderstorm" },
				{ "basketball", Arrays.asList("basketball"), "basketball" }, // Doesn't recognize that!?
				{ "zusammenschustern", Arrays.asList("zusammenschu", "stern"), "zusammenschustern" } // Expected failure.
		});
	}

	/**
	 * @param wordToTest Sentence to test.
	 * @param endsWithAbbrevation Expected result.
	 */
	public BaseFormConverterTest(final String wordToTest, final List<String> expectedResultZerlegung, final String expectedResultBaseformOnly) {
		this.wordToTest = wordToTest;
		this.expectedResultZerlegung = expectedResultZerlegung;
		this.expectedResultBaseformOnly = expectedResultBaseformOnly;
	}

	@Test
	public void testBaseformConversionOfWord() {
		final BaseformConverter converter = new BaseformConverter();
		final List<String> baseforms = converter.convertToBaseform(wordToTest, true);
		assertThat("Unexpected result for: " + wordToTest, baseforms, equalTo(expectedResultZerlegung));
	}

	@Test
	public void testBaseformConversionOfNouns() {
		final BaseformConverter converter = new BaseformConverter();
		final List<String> baseforms = converter.convertToBaseform(wordToTest, false);
		assertThat("Unexpected result for: " + wordToTest, baseforms.get(0), equalTo(expectedResultBaseformOnly));
	}

}
