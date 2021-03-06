package edu.cmu.sv.arinc838.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.sv.arinc838.dao.FileDefinitionDao;
import edu.cmu.sv.arinc838.dao.IntegrityDefinitionDao;
import edu.cmu.sv.arinc838.dao.SoftwareDefinitionFileDao;
import edu.cmu.sv.arinc838.dao.SoftwareDescriptionDao;
import edu.cmu.sv.arinc838.dao.TargetHardwareDefinitionDao;

/**
 * Throughout the comparisons a case of this.<obj> == null && other.<obj> ==
 * null is not considered an error. Only if they are different.
 * 
 * Order in any lists is maintained. [a,b,c] != [c,a,b]
 * 
 * @author ryan
 */
public class SdfChecker {

	/**
	 * Interface that acts similarly to a function pointer. Allows lists of
	 * objects to be handled generically but still call back into the sdf
	 * checker code.
	 * 
	 * @param <D>
	 */
	protected interface CompareRef<D> {
		public Collection<String> passThru(D mine, D theirs);
	}

	public List<String> compare(SoftwareDefinitionFileDao mine, SoftwareDefinitionFileDao theirs) {
		List<String> results = new ArrayList<String>();
		Boolean nc = nullCheck (mine, theirs);
		if (nc == null) {
			return results;
		} else if (nc == true) {
			results.add("Can't compare software definitions, null detected. " + mine + ", " + theirs);
			return results;
		}

		if (!compareBytes (mine.getFileFormatVersion(), theirs.getFileFormatVersion())) {
			results.add("The file format versions differ");
		}

		results.addAll(check(mine.getFileDefinitions(), theirs.getFileDefinitions(), "file definition",
				new CompareRef<FileDefinitionDao>() {
					@Override
					public Collection<String> passThru(FileDefinitionDao mine, FileDefinitionDao theirs) {
						return compare(mine, theirs);
					}
				}));

		results.addAll(check(mine.getTargetHardwareDefinitions(), theirs.getTargetHardwareDefinitions(),
				"target hardware definition", new CompareRef<TargetHardwareDefinitionDao>() {
					@Override
					public Collection<String> passThru(TargetHardwareDefinitionDao mine,
							TargetHardwareDefinitionDao theirs) {
						return compare(mine, theirs);
					}
				}));

		results.addAll(compare(mine.getLspIntegrityDefinition(), theirs.getLspIntegrityDefinition()));
		results.addAll(compare(mine.getSdfIntegrityDefinition(), theirs.getSdfIntegrityDefinition()));
		results.addAll(compare(mine.getSoftwareDescription(), theirs.getSoftwareDescription()));

		return results;
	}

	protected List<String> compare(SoftwareDescriptionDao mine, SoftwareDescriptionDao theirs) {
		List<String> results = new ArrayList<String>();

		Boolean nc = nullCheck (mine, theirs);
		if (nc == null) {
			return results;
		} else if (nc == true) {
			results.add("Can't compare software descriptions, null detected. " + mine + ", " + theirs);
			return results;
		}
		
		check(mine.getSoftwarePartnumber(), theirs.getSoftwarePartnumber(), "software part number", results);
		check(mine.getSoftwareTypeDescription(), theirs.getSoftwareTypeDescription(), "software type description", results);
		if (!compareBytes(mine.getSoftwareTypeId(), theirs.getSoftwareTypeId())) {
			results.add("The software type ids differ");
		}
		
		return results;
	}

	protected List<String> compare(IntegrityDefinitionDao mine, IntegrityDefinitionDao theirs) {
		List<String> results = new ArrayList<String>();

		Boolean nc = nullCheck (mine, theirs);
		if (nc == null) {
			return results;
		} else if (nc == true) {
			results.add("Can't compare integrity definitions, null detected. " + mine + ", " + theirs);
			return results;
		}
		
		check (mine.getIntegrityType(), theirs.getIntegrityType(), "integrity type", results);
		if (!compareBytes(mine.getIntegrityValue(), theirs.getIntegrityValue())) {
			results.add("The integrity values differ");
		}
		
		return results;
	}

	protected List<String> compare(FileDefinitionDao mine, FileDefinitionDao theirs) {
		List<String> results = new ArrayList<String>();

		Boolean nc = nullCheck (mine, theirs);
		if (nc == null) {
			return results;
		} else if (nc == true) {
			results.add("Can't compare file definitions, null detected. " + mine + ", " + theirs);
			return results;
		}
		
		check (mine.getFileName(), theirs.getFileName(), "filename", results);
		check (mine.getFileSize(), theirs.getFileSize(), "file size", results);
		check (mine.isFileLoadable(), theirs.isFileLoadable(), "loadable flag", results);
		
		results.addAll(compare (mine.getFileIntegrityDefinition(), theirs.getFileIntegrityDefinition()));
		
		return results;
	}

	protected List<String> compare(TargetHardwareDefinitionDao mine, TargetHardwareDefinitionDao theirs) {
		List<String> results = new ArrayList<String>();
		
		Boolean nc = nullCheck (mine, theirs);
		if (nc == null) {
			return results;
		} else if (nc == true) {
			results.add("Can't compare target hardware definitions, null detected. " + mine + ", " + theirs);
			return results;
		}
		
		check (mine.getThwId(), theirs.getThwId(), "target hardware id", results);

		results.addAll(check(mine.getPositions(), theirs.getPositions(),
				"positions", new CompareRef<String>() {
					@Override
					public Collection<String> passThru(String mine,
							String theirs) {
						List<String> results = new ArrayList<String> ();
						check (mine, theirs, "positions", results);
						return results;
					}
				}));
		
		return results;
	}

	/**
	 * True iff both are null, or if either one is.
	 * A message is logged if one is null and the other isn't
	 * @param mine
	 * @param theirs
	 * @param results
	 * @return
	 */
	protected <T> Boolean nullCheck (T mine, T theirs) {
		if ((mine == null && theirs != null) || (mine != null && theirs== null)) {
			return true;
		} else if (mine == null && theirs == null) {
			return null;
		}		
		
		return false;
	}
	
	/**
	 * Compares two objects - 
	 * 	If  they are the same (defined above) then nothing is done. TRUE is returned
	 * 	Otherwise (e.g. null & !null, not equal) then FALSE is returned;
	 * @param mine
	 * @param theirs
	 * @param type
	 * @param results
	 * @return
	 */
	protected <T> boolean check (T mine, T theirs, String type, List<String> results) {
		if ((mine != null && theirs != null && !mine.equals(theirs)) || 
			(mine == null && theirs != null) ||
			(mine != null && theirs == null)) {
				results.add("The " + type + "s differ: " + mine + "," + theirs);
				return false;
		}
		return true;
	}
	
	protected boolean compareBytes(byte[] mine, byte[] theirs) {
		if (nullCheck (mine, theirs)) {
			return false;
		}
		if (mine.length != theirs.length) {
			return false;
		}
		
		for (int i = 0; i < mine.length; i++) {
			if (mine[i] != theirs[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	protected <L extends List<I>, I> List<String> check(L mine, L theirs, String type, CompareRef<I> dc) {
		List<String> ret = new ArrayList<String>();

		if (mine != null && theirs != null) {
			// both exist - compare
			if (mine.size() != theirs.size()) {
				ret.add("The number of " + type + " differ: " + mine.size() + ", " + theirs.size());
			} else {
				// they could match
				for (int i = 0; i < mine.size(); ++i) {
					ret.addAll(dc.passThru(mine.get(i), theirs.get(i)));
				}
			}
		}
		if ((mine != null && theirs == null) || mine == null && theirs != null) {
			// one of us exists - problem
			ret.add("The list of " + type + " differ");
		} else {
			// we're both null - ok
		}

		return ret;
	}
}