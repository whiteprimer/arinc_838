package edu.cmu.sv.arinc838.binary;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
import edu.cmu.sv.arinc838.builder.SoftwareDefinitionSectionsBuilder;
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

		//build a string that is too big
		for (int i = 0; i < DataValidator.STR64K_MAX_LENGTH + 1; i++) {
			value.append('c');
		}

		f.writeStr64k(value.toString());
	}
	
	@Test
	public void readUint32() throws Exception{
		long uInt32 = (long) Integer.MAX_VALUE;
		uInt32++;
		
		f.writeUint32(uInt32);
		
		f.seek(0);
		
		assertEquals(f.readUint32(), uInt32);		
	}

	@Test
	public void testWriteFileFormatVersion() throws Exception {
		long expectedFileFormatVersion = SoftwareDefinitionFileBuilder.DEFAULT_FILE_FORMAT_VERSION;

		
		f.writeFileFormatVersion(expectedFileFormatVersion);

		f.seek(BdfFile.BINARY_FILE_FORMAT_VERSION_LOCATION);
		assertEquals(f.readUint32(), expectedFileFormatVersion);
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
		assertEquals(f.getFilePointer(),expected);
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