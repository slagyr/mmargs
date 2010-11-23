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
