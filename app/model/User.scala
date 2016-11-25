package model

import play.api.libs.json.{Json, OFormat}

/**
  * Created by henri on 11/16/2016.
  */

case class User(userId: String,
                lastName: String,
                firstName: String,
                email: String,
                pw: String
               ) {
  def fullName = s"$firstName $lastName"
  def formalName = s"$lastName, $firstName"
}

object User {
  implicit val personFormat: OFormat[User] = Json.format[User]
}
