# adfs-test

A working proof-of-concept for ADFS/bearer token auth for Play against ADFS

## What it is

This is a testing ground for better understanding ADFS/SSO with regard to both frontend and backend services.
It's using a local filter and the nimbus library to authenticate bearer tokens

## How to use it

So long as you have sbt installed, it's pretty simple to get the backend up and running. `sbt run` should do the trick.
All library dependencies are listed in `build.sbt`.

There is a frontend, but in this implementation it is redundant.  Still, if you want to build the frontend, 
you need node.js>=12 and npm installed:
```$bash
cd frontend
npm i
npm run dev

```

This will watch the files and rebuild as they are modified.

## How to set it up

This bearer-token-only implementation does not require any direct connection to the IdP (identity provider server).
It only requires the signing certificate that can be obtained from the identity provider together.

1. Get the signing certificate from your IdP and paste its contents into application.conf under auth.signingCertPem.
Include the BEGIN CERTIFICATE lines and all of the line-breaks.
2. Set up your allowed_hosts in application.conf if you are using a reverse-proxy for development.
3. Kick it off either with your IDE or by running `sbt run`.
4. The backend will expect an authorization header in incoming requests of the form "Authorization: Bearer {jwt}" where
jwt is a signed json web token.  The user's profile will be extracted from the claims section of the jwt.

