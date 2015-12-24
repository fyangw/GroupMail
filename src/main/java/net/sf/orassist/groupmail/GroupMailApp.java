package net.sf.orassist.groupmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GroupMailApp {

	private static void printUsage() {
		System.err.println("Options: \n"
				+ "  --smtp:<smtp server>[:<smtp port(25)>] [--tls:<true|false>] \n"
				+ "  [\"--from:Your Name <your@email.com>\" --pass:<your email password>] \n"
				+ "  [\"--to:Recipient Name <recipient@email.com>, Recipient Name <recipient@email.com>,...\"] \n"
				+ "  [\"--cc:Recipient Name <recipient@email.com>, Recipient Name <recipient@email.com>,...\"] \n"
				+ "  [\"--bcc:Recipient Name <recipient@email.com>, Recipient Name <recipient@email.com>,...\"] \n"
				+ "  [--mails:<mails file (mails.csv), when --text is available, the text of csv will be omit>] \n"
				+ "  [--encoding:<the encoding of input mails file or mail text>]\n"
				+ "  [--subject:<subject> --text:<mail text file, - for console input>]\n"
				+ "The format of mails.csv(the 1st line is title, will not be proceed as mail) Ex:\n"
				+ "  <To mail address>, <To name>, <Inline images>, <Attachments>, <Subject>, <Text(HTML)> \n"
				+ "  a@a.com;b@b.com, Recipient, a.jpg;b.jpg;c.jpg, a.doc;b.ppt;c.zip, Subject, Email text ");
	}

	public static void main(String[] args) throws MailException, IOException {
		String smtpServer = null;
		int smtpPort = 25;
		boolean smtpTls = false;
		String fromMailAddr = null;
		String fromMailPassword = null;
		String[] toMailAddrs = null;
		String[] ccMailAddrs = null;
		String[] bccMailAddrs = null;
		String subject = null;
		String text = null;
		String[] inlineImages = null;
		String[] attachments = null;
		String mailsFilename = "mails.csv";
		String encoding = System.getProperty("sun.jnu.encoding");
		
		for (String arg:args) {
			if (arg.startsWith("--smtp:")) {
				String[] smtpArgs = arg.split(":");
				smtpServer = smtpArgs[1];
				if (smtpArgs.length >= 3) {
					smtpPort = Integer.parseInt(smtpArgs[2]);
					if (smtpPort == 587 || smtpPort == 465) {
						smtpTls = true;
					}
				}
			} else if (arg.startsWith("--tls:")) {
				smtpTls = Boolean.parseBoolean(arg.split(":")[1]);
			} else if (arg.startsWith("--from:")) {
				fromMailAddr = arg.split(":")[1];
			} else if (arg.startsWith("--pass:")) {
				fromMailPassword = arg.split(":")[1];
			} else if (arg.startsWith("--to:")) {
				toMailAddrs = arg.split(":")[1].split(";");
			} else if (arg.startsWith("--cc:")) {
				ccMailAddrs = arg.split(":")[1].split(";");
			} else if (arg.startsWith("--bcc:")) {
				bccMailAddrs = arg.split(":")[1].split(";");
			} else if (arg.startsWith("--subject:")) {
				subject  = arg.split(":")[1];
			} else if (arg.startsWith("--text:")) {
				text = arg.split(":")[1];
			} else if (arg.startsWith("--inlines:")) {
				inlineImages = arg.split(":")[1].split(";");
			} else if (arg.startsWith("--attatchments:")) {
				attachments = arg.split(":")[1].split(";");
			} else if (arg.startsWith("--mails:")) {
				mailsFilename = arg.split(":")[1];
			} else if (arg.startsWith("--encoding:")) {
				encoding = arg.split(":")[1];
			} 
		}
		
		// when console (-) is specified, read all the input from STDIN
		if (text != null && text.equals("-")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, encoding));
			StringBuilder b = new StringBuilder();
			for (String s; (s = reader.readLine()) != null // reach the end of stream 
					&& !s.equals("."); // detect end of input
					b.append(s));
		}

		if (smtpServer == null) {
			printUsage();
			System.exit(-1);
		}
		
		GroupMail groupMail = new GroupMail(smtpServer, smtpPort, smtpTls, fromMailAddr, fromMailPassword);
		if (toMailAddrs != null) {
			groupMail.send(fromMailAddr, toMailAddrs, ccMailAddrs, bccMailAddrs, subject, text, inlineImages, attachments);
		} else {
			groupMail.send(mailsFilename, encoding, fromMailAddr, text);
		}
		
	}
}