package com.apachelogparser

import java.text.SimpleDateFormat

import org.scalatest._

class LogSpec extends FunSpec with BeforeAndAfter {
  describe("AccessLogParser") {
    it("Should not parse invalid log") {
      val parser = new AccessLogParser
      assert(parser.parse("some non matching string") == None)
    }
    it("Should not parse invalid ip in log") {
      val parser = new AccessLogParser
      assert(parser.parse(TestData.not_valid_ip) == None)
    }
    it("Should parse valid log line") {
      val parser = new AccessLogParser
      val dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z")
      parser.parse(TestData.valid_log_line) match {
        case Some(log) => {
          assert(log.remoteHostName == "172.17.0.1")
          assert(log.remoteLogName == "-")
          assert(log.remoteUser == "-")
          assert(log.time == dateFormat.parse("17/Mar/2018:09:16:45 +0000"))
          assert(log.request == "GET")
          assert(log.statusCode == "200")
          assert(log.sizeOfResponse == "76")
        }
        case None => fail()
      }
    }
  }

  describe("AccessLogs") {
    it("Should select leave log with actual time in the list") {
      val access_log = new AccessLogs(List(TestData.now_access_log))
      assert(access_log.filterByMinutesAgo(1).length == 1)
    }
    it("Should filter all time from list") {
      val access_log = new AccessLogs(List(TestData.past_time_access_log))
      assert(access_log.filterByMinutesAgo(1).length == 0)
    }
    it("Should only leave GET requests in list") {
      val access_log = new AccessLogs(List(
        TestData.post_access_log,
        TestData.get_access_log
      ))
      assert(access_log.length == 2)
      assert(access_log.filterByRequestType("GET").length == 1)
    }
    it("Should only leave all POST requests in list") {
      val access_log = new AccessLogs(List(
        TestData.post_access_log,
        TestData.post_access_log,
        TestData.post_access_log,
        TestData.post_access_log,
      ))
      assert(access_log.length == 4)
      assert(access_log.filterByRequestType("POST").isMoreLogsThan(3))
    }
  }

  describe("Log"){
    it("Should pass when there are 11 GET requests in 5 minutes"){
      assert(Log.processLogs(TestData.number_of_actual_requests(11, "GET")))
    }
    it("Should fail when there are 10 GET requests in 5 minutes"){
      assert(Log.processLogs(TestData.number_of_actual_requests(10, "GET")) == false)
    }
    it("Should fail when there are  11 POST requests in 5 minutes"){
      assert(Log.processLogs(TestData.number_of_actual_requests(10, "POST")) == false)
    }
  }
}
