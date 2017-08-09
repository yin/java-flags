java-flags
==========

Light-weight command-line flags using an easy-to-use static binding approach. It provides annotations to describe
classes and flags and tools to generate program usage documentation from sources at runtime.

## Usage


To use a command-line flag value you need to create an accessor for it. This is done by `Flags.create()` method or
by one of the specific variants:

````java
class ReadmeExample {
    static final Flag<String> input = Flags.create("");
}
````

The `name` argument must match the flag name as specified on command-line (without '`--`' prepended), i.e.:

````bash
java ProgramRunner --input input.txt
````

In your program `main()` method pass the command-line arguments to `Flags`:

````java
public class ProgramRunner {
    public static main(String[] args) {
            Flags.parse(args, ImmutableList.of("com.github.yin.flags.example"));
        // ....
    }
}
````

To access the flag value, use the accessor `get()` method. This is typically done in the constructor, or
in a Guice Module `Provider`:

````java
public class ReadmeExample {
    static final Flag<String> input = Flags.create("");
    private final String inputfile;
    // ...

    ReadmeExample() {
	this.inputfile = input.get();
    }
}
````

### Generating command-line usage help

And finally, to attach some docs and generate the documentation, you just use the @FlagDesc annotation:

````java
@FlagDesc("This class is an example how to print files")
public class ReadmeExample {
    @FlagDesc("Specifies path to input file")
    static final Flag<String> input = Flags.create("");
    // ...
}
````

... and call the `printUsage()` method for you program package:

````java
@FlagDesc("This class is an example how to print files")
public class ReadmeExample {
    // ...
    public static void main(String[] args) {
        try {
            Flags.parse(args, ImmutableList.of("com.github.yin.flags.example"));
        } catch (Flags.ParseException e) {
            System.err.println(e.getMessage());
            Flags.printUsage("com.github.yin.flags.example");
            System.exit(1);
            return;
        }
        // TODO yin: Default value is not validated, implement required flags
        if (input.get().isEmpty()) {
            System.err.println("--input is empty");
            Flags.printUsage("com.github.yin.flags.example");
            System.exit(1);
            return;
        }
        ReadmeExample re = new ReadmeExample();
        re.run();
    }
    // ...
}
````

### Validators

````java
public class ProgramRunner {
    static final Flag<String> input = Flags.create("")
            .validator((String path) -> {
                if (path == null || path.isEmpty()) {
                    throw new Flags.ParseException("Input path is empty");
                } else if (!Files.isRegularFile(Paths.get(path))) {
                    throw new Flags.ParseException("Input path is not regular file");
                } else if (!Files.isReadable(Paths.get(path))) {
                    throw new Flags.ParseException("Input path is not readable");
                }
            });
}
````


### Installation

Just grab the package from Maven Central:

````xml
<dependency>
    <groupdId>com.github.yin.flags</groupId>
    <artifactId>java-flags</artifactId>
    <version>0.3.0-beta1</version>
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
