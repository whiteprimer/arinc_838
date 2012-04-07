package edu.cmu.sv.arinc838.ui.item;

import java.util.ArrayList;

import edu.cmu.sv.arinc838.dao.SoftwareDefinitionFileDao;
import edu.cmu.sv.arinc838.reader.SdfReader;
import edu.cmu.sv.arinc838.ui.ErrorPrinter;

public class SdfReadItem extends AbstractMenuItem {

	private SdfReader reader;

	public SdfReadItem(String prompt, SdfReader reader) {
		super(prompt);
		this.reader = reader;
	}

	@Override
	public MenuItem[] execute(SoftwareDefinitionFileDao sdfDao) throws Exception {
		String filename = promptForResponse("Which file?");
	
		ArrayList<Exception> errorList = new ArrayList<Exception>();
		SoftwareDefinitionFileDao read = reader.read(filename, errorList);
	
		if (errorList.isEmpty()) {
			sdfDao.initialize(read);
			System.out.println("Successfully read in " + filename);
		} else {
			ErrorPrinter.printErrors(filename, errorList);
		}
	
		return this.getEmptyItems();
	}

}