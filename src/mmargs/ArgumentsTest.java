//- Copyright © 2010 Micah Martin. All Rights Reserved.
//- mmargs and all included source files are distributed under terms of the MIT License.

package mmargs;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArgumentsTest
{
  private Arguments args;
  private Map<String, Object> results;

  @Before
  public void setUp() throws Exception
  {
    args = new Arguments();
  }

  @Test
  public void parsingNothing() throws Exception
  {
    results = args.parse();

    assertEquals(0, results.size());
  }

  @Test
  public void unexpectedParameter() throws Exception
  {
    checkParseError("Unexpected parameter: foo", "foo");
    checkParseError("Unexpected parameter: bar", "bar");
  }

  @Test
  public void parsingOneParameter() throws Exception
  {
    args.addParameter("foo", "Some Description");

    results = args.parse("bar");
    assertEquals("bar", results.get("foo"));

    results = args.parse("fizz");
    assertEquals("fizz", results.get("foo"));
  }

  @Test
  public void missingParameter() throws Exception
  {
    args.addParameter("foo", "Some Description");
    checkParseError("Missing parameter: foo");
  }

  @Test
  public void parsingTwoParameters() throws Exception
  {
    args.addParameter("foo", "Some Description");
    args.addParameter("bar", "Some Description");

    results = args.parse("fizz", "bang");

    assertEquals("fizz", results.get("foo"));
    assertEquals("bang", results.get("bar"));
  }

  @Test
  public void missingOneOfTwoParameters() throws Exception
  {
    args.addParameter("foo", "Some Description");
    args.addParameter("bar", "Some Description");

    checkParseError("Missing parameter: foo");
    checkParseError("Missing parameter: bar", "fizz");
  }

  @Test
  public void optionalParameter() throws Exception
  {
    args.addOptionalParameter("foo", "Some Description");

    results = args.parse();
    assertEquals(null, results.get("foo"));
    assertEquals(null, results.get("*errors"));

    results = args.parse("fizz");
    assertEquals("fizz", results.get("foo"));
    assertEquals(null, results.get("*errors"));
  }

  @Test
  public void oneSwitchOption() throws Exception
  {
    args.addSwitchOption("m", "my-option", "my test option");

    results = args.parse();
    assertEquals(false, results.containsKey("my-option"));

    results = args.parse("-m");
    assertEquals("on", results.get("my-option"));

    results = args.parse("--my-option");
    assertEquals("on", results.get("my-option"));
  }

  @Test
  public void twoSwitchOptions() throws Exception
  {
    args.addSwitchOption("a", "a-option", "Option A");
    args.addSwitchOption("b", "b-option", "Option B");

    results = args.parse();
    assertEquals(false, results.containsKey("a-option"));
    assertEquals(false, results.containsKey("b-option"));

    results = args.parse("-a");
    assertEquals(true, results.containsKey("a-option"));
    assertEquals(false, results.containsKey("b-option"));

    results = args.parse("--b-option");
    assertEquals(false, results.containsKey("a-option"));
    assertEquals(true, results.containsKey("b-option"));

    results = args.parse("--a-option", "-b");
    assertEquals(true, results.containsKey("a-option"));
    assertEquals(true, results.containsKey("b-option"));
  }

  @Test
  public void optionNamesAreRequired() throws Exception
  {
    checkOptionError("Options require a shortName and fullName", "a", null, null);
    checkOptionError("Options require a shortName and fullName", null, "a-option", null);
    args.addSwitchOption("a", "a-option", null);
  }

  @Test
  public void unrecognizedOption() throws Exception
  {
    checkParseError("Unrecognized option: -a", "-a");
    checkParseError("Unrecognized option: --a-option", "--a-option");
  }

  @Test
  public void oneValueOption() throws Exception
  {
    args.addValueOption("a", "a-option", "value", "Option A");

    results = args.parse("-a", "value");
    assertEquals("value", results.get("a-option"));

    results = args.parse("--a-option=value");
    assertEquals("value", results.get("a-option"));
  }

  @Test
  public void missingOptionValue() throws Exception
  {
    args.addValueOption("a", "a-option", "value", "Option A");

    checkParseError("Missing value for option: a", "-a");
    checkParseError("Missing value for option: a-option", "--a-option");
  }

  @Test
  public void missingOptionValueWhenFollowedByOption() throws Exception
  {
    args.addValueOption("a", "a-option", "value", "Option A");
    args.addSwitchOption("b", "b-option", "Option B");

    checkParseError("Missing value for option: a", "-a", "-b");
    checkParseError("Missing value for option: a", "-a", "--b-option");
  }

  @Test
  public void parameterWithSwitchOption() throws Exception
  {
    args.addParameter("param", "Some Description");
    args.addSwitchOption("a", "a-option", "Option A");

    checkParseError("Missing parameter: param");
    checkParseError("Missing parameter: param", "-a");
    checkParseError("Missing parameter: param", "--a-option");

    results = args.parse("-a", "blah");
    assertEquals("on", results.get("a-option"));
    assertEquals("blah", results.get("param"));

    results = args.parse("--a-option", "blah");
    assertEquals("on", results.get("a-option"));
    assertEquals("blah", results.get("param"));
  }

  @Test
  public void parameterWithValueOption() throws Exception
  {
    args.addParameter("param", "Some Description");
    args.addValueOption("a", "a-option", "value", "Option A");

    checkParseError("Missing parameter: param");
    checkParseError("Missing value for option: a", "-a");
    checkParseError("Missing parameter: param", "-a", "foo");
    checkParseError("Missing parameter: param", "--a-option=foo");

    results = args.parse("-a", "foo", "bar");
    assertEquals("foo", results.get("a-option"));
    assertEquals("bar", results.get("param"));

    results = args.parse("--a-option=foo", "bar");
    assertEquals("foo", results.get("a-option"));
    assertEquals("bar", results.get("param"));
  }

  @Test
  public void parameterOptionsAreParsableInLongFormWithoutEqualsSign() throws Exception
  {
    args.addParameter("param", "Some Description");
    args.addValueOption("a", "a-option", "value", "Option A");

    results = args.parse("--a-option", "foo", "bar");
    assertEquals("foo", results.get("a-option"));
    assertEquals("bar", results.get("param"));
  }

  @Test
  public void remainingArgs() throws Exception
  {
    results = args.parse("foo");
    assertArrayEquals(new String[] {"foo"}, ((List)results.get("*leftover")).toArray());

    args.addParameter("param", "Some Description");
    results = args.parse("foo", "bar");
    assertArrayEquals(new String[] {"bar"}, ((List)results.get("*leftover")).toArray());

    args.addSwitchOption("a", "a-option", "Option A");
    results = args.parse("-a", "foo", "bar");
    assertArrayEquals(new String[] {"bar"}, ((List)results.get("*leftover")).toArray());

    results = args.parse("-z", "foo", "bar");
    assertArrayEquals(new String[] {"-z", "bar"}, ((List)results.get("*leftover")).toArray());
  }

  @Test
  public void remainingArgsWithValueOption() throws Exception
  {
    args.addParameter("param", "Some Description");
    args.addValueOption("a", "a-option", "value", "Option A");

    results = args.parse("-z");
    assertArrayEquals(new String[] {"-z"}, ((List)results.get("*leftover")).toArray());

    results = args.parse("-z", "foo", "bar");
    assertArrayEquals(new String[] {"-z", "bar"}, ((List)results.get("*leftover")).toArray());

    results = args.parse("-a", "foo", "bar", "fizz");
    assertArrayEquals(new String[] {"fizz"}, ((List)results.get("*leftover")).toArray());
  }

  @Test
  public void canParseOptionsMixedInWithParameters() throws Exception
  {
    args.addParameter("param1", "Some Description");
    args.addParameter("param2", "Some Description");
    args.addSwitchOption("a", "a-switch", "Switch A");
    args.addValueOption("b", "b-option", "B", "Option B");
    args.addValueOption("c", "c-option", "C", "Option C");

    results = args.parse("-a", "one", "--b-option=two", "three", "--c-option", "four", "five");

    assertEquals("on", results.get("a-switch"));
    assertEquals("one", results.get("param1"));
    assertEquals("two", results.get("b-option"));
    assertEquals("three", results.get("param2"));
    assertEquals("four", results.get("c-option"));
  }

  @Test
  public void multiParameters() throws Exception
  {
    args.addMultiParameter("colors", "Any number of colors");

    results = args.parse("red", "orange", "yellow");
    assertEquals(null, results.get("*errors"));
    assertListsEquals(list("red", "orange", "yellow"), results.get("colors"));

    results = args.parse();
    assertEquals(null, results.get("*errors"));
    assertEquals(null, results.get("colors"));

    results = args.parse("red");
    assertEquals(null, results.get("*errors"));
    assertListsEquals(list("red"), results.get("colors"));
  }

  @Test
  public void multiOptions() throws Exception
  {
    args.addMultiOption("c", "color", "COLOR", "Some colors");

    results = args.parse();
    assertEquals(null, results.get("*errors"));
    assertEquals(null, results.get("color"));

    results = args.parse("-c", "red");
    assertEquals(null, results.get("*errors"));
    assertListsEquals(list("red"), results.get("color"));

    results = args.parse("-c", "red", "--color", "orange", "--color=yellow");
    assertEquals(null, results.get("*errors"));
    assertListsEquals(list("red", "orange", "yellow"), results.get("color"));
  }

  @Test
  public void argString() throws Exception
  {
    assertEquals("", args.argString());

    args.addParameter("param", "Some Description");
    assertEquals("<param>", args.argString());

    args.addSwitchOption("a", "a-option", "Option A");
    assertEquals("[options] <param>", args.argString());

    args.addParameter("another-param", "Some Description");
    assertEquals("[options] <param> <another-param>", args.argString());
  }

  @Test
  public void argStringWithOptionalParameter() throws Exception
  {
    args.addOptionalParameter("param", "Some Description");
    assertEquals("[param]", args.argString());
  }

  @Test
  public void parametersString() throws Exception
  {
    assertEquals("", args.parametersString());

    args.addParameter("foo", "Foo Param");
    assertEquals("  foo  Foo Param\n", args.parametersString());

    args.addParameter("fizz", "Fizz Param");
    assertEquals("  foo   Foo Param\n" +
                 "  fizz  Fizz Param\n", args.parametersString());
  }

  @Test
  public void optionsString() throws Exception
  {
    assertEquals("", args.optionsString());

    args.addSwitchOption("a", "a-option", "Option A");
    assertEquals("  -a, --a-option  Option A\n", args.optionsString());

    args.addValueOption("b", "b-option", "value", "Option B");
    final String expected = "  -a, --a-option          Option A\n" +
                            "  -b, --b-option=<value>  Option B\n";

    assertEquals(expected, args.optionsString());
  }

  @Test
  public void multilineOptionsAreAlignedProperly() throws Exception
  {
    args.addSwitchOption("a", "a-option", "Option A");

    args.addValueOption("b", "b-option", "value", "Option B\nmore info on b option");
    final String expected = "  -a, --a-option          Option A\n" +
                            "  -b, --b-option=<value>  Option B\n" +
                            "                          more info on b option\n";

    assertEquals(expected, args.optionsString());
  }

  @Test
  public void longOptionDescriptionsAreSplitIntoMultipleLines() throws Exception
  {
    args.addSwitchOption("a", "a-option", "Option A");

    args.addValueOption("b", "b-option", "value", "Option B which has a really long description that should be cutoff at 72 chars.");
    final String expected = "  -a, --a-option          Option A\n" +
                            "  -b, --b-option=<value>  Option B which has a really long description that should be cutoff at 72\n" +
                            "                          chars.\n";

    assertEquals(expected, args.optionsString());
  }

  @Test
  public void extraNewlinesArePreservedInOptionsString() throws Exception
  {
    args.addSwitchOption("a", "a-option", "Option A");

    args.addValueOption("b", "b-option", "value", "Option B\n\nThat's it");
    final String expected = "  -a, --a-option          Option A\n" +
                            "  -b, --b-option=<value>  Option B\n" +
                            "                          \n" +
                            "                          That's it\n";

    assertEquals(expected, args.optionsString());
  }

  private void checkOptionError(String message, String shortName, String fullName, String description)
  {
    try
    {
      args.addSwitchOption(shortName, fullName, description);
      fail("should throw exception");
    }
    catch(RuntimeException e)
    {
      assertEquals(message, e.getMessage());
    }
  }

  private void checkParseError(String message, String... arguments)
  {
    results = args.parse(arguments);
    final List errors = (List)results.get("*errors");
    assertNotNull(errors);
    String joinedErrors = "";
    for(Object error : errors)
      joinedErrors += (error.toString() + ", ");
    assertEquals(joinedErrors, true, errors.contains(message));
  }
  
  private void assertListsEquals(List expected, Object actualObject)
  {
    List actual = (List)actualObject;
    assertEquals(expected.size(), actual.size());
    for(int i = 0; i < expected.size(); i++)
      assertEquals("list mismatch at index: " + i, expected.get(i), actual.get(i));
  }

  private List list(String... values)
  {
    return Arrays.asList(values);
  }
}
