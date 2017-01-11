Java-Flags
==========

Light-weight command-line flags using an easy-to-use static binding approach. It provides annotations to describe
classes and flags and tools to generate program usage documentation from sources at runtime.

## Usage


To use a command-line flag value you need to create an accessor for it. This is done by `Flags.create()` method or
by one of the specific variants:

````java
class ProgramModule {
    static final Flag<String> arg_inputFile = Flags.string("inputFile");
}
````

The `name` argument must match the flag name as specified on command-line (without '`--`' prepended), i.e.:

````bash
java ProgramRunner --inputFile input.txt
````

In your program `main()` method pass the command-line arguments to `Flags`:

````java
public class ProgramRunner {
    public static main(String[] args) {
        Flags.init(args);
        // ....
    }
}
````

To access the flag value, use the accessor `get()` method. This is typically done in the constructor, or
in a Guice Module `Provider`:

````java
class ProgramModule {
    // ...
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
public static class ProgramModule {
    @FlagDesc("This is a java-flag accessor")
    private static final Flag<String> arg_inputFile = Flags.string("inputFile");
}
````

... and call the `printUsage()` method for you program package:

````java
public class ProgramRunner {
    public static main(String[] args) {
        if (!check()) {
            Flags.printUsage("com.github.yin.flags.example");
        }
    }
}
````

### Type conversions

String value of a flag is converted by `Flag.get()` into the target type `T` by calling a
`TypeConversion.Conversion<T>` function. Default conversions for all primitive types,
BigInteger and BigDecimal will work out of the box.

If String value can not be parsed an exception is always thrown when calling `Flag.get()`.
Clients can register their own type conversion:

````java
public class ProgramRunner {
    public static main(String[] args) {
        Flags.typeConversion().register(URL.class, new Conversion<BigDecimal>() {
            @Override public URL apply(String s) {
                return new URL(s);
            }
        });
    }
}
````

All conversion are global, but this will change in the future and conversion will be scoped only.

NOTE: This mechanism is Java 6 compatible, so lambdas won't work here, at least for now.

### Installation

Just grab the package from Maven Central:

````xml
<dependency>
    <groupdId>com.github.yin.flags</groupId>
    <artifactId>java-flags</artifactId>
    <version>0.2</version>
</dependency>
````

## Building

Easy, use this GitHub repository and Maven:

````bash
git clone https://github.com/yin/java-flags.git java-flags
cd !$
mvn install
````

## License

MIT License, (C) 2016-2017 Matej 'Yin' Gagyi
