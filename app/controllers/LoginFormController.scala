package controllers

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader}

class LoginFormController @Inject() (config:Configuration, cc:ControllerComponents) extends AbstractController(cc) {
  def loginForm= Action {
    Ok(views.html.loginform.render)
  }

  def doLogin = Action {
    InternalServerError("not implemented")
  }
}
