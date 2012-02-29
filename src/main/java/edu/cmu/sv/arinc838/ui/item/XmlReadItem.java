/*
 * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
 * Brandon Sutherlin, Scott Griffin
 * 
 * This software is released under the MIT license
 * (http://www.opensource.org/licenses/mit-license.php)
 * 
 * Created on Feb 18, 2012
 */
package edu.cmu.sv.arinc838.ui.item;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.arinc.arinc838.SdfFile;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.arinc.arinc838.SdfFile;

import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.arinc.arinc838.SdfFile;

import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;


public class XmlReadItem  extends AbstractMenuItem {	
	public XmlReadItem(String prompt) {
		super(prompt);
	}
	
	@Override	
	public MenuItem[] execute(SoftwareDefinitionFileBuilder builder) throws Exception {		
		String filename = promptForResponse("Which file?");
				
		File file = new File(filename);
				
		JAXBContext jaxbContext = JAXBContext.newInstance(SdfFile.class);		
		Unmarshaller jaxbMarshaller = jaxbContext.createUnmarshaller();
		
		SdfFile jaxbFile = (SdfFile) jaxbMarshaller.unmarshal(file);
				
		builder.initialize(jaxbFile);
		
		System.out.println ("Successfully read in " + file);
		
		return super.getEmptyItems();
	}
}e) jaxbMarshaller.unmarshal(file);
				
		builder.initialize(jaxbFile);
		
<<<<<<< HEAD
		return new XmlFileMenu(builder.getSoftwareDescription().getSoftwarePartNumber());
=======
		System.out.println ("Successfully read in " + file);
		
		return super.getEmptyItems();
>>>>>>> feature-23773803_add_rw_bdf_to_ui
	}
}
/*
 * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
 * Brandon Sutherlin, Scott Griffin
 * 
 * This software is released under the MIT license
 * (http://www.opensource.org/licenses/mit-license.php)
 * 
 * Created on Feb 18, 2012
 */
package edu.cmu.sv.arinc838.ui.item;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.arinc.arinc838.SdfFile;

import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;

public class XmlReadItem  extends AbstractMenuItem {	
	public XmlReadItem(String prompt) {
		super(prompt);
	}
	
	@Override
	public MenuItem[] execute(SoftwareDefinitionFileBuilder builder) throws Exception {		
		String filename = promptForResponse("Which file?");
				
		File file = new File(filename);
				
		JAXBContext jaxbContext = JAXBContext.newInstance(SdfFile.class);		
		Unmarshaller jaxbMarshaller = jaxbContext.createUnmarshaller();
		
		SdfFile jaxbFile = (SdfFile) jaxbMarshaller.unmarshal(file);
				
		builder.initialize(jaxbFile);
		
		System.out.println ("Successfully read in " + file);
		
		return super.getEmptyItems();
	}
}
