/*
 * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
 * Brandon Sutherlin, Scott Griffin
 * 
 * This software is released under the MIT license
 * (http://www.opensource.org/licenses/mit-license.php)
 * 
 * Created on Feb 12, 2012
 */
package edu.cmu.sv.arinc838.dao;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.arinc.arinc838.FileDefinition;
import com.arinc.arinc838.IntegrityDefinition;
import com.arinc.arinc838.SdfFile;
import com.arinc.arinc838.SoftwareDescription;
import com.arinc.arinc838.ThwDefinition;

import edu.cmu.sv.arinc838.binary.BdfFile;
import edu.cmu.sv.arinc838.dao.IntegrityDefinitionDao.IntegrityType;
import edu.cmu.sv.arinc838.util.Converter;
import edu.cmu.sv.arinc838.validation.ReferenceData;
import edu.cmu.sv.arinc838.writer.BdfWriter;

public class SoftwareDefinitionFileDaoTest {

	private SdfFile swDefFile;
	private IntegrityDefinition integrity;
	private SoftwareDescription description;
	private FileDefinition fileDef;
	private ThwDefinition hardwareDef;

	private SoftwareDefinitionFileDao swDefFileDao;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		integrity = new IntegrityDefinition();
		integrity.setIntegrityType(IntegrityType.CRC16.getType());
		integrity.setIntegrityValue(Converter.hexToBytes("0000000A"));

		description = new SoftwareDescription();
		description.setSoftwarePartnumber(ReferenceData.SOFTWARE_PART_NUMBER_REFERENCE);
		description.setSoftwareTypeDescription("desc");
		description.setSoftwareTypeId(Converter.hexToBytes("0000000A"));

		fileDef = new FileDefinition();
		fileDef.setFileName("file");
		fileDef.setFileIntegrityDefinition(integrity);
		fileDef.setFileSize(1234);

		List<FileDefinition> fileDefs = new ArrayList<FileDefinition>();
		fileDefs.add(fileDef);

		hardwareDef = new ThwDefinition();
		hardwareDef.setThwId("hardware");

		swDefFile = new SdfFile();

		swDefFile.setFileFormatVersion(SoftwareDefinitionFileDao.DEFAULT_FILE_FORMAT_VERSION);
		swDefFile.setSdfIntegrityDefinition(integrity);
		swDefFile.setLspIntegrityDefinition(integrity);
		swDefFile.setSoftwareDescription(description);

		swDefFile.getFileDefinitions().add(fileDef);
		swDefFile.getFileDefinitions().add(fileDef);

		swDefFile.getThwDefinitions().add(hardwareDef);
		swDefFile.getThwDefinitions().add(hardwareDef);

		swDefFileDao = new SoftwareDefinitionFileDao();
	}

	@Test
	public void testFileFormatVersionAccessor() {
		/**
		 * Only has one value currently - hard coded (no need for a setter
		 * test...)
		 */
		assertEquals(swDefFileDao.getFileFormatVersion(), SoftwareDefinitionFileDao.DEFAULT_FILE_FORMAT_VERSION);
	}

	@Test
	public void testSoftwareDefinitionSectionAccessor() {
		SoftwareDescriptionDao refVal = new SoftwareDescriptionDao();
		refVal.setSoftwarePartnumber("SomeJunk");

		swDefFileDao.setSoftwareDescription(refVal);
		assertEquals(swDefFileDao.getSoftwareDescription(), refVal);

		swDefFileDao.setSoftwareDescription(new SoftwareDescriptionDao());
		assertNotEquals(swDefFileDao.getSoftwareDescription(), refVal);
	}

	@Test
	public void testLspIngerityDefinitionAccessor() {
		IntegrityDefinitionDao refVal = new IntegrityDefinitionDao();
		refVal.setIntegrityType(123);

		swDefFileDao.setLspIntegrityDefinition(refVal);
		assertEquals(swDefFileDao.getLspIntegrityDefinition(), refVal);

		swDefFileDao.setLspIntegrityDefinition(new IntegrityDefinitionDao());
		assertNotEquals(swDefFileDao.getLspIntegrityDefinition(), refVal);
	}

	@Test
	public void testSdfIngerityDefinitionAccessor() {
		IntegrityDefinitionDao refVal = new IntegrityDefinitionDao();
		refVal.setIntegrityType(123);

		swDefFileDao.setSdfIntegrityDefinition(refVal);
		assertEquals(swDefFileDao.getSdfIntegrityDefinition(), refVal);

		swDefFileDao.setSdfIntegrityDefinition(new IntegrityDefinitionDao());
		assertNotEquals(swDefFileDao.getSdfIntegrityDefinition(), refVal);
	}

	@Test
	public void testDefaultConstructor() {
		assertEquals(swDefFileDao.getFileFormatVersion(), SoftwareDefinitionFileDao.DEFAULT_FILE_FORMAT_VERSION);
	}

	@Test
	public void testXmlConstructor() {
		swDefFileDao = new SoftwareDefinitionFileDao(swDefFile);

		assertEquals(swDefFileDao.getSoftwareDescription(),
				new SoftwareDescriptionDao(swDefFile.getSoftwareDescription()));
		assertEquals(swDefFileDao.getLspIntegrityDefinition(),
				new IntegrityDefinitionDao(swDefFile.getLspIntegrityDefinition()));
		assertEquals(swDefFileDao.getSdfIntegrityDefinition(),
				new IntegrityDefinitionDao(swDefFile.getSdfIntegrityDefinition()));

		assertEquals(swDefFileDao.getFileDefinitions().size(), swDefFile.getFileDefinitions().size());
		assertEquals(swDefFileDao.getTargetHardwareDefinitions().size(), swDefFile.getThwDefinitions().size());
	}

	@Test
	public void getFileDefinitions() {
		FileDefinitionDao first = mock(FileDefinitionDao.class);
		FileDefinitionDao second = mock(FileDefinitionDao.class);

		swDefFileDao.getFileDefinitions().add(first);
		swDefFileDao.getFileDefinitions().add(second);

		assertEquals(swDefFileDao.getFileDefinitions().size(), 2);
		assertEquals(swDefFileDao.getFileDefinitions().get(0), first);
		assertEquals(swDefFileDao.getFileDefinitions().get(1), second);
	}

	// @Test
	// public void setSoftwareDescription() {
	// SoftwareDescriptionDao newDesc = new SoftwareDescriptionDao();
	// newDesc.setSoftwarePartnumber(DataValidator.generateSoftwarePartNumber("YZT??-ABCD-EFGH"));
	//
	// newDesc.setSoftwareTypeDescription("new desc");
	// newDesc.setSoftwareTypeId(Converter.hexToBytes("0000000A"));
	//
	// swDefFileDao.setSoftwareDescription(newDesc);
	//
	// assertEquals(swDefFileDao.getSoftwareDescription().getSoftwarePartnumber(),
	// newDesc.getSoftwarePartnumber());
	// assertEquals(swDefFileDao.getSoftwareDescription().getSoftwareTypeDescription(),
	// newDesc.getSoftwareTypeDescription());
	// assertEquals(swDefFileDao.getSoftwareDescription().getSoftwareTypeId(),
	// newDesc.getSoftwareTypeId());
	// }
	//
	// @Test
	// public void addTargetHardwareDefinitions() {
	// swDefFileDao.getTargetHardwareDefinitions().clear();
	//
	// TargetHardwareDefinitionBuilder expectedHardwareDefinition = new
	// TargetHardwareDefinitionBuilder();
	//
	// swDefFileDao.getTargetHardwareDefinitions().add(expectedHardwareDefinition);
	// TargetHardwareDefinitionBuilder actualHardwareDefinition =
	// swDefFileDao.getTargetHardwareDefinitions().get(
	// 0);
	// assertEquals(actualHardwareDefinition, expectedHardwareDefinition);
	// }

	@Test
	public void testHasBinaryFileName() {
		String partNumber = "some-part";
		SoftwareDescriptionDao descDao = new SoftwareDescriptionDao();
		descDao.setSoftwarePartnumber(partNumber);
		swDefFileDao.setSoftwareDescription(descDao);

		assertEquals(swDefFileDao.getBinaryFileName(), partNumber.replace("-", "") + ".BDF");
	}

	@Test
	public void testHasXmlFileName() {
		String partNumber = "some-part";
		SoftwareDescriptionDao descDao = new SoftwareDescriptionDao();
		descDao.setSoftwarePartnumber(partNumber);
		swDefFileDao.setSoftwareDescription(descDao);

		assertEquals(swDefFileDao.getBinaryFileName(), partNumber.replace("-", "") + ".XDF");
	}

	@Test
	public void testReadBinaryActualFilesOnDisk() throws Exception {
		BdfWriter writer = new BdfWriter();

		String path = System.getProperty("java.io.tmpdir");

		writer.write(path, swDefFileDao);
		File firstOnDisk = new File(writer.getFilename(swDefFileDao));

		BdfFile file = new BdfFile(firstOnDisk);
		SoftwareDefinitionFileDao actual = new SoftwareDefinitionFileDao(file);

		RandomAccessFile first = new RandomAccessFile(firstOnDisk, "r");
		byte[] firstBytes = new byte[(int) first.length()];
		first.readFully(firstBytes);

		writer.write(path, actual);
		File secondOnDisk = new File(writer.getFilename(actual));
		RandomAccessFile second = new RandomAccessFile(secondOnDisk, "r");
		byte[] secondBytes = new byte[(int) second.length()];
		second.readFully(secondBytes);

		assertEquals(firstBytes, secondBytes);

		firstOnDisk.delete();
		secondOnDisk.delete();
	}

	@Test
	public void testEquals() {
		SoftwareDefinitionFileDao copy = new SoftwareDefinitionFileDao(swDefFile);
		swDefFileDao = new SoftwareDefinitionFileDao(swDefFile);
		
		assertEquals(swDefFileDao, copy);
	}

	@Test
	 public void testHashcode() {
	 assertEquals(swDefFileDao.hashCode(),
	 swDefFileDao.getXmlFileName().hashCode());
	 }
	
	 @Test
	 public void testHashcodeWithNoDescriptionSet() {
	 SoftwareDefinitionFileDao builder = new SoftwareDefinitionFileDao();
	
	 assertEquals(builder.hashCode(), 0);
	 }
}