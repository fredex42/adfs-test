package filters

import java.time.Instant
import java.util.Date

import akka.stream.Materializer
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Filter, RequestHeader, ResponseHeader, Result, Results}
import play.api.{Configuration, Logging}
import play.api.libs.json.Json
import play.api.libs.typedmap.TypedKey

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object JwtAuthenticationFilter {
  final val ClaimsAttributeKey = TypedKey[JWTClaimsSet]("claims")
}

/**
 * simple filter to perform JWT validation against an incoming bearer token in the Authorization header
 * if validation succeeds then the request is passed along, with an extra attribute called "claims"
 * containing a JWTClaimsSet.
 * if it fails then a 403 is returned and prcessing stops
 * @param config application configuration from injector
 * @param mat implicitly provided materializer from injector
 * @param ec implicitly provided execution context from injector
 */
@Singleton
class JwtAuthenticationFilter @Inject() (config:Configuration)(implicit val mat:Materializer, ec:ExecutionContext) extends Filter with Logging with Results  {
  //see https://stackoverflow.com/questions/475074/regex-to-parse-or-validate-base64-data
  //it is not the best option but is the simplest that will work here
  //private val authXtractor = "^Bearer\\s+([a-zA-Z0-9+/.]={0,3})?$".r
  private val authXtractor = "^Bearer\\s+([a-zA-Z0-9+/._-]*={0,3})$".r
  private val (signingKey, verifier) = loadInKey()

  def extractAuthorization(fromString:String) =
    fromString match {
      case authXtractor(token)=>
        logger.debug("found valid base64 bearer")
        Some(token)
      case _=>
        logger.warn("no bearer token found or it failed to validate")
        None
    }

  //this fails if it can't load, deliberately; it is called at init so should block the server from
  //initialising. This is desired fail-fast behaviour
  def loadInKey() = {
    val pemCertData = config.get[String]("auth.tokenSigningCert")
    val jwk = JWK.parseFromPEMEncodedX509Cert(pemCertData)
    val verifier = new RSASSAVerifier(jwk.toRSAKey)
    (jwk, verifier)
  }

  /**
   * try to validate the given token with the key provided
   * returns a JWTClaimsSet if successful
   * the Try is cast to a Future to make composition easier
   * @param token JWT token to verify
   * @return a Future, containing a JWTClaimsSet. The Future fails if it can't be verified
   */
  def validateToken(token:String) = Future.fromTry({
    logger.debug(s"validating token $token")
    Try { SignedJWT.parse(token) }.flatMap(signedJWT=>
      if(signedJWT.verify(verifier)) {
        logger.debug("verified JWT")
        Success(signedJWT.getJWTClaimsSet)
      } else {
        Failure(new RuntimeException("Failed to validate JWT)"))
      }
    )
  })

  def checkExpiry(claims:JWTClaimsSet) = {
    if(claims.getExpirationTime.before(Date.from(Instant.now()))) {
      logger.debug(s"JWT was valid but expired at ${claims.getExpirationTime.formatted("YYYY-MM-dd HH:mm:ss")}")
      Future.failed(new RuntimeException("Token has expired"))
    } else {
      Future(claims)
    }
  }

  override def apply(nextFilter: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    if(rh.method=="OPTIONS") {  //always allow options requests
      return nextFilter(rh)
    }

    rh.headers.get("Authorization") match {
      case Some(authValue)=>
        extractAuthorization(authValue)
          .map(validateToken)
          .getOrElse(Future.failed(new RuntimeException("No authorization was present")))
          .flatMap(checkExpiry)
          .flatMap(claims=>{
            val updatedRequestHeader = rh.addAttr(JwtAuthenticationFilter.ClaimsAttributeKey, claims)
            nextFilter(updatedRequestHeader)
          }).recover({
          case err:Throwable=>
            logger.error("Could not complete authorization: ", err)
            Unauthorized(Json.obj("status"->"unauthorized"))
        })
      case None=>
        logger.error("Attempt to access without authorization")
        Future(Unauthorized(Json.obj("status"->"unauthorized")))
    }
  }
}
