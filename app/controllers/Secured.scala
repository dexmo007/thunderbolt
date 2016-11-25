package controllers

import play.api.mvc._
import play.api.mvc.Result

/**
  * Created by henri on 11/19/2016.
  */
trait Secured {

  def userId(request: RequestHeader): Option[String] = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login())

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(userId, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
}
