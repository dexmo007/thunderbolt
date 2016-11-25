package controllers

import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject.Inject

import func.FunctionParser
import model.{Db, User}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Controller that handles all rest api calls
  */
class RestApi @Inject()(val messagesApi: MessagesApi, val configuration: Configuration)
  extends Controller with I18nSupport with Secured {

  implicit lazy val conf: Configuration = configuration

  val userIdOnceConstraint: Constraint[String] = Constraint("constraints.useridonce")({
    plain =>
      if (Db.query[User].whereEqual("userId", plain).fetchOne().isDefined)
        Invalid(ValidationError("error.userIdOnce"))
      else
        Valid
  })

  private val userIdOnce = nonEmptyText().verifying(userIdOnceConstraint)

  val userForm: Form[User] = Form {
    mapping(
      "userId" -> userIdOnce,
      "lastName" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "email" -> email,
      "pw" -> nonEmptyText
    )(User.apply)(User.unapply)
  }

  def addUser() = withAuth { userId =>
    implicit request =>
      userForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.app.adduser(formWithErrors))
        },
        data => {
          Db.save(data)
          Redirect(routes.Application.addUser()).flashing("success" -> Messages("add.success"))
        }
      )
  }

  def getUsers = withAuth { userId =>
    implicit request =>
      val users = Db.query[User].fetch()
      Ok(Json.toJson(users))
  }

  def delete(toDelete: String) = withAuth { userId =>
    implicit request =>
      val userOption = Db.query[User].whereEqual("userId", toDelete).fetchOne()
      if (userOption.isEmpty)
        Redirect(routes.Application.search())
      else {
        val user = userOption.get
        Db.delete(user)
        if (toDelete == userId)
          Redirect(routes.Auth.login()).withNewSession.flashing("success" -> Messages("delete.logout"))
        else {
          val msg = Messages("delete.success", s"'${user.userId}' (${user.formalName})")
          Redirect(routes.Application.search()).flashing("success" -> msg)
        }
      }
  }

  private val jsonConstraint: Constraint[String] = Constraint("constraints.json")({
    plain =>
      try {
        Json.parse(plain)
        Valid
      } catch {
        case _: Throwable => Invalid(ValidationError("error.json"))
      }
  })

  private val json = nonEmptyText().verifying(jsonConstraint)

  val jcgForm = Form {
    tuple(
      "json" -> json,
      "lang" -> nonEmptyText,
      "annotate" -> optional(text)
    )
  }

  def jcg = withAuth { userId =>
    implicit request =>
      val thisForm: Form[(String, String, Option[String])] = jcgForm.bindFromRequest
      if (thisForm.hasErrors) {
        BadRequest(views.html.app.jcg(form = thisForm))
      } else {
        val genClazz = JcgUtil
          .generators(thisForm("lang").value.get)
          .annotate(thisForm("annotate").value.isDefined)
          .generateClass(thisForm("json").value.get)
        Ok(views.html.app.jcg(form = thisForm, clazz = genClazz))
      }
  }

  //todo implement download
  def jcgDownload = withAuth { userId =>
    implicit request =>
      val form: Form[(String, String, Option[String])] = jcgForm.bindFromRequest
      if (form.hasErrors) {
        BadRequest(views.html.app.jcg(form = form))
      } else {
        val classes = JcgUtil
          .generators(form("lang").value.get)
          .annotate(form("annotate").value.isDefined)
          .generateClasses(form("json").value.get)

        val enumerator = Enumerator.outputStream { os =>
          val zip = new ZipOutputStream(os)
          classes.foreach { clazz =>
            zip.putNextEntry(new ZipEntry("zip/" + extractName(clazz)))
            zip.write(clazz.map(_.toByte).toArray)
            zip.closeEntry()
          }
          zip.close()
        }
        enumerator >>> Enumerator.eof
        Ok
      }
  }

  private def extractName(clazz: String): String = {
    val start = clazz.indexOf("public class") + 12
    val end = clazz.indexOf("{", start)
    clazz.substring(start, end).trim
  }

  def messages(id: String) = withAuth { userId =>
    implicit request =>
      Ok(Messages(id))
  }

  private val mathForm = Form {
    single(
      "input" -> nonEmptyText
    )
  }

  def xMath = withAuth { userId =>
    implicit request =>
      // todo contraint that checks function and catches exception, try evaluation first, if fails then parse as function, if that fails show error
      val form = mathForm.bindFromRequest
      if (form.hasErrors) {
        BadRequest(views.html.app.xmath(form))
      } else {
        val in = form("input").value.get
        val func = FunctionParser.parse(in)
        Ok(views.html.app.xmath(form, out = func))
      }

  }
}
