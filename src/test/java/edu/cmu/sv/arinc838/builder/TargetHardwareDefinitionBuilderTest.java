package edu.cmu.sv.arinc838.builder;

import static org.testng.Assert.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.arinc.arinc838.ThwDefinition;

import edu.cmu.sv.arinc838.builder.TargetHardwareDefinitionBuilder;

public class TargetHardwareDefinitionBuilderTest {
	private TargetHardwareDefinitionBuilder first;
	private TargetHardwareDefinitionBuilder second;

	@BeforeMethod
	public void setup() {
		first = new TargetHardwareDefinitionBuilder();
		first.setId("first");
		first.getPositions().add("one");
		first.getPositions().add("two");
		
		second = new TargetHardwareDefinitionBuilder();
		second.setId("first");
		second.getPositions().add("one");
		second.getPositions().add("two");		
	}

	@Test
	public void getId() {
		com.arinc.arinc838.ThwDefinition jaxbDef = new com.arinc.arinc838.ThwDefinition();
		jaxbDef.setThwId("test");
		TargetHardwareDefinitionBuilder xmlDef = new TargetHardwareDefinitionBuilder(
				jaxbDef);

		assertEquals(xmlDef.getId(), jaxbDef.getThwId());
	}

	@Test
	public void setId() {
		com.arinc.arinc838.ThwDefinition jaxbDef = new com.arinc.arinc838.ThwDefinition();
		jaxbDef.setThwId("test");
		TargetHardwareDefinitionBuilder xmlDef = new TargetHardwareDefinitionBuilder(
				jaxbDef);

		String value = "new test";

		xmlDef.setId(value);

		assertEquals(xmlDef.getId(), value);
	}

	@Test
	public void getPositions() {
		com.arinc.arinc838.ThwDefinition jaxbDef = new ThwDefinition();
		jaxbDef.getThwPosition().add("one");
		jaxbDef.getThwPosition().add("two");
		TargetHardwareDefinitionBuilder xmlDef = new TargetHardwareDefinitionBuilder(
				jaxbDef);

		assertEquals(xmlDef.getPositions().size(), 2);
		assertEquals(xmlDef.getPositions().get(0), "one");
		assertEquals(xmlDef.getPositions().get(1), "two");
	}

	@Test
	public void getPositionsWithNoPositions() {
		com.arinc.arinc838.ThwDefinition jaxbDef = new ThwDefinition();
		TargetHardwareDefinitionBuilder xmlDef = new TargetHardwareDefinitionBuilder(
				jaxbDef);

		assertEquals(xmlDef.getPositions().size(), 0);
	}

	@Test
	public void equalsUsesIdAndPositions() {
		assertEquals(first, second);
	}

	@Test
	public void equalsFailsIfIdIsDifferent() {
		second.setId("second");
		
		assertNotEquals(first, second);
	}

	@Test
	public void equalsFailsIfPositionsAreNotSame() {
		second.getPositions().clear();
		second.getPositions().add("two");
		second.getPositions().add("one");

		assertNotEquals(first, second);
	}

	@Test
	public void equalsFailsForDifferentTypes() {
		assertNotEquals(first, new Object());
	}

	@Test
	public void equalsWorksForSameObject() {
		assertEquals(first, first);
	}

	@Test
	public void hashcodeIsCombinationOfIdAndPositions() {
		assertEquals(first.hashCode(), first.getId().hashCode()
				^ first.getPositions().hashCode());
	}

	@Test
	public void testBuildReturnsProperJaxbObject() {
		ThwDefinition def = first.build();
		
		assertEquals(def.getThwId(), first.getId());
		
		for(int i = 0; i < def.getThwPosition().size(); i++){
			assertEquals(def.getThwPosition().get(i), first.getPositions().get(i));
		}
	}
}
