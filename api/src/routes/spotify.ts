import querystring from "querystring";
import { db } from "../db/db";
import { userTable } from "../db/schema";
import { eq, or } from "drizzle-orm";
import z from "zod";
import { Plugin } from "../types";

const { SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET, SPOTIFY_REDIRECT_URI } = process.env;

const x = `<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Authentication Success</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
            background-color: #003847;
            color: #00FC64;
            font-family: 'Arial', sans-serif;
        }

        #message-container {
            text-align: center;
            padding: 20px;
            border-radius: 10px;
            background-color: #001823;
        }
    </style>
</head>
<body>
    <div id="message-container">
        <p>Authenticated, you can return to the app</p>
    </div>
</body>
</html>`;

export const spotify: Plugin = (server, _, done) => {
  server.get(
    "/spotify/login/:identifier",
    {
      schema: {
        params: z.object({
          identifier: z.string(),
        }),
      },
    },
    (req, res) => {
      const scope = "user-read-recently-played user-read-private user-read-currently-playing user-top-read";

      const { identifier } = req.params;

      console.log(identifier);

      res.redirect(
        "https://accounts.spotify.com/authorize?" +
          querystring.stringify({
            response_type: "code",
            client_id: SPOTIFY_CLIENT_ID,
            scope: scope,
            redirect_uri: "http://10.0.2.2:8080/spotify/callback",
            state: identifier,
          }),
      );
    },
  );

  server.get(
    "/spotify/callback",
    {
      schema: {
        querystring: z.object({
          code: z.string(),
          state: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { code, state: identifier } = req.query;

        // if (spotifyState !== state) {
        //   return "State mismatch.";
        // }

        const resp = await fetch("https://accounts.spotify.com/api/token", {
          method: "POST",
          headers: {
            "content-type": "application/x-www-form-urlencoded",
            Authorization: "Basic " + Buffer.from(SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).toString("base64"),
          },
          body: querystring.stringify({
            code,
            redirect_uri: SPOTIFY_REDIRECT_URI,
            grant_type: "authorization_code",
          }),
        });

        if (!resp.ok) {
          const text = await resp.text();
          return res.code(resp.status).send(`Request failed with status ${resp.status} ${text}`);
        }

        const data = await resp.json();

        // const profile_pic = getProfilePic(data.access_token)
        var profile_pic = ""
        var starting = "Bearer "

        const response = await fetch('https://api.spotify.com/v1/me', {headers: {'Authorization': starting.concat(data.access_token)}})
        // console.log(response.text())
        const value = await response.json();

        const display_name = value["display_name"]

        if (value["images"].length > 0){
          profile_pic = value["images"][1]["url"]
        }
        else {
          profile_pic = "https://builtprefab.com/wp-content/uploads/2019/01/cropped-blank-profile-picture-973460_960_720-300x300.png"
        }

        await db
          .update(userTable)
          .set({
            spotifyAccessToken: data.access_token,
            spotifyRefreshToken: data.refresh_token,
            profilePicture: profile_pic,
            displayName: display_name
          })
          .where(or(eq(userTable.username, identifier), eq(userTable.id, identifier)));

        return res.code(200).header("Content-Type", "text/html").send(x);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
