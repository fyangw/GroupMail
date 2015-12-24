package net.sf.orassist.groupmail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;  
import javax.mail.internet.MimeMessage;  

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;  
import org.springframework.mail.javamail.MimeMessageHelper;  
  
public class GroupMail {  
	private static final int TO_MAIL_ADDR = 0;
	private static final int CC_MAIL_ADDR = 1;
	private static final int BCC_MAIL_ADDR = 2;
	private static final int INLINE = 3;
	private static final int ATTACHMENT = 4;
	private static final int SUBJECT = 5;
	private static final int TEXT = 6;

	private JavaMailSenderImpl mailSender;
	
	public GroupMail(String smtpServer, int smtpPort, boolean smtpTls, String fromMailAddr,
			String fromMailPassword) {
		
		// 发送器  
		mailSender = new JavaMailSenderImpl();
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

	public void send(String fromMailAddr, 
			String[] toMailAddrs, String[] ccMailAddrs, String[] bccMailAddrs,
			String subject, String text,
			String[] inlineImages, String[] attechments) throws MailException {
		
		try {
			//SimpleMailMessage mail = new SimpleMailMessage(); // 简单邮件
			MimeMessage mail = mailSender.createMimeMessage(); // 复杂邮件
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);  
			helper.setFrom(fromMailAddr); // 来自  
			if (toMailAddrs != null) for (String mailAddr:toMailAddrs) if (!mailAddr.equals("")){ 
				helper.addTo(mailAddr); // TO邮件地址
			}
			if (ccMailAddrs != null) for (String mailAddr:ccMailAddrs) if (!mailAddr.equals("")){ 
				helper.addCc(mailAddr); // CC邮件地址
			}
			if (ccMailAddrs != null) for (String mailAddr:bccMailAddrs) if (!mailAddr.equals("")){ 
				helper.addBcc(mailAddr); // BCC邮件地址
			}
			helper.setSubject(subject); // 标题  
			helper.setText(text, true); // 邮件内容，第二个参数指定发送的是HTML格式  
			// 增加CID内容  
			if (inlineImages != null) {
				for (String inlineImage : inlineImages)	if (inlineImage.length() > 0) {
					helper.addInline(inlineImage, new File(inlineImage));
				}
			}
			// 增加附件  
			if (attechments != null) {
				for (String attachment : attechments) if (attachment.length() > 0) {
					helper.addAttachment(attachment, new File(attachment));
				}
			}
			  
			mailSender.send(mail); // 发送  
		 
		} catch (Throwable t) {
			throw new MailException(t, "Error occured when send"
					+ " from:" + fromMailAddr
					+ " to:" + StringUtils.join((toMailAddrs), ";")
					+ " cc:" + StringUtils.join((ccMailAddrs), ";")
					+ " bcc:" + StringUtils.join((bccMailAddrs), ";")
					+ " subject:" + subject
					+ " text:" + text
					+ " inlines:" + StringUtils.join(inlineImages)
					+ " attachments:" + StringUtils.join(inlineImages));
		}
	}

	public int send(String mailsFilename, String encoding, String fromMailAddr, String text) throws MailException {
		int i = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mailsFilename), encoding));
			String line = reader.readLine(); // Skip the first line (header title) of csv
			for (i = 0; (line = reader.readLine()) != null; i ++) {
				String[] mail = line.split(",");
				send(fromMailAddr, 
					mail[TO_MAIL_ADDR].split(";"),
					mail[CC_MAIL_ADDR].split(";"),
					mail[BCC_MAIL_ADDR].split(";"),
					mail[SUBJECT],
					text != null ? text : mail[TEXT],
					mail.length <= INLINE ? new String[] {} : mail[INLINE].split(":"), 
					mail.length <= ATTACHMENT ? new String[] {} : mail[ATTACHMENT].split(":"));
			}
			reader.close();
		} catch(UnsupportedEncodingException e) {
			throw new MailException(e, mailsFilename);
		} catch(FileNotFoundException e) {
			throw new MailException(e, mailsFilename);
		} catch(IOException e) {
			throw new MailException(e, mailsFilename);
		}
		return i;
	}
}
