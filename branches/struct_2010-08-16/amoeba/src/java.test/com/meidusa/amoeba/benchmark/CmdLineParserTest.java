package com.meidusa.amoeba.benchmark;

import com.meidusa.amoeba.util.CmdLineParser;
import com.meidusa.amoeba.util.OptionType;

/**
 * This example shows how to dynamically create basic output for a --help option. 
 */
public class CmdLineParserTest {

    public static void main( String[] args ) {
    	CmdLineParser parser = new CmdLineParser("test");
    	CmdLineParser.Option<Boolean> verbose = parser.addOption(OptionType.Boolean,'v', "verbose",true,"Print extra information.\r\nother informations");
        CmdLineParser.Option size = parser.addOption(OptionType.Int,'s', "size",true,"The extent of the thing");
        CmdLineParser.Option name = parser.addOption(OptionType.String,'n', "name",true,"Name given to the widget");
        CmdLineParser.Option fraction = parser.addOption(OptionType.Double,'f', "fraction",true,"What percentage should be discarded");
        CmdLineParser.Option help = parser.addOption(OptionType.Boolean,'?', "help",true,"Show this help message");
        try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
            parser.printUsage();
            System.exit(2);
        }

        if ( Boolean.TRUE.equals(parser.getOptionValue(help))) {
            parser.printUsage();
            System.exit(0);
        }

        // Extract the values entered for the various options -- if the
        // options were not specified, the corresponding values will be
        // null.
        Boolean verboseValue = (Boolean)parser.getOptionValue(verbose);
        Integer sizeValue = (Integer)parser.getOptionValue(size);
        String nameValue = (String)parser.getOptionValue(name);
        Double fractionValue = (Double)parser.getOptionValue(fraction);

        // For testing purposes, we just print out the option values
        System.out.println("verbose: " + verboseValue);
        System.out.println("size: " + sizeValue);
        System.out.println("name: " + nameValue);
        System.out.println("fraction: " + fractionValue);

        // Extract the trailing command-line arguments ('a_nother') in the
        // usage string above.
        String[] otherArgs = parser.getRemainingArgs();
        System.out.println("remaining args: ");
        for ( int i = 0; i < otherArgs.length; ++i ) {
            System.out.println(otherArgs[i]);
        }

        // In a real program, one would pass the option values and other
        // arguments to a function that does something more useful.

        System.exit(0);
    }

}
