package controllers

import java.io.FileNotFoundException
import java.sql.SQLException

import com.fasterxml.jackson.core.JsonParseException
import context.CoreContext
import data.{RsaDecoder, RsaEncoder, RsaHelper}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import repository.{RsaRepository, User, UserInfo}
import utils.EmailHelper

import scala.io.Source


/**
  * * # Created by wacharint on 7/1/16.
  **/
object LoginApi extends Controller {

  //<editor-fold desc="Constants & Global variables">

  val LoginFailMessage = "Login Failed"
  val RegisterFailMessage = "Register Failed"
  val EmailExistingMessage = "Email Existing"
  val BadRequestMessage = "Bad Request"
  val OkMessage = "Ok"
  val ServerErrorMessage = "Server Error"
  val UserDoesNotExistMessage = "User is not exist"
  val KeyD = "ad8cemekse6eh1hrkp8j39kvb"
  val KeyN = "aq7k03xk4ouvzsrktw2lasctp"

  val ForgotPasswordHtmlTemplateFile = "./conf/forgotPassword.html"

  var OverrideContext: Option[CoreContext] = None
  var OverrideEmailHelper: Option[EmailHelper] = None

  val encoder = new RsaEncoder(BigInt("nz", 36), BigInt("aq7k03xk4ouvzsrktw2lasctp", 36))

  //</editor-fold>

  //<editor-fold desc="actions">

  def handShake = Action {

    implicit val coreContext: CoreContext = if(OverrideContext.isEmpty) new CoreContext else OverrideContext.get

    val helper = new RsaHelper
    val decoder = helper.getRsaDecoder
    val encoder = helper.getRsaEncode

    val rsaData = new RsaRepository {

      d = decoder.d.toString(36)
      e = encoder.e.toString(36)
      n = encoder.n.toString(36)
    }

    val id = rsaData.insert()

    Ok("""{"id":"%s","e":"%s","n":"%s"}""".format(rsaData.rsaUuid, encoder.e.toString(36), encoder.n.toString(36)))
  }

  def login(id: String) = Action{ request: Request[AnyContent] => {

    implicit val coreContext: CoreContext = if(OverrideContext.isEmpty) new CoreContext else OverrideContext.get

    val decryptedMg = decryptMsg(id, request)
    var loginIsFail = false

    if(decryptedMg.equals(BadRequestMessage) ){
      Ok(BadRequestMessage)
    } else {

      var jsValue: JsValue = null

      try {
        jsValue = Json.parse(decryptedMg)
      } catch {
        case e: JsonParseException => loginIsFail = true
      }

      if(loginIsFail) {
        Ok(LoginFailMessage)
      } else {

        var userName = jsValue.asInstanceOf[JsObject].value.find(p => p._1.equalsIgnoreCase("userName")).getOrElse("" -> "\"\"")._2.toString
        var password = jsValue.asInstanceOf[JsObject].value.find(p => p._1.equalsIgnoreCase("password")).getOrElse("" -> "\"\"")._2.toString
        userName = userName.substring(1, userName.length - 1)
        password = password.substring(1, password.length - 1)

        val userInfo = new UserInfo().get(Seq(("user_email", userName), ("password", encoder.encrypt(password)))).headOption.asInstanceOf[Option[UserInfo]]

        if (userInfo.isEmpty) {
          Ok(LoginFailMessage)
        } else {
          Ok(userInfo.head.userToken.toString)
        }
      }
    }
  }}

  def register(id: String) = Action{ request: Request[AnyContent] => {

    implicit val coreContext: CoreContext = if(OverrideContext.isEmpty) new CoreContext else OverrideContext.get
    var registerIsFail = false

    val decryptedMsg = decryptMsg(id, request)
    if(decryptedMsg.equals(BadRequestMessage)) {
      Ok(BadRequestMessage)
    } else {

      var reqJson: JsValue = null

      try {

        reqJson = Json.parse(decryptedMsg)
      } catch {
        case e: JsonParseException => registerIsFail = true
        case e: java.lang.Exception => registerIsFail = true
      }

      if(registerIsFail) {

        Ok(RegisterFailMessage)
      } else {

        var email = reqJson.asInstanceOf[JsObject].value.find( p => p._1.equalsIgnoreCase("email")).getOrElse("" -> "\"\"")._2.toString
        var passwd = reqJson.asInstanceOf[JsObject].value.find( p => p._1.equalsIgnoreCase("password")).getOrElse("" -> "\"\"")._2.toString

        email = email.substring(1, email.length -1)
        passwd = passwd.substring(1, passwd.length -1)

        if(new UserInfo().get(Seq("user_email" -> email)).nonEmpty) {

          Ok(EmailExistingMessage)
        } else if(email.length < 3 || passwd.length < 3) {

          Ok(RegisterFailMessage)
        } else {

          val newUser = new User() {
            descr = "Registered user"
          }
          val newUserInfo = new UserInfo() {

            userId = newUser.userId
            userEmail = email
            password = encoder.encrypt(passwd)
          }

          var msg = ""
          try {

            newUser.insert()
            newUserInfo.insert()
          } catch {
            case e: SQLException => {
              msg = e.getMessage()
              registerIsFail = true
            }
          }

          if(registerIsFail) {

            Ok(RegisterFailMessage)
//            Ok(msg)
          } else {

            Ok(newUserInfo.userToken.toString)
          }
        }
      }
    }
  } }

  def forgetPassword(id: String) = Action{ request: Request[AnyContent] => {

    implicit val context = if(OverrideContext.isEmpty) new CoreContext else OverrideContext.get
    val decryptedMsg = decryptMsg(id, request)
    var forgetPasswordIsFail = false

    if(decryptedMsg equals BadRequestMessage) {

      Ok(BadRequestMessage)
    } else {

      var jsonRequest: JsValue = null

      try {

        jsonRequest = Json.parse(decryptedMsg)
      } catch {
        case e: JsonParseException => forgetPasswordIsFail = true
      }

      if(forgetPasswordIsFail) {
        Ok(BadRequestMessage)
      } else {

        var email = jsonRequest.asInstanceOf[JsObject].value.find(
          j => j._1 equals "email").getOrElse(("email", ""))._2.toString

        if(email isEmpty) {

          Ok (BadRequestMessage)
        } else {

          val emailHelper = if (OverrideEmailHelper.isEmpty) {
            new EmailHelper
          } else {
            OverrideEmailHelper.get
          }

          var canReadMailTemplate = true
          var unknownException = false
          var mailTemplate = ""
          var exceptionMsg = ""

          try {
            mailTemplate = Source.fromFile(ForgotPasswordHtmlTemplateFile).mkString
          } catch {
            case e: FileNotFoundException => {
              canReadMailTemplate = false
              exceptionMsg = e.getMessage
            }
            case e: Exception => {
              unknownException = true
              exceptionMsg = e.getMessage
            }
          }

          if(!canReadMailTemplate) {

            // Send another mail warn myself
            Ok(ServerErrorMessage)
          } else {

            email = email.substring(1, email.size - 1)
            val userInfo = new UserInfo().get(Seq("user_email" -> email)).asInstanceOf[Seq[UserInfo]]

            if(userInfo.isEmpty){
              Ok(UserDoesNotExistMessage)
            } else {

              val encrytedPasswd = userInfo.head.password
              val decryptedPasswd = new RsaDecoder(BigInt(KeyD, 36), BigInt(KeyN, 36)).decrypt(encrytedPasswd)

              mailTemplate = mailTemplate.replace("{1}", decryptedPasswd)

              emailHelper.sendEmail(email, EmailHelper.From, EmailHelper.Subject, mailTemplate)
              Ok(OkMessage)
            }
          }
        }
      }
    }
  }}

  def test = Action {

    val emailHelper = new EmailHelper

    val htmlFile = Source.fromFile("./conf/forgotPassword.html").mkString

    emailHelper.sendEmail("wacharin.tangseree@gmail.com", EmailHelper.From, "Test subject", htmlFile)


    Ok("Ok")
  }

  //</editor-fold>

  //<editor-fold desc="Internal functions">

  def decryptMsg(id: String, request: Request[AnyContent])(implicit coreContext: CoreContext): String = {

    val rsaData = new RsaRepository().get(Seq(("rsa_uuid", id))).headOption.asInstanceOf[Option[RsaRepository]]
    var decryptFail = false

    if (rsaData.isEmpty) {
      BadRequestMessage
    } else {

      val decoder = new RsaDecoder(BigInt(rsaData.get.d, 36), BigInt(rsaData.get.n, 36))
      val textBody = request.body.asText

      if (textBody.isEmpty) {
        BadRequestMessage
      } else {

        var decryptedMsg = ""

        try {
          decryptedMsg = decoder.decrypt(textBody.get)
        } catch {
          case e: NumberFormatException => decryptFail = true
        }

        if(decryptFail) {
          BadRequestMessage
        } else {
          decryptedMsg
        }
      }
    }
  }

  //</editor-fold>
}
