package com.taitl.commandline;

import java.util.*;

/*
 * Copyright 2011 Taitl Design. All rights reserved.
 */

/**
 * Auxiliary class used by CommandLineParser in the parsing process.
 * 
 * Switch class holds information about a command line switch and its values.
 */
class Switch
{
	/** Name of switch. */
	private String switchName;

	/** Minimum allowed number of values for this switch. */
	private int minValues;

	/** Maximum allowed number of values for this switch. */
	private int maxValues;

	/** Values of this switch. Populated by the parsing process. */
	private List<String> values = null;

	/** Is implicit switch? */
	private boolean isImplicit = false;

	/**
	 * Constructs a Switch class object.
	 * 
	 * @param name
	 *            Name of the switch, e.g. --version.
	 * @param minNumberOfValues
	 *            Minimum allowed number of values for this switch (set while
	 *            initializing CommandLineParser object by calling
	 *            setPossibleSwitches().)
	 * @param maxNumberOfValues
	 *            Maximum allowed number of values for this switch (set while
	 *            initializing CommandLineParser object by calling
	 *            setPossibleSwitches().)
	 * */
	public Switch(String name, int minNumberOfValues, int maxNumberOfValues)
	{
		if (name == null || name.length() == 0)
		{
			throw new IllegalArgumentException("Switch name is null or empty string.");
		}
		if (name.contains("(") || name.contains(")"))
		{
			throw new IllegalArgumentException("Switch name can not contain special characters.");
		}
		if (minNumberOfValues < 0 || maxNumberOfValues < 0)
		{
			throw new IllegalArgumentException("Min and max values must be non-negative.");
		}
		if (minNumberOfValues > maxNumberOfValues)
		{
			throw new IllegalArgumentException(
					"Min number of values must be no less than max number of values.");
		}

		setSwitchName(name);
		setMinValues(minNumberOfValues);
		setMaxValues(maxNumberOfValues);
	}

	/**
	 * Sets switch name.
	 * 
	 * @param name
	 *            Name of switch.
	 */
	public void setSwitchName(String name)
	{
		switchName = name;
	}

	/**
	 * Gets switch name.
	 * 
	 * @return Name of switch.
	 */
	public String getSwitchName()
	{
		return switchName;
	}

	/**
	 * Sets minimum number of values that this switch can accept.
	 * 
	 * @param minNumberOfValues
	 *            The minimum number of values that this switch can accept.
	 */
	public void setMinValues(int minNumberOfValues)
	{
		if (minNumberOfValues < 0)
		{
			throw new IllegalArgumentException("Min number of values must be non-negative.");
		}

		minValues = minNumberOfValues;
	}

	/**
	 * Gets minimum number of values that this switch can accept.
	 * 
	 * @return The minimum number of values that this switch can accept.
	 */
	public int getMinValues()
	{
		return minValues;
	}

	/**
	 * Sets maximum number of values that this switch can accept.
	 * 
	 * @param maxNumberOfValues
	 *            The maximum number of values that this switch can accept.
	 */
	public void setMaxValues(int maxNumberOfValues)
	{
		if (maxNumberOfValues < 0)
		{
			throw new IllegalArgumentException("Max number of values must be non-negative.");
		}

		maxValues = maxNumberOfValues;
	}

	/**
	 * Gets maximum number of values that this switch can accept.
	 * 
	 * @return The maximum number of values that this switch can accept.
	 */
	public int getMaxValues()
	{
		return maxValues;
	}

	/**
	 * Adds value to the switch. This method may be called repeatedly, because
	 * switch values are stored in a list. The method may not be called more
	 * than maxValues times.
	 * 
	 * @param switchValue
	 *            The value of the switch.
	 */
	public void addValue(String switchValue)
	{
		if (values == null)
		{
			values = new ArrayList<String>();
		}
		if (values.size() >= maxValues)
		{
			String implicitDescription = isImplicit() ? " implicit" : "";

			if (maxValues == 1)
			{
				throw new IllegalStateException("The" + implicitDescription + " switch '"
						+ getSwitchName() + "' already has a value.");
			}
			else
			{
				throw new IllegalStateException("The " + implicitDescription + " switch '"
						+ getSwitchName() + "' already has " + values.size() + " value specified.");
			}

		}

		values.add(switchValue);
	}

	/**
	 * Returns the list of values of the switch.
	 * 
	 * @return The list of values of the switch.
	 */
	public List<String> getValues()
	{
		if (values == null)
		{
			values = new ArrayList<String>();
		}
		return values;
	}

	/**
	 * Validates the Switch object, ensuring that it has no less values that
	 * minValues, and no more values that maxValues. Throws
	 * IllegalStateException if this is not the case.
	 */
	public void validate()
	{
		int numValues = values == null ? 0 : values.size();

		if (numValues < minValues)
		{
			throw new IllegalStateException("Too few values are specified for switch " + switchName
					+ " (must be no less than " + minValues + " value, specified " + numValues
					+ " values).");
		}

		if (numValues > maxValues)
		{
			throw new IllegalStateException("Too many values are specified for switch "
					+ switchName + " (must be up to " + maxValues + " value, specified "
					+ numValues + " values).");
		}
	}

	/**
	 * Sets if this as an implicit switch.
	 * 
	 * @param implicit
	 *            True if this is an implicit switch.
	 */
	public void setImplicit(boolean implicit)
	{
		this.isImplicit = implicit;
	}

	/**
	 * Returns true if this is an implicit switch.
	 * 
	 * @return True if this is an implicit switch.
	 * */
	public boolean isImplicit()
	{
		return isImplicit;
	}
}
