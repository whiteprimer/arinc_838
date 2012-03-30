/*
 * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
 * Brandon Sutherlin, Scott Griffin
 * 
 * This software is released under the MIT license
 * (http://www.opensource.org/licenses/mit-license.php)
 * 
 * Created on Feb 12, 2012
 */
package edu.cmu.sv.arinc838.builder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.arinc.arinc838.FileDefinition;
import com.arinc.arinc838.IntegrityDefinition;
import com.arinc.arinc838.SdfFile;
import com.arinc.arinc838.SoftwareDescription;
import com.arinc.arinc838.ThwDefinition;

import edu.cmu.sv.arinc838.binary.BdfFile;
import edu.cmu.sv.arinc838.dao.FileDefinitionDao;
import edu.cmu.sv.arinc838.dao.IntegrityDefinitionDao;
import edu.cmu.sv.arinc838.dao.IntegrityDefinitionDao.IntegrityType;
import edu.cmu.sv.arinc838.dao.SoftwareDefinitionFileDao;
import edu.cmu.sv.arinc838.dao.SoftwareDescriptionDao;
import edu.cmu.sv.arinc838.dao.TargetHardwareDefinitionDao;
import edu.cmu.sv.arinc838.util.Converter;
import edu.cmu.sv.arinc838.validation.ReferenceData;
import edu.cmu.sv.arinc838.writer.BdfWriter;

public class SoftwareDefinitionFileBuilderTest {
	private SdfFile swDefFile;
	private SoftwareDefinitionFileBuilder swDefFileBuilder;
	private com.arinc.arinc838.IntegrityDefinition integrity;
	private SoftwareDescription description;
	private SoftwareDescriptionBuilder swDescBuilder;
	private com.arinc.arinc838.FileDefinition fileDef;
	private com.arinc.arinc838.ThwDefinition hardwareDef;
	private BdfFile binaryFile;
	private SoftwareDefinitionFileDao readBinaryFile;
	private BuilderFactory bFactory;
	private TargetHardwareDefinitionBuilder thdBuilder;
	private FileDefinitionBuilder fdBuilder; 
	private IntegrityDefinitionBuilder integDefBuilder;

	@BeforeMethod
	public void beforeMethod() throws Exception {

		bFactory = mock(BuilderFactory.class);
		swDescBuilder = mock(SoftwareDescriptionBuilder.class);
		
		when(bFactory.getBuilder(SoftwareDescriptionDao.class, SoftwareDescription.class)).thenReturn(swDescBuilder); 
		
		swDefFileBuilder = new SoftwareDefinitionFileBuilder(bFactory);
		
		thdBuilder = mock(TargetHardwareDefinitionBuilder.class);
		when(bFactory.getBuilder(TargetHardwareDefinitionDao.class, ThwDefinition.class)).thenReturn(thdBuilder);
		
		fdBuilder = mock(FileDefinitionBuilder.class);
		when(bFactory.getBuilder(FileDefinitionDao.class, FileDefinition.class)).thenReturn(fdBuilder);
		
		integDefBuilder = mock(IntegrityDefinitionBuilder.class);
		when(bFactory.getBuilder(IntegrityDefinitionDao.class, IntegrityDefinition.class)).thenReturn(integDefBuilder);
		
		integrity = new com.arinc.arinc838.IntegrityDefinition();
		integrity.setIntegrityType(IntegrityType.CRC16.getType());
		integrity.setIntegrityValue(Converter.hexToBytes("0000000A"));

		description = new SoftwareDescription();
		description
				.setSoftwarePartnumber(ReferenceData.SOFTWARE_PART_NUMBER_REFERENCE);
		description.setSoftwareTypeDescription("desc");
		description.setSoftwareTypeId(Converter.hexToBytes("0000000A"));

		fileDef = new com.arinc.arinc838.FileDefinition();
		fileDef.setFileName("file");
		fileDef.setFileIntegrityDefinition(integrity);
		fileDef.setFileSize(1234);
		List<FileDefinition> fileDefs = new ArrayList<FileDefinition>();
		fileDefs.add(fileDef);

		hardwareDef = new ThwDefinition();
		hardwareDef.setThwId("hardware");

		swDefFile = new SdfFile();

		swDefFile
				.setFileFormatVersion(SoftwareDefinitionFileDao.DEFAULT_FILE_FORMAT_VERSION);
		swDefFile.setSdfIntegrityDefinition(integrity);
		swDefFile.setLspIntegrityDefinition(integrity);
		swDefFile.setSoftwareDescription(description);

		swDefFile.getFileDefinitions().add(fileDef);
		swDefFile.getFileDefinitions().add(fileDef);

		swDefFile.getThwDefinitions().add(hardwareDef);
		swDefFile.getThwDefinitions().add(hardwareDef);

		

		binaryFile = new BdfFile(File.createTempFile("tmp", "bin"));
		readBinaryFile = new SoftwareDefinitionFileDao(binaryFile);
		swDefFileBuilder.buildBinary(readBinaryFile, binaryFile);
	}


	@Test
	public void testBuildAddsFileFormatVersion() {
		SdfFile file = swDefFileBuilder.buildXml(readBinaryFile);

		assertEquals(swDefFile.getFileFormatVersion(),
				file.getFileFormatVersion());
	}

	@Test
	public void testDefaultConstructor() {
		SoftwareDefinitionFileDao builder = new SoftwareDefinitionFileDao();

		assertEquals(builder.getFileFormatVersion(),
				SoftwareDefinitionFileDao.DEFAULT_FILE_FORMAT_VERSION);

	}


	

	@Test
	public void testBuildBinaryWritesHeader() throws FileNotFoundException,
			IOException {
		BdfFile file = new BdfFile(File.createTempFile("tmp", "bin"));
		int bytesWritten = swDefFileBuilder.buildBinary(readBinaryFile,file);

		assertEquals(bytesWritten, 169);

		file.seek(0);
		assertEquals(file.readUint32(), file.length());
		byte[] fileFormatVersion = new byte[4];
		file.read(fileFormatVersion);
		assertEquals(fileFormatVersion,
				SoftwareDefinitionFileDao.DEFAULT_FILE_FORMAT_VERSION);

		assertEquals(file.readUint32(), 28); // software description pointer
		assertEquals(file.readUint32(), 28 + 27); // target hardware definition
													// pointer
		assertEquals(file.readUint32(), 28 + 27 + 40);// file definition pointer
		assertEquals(file.readUint32(), 28 + 27 + 40 + 54);// SDF integrity
															// definition
															// pointer
		assertEquals(file.readUint32(), 28 + 27 + 40 + 54 + 10);// LSP integrity
																// definition
																// pointer
	}

	@Test
	public void testBuildBinaryWritesSoftwareDefinition() throws IOException {
		BdfFile file = mock(BdfFile.class);
		SoftwareDescriptionDao swDescription = mock(SoftwareDescriptionDao.class);
		TargetHardwareDefinitionDao thdDao = mock(TargetHardwareDefinitionDao.class);
		TargetHardwareDefinitionDao thdDAOLast = mock(TargetHardwareDefinitionDao.class);
		FileDefinitionDao fdDao = mock(FileDefinitionDao.class);
		FileDefinitionDao fdDaoLast = mock(FileDefinitionDao.class);
		IntegrityDefinitionDao sdfIntegDao = mock(IntegrityDefinitionDao.class);
		IntegrityDefinitionDao lspIntegDao = mock(IntegrityDefinitionDao.class);
		

		readBinaryFile.setSoftwareDescription(swDescription);
		readBinaryFile.getTargetHardwareDefinitions().clear();
		readBinaryFile.getTargetHardwareDefinitions().add(thdDao);
		readBinaryFile.getTargetHardwareDefinitions().add(thdDAOLast);
		readBinaryFile.getFileDefinitions().clear();
		readBinaryFile.getFileDefinitions().add(fdDao);
		readBinaryFile.getFileDefinitions().add(fdDao);
		readBinaryFile.getFileDefinitions().add(fdDaoLast);

		readBinaryFile.setSdfIntegrityDefinition(sdfIntegDao);
		readBinaryFile.setLspIntegrityDefinition(lspIntegDao);

		InOrder order = inOrder(file, swDescription, thdDao,
				thdDAOLast, fdDao, fdDaoLast, sdfIntegDao, lspIntegDao);

		when(file.length()).thenReturn(14L);
		int bytesWritten = swDefFileBuilder.buildBinary(readBinaryFile,file);
		assertEquals(bytesWritten, 14L);

		order.verify(file).seek(0);
		order.verify(file).writePlaceholder();
		order.verify(file).writeHexbin32(
				readBinaryFile.getFileFormatVersion());
		order.verify(file, times(5)).writePlaceholder();

		order.verify(swDescBuilder).buildBinary(swDescription, file);

		order.verify(file).writeTargetDefinitionsPointer();
		order.verify(file).writeUint32(2);
		order.verify(thdDAOLast).setIsLast(true);
		order.verify(thdBuilder).buildBinary(thdDao,file);
		order.verify(thdBuilder).buildBinary(thdDAOLast, file);

		order.verify(file).writeFileDefinitionsPointer();
		order.verify(file).writeUint32(3);
		order.verify(fdDaoLast).setIsLast(true);
		order.verify(fdBuilder, times(2)).buildBinary(fdDao,file);
		order.verify(fdBuilder).buildBinary(fdDaoLast,file);

		order.verify(file).writeSdfIntegrityDefinitionPointer();
		// TODO actually calculate the CRC
		order.verify(sdfIntegDao).setIntegrityValue(
				Converter.hexToBytes("0000000A"));
		order.verify(integDefBuilder).buildBinary(sdfIntegDao,file);

		order.verify(file).writeLspIntegrityDefinitionPointer();
		// TODO actually calculate the CRC
		order.verify(lspIntegDao).setIntegrityValue(
				Converter.hexToBytes("0000000A"));

		order.verify(integDefBuilder).buildBinary(lspIntegDao,file);
		order.verify(file).seek(0);
		order.verify(file).writeUint32(file.length());
	}


	@Test
	public void testReadBinary() throws Exception {
		SoftwareDefinitionFileDao actual = new SoftwareDefinitionFileDao(binaryFile);		
		assertEquals(actual,  readBinaryFile);
	}
	
	@Test
	public void testReadBinaryActualFilesOnDisk() throws Exception {
		BdfWriter writer = new BdfWriter();
		
		
		String path = System.getProperty("java.io.tmpdir");
		writer.write(path, readBinaryFile);

		String firstFileName = path + readBinaryFile.getBinaryFileName();
		File firstOnDisk = new File(firstFileName);

		BdfFile file = new BdfFile(firstOnDisk);

		SoftwareDefinitionFileDao actual = new SoftwareDefinitionFileDao(
				file);
		String nextPath = path + "/newBinary/";
		writer.write(nextPath, actual);

		String secondFileName = nextPath + actual.getBinaryFileName();

		RandomAccessFile first = new RandomAccessFile(firstOnDisk, "r");
		byte[] firstBytes = new byte[(int) first.length()];
		first.readFully(firstBytes);

		File secondOnDisk = new File(secondFileName);
		RandomAccessFile second = new RandomAccessFile(secondOnDisk, "r");
		byte[] secondBytes = new byte[(int) second.length()];
		second.readFully(secondBytes);

		assertEquals(firstBytes, secondBytes);

		firstOnDisk.delete();
		secondOnDisk.delete();
	}

	

}
