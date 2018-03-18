package com.apachelogparser

import scala.io.Source
import java.io.{FileNotFoundException, IOException}

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

// Main object wher program execution starts
// Program takes one agrument which is system path to apache access log file
// usually /var/log/apache2/access.log
object Log extends App {
  if (args.length == 0) {
    println("You need to give me path to the log file!")
  }
  else if (args.length > 1) {
    println("You've given me too much arguments, I just need a path to the log file.")
  }
  else {
    processFileLines(args(0), processLogs)
  }

  // Read the file line by line into the List and call func to process it
  def processFileLines(file_path: String, func: (List[String]) => Boolean): Boolean = {
    try {
      var log_file = Source.fromFile(file_path)
      try {
        val list_of_lines: List[String] = log_file.getLines.toList
        func(list_of_lines)
      }
      finally {
        log_file.close
      }
    }
    catch {
      case e: FileNotFoundException => {
       println("Couldn't find the file: %s".format(file_path))
       false
      }
      case e: IOException => {
        println("Got an IOException!")
        false
      }
    }
  }

  def parseLogs(logs: List[String]): AccessLogs = {
    var parser = new AccessLogParser
    new AccessLogs(
      logs
        .map((log) => parser.parse(log))
        .flatten
    )
  }

  def processLogs(logs: List[String]): Boolean = {
    val access_logs = parseLogs(logs)
    val is_more_requests = access_logs
      .filterByMinutesAgo(5)
      .filterByRequestType("GET")
      .isMoreLogsThan(10)
    if (is_more_requests){
      println("Attention, more than 10 GET requests in the past 5 min!")
      true
    }
    else {
      println("No more than 10 GET requests in the past 5 min!")
      false
    }
  }
}

// Parser works on regular expressions when
// access log format is: "%h %l %u %t \"%r\" %>s %b" 
// https://httpd.apache.org/docs/2.4/mod/mod_log_config.html#formats
class AccessLogParser() {
  private val ddd = "\\d{1,3}"
  private val ip = "(%s\\.%s\\.%s\\.%s)".format(ddd, ddd, ddd, ddd)
  private val user = "(\\S+)"
  private val time = "\\[(.+)\\]"
  private val request = "\"(.*?)\""
  private val status = "(\\d{3})"
  private val bytes = "(\\S+)"
  private val regex = "^%s %s %s %s %s %s %s$"
    .format(ip, user, user, time, request, status, bytes)
  private val pattern = Pattern.compile(regex)

  def parse(log: String): Option[AccessLog] = {
    val matcher = pattern.matcher(log)
    if(matcher.find) {
      Some(buildAccessLog(matcher))
    }
    else {
      None
    }
  }

  def buildAccessLog(matcher: Matcher): AccessLog = {
    AccessLog(
      matcher.group(1),
      matcher.group(2),
      matcher.group(3),
      parseTime(matcher.group(4)),
      parseRequest(matcher.group(5)),
      matcher.group(6),
      matcher.group(7)
    )
  }

  def parseTime(time: String): java.util.Date = {
    val dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z")
    try {
      dateFormat.parse(time)
    }
  }

  def parseRequest(request: String): String = {
    val request_regex = "^(\\S+) .*?$"
    val request_pattern = Pattern.compile(request_regex)
    val request_matcher = request_pattern.matcher(request)
    if (request_matcher.find) {
      request_matcher.group(1)
    }
    else {
      ""
    }
  }
}

class AccessLogs(access_logs: List[AccessLog]) {

  def filterByMinutesAgo(minutes: Int): AccessLogs = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MINUTE, -minutes)
    val five_min_ago = cal.getTime()
  
    new AccessLogs(access_logs.filter((log) => log.time.after(five_min_ago)))
  }

  def filterByRequestType(request_type: String): AccessLogs = {
    new AccessLogs(access_logs.filter((log) => log.request == request_type))
  }

  def isMoreLogsThan(number: Int): Boolean = {
    length > number
  }
  
  def length(): Int = {
    access_logs.length
  }

  override def toString(): String = {
    access_logs.toString
  }
}

case class AccessLog (
  remoteHostName: String,
  remoteLogName: String,
  remoteUser: String,
  time: java.util.Date,
  request: String,
  statusCode: String,
  sizeOfResponse: String
)