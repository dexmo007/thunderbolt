package model

import play.api.libs.json.Json

/**
  * Created by henri on 11/16/2016.
  */

case class User (userId: String,
                 lastName: String,
                firstName:String,
                email: String,
                pw: String
               ) {

}

object User {
  implicit val personFormat = Json.format[User]
}
