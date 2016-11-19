package controllers

import javax.inject.Inject

import model.{Db, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
  * Created by henri on 11/17/2016.
  */
class RestApi @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport with Secured {

  def users = Db.query[User].fetch()

  val userIdOnceConstraint: Constraint[String] = Constraint("constraints.useridonce")({
    plain =>
      if (Db.query[User].whereEqual("userId", plain).fetchOne().isDefined)
        Invalid(ValidationError("error.userIdOnce"))
      else
        Valid
  })

  val userIdOnce = nonEmptyText().verifying(userIdOnceConstraint)

  val userForm: Form[User] = Form {
    mapping(
      "userId" -> userIdOnce,
      "lastName" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "email" -> email,
      "pw" -> nonEmptyText
    )(User.apply)(User.unapply)
  }

  def addUser() = withAuth { userId => implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.app.adduser(userId, formWithErrors))
      },
      data => {
        Db.save(data)
        Ok(views.html.app.adduser(userId, success = true))
      }
    )
  }

  def getUsers = withAuth { userId => implicit request =>
    val users = Db.query[User].fetch()
    Ok(Json.toJson(users))
  }

  def delete(userId: String) = withAuth { userId => implicit request =>
    val user = Db.query[User].whereEqual("userId", userId).fetchOne()
    if (user.isDefined)
      Db.delete(user.get)
    Redirect(routes.Application.search())
  }

}
