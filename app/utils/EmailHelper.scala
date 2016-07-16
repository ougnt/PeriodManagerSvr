package utils

import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage}
import javax.mail.{Message, Session}

/**
  * * # Created by wacharint on 7/6/16.
  **/
class EmailHelper {

  def sendEmail(to: String, from: String, subject: String, message: String) = {

    val properties = System.getProperties
    properties.setProperty("mail.smtp.host", EmailHelper.Host)
    properties.put("mail.smtp.starttls.enable", "true")

    val session = Session.getInstance(properties)

    val mimeMessage = new MimeMessage(session)
    mimeMessage setFrom new InternetAddress(from)
    mimeMessage setRecipient(Message.RecipientType.TO, new InternetAddress(to))
    mimeMessage setSubject subject

    val body = new MimeBodyPart
    body setText message

    mimeMessage setContent(message, """text/html""")

    val transport = session getTransport

    transport connect(EmailHelper.Host, EmailHelper.Port, EmailHelper.From, EmailHelper.Password)
    transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients)
  }
}

object EmailHelper {

  val Host = "smtp.gmail.com"
  val Port = 587
  val Password = "Apibkk123*"
  val From = "periodmanager.forgot@gmail.com"
  val Subject = "Password Reset Instruction"
}