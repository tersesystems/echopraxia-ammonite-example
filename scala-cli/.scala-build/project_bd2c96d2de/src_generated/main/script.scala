



object script {
/*<script>*///> using scala "2.13.8"

//> using dep "com.tersesystems.echopraxia.plusscala::logger:1.1.2"
//> using dep "com.tersesystems.echopraxia:scripting:2.3.0-RC1"
//> using dep "com.tersesystems.echopraxia:logstash:2.3.0-RC1"
//> using dep "com.tersesystems.logback:logback-classic:1.2.0"
//> using dep "com.lihaoyi::os-lib:0.9.1"

import com.tersesystems.echopraxia.plusscala._
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.scripting._
import com.tersesystems.logback.classic.ChangeLogLevel

case class ScriptService(dir: os.Path) {
  private val sws = new ScriptWatchService(dir.toNIO);
  
  def condition(path: os.Path) = {
    val scriptHandle = sws.watchScript(path.toNIO, _.printStackTrace)
    ScriptCondition.create(scriptHandle).asScala
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
  val logger = LoggerFactory.getLogger
  changer.changeLogLevel(logger.name, "DEBUG")

  // Ensure a script exists and is watched
  val dir = os.pwd
  val service = ScriptService(dir)
  val tweakflowFile = dir / "tweakflow.tf"
  if (! os.isFile(tweakflowFile)) {
    os.write(tweakflowFile, TweakFlow.default)
  }

  // now we're sure the file exists, set up a condition and run in a loop.
  val condition = service.condition(tweakflowFile)
  while (true) {
    try {
      logger.debug(condition, "{}", fb => fb.keyValue("foo" -> "bar"));
    } finally {
      Thread.sleep(2000L);
    }
  }
}

main()/*</script>*/ /*<generated>*/
def args = script_sc.args$
  /*</generated>*/
}

object script_sc {
  private var args$opt0 = Option.empty[Array[String]]
  def args$set(args: Array[String]): Unit = {
    args$opt0 = Some(args)
  }
  def args$opt: Option[Array[String]] = args$opt0
  def args$: Array[String] = args$opt.getOrElse {
    sys.error("No arguments passed to this script")
  }
  def main(args: Array[String]): Unit = {
    args$set(args)
    script.hashCode() // hasCode to clear scalac warning about pure expression in statement position
  }
}

