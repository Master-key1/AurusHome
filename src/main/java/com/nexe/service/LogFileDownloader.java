package com.nexe.service;
import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class LogFileDownloader {
	private static String txnID = null;

	static Scanner input = new Scanner(System.in);

	private static String time1;
	private static String date1;
	private static String uniqueId;

	private static String CCT_Req;
	private static String PRO_Req;
	private static String CCT_Resp;
	private static String PRO_Resp;
	private static String DateTime,Timer;
	private static String terminalID;
	private static String transactionID;
	private static String CardType;
	private static String totalAmount;
	private static String approval;
	private static String serverAddress,entryDataSrc,ctycode,curcode;
	private static String cardData,MaskedData;
	private static String rrn,respText,actionCode,txnType,url,runCommand,processor,responseCode,actResult,stan;

	private static String textFilePath = "/home/rsonawane@offaurusinc.com/zLogUtility/Logs/";

	private static StringBuilder logs = new StringBuilder();
	private static StringBuilder logsDetails = new StringBuilder();

	public static void main(String[] args) {
		// Initialize variables
		String scpHost = "uat42.auruspay.com";
		String scpUser = "vchavan";
		String scpPassword = "R@!G@d_2k25!";
		String nodeValue = "195240800824139101";
		String[] final_Req_Resp = new String[4];

		String mainHost = "";
		String mainUser = "vchavan";
		String mainPassword = "S@nGl!_2k25!";

		int mainPort = 22;
		int localPort = 2222;  // Local port to use for forwarding

		Session scpSession = null;
		Session mainSession = null;

		CommonCode obj =new CommonCode();

		try {
			String txnId = null;
			
			System.out.println(" =====[[ ENTER TRANSACTION ID ]]=====");
			txnId = input.next();
			txnID = obj.date_time(txnId);
			if (txnID.length() == 18) {
				try {
					mainHost = obj.getSevAdd();
					Timer = obj.getTimer();
					date1= obj.getDate1();
					time1 = obj.getTime1();
					System.out.println("\nOpening SSH connection...");
					 TimeConverter.cmdBuilder(Timer, txnID);

					JSch jsch = new JSch();
					scpSession = jsch.getSession(scpUser, scpHost, 22);
					scpSession.setPassword(scpPassword);
					scpSession.setConfig("StrictHostKeyChecking", "no");

					System.out.println("Connecting to SCP server...");
					scpSession.connect(30000);
					if(scpSession.isConnected()) {
						System.out.println("Connected to SCP server.");
					}

					// Set up port forwarding
					int assignedPort = scpSession.setPortForwardingL(localPort, mainHost, mainPort);
					System.out.println(
							"Port forwarding set up: localhost:" + assignedPort + " -> " + mainHost + ":" + mainPort);

					// Now connect to the main server through the forwarded port
					mainSession = jsch.getSession(mainUser, "localhost", localPort);
					mainSession.setPassword(mainPassword);

					java.util.Properties config = new java.util.Properties();
					config.put("StrictHostKeyChecking", "no");

					mainSession.setConfig(config);

					// Connect to Backup server
					System.out.println("Connecting to Backup server...");
					try {
						mainSession.connect();
					} catch (JSchException e) {
						System.err.println("Failed to connect: " + e.getMessage());
						e.printStackTrace();
					}

					System.out.println("Connected to main server through SCP server." + mainHost);
					runCommand = "hostname";
					if(mainSession.isConnected())
					{
						System.out.println("Connected to Main server.");
					}
					System.out.println("ZGREP COMMOND : " + runCommand);
					System.out.println(
							"--------------------------------------------------------------------------------------------------------------");


					ChannelExec channelExec = (ChannelExec) mainSession.openChannel("exec");
					channelExec.setCommand(runCommand);
					InputStream commandOutput = channelExec.getInputStream();
					channelExec.connect(30000);
					System.out.println(
							"--------------------------------------------------------------------------------------------------------------");
					// Read and print command output
					BufferedReader reader = new BufferedReader(new InputStreamReader(commandOutput));
					String line;
					if ((line = reader.readLine()) != null) {
						// System.out.println(line);
						// Extract specific value from output (assuming format is consistent)
						String regex = "SPO\\.([a-fA-F0-9\\-]{36})";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(line);
						if (line.contains("Generated Aurus Transaction ID") || line.contains("Generated TRANSACTION ID") || line.contains("AURUSPAY_TRANSACTION_ID :")) {
							uniqueId = obj.ExactID(line);
						}else if (matcher.find()) {
							uniqueId = matcher.group(1); // Group 1 is the UUID part
						}
						System.out.println(" UNIQUE ID : " + uniqueId);
						System.out.println(
								"--------------------------------------------------------------------------------------------------------------");
						// Run command with extracted value
						String newZgrepCommand = runCommand.replace(txnID, uniqueId);
						//String newZgrepCommand = "zgrep --color --text \"0f03ee0e-d9a1-479f-b2ae-48d3341a38c6\" /opt/auruspay_switch/log/auruspay/auruspay.log-2025-04-15-03.zip";
						System.out.println("ZGREP COMMOND : " + newZgrepCommand);

						// Disconnect the first channel before opening the second one
						channelExec.disconnect();

						ChannelExec channelExec1 = (ChannelExec) mainSession.openChannel("exec");
						channelExec1.setCommand(newZgrepCommand);
						InputStream ZgrepOutput = channelExec1.getInputStream();
						channelExec1.connect(30000);
						System.out.println(
								"--------------------------------------------------------------------------------------------------------------");
						BufferedReader reader2 = new BufferedReader(new InputStreamReader(ZgrepOutput));
						String newline;
						while ((newline = reader2.readLine()) != null) {
							// System.out.println(line);
							logs.append(newline);
							logs.append("\r\n");
							if ((newline.contains("Received File : SM." + uniqueId))  || (newline.contains("] - [SM." + uniqueId))) {
								break;
							}
						}

						channelExec1.disconnect();
						channelExec.disconnect();

					}

				} catch (JSchException | IOException e) {
					e.printStackTrace();
				} finally {
					if (mainSession != null && mainSession.isConnected()) {
						mainSession.disconnect();
						System.out.println("Disconnected from main server.");
					}
					if (scpSession != null && scpSession.isConnected()) {
						scpSession.disconnect();
						System.out.println("Disconnected from SCP server.");
					}
				}
			} else {
				System.out.println(" INVALID TXNID LENGTH	= " + txnID.length());
				System.out.println(" TRANSACTION ID	= " + txnID);
			}
			System.out.println(
					"\n::::::::::::::::::::::::::::::::::::::LOGGER DETAILS::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

		detailsExtraction(logs, uniqueId);
		final_Req_Resp = extractValue(CCT_Req, PRO_Req, PRO_Resp, CCT_Resp);
		if (StringUtils.isNotBlank(processor) && processor.equals("67")) {
			swedBankDetails(logs, uniqueId);
			System.out.println(logsDetails.toString());
		} else if(StringUtils.isNotBlank(processor) && processor.equals("60")){
			sixxpaymentDetails(logs, uniqueId);
			System.out.println(logsDetails.toString());
		} else if(StringUtils.isNotBlank(processor) && processor.equals("609")){
			synchronyDetails(logs, uniqueId);
			System.out.println(logsDetails.toString());
		} else {
			System.out.println(" Aurus Transaction Id	: " + txnID);
			System.out.println(" Aurus Unique Id	: " + uniqueId);
			System.out.println(" Date & Time	: " + DateTime + " EST");
			if (entryDataSrc != null && !entryDataSrc.isEmpty())
				System.out.println(" Entry Data Src	: " + entryDataSrc.substring(0, entryDataSrc.length() - 1));
		}
		System.out.println("_______________________________________________________________________________________\n");
		System.out.println(" CCT Request	: \n" + final_Req_Resp[0] + "\n");
		System.out.println(" Processor Request	: \n" + final_Req_Resp[1] + "\n");
		if(StringUtils.isNotBlank(txnType) &&  txnType.trim().equalsIgnoreCase("4")) {
			System.out.println(" Processor Response	: \n" + PRO_Resp + "\n");
		}else {
			System.out.println(" Processor Response	: \n" + final_Req_Resp[2] + "\n");
		}
		System.out.println(" CCT Response	: \n" + final_Req_Resp[3]);
		System.out.println("_______________________________________________________________________________________\n");

		System.out.println(logs.toString());
		System.out.println("=================================================================================================================================\n"
				+ "----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n"
				+ "=================================================================================================================================");
	}

	public static String[] extractValue(String CCT_Req,String PRO_Req,String PRO_Resp,String CCT_Resp) {
		String[] Req_Resp = new String[4];
		if (StringUtils.isNotBlank(CCT_Req)) {
			//	Req_Resp[0] = Encrypter.decrypt(CCT_Req);
			System.out.println(" CCT REQUEST	: " + CCT_Req);
		}
		if (StringUtils.isNotBlank(PRO_Req)) {
			System.out.println(" PROCESSOR REQUEST	: " + PRO_Req);
			//	Req_Resp[1] = Encrypter.decrypt(PRO_Req);
		}
		if(StringUtils.isNotBlank(processor) && processor.equals("609")){
			Req_Resp[2] = (PRO_Resp);
		} else if (StringUtils.isNoneBlank(PRO_Resp)) {
			//Req_Resp[2] = Encrypter.decrypt(PRO_Resp);
			System.out.println(" PROCESSOR RESPONSE	: " + PRO_Resp);
			//System.out.println(" PROCESSOR RESPONSE	: " + Req_Resp[2]);
		}
		if (StringUtils.isNoneBlank(CCT_Resp)) {
			//Req_Resp[3] = Encrypter.decrypt(CCT_Resp);
			System.out.println(" CCT RESPONSE	: " + CCT_Resp);
		}
		return Req_Resp;
	}

	private static void detailsExtraction(StringBuilder logs2, String uniqueId) {
		try {
			String logsAsString = logs2.toString(); // Convert StringBuilder to String

			String[] lines = logsAsString.split("\\r?\\n"); // Split by newlines

			for (String line : lines) {
				if (line.contains("IMF PROCESSOR ID : ") && line.contains("ProcessorTypeSelector.getGroupSelector") && (line.contains("IS TEST PROCESSOR :"))) {
					processor = line.substring(line.indexOf("PROCESSOR ID : ") + 15).trim();
				}
				if (line.contains("[SchemaValidationParticipant.") && line.contains("TRANSACTION TYPE : ")) {
					txnType = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST : ")) {
					CCT_Req = line.substring(line.indexOf("REQUEST : ") + 10);
				}

				if (line.contains(" CGI REQUEST :")) {
					PRO_Req = line.substring(line.indexOf("REQUEST :") + 9);
					DateTime = line.substring(0, 19);
				} else if (line.contains("[STPL-GRAY-STREAM]- PROCESSOR REQUEST === ")) {
					PRO_Req = line.substring(line.indexOf("REQUEST === ") + 12);
					DateTime = line.substring(0, 19);
				} else if (line.contains("SixxPaymentCreditProcessor.buildRequest") || line.contains("SixxPaymentCreditProcessorUK.buildRequest") && line.contains("REQUEST : ")) {
					PRO_Req = line.substring(line.indexOf("REQUEST : ") + 10);
					DateTime = line.substring(0, 19);
				}else if (line.contains("CITCON ENQUIRY REQUEST :: ") || line.contains(" CITCON SALE REQUEST :: ") || line.contains(" CITCON REFUND REQUEST :: ") || line.contains("CITCON VOID REQUEST :: ")) {
					PRO_Req = line.substring(line.indexOf("REQUEST :: ") + 11);
					DateTime = line.substring(0, 19);
				} else if (line.contains("SixxPaymentCreditProcessorUK.buildRequest") && line.contains("PROCESSOR REQUEST ::")) {
					PRO_Req = line.substring(line.indexOf("REQUEST :: ") + 11);
					DateTime = line.substring(0, 19);
				} else if (line.contains("SwedBankCreditProcessor.buildRequest") && line.contains("PROCESSOR REQUEST ::")) {
					PRO_Req = line.substring(line.indexOf("REQUEST :: ") + 11);
					DateTime = line.substring(0, 19);
				} else if ((line.contains("SynchronyCartridge") && line.contains("REQUEST : ")) || line.contains("[STPL-GRAY-STREAM]-PROCESSOR REQUEST : ")) {
					PRO_Req = line.substring(line.indexOf("REQUEST : ") + 10);
					DateTime = line.substring(0, 19);
				}
				//System.out.println(" PROCESSOR REQUEST	: " + PRO_Req);
				if (line.contains("[STPL-GRAY-STREAM]- PROCESSOR RESPONSE  : ")) {
					PRO_Resp = line.substring(line.indexOf("PROCESSOR RESPONSE  : ") + 22);
				} else if (line.contains("[STPL-GRAY-STREAM]- PROCESSOR RESPONSE === ")) {
					PRO_Resp = line.substring(line.indexOf("RESPONSE === ") + 13);
				} else if (line.contains("----- RESPONSE : ")) {
					PRO_Resp = line.substring(line.indexOf("RESPONSE : ") + 11);
				} else if (line.contains("CITCON RESPONSE :: ")) {
					PRO_Resp = line.substring(line.indexOf("RESPONSE :: ") + 12);
				} else if (line.contains("[STPL-GRAY-STREAM]- Response : ")) {
					PRO_Resp = line.substring(line.indexOf("Response : ") + 11);
				}else if (line.contains("SwedBankPersistenceCartridge.connect") && line.contains("[STPL-GRAY-STREAM]- FINAL RESPONSE :")) {
					PRO_Resp = line.substring(line.indexOf("FINAL RESPONSE : ") + 17);
				}else if (line.contains("STPL-GRAY-STREAM]- FINAL RESPONSE : ") && line.contains("CGIPersistenceCartridge")) {
					PRO_Resp = line.substring(line.indexOf("FINAL RESPONSE : ") + 17);
				}else if (line.contains("SixxPaymentsCartridge.connectmTLSConn") && line.contains("[STPL-GRAY-STREAM]- ----- PROCESSOR RESPONSE :: ")) {
					PRO_Resp = line.substring(line.indexOf("RESPONSE :: ") + 12);
				}else if (line.contains("SynchronyCartridge") && line.contains("[STPL-GRAY-STREAM]-PROCESSOR RESPONSE :")) {
					PRO_Resp = line.substring(line.indexOf("RESPONSE :") + 10);
				}
				//System.out.println(" PROCESSOR RESPONSE	: " + PRO_Resp);
				if (line.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE : ")) {
					CCT_Resp = line.substring(line.indexOf("RESPONSE :") + 10);
				}
				if (line.contains("IMF CARD TYPE           = ")) {
					CardType = line.substring(line.indexOf("CARD TYPE           = ") + 22);
				}else if(line.contains("IMF_CARD_TYPE : ")) {
					CardType = line.substring(line.indexOf("CARD_TYPE : ") + 12);
				}
				if (line.contains("CONNECTING URL         = ")) {
					url = line.substring(line.indexOf("CONNECTING URL         = ") + 25);
				}
				if (line.contains("[ENTRY DATA SOURCE  : ") && line.contains("ValidateTransactionParticipant")) {
					entryDataSrc = line.substring(line.indexOf("DATA SOURCE  :") + 14).trim();
				}
				if (line.contains("RequestAuthorizationParticipant.prepare") && line.contains("[CURRENCY CODE  : ")) {
					curcode = line.substring(line.indexOf("CODE  : ") + 8).trim();
				}
				if (line.contains("RequestAuthorizationParticipant.prepare") && line.contains("[COUNTRY CODE  : ")) {
					ctycode = line.substring(line.indexOf("CODE  : ") + 8).trim();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void swedBankDetails(StringBuilder logs2, String uniqueId) {
		try {
			String logsAsString = logs2.toString(); // Convert StringBuilder to String

			String[] lines = logsAsString.split("\\r?\\n"); // Split by newlines

			for (String line : lines) {
				if (line.contains("Card Acceptor Terminal Identification (P-41) : ") || line.contains("DE - 41 - Card Acceptor Terminal Identification : ") || line.contains("(P-41) Card Acceptor Terminal Identification : ")) {
					terminalID = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("INDOOR_MERCHANT_ID : ")|| line.contains("OUTDOOR_MERCHANT_ID : ")) {
					transactionID = line.substring(line.indexOf("ID : ") + 5);
				}
				if (line.contains("Systems Trace Audit Number (P-11) : ") || line.contains("(P-11) Systems Trace Audit Number : ")) {
					stan = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("Transaction Amount (P-4) : ") || line.contains("(P-4)-Transaction Amount : ")) {
					totalAmount = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("SwedBankResponseParser.parseCreditResponse") && line.contains("(P-35)-Track-2 Data  : ")) {
					cardData = line.substring(line.indexOf(" : ") + 6);
				}
				if (line.contains("[ENTRY DATA SOURCE  : ") && line.contains("ValidateTransactionParticipant")) {
					entryDataSrc = line.substring(line.indexOf("DATA SOURCE  :") + 13);
				}
				if (line.contains("(P-37) Retrieval Reference Number : ") || line.contains("P-37)-Retrieval Reference Number    : ")) {
					rrn = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("(P-38)-Approval Code : ") || line.contains("(P-38) Auth.Approval Code : ")) {
					approval = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("PRES_TA_PROCESSOR_RESPONSE_CODE : ")) {
					responseCode = line.substring(line.indexOf("PROCESSOR_RESPONSE_CODE : ") + 26);
				}
				if (line.contains("PRES_PROCESSOR_RESPONSE_MESSAGE : ")) {
					actResult = line.substring(line.indexOf("PROCESSOR_RESPONSE_MESSAGE : ") + 29);
				}
			}

			logsDetails.append(" Aurus Transaction Id : ").append(txnID).append("\n");
			logsDetails.append(" Aurus Unique ID : ").append(uniqueId).append("\n");
			logsDetails.append(" Transaction Type : ").append(txnType).append("\n");
			logsDetails.append(" Transaction Date and Time : ").append(DateTime).append(" EST\n");
			logsDetails.append(" Entry Data Source  : ").append(entryDataSrc).append("\n");
			logsDetails.append(" Mask Card Number : ").append(cardData).append("\n");
			logsDetails.append(" Processor Terminal ID : ").append(terminalID).append("\n");
			logsDetails.append(" Processor Merchant ID : ").append(transactionID).append("\n");
			logsDetails.append(" Total Amount : ").append(totalAmount).append("\n");
			logsDetails.append(" Card Type : ").append(CardType).append("\n");
			logsDetails.append(" STAN : ").append(stan).append("\n");
			logsDetails.append(" RRN : ").append(rrn).append("\n");
			logsDetails.append(" Approval Code : ").append(approval).append("\n");
			logsDetails.append(" Response Code : ").append(responseCode).append("\n");
			logsDetails.append(" Response Message : ").append(actResult).append("\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void sixxpaymentDetails(StringBuilder logs2, String uniqueId) {
		try {
			String logsAsString = logs2.toString(); // Convert StringBuilder to String

			String[] lines = logsAsString.split("\\r?\\n"); // Split by newlines

			for (String line : lines) {
				if (line.contains("Bit 02 - Primary Account Number       : ")|| line.contains("Bit 02 - Primary Account Number      : ")) {
					cardData = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("Bit 41 - POS Terminal ID             : ") || line.contains("DE - 41 - Card Acceptor Terminal Identification : ")) {
					terminalID = line.substring(line.indexOf("POS Terminal ID             : ") + 30);
				}
				if (line.contains("Bit 42 - Card Acceptor ID Code       : ")|| line.contains("DE - 42 - Card Acceptor Identification Code : ")) {
					transactionID = line.substring(line.indexOf("Card Acceptor ID Code       : ") + 30);
				}
				if (line.contains("Bit 11 - Systems Trace Audit Number  :")|| line.contains("DE - 11 - System Trace Audit Number : ")) {
					stan = line.substring(line.indexOf("Systems Trace Audit Number  :") + 29);
				}
				if (line.contains("Bit 04 - Transaction Amount           : ")|| line.contains("Bit 04 - Transaction Amount          : ")) {
					totalAmount = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("[ENTRY DATA SOURCE  : ") && line.contains("ValidateTransactionParticipant")) {
					entryDataSrc = line.substring(line.indexOf("DATA SOURCE  :") + 14);
				}
				if (line.contains("Bit (38) AUTHORIZATION_IDENTIFICATION_RESPONSE : 12,") && line.contains("SixxPaymentsResponseParser.parseCreditResponse")|| line.contains("DE - 38 - Approval Code : ")) {
					approval = line.substring(line.indexOf("IDENTIFICATION_RESPONSE : 12,") + 29);
				}
				if (line.contains("PRES_TA_PROCESSOR_RESPONSE_CODE : ")) {
					responseCode = line.substring(line.indexOf("PROCESSOR_RESPONSE_CODE : ") + 26);
				}
				if (line.contains("PRES_PROCESSOR_RESPONSE_MESSAGE : ")) {
					actResult = line.substring(line.indexOf("PROCESSOR_RESPONSE_MESSAGE : ") + 29);
				}
			}
			logsDetails.append(" Aurus Transaction Id : ").append(txnID).append("\n");
			logsDetails.append(" Aurus Unique ID : ").append(uniqueId).append("\n");
			logsDetails.append(" Transaction Type : ").append(txnType).append("\n");
			logsDetails.append(" Transaction Date and Time : ").append(DateTime).append(" EST\n");
			logsDetails.append(" Entry Data Source  : ").append(entryDataSrc).append("\n");
			//logsDetails.append(" Mask Card Number : ").append(Encrypter.decrypt(cardData)).append("\n");
			logsDetails.append(" Processor Terminal ID : ").append(terminalID).append("\n");
			logsDetails.append(" Processor Merchant ID : ").append(transactionID).append("\n");
			logsDetails.append(" STAN : ").append(stan).append("\n");
			logsDetails.append(" Total Amount : ").append(totalAmount).append("\n");
			logsDetails.append(" Card Type : ").append(CardType).append("\n");
			logsDetails.append(" Approval Code : ").append(approval.replaceAll("f", "").replaceAll("F", "")).append("\n");
			logsDetails.append(" Response Code : ").append(responseCode).append("\n");
			logsDetails.append(" Response Message : ").append(actResult).append("\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void worldlineUKDetails(StringBuilder logs2, String uniqueId) {
		try {
			String logsAsString = logs2.toString(); // Convert StringBuilder to String

			String[] lines = logsAsString.split("\\r?\\n"); // Split by newlines

			for (String line : lines) {
				if (line.contains("Bit 02 - Primary Account Number      : ")) {
					cardData = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("Bit 41 - POS Terminal ID             : ") || line.contains("DE - 41 - Card Acceptor Terminal Identification : ")) {
					terminalID = line.substring(line.indexOf("POS Terminal ID             : ") + 30);
				}
				if (line.contains("Bit 42 - Card Acceptor ID Code       : ")|| line.contains("DE - 42 - Card Acceptor Identification Code : ")) {
					transactionID = line.substring(line.indexOf("Card Acceptor ID Code       : ") + 30);
				}
				if (line.contains("Bit 11 - Systems Trace Audit Number  : ")|| line.contains("Bit 11 - Systems Trace Audit Number  : ")) {
					stan = line.substring(line.indexOf("Number  : ") + 10);
				}
				if (line.contains("Bit 04 - Transaction Amount           : ")) {
					totalAmount = line.substring(line.indexOf("Transaction Amount           : ") + 31);
				}
				if (line.contains("[ENTRY DATA SOURCE  : ") && line.contains("ValidateTransactionParticipant")) {
					entryDataSrc = line.substring(line.indexOf("DATA SOURCE  :") + 14);
				}
				if (line.contains("Bit (38) AUTHORIZATION_IDENTIFICATION_RESPONSE : 12,") && line.contains("SixxPaymentsResponseParser.parseCreditResponse")|| line.contains("DE - 38 - Approval Code : ")) {
					approval = line.substring(line.indexOf("IDENTIFICATION_RESPONSE : 12,") + 29);
				}
				if (line.contains("PRES_TA_PROCESSOR_RESPONSE_CODE : ")) {
					responseCode = line.substring(line.indexOf("PROCESSOR_RESPONSE_CODE : ") + 26);
				}
				if (line.contains("PRES_PROCESSOR_RESPONSE_MESSAGE : ")) {
					actResult = line.substring(line.indexOf("PROCESSOR_RESPONSE_MESSAGE : ") + 29);
				}
			}
			logsDetails.append(" Aurus Transaction Id : ").append(txnID).append("\n");
			logsDetails.append(" Aurus Unique ID : ").append(uniqueId).append("\n");
			logsDetails.append(" Transaction Type : ").append(txnType).append("\n");
			logsDetails.append(" Transaction Date and Time : ").append(DateTime).append(" EST\n");
			logsDetails.append(" Entry Data Source  : ").append(entryDataSrc).append("\n");
			//   logsDetails.append(" Mask Card Number : ").append(Encrypter.decrypt(cardData)).append("\n");
			logsDetails.append(" Processor Terminal ID : ").append(terminalID).append("\n");
			logsDetails.append(" Processor Merchant ID : ").append(transactionID).append("\n");
			logsDetails.append(" STAN : ").append(stan).append("\n");
			logsDetails.append(" Total Amount : ").append(totalAmount).append("\n");
			logsDetails.append(" Card Type : ").append(CardType).append("\n");
			logsDetails.append(" Approval Code : ").append(approval.replaceAll("f", "").replaceAll("F", "")).append("\n");
			logsDetails.append(" Response Code : ").append(responseCode).append("\n");
			logsDetails.append(" Response Message : ").append(actResult).append("\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void synchronyDetails(StringBuilder logs2, String uniqueId) {
		try {
			String logsAsString = logs2.toString(); // Convert StringBuilder to String

			String[] lines = logsAsString.split("\\r?\\n"); // Split by newlines

			for (String line : lines) {
				if (line.contains("ACCESS TOKEN : ")) {
					cardData = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("INF_TERMINAL_ID = ")) {
					terminalID = line.substring(line.indexOf(" = ") + 3);
				}
				if (line.contains("INF_PLCC_PROCESSOR_MERCHANT_ID = ")) {
					transactionID = line.substring(line.indexOf(" = ") + 3);
				}
				if (line.contains("X-SYF-Request-TrackingId : ")) {
					stan = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("Bit 04 - Transaction Amount           : ")|| line.contains("Bit 04 - Transaction Amount          : ")) {
					totalAmount = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("[ENTRY DATA SOURCE  : ") && line.contains("ValidateTransactionParticipant")) {
					entryDataSrc = line.substring(line.indexOf("DATA SOURCE  :") + 14);
				}
				if (line.contains("X-SYF-Consumer-Id Value : ") || line.contains("clientId : ")) {
					approval = line.substring(line.indexOf("Value : ") + 8);
				}
				if (line.contains("APPROVAL_CODE  : ")) {
					responseCode = line.substring(line.indexOf(" : ") + 3);
				}
				if (line.contains("RESULT DESCIPTION  = ")) {
					actResult = line.substring(line.indexOf(" = ") + 3);
				}
				if (line.contains("IMF_PLCC_SUB_CARD_TYPE :")) {
					CardType = line.substring(line.indexOf("CARD_TYPE :") + 11);
				}
			}
			logsDetails.append(" Aurus Transaction Id : ").append(txnID).append("\n");
			logsDetails.append(" Aurus Unique ID : ").append(uniqueId).append("\n");
			logsDetails.append(" Transaction Type : ").append(txnType).append("\n");
			logsDetails.append(" Transaction Date and Time : ").append(DateTime).append(" EST\n");
			logsDetails.append(" Entry Data Source  : ").append(entryDataSrc).append("\n");
			logsDetails.append(" Client ID : ").append(approval.replaceAll("f", "").replaceAll("F", "")).append("\n");
			logsDetails.append(" Access Token : ").append(cardData).append("\n");
			logsDetails.append(" Tracking ID : ").append(stan).append("\n");
			logsDetails.append(" Processor Terminal ID : ").append(terminalID).append("\n");
			logsDetails.append(" Processor Merchant ID : ").append(transactionID).append("\n");
			logsDetails.append(" Total Amount : ").append(totalAmount).append("\n");
			logsDetails.append(" Card Type : ").append(CardType).append("\n");
			logsDetails.append(" Response Code : ").append(responseCode).append("\n");
			logsDetails.append(" Response Message : ").append(actResult).append("\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void formatCreator(String[] final_Req_Resp) {
		// TODO Auto-generated method stub
		logsDetails.append(" Aurus Transaction Id	: ").append(txnID).append("\n");
		logsDetails.append(" Aurus Unique Id	: ").append(uniqueId).append("\n");
		logsDetails.append(" Date & Time	: ").append(DateTime).append(" EST\n\n");
		logsDetails.append(" Transaction Type	: ").append(txnType).append("\n");
		logsDetails.append(" Processor Terminal Id 	: ").append(terminalID).append("\n");
		logsDetails.append(" Processor Merchant Id	: ").append(transactionID).append("\n");
		logsDetails.append(" Total Amount	: ").append(totalAmount).append("\n\n");
		logsDetails.append(" Card Type	: ").append(CardType).append("\n");
		logsDetails.append(" STAN	: ").append(stan).append("\n\n");
		logsDetails.append(" RRN	: ").append(rrn).append("\n");
		logsDetails.append(" Approval Code	: ").append(approval).append("\n\n");
		logsDetails.append(" Response Code	: ").append(actionCode).append("\n");
		logsDetails.append(" Response Message	: ").append(respText).append("\n\n");

		logsDetails.append("_______________________________________________________________________________________").append("\n\n");

		logsDetails.append(" CCT Request : \n\n").append(final_Req_Resp[0]).append("\n\n");
		logsDetails.append(" Processor Request : \n\n").append(final_Req_Resp[1]).append("\n\n");
		if (StringUtils.isNotBlank(final_Req_Resp[2]))
			logsDetails.append(" Processor Response : \n\n").append(final_Req_Resp[2]).append("\n\n");
		else
			logsDetails.append(" Processor Response : \n\n").append(PRO_Resp).append("\n\n");
		logsDetails.append(" CCT Response : \n\n").append(final_Req_Resp[3]).append("\n\n");

		logsDetails.append("_______________________________________________________________________________________").append("\n");

	}



}
