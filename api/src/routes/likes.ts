import { db } from "../db/db";
import { Plugin, authSchema } from "../types";
import { z } from "zod";
import { and, eq } from "drizzle-orm";
import { authenticateUser } from "../lib/authenticateUser";
import { likeTable, postTable } from "../db/schema";

export const likes: Plugin = (server, _, done) => {
  server.post(
    "/like/:postId",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          postId: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { postId } = req.params;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        const post = await db.query.postTable.findFirst({
          where: eq(postTable.id, postId),
        });

        if (!post) {
          return res.code(404).send({ error: "Post doesn't exist." });
        }

        await db.insert(likeTable).values({ userId: user.id, postId });

        const likes = await db.select().from(likeTable).where(eq(likeTable.postId, postId));

        return res.code(200).send({ likes: likes.length });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.post(
    "/unlike/:postId",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          postId: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { postId } = req.params;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        await db.delete(likeTable).where(and(eq(likeTable.userId, user.id), eq(likeTable.postId, postId)));
        const likes = await db.select().from(likeTable).where(eq(likeTable.postId, postId));

        return res.code(200).send({ likes: likes.length });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/likes/:postId",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          postId: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { postId } = req.params;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        const post = await db.query.postTable.findFirst({
          where: eq(postTable.id, postId),
        });

        if (!post) {
          return res.code(404).send({ error: "Post doesn't exist." });
        }

        const likes = await db.select().from(likeTable).where(eq(likeTable.postId, postId));
        const is_liked = likes.some((like) => like.userId === user.id);

        return res.code(200).send({ likes: likes.length, is_liked: is_liked });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
