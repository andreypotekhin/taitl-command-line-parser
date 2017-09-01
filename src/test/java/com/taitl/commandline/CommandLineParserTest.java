package com.taitl.commandline;

import java.util.*;

import junit.framework.*;

import org.junit.*;
import org.junit.Test;

/**
 * Tests for CommandLineParser class.
 * 
 * @author Andrey Potekhin
 * 
 */
public class CommandLineParserTest extends TestCase
{

	CommandLineParser commandLineParser = null;
	CommandLineParser newCommandLineParser = null;

	// Error strings
	static final String MISSING_EXCEPTION = "Exception not thrown where it should";
	static final String ERRONEOUS_EXCEPTION = "Exception thrown where it shouldn't";

	// Trivial command lines
	static final String cmdlineNULL = null;
	static final String cmdlineEmpty = "";
	static final String cmdlineUsage = "--prev --usage --next";
	static final String cmdlineVersion = "--prev --version --next";
	static final String cmdlineHelp = "--prev --help --next";
	static final String cmdlineHelpWithArgument = "--prev --help general --next";

	// Values of switches
	static final String cmdlineNoValue = "--novalue --next";
	static final String cmdlineOneValue = "--onevalue val --next";
	static final String cmdlineOneValueOnEnd = "--prev --onevalue val";
	static final String cmdlineTwoValues = "--twovalues val1 val2 --next";
	static final String cmdlineTwoValuesOnEnd = "--prev --twovalues val1 val2";

	static final String cmdlineThreeValues = "--threevalues val1 val2 val3 --next";
	static final String cmdlineThreeValuesOnEnd = "--prev --threevalues val1 val2 val3";
	static final String cmdlineOneToThree = "--prev --onetothree val1 val2 --next";
	static final String cmdlineOneToThreeTooFew = "--prev --onetothree --next";
	static final String cmdlineOneToThreeTooMany = "--prev --onetothree val1 val2 val3 val4 --next";

	// Multiple values of a switch
	static final String cmdlineMultiValues = "--prev --multi val1 val2 val3 val4 val5 --next";
	static final String cmdlineMultiValuesOnEnd = "--prev --multi val1 val2 val3 val4 val5";
	static final String cmdlineMany = "--prev --many val1 val2 \"val3\" val4 val5 val6 --next";
	static final String cmdlineManyTooFew = "--prev --many val1 \"val2\" --next";

	// Default switch
	static final String cmdlineImplicitSwitch = "--prev file.txt --next";
	static final String cmdlineImplicitSwitchTooManyValues = "--prev file.txt file2.txt --next";

	// Quoted arguments
	static final String cmdlineQuote = "--prev --onevalue \"val1 --next val2 val3\" --next";

	// Switchless arguments
	static final String cmdlineSwitchlessArguments1 =
			"--prev file1.txt --next file2.txt file3.txt file4.txt";
	static final String cmdlineSwitchlessArguments2 =
			"file1.txt --prev file2.txt file3.txt --next file4.txt";

	static final String possibleSwitches =
			"--usage(0) --version(0) --help(0-1) --prev(0) --next(0)"
					+ " --novalue(0) --onevalue(1) --twovalues(2) --threevalues(3) --onetothree(1-3)"
					+ " --multi(*) --many(3-*)";
	static final String possibleThreeSwitches = "--prev(0) --next(0) --onevalue(1)";
	static final String incorrectPossibleSwitches1 = "--usage";
	static final String incorrectPossibleSwitches2 = "--usage(?)";
	static final String incorrectPossibleSwitches3 = "--usage(3-2)";
	static final String incorrectPossibleSwitches4 = "--usage[0]";
	static final String implicitSwitch = "--file(1)";
	static final String implicitSwitchAlternative = "--file";

	/** Sets up test class object. */
	@Override
	@Before
	public void setUp() throws Exception
	{
		commandLineParser = new CommandLineParser();
		commandLineParser.setPossibleSwitches(possibleSwitches);
		commandLineParser.setImplicitSwitch(implicitSwitch);
		newCommandLineParser = new CommandLineParser(); // Not to be initialized
	}

	/**
	 * Performs cleanup of objects created in setUp() method.
	 * 
	 * @throws Exception
	 *             An exception to indicate problems during tear-down.
	 */
	@Override
	@After
	public void tearDown() throws Exception
	{
		commandLineParser = null;
	}

	/*
	 * Trivial tests
	 */

	/** */
	@Test
	public final void testCommandLineParser()
	{
		assertFalse(commandLineParser.isInitialized());
	}

	/** */
	@Test
	public final void testSetPossibleSwitches()
	{
		commandLineParser.setPossibleSwitches(possibleThreeSwitches);
		final int requiredNumberOfSwitches = 3;
		assertEquals(requiredNumberOfSwitches, commandLineParser.getPossibleSwitches().size());
	}

	/** */
	@Test
	public final void testSetCommandLine()
	{
		commandLineParser.setCommandLine(cmdlineUsage);
		assertEquals(cmdlineUsage, commandLineParser.getOriginalCommandLine());
	}

	/** */
	@Test
	public final void testSetArguments()
	{
		String commandLine;
		String[] arguments;
		arguments = new String[] { "--prev", "--usage", "--next" };
		commandLineParser.setArguments(arguments);
		assertEquals("--prev --usage --next", commandLineParser.getOriginalCommandLine());

		arguments = new String[] { "--prev", "--usage", "usagevalue1 usagevalue2", "--next" };
		commandLineParser.setArguments(arguments);
		commandLine = commandLineParser.getOriginalCommandLine();
		assertEquals("--prev --usage \"usagevalue1 usagevalue2\" --next", commandLine);
		arguments = commandLineParser.getArguments();
		assertTrue(arguments.length == 4);
	}

	/** */
	@Test
	public final void testParse()
	{
		// String[] arguments = new String[] { "--prev", "--usage", "--next" };

		// commandLineParser.setCommandLine(cmdlineUsage); // Calls parse()
		// method
		// assertTrue(commandLineParser.isInitialized());

		newCommandLineParser.setPossibleSwitches(possibleSwitches);

		// Try parsing without specifying command line arguments
		try
		{
			newCommandLineParser.parse();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException ise)
		{
		}

		try
		{
			newCommandLineParser.doParsing(null);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException ise)
		{
		}

		newCommandLineParser.setCommandLine(cmdlineUsage);
		assertTrue(newCommandLineParser.isInitialized());
	}

	/** */
	@Test
	public final void testGetArguments()
	{
		commandLineParser.setCommandLine(cmdlineQuote);
		String[] arguments = commandLineParser.getArguments();
		assertTrue(arguments.length == 4);
	}

	/** */
	@Test
	public final void testAddPossibleSwitch()
	{
		commandLineParser.setPossibleSwitches(possibleThreeSwitches);
		assertTrue(commandLineParser.isPossibleSwitch("--prev"));
		assertFalse(commandLineParser.isPossibleSwitch("--usage"));

		// Try adding a malformed switch
		try
		{
			commandLineParser.addPossibleSwitch("--usage");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		commandLineParser.addPossibleSwitch("--usage(0)");
		assertTrue(commandLineParser.isPossibleSwitch("--usage"));
	}

	/** */
	@Test
	public final void testRemovePossibleSwitch()
	{
		commandLineParser.setPossibleSwitches(possibleThreeSwitches);

		// Try removing a non-existing switch
		try
		{
			commandLineParser.removePossibleSwitch("--nonexisting");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Remove switch with fully-specified name (including cardinality)
		commandLineParser.removePossibleSwitch("--next(0)");
		int requiredNumberOfSwitches = 2;
		assertEquals(requiredNumberOfSwitches, commandLineParser.getPossibleSwitches().size());

		// Try removing a malformed switch
		try
		{
			commandLineParser.removePossibleSwitch("--prev(0");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Try removing a malformed switch
		try
		{
			commandLineParser.removePossibleSwitch("--prev0)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Remove switch with omitted cardinality
		commandLineParser.removePossibleSwitch("--prev");
		requiredNumberOfSwitches = 1;
		assertEquals(requiredNumberOfSwitches, commandLineParser.getPossibleSwitches().size());
	}

	/** */
	@Test
	public final void testIsSwitchPresent()
	{
		commandLineParser.setCommandLine(cmdlineUsage);
		assertTrue(commandLineParser.isSwitchPresent(commandLineParser.getUsageSwitchName()));
		assertFalse(commandLineParser.isSwitchPresent(commandLineParser.getHelpSwitchName()));
	}

	/** */
	@Test
	public final void testGetSwitchValue()
	{
		String switchValue = null;

		commandLineParser.setCommandLine(cmdlineNoValue);
		assertTrue(commandLineParser.isSwitchPresent("--novalue"));
		assertTrue(commandLineParser.isSwitchPresent("--next"));
		assertFalse(commandLineParser.isSwitchPresent("--prev"));

		try
		{
			commandLineParser.getSwitchValue("--prev");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		switchValue = commandLineParser.getSwitchValue("--novalue");
		assertEquals(switchValue, "");

		commandLineParser.setCommandLine(cmdlineOneValue);
		assertTrue(commandLineParser.isSwitchPresent("--onevalue"));
		switchValue = commandLineParser.getSwitchValue("--onevalue");
		assertEquals(switchValue, "val");

		commandLineParser.setCommandLine(cmdlineOneValueOnEnd);
		assertTrue(commandLineParser.isSwitchPresent("--onevalue"));
		switchValue = commandLineParser.getSwitchValue("--onevalue");
		assertEquals(switchValue, "val");

		commandLineParser.setCommandLine(cmdlineTwoValues);
		assertTrue(commandLineParser.isSwitchPresent("--twovalues"));
		try
		{
			// An exception must be thrown if switch has more than one value
			switchValue = commandLineParser.getSwitchValue("--twovalues");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
	}

	/** */
	@Test
	public final void testGetSwitchValues()
	{
		List<String> switchValues = null;

		commandLineParser.setCommandLine(cmdlineNoValue);
		assertTrue(commandLineParser.isSwitchPresent("--novalue"));
		assertTrue(commandLineParser.isSwitchPresent("--next"));
		assertFalse(commandLineParser.isSwitchPresent("--prev"));
		switchValues = commandLineParser.getSwitchValues("--novalue");
		assertEquals(switchValues.size(), 0);

		commandLineParser.setCommandLine(cmdlineOneValue);
		switchValues = commandLineParser.getSwitchValues("--onevalue");
		assertEquals(switchValues.size(), 1);
		commandLineParser.setCommandLine(cmdlineOneValueOnEnd);
		assertEquals(switchValues.size(), 1);

		commandLineParser.setCommandLine(cmdlineTwoValues);
		switchValues = commandLineParser.getSwitchValues("--twovalues");
		assertEquals(switchValues.size(), 2);
		commandLineParser.setCommandLine(cmdlineTwoValuesOnEnd);
		switchValues = commandLineParser.getSwitchValues("--twovalues");
		assertEquals(switchValues.size(), 2);

		commandLineParser.setCommandLine(cmdlineThreeValues);
		switchValues = commandLineParser.getSwitchValues("--threevalues");
		assertEquals(switchValues.size(), 3);
		commandLineParser.setCommandLine(cmdlineThreeValuesOnEnd);
		switchValues = commandLineParser.getSwitchValues("--threevalues");
		assertEquals(switchValues.size(), 3);

		commandLineParser.setCommandLine(cmdlineOneToThree);
		switchValues = commandLineParser.getSwitchValues("--onetothree");
		assertEquals(switchValues.size(), 2);

		commandLineParser.setCommandLine(cmdlineMultiValues);
		switchValues = commandLineParser.getSwitchValues("--multi");
		assertTrue(switchValues.size() >= 4);
		commandLineParser.setCommandLine(cmdlineMultiValuesOnEnd);
		switchValues = commandLineParser.getSwitchValues("--multi");
		assertTrue(switchValues.size() >= 4);

		commandLineParser.setCommandLine(cmdlineMany);
		switchValues = commandLineParser.getSwitchValues("--many");
		assertTrue(switchValues.size() >= 4);
	}

	/** */
	@Test
	public final void testCreateSwitch()
	{
		Switch s;

		s = commandLineParser.createSwitch("--prev(0)");
		assertTrue(s != null && s.getMinValues() == 0 && s.getMaxValues() == 0);
		// Now try creating the switch by only specifying its name - without
		// cardinality
		s = commandLineParser.createSwitch("--prev");
		assertTrue(s != null && s.getMinValues() == 0 && s.getMaxValues() == 0);

		// Attempt to redefine cardinality of existing switch ("--prev(0)")
		try
		{
			commandLineParser.addPossibleSwitch("--prev(1)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Try creating a switch with malformed specification
		try
		{
			commandLineParser.createSwitch("--prev(0");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		try
		{
			commandLineParser.createSwitch("--prev0)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Try creating a switch with incorrect cardinality (different from
		// what was specified in setPossibleSwitches()
		try
		{
			commandLineParser.createSwitch("--prev(1)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Try creating a switch with incorrect cardinality (non-number)
		try
		{
			commandLineParser.addPossibleSwitch("--myswitch1(x)");
			commandLineParser.createSwitch("--myswitch1(x)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		try
		{
			commandLineParser.addPossibleSwitch("--myswitch2(1-x)");
			commandLineParser.createSwitch("--myswitch2(1-x)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		try
		{
			commandLineParser.addPossibleSwitch("--myswitch3(x-100)");
			commandLineParser.createSwitch("--myswitch3(x-100)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Try creating a switch which does not exist among possible switches
		try
		{
			commandLineParser.createSwitch("--nonexisting(0)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

	}

	/*******************************
	 * Trivial tests
	 *******************************/

	/** */
	@Test
	public final void testGetSwitchValueCount()
	{
		commandLineParser.setCommandLine(cmdlineThreeValues);
		assertEquals(commandLineParser.getSwitchValueCount("--threevalues"), 3);
	}

	/** */
	@Test
	public final void testGetOriginalCommandLine()
	{
		commandLineParser.setCommandLine(cmdlineUsage);
		assertEquals(cmdlineUsage, commandLineParser.getOriginalCommandLine());
	}

	/** */
	@Test
	public final void testIsUsageRequested()
	{
		commandLineParser.setCommandLine(cmdlineHelp);
		assertFalse(commandLineParser.isUsageRequested());
		commandLineParser.setCommandLine(cmdlineUsage);
		assertTrue(commandLineParser.isUsageRequested());
	}

	/** */
	@Test
	public final void testIsHelpRequested()
	{
		commandLineParser.setCommandLine(cmdlineUsage);
		assertFalse(commandLineParser.isHelpRequested());
		commandLineParser.setCommandLine(cmdlineHelp);
		assertTrue(commandLineParser.isHelpRequested());
	}

	/** */
	@Test
	public final void testIsVersionRequested()
	{
		commandLineParser.setCommandLine(cmdlineEmpty);
		assertFalse(commandLineParser.isVersionRequested());
		commandLineParser.setCommandLine(cmdlineVersion);
		assertTrue(commandLineParser.isVersionRequested());
	}

	/** */
	@Test
	public final void testGetUsageSwitch()
	{
		commandLineParser.setUsageSwitchName("a");
		assertEquals("a", commandLineParser.getUsageSwitchName());
	}

	/** */
	@Test
	public final void testSetUsageSwitch()
	{
		commandLineParser.setUsageSwitchName("a");
		assertEquals("a", commandLineParser.getUsageSwitchName());
	}

	/** */
	@Test
	public final void testGetHelpSwitch()
	{
		commandLineParser.setHelpSwitchName("a");
		assertEquals("a", commandLineParser.getHelpSwitchName());
	}

	/** */
	@Test
	public final void testSetHelpSwitch()
	{
		commandLineParser.setHelpSwitchName("a");
		assertEquals("a", commandLineParser.getHelpSwitchName());
	}

	/** */
	@Test
	public final void testGetVersionSwitch()
	{
		commandLineParser.setVersionSwitchName("a");
		assertEquals("a", commandLineParser.getVersionSwitchName());
	}

	/** */
	@Test
	public final void testSetVersionSwitch()
	{
		commandLineParser.setVersionSwitchName("a");
		assertEquals("a", commandLineParser.getVersionSwitchName());
	}

	/** */
	@Test
	public final void testRequireInitialization()
	{
		try
		{
			newCommandLineParser.requireInitialization();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
		}

		try
		{
			newCommandLineParser.setPossibleSwitches(possibleSwitches);
			newCommandLineParser.requireInitialization();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
		}

		try
		{
			// Specify erroneous command line to prevent parse() from completing
			newCommandLineParser.setCommandLine(cmdlineUsage + " --unknown");
			// newCommandLineParser.requireInitialization();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
			// fail(ERRONEOUS_EXCEPTION);
		}

		try
		{
			newCommandLineParser.requireInitialization();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
			// fail(ERRONEOUS_EXCEPTION);
		}

		try
		{
			commandLineParser.requireInitialization();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
		}

		// Initialize
		commandLineParser.setCommandLine(cmdlineUsage);
		commandLineParser.parse();

		try
		{
			commandLineParser.requireInitialization();
		}
		catch (IllegalStateException iae)
		{
			fail(ERRONEOUS_EXCEPTION);
		}
	}

	/** */
	@Test
	public final void testForbidNullString()
	{
		try
		{
			commandLineParser.forbidNullString("", "No error message");
		}
		catch (IllegalArgumentException iae)
		{
			fail(ERRONEOUS_EXCEPTION);
		}

		try
		{
			commandLineParser.forbidNullString(null, "Error message");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
			assertTrue(iae.getMessage().contains("Error message"));
		}
	}

	/** */
	@Test
	public final void testForbidEmptyString()
	{
		try
		{
			commandLineParser.forbidEmptyString("Non-empty string", "No error message");
		}
		catch (IllegalArgumentException iae)
		{
			fail(ERRONEOUS_EXCEPTION);
		}

		try
		{
			commandLineParser.forbidEmptyString(null, "Error message");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
			assertTrue(iae.getMessage().contains("Error message"));
		}

		try
		{
			commandLineParser.forbidEmptyString("", "Error message");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
			assertTrue(iae.getMessage().contains("Error message"));
		}
	}

	/** */
	@Test
	public final void testForbid()
	{
		try
		{
			commandLineParser.forbid(false, "No error message");
		}
		catch (IllegalArgumentException iae)
		{
			fail(ERRONEOUS_EXCEPTION);
		}

		try
		{
			commandLineParser.forbid(true, "Error message");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
	}

	/** */
	@Test
	public final void testRemoveCardinality()
	{
		// String switchSpecification = "--next(1-10)";

		// Null string
		try
		{
			String nul = null;
			commandLineParser.removeCardinality(nul);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Forgotten left brace
		try
		{
			commandLineParser.removeCardinality("--next(1-10");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		// Forgotten right brace
		try
		{
			commandLineParser.removeCardinality("--next1-10)");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		assertEquals(commandLineParser.removeCardinality("--next(1-10)"), "--next");

		// If cardinality is not specified, return unmodified string
		assertEquals(commandLineParser.removeCardinality("--next"), "--next");
	}

	/*******************************
	 * Initialization sequence tests
	 *******************************/

	/** */
	@Test
	public final void testInitSequence()
	{
		// Disallowed operations on an uninitialized command line parser

		// Require setPossibleSwitches() before setCommandLine()
		try
		{
			newCommandLineParser.setCommandLine(cmdlineUsage);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
		}
	}

	/** */
	@Test
	public final void testGetters()
	{
		CommandLineParser p = newCommandLineParser;

		// Require setPossibleSwitches() before getPossibleSwitches()
		try
		{
			p.getPossibleSwitches();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException ise)
		{
		}
		// Require setImplicitSwitch() before getImplicitSwitch()
		try
		{
			p.getImplicitSwitch();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException ise)
		{
		}
		// Require setCommandLine() before getCommandLine()
		try
		{
			p.getOriginalCommandLine();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException ise)
		{
		}
		// Require setArguments() before getArguments()
		try
		{
			p.getArguments();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException ise)
		{
		}
		p.setPossibleSwitches(possibleSwitches);
		assertTrue(p.isPossibleSwitch("--prev"));
		p.setImplicitSwitch("--file(1)");
		assertEquals("--file", p.getImplicitSwitch());
		p.setCommandLine(cmdlineOneValue);
		assertEquals(cmdlineOneValue, p.getOriginalCommandLine());
		assertEquals(p.getSwitchMap().size(), 2);
	}

	/*********************
	 * Less trivial tests
	 *********************/

	/** Test command line with quotes in arguments */
	@Test
	public final void testDoubleQuotesInCommandLine()
	{
		String s;
		s = "--onevalue \"val\" --next";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--onevalue"), "val");
		s = "--onevalue \"val1 val2\" --next";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--onevalue"), "val1 val2");
		s = "--onevalue \"val1 --next val2\" --next";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--onevalue"), "val1 --next val2");
		s = "--onevalue \"val3   val4\"";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--onevalue"), "val3   val4");
	}

	/** Test command line with several switch values per switch */
	@Test
	public final void testSwitchCardinality()
	{
		List<String> switchValues = null;

		// Require an exception in case of too few switch values
		try
		{
			commandLineParser.setCommandLine(cmdlineOneToThreeTooFew);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		try
		{
			commandLineParser.setCommandLine(cmdlineManyTooFew);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		// Require an exception in case of too many switch values
		try
		{
			commandLineParser.setCommandLine(cmdlineImplicitSwitchTooManyValues);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
			String message = iae.getMessage();
			assertTrue(message != null && message.contains("already has a value"));
			assertTrue(message != null && message.contains("implicit"));
		}

		// In case of too many switch values, assure that the 'overflow' values
		// get assigned to implicit switch and to switchless arguments:
		commandLineParser.setCommandLine(cmdlineOneToThreeTooMany);
		switchValues = commandLineParser.getSwitchValues("--onetothree");
		assertTrue(switchValues.size() == 3);
		String implicitSwitchValue =
				commandLineParser.getSwitchValue(commandLineParser.getImplicitSwitch());
		assertEquals(implicitSwitchValue, "val4");
		String[] switchlessArguments = commandLineParser.getSwitchlessArguments();
		assertEquals(switchlessArguments[0], "val4");

		commandLineParser.setCommandLine(cmdlineMany);
		switchValues = commandLineParser.getSwitchValues("--many");
		assertTrue(switchValues.size() == 6);
	}

	/** Test command line with a switch-less value */
	@Test
	public final void testSwitchlessValue()
	{
		String s;
		s = "--onevalue val1 val2 --next";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--onevalue"), "val1");
		assertEquals(commandLineParser.getSwitchValue(commandLineParser.getImplicitSwitch()),
				"val2");
		String[] switchlessArguments = commandLineParser.getSwitchlessArguments();
		assertEquals(switchlessArguments[0], "val2");
	}

	/** Test succession of command lines */
	@Test
	public final void testCommandLineSuccession()
	{
		String s;
		s = "--onevalue val1 val2 --next";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--onevalue"), "val1");
		assertEquals(commandLineParser.getSwitchValue(commandLineParser.getImplicitSwitch()),
				"val2");
		s = "--onevalue val3 val4 --next";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--onevalue"), "val3");
		assertEquals(commandLineParser.getSwitchValue(commandLineParser.getImplicitSwitch()),
				"val4");
	}

	/** Test succession of possible parameters */
	@Test
	public final void testSuccessionOfPossibleParameters()
	{
		String s;
		commandLineParser.setPossibleSwitches("--one(1) --next(0)");
		s = "--one val1 val2 --next";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--one"), "val1");
		assertEquals(commandLineParser.getSwitchValue(commandLineParser.getImplicitSwitch()),
				"val2");
		commandLineParser.setPossibleSwitches("--another(1) --prev(0)");
		// Require old switch to be invalid
		try
		{
			s = "--another val3 val4 --next";
			commandLineParser.setCommandLine(s);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		s = "--prev --another val3 val4";
		commandLineParser.setCommandLine(s);
		assertEquals(commandLineParser.getSwitchValue("--another"), "val3");
		assertEquals(commandLineParser.getSwitchValue(commandLineParser.getImplicitSwitch()),
				"val4");
	}
}
