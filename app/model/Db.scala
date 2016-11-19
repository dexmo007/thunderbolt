package model

import sorm._

object Db extends Instance (
  entities = Seq(Entity[User]()),
  url = "jdbc:mysql://localhost:3306/doj-db",
  user = "root",
  password = "root") {

  def fetchUserById(userId: String): Option[User with Persisted] = {
    Db.query[User].whereEqual("userId", userId).fetchOne()
  }
}
