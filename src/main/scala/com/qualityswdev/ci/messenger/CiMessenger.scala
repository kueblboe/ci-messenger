package com.qualityswdev.ci.messenger

import scala.util.parsing.json.JSON

import java.io._
import java.net.{ ServerSocket, Socket }

object CiMessenger extends App {
  try {
    val listener = new ServerSocket(9999)
    println("Listening on port 9999.")
    while (true) {
      val socket = listener accept
      val input = new BufferedReader(new InputStreamReader(socket getInputStream))
      val buildStatus = BuildStatusParser parse(input readLine)
      //TODO: medium display(buildStatus)
      println(buildStatus)
      input.close
      socket.close
    }
    listener close
  } catch {
    case e: IOException =>
      System.err.println("Could not listen on port 9999.")
      System.exit(-1)
  }
}

object BuildStatusParser {
  def parse(buildStatusAsJson: String): BuildStatus = {
    implicit def any2string(a: Any)  = a.toString
    implicit def any2double(a: Any)  = a.asInstanceOf[Double]

    JSON.parseFull(buildStatusAsJson) match {
	  case Some(x) => {
        val statusMap = x.asInstanceOf[Map[String, Map[String, Any]]]
        val build = statusMap("build")
        BuildStatus(build("url"), build("phase"), build("number"), build.getOrElse("status", "SUCCESS"))
      }
      case None => throw new InvalidBuildStatusException()
    }
  }
}

case class BuildStatus(url: String, phase: String, number: Double, status: String)

case class InvalidBuildStatusException extends Exception
