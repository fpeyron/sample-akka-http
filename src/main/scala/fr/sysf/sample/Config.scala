package fr.sysf.sample

import java.time.Duration

import com.typesafe.config.ConfigFactory

/**
  * Configuration object to get parameters
  */
object Config {

  object api {
    lazy val port: Int         = ConfigFactory.load().getInt("api.http.port")
    lazy val hostname: String  = ConfigFactory.load().getString("api.http.hostname")
    lazy val timeout: Duration = ConfigFactory.load().getDuration("api.http.timeout")
  }

  import com.typesafe.config.Config

  implicit class RichConfig(val underlying: Config) extends AnyVal {

    def getOptionalInt(path: String): Option[Int] = if (underlying.hasPath(path)) Some(underlying.getInt(path)) else None

    def getOptionalString(path: String): Option[String] = if (underlying.hasPath(path)) Some(underlying.getString(path)) else None
  }

}
