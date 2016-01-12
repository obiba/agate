package org.obiba.agate.web.rest.ticket;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("/oauth2")
@Scope("request")
public class OAuthResource {

  @GET
  @Path("/authz")
  public Response authorize(@Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException {
    try {
      OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
      OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());

      //build response according to response_type
      String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

      OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
        .authorizationResponse(request, HttpServletResponse.SC_FOUND);

      // 1
      if(responseType.equals(ResponseType.CODE.toString())) {
        String authorizationCode = oAuthIssuer.authorizationCode();
        //database.addAuthCode(authorizationCode);
        builder.setCode(authorizationCode);
      }

      String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
      OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
      URI url = new URI(response.getLocationUri());
      return Response.status(response.getResponseStatus()).location(url).build();
    } catch(OAuthProblemException e) {
      throw new ForbiddenException();
    }
  }

  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/token")
  public Response access(@Context HttpServletRequest request) throws OAuthSystemException {
    try {
      OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
      OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

      // check if client id and secret are valid
      validateClient(oauthRequest.getClientId(), oauthRequest.getClientSecret());

      // do checking for different grant types
      if(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) {
        validateAuthCode(oauthRequest.getParam(OAuth.OAUTH_CODE));
      } else if(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) {
        validateUsernamePassword(oauthRequest.getUsername(), oauthRequest.getPassword());
      } else if(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString())) {
        // refresh token is not supported in this implementation
        throw new UnsupportedOperationException("Refresh token is not supported");
      }

      String accessToken = oauthIssuerImpl.accessToken();
      //database.addToken(accessToken);

      OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(accessToken)
        .setExpiresIn("3600").buildJSONMessage();
      return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

    } catch(OAuthProblemException e) {
      OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
      return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
    }
  }

  @GET
  @Produces("application/json")
  @Path("/resource")
  public Response get(@Context HttpServletRequest request) throws OAuthSystemException {
    try {
      // Make the OAuth Request out of this request
      OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
      // Get the access token
      String accessToken = oauthRequest.getAccessToken();

      // Validate the access token
      if(!validateAccessToken(accessToken)) {
        // Return the OAuth error message
        OAuthResponse oauthResponse = OAuthRSResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
          //.setRealm(Common.RESOURCE_SERVER_NAME)
          .setError(OAuthError.ResourceResponse.INVALID_TOKEN).buildHeaderMessage();

        return Response.status(Response.Status.UNAUTHORIZED)
          .header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
          .build();

      }
      
      return Response.status(Response.Status.OK).entity(accessToken).build();
    } catch(OAuthProblemException e) {
      // Check if the error code has been set
      // Build error response....
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }

  private void validateUsernamePassword(String username, String password) {

  }

  private void validateAuthCode(String code) {

  }

  private void validateClient(String clientId, String clientSecret) {

  }

  private boolean validateAccessToken(String accessToken) {
    return true;
  }
}
