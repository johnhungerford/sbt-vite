{
  addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.15.0")
  val pluginVersion = System.getProperty("vite.plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("org.hungerford" % """sbt-vite""" % pluginVersion)
}
