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
import { asc, desc } from "drizzle-orm";
import { count, sql } from "drizzle-orm";
import { getSystemErrorMap } from "util";

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

        const profilePicture = "";
        const createdAt = new Date();

        await db.insert(userTable).values({
          id,
          ...body,
          passwordHash,
          salt,
          profilePicture,
          createdAt,
        });

        return res.code(200).send({
          id,
          ...body,
          passwordHash: undefined,
          salt: undefined,
          token: encrypt(`${body.username}:${password}`),
        });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.post(
    "/user/update_user",
    {
      schema: {
        body: z.object({
          currUsername: z.string(),
          newUsername: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { currUsername, newUsername } = req.body;

        // check if newUsername already exists in the database
        const user = await db.query.userTable.findFirst({
          where: eq(userTable.username, newUsername),
        });

        // if user exists, return error
        if (user) {
          return res.code(400).send({ error: "User already exists with given parameters." });
        }

        // update user with new username
        await db
          .update(userTable)
          .set({
            username: newUsername,
          })
          .where(eq(userTable.username, currUsername));

        return res.code(200).send({ newUsername: newUsername });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.post(
    "/user/update_password",
    {
      schema: {
        body: z.object({
          username: z.string(),
          newPassword: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { username, newPassword } = req.body;

        // check if user exists in DB
        const user = await db.query.userTable.findFirst({
          where: eq(userTable.username, username),
        });

        // if user doesn't exist, return error
        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        // generate new salt and hash for new password
        const salt = await generateSalt();
        const passwordHash = (await hash(newPassword, salt)).toString("hex");

        // update user with new password
        await db
          .update(userTable)
          .set({
            passwordHash,
            salt,
          })
          .where(eq(userTable.username, username));

        return res.code(200).send({ message: "Password updated successfully." });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.post(
    "/user/update_profile_picture",
    {
      schema: {
        body: z.object({
          username: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { username } = req.body;

        // check if user exists in DB
        const user = await db.query.userTable.findFirst({
          where: eq(userTable.username, username),
        });

        // if user doesn't exist, return error
        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        // let url_default =
        // "https://builtprefab.com/wp-content/uploads/2019/01/cropped-blank-profile-picture-973460_960_720-300x300.png";

        // // update user with new profile picture
        // await db
        //   .update(userTable)
        //   .set({
        //     profilePicture: "",
        //   })
        //   .where(eq(userTable.username, username));

        const access_token = user["spotifyAccessToken"] || "None";
        const reset_token = user["spotifyRefreshToken"] || "None";
        const starting = "Bearer ";

        const response = await fetch("https://api.spotify.com/v1/me", {
          headers: { Authorization: starting.concat(access_token) },
        });

        const value = await response.json();

        let refresh_flag = false;

        if (Object.keys(value)[0] === "error") {
          console.log("Need to refresh token");
          refresh_flag = true;

          const url = "https://accounts.spotify.com/api/token";

          const payload = {
            method: "POST",
            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
              Authorization:
                "Basic " +
                Buffer.from(process.env.SPOTIFY_CLIENT_ID + ":" + process.env.SPOTIFY_CLIENT_SECRET).toString("base64"),
            },
            body: new URLSearchParams({
              grant_type: "refresh_token",
              refresh_token: reset_token,
              client_id: process.env.SPOTIFY_CLIENT_ID,
            }),
          };
          const refresh_response = await fetch(url, payload);
          const refresh_response_json = await refresh_response.json();

          console.log(refresh_response_json["access_token"]);

          await db
            .update(userTable)
            .set({
              spotifyAccessToken: refresh_response_json["access_token"],
              spotifyRefreshToken: refresh_response_json["refresh_token"],
            })
            .where(eq(userTable.username, username));
        }

        if (refresh_flag) {
          const user = await db.query.userTable.findFirst({
            where: eq(userTable.username, username),
          });

          if (!user) {
            return res.code(404).send({ error: "User not found with given parameters." });
          }

          const access_token = user["spotifyAccessToken"] || "None";
          const starting = "Bearer ";

          const response = await fetch("https://api.spotify.com/v1/me", {
            headers: { Authorization: starting.concat(access_token) },
          });

          const value = await response.json();

          let profile_pic = "";
          if (value["images"].length > 0) {
            profile_pic = value["images"][1]["url"];
          } else {
            profile_pic =
              "https://builtprefab.com/wp-content/uploads/2019/01/cropped-blank-profile-picture-973460_960_720-300x300.png";
          }

          await db
            .update(userTable)
            .set({
              profilePicture: profile_pic,
            })
            .where(eq(userTable.username, username));

          return res.code(200).send({ message: "Profile picture updated successfully." });
        } else {
          let profile_pic = "";
          if (value["images"].length > 0) {
            profile_pic = value["images"][1]["url"];
          } else {
            profile_pic =
              "https://builtprefab.com/wp-content/uploads/2019/01/cropped-blank-profile-picture-973460_960_720-300x300.png";
          }

          await db
            .update(userTable)
            .set({
              profilePicture: profile_pic,
            })
            .where(eq(userTable.username, username));

          return res.code(200).send({ message: "Profile picture updated successfully." });
        }
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
    "/feed/:identifier",
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

        interface resultsObj {
          id: string;
          name: string;
          albumName: string;
          artists: string;
          imageUrl: string;
          userId: string;
          username: string;
          profilePicture: string;
          spotifyUrl: string;
        }

        const results: resultsObj[] = await db.all(sql`
          WITH RankedPosts AS (
            SELECT
              *,
              ROW_NUMBER() OVER (
                PARTITION BY
                  user_id
                ORDER BY
                  listened_at DESC
              ) AS RowNum
            FROM
              post
          )
          SELECT
            rp.id,
            rp.name,
            rp.album_name AS albumName,
            rp.artists,
            rp.image_url AS imageUrl,
            rp.user_id AS userId,
            ut.username,
            ut.profile_picture AS profilePicture,
            rp.spotify_url AS spotifyUrl
          FROM
            RankedPosts rp
            INNER JOIN user ut ON rp.user_id = ut.id
          WHERE
            RowNum = 1;
        `);

        interface friendsObj {
          other_user_id: string;
        }

        const friends: friendsObj[] = await db.all(sql`
          SELECT user_id_1 AS other_user_id
          FROM friendship
          WHERE user_id_2 = ${identifier}
          UNION
          SELECT user_id_2 AS other_user_id
          FROM friendship
          WHERE user_id_1 = ${identifier};
        `);

        const filter_list = [];
        for (let i = 0; i < friends.length; i++) {
          filter_list.push(friends[i]["other_user_id"]); // Access each object using array indexing
        }
        filter_list.push(identifier);

        const final_posts = [];
        for (let x = 0; x < results.length; x++) {
          if (filter_list.includes(results[x]["userId"])) final_posts.push(results[x]); // Access each object using array indexing
        }

        debugger;
        return res.code(200).send({ posts: final_posts });
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

        await db
          .update(userTable)
          .set({
            androidRegistrationToken: androidRegistrationToken,
          })
          .where(eq(userTable.id, user.id));

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
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
        const { identifier } = req.params;

        const user = await db.query.userTable.findFirst({
          where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
        });

        const posts = await db
          .select()
          .from(postTable)
          .where(eq(postTable.userId, identifier))
          .orderBy(desc(postTable.name))
          .limit(3);

        const previousPosts = [];

        if (posts.length === 0) {
          previousPosts.push({
            name: "No Posts",
            albumName: "Wait for the next",
            artists: "Wait for Daily Post",
            imageUrl: "https://en.wikipedia.org/wiki/File:Color_icon_gray_v2.svg",
            caption: "No Posts",
          })
        }
        
        if (posts.length > 0) {
          previousPosts.push({
            id: posts[0].id,
            name: posts[0].name,
            albumName: posts[0].albumName,
            artists: posts[0].artists,
            imageUrl: posts[0].imageUrl,
            visible: posts[0].visible,
            caption: "Today",
          })
        }

        if (posts.length > 1) {
          previousPosts.push({
            id: posts[1].id,
            name: posts[1].name,
            albumName: posts[1].albumName,
            artists: posts[1].artists,
            imageUrl: posts[1].imageUrl,
            visible: posts[1].visible,
            caption: "Yesterday",
          })
        }

        if (posts.length > 2) {
          previousPosts.push({
            id: posts[2].id,
            name: posts[2].name,
            albumName: posts[2].albumName,
            artists: posts[2].artists,
            imageUrl: posts[2].imageUrl,
            visible: posts[2].visible,
            caption: "2 Days Ago",
          })
        }

        const friends_number = await db
          .select({ count: count() })
          .from(friendshipTable)
          .where(or(eq(friendshipTable.userId1, identifier), eq(friendshipTable.userId2, identifier)));
        const friendsNum = friends_number[0]["count"];

        if (!user) {
          return res.code(404).send({ error: "User not found with given parameters." });
        }

        const firstName = user["firstName"];
        const username = user["username"];
        const profilePic = user["profilePicture"];
        let created = user["createdAt"];

        if (created == null) {
          created = new Date();
        }

        const spotifyName = user["displayName"];

        // const previous_posts = [
        //   {name: "Keep The Family Close", album_name: "Views", artists: "Drake", image_url: "https://i.scdn.co/image/ab67616d00001e029416ed64daf84936d89e671c", caption: "Today"},
        //   {name: "Out of Time", album_name: "The Highlights (Deluxe)", artists: "The Weeknd", image_url: "https://i.scdn.co/image/ab67616d00001e02c87bfeef81a210ddb7f717b5", caption: "Yesterday"},
        // ]

        return res.code(200).send({
          profile: {
            firstName,
            username,
            spotifyName,
            friendsNum,
            profilePic,
            created,
          },
          posts: previousPosts,
        });
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

        const access_token = user["spotifyAccessToken"] || "None";
        const starting = "Bearer ";

        const response = await fetch("https://api.spotify.com/v1/me", {
          headers: { Authorization: starting.concat(access_token) },
        });

        const value = await response.json();

        if (value["images"].length > 0) {
          console.log(value["images"][1]["url"]);
        } else {
          console.log(
            "https://builtprefab.com/wp-content/uploads/2019/01/cropped-blank-profile-picture-973460_960_720-300x300.png",
          );
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
        const access_token = user["spotifyAccessToken"] || "None";
        const reset_token = user["spotifyRefreshToken"] || "None";
        const starting = "Bearer ";

        const response = await fetch("https://api.spotify.com/v1/me/player/recently-played", {
          headers: { Authorization: starting.concat(access_token) },
        });
        // console.log(response.text())
        const value = await response.json();

        if (Object.keys(value)[0] === "error") {
          console.log("Need to refresh token");

          const url = "https://accounts.spotify.com/api/token";

          const payload = {
            method: "POST",
            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
              Authorization:
                "Basic " +
                Buffer.from(process.env.SPOTIFY_CLIENT_ID + ":" + process.env.SPOTIFY_CLIENT_SECRET).toString("base64"),
            },
            body: new URLSearchParams({
              grant_type: "refresh_token",
              refresh_token: reset_token,
              client_id: process.env.SPOTIFY_CLIENT_ID,
            }),
          };
          const refresh_response = await fetch(url, payload);
          const refresh_response_json = await refresh_response.json();

          console.log(refresh_response_json["access_token"]);

          await db
            .update(userTable)
            .set({ spotifyAccessToken: refresh_response_json["access_token"] })
            .where(eq(userTable.id, identifier));

          const user = await db.query.userTable.findFirst({
            where: or(eq(userTable.username, identifier), eq(userTable.id, identifier)),
          });

          if (!user) {
            return res.code(404).send({ error: "User not found with given parameters." });
          }
          const access_token = user["spotifyAccessToken"] || "None";

          const starting = "Bearer ";

          const response_test = await fetch("https://api.spotify.com/v1/me/player/currently-playing", {
            headers: { Authorization: starting.concat(access_token) },
          });

          let use_currently_playing = true
          // let value_test = {'item': ""}

          try {
            const value_test = await response_test.json()
          }
          catch (SyntaxError) {
            use_currently_playing = false
          }

          if (use_currently_playing) {
            const response = await fetch("https://api.spotify.com/v1/me/player/currently-playing", {
              headers: { Authorization: starting.concat(access_token) },
            });
            // console.log(response.text())
            const value = await response.json();
            const currently_played_song = value["item"];

            const all_artists = [];
            for (const artist_obj of currently_played_song["artists"]) {
              all_artists.push(artist_obj["name"]);
            }
            const artists = all_artists.join(",");

            const imageUrl = value["item"]["album"]["images"][1]["url"];
            const albumName = value["item"]["album"]["name"];
            const name = value["item"]["name"];

            const id = randomUUID();
            const spotifyTrackId = currently_played_song["id"];
            const durationMs = currently_played_song["duration_ms"];
            const spotifyUrl = currently_played_song["external_urls"]["spotify"];
            const userId = identifier;
            const listenedAt = new Date(currently_played_song["played_at"]);
            const createdAt = new Date();

            const resp_obj = {
              id: id,
              spotify_track_id: spotifyTrackId,
              name: name,
              album_name: albumName,
              artists: artists,
              duration_ms: durationMs,
              image_url: imageUrl,
              spotify_url: spotifyUrl,
              user_id: userId,
              listened_at: listenedAt,
              created_at: createdAt,
            };
  
            console.log(resp_obj);
  
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
              createdAt,
            });
  
            return res.code(200).send(resp_obj);
          }

          else {
            const response = await fetch("https://api.spotify.com/v1/me/player/recently-played", {
            headers: { Authorization: starting.concat(access_token) },
            });
            // console.log(response.text())
            const value = await response.json();

            const recently_played_song = value["items"][0];

            const all_artists = [];
            for (const artist_obj of recently_played_song["track"]["album"]["artists"]) {
              all_artists.push(artist_obj["name"]);
            }
            const artists = all_artists.join(",");
            console.log(artists);

            const imageUrl = value["items"][0]["track"]["album"]["images"][1]["url"];
            const albumName = value["items"][0]["track"]["album"]["name"];
            const name = value["items"][0]["track"]["name"];

            const id = randomUUID();
            const spotifyTrackId = recently_played_song["track"]["id"];
            const durationMs = recently_played_song["track"]["duration_ms"];
            const spotifyUrl = recently_played_song["track"]["external_urls"]["spotify"];
            const userId = identifier;
            const listenedAt = new Date();
            const createdAt = new Date();

            const resp_obj = {
              id: id,
              spotify_track_id: spotifyTrackId,
              name: name,
              album_name: albumName,
              artists: artists,
              duration_ms: durationMs,
              image_url: imageUrl,
              spotify_url: spotifyUrl,
              user_id: userId,
              listened_at: listenedAt,
              created_at: createdAt,
            };
  
            console.log(resp_obj);
  
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
              createdAt,
            });
  
            return res.code(200).send(resp_obj);

          }

        } else {
          console.log("TOken is goood");

          const response_test = await fetch("https://api.spotify.com/v1/me/player/currently-playing", {
            headers: { Authorization: starting.concat(access_token) },
          });

          let use_currently_playing = true
          // let value_test = {'item': ""}

          try {
            const value_test = await response_test.json()
          }
          catch (SyntaxError) {
            use_currently_playing = false
          }

          if (use_currently_playing) {
            const response = await fetch("https://api.spotify.com/v1/me/player/currently-playing", {
              headers: { Authorization: starting.concat(access_token) },
            });
            // console.log(response.text())
            const value = await response.json();
            const currently_played_song = value["item"];

            const all_artists = [];
            for (const artist_obj of currently_played_song["artists"]) {
              all_artists.push(artist_obj["name"]);
            }
            const artists = all_artists.join(",");

            const imageUrl = value["item"]["album"]["images"][1]["url"];
            const albumName = value["item"]["album"]["name"];
            const name = value["item"]["name"];

            const id = randomUUID();
            const spotifyTrackId = currently_played_song["id"];
            const durationMs = currently_played_song["duration_ms"];
            const spotifyUrl = currently_played_song["external_urls"]["spotify"];
            const userId = identifier;
            const listenedAt = new Date();
            const createdAt = new Date();

            const resp_obj = {
              id: id,
              spotify_track_id: spotifyTrackId,
              name: name,
              album_name: albumName,
              artists: artists,
              duration_ms: durationMs,
              image_url: imageUrl,
              spotify_url: spotifyUrl,
              user_id: userId,
              listened_at: listenedAt,
              created_at: createdAt,
            };
  
            console.log(resp_obj);
  
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
              createdAt,
            });
  
            return res.code(200).send(resp_obj);
          }

          else {
            const response = await fetch("https://api.spotify.com/v1/me/player/recently-played", {
            headers: { Authorization: starting.concat(access_token) },
            });
            // console.log(response.text())
            const value = await response.json();

            const recently_played_song = value["items"][0];

            const all_artists = [];
            for (const artist_obj of recently_played_song["track"]["album"]["artists"]) {
              all_artists.push(artist_obj["name"]);
            }
            const artists = all_artists.join(",");
            console.log(artists);

            const imageUrl = value["items"][0]["track"]["album"]["images"][1]["url"];
            const albumName = value["items"][0]["track"]["album"]["name"];
            const name = value["items"][0]["track"]["name"];

            const id = randomUUID();
            const spotifyTrackId = recently_played_song["track"]["id"];
            const durationMs = recently_played_song["track"]["duration_ms"];
            const spotifyUrl = recently_played_song["track"]["external_urls"]["spotify"];
            const userId = identifier;
            const listenedAt = new Date(recently_played_song["played_at"]);
            const createdAt = new Date();

            const resp_obj = {
              id: id,
              spotify_track_id: spotifyTrackId,
              name: name,
              album_name: albumName,
              artists: artists,
              duration_ms: durationMs,
              image_url: imageUrl,
              spotify_url: spotifyUrl,
              user_id: userId,
              listened_at: listenedAt,
              created_at: createdAt,
            };
  
            console.log(resp_obj);
  
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
              createdAt,
            });
  
            return res.code(200).send(resp_obj);

          }


          // const recently_played_song = value["items"][0];

          // const all_artists = [];
          // for (const artist_obj of recently_played_song["track"]["album"]["artists"]) {
          //   all_artists.push(artist_obj["name"]);
          // }
          // const artists = all_artists.join(",");
          // console.log(artists);

          // const imageUrl = value["items"][0]["track"]["album"]["images"][1]["url"];
          // const albumName = value["items"][0]["track"]["album"]["name"];
          // const name = value["items"][0]["track"]["name"];

          // const id = randomUUID();
          // const spotifyTrackId = recently_played_song["track"]["id"];
          // const durationMs = recently_played_song["track"]["duration_ms"];
          // const spotifyUrl = recently_played_song["track"]["external_urls"]["spotify"];
          // const userId = identifier;
          // const listenedAt = new Date(recently_played_song["played_at"]);
          // const createdAt = new Date();

          // const resp_obj_2 = {
          //   id: id,
          //   spotify_track_id: spotifyTrackId,
          //   name: name,
          //   album_name: albumName,
          //   artists: artists,
          //   duration_ms: durationMs,
          //   image_url: imageUrl,
          //   spotify_url: spotifyUrl,
          //   user_id: userId,
          //   listened_at: listenedAt,
          //   created_at: createdAt,
          // };

          // console.log(resp_obj_2);

          // await db.insert(postTable).values({
          //   id,
          //   spotifyTrackId,
          //   name,
          //   albumName,
          //   artists,
          //   durationMs,
          //   imageUrl,
          //   spotifyUrl,
          //   userId,
          //   listenedAt,
          //   createdAt,
          // });

          // // console.log(resp_obj_2)
          // return res.code(200).send(resp_obj_2);
          // // return res.code(200).send(recently_played_song)
        }
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
