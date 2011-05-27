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
      Medium display(buildStatus)
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
    JSON.parseFull(buildStatusAsJson) match {
	  case Some(buildStatus) => {
        val buildStatusAsMap = buildStatus.asInstanceOf[Map[String, Map[String, Any]]]
        val build = buildStatusAsMap("build")
        BuildStatus(build("url").toString,
        		    build("phase").toString,
        		    build("number").asInstanceOf[Double],
        		    build.getOrElse("status", "SUCCESS").toString)
      }
      case None => throw new InvalidBuildStatusException()
    }
  }
}

object Medium {
  def display(buildStatus: BuildStatus) {
	println(buildStatus)
  }
}

case class BuildStatus(url: String, phase: String, number: Double, status: String)

case class InvalidBuildStatusException extends Exception
