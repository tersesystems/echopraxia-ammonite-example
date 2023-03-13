///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.tersesystems.echopraxia:logger:2.2.4
//DEPS com.tersesystems.echopraxia:logstash:2.2.4
//DEPS com.tersesystems.echopraxia:scripting:2.2.4
//DEPS com.tersesystems.logback:logback-classic:1.2.0

import com.tersesystems.echopraxia.*;
import com.tersesystems.echopraxia.api.*;
import com.tersesystems.echopraxia.scripting.*;
import com.tersesystems.logback.classic.ChangeLogLevel;

import java.nio.*;
import java.nio.file.*;

public class Script {
    private static final Logger<?> logger = LoggerFactory.getLogger(Script.class);

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
        ChangeLogLevel changer = new ChangeLogLevel();
        changer.changeLogLevel("ROOT", "INFO");
        changer.changeLogLevel(logger.getName(), "DEBUG");

        Path watchedDir = Paths.get(".");
        ScriptWatchService watchService = new ScriptWatchService(watchedDir);
        Path filePath = watchedDir.resolve("tweakflow.tf");

        if (! Files.exists(filePath)) {
            Files.writeString(filePath, defaultScript);
        }

        ScriptHandle watchedHandle = watchService.watchScript(filePath, e -> logger.error("Script compilation error", e));
        Condition condition = ScriptCondition.create(watchedHandle);

        logger.info(condition, "{}", fb -> fb.string("foo", "BAR"));
    }
}
