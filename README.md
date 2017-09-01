# CommandLineParser

`CommandLineParser` class takes command line arguments and parses them into a map of [switch, list of values] pairs. A switch on the command line can have no, one, or multiple values. The command line can also have arguments that are not related to any switches.

Usage:
```
   public static void main(String[] arguments)
   {
   	CommandLineParser commandLineParser = new CommandLineParser();
   	commandLineParser.setPossibleSwitches("--version(0) --usage(0)"
   			+ " --help(0) --multi(*)");
   	commandLineParser.setEmptyCommandLineSwitch("--usage");
   	commandLineParser.setImplicitSwitch("--file(1)");
   	commandLineParser.setArguments(arguments);
   	// ...same as commandLineParser.parse(arguments);
   
   	if (commandLineParser.isUsageRequested())
   	{
   		System.out.println(usageText);
   	}
   	else if (commandLineParser.isVersionRequested())
   	{
   		System.out.println(VERSION_STRING);
   	}
   	else if (commandLineParser.isHelpRequested())
   	{
   		System.out.println(helpText);
   	}
   	else if (commandLineParser.isSwitchPresent("--multi"))
   	{
   		// Process custom switch
   		List<String> switchValues = commandLineParser
   				.getSwitchValues("--multi");
   		String[] arguments = commandLineParser.getSwitchlessArguments();
   		// ...
   	}
   }
``` 
