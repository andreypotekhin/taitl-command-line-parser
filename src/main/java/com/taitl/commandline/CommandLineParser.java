package com.taitl.commandline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/*
 * Copyright 2011 Taitl Design. All rights reserved.
 */

/**
 * CommandLineParser class takes command line arguments and parses them into a
 * map of [switch, list of values] pairs. A switch on the command line can have
 * no, one, or many values. The command line can also have arguments that are
 * not related to any switches.
 * <p>
 * Usage:
 * <p>
 * 
 * <pre>
 * public static void main(String[] arguments)
 * {
 * 	CommandLineParser commandLineParser = new CommandLineParser();
 * 	commandLineParser.setPossibleSwitches(&quot;--version(0) --usage(0)&quot;
 * 			+ &quot; --help(0) --multi(*)&quot;);
 * 	commandLineParser.setEmptyCommandLineSwitch(&quot;--usage&quot;);
 * 	commandLineParser.setImplicitSwitch(&quot;--file(1)&quot;);
 * 	commandLineParser.setArguments(arguments);
 * 	// ...same as commandLineParser.parse(arguments);
 * 
 * 	if (commandLineParser.isUsageRequested())
 * 	{
 * 		System.out.println(usageText);
 * 	}
 * 	else if (commandLineParser.isVersionRequested())
 * 	{
 * 		System.out.println(VERSION_STRING);
 * 	}
 * 	else if (commandLineParser.isHelpRequested())
 * 	{
 * 		System.out.println(helpText);
 * 	}
 * 	else if (commandLineParser.isSwitchPresent(&quot;--multi&quot;))
 * 	{
 * 		// Process custom switch
 * 		List&lt;String&gt; switchValues = commandLineParser
 * 				.getSwitchValues(&quot;--multi&quot;);
 * 		String[] arguments = commandLineParser.getSwitchlessArguments();
 * 		// ...
 * 	}
 * }
 * </pre>
 * 
 * @author Andrey Potekhin, 03/08/2011
 */
public class CommandLineParser
{

	/** Default usage switch name: --usage. */
	static final String DEFAULT_USAGE_SWITCH = "--usage";

	/** Default help switch name: --help. */
	static final String DEFAULT_HELP_SWITCH = "--help";

	/** Default version switch name: --version. */
	static final String DEFAULT_VERSION_SWITCH = "--version";

	/** Default switch name prefixes: -- and -. */
	static final String DEFAULT_SWITCH_PREFIXES = "(--|-)";

	/** Default value of usage switch, defaulted to --usage. */
	private String usageSwitchName = DEFAULT_USAGE_SWITCH;

	/** Default value of usage switch, defaulted to --help. */
	private String helpSwitchName = DEFAULT_HELP_SWITCH;

	/** Default value of usage switch, defaulted to --version. */
	private String versionSwitchName = DEFAULT_VERSION_SWITCH;

	/** Default value of switch prefixes, defaulted to (--|-). */
	private String switchPrefixesMask = DEFAULT_SWITCH_PREFIXES;

	/** The original command line. */
	private String originalCommandLine = null;

	/** The original arguments array, as usually passed in the main() method. */
	private String[] arguments = null;

	/**
	 * The command line arguments that do not have a switch corresponding to
	 * them.
	 */
	private String[] switchlessArguments = null;

	/**
	 * Has this object been properly initialized? This is false unless both
	 * setPossibleSwitches() and setArguments()/parse() has been called.
	 */
	private boolean isInitialized = false;

	/** The set of possible switches. */
	private Set<String> possibleSwitches = new LinkedHashSet<String>();

	/**
	 * The default (implicit) switch to which all switchless arguments are
	 * attributed.
	 * 
	 * @see CommandLineParser#setImplicitSwitch
	 */
	private String implicitSwitch = null;

	/**
	 * Main structure to hold information about the parsed command line switches
	 * and their values and switchless arguments.
	 */
	private Map<String, List<String>> switchMap = new LinkedHashMap<String, List<String>>();

	/** Command line switches as result of parsing. */
	private Map<String, Switch> switches = new LinkedHashMap<String, Switch>();

	/**
	 * Default constructor.
	 */
	public CommandLineParser()
	{
		originalCommandLine = null;
		isInitialized = false;
	}

	/**
	 * Sets command line arguments form the array of arguments passed to Java
	 * program's main() method, and parses them into map of [switch-list of
	 * values] pairs by calling
	 * <p>
	 * According to Java documentation, the arguments that are passed to mail()
	 * method conform to the following rules:
	 * <p>
	 * <p>
	 * - On the command line, Java program arguments are separated by spaces and
	 * tabs, except when an argument is enclosed by double-quotes.
	 * <p>
	 * - Leading and trailing whitespace characters are removed from the values
	 * stored in the arguments array.
	 * <p>
	 * - The double-quoted argument is not broken into several separate
	 * arguments.
	 * <p>
	 * - For arguments enclosed with double-quotes, the double-quotes are
	 * removed.
	 * <p>
	 * 
	 * @param args
	 *            Command line arguments to parse.
	 */
	public final void setArguments(final String[] args)
	{
		forbid(args == null, "Args argument must not be null.");

		StringBuffer commandLine = new StringBuffer();
		String arg;
		StringBuilder stringBuilder;

		for (String arg2 : args)
		{
			forbid(arg2 == null, "Null value in argument array.");

			arg = arg2.trim();

			// If argument has spaces in it, enclose it in double quotes
			if (arg.matches(".*\\s.*"))
			{
				// arg = "\"" + arg + "\"";
				stringBuilder = new StringBuilder();
				stringBuilder.append("\"").append(arg).append("\"");
				arg = stringBuilder.toString();
			}
			if (commandLine.length() > 0 && arg.length() > 0)
			{
				commandLine.append(" ");
			}
			commandLine.append(arg);
		}

		originalCommandLine = commandLine.toString();
		arguments = args;
		switches.clear();
		switchlessArguments = null;

		// Uninitialize the object
		isInitialized = false;

		// Immediately parse
		parse();
	}

	/**
	 * An alternative to <code>setArguments()</code> method, sets arguments
	 * using a command line string.
	 * <p>
	 * According to Java documentation, the arguments that are passed to mail()
	 * method conform to the following rules:
	 * <p>
	 * <p>
	 * - On the command line, Java program arguments are separated by spaces and
	 * tabs, except when an argument is enclosed by double-quotes.
	 * <p>
	 * - Leading and trailing whitespace characters are removed from the values
	 * stored in the arguments array.
	 * <p>
	 * - The double-quoted argument is not broken into several separate
	 * arguments.
	 * <p>
	 * - For arguments enclosed with double-quotes, the double-quotes are
	 * removed.
	 * <p>
	 * 
	 * @param commandLine
	 *            Command line to parse.
	 */
	public void setCommandLine(String commandLine)
	{
		forbidNullString(commandLine, "commandLine");
		originalCommandLine = commandLine;

		// String[] args = commandLine.split("\\s");

		// Go over command line, splitting it into whitespace-separated
		// arguments, preserving arguments enclosed with double quotes,
		// treating them as a single argument, while also removing their
		// enclosing double quotes.
		boolean insideDoubleQuotes = false;
		boolean inWord = false;
		boolean isWhitespace = false;
		StringBuffer arg = new StringBuffer();
		List<String> args = new ArrayList<String>();

		for (int i = 0; i < commandLine.length(); i++)
		{
			char c = commandLine.charAt(i);
			boolean finalizeArgument = false;
			boolean isClosingDoubleQuote = false;

			if (c == '"')
			{
				if (!insideDoubleQuotes)
				{
					insideDoubleQuotes = true;
					continue;
				}
				else
				{
					insideDoubleQuotes = false;
					isClosingDoubleQuote = true;
					finalizeArgument = true;
				}
			}

			if (insideDoubleQuotes)
			{
				arg.append(c);
				continue;
			}

			isWhitespace = Character.isWhitespace(c);

			if (isWhitespace)
			{
				if (inWord)
				{
					inWord = false;
					finalizeArgument = true;
				}
			}
			else
			{
				if (!inWord && !isClosingDoubleQuote)
				{
					inWord = true;
				}
			}

			if (inWord)
			{
				arg.append(c);
			}

			if (finalizeArgument)
			{
				// Finalize argument
				args.add(arg.toString());
				arg.setLength(0);
			}
		}

		// Finalize last argument
		if (arg.length() > 0)
		{
			args.add(arg.toString());
		}

		// Convert to String[]
		String[] argArray = new String[args.size()];
		for (int i = 0; i < args.size(); i++)
		{
			argArray[i] = args.get(i);
		}

		// Immediately parse
		setArguments(argArray);
	}

	/**
	 * Gets command line arguments previously set by call to
	 * <code> setArguments()</code> or <code>setCommandLine()</code>.
	 * <p>
	 * 
	 * @return String array of arguments previously set by a call to
	 *         setArguments() or setCommandLine(). Throws IllegalStateException
	 *         if user tries to get arguments that have not yet been set.
	 */
	public final String[] getArguments()
	{
		forbidState(arguments == null,
				"You must call setArguments() or setCommandLine() before calling this method.");
		return arguments;
	}

	/**
	 * Returns the set of possible command line switches previously specified
	 * with a call to setPossibleSwitches().
	 * 
	 * @return The set of possible switches previously specified with a call to
	 *         setPossibleSwitches().
	 */
	public Set<String> getPossibleSwitches()
	{
		forbidState(possibleSwitches.isEmpty(),
				"You must call setPossibleSwitches() before calling this method.");
		return possibleSwitches;
	}

	/**
	 * This method lets the CommandLineParser object know of the legitimate
	 * switches. You are required to call this method after you constructed the
	 * CommandLineParser object, and before proceeding with any further work
	 * with it.
	 * <p>
	 * Each switch is succeed by parenthesis indicating the potential number of
	 * it arguments, for example:
	 * <p>
	 * (0) - no arguments,
	 * <p>
	 * (1) - one argument,
	 * <p>
	 * (0-3) - from zero to three arguments,
	 * <p>
	 * (*) - any number of arguments,
	 * <p>
	 * (3-*) - from three to any greater number of arguments, and so on.
	 * <p>
	 * Example:
	 * <p>
	 * <code>setPossibleSwitches("--version(0) --usage(0)" 
	 * 		+ " --help(0-1) --multi(1-*)");</code>
	 * 
	 * @param possibleSwitchesList
	 *            Space-separated list of allowed command line switches, each of
	 *            which is followed by parenthesized number of switch values.
	 *            This can be either a precise number, a min-max expression, or
	 *            expression involving an asterisk (*), which stands for any
	 *            number of arguments.
	 */
	public final void setPossibleSwitches(String possibleSwitchesList)
	{
		forbidEmptyString(possibleSwitchesList, "possibleSwitchesList");

		// Forget old switches that were specified earlier
		possibleSwitches.clear();

		// Parse comma-separated list into hash map
		for (String switchSpecification : possibleSwitchesList.split(" "))
		{
			addPossibleSwitch(switchSpecification);
		}

		// The implicit switch, if specified, must be a part of possible
		// switch collection. Since we have emptied the set of
		// possible switches in the beginning of this method, we
		// need to add the implicit switch again to it.
		if (implicitSwitch != null)
		{
			setImplicitSwitch(implicitSwitch);
		}
	}

	/**
	 * Sets the implicit switch, that is, the switch to which the argument value
	 * attributed of the first argument that is not preceded by any switch.
	 * Example:
	 * <p>
	 * If you call setImplicitSwitch("--file(1)") and process command line
	 * <p>
	 * <code>myprogram --vertical --split myfile.txt</code>
	 * <p>
	 * then CommandLineParser will attribute "myfile.txt" value to the "--file"
	 * switch, and you will be able to retrieve this value by calling
	 * <code>getSwitchValue("--file")</code>.
	 * <p>
	 * <p>
	 * Note: you don't have to specify the implicit switch in the call to
	 * <code>setPossibleSwitches()</code>.
	 * 
	 * @param implicitSwitchSpecification
	 *            Default (implicit) switch will correspond to command line
	 *            parameter that is not preceded with any switch. If there are
	 *            several such parameters, they are united in a list and this
	 *            list is set a list of values that correspond to this implicit
	 *            switch.
	 */
	public void setImplicitSwitch(String implicitSwitchSpecification)
	{
		assert possibleSwitches != null;
		forbid(possibleSwitches.isEmpty(),
				"You must call setPossibleSwitches() before calling this method.");
		forbidEmptyString(implicitSwitchSpecification,
				"implicitSwitchSpecification");

		/*
		 * if (implicitSwitch != null &&
		 * isPossibleSwitch(removeCardinality(implicitSwitch))) {
		 * removePossibleSwitch(implicitSwitch); }
		 */

		implicitSwitch = implicitSwitchSpecification;
		// addPossibleSwitch(implicitSwitch);
	}

	/**
	 * Returns the implicit command line switch, that is, the implicit switch
	 * (set earlier with a call to setImplicitSwitch()), which corresponds to
	 * command line parameter(s) that are not part of values of any other
	 * switch. Example:
	 * 
	 * <pre>
	 * command line: myprogram --verbose file.txt
	 * ...
	 * setPossibleSwitches("--verbose(0)");
	 * setImplicitSwitch(&quot;--file&quot;);
	 * ...
	 * String filename = getSwitchValue(getImplicitSwitch());
	 * </pre>
	 * <p>
	 * A call to this method must be preceded by a call to setImplicitSwitch(),
	 * otherwise an IllegalStateException is thrown.
	 * <p>
	 * To get the implicit switch value(s), call <code>getSwitchValue()</code>
	 * or <code>getSwitchValues()</code> with the name of implicit switch.
	 * 
	 * @return The name of the implicit switch, e.g. "--file".
	 * @throws IllegalArgumentException
	 *             when this method is not preceded by a call to
	 *             setImplicitSwitch()
	 */
	public String getImplicitSwitch() throws IllegalArgumentException
	{
		forbidState(
				implicitSwitch == null,
				"Null in implicit switch value. You need to call setImplicitSwitch() before calling this method.");
		return removeCardinality(implicitSwitch);
	}

	/**
	 * Parses command line arguments specified in a call to setArguments() or
	 * setCommandLine() and prepares as a result the data that reflect which
	 * command line switches were specified and with which values. This data can
	 * be retrieved with calls to getSwitchValue(), getSwitchValues(),
	 * getSwitchValueCount(), isSwitchPresent().
	 * 
	 * @throws IllegalArgumentException
	 *             - when a violation of parsing rules is encountered, for
	 *             example, when a switch is present on command line which was
	 *             not specified in a call to setPossibleSwitches().
	 * @throws IllegalStateException
	 *             when the CommandLineParser object is not initialized, for
	 *             instance, when its the command line arguments have not been
	 *             set yet. You must at a minimum call setPossibleSwitches(),
	 *             setArguments() or setCommandLine() before calling this
	 *             method.
	 */
	protected void parse() throws IllegalArgumentException,
			IllegalStateException
	{
		forbidState(
				originalCommandLine == null || arguments == null,
				"You must call setArguments() or setCommandLine() method before calling parse() method without arguments.");

		// Here happens the magic!
		doParsing(arguments);

		isInitialized = true;

		// Make sure the object is now properly initialized
		requireInitialization();
	}

	/**
	 * Is command line switch present?
	 * 
	 * @param switchString
	 *            The command line switch, e.g. --version.
	 * @return True or false depending on whether the switch is present on the
	 *         command line.
	 */
	public boolean isSwitchPresent(String switchString)
	{
		requireInitialization();
		forbidEmptyString(switchString, "switchString");
		forbid(!looksLikeSwitch(switchString),
				"Switch must start with one of switch prefixes defined by the switchPrefixesMask mask.");
		boolean returnValue = switches.containsKey(switchString);
		return returnValue;
	}

	/**
	 * Returns switch value.
	 * <p>
	 * Example:
	 * <p>
	 * For command line <code>myprogram --version</code>
	 * <p>
	 * <code>getSwitchValue("--version")</code> will return an empty string.
	 * <p>
	 * For command line <code>myprogram --version ver</code>
	 * <p>
	 * <code>getSwitchValue("--version")</code> will return string "ver".
	 * <p>
	 * <p>
	 * If the switch is not present, the <code>IllegalArgumentException</code>
	 * is thrown.
	 * <p>
	 * <p>
	 * 
	 * @param switchString
	 *            The name of command line switch.
	 * @return The value of command line switch, empty string if switch has no
	 *         value (as, for example, in case with --version switch).
	 */
	public String getSwitchValue(String switchString)
	{
		String returnValue = null;

		requireInitialization();
		forbidEmptyString(switchString, "switchString");
		forbid(!isSwitchPresent(switchString),
				"Switch "
						+ switchString
						+ " is not present on the command line. Use isSwitchPresent() to check for a switch.");
		List<String> valueList = getSwitchValues(switchString);
		assert valueList != null;
		if (valueList.size() == 0)
		{
			returnValue = "";
		}
		else
		{
			returnValue = valueList.get(0);
		}
		forbid(valueList.size() > 1,
				"Switch "
						+ switchString
						+ " has several values. Use getSwitchValueCount() to check for number of switch values."
						+ " Use getSwitchValues() to get the list of switch values.");
		assert returnValue != null; // Make sure we didn't skip any branches in
									// logic
		return returnValue;
	}

	/**
	 * Returns the list of values of command switch. In the command line, the
	 * values are assigned to a switch until either the next switch is
	 * encountered. Next switch is not considered such if it appears inside a
	 * single or double quoted string.
	 * 
	 * The arguments that
	 * <p>
	 * Example:
	 * <p>
	 * For command line <code>myprogram --version</code>
	 * <p>
	 * <code>getSwitchValue("--version")</code> will return an empty list.
	 * <p>
	 * For command line myprogram --version ver
	 * <p>
	 * <code>getSwitchValue("--version")</code> will return a list with one
	 * element, string "ver".
	 * <p>
	 * For command line <code>myprogram --version ver ber</code>
	 * <p>
	 * <code>getSwitchValue("--version")</code> will return a list with two
	 * elements: strings "ver" and "ber".
	 * <p>
	 * <p>
	 * If the switch is not present, the <code>IllegalArgumentException</code>
	 * is thrown.
	 * <p>
	 * <p>
	 * 
	 * @param switchString
	 *            The name of command line switch.
	 * @return The list of values of command line switch.
	 */
	public List<String> getSwitchValues(String switchString)
	{
		List<String> returnValue = null;

		requireInitialization();
		forbidEmptyString(switchString, "switchString");
		forbid(!isSwitchPresent(switchString),
				"Switch "
						+ switchString
						+ " is not present on the command line. Use isSwitchPresent() to check for a switch.");

		List<String> valueList = switchMap.get(switchString);
		assert valueList != null;
		returnValue = valueList;
		assert returnValue != null; // Make sure we didn't skip any branches in
									// logic
		return returnValue;
	}

	/**
	 * Returns value count of switch.
	 * <p>
	 * Example:
	 * <p>
	 * For command line <code>myprogram --version</code>
	 * <p>
	 * <code>getSwitchValueCount("--version")</code> will return 0.
	 * <p>
	 * For command line <code>myprogram --version ver</code>
	 * <p>
	 * <code>getSwitchValueCount("--version")</code> will return 1.
	 * <p>
	 * <p>
	 * If the switch is not present, the <code>IllegalArgumentException</code>
	 * is thrown.
	 * <p>
	 * <p>
	 * 
	 * @param switchString
	 *            The name of command line switch.
	 * @return The number of values of command line switch.
	 */
	public int getSwitchValueCount(String switchString)
	{
		Integer returnValue = null;

		requireInitialization();
		forbidEmptyString(switchString, "switchString");
		forbid(!isSwitchPresent(switchString),
				"Switch "
						+ switchString
						+ " is not present on the command line. Use isSwitchPresent() to check for a switch.");

		List<String> valueList = switchMap.get(switchString);
		assert valueList != null;
		returnValue = Integer.valueOf(valueList.size());
		assert returnValue != null; // Make sure we didn't skip any branches in
									// logic
		return returnValue.intValue();
	}

	/**
	 * Returns the original command line that was parsed.
	 * 
	 * @return The command line that has been passed to setCommandLine() method,
	 *         or to setArguments() as array of arguments.
	 */
	public String getOriginalCommandLine()
	{
		forbidState(originalCommandLine == null,
				"You must call setCommandLine() or setArguments() before calling this method.");
		return originalCommandLine;
	}

	/**
	 * Returns true if usageSwitchName (default --usage) is present on the
	 * command line.
	 * 
	 * @return True if usageSwitchName is present on command line.
	 */
	public boolean isUsageRequested()
	{
		requireInitialization();
		return isSwitchPresent(usageSwitchName);
	}

	/**
	 * Returns true if helpSwitchName (default --help) is present on the command
	 * line.
	 * 
	 * @return True if helpSwitchName is present on command line.
	 */
	public boolean isHelpRequested()
	{
		requireInitialization();
		return isSwitchPresent(helpSwitchName);
	}

	/**
	 * Returns true if versionSwitchName (default --version) is present on the
	 * command line.
	 * 
	 * @return True if versionSwitchName is present on command line.
	 */
	public boolean isVersionRequested()
	{
		requireInitialization();
		return isSwitchPresent(versionSwitchName);
	}

	/**
	 * Returns the default value of usageSwitchName (default --usage).
	 * 
	 * @return The default value of usageSwitchName.
	 */
	public String getUsageSwitchName()
	{
		return usageSwitchName;
	}

	/**
	 * Sets the default value of usageSwitchName.
	 * 
	 * @param usageSwitch
	 *            New default value of usageSwitchName.
	 */
	public void setUsageSwitchName(String usageSwitch)
	{
		forbid(looksLikeSwitch(usageSwitch),
				"Switch must start with an appropriate switch prefix.");
		this.usageSwitchName = usageSwitch;
	}

	/**
	 * Returns the default value of helpSwitchName (default --help).
	 * 
	 * @return The default value of helpSwitchName.
	 */
	public String getHelpSwitchName()
	{
		return helpSwitchName;
	}

	/**
	 * Sets the default value of helpSwitchName.
	 * 
	 * @param helpSwitch
	 *            New default value of helpSwitchName.
	 */
	public void setHelpSwitchName(String helpSwitch)
	{
		this.helpSwitchName = helpSwitch;
	}

	/**
	 * Returns the default value of versionSwitchName (default --version).
	 * 
	 * @return The default value of versionSwitchName.
	 */
	public String getVersionSwitchName()
	{
		return versionSwitchName;
	}

	/**
	 * Sets the default value of versionSwitchName.
	 * 
	 * @param versionSwitch
	 *            New default value of versionSwitchName.
	 */
	public void setVersionSwitchName(String versionSwitch)
	{
		this.versionSwitchName = versionSwitch;
	}

	/**
	 * Checks if CommandLineParser has not been properly initialized, that is,
	 * the methods <code>setPossibleSwitches()</code> and
	 * <code>setArguments()</code> have been called, and therefore, the parsing
	 * process has been carried out.
	 * 
	 * Throws <code>IllegalStateException</code> exception if the
	 * <code>CommandLineParser</code> has not been initialized, that is, the
	 * methods <code>setPossibleSwitches()</code> and
	 * <code>setArguments()</code> have not been called.
	 * 
	 * @throws IllegalStateException
	 *             if CommandLineParser has not been initialized.
	 */
	protected void requireInitialization() throws IllegalStateException
	{
		assert possibleSwitches != null;

		if (possibleSwitches.isEmpty())
		{
			throw new IllegalStateException(
					"This class must be initialized first by calling setPossibleSwitches() and setArguments() method.");
		}
		if (arguments == null)
		{
			throw new IllegalStateException(
					"This class must be initialized first by calling setArguments() or setCommandLine() method.");
		}
		if (!isInitialized)
		{
			throw new IllegalStateException(
					"This class must be initialized first by calling setArguments() or parse() method.");
		}
	}

	/**
	 * Throws IllegalArgumentException if the specified string is null.
	 * 
	 * @param s
	 *            The string to check for nullability.
	 * @param parameterName
	 *            The name of parameter of the caller method that we want to
	 *            include in exception description if the tested string is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if passed-in string is null.
	 */
	protected void forbidNullString(String s, String parameterName)
			throws IllegalArgumentException
	{
		if (s == null)
		{
			throw new IllegalArgumentException(
					"Non-null value required in parameter " + parameterName
							+ ".");
		}
	}

	/**
	 * Throws IllegalArgumentException if the specified string is a null or
	 * empty string.
	 * 
	 * @param s
	 *            The string to check for nullability or emptiness.
	 * @param parameterName
	 *            The name of parameter of the caller method that we want to
	 *            include in exception description if the tested string is null
	 *            or empty.
	 * 
	 * @throws IllegalArgumentException
	 *             if passed-in string is null or empty.
	 */
	protected void forbidEmptyString(String s, String parameterName)
			throws IllegalArgumentException
	{
		if (s == null)
		{
			throw new IllegalArgumentException(
					"Non-null value required in parameter " + parameterName
							+ ".");
		}
		if (s.length() == 0)
		{
			throw new IllegalArgumentException(
					"Non-empty value required in parameter " + parameterName
							+ ".");
		}
	}

	/**
	 * Throws IllegalArgumentException if the specified boolean condition is
	 * false.
	 * 
	 * @param condition
	 *            The boolean condition string to check for falseness.
	 * @param message
	 *            The message to set to the thrown IllegalArgumentException.
	 * 
	 * @throws IllegalArgumentException
	 *             if passed-in boolean condition is false.
	 */
	protected void forbid(boolean condition, String message)
			throws IllegalArgumentException
	{
		if (condition)
		{
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Throws IllegalStateException if the specified boolean condition is false.
	 * Same as forbid() except an IllegalStateException is thrown rather than
	 * IllegalArgumentException.
	 * 
	 * @param condition
	 *            The boolean condition string to check for falseness.
	 * @param message
	 *            The message to set to the thrown IllegalArgumentException.
	 * 
	 * @throws IllegalStateException
	 *             if passed-in string is null or empty.
	 */
	protected void forbidState(boolean condition, String message)
			throws IllegalStateException
	{
		if (condition)
		{
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Returns command line arguments that do not have any switch corresponding
	 * to them.
	 * <p>
	 * 
	 * Alternatively, you can use <code>getValues(getImlicitSwitchName())</code>.
	 * 
	 * @return The string array consisting of values of switchless arguments in
	 *         the order they appear in the command line.
	 */
	public String[] getSwitchlessArguments()
	{
		requireInitialization();
		forbidState(switchlessArguments == null,
				"Member switchlessArguments must not be null.");
		// forbidState(switchlessArguments.length == 0,
		// "Member switchlessArguments must not be empty.");
		return switchlessArguments;
	}

	/*
	 * Deep implementation methods
	 */

	/**
	 * Implements the algorithm for parsing command line arguments into
	 * meaningful pairs of switch name - switch value(s).
	 * <p>
	 * 
	 * This method considers the argument array to follow Java rules for the
	 * array of arguments passed into Java program's main() method:
	 * <p>
	 * - On the command line, Java program arguments are separated by spaces and
	 * tabs, except when an argument is enclosed in double quotes.
	 * <p>
	 * - Leading and trailing whitespace characters are removed from the values
	 * stored in the arguments array.
	 * <p>
	 * - The double-quoted argument is not broken into several separate
	 * arguments.
	 * <p>
	 * - For arguments enclosed with double quotes, the double quotes are
	 * removed.
	 * <p>
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws IllegalArgumentException
	 *             when a violation of parsing rules is encountered, for
	 *             example, when a switch is present on command line which was
	 *             not specified in a call to setPossibleSwitches().
	 * @throws IllegalStateException
	 *             when the CommandLineParser object is not initialized, for
	 *             instance, when its the command line arguments have not been
	 *             set yet. You must at a minimum call setPossibleSwitches(),
	 *             setArguments() or setCommandLine() before calling this
	 *             method.
	 */
	protected void doParsing(String[] args) throws IllegalArgumentException,
			IllegalStateException
	{
		assert possibleSwitches != null;

		if (possibleSwitches.isEmpty())
		{
			throw new IllegalStateException(
					"This class must be initialized first by calling setPossibleSwitches() and setArguments() method.");
		}
		if (args == null)
		{
			throw new IllegalArgumentException(
					"This arguments array must not be null.");
		}

		// Process array elements one by one, accumulating
		// the found switches and their values into switch-to-value list map.

		Switch curSwitch = null;
		Map<String, Switch> switchByName = new LinkedHashMap<String, Switch>();
		List<String> switchless = new ArrayList<String>();

		// Implicit switch to hold the switchless arguments
		Switch implicit = null;
		if (implicitSwitch != null)
		{
			implicit = createSwitch(implicitSwitch);
			implicit.setImplicit(true);
		}

		// MAIN LOOP: go over arguments one by one, making decisions
		for (String arg : args)
		{
			String argument = arg;
			// boolean isLastArgument = (i == args.length - 1);
			boolean isSwitch = looksLikeSwitch(argument);
			String switchName = isSwitch ? argument : null;
			boolean needToFinalizePreviousSwitch = (curSwitch != null)
					&& isSwitch;
			boolean needToInilializeNewSwitch = isSwitch;

			// Parsing rule 1: forbid unknown switches. All switches must be
			// declared in a call to setPossibleSwitches().
			forbid(isSwitch && !isPossibleSwitch(argument),
					"Unknown switch found: "
							+ argument
							+ ". You must specify this switch in a call to setPossibleSwitches() first.");

			if (needToFinalizePreviousSwitch)
			{
				// Finalize previous switch
				if (curSwitch != null)
				{
					curSwitch = null;
				}
			}
			if (needToInilializeNewSwitch)
			{
				// Initialize data structures for newly discovered switch
				curSwitch = createSwitch(argument);
				switchByName.put(switchName, curSwitch);
			}

			if (isSwitch)
			{
				// Next will come the switch value, if any
				continue;
			}
			else
			{
				boolean tooManyArguments = false;

				if (curSwitch != null)
				{
					tooManyArguments = (curSwitch.getValues().size() >= curSwitch
							.getMaxValues());
				}

				if (curSwitch != null && !tooManyArguments)
				{
					curSwitch.addValue(argument);
				}
				else
				{
					// Assign argument to switchless arguments
					switchless.add(argument);

					// If there is an implicit switch, assign argument to it,
					// too.
					if (implicitSwitch != null)
					{
						implicit.addValue(argument);
					}
				}
			}
		}
		// END OF MAIN LOOP

		// Add implicit switch values, if any
		if (implicitSwitch != null && implicit.getValues().size() > 0)
		{
			switchByName.put(getImplicitSwitch(), implicit);
		}

		// Copy accumulated switch map to CommandLineParser's switch
		// collection.
		switches.clear();
		switchMap.clear();

		try
		{
			for (Entry<String, Switch> entry : switchByName.entrySet())
			{
				String switchName = entry.getKey();
				Switch sw = entry.getValue(); // switchByName.get(switchName);
				sw.validate();
				switches.put(switchName, sw);
				List<String> values = new ArrayList<String>(sw.getValues());
				// Collections.copy(values, sw.getValues());
				switchMap.put(switchName, values);
			}
		}
		catch (IllegalStateException ise)
		{
			switches.clear();
			switchMap.clear();
			throw new IllegalArgumentException(ise.getMessage());
		}

		// Not to forget the rest of the arguments
		switchlessArguments = new String[switchless.size()];
		for (int i = 0; i < switchless.size(); i++)
		{
			switchlessArguments[i] = switchless.get(i);
		}

		// Considered all possible switches?

		forbidState(switches == null,
				"Post-condition failure: member 'switches' is null");
		forbidState(switchMap == null,
				"Post-condition failure: member 'switchMap' is null");
		forbidState(
				switchMap.size() != switches.size(),
				"Post-condition failure: member 'switchMap' has different size from member 'switches'");

		// Ensure the object is now properly initialized.
		// requireInitialization();
	}

	/**
	 * Returns true if CommandLineParser has been initialized, that is the
	 * setPossibleSwitches() and setArguments() methods have been called.
	 * 
	 * @return True if CommandLineParser has been initialized.
	 */
	public boolean isInitialized()
	{
		return isInitialized;
	}

	/**
	 * Returns the mapping of switch names to list of their corresponding
	 * values. Example: for command line "--version major minor" a call to
	 * getSwitchMap() will return a map consisting of one element which maps
	 * switch "--version" to list of two values, "major" and "minor" (without
	 * quotes).
	 * 
	 * @return The mapping of command line switch names to lists of their
	 *         corresponding values.
	 */
	public Map<String, List<String>> getSwitchMap()
	{
		return switchMap;
	}

	/**
	 * Adds a switch to the required switches.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * addPossibleSwitch(&quot;--switch(0-*)&quot;);
	 * </pre>
	 * 
	 * will allow for the command line switch <code>--switch</code> with zero to
	 * infinite number of arguments.
	 * 
	 * @param switchSpecification
	 *            Name and cardinality of switch, e.g. --file(1), meaning that
	 *            the --file switch requires exactly one argument.
	 */
	protected void addPossibleSwitch(String switchSpecification)
	{
		forbidEmptyString(switchSpecification, "switchSpecification");
		switchSpecification = switchSpecification.trim();
		String switchName = removeCardinality(switchSpecification);
		forbid(isPossibleSwitch(switchName), "The possible switch "
				+ switchName + " is already defined for this object.");

		if (!possibleSwitches.contains(switchSpecification))
		{
			if (!switchSpecification.endsWith(")"))
			{
				throw new IllegalArgumentException(
						"Incorrect format of switch:"
								+ " each switch must be followed by numbers in parenthesis specifying the required number"
								+ " of its values, e.g. \"--version(0), --file(1) --twomore(2-*) --any(*)\"");
			}

			possibleSwitches.add(switchSpecification);
		}
	}

	/**
	 * Removes a previously specified with a call to
	 * <code>addPossibleSwitch()</code> or <code>setPossibleSwitches()</code>
	 * switch from the set of required switches.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * removePossibleSwitch(&quot;--switch(0-*)&quot;);
	 * -or-
	 * removePossibleSwitch(&quot;--switch&quot;);
	 * </pre>
	 * 
	 * will remove the switch <code>--switch</code> from the set of possible
	 * command line switches.
	 * 
	 * @param switchSpecification
	 *            Name and cardinality of switch, e.g. --file(1), specifying the
	 *            --file switch which requires exactly one argument.
	 */
	protected void removePossibleSwitch(String switchSpecification)
	{
		forbidEmptyString(switchSpecification, "switchSpecification");
		switchSpecification = switchSpecification.trim();
		int firstBrace = switchSpecification.indexOf("(");
		boolean bracePresent = firstBrace != -1;
		String switchName = bracePresent ? switchSpecification.substring(0,
				firstBrace).trim() : switchSpecification;
		boolean found = false;

		if (bracePresent && !switchSpecification.endsWith(")"))
		{
			throw new IllegalArgumentException(
					"Incorrect format of switch:"
							+ " each switch must be followed by numbers in parenthesis specifying the required number"
							+ " of its values, e.g. \"--version(0), --file(1) --twomore(2-*) --any(*)\"");
		}

		for (String possibleSwitch : possibleSwitches)
		{
			int brace = possibleSwitch.indexOf("(");
			assert brace != -1;
			String name = possibleSwitch.substring(0, brace).trim();

			if (bracePresent && possibleSwitch.equals(switchSpecification)
					|| !bracePresent && switchName.equals(name))
			{
				possibleSwitches.remove(possibleSwitch);
				found = true;
				break;
			}
		}

		if (!found)
		{
			throw new IllegalArgumentException(
					"Switch "
							+ switchSpecification
							+ " is not found in the collection of possible switches. You must first call "
							+ " setPossibleSwitches() or setImplicitSwitch(), or check the spelling of the argument.");
		}
	}

	/**
	 * Creates a new Switch object for the specified switchName. Switch name
	 * must be one of possible switches (see
	 * {@link CommandLineParser#setPossibleSwitches}() method)
	 * 
	 * @param switchName
	 *            The name of switch, e.g. --file, or its specification, e.g.
	 *            --file(1).
	 * @return The newly created Switch object.
	 */
	public Switch createSwitch(String switchName)
	{
		boolean switchSpecificationFound = false;
		String name = null;
		int minValues = -1;
		int maxValues = -1;

		Set<String> switchCandidates = new LinkedHashSet<String>(
				possibleSwitches);

		if (implicitSwitch != null)
		{
			switchCandidates.add(implicitSwitch);
		}

		for (String switchSpecification : switchCandidates)
		{
			assert switchSpecification.endsWith(")");
			if (switchSpecification.startsWith(switchName))
			{
				boolean nameIsSpecification = switchName
						.equals(switchSpecification);
				int leftBrace = switchSpecification.lastIndexOf("(");
				int rightBrace = switchSpecification.lastIndexOf(")");
				forbid(leftBrace == -1,
						"Malformed switch specification: missing left brace.");
				forbid(rightBrace == -1,
						"Malformed switch specification: missing right brace.");
				forbid(leftBrace > rightBrace,
						"Malformed switch specification: left brace found after right brace.");
				forbid(leftBrace == rightBrace - 1,
						"Malformed switch specification: a number or an interval"
								+ " must be specified between left and right braces.");
				name = switchSpecification.substring(0, leftBrace);

				if (name.equals(switchName) || nameIsSpecification)
				{
					switchSpecificationFound = true;
					String valueInterval = switchSpecification.substring(
							leftBrace + 1, rightBrace);
					int dash = valueInterval.indexOf("-");

					if (dash == -1)
					{
						// Must be a * or a number
						if (valueInterval.equals("*"))
						{
							minValues = 0;
							maxValues = Integer.MAX_VALUE;
						}
						else
						{
							// One number is specified
							try
							{
								minValues = Integer.parseInt(valueInterval);
								maxValues = minValues;
							}
							catch (NumberFormatException e)
							{
								throw new IllegalArgumentException(
										"Malformed switch specification: a number or interval"
												+ " must be specified between left and right braces.");
							}
						}
					}
					else
					{
						// Two numbers or a combination number-* is specified
						String leftValue = valueInterval.substring(0, dash);
						String rightValue = valueInterval.substring(dash + 1);
						try
						{
							minValues = Integer.parseInt(leftValue);
						}
						catch (NumberFormatException e)
						{
							throw new IllegalArgumentException(
									"Malformed switch specification:"
											+ " a number must be specified to the left of dash.");
						}
						// Must be a * or a number
						if (rightValue.equals("*"))
						{
							maxValues = Integer.MAX_VALUE;
						}
						else
						{
							try
							{
								maxValues = Integer.parseInt(rightValue);
							}
							catch (NumberFormatException e)
							{
								throw new IllegalArgumentException(
										"Malformed switch specification:"
												+ " a number or a * must be specified to the right of dash.");
							}
						}
					}
				}

				if (switchSpecificationFound)
				{
					break;
				}
			}
		}

		forbid(!switchSpecificationFound,
				"Switch "
						+ switchName
						+ " is not found among the possible switches or implicit switch. "
						+ " Check if this switch is specified in a call to setPossibleSwitches() or setImplicitSwitch()");

		Switch newSwitch = new Switch(name, minValues, maxValues);
		return newSwitch;
	}

	/**
	 * Returns true if switch name looks like a switch, that is, starts with one
	 * of the prefixes specified by the <code>switchPrefixesMask</code>.
	 * 
	 * @param switchName
	 *            Name of switch.
	 * @return True if switch name starts with one of the prefixes specified by
	 *         the <code>switchPrefixesMask</code>.
	 */
	protected boolean looksLikeSwitch(String switchName)
	{
		boolean looksLikeSwitch = switchName.matches("^" + switchPrefixesMask
				+ ".*");
		return looksLikeSwitch;
	}

	/**
	 * Returns true if switch is among possible switches that are specified with
	 * a call to <code>setPossibleSwitches()</code> or
	 * <code>setImplicitSwitch()</code>.
	 * 
	 * @param switchName
	 *            Name of switch, e.g. --file
	 * @return True if switch is among possible switches.
	 */
	public boolean isPossibleSwitch(String switchName)
	{
		boolean result = false;

		for (String possibleSwitch : possibleSwitches)
		{
			int leftBrace = possibleSwitch.indexOf("(");
			forbid(leftBrace == -1, "Misformed possible switch element: "
					+ possibleSwitch);
			String name = possibleSwitch.substring(0, leftBrace);
			if (switchName.equals(name))
			{
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Removes switch cardinality (the numbers in braces in the end of switch
	 * specification) from switch specification. If cardinality is not present,
	 * returns the same string as the passed-in argument.
	 * <p>
	 * Example: <code>removeCardinality("--file(0-*)")</code> will return string
	 * "--file"
	 * 
	 * @param switchSpecification
	 *            Name and cardinality of switch, e.g. --file(0-*)
	 * @return Name of switch without cardinality, e.g. --file.
	 */
	protected String removeCardinality(String switchSpecification)
	{
		forbidNullString(switchSpecification, "switchSpecification");

		String result;

		int leftBrace = switchSpecification.indexOf("(");
		int rightBrace = switchSpecification.indexOf(")");

		if (leftBrace != -1)
		{
			forbid(rightBrace == -1,
					"Malformed switch specification: must have left and right braces");
			forbid(rightBrace < leftBrace,
					"Malformed switch specification: must have right brace after left brace");
			result = switchSpecification.substring(0, leftBrace);
		}
		else
		{
			forbid(rightBrace != -1,
					"Malformed switch specification: must have left and right braces or have neither");
			result = switchSpecification;
		}

		return result;
	}
}
