package controllers

import java.time.Instant

import javax.inject.{Inject, Singleton}
import org.pac4j.core.profile.ProfileManager
import org.pac4j.play.PlayWebContext
import org.pac4j.saml.profile.SAML2Profile
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request, Result}
import io.circe.syntax._
import io.circe.generic.auto._
import org.pac4j.play.store.PlaySessionStore

import scala.jdk.CollectionConverters._
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

@Singleton
class JwtController @Inject() (config:Configuration,sessionStore:PlaySessionStore, cc:ControllerComponents) extends AbstractController(cc) {
  def withLoginProfile(request:Request[AnyContent])(block:SAML2Profile =>Result) = {
    val webContext = new PlayWebContext(request, sessionStore)
    val profileManager = new ProfileManager[SAML2Profile](webContext)
    if(profileManager.isAuthenticated) {
      val profile = profileManager.get(true)

      if(profile.isPresent){
        val profileContent = profile.get()
        block(profileContent)
      } else {
        Unauthorized(Map("error"->"unauthorized","detail"->"No profile found").asJson)
      }
    } else {
      Forbidden(Map("error"->"forbidden","detail"->"Not logged in").asJson)
    }
  }

  def myProfile = Action { request=>
    withLoginProfile(request) { profileContent =>
      val returnMap = Map[String, Option[String]](
        "displayName" -> Option(profileContent.getDisplayName),
        "email" -> Option(profileContent.getEmail),
        "surname" -> Option(profileContent.getFamilyName),
        "forename" -> Option(profileContent.getFirstName),
        "location" -> Option(profileContent.getLocation),
        "username" -> Option(profileContent.getUsername),
        "gender" -> Option(profileContent.getGender).map(_.toString),
        "locale" -> Option(profileContent.getLocale).map(_.toString),
        "pictureUri" -> Option(profileContent.getPictureUrl).map(_.toString),
        "profileUrl" -> Option(profileContent.getProfileUrl).map(_.toString),
        "job_title" -> Option(profileContent.getAttribute("job_title", classOf[java.util.ArrayList[String]]).asScala.mkString(",")),
        "issuerEntityId" -> Option(profileContent.getIssuerEntityID),
        "samlNameIDFormat" -> Option(profileContent.getSamlNameIdFormat),
        "sessionIndex" -> Option(profileContent.getSessionIndex),
        "authNContexts" -> Option(profileContent.getAuthnContexts).map(_.asScala.mkString(","))
      )

      Ok(returnMap.asJson)
    }
  }

  def getJwt = Action { request=>
    withLoginProfile(request) {profileContent =>
      //default to 8 hours or what the config tells us
      val jwtDuration = config.getOptional[Long]("jwt.validDuration").getOrElse(8*3600)
      val claim = JwtClaim(
        expiration=Some(Instant.now.plusSeconds(jwtDuration).getEpochSecond),
        issuedAt = Some(Instant.now.getEpochSecond),
        content = profileContent.getEmail,
      )

      val encodingKey = config.getOptional[String]("jwt.encodingSecret").getOrElse(config.get[String]("play.http.secret.key"))
      val token = JwtCirce.encode(claim,encodingKey,JwtAlgorithm.HS256)
      Ok(Map("status"->"ok","token"->token).asJson)
    }
  }
}
