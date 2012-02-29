package edu.cmu.sv.arinc838.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.testng.Assert.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import edu.cmu.sv.arinc838.binary.BdfFile;

public class TargetHardwareDefinitionBuilderBinaryTest {
	
	private TargetHardwareDefinitionBuilder thwDefBuilder;
	
	@BeforeMethod
	public void setup() {
		thwDefBuilder = new TargetHardwareDefinitionBuilder();
		thwDefBuilder.setId("ID3");
		thwDefBuilder.getPositions().add("R");
		thwDefBuilder.getPositions().add("L");
	}
	
	@Test
	public void buildBinary() throws FileNotFoundException, IOException {
		BdfFile bdfFile = new BdfFile(File.createTempFile("tmpFile", ".bdf"));
		int bytesWritten = thwDefBuilder.buildBinary(bdfFile);

		// 4 ptr to next + 5 thwID + 4 positions length +
		// 2(4 next position ptr + 3 thw postion) =
		// 13 + 2*7 = 13 + 14 = 27
		assertEquals(bytesWritten, 27);
		
		bdfFile.seek(0);

		long nextThwPointer = bdfFile.readUint32();
		assertEquals(bytesWritten, nextThwPointer);
		assertEquals(bdfFile.readUTF(), "ID3");
		assertEquals(bdfFile.readUint32(), 2); // 2 thw-positions
		assertEquals(bdfFile.readUint32(), 20); // pointer to next thw-position
		assertEquals(bdfFile.readUTF(), "R");
		assertEquals(bdfFile.readUint32(), 0); // pointer to next thw-position
		assertEquals(bdfFile.readUTF(), "L");
	}

	@Test
	public void buildBinaryIsLast() throws FileNotFoundException, IOException {
		thwDefBuilder.setIsLast(true);
		BdfFile bdfFile = new BdfFile(File.createTempFile("tmpFile", ".bdf"));
		int bytesWritten = thwDefBuilder.buildBinary(bdfFile);

		// 4 ptr to next + 5 thwID + 4 positions length +
		// 2(4 next position ptr + 3 thw postion) =
		// 13 + 2*7 = 13 + 14 = 27
		assertEquals(bytesWritten, 27);
		
		bdfFile.seek(0);

		long nextThwPointer = bdfFile.readUint32();
		assertEquals(nextThwPointer, 0);
	}	
}