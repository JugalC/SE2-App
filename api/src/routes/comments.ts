import { db } from "../db/db";
import { Plugin, authSchema } from "../types";
import { z } from "zod";
import { and, eq } from "drizzle-orm";
import { authenticateUser } from "../lib/authenticateUser";
import { commentTable, postTable } from "../db/schema";

export const comments: Plugin = (server, _, done) => {
  server.post(
    "/comment/:postId",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          postId: z.string(),
        }),
        body: z.object({
          content: z.string().min(1),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { postId } = req.params;
        const { content } = req.body;

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

        await db.insert(commentTable).values({ userId: user.id, postId, content });

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.delete(
    "/comment/:postId",
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

        await db.delete(commentTable).where(and(eq(commentTable.userId, user.id), eq(commentTable.postId, postId)));

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/comments/:postId",
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

        const likes = await db.select().from(commentTable).where(eq(commentTable.postId, postId));

        return res.code(200).send(likes);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
