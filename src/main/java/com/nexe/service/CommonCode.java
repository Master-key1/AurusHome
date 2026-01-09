package com.nexe.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;

public class CommonCode {

	static Scanner input = new Scanner(System.in);

	private static String time1;
	private static String date1;
	private static String Timer;
	private static String serverAddress,txnNodeId;


	public static String date_time(String txnId) {

		try {


			if (txnId.length() == 18) {
				System.out.println(
						"\n_______________________________________________________________________________________");
				System.out.println();
				System.out.println("  Transaction Id         Date        Time        App Server        IP");
				System.out.println(
						"------------------------------------------------------------------------------------");

				txnNodeId = txnId.substring(1, 3);
				serverAddress = "192.168.50." + SERVER_IP.get(txnNodeId);
				setSecAdd("192.168.50." + SERVER_IP.get(txnNodeId));
				int year = Integer.parseInt((txnId.substring(3, 5)));
				String date = txnId.substring(5, 8); // Day of the year
				String yy = "20" + year;
				int dayOfYear = Integer.parseInt(date);
				Year y = Year.of(Integer.parseInt(yy));
				LocalDate ld = y.atDay(dayOfYear);

				String timestamp = txnId.substring(8, 16);

				long millis = Long.parseLong(timestamp);
				String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
						TimeUnit.MILLISECONDS.toMinutes(millis)
								- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
						TimeUnit.MILLISECONDS.toSeconds(millis)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

				String dateTime = "";
				String inputDate = ld + " " + hms;
				System.out.println("INPUT DATE : " + inputDate);
				Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inputDate);
				dateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa").format(date2);
				System.out.println();
				System.out.println(txnId + "   " + dateTime + "     " + NodeId.get(txnNodeId));
				System.out.println(
						"_______________________________________________________________________________________");
				Timer = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date2);
				setTimer(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date2));
				time1 = inputDate.substring(11, 13);
				setTime1(inputDate.substring(11, 13));
				date1 = inputDate.substring(0, 10);
				setDate1(inputDate.substring(0, 10));
			} else {
				System.out.println(" INVALID TXNID LENGTH	= " + txnId.length());
				System.out.println(" TRANSACTION ID	= " + txnId);
			}
			return txnId;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String[] extractValue(String CCT_Req,String PRO_Req,String PRO_Resp,String CCT_Resp) {
		try {
			String[] Req_Resp = new String[4];
			if (StringUtils.isNotBlank(CCT_Req)) {
			//	Req_Resp[0] = Encrypter.decrypt(CCT_Req);
			}
			if (StringUtils.isNotBlank(PRO_Req)) {
			//	Req_Resp[1] = Encrypter.decrypt(PRO_Req);
			}
			if (StringUtils.isNoneBlank(PRO_Resp)) {
			//	Req_Resp[2] = Encrypter.decrypt(PRO_Resp);
			}
			if (StringUtils.isNoneBlank(CCT_Resp)) {
			//	Req_Resp[3] = Encrypter.decrypt(CCT_Resp);
			}
			return Req_Resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }

	public static String ExactID(String line) {
		try {
			String id = null;
			String pattern = "\\[([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(line);
			if (m.find()) {
				id = m.group(1);
				//System.out.println("Extracted ID: " + id);
			} else {
				System.out.println("No ID found in the line.");
			}
			return id;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String backupDirectoryPath() {
		// TODO Auto-generated method stub
		String[] dateParts = date1.split("-");

		int year = Integer.parseInt(dateParts[0]);
	    int month = Integer.parseInt(dateParts[1]);
	    int day = Integer.parseInt(dateParts[2]);
	    String formattedDate = "";
		if (txnNodeId.contains("95")) {
			formattedDate = year + "/" + month ;
		} else {
			formattedDate = year + "/" + month + "/" + day + "/Auruspay";
		}
	    String backupDirectory = BACKUP_DIRECTORY.get(txnNodeId)+formattedDate;
		return backupDirectory;
	}
	public static final ImmutableMap<String, String> BACKUP_DIRECTORY = ImmutableMap.<String, String>builder()
			.put("99", "/logbackup/Auruspay/vwuat42a/vwuat42app01.auruspay.com/")
			.put("91", "/logbackup/Auruspay/CHAAUS42UATAPP0001/CHAAUS42UATAPP0001/")
			.put("92", "/logbackup/Auruspay/CHAAUS42UATAPP0002/CHAAUS42UATAPP0002/")
			.put("93", "/logbackup/Auruspay/CHAAUS42UATAPP0003/CHAAUS42UATAPP0003/")
			.put("94", "/logbackup/Auruspay/CHAAUS42UATAPP0004/CHAAUS42UATAPP0004/")
			.put("96", "/logbackup/Auruspay/CHALTD42STGAPP0001/")
			.put("95", "/logbackup/Auruspay/CHALTD42STGAPP0001/")
			.build();

	public static final ImmutableMap<String, String> SERVER_IP = ImmutableMap.<String, String>builder()
			.put("91", "152")
			.put("92", "153")
			.put("93", "172")
			.put("94", "72")
			.put("95", "155")
			.put("96", "169")
			.put("97", "69")
			.put("98", "11")
			.put("99", "71")
			.build();

	public static final ImmutableMap<String, String> NodeId = ImmutableMap.<String, String>builder()
			.put("91", "UAT42 APP1  192.168.50.152")
			.put("92", "UAT42 APP2  192.168.50.153")
			.put("93", "UAT42 APP3  192.168.50.172")
			.put("94", "UAT42 APP4  192.168.50.72")
			.put("95", "STG42 APP1  192.168.50.155")
			.put("96", "PFX42 APP1  192.168.50.169")
			.put("97", "DEV42 APP1  192.168.50.69")
			.put("98", "UAT51 APP1  192.168.106.11")
			.put("99", "STGCRT APP1  192.168.50.71")
			.build();


	//Setters
	public static void setDate1(String date1) {
        CommonCode.date1 = date1;
    }
    public static void setTime1(String time1) {
    	CommonCode.time1 = time1;
    }
    public static void setTimer(String timer) {
    	CommonCode.Timer = timer;
    }
    public static void setSecAdd(String sevadd) {
    	CommonCode.serverAddress = sevadd;
    }

    // Getters
    public static String getDate1() {
        return date1;
    }

    public static String getTime1() {
        return time1;
    }
    public static String getTimer() {
        return Timer;
    }
    public static String getSevAdd() {
        return serverAddress;
    }
}
