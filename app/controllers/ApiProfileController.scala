package controllers

import javax.inject.{Inject, Singleton}
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.{AbstractController, ControllerComponents}
import io.circe.syntax._
import io.circe.generic.auto._
import play.api.libs.circe.Circe

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

@Singleton
class ApiProfileController @Inject() (sessionStore: PlaySessionStore, cc:ControllerComponents) extends AbstractController(cc) with Circe {
  def myProfile = Action { request=>
    val webContext = new PlayWebContext(request, sessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val maybeContent = profileManager.get(true).toScala.map(profile=>Map(
      "email"->Option(profile.getEmail),
      "displayName"->Option(profile.getDisplayName),
      "family_name"->Option(profile.getFamilyName),
      "first_name"->Option(profile.getFirstName),
      "gender"->Option(profile.getGender).map(_.toString),
      "locale"->Option(profile.getLocale).map(_.toString),
      "location"->Option(profile.getLocation),
      "username"->Option(profile.getUsername),
      "job_title"->Option(profile.getAttribute("job_title",classOf[String]))
    ))

    Ok(maybeContent.asJson)
  }
}
