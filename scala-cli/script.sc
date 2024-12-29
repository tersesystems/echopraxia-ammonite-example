//> using scala "3.6.2"

//> using dep "com.tersesystems.echopraxia.plusscala::simple::2.0.0"
//> using dep "com.tersesystems.echopraxia:scripting:4.0.0"
//> using dep "com.tersesystems.echopraxia:logstash:4.0.0"
//> using dep "com.tersesystems.logback:logback-classic:1.2.0"
//> using dep "ch.qos.logback:logback-classic:1.5.15"
//> using dep "net.logstash.logback:logstash-logback-encoder:8.0"

//> using dep "com.lihaoyi::os-lib:0.11.3"

import echopraxia.plusscala.simple._
import echopraxia.plusscala.api._
import echopraxia.plusscala.logging.api._
import echopraxia.scripting._
import com.tersesystems.logback.classic.ChangeLogLevel

case class ScriptService(dir: os.Path) {
  private val sws = new ScriptWatchService(dir.toNIO);
  
  def condition(path: os.Path) = {
    val scriptHandle = sws.watchScript(path.toNIO, _.printStackTrace)
    ScriptCondition.create(scriptHandle)
  }
}

object TweakFlow {
  val default = """
    |library echopraxia {
    |  function evaluate: (string level, dict ctx) ->
    |    let {
    |      find_string: ctx[:find_string];
    |    }
    |    find_string("$.foo") == "bar";
    |}  
  """.stripMargin
}

def main() = {
  // No logback.xml, we're doing it live       
  val changer = new ChangeLogLevel
  changer.changeLogLevel("ROOT", "INFO")

  // Ensure a script exists and is watched
  val dir = os.pwd
  val service = ScriptService(dir)
  val tweakflowFile = dir / "tweakflow.tf"
  if (! os.isFile(tweakflowFile)) {
    os.write(tweakflowFile, TweakFlow.default)
  }

  // now we're sure the file exists, set up a condition and run in a loop.
  val condition = service.condition(tweakflowFile).asScala
  val logger = LoggerFactory.getLogger().withCondition(condition)
  changer.changeLogLevel(logger.core.getName, "DEBUG")

  val fb = FieldBuilder
  while (true) {
    try {
      logger.debug("{}", fb.keyValue("foo" -> "bar"));
    } finally {
      Thread.sleep(2000L);
    }
  }
}

main()
