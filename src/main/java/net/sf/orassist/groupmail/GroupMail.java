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
  
public class GroupMail {  
	protected static final int TO_MAIL_ADDR = 0;
	protected static final int TO_NAME = 1;
	protected static final int SUBJECT = 2;
	protected static final int TEXT = 3;
	protected static final int INLINE = 4;
	protected static final int ATTACHMENT = 5;

	private String smtpServer;
	private int smtpPort;
	private boolean smtpTls;
	private String fromMailAddr;
	private String fromMailPassword;
	private JavaMailSenderImpl mailSender;

	public GroupMail(String smtpServer, int smtpPort, boolean smtpTls, String fromMailAddr,
			String fromMailPassword) {
		
		this.smtpServer = smtpServer;
		this.smtpPort = smtpPort;
		this.smtpTls = smtpTls;
		this.fromMailAddr = fromMailAddr;
		this.fromMailPassword = fromMailPassword;
		
		// 发送器  
		this.mailSender = new JavaMailSenderImpl();
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

		Session mailSession = Session.getInstance(mailProps);  
		mailSender.setSession(mailSession); // 为发送器指定会话
	}

	public void send(String fromMailAddr, String fromName, 
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

	public void send(String mailsFilename, String fromName, String encoding) {
		try {
			  
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mailsFilename), encoding));
			String line = reader.readLine(); // Skip the first line (header title) of csv
			while ((line = reader.readLine()) != null) {
				String[] mail = line.split(",");
				String toName;
				String toMailAddr;
				send(fromMailAddr, 
					fromName,  
					toMailAddr = mail[TO_MAIL_ADDR],
					toName = mail[TO_NAME], 
					mail[SUBJECT],
					mail[TEXT],
					mail.length <= INLINE ? new String[] {} : mail[INLINE].split(":"), 
					mail.length <= ATTACHMENT ? new String[] {} : mail[ATTACHMENT].split(":"));
			}
			reader.close();
		  
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
