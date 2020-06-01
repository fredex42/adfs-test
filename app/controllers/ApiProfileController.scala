package controllers

import com.nimbusds.jwt.JWTClaimsSet
import filters.JwtAuthenticationFilter
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request, Result}
import io.circe.syntax._
import io.circe.generic.auto._
import org.slf4j.LoggerFactory
import play.api.libs.circe.Circe
import play.api.libs.typedmap.TypedKey

import scala.jdk.CollectionConverters._

@Singleton
class ApiProfileController @Inject() (cc:ControllerComponents) extends AbstractController(cc) with Circe {
  private val logger = LoggerFactory.getLogger(getClass)

  def myProfile = Action { request=>
//    withLoginProfile(request) { profileContent =>
//      val returnMap = Map[String, Option[String]](
//        "displayName" -> Option(profileContent.getDisplayName),
//        "email" -> Option(profileContent.getEmail),
//        "surname" -> Option(profileContent.getFamilyName),
//        "forename" -> Option(profileContent.getFirstName),
//        "location" -> Option(profileContent.getLocation),
//        "username" -> Option(profileContent.getUsername),
//        "gender" -> Option(profileContent.getGender).map(_.toString),
//        "locale" -> Option(profileContent.getLocale).map(_.toString),
//        "pictureUri" -> Option(profileContent.getPictureUrl).map(_.toString),
//        "profileUrl" -> Option(profileContent.getProfileUrl).map(_.toString),
//        "job_title" -> Option(profileContent.getAttribute("job_title", classOf[java.util.ArrayList[String]]).asScala.mkString(",")),
//        "issuerEntityId" -> Option(profileContent.getIssuerEntityID),
//        "samlNameIDFormat" -> Option(profileContent.getSamlNameIdFormat),
//        "sessionIndex" -> Option(profileContent.getSessionIndex),
//        "authNContexts" -> Option(profileContent.getAuthnContexts).map(_.asScala.mkString(","))
//      )

//      Ok(returnMap.asJson)
    logger.debug(s"have claims? ${request.attrs.contains(JwtAuthenticationFilter.ClaimsAttributeKey)}")

    request.attrs.get(JwtAuthenticationFilter.ClaimsAttributeKey) match {
      case Some(claims)=>
        Ok(claims.toJSONObject.toJSONString)
      case None=>
        InternalServerError(Map("error"->"no_profile","detail"->"received no claims data").asJson)
    }

  }
}
