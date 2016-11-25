package controllers

import javax.inject.Inject

import play.api.Configuration
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc._

class Application @Inject()(val messagesApi: MessagesApi, val configuration: Configuration)
  extends Controller with I18nSupport with Secured {

  implicit lazy val config: Configuration = configuration

  def search = withAuth { userId =>
    implicit request =>
      Ok(views.html.app.search())
  }

  def setLang() = withAuth { userId =>
    implicit request =>
      val lang = request.body.asText
      if (lang.isDefined) {
        Ok.withLang(Lang(lang.get))
      } else
        BadRequest
  }

  def addUser() = withAuth { userId =>
    implicit request =>
      Ok(views.html.app.adduser())
  }

  def jcg: EssentialAction = withAuth { userId =>
    implicit request =>
      Ok(views.html.app.jcg())
  }

  def translate = withAuth { userId =>
    implicit request =>
      Ok(views.html.app.translate())
  }

  def xMath = withAuth { userId =>
    implicit request =>
      Ok(views.html.app.xmath())
  }
}