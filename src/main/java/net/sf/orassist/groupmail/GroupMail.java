package net.sf.orassist.groupmail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;  
import javax.mail.internet.MimeMessage;  
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;  
import org.springframework.mail.javamail.MimeMessageHelper;  
  
public class GroupMail implements Runnable {  
	private static final int TO_MAIL_ADDR = 0;
	private static final int TO_NAME = 1;
	private static final int SUBJECT = 2;
	private static final int TEXT = 3;
	private static final int INLINE = 4;
	private static final int ATTACHMENT = 5;
	
	private String smtpServer;
	private int smtpPort;
	private boolean smtpTls;
	private String fromMailAddr;
	private String fromMailPassword;
	private String mailsFilename;
	private String fromName;
	private String encoding;

	public GroupMail(String smtpServer, int smtpPort, boolean smtpTls, String fromMailAddr,
			String fromMailPassword, String mailsFilename, String fromName, String encoding) {
		
		this.smtpServer = smtpServer;
		this.smtpPort = smtpPort;
		this.smtpTls = smtpTls;
		this.fromMailAddr = fromMailAddr;
		this.fromMailPassword = fromMailPassword;
		this.fromName = fromName;
		this.mailsFilename = mailsFilename;
		this.encoding = encoding;
		
	}

	public void send(JavaMailSenderImpl mailSender, 
			String fromMailAddr, String fromName, 
			String toMailAddr, String toName, 
			String subject, String text,
			String[] inlineImages, String[] attechments) throws MessagingException {
		
		//SimpleMailMessage mail = new SimpleMailMessage(); // 简单邮件
		MimeMessage mail = mailSender.createMimeMessage(); // 复杂邮件
		MimeMessageHelper helper = new MimeMessageHelper(mail, true);  
		helper.setFrom(fromMailAddr); // 来自  
		helper.setTo(toMailAddr); // 发送到邮件地址  
		helper.setSubject(subject); // 标题  
		// 邮件内容，第二个参数指定发送的是HTML格式  
		helper.setText(text, true);  
		// 增加CID内容  
		if (inlineImages != null) {
			for (String inlineImage : inlineImages) {
				if (inlineImage.length() > 0) {
					helper.addInline(inlineImage, new File(inlineImage));
				}
			}
		}
		// 增加附件  
		if (attechments != null) {
			for (String attachment : attechments) {
				if (attachment.length() > 0) {
					helper.addAttachment(attachment, new File(attachment));
				}
			}
		}
		  
		mailSender.send(mail); // 发送  
		 
	}

	public void run() {
		try {
			  
			// 发送器  
			JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
			mailSender.setHost(smtpServer);  
			mailSender.setPort(smtpPort);
			mailSender.setUsername(fromMailAddr);
			mailSender.setPassword(fromMailPassword);
			mailSender.setDefaultEncoding("UTF-8");
			
			// 配置文件对象  
			Properties mailProps = new Properties();
			if (fromMailPassword != null) {
				mailProps.put("mail.smtp.auth", "true"); // 是否进行验证
			}
			if (smtpTls) {
				mailProps.put("mail.smtp.starttls.enable", "true"); // 使用TLS
			}

			Session session = Session.getInstance(mailProps);  
			mailSender.setSession(session); // 为发送器指定会话
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mailsFilename), encoding));
			String line = reader.readLine();
			for (;(line = reader.readLine()) != null;) {
				String[] mail = line.split(",");
				String toName;
				String toMailAddr;
				send(mailSender, 
						fromMailAddr, 
						fromName,  
						toMailAddr = mail[TO_MAIL_ADDR],
						toName = mail[TO_NAME], 
						mail[SUBJECT],
						mail[TEXT],
						mail.length <= INLINE ? new String[] {} : mail[INLINE].split(":"), 
						mail.length <= ATTACHMENT ? new String[] {} : mail[ATTACHMENT].split(":"));
		        System.out.println(toName + " " + toMailAddr + " 邮件发送成功");
			}
		  
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	private static void printUsage() {
		System.err.println("Options: "
				+ "  --smtp:<smtp server>[:<smtp port(25)>] [--tls:<true|false>] \n"
				+ "  [--from:<your email address> --pass:<your email password>] \n"
				+ "  [--mails:<mails file (mails.csv)> [--encoding:<the encoding of mails file>]]\n"
				+ "The format of mails.csv(the 1st line is title, will not be proceed as mail) Ex:\n"
				+ "  <To Mail Address>, <To Name>, <Subject>, <Text>\n"
				+ "  tomail@domain.com, your name, subject, email");
	}

	public static void main(String[] args) {
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
		
		new GroupMail(smtpServer, smtpPort, smtpTls, fromMailAddr, fromMailPassword, mailsFilename, fromName, encoding).run();
    }
}
