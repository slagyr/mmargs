MMARGS
================================================
A command line arguments parser for the JVM.  Written in Java means it's fast, yet it'll work with any JVM language such as JRuby, Clojure, Scala, Mirah, etc...

Download
================================================
https://github.com/downloads/slagyr/mmargs/mmargs-1.2.0.jar

Example Usage
================================================
Below is a simple example using all of the API methods for this library.

	import mmargs.Arguments;
	import java.util.List;
	import java.util.Map;

	public class Greetings
	{
	  public static void main(String[] args)
	  {
	    Arguments spec = new Arguments();
	    spec.addParameter("first-name", "The person's first name");
	    spec.addOptionalParameter("title", "The person's title");
	    spec.addMultiParameter("remaining-names", "The person's remaining names");
	    spec.addSwitchOption("C", "caps", "Prints the name in all uppercase letters");
	    spec.addValueOption("s", "salutation", "SALUTATION", "A salutation to be printed before the name");
	    spec.addMultiOption("b", "body", "BODY", "Specifies parts of the message body");

	    final Map<String,Object> options = spec.parse(args);

	    if(options.containsKey("*errors"))
	      usage(spec, (List) options.get("*errors"));
	    else
	    {
	      String greeting = options.get("first-name").toString();
	      if(options.containsKey("title"))
	        greeting = options.get("title").toString() + " " + greeting;
	      if(options.containsKey("remaining-names"))
	        for(Object name : (List)options.get("remaining-names"))
	          greeting += " " + name.toString();

	      if(options.containsKey("caps"))
	        greeting = greeting.toUpperCase();

	      if(options.containsKey("salutation"))
	        greeting = options.get("salutation") + " " + greeting;

	      System.out.println(greeting + ",");

	      if(options.containsKey("body"))
	        for(Object body : (List)options.get("body"))
	          System.out.println(body);
	    }
	  }

	  private static void usage(Arguments spec, List list)
	  {
	    for(Object error : list)
	      System.out.println(error);
	    System.out.println("Usage: java Greetings " + spec.argString());
	    System.out.println(spec.parametersString());
	    System.out.println(spec.optionsString());
	    System.exit(-1);
	  }
	}

Creating the Spec
================================================
To specify required parameters, provide a name and description.  If not provided an error is reported.

	spec.addParameter("first-name", "The person's first name");
	
If a parameter is optional....	
	
	spec.addOptionalParameter("title", "The person's title");
	
Sometimes it's handy to have a catch all for parameters.  A multi-parameter should be that last parameter added to the spec.	

	spec.addMultiParameter("remaining-names", "The person's remaining names");
	
Switch options result in a value of "on" if provided and null if not.

	spec.addSwitchOption("C", "caps", "Prints the name in all uppercase letters");
	
Value options map to the provided value or null if none is provided.	

	spec.addValueOption("s", "salutation", "SALUTATION", "A salutation to be printed before the name");
	
Multi-options are collected into a <code>List</code>.	

	spec.addMultiOption("b", "body", "BODY", "Specifies parts of the message body");

Parsing
================================================
When <code>parse</code> is called, all the arguments are parsed and a map of values it returned.  All of the  supplied params and options are strings keyed by their names.  For multi-parameters, and multi-options, the key maps to a <code>List</code> of strings.  

There are two special keys that you may find in the resulting map:

1. <code>\*errors  </code>: In the event of errors during parsing, the <code>*errors</code> maps to a <code>List</code> of error strings.
2. <code>*leftover</code>: Any extra parameters or unrecognized options will be collected in a <code>List</code> of strings.
	
Usage Message
================================================	
Due to the many various way that a usage message could be printed, mmargs provides 3 methods to give you some flexibility.

1. <code>argString        </code>: describes the general structure of the command
2. <code>parametersString </code>: describes each of the parameters
3. <code>optionsString    </code>: describes each of the options

Here's the out put of a parse error in our <code>Greetings</code> example:

	Usage: java Greetings [options] <title> [first-name] [remaining-names*]
	  title            The person's title
	  first-name       The person's first name
	  remaining-names  The person's remaining names

	  -C, --caps                     Prints the name in all uppercase letters
	  -s, --salutation=<SALUTATION>  A salutation to be printed before the name
	  -b, --body=<BODY>              Specifies parts of the message body


Trying It Out
================================================
Minimal example

	$ java -cp classes Greetings Yoda
	Yoda,

No optional parameters and using the short names of:

	$ java -cp classes Greetings Dave -C -s "I'm sorry" -b "I'm afraid I can't do that."
	I'm sorry DAVE,
	I'm afraid I can't do that.

Using two long names for options and taking advantage of multi option:

	$ java -cp classes Greetings Bond Mr. --caps --salutation="No," --body="I expect you to die." --body="*evil grin*"
	No, MR. BOND,
	I expect you to die.
	*evil grin*	
	
Using multi parameter

	$ java -cp classes Greetings John Mr. Jacob Jingleheimer Schmidt --body "His name is my name too"
	Mr. John Jacob Jingleheimer Schmidt,
	His name is my name too
	



