package controllers

import javax.inject.{Inject, Singleton}
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.{AbstractController, ControllerComponents}
import io.circe.syntax._
import io.circe.generic.auto._
import org.pac4j.saml.profile.SAML2Profile
import play.api.libs.circe.Circe
import scala.jdk.CollectionConverters._

@Singleton
class ApiProfileController @Inject() (sessionStore: PlaySessionStore, cc:ControllerComponents) extends AbstractController(cc) with Circe {
  def myProfile = Action { request=>
    val webContext = new PlayWebContext(request, sessionStore)
    val profileManager = new ProfileManager[SAML2Profile](webContext)
    val profile = profileManager.get(true)

    val profileContent = profile.get()


    val returnMap = Map[String,Option[String]](
      "displayName"->Option(profileContent.getDisplayName),
      "email"->Option(profileContent.getEmail),
      "surname"->Option(profileContent.getFamilyName),
      "forename"->Option(profileContent.getFirstName),
      "location"->Option(profileContent.getLocation),
      "username"->Option(profileContent.getUsername),
      "gender"->Option(profileContent.getGender).map(_.toString),
      "locale"->Option(profileContent.getLocale).map(_.toString),
      "pictureUri"->Option(profileContent.getPictureUrl).map(_.toString),
      "profileUrl"->Option(profileContent.getProfileUrl).map(_.toString),
      "job_title"->Option(profileContent.getAttribute("job_title",classOf[java.util.ArrayList[String]]).asScala.mkString(",")),
      "issuerEntityId"->Option(profileContent.getIssuerEntityID),
      "samlNameIDFormat"->Option(profileContent.getSamlNameIdFormat),
      "sessionIndex"->Option(profileContent.getSessionIndex),
      "authNContexts"->Option(profileContent.getAuthnContexts).map(_.asScala.mkString(","))
    )

    Ok(returnMap.asJson)
  }
}
