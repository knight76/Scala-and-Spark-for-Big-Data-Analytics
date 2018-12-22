name := "Scala-and-Spark-for-Big-Data-Analytics"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-hive" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-yarn" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-network-shuffle" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming-flume" % "2.3.2"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1" classifier "models"
libraryDependencies += "com.holdenkarau" %% "spark-testing-base" % "2.3.1_0.10.0"