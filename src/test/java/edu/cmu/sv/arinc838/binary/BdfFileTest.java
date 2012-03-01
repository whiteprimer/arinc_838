package edu.cmu.sv.arinc838.binary;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
import edu.cmu.sv.arinc838.validation.DataValidator;

public class BdfFileTest {

	private BdfFile f;

	@BeforeMethod
	public void setup() throws FileNotFoundException, IOException {
		f = new BdfFile(File.createTempFile("tmpFile", ".bdf"));
	}

	@Test
	public void writeUint32Test() throws Exception {
		// Grab on more than the max value if we get a negative we know we go
		// boom
		long uInt32 = (long) Integer.MAX_VALUE;
		uInt32++;

		f.writeUint32(uInt32);
		assertEquals(f.length(), BdfFile.UINT32_LENGTH);
		f.seek(0);

		long actualUint32 = BdfFile.asUint32(f.readInt());

		assertEquals(actualUint32, uInt32);
	}
	
	@Test
	public void writePlaceholderTest() throws Exception {
		// Grab on more than the max value if we get a negative we know we go
		// boom

		f.writePlaceholder();
		
		assertEquals(f.length(), BdfFile.UINT32_LENGTH);
		f.seek(0);

		long actualUint32 = BdfFile.asUint32(f.readInt());

		assertEquals(actualUint32, 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void writeUint32UsesValidationTest() throws Exception {
		long badUInt32 = Long.MAX_VALUE;

		f.writeUint32(badUInt32);
	}

	/**
	 * We have this test to ensure that the RandomAccessFile class we are
	 * extending behaves as our API requires.
	 * 
	 * There is no validation test because you can't have a bad boolean.
	 * 
	 * @throws Exception
	 */
	@Test
	public void writeBoolean() throws Exception {
		// Grab on more than the max value if we get a negative we know we go
		// boom
		boolean expected = true;

		f.writeBoolean(expected);
		assertEquals(f.length(), BdfFile.BOOLEAN_LENGTH);
		f.seek(0);

		boolean actual = f.readBoolean();

		assertEquals(actual, expected);
	}

	@Test
	public void writeStr64k() throws Exception {
		String ipsum = "lorum ipsum";

		f.writeStr64k(ipsum);
		assertEquals(f.length(), ipsum.toCharArray().length + 2);
		f.seek(0);

		String actual = f.readUTF();

		assertEquals(actual, ipsum);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void writeStr64kUsesValidation() throws Exception {

		StringBuilder value = new StringBuilder();

		// build a string that is too big
		for (int i = 0; i < DataValidator.STR64K_MAX_LENGTH + 1; i++) {
			value.append('c');
		}

		f.writeStr64k(value.toString());
	}
	
	@Test
	public void writeHexbin64k() throws IOException
	{
		byte[] hexBin = new byte[10];
		Arrays.fill(hexBin, (byte)99);
		
		f.writeHexbin64k(hexBin);
		// offset 2 bytes for length
		f.seek(2);
		byte[] hexBin2 = new byte[hexBin.length];
		
		assertEquals(f.read(hexBin2), hexBin.length);
		assertEquals(hexBin2, hexBin);
	}
	
	@Test
	public void writeHexbin64kMax() throws IOException
	{
		byte[] hexBin = new byte[DataValidator.HEXBIN64K_MAX_LENGTH];
		Arrays.fill(hexBin, (byte)104);
		
		f.writeHexbin64k(hexBin);
		// offset 2 bytes for length
		f.seek(2);
		byte[] hexBin2 = new byte[DataValidator.HEXBIN64K_MAX_LENGTH];
		
		assertEquals(f.read(hexBin2), DataValidator.HEXBIN64K_MAX_LENGTH);
		assertEquals(hexBin2, hexBin);
	}

	@Test
	public void readUint32() throws Exception {
		long uInt32 = (long) Integer.MAX_VALUE;
		uInt32++;

		f.writeUint32(uInt32);

		f.seek(0);

		assertEquals(f.readUint32(), uInt32);
	}

	@Test
	public void testWriteFileFormatVersion() throws Exception {
		byte[] expectedFileFormatVersion = SoftwareDefinitionFileBuilder.DEFAULT_FILE_FORMAT_VERSION;

		f.writeFileFormatVersion(expectedFileFormatVersion);

		f.seek(BdfFile.BINARY_FILE_FORMAT_VERSION_LOCATION);
		byte[] actualFileFormatVerion = new byte[4];
		f.read(actualFileFormatVerion);
		assertEquals(actualFileFormatVerion, expectedFileFormatVersion);
	}

	@Test
	public void testWriteSoftwareDescriptionPointer() throws Exception {
		long expected = 42;
		f.seek(expected);

		f.writeSoftwareDescriptionPointer();

		f.seek(BdfFile.SOFTWARE_DESCRIPTION_POINTER_LOCATION);
		assertEquals(f.readUint32(), expected);
	}

	@Test
	public void testWriteTargetDefinitionsPointer() throws Exception {
		long expected = 42;
		f.seek(expected);

		f.writeTargetDefinitionsPointer();

		f.seek(BdfFile.TARGET_DEFINITIONS_POINTER_LOCATION);
		assertEquals(f.readUint32(), expected);
	}

	@Test
	public void testWriteFileDefinitionsPointer() throws Exception {
		long expected = 42;
		f.seek(expected);

		f.writeFileDefinitionsPointer();

		f.seek(BdfFile.FILE_DEFINITIONS_POINTER_LOCATION);
		assertEquals(f.readUint32(), expected);
	}

	@Test
	public void testWriteSdfIntegrityDefinitionPointer() throws Exception {
		long expected = 42;
		f.seek(expected);

		f.writeSdfIntegrityDefinitionPointer();

		f.seek(BdfFile.SDF_INTEGRITY_POINTER_LOCATION);
		assertEquals(f.readUint32(), expected);
	}

	@Test
	public void testSeekAndRestoreFilePointer() throws Exception {
		long expected = 42;
		f.seek(expected);

		f.writeSdfIntegrityDefinitionPointer();
		assertEquals(f.getFilePointer(), expected);
	}

	@Test
	public void testWriteLspIntegrityDefinitionPointer() throws Exception {
		long expected = 42;
		f.seek(expected);

		f.writeLspIntegrityDefinitionPointer();

		f.seek(BdfFile.LSP_INTEGRITY_POINTER_LOCATION);
		assertEquals(f.readUint32(), expected);
	}

}
