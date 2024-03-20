import { db } from "../db/db";
import { getUserSchema, insertUserSchema, userTable } from "../db/schema";
import { randomUUID, timingSafeEqual } from "crypto";
import { generateSalt, hash } from "../lib/hashing";
import { z } from "zod";
import { eq, or } from "drizzle-orm";
import { Plugin, paginationSchema, searchSchema } from "../types";
import { generateLikeFilters } from "../lib/generateLikeFilters";
<<<<<<< Updated upstream
import { encrypt } from "../lib/encryption";
=======
import { CONNREFUSED } from "dns";
>>>>>>> Stashed changes

export const users: Plugin = (server, _, done) => {
  server.post(
    "/user",
    {
      schema: {
        body: insertUserSchema,
      },
    },
    async (req, res) => {
      try {
        const salt = await generateSalt();
        const password = req.body.password;
        const passwordHash = (await hash(password, salt)).toString("hex");
        const body = { ...req.body, password: undefined };

        const id = randomUUID();

        await db.insert(userTable).values({
          id,
          ...body,
          passwordHash,
          salt,
        });

        return res.code(200).send({id, ...body, passwordHash: undefined, salt: undefined, token: encrypt(`${body.username}:${password}`) });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.post(
    "/login/:username",
    {
      schema: {
        params: z.object({
          username: z.string(),
        }),
        body: z.object({
          password: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { username } = req.params;
        const { password } = req.body;

        const user = await db.query.userTable.findFirst({
          where: eq(userTable.username, username),
        });

        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        const passwordHash = await hash(password, user.salt);

        if (!timingSafeEqual(passwordHash, Buffer.from(user.passwordHash, "hex"))) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        return res
          .code(200)
          .send({ ...user, token: encrypt(`${username}:${password}`), passwordHash: undefined, salt: undefined });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/user/:identifier",
    {
      schema: {
        params: z.object({
          identifier: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { identifier } = req.params;

        const user = await db.query.userTable.findFirst({
          where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
        });

        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        return res.code(200).send({ ...user, passwordHash: undefined, salt: undefined });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.patch(
    "/user/notificationToken/:identifier",
    {
      schema: {
        params: z.object({
          identifier: z.string(),
        }),
        body: z.object({
          androidRegistrationToken: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { identifier } = req.params;

        const user = await db.query.userTable.findFirst({
          where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
        });

        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        const androidRegistrationToken = req.body.androidRegistrationToken;

        await db.update(userTable).set({
          androidRegistrationToken: androidRegistrationToken
        }).where(eq(userTable.id, user.id));

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    }
  );

  server.get(
    "/user/spotifyauth/:identifier",
    {
      schema: {
        params: z.object({
          identifier: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { identifier } = req.params;

        const user = await db.query.userTable.findFirst({
          where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
        });

        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        return res.code(200).send({ authenticated: user.spotifyAccessToken !== null });
        //return res.code(200).send({ ...user, passwordHash: undefined, salt: undefined });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/users",
    {
      schema: {
        querystring: getUserSchema.and(paginationSchema).and(searchSchema),
      },
    },
    async (req, res) => {
      try {
        const { firstName, lastName, page, limit, search } = req.query;

        const where =
          search != ""
            ? generateLikeFilters([
                {
                  col: userTable.firstName,
                  val: search,
                },
                {
                  col: userTable.lastName,
                  val: search,
                },
                {
                  col: userTable.username,
                  val: search,
                },
              ])
            : generateLikeFilters([
                {
                  col: userTable.firstName,
                  val: firstName,
                },
                {
                  col: userTable.lastName,
                  val: lastName,
                },
              ]);

        const users = await db
          .select()
          .from(userTable)
          .limit(limit)
          .offset(page * limit)
          .where(where);

        return res.code(200).send(users);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/most_recent_song/:identifier",
    {
      schema: {
        params: z.object({
          identifier: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { identifier } = req.params;

        const user = await db.query.userTable.findFirst({
          where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
        });

        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }
        var access_token = user["spotifyAccessToken"] || "None"
        var reset_token = user["spotifyRefreshToken"] || "None"

        var starting = "Bearer "

        const response = await fetch('https://api.spotify.com/v1/me/player/recently-played', {headers: {'Authorization': starting.concat(access_token)}})
        // console.log(response.text())
        const value = await response.json();
        
        if (Object.keys(value)[0] === "error") {
          console.log("Need to refresh token")
          
          const url = "https://accounts.spotify.com/api/token";

          const payload = {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
              Authorization: "Basic " + Buffer.from("68edc012e9bf47b7a7769a372b705080" + ":" + "e5ea9469569c443e83a2913bd3ad2bf7").toString("base64"),
            },
            body: new URLSearchParams({
              grant_type: 'refresh_token',
              refresh_token: reset_token,
              client_id: "68edc012e9bf47b7a7769a372b705080"
            }),
          }
          const refresh_response = await fetch(url, payload);
          const refresh_response_json = await refresh_response.json();

          console.log(refresh_response_json["access_token"])

          await db.update(userTable)
            .set({ spotifyAccessToken: refresh_response_json["access_token"] })
            .where(eq(userTable.id, identifier));


            const user = await db.query.userTable.findFirst({
              where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
            });
    
            if (!user) {
              return res.code(404).send({ error: "User not found with given parameters." });
            }
            var access_token = user["spotifyAccessToken"] || "None"
            var reset_token = user["spotifyRefreshToken"] || "None"
    
            var starting = "Bearer "
    
            const response = await fetch('https://api.spotify.com/v1/me/player/recently-played', {headers: {'Authorization': starting.concat(access_token)}})
            // console.log(response.text())
            const value = await response.json();

            var artist = value["items"][0]["track"]["album"]["artists"][0]["name"]
            var album_art = value["items"][0]["track"]["album"]["images"][1]["url"]
            var album_name = value["items"][0]["track"]["album"]["name"]
            var track_name = value["items"][0]["track"]["name"]

            var resp_obj = {"artist": artist, "album_art": album_art, "album_name": album_name, "track_name": track_name}
            console.log(resp_obj)
            return res.code(200).send(resp_obj);

        }


        else {
          console.log("TOken is goood")
          // console.log(value["items"][0])
          var artist = value["items"][0]["track"]["album"]["artists"][0]["name"]
          var album_art = value["items"][0]["track"]["album"]["images"][1]["url"]
          var album_name = value["items"][0]["track"]["album"]["name"]
          var track_name = value["items"][0]["track"]["name"]

          var resp_obj = {"artist": artist, "album_art": album_art, "album_name": album_name, "track_name": track_name}
          console.log(resp_obj)
          return res.code(200).send(resp_obj);
        }
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
