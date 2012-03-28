package edu.cmu.sv.arinc838.builder;

import java.io.IOException;
import java.util.List;

import com.arinc.arinc838.SdfFile;

import edu.cmu.sv.arinc838.binary.BdfFile;
import edu.cmu.sv.arinc838.dao.FileDefinitionDao;
import edu.cmu.sv.arinc838.dao.SoftwareDefinitionFileDao;
import edu.cmu.sv.arinc838.dao.TargetHardwareDefinitionDao;
import edu.cmu.sv.arinc838.util.Converter;

public class SoftwareDefinitionFileBuilder implements Builder<SoftwareDefinitionFileDao, SdfFile> {
	@Override
	public SdfFile buildXml(SoftwareDefinitionFileDao softwareDefinitionFileDao) {
		SdfFile file = new SdfFile();
		file.setFileFormatVersion(softwareDefinitionFileDao.getFileFormatVersion());

		// we have to re-validate this as a LIST1 since it can be modified
		// without a set method to verify its validity prior to building
		List<FileDefinitionDao> fileDefsValidated = softwareDefinitionFileDao.getFileDefinitions();
		FileDefinitionBuilder fileDefBuilder = new FileDefinitionBuilder();
		for (FileDefinitionDao fileDef : fileDefsValidated) {
			file.getFileDefinitions().add(fileDefBuilder.buildXml(fileDef));
		}

		TargetHardwareDefinitionBuilder thwDefBuilder = new TargetHardwareDefinitionBuilder();
		for (TargetHardwareDefinitionDao thwDef : softwareDefinitionFileDao.getTargetHardwareDefinitions()) {
			file.getThwDefinitions().add(thwDefBuilder.buildXml(thwDef));
		}

		IntegrityDefinitionBuilder integDefBuilder = new IntegrityDefinitionBuilder(); 
		file.setLspIntegrityDefinition(integDefBuilder.buildXml(softwareDefinitionFileDao.getLspIntegrityDefinition()));
		file.setSdfIntegrityDefinition(integDefBuilder.buildXml(softwareDefinitionFileDao.getSdfIntegrityDefinition()));
		
		file.setSoftwareDescription(new SoftwareDescriptionBuilder().buildXml(softwareDefinitionFileDao.getSoftwareDescription()));

		return file;
	}

	@Override
	public int buildBinary(SoftwareDefinitionFileDao softwareDefinitionFileDao, BdfFile file) throws IOException {
		file.seek(0);
		// write the header
		file.writePlaceholder(); // file size
		file.writeHexbin32(softwareDefinitionFileDao.getFileFormatVersion());
		file.writePlaceholder(); // software description pointer
		file.writePlaceholder(); // target hardware definition pointer
		file.writePlaceholder(); // file definition pointer
		file.writePlaceholder(); // SDF integrity definition pointer
		file.writePlaceholder(); // LSP integrity definition pointer
		new SoftwareDescriptionBuilder().buildBinary(softwareDefinitionFileDao.getSoftwareDescription(), file);

		// Write the target hardware definitions
		int size = softwareDefinitionFileDao.getTargetHardwareDefinitions().size();
		file.writeTargetDefinitionsPointer();
		file.writeUint32(size);
		TargetHardwareDefinitionBuilder targetHardwareDefinitionBuilder = new TargetHardwareDefinitionBuilder();
		if (size > 0) {
			softwareDefinitionFileDao.getTargetHardwareDefinitions().get(size - 1).setIsLast(true);
			for (int i = 0; i < size; i++) {
				targetHardwareDefinitionBuilder.buildBinary(softwareDefinitionFileDao.getTargetHardwareDefinitions()
						.get(i), file);
			}
		}

		// write the file definitions
		size = softwareDefinitionFileDao.getFileDefinitions().size();
		file.writeFileDefinitionsPointer();
		file.writeUint32(size);
		softwareDefinitionFileDao.getFileDefinitions().get(size - 1).setIsLast(true);
		FileDefinitionBuilder fileDefBuilder = new FileDefinitionBuilder();
		for (int i = 0; i < size; i++) {
			fileDefBuilder.buildBinary(softwareDefinitionFileDao.getFileDefinitions().get(i), file);
		}

		IntegrityDefinitionBuilder integDefBuilder = new IntegrityDefinitionBuilder();

		// write the SDF integrity def
		file.writeSdfIntegrityDefinitionPointer();
		softwareDefinitionFileDao.getSdfIntegrityDefinition().setIntegrityValue(Converter.hexToBytes("0000000A"));
		integDefBuilder.buildBinary(softwareDefinitionFileDao.getSdfIntegrityDefinition(), file);

		// write the LSP integrity def
		file.writeLspIntegrityDefinitionPointer();
		softwareDefinitionFileDao.getLspIntegrityDefinition().setIntegrityValue(Converter.hexToBytes("0000000A"));
		integDefBuilder.buildBinary(softwareDefinitionFileDao.getLspIntegrityDefinition(), file);

		// write the file size
		file.seek(0);
		file.writeUint32(file.length());
		file.seek(file.length());

		return (int) file.length();
	}

}
