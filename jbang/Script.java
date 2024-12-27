///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.tersesystems.echopraxia:simple:4.0.0
//DEPS com.tersesystems.echopraxia:logstash:4.0.0
//DEPS com.tersesystems.echopraxia:scripting:4.0.0
//DEPS ch.qos.logback:logback-classic:1.5.15
//DEPS net.logstash.logback:logstash-logback-encoder:8.0

import echopraxia.api.*;
import echopraxia.logging.api.*;
import echopraxia.scripting.*;
import echopraxia.simple.*;

import java.nio.file.*;

import ch.qos.logback.classic.LoggerContext;

public class Script {
    private static Logger logger;

    private static final String defaultScript = """
        import * as std from "std";
        alias std.strings as str;
        
        library echopraxia {
          function evaluate: (string level, dict ctx) ->
            let {
              find_string: ctx[:find_string];
            }
            str.lower_case(find_string("$.foo")) == "bar";   
        }        
        """;

    public static void main(String... args) throws java.io.IOException {
        Path watchedDir = Paths.get(".");
        ScriptWatchService watchService = new ScriptWatchService(watchedDir);

        try (watchService) {
            Path filePath = watchedDir.resolve("tweakflow.tf");

            if (! Files.exists(filePath)) {
                Files.writeString(filePath, defaultScript);
            }
    
            ScriptHandle watchedHandle = watchService.watchScript(filePath, e -> System.err.println("Script compilation error " + e.toString()));
            Condition condition = ScriptCondition.create(watchedHandle);
    
            logger = LoggerFactory.getLogger(Script.class).withCondition(condition);
        
            // Set log levels as necessary
            var loggerContext = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
            var rootLogger = loggerContext.getLogger("ROOT");
            rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);
            var thisLogger = loggerContext.getLogger(Script.class);
            thisLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
            
            var fb = FieldBuilder.instance();
            logger.info("{}", fb.string("foo", "BAR"));
        }
    }
}
