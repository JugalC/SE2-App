import { db } from "../db/db";
import { friendshipTable, getUserSchema, insertUserSchema, postTable, userTable } from "../db/schema";
import { randomUUID, timingSafeEqual } from "crypto";
import { generateSalt, hash } from "../lib/hashing";
import { z } from "zod";
import { eq, or } from "drizzle-orm";
import { Plugin, paginationSchema, searchSchema } from "../types";
import { generateLikeFilters } from "../lib/generateLikeFilters";
import { encrypt } from "../lib/encryption";
import { CONNREFUSED } from "dns";
import { asc, desc } from 'drizzle-orm';
import { count, sql } from 'drizzle-orm';


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

        const profilePicture = ""
        var createdAt = new Date()

        await db.insert(userTable).values({
          id,
          ...body,
          passwordHash,
          salt,
          profilePicture,
          createdAt
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
    "/profile_info/:identifier",
    {
      schema: {
        params: z.object({
          identifier: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        console.log("Request Made")
        const { identifier } = req.params;

        const user = await db.query.userTable.findFirst({
          where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
        });

        const posts = await db.select().from(postTable).where(eq(postTable.userId, identifier)).orderBy(desc(postTable.name)).limit(3);

        var previous_posts = [{}]

        if ((posts.length) == 3) {
          previous_posts = [
            {name: posts[0].name, album_name: posts[0].albumName, artists: posts[0].artists, image_url: posts[0].imageUrl, caption: "Today"},
            {name: posts[1].name, album_name: posts[1].albumName, artists: posts[1].artists, image_url: posts[1].imageUrl, caption: "Yesterday"},
            {name: posts[2].name, album_name: posts[2].albumName, artists: posts[2].artists, image_url: posts[2].imageUrl, caption: "2 Days Ago"}
          ]
        }

        if ((posts.length) == 2) {
          previous_posts = [
            {name: posts[0].name, album_name: posts[0].albumName, artists: posts[0].artists, image_url: posts[0].imageUrl, caption: "Today"},
            {name: posts[1].name, album_name: posts[1].albumName, artists: posts[1].artists, image_url: posts[1].imageUrl, caption: "Yesterday"}
          ]
        }

        if ((posts.length) == 1) {
          previous_posts = [
            {name: posts[0].name, album_name: posts[0].albumName, artists: posts[0].artists, image_url: posts[0].imageUrl, caption: "Today"},
          ]
        }

        if ((posts.length) == 0) {
          previous_posts = [
            {name: "No Posts", album_name: "Wait for the next", artists: "Wait for Daily Post", image_url: "https://en.wikipedia.org/wiki/File:Color_icon_gray_v2.svg", caption: "No Posts"},
          ]
        }

        const friends_number = await db.select({ count: count() }).from(friendshipTable).where(or(eq(friendshipTable.userId1, identifier), eq(friendshipTable.userId2, identifier)));
        const friends_num = (friends_number[0]["count"])

        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        const first_name = user["firstName"]
        const username = user["username"]
        const profile_pic = user["profilePicture"]
        const created = user["createdAt"]
        const spotify_name = user["displayName"]
        
        // const previous_posts = [
        //   {name: "Keep The Family Close", album_name: "Views", artists: "Drake", image_url: "https://i.scdn.co/image/ab67616d00001e029416ed64daf84936d89e671c", caption: "Today"},
        //   {name: "Out of Time", album_name: "The Highlights (Deluxe)", artists: "The Weeknd", image_url: "https://i.scdn.co/image/ab67616d00001e02c87bfeef81a210ddb7f717b5", caption: "Yesterday"},
        // ]

        return res.code(200).send(
          { first_name: first_name, username: username, spotify_name: spotify_name, friends_num: friends_num, profile_pic: profile_pic, created: created, previous_posts: previous_posts}
        );
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/profile_pic/:identifier",
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
        var starting = "Bearer "

        const response = await fetch('https://api.spotify.com/v1/me', {headers: {'Authorization': starting.concat(access_token)}})
        // console.log(response.text())
        const value = await response.json();

        if (value["images"].length > 0){
          console.log(value["images"][1]["url"])
        }
        else {
          console.log("https://builtprefab.com/wp-content/uploads/2019/01/cropped-blank-profile-picture-973460_960_720-300x300.png")
        }

        return res.code(200).send({ ...user, passwordHash: undefined, salt: undefined });
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
              Authorization: "Basic " + Buffer.from(process.env.SPOTIFY_CLIENT_ID + ":" + process.env.SPOTIFY_CLIENT_SECRET).toString("base64"),
            },
            body: new URLSearchParams({
              grant_type: 'refresh_token',
              refresh_token: reset_token,
              client_id: process.env.SPOTIFY_CLIENT_ID
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

            const recently_played_song = value["items"][0]
          
            let all_artists = []
            for (const artist_obj of recently_played_song["track"]["album"]["artists"]) { all_artists.push(artist_obj["name"]) }
            var artists = all_artists.join(',');
            console.log(artists)

            var imageUrl = value["items"][0]["track"]["album"]["images"][1]["url"]
            var albumName = value["items"][0]["track"]["album"]["name"]
            var name = value["items"][0]["track"]["name"]

            const id = randomUUID();
            var spotifyTrackId = recently_played_song["track"]["id"]
            var durationMs = recently_played_song["track"]["duration_ms"]
            var spotifyUrl = recently_played_song["track"]["external_urls"]["spotify"]
            var userId = identifier
            var listenedAt = new Date(recently_played_song["played_at"])
            var createdAt = new Date()

            var resp_obj = {"id": id, "spotify_track_id": spotifyTrackId, "name": name, "album_name": albumName, 
            "artists": artists, "duration_ms": durationMs, "image_url": imageUrl, "spotify_url": spotifyUrl,
            "user_id": userId, "listened_at": listenedAt, "created_at": createdAt}

            console.log(resp_obj)

            await db.insert(postTable).values({
              id,
              spotifyTrackId,
              name,
              albumName,
              artists,
              durationMs,
              imageUrl,
              spotifyUrl,
              userId,
              listenedAt,
              createdAt
            });

            return res.code(200).send(resp_obj);

        }


        else {
          console.log("TOken is goood")
        
          const recently_played_song = value["items"][0]
          
          let all_artists = []
          for (const artist_obj of recently_played_song["track"]["album"]["artists"]) { all_artists.push(artist_obj["name"]) }
          var artists = all_artists.join(',');
          console.log(artists)

          var imageUrl = value["items"][0]["track"]["album"]["images"][1]["url"]
          var albumName = value["items"][0]["track"]["album"]["name"]
          var name = value["items"][0]["track"]["name"]

          const id = randomUUID();
          var spotifyTrackId = recently_played_song["track"]["id"]
          var durationMs = recently_played_song["track"]["duration_ms"]
          var spotifyUrl = recently_played_song["track"]["external_urls"]["spotify"]
          var userId = identifier
          var listenedAt = new Date(recently_played_song["played_at"])
          var createdAt = new Date()

          var resp_obj_2 = {"id": id, "spotify_track_id": spotifyTrackId, "name": name, "album_name": albumName, 
          "artists": artists, "duration_ms": durationMs, "image_url": imageUrl, "spotify_url": spotifyUrl,
          "user_id": userId, "listened_at": listenedAt, "created_at": createdAt}

          console.log(resp_obj_2)

          await db.insert(postTable).values({
            id,
            spotifyTrackId,
            name,
            albumName,
            artists,
            durationMs,
            imageUrl,
            spotifyUrl,
            userId,
            listenedAt,
            createdAt
          });

         

          // console.log(resp_obj_2)
          return res.code(200).send(resp_obj_2);
          // return res.code(200).send(recently_played_song)
        }
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
