package com.nexe.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class TimeConverter {
	private static String time1;
	private static String date1;
	private static List<String> dateTime = new ArrayList<String>();

	 public static List<String> cmdBuilder(String estTimestamp, String txnId) {
	        String Command;
	        String time1 = estTimestamp.substring(11, 13);
	        String date1 = estTimestamp.substring(0, 10);

	        // Replace hour part if it is "12"
	        if (time1.equals("12")) {
	            estTimestamp = estTimestamp.substring(0, 11) + "00" + estTimestamp.substring(13);
	            time1 = estTimestamp.substring(11, 13);
	        }

	        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        LocalDateTime estDateTime = LocalDateTime.parse(estTimestamp, inputFormatter);

	        LocalDateTime istDateTime = estDateTime.plusHours(10).plusMinutes(30);

	        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
	        String istTimestamp = istDateTime.format(outputFormatter.withZone(ZoneId.of("Asia/Kolkata")));

	        System.out.println("EST Timestamp: " + estTimestamp + " EST");
	        System.out.println("IST Timestamp: " + istTimestamp + " IST");

	        LocalDateTime currentIstTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

	        long minutesDifference = java.time.Duration.between(istDateTime, currentIstTime).toMinutes();

		 dateTime.add(date1);
		 dateTime.add(time1);

	      // Command = "zgrep --color --text \"" + txnId + "\" /opt/auruspay_switch/log/auruspay/auruspay.log-" + date1 + "-" + time1 + ".zip";
	   //   Command = "zgrep --color --text \"" + txnId + "\" /opt/auruspay_switch/log/auruspay/auruspay.log";
	       //Command = "zgrep --color --text \"" + txnId + "\" /opt/auruspay_switch/log/auruspay/auruspay.log-" + "2025-11-26-07" + ".zip";
	        return dateTime;
	    }

}
