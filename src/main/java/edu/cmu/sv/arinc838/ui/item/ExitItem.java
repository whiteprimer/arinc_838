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

import edu.cmu.sv.arinc838.dao.SoftwareDefinitionFileDao;

public class ExitItem extends AbstractMenuItem {

	public ExitItem() {
		super("Exit");
	}

	@Override
	public MenuItem[] execute(SoftwareDefinitionFileDao builder) throws Exception {
		return null;
	}
}
