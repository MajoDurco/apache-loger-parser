package com.apachelogparser

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar

object TestData {
  val dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z")

  val valid_log_line = "172.17.0.1 - - [17/Mar/2018:09:16:45 +0000] \"GET / HTTP/1.1\" 200 76"
  val not_valid_ip = "1a.17.0.1 - - [17/Mar/2018:09:16:45 +0000] \"GET / HTTP/1.1\" 200 76"
  val now_access_log = new AccessLog("", "", "", new Date(), "" ,"", "")
  val past_time_access_log = new AccessLog("", "", "", getTime(-5), "" ,"", "")
  val get_access_log = new AccessLog("", "", "", new Date(), "GET" ,"", "")
  val post_access_log = new AccessLog("", "", "", getTime(-5), "POST" ,"", "")

  val now_formated = dateFormat.format(new Date())

  def number_of_actual_requests(number: Int, req_type: String):  List[String] = {
    List.fill(number)("172.17.0.1 - - [%s] \"%s / HTTP/1.1\" 200 76".format(now_formated, req_type))
  }
  def getTime(minutes: Int): java.util.Date = {
   val five_min_ago = Calendar.getInstance()
   five_min_ago.add(Calendar.MINUTE, minutes)
   five_min_ago.getTime()
  }

}