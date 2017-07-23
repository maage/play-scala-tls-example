name := """play-tls-example"""

version := "1.0.0"

lazy val one = (project in file("modules/one")).enablePlugins(PlayScala)

lazy val two = (project in file("modules/two")).enablePlugins(PlayScala)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayAkkaHttp2Support)
  .aggregate(one, two)
  .dependsOn(one, two)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
    ws,
    guice,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.1" % Test,

    // https://mvnrepository.com/artifact/org.mortbay.jetty.alpn/jetty-alpn-agent
    "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.6"
)

fork in run := true

javaOptions in root ++= Seq(
    "-Djavax.net.debug=ssl:handshake",

    // Turn on HTTPS, turn off HTTP.
    // This should be https://example.com:9443
    "-Dhttp.port=disabled",
    "-Dhttps.port=9443",

    // Note that using the HTTPS port by itself doesn't set rh.secure=true.
    // rh.secure will only return true if the "X-Forwarded-Proto" header is set, and
    // if the value in that header is "https", if either the local address is 127.0.0.1, or if
    // trustxforwarded is configured to be true in the application configuration file.

    // Define the SSLEngineProvider in our own class.
    "-Dplay.http.sslengineprovider=https.CustomSSLEngineProvider",

    // Enable this if you want to turn on client authentication
    //"-Dplay.ssl.needClientAuth=true"

    // Enable the handshake parameter to be extended for better protection.
    // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html//customizing_dh_keys
    // Only relevant for "DHE_RSA", "DHE_DSS", "DH_ANON" algorithms, in ServerHandshaker.java.
    "-Djdk.tls.ephemeralDHKeySize=2048",

    // Don't allow client to dictate terms - this can also be used for DoS attacks.
    // Undocumented, defined in sun.security.ssl.Handshaker.java:205
    "-Djdk.tls.rejectClientInitiatedRenegotiation=true",

    // Add more details to the disabled algorithms list
    // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html//DisabledAlgorithms
    // and http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7133344
    "-Djava.security.properties=disabledAlgorithms.properties",


    // Fix a version number problem in SSLv3 and TLS version 1.0.
    // http://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html
    "-Dcom.sun.net.ssl.rsaPreMasterSecretFix=true",

    // Tighten the TLS negotiation issue.
    // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html//descPhase2
    // Defined in JDK 1.8 sun.security.ssl.Handshaker.java:194
    "-Dsun.security.ssl.allowUnsafeRenegotiation=false",
    "-Dsun.security.ssl.allowLegacyHelloMessages=false"

    // Enable this if you need to use OCSP or CRL
    // http://docs.oracle.com/javase/8/docs/technotes/guides/security/certpath/CertPathProgGuide.html//AppC
    //"-Dcom.sun.security.enableCRLDP=true"
    //"-Dcom.sun.net.ssl.checkRevocation=true"

    // Enable this if you need TLS debugging
    // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html//Debug
    //"-Djavax.net.debug=ssl:handshake"

    // Change this if you need X.509 certificate debugging
    // http://docs.oracle.com/javase/8/docs/technotes/guides/security/troubleshooting-security.html
    //"-Djava.security.debug=certpath:x509:ocsp"

    // Uncomment if you want to run "./play client" explicitly without SNI.
    //"-Djsse.enableSNIExtension=false"
)

addCommandAlias("client", "runMain Main")

import com.typesafe.sbt.packager.archetypes.systemloader._
enablePlugins(JavaServerAppPackaging, SystemdPlugin)

requiredStartFacilities := Some("systemd-journald.service")

javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.6" % "dist"
