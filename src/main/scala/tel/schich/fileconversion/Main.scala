package tel.schich.fileconversion

import java.nio.file._
import java.util.regex.Pattern.{CASE_INSENSITIVE, compile}

import sun.misc.{Signal, SignalHandler}

/**
  * Created by Alex on 24.09.2016.
  */
object Main extends App {

    val path = Paths.get(if (args.isEmpty) "." else args(0))
    val targetPath = path.resolve("source")
    if (!Files.exists(targetPath)) {
        Files.createDirectory(targetPath)
    }

    val rules: Seq[ProcessingRule] = Seq(
        Rule.any(new FileCopier(targetPath)),
        Rule.any(new NameNormalizer(_.toLowerCase(), compile("\\s+").matcher(_).replaceAll("_"))),
        Rule(compile("\\.svg$", CASE_INSENSITIVE), new InkscapeConverter("png", deleteOriginal = false)),
        Rule(compile("\\.svg$", CASE_INSENSITIVE), new InkscapeConverter("pdf"))
    )

    val monitor = new FolderMonitor(path, rules)
    val t = new Thread(monitor)
    t.start()

    Signal.handle(new Signal("INT"), new SignalHandler {
        override def handle(signal: Signal): Unit = {
            monitor.stop()
        }
    })
}
