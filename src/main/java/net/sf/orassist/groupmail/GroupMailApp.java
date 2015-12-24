package net.sf.orassist.groupmail;

public class GroupMailApp {

	private static void printUsage() {
		System.err.println("Options: "
				+ "  --smtp:<smtp server>[:<smtp port(25)>] [--tls:<true|false>] \n"
				+ "  [--from:<your email address> --pass:<your email password>] \n"
				+ "  [--mails:<mails file (mails.csv)> [--encoding:<the encoding of mails file>]]\n"
				+ "The format of mails.csv(the 1st line is title, will not be proceed as mail) Ex:\n"
				+ "  <To Mail Address>, <To Name>, <Subject>, <Text>\n"
				+ "  tomail@domain.com, your name, subject, email");
	}

	public static void main(String[] args) throws MailException {
		String smtpServer = null;
		int smtpPort = 25;
		boolean smtpTls = false;
		String fromMailAddr = null;
		String fromMailPassword = null;
		String mailsFilename = "mails.csv";
		String fromName = "";
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
			} else if (arg.startsWith("--mails:")) {
				mailsFilename = arg.split(":")[1];
			} else if (arg.startsWith("--encoding:")) {
				encoding = arg.split(":")[1];
			} 
		}
		
		if (smtpServer == null) {
			printUsage();
			System.exit(-1);
		}
		
		new GroupMail(smtpServer, smtpPort, smtpTls, fromMailAddr, fromMailPassword)
		.send(mailsFilename, fromMailAddr, fromName, encoding);
	}

}