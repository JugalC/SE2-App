import { db } from "../db/db";
import { getUserSchema, insertUserSchema, userTable } from "../db/schema";
import { randomUUID, timingSafeEqual } from "crypto";
import { generateSalt, hash } from "../lib/hashing";
import { z } from "zod";
import { eq, or } from "drizzle-orm";
import { Plugin, paginationSchema } from "../types";
import { generateLikeFilters } from "../lib/generateLikeFilters";

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
        const passwordHash = (await hash(req.body.password, salt)).toString("hex");
        const body = { ...req.body, password: undefined };

        await db.insert(userTable).values({
          id: randomUUID(),
          ...body,
          passwordHash,
          salt,
        });

        return res.code(200).send();
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

        return res.code(200).send({ ...user, passwordHash: undefined, salt: undefined });
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

  server.get(
    "/users",
    {
      schema: {
        querystring: getUserSchema.and(paginationSchema),
      },
    },
    async (req, res) => {
      try {
        const { firstName, lastName, page, limit } = req.query;

        const where = generateLikeFilters([
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

  done();
};