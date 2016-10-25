Java-Flags
==========

Light-weight command-line flags using an easy-to-use static binding approach. It provides annotations to describe
classes and flags and tools to geneerate program usage documentation from sources at runtime.

## Usage


To use a command-line flag value you need to create an accessor for it. This is done by `Flags.create()` method or
by one of the specific variants:

````java
class ProgramModule {
    static final Flag<String> arg_inputFile = Flags.string("inputFile");
}
````

The `name` argument must match the flag name as specified on command-line (without '`--`' prepended), i.e.:

````shell
java ProgramRunner --inputFile input.txt
````

In your program `main()` method pass the command-line arguments to `Flags`:

````java
public static main(String[] args) {
    Flags.init(args);
    ....
````

To access the flag value, use the accessor `get()` method. Type checking and type conversion is
done for you automatically (but currently I support only `String`s, no type conversion, fu2).
This is typically done in the constructor, or in a Guice Module `Provider`:

````java
class ProgramModule {
    ...
    final String inputFile;
    ProgramModule() {
        this.inputfile = arg_inputFile.get();
    }
}
````

### Generating command-line usage help

And finally, to attach some docs and generate the documentation, you just use the @FlagDesc annotation:

````java
@FlagDesc("This class uses java-flags")
public static class TestFlagDesc {
    @FlagDesc("This is a java-flag accessor")
    private static final Flag<String> arg_inputFile = Flags.string("inputFile");
}
````

... and call the `printUsage()` method for you program package:

````java
if (...) {
    Flags.printUsage("com.github.yin.flags.example");
}
````


### Installation

Just grab the package from Maven Central:

````xml
<dependency>
    <groupdId>com.github.yin.flags</groupId>
    <artifactId>java-flags</artifactId>
    <version>0.1.1</version>
</dependency>
````

## Building

Easy, use this GitHub repository and Maven:

````shell
git clone https://github.com/yin/java-flags.git java-flags
cd !$
mvn install
````

## Contributing

Fork this GitHub repository (or ask me to make BitBucket mirror public) and prepare your changes into a Pull Request.

Make sure yo do:

1. Create tests all relevant changes and any fixed bugs!
2. Run all tests to make sure they are not broken.
3. Add your name and GitHub ID to CONTRIBUTORS before submitting your Pull Request.
4. Check README.md for up-to-dateness (my favorite ness-ness)
5. No failing or broken tests ar accepted in Pull Requests and may result in execssive trolling in the Code Review.

## LICENSE

MIT License, 2016 Matej 'Yin' Gagyi

-- enjoy, Matej
