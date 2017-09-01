package com.taitl.commandline;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SwitchTest extends TestCase
{
	// Error strings
	static final String MISSING_EXCEPTION = "Exception not thrown where it should";
	static final String ERRONEOUS_EXCEPTION = "Exception thrown where it shouldn't";

	// The protagonist
	Switch sw;

	@Override
	@Before
	public void setUp() throws Exception
	{
		sw = new Switch("--switch", 0, 10);
	}

	@Override
	@After
	public void tearDown() throws Exception
	{
		sw = null;
	}

	@Test
	public final void testSwitch()
	{
		// Disallow null name
		try
		{
			new Switch(null, 1, 2);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		// Disallow empty name
		try
		{
			new Switch("", 1, 2);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		// Disallow (left) brace name
		try
		{
			new Switch("--switch(1)", 1, 2);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		// Disallow (right) brace name
		try
		{
			new Switch("--switch1)", 1, 2);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		// Disallow negative min values
		try
		{
			new Switch("--switch", -1, 2);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		// Disallow negative max values
		try
		{
			new Switch("--switch", 1, -2);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}
		// Disallow min values greater than max values
		try
		{
			new Switch("--switch", 2, 1);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		new Switch("--switch", 1, 2);
	}

	@Test
	public final void testSetSwitchName()
	{
		sw.setSwitchName("--another");
		assertEquals("--another", sw.getSwitchName());
	}

	@Test
	public final void testGetSwitchName()
	{
		assertEquals("--switch", sw.getSwitchName());
	}

	@Test
	public final void testSetMinValues()
	{
		// Disallow negative values
		try
		{
			sw.setMinValues(-1);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		sw.setMinValues(1);
		assertEquals(1, sw.getMinValues());
	}

	@Test
	public final void testGetMinValues()
	{
		assertEquals(0, sw.getMinValues());
	}

	@Test
	public final void testSetMaxValues()
	{
		// Disallow negative values
		try
		{
			sw.setMaxValues(-1);
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalArgumentException iae)
		{
		}

		sw.setMaxValues(1);
		assertEquals(1, sw.getMaxValues());
	}

	@Test
	public final void testGetMaxValues()
	{
		assertEquals(10, sw.getMaxValues());
	}

	@Test
	public final void testAddValue()
	{
		// Disallow next value if max values already reached
		try
		{
			Switch s = new Switch("--s", 1, 1);
			s.addValue("uno");
			s.addValue("dos");
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
		}
		sw.addValue("value");
		assertEquals("value", sw.getValues().get(0));
	}

	@Test
	public final void testGetValues()
	{
		sw.addValue("value0");
		sw.addValue("value1");
		assertEquals("value0", sw.getValues().get(0));
		assertEquals("value1", sw.getValues().get(1));
	}

	@Test
	public final void testValidate()
	{
		sw.setMinValues(1);
		sw.setMaxValues(2);

		// Disallow insufficient number of values
		try
		{
			sw.validate();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
		}

		// Disallow excessive number of values
		try
		{
			sw.addValue("uno");
			sw.addValue("dos");
			sw.setMaxValues(1);
			sw.validate();
			fail(MISSING_EXCEPTION);
		}
		catch (IllegalStateException iae)
		{
		}

		sw.setMaxValues(2);
		sw.validate();
	}
}
