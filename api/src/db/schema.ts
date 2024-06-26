import { sqliteTable, text, unique, numeric, integer, primaryKey } from "drizzle-orm/sqlite-core";
import { sql } from "drizzle-orm";
import { createInsertSchema, createSelectSchema } from "drizzle-zod";
import { z } from "zod";
import { zodPreprocessDate } from "../lib/zodHelpers";
import { boolean } from "drizzle-orm/mysql-core";

export const userTable = sqliteTable("user", {
  id: text("id").notNull().primaryKey(),
  firstName: text("first_name").notNull(),
  lastName: text("last_name").notNull(),
  username: text("username").notNull(),
  spotifyAccessToken: text("last_spotify_access_token"),
  spotifyRefreshToken: text("spotify_refresh_token"),
  androidRegistrationToken: text("android_registration_token").notNull(),
  passwordHash: text("password_hash").notNull(),
  salt: text("salt").notNull(),
  profilePicture: text("profile_picture"),
  displayName: text("display_name"),
  createdAt: integer("created_at", { mode: "timestamp" })
    .notNull()
    .default(sql`CURRENT_TIMESTAMP`),
});

export const insertUserSchema = createInsertSchema(userTable)
  .omit({ id: true, passwordHash: true, salt: true })
  .and(z.object({ password: z.string().min(6) }));

export const getUserSchema = createSelectSchema(userTable)
  .omit({
    passwordHash: true,
    salt: true,
    spotifyRefreshToken: true,
    spotifyAccessToken: true,
    createdAt: true,
  })
  .partial();

export const userSchema = createSelectSchema(userTable);

export type User = z.infer<typeof userSchema>;

export const postTable = sqliteTable("post", {
  id: text("id").notNull().primaryKey(),
  spotifyTrackId: text("spotify_track_id").notNull(),
  name: text("name").notNull(),
  albumName: text("album_name").notNull(),
  artists: text("artists").notNull(),
  durationMs: numeric("duration_ms").notNull(),
  imageUrl: text("image_url").notNull(),
  spotifyUrl: text("spotify_url").notNull(),
  userId: text("user_id")
    .notNull()
    .references(() => userTable.id),
  listenedAt: integer("listened_at", { mode: "timestamp" }),
  createdAt: integer("created_at", { mode: "timestamp" })
    .notNull()
    .default(sql`CURRENT_TIMESTAMP`),
  visible: integer("visible", { mode: "boolean" }).notNull().default(false),
  userViewed: integer("user_viewed", { mode: "boolean" }).notNull().default(false),
});

export const likeTable = sqliteTable(
  "like",
  {
    userId: text("user_id")
      .notNull()
      .references(() => userTable.id),
    postId: text("post_id")
      .notNull()
      .references(() => postTable.id),
  },
  ({ userId, postId }) => ({
    primary: primaryKey({ columns: [userId, postId] }),
  }),
);

export const commentTable = sqliteTable("comment", {
  id: text("id").notNull().primaryKey(),
  userId: text("user_id")
    .notNull()
    .references(() => userTable.id),
  postId: text("post_id")
    .notNull()
    .references(() => postTable.id),
  content: text("text").notNull(),
});

export const getPostSchema = createSelectSchema(postTable)
  .omit({ listenedAt: true, createdAt: true })
  .and(
    z
      .object({
        listenedAtFrom: zodPreprocessDate(),
        listenedAtTo: zodPreprocessDate(),
        createdAtFrom: zodPreprocessDate(),
        createdAtTo: zodPreprocessDate(),
      })
      .partial(),
  );

export const friendshipTable = sqliteTable(
  "friendship",
  {
    id: text("id").notNull().primaryKey(),
    userId1: text("user_id_1")
      .notNull()
      .references(() => userTable.id),
    userId2: text("user_id_2")
      .notNull()
      .references(() => userTable.id),
    createdAt: integer("created_at", { mode: "timestamp" })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  ({ userId1, userId2 }) => ({
    unq: unique().on(userId1, userId2),
  }),
);

export const friendshipRequestTable = sqliteTable(
  "friendship_request",
  {
    id: text("id").notNull().primaryKey(),
    userIdRequesting: text("user_id_requesting")
      .notNull()
      .references(() => userTable.id),
    userIdReceiving: text("user_id_receiving")
      .notNull()
      .references(() => userTable.id),
    createdAt: integer("created_at", { mode: "timestamp" })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
    rejectedAt: integer("rejected_at", { mode: "timestamp" }).default(sql`CURRENT_TIMESTAMP`),
  },
  ({ userIdRequesting, userIdReceiving }) => ({
    unq: unique().on(userIdRequesting, userIdReceiving),
  }),
);
