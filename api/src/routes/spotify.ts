import querystring from "querystring";
import { randomUUID } from "crypto";
import z from "zod";
import { Plugin } from "../types";

const { SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET, SPOTIFY_REDIRECT_URI } = process.env;

export const spotify: Plugin = (server, _, done) => {
  let state = randomUUID();

  server.get("/spotify/login", (_, res) => {
    const scope = "user-read-recently-played";

    res.redirect(
      "https://accounts.spotify.com/authorize?" +
        querystring.stringify({
          response_type: "code",
          client_id: SPOTIFY_CLIENT_ID,
          scope: scope,
          redirect_uri: SPOTIFY_REDIRECT_URI,
          state: state,
        }),
    );
  });

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
        const { code, state: spotifyState } = req.query;

        if (spotifyState !== state) {
          return "State mismatch.";
        }

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

        state = randomUUID();

        return res.code(200).header("Content-Type", "application/json").send(data);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
