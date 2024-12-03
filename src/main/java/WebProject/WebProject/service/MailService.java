package WebProject.WebProject.service;

import WebProject.WebProject.model.Mail;

public interface MailService 
{
	public void sendEmail(Mail mail);

	public void sendEmailNotification(Mail mail) throws Exception;
}
