import { db } from "../db/db";
import { getPostSchema, postTable } from "../db/schema";
import { z } from "zod";
import { and, between, eq } from "drizzle-orm";
import { Plugin, authSchema, paginationSchema } from "../types";
import { generateLikeFilters } from "../lib/generateLikeFilters";
import { authenticateUser } from "../lib/authenticateUser";

export const posts: Plugin = (server, _, done) => {
  server.get(
    "/post/:id",
    {
      schema: {
        params: z.object({
          id: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { id } = req.params;

        const post = await db.query.postTable.findFirst({
          where: eq(postTable.id, id),
        });

        if (!post) {
          return res.code(404).send({ error: `Post not found with id ${id}.` });
        }

        return res.code(200).send(post);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/posts",
    {
      schema: {
        querystring: getPostSchema.and(paginationSchema),
      },
    },
    async (req, res) => {
      try {
        const {
          userId,
          albumName,
          artists,
          createdAtFrom,
          listenedAtFrom,
          createdAtTo,
          listenedAtTo,
          name,
          spotifyTrackId,
          page,
          limit,
        } = req.query;

        const whereLike = generateLikeFilters([
          {
            col: postTable.userId,
            val: userId,
          },
          {
            col: postTable.albumName,
            val: albumName,
          },
          {
            col: postTable.artists,
            val: artists,
          },
          {
            col: postTable.name,
            val: name,
          },
          {
            col: postTable.spotifyTrackId,
            val: spotifyTrackId,
          },
        ]);

        const longTimeAgo = new Date(0);
        const farFuture = new Date(8640000000000000);

        const whereDate = and(
          between(postTable.listenedAt, listenedAtFrom || longTimeAgo, listenedAtTo || farFuture),
          between(postTable.createdAt, createdAtFrom || longTimeAgo, createdAtTo || farFuture),
        );

        const posts = await db
          .select()
          .from(postTable)
          .limit(limit)
          .offset(page * limit)
          .where(and(whereLike, whereDate));

        return res.code(200).send(posts);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.put(
    "/posts/visibility/:postId",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          postId: z.string(),
        }),
        body: z.object({
          action: z.enum(["hide", "show"]),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { postId } = req.params;
        const { action } = req.body;
        const newVal = action === "show";

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        const post = await db.query.postTable.findFirst({
          where: eq(postTable.id, postId),
        });

        if (!post) {
          return res.code(404).send();
        }

        await db.update(postTable).set({ visible: newVal }).where(eq(postTable.id, postId));

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
