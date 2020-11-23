name := """backend-exercise"""
organization := "com.example"

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

val akkaManagementVersion = "1.0.0"
val akkaVersion = "2.6.0"
val akkaHTTPVersion = "10.1.10"

routesGenerator := InjectedRoutesGenerator

PlayKeys.devSettings +="config.resource" -> "development.conf"

scalaVersion := "2.12.9"

libraryDependencies ++= Seq(
  javaWs,
  guice,
  ehcache,
  filters,
  "org.hibernate.validator" % "hibernate-validator" % "6.1.6.Final",
  "junit" % "junit" % "4.12",
  "org.projectlombok" % "lombok" % "1.18.12",
  "org.mongodb" % "mongodb-driver-sync" % "4.1.0",
  "io.jsonwebtoken" % "jjwt" % "0.9.1",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0",
  "org.glassfish" % "javax.el" % "3.0.0",

//  Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  // akka cluster related stuff
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
  // akka htttp related stuff
  "com.typesafe.akka" %% "akka-http-core" % akkaHTTPVersion,
  "com.typesafe.akka" %% "akka-http2-support" % akkaHTTPVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHTTPVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHTTPVersion
)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.sbtPluginRepo("releases")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"


