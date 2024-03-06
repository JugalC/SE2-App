import { db } from "../db/db";
import { friendshipRequestTable, friendshipTable } from "../db/schema";
import { Plugin, authSchema, paginationSchema } from "../types";
import { z } from "zod";
import { randomUUID } from "crypto";
import { and, eq, or } from "drizzle-orm";
import { authenticateUser } from "../lib/authenticateUser";

export const friendships: Plugin = (server, _, done) => {
  server.post(
    "/friendship-request/:userIdReceiving",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          userIdReceiving: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { userIdReceiving } = req.params;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        await db
          .insert(friendshipRequestTable)
          .values({ id: randomUUID(), userIdRequesting: user.id, userIdReceiving });

        return res.code(200).send();
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.put(
    "/friendship-request/:id",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          id: z.string(),
        }),
        body: z.object({
          action: z.enum(["accept", "reject"]),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { id } = req.params;
        const { action } = req.body;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        const friendshipRequest = (
          await db
            .select()
            .from(friendshipRequestTable)
            .where(
              and(
                eq(friendshipRequestTable.id, id),
                or(
                  eq(friendshipRequestTable.userIdRequesting, user.id),
                  eq(friendshipRequestTable.userIdReceiving, user.id),
                ),
              ),
            )
        )[0];

        if (!friendshipRequest) {
          return res.code(404).send();
        }

        if (action === "accept") {
          await db.insert(friendshipTable).values({
            id: randomUUID(),
            userId1: friendshipRequest.userIdReceiving,
            userId2: friendshipRequest.userIdRequesting,
          });

          await db.delete(friendshipRequestTable).where(eq(friendshipRequestTable.id, id));
        } else {
          await db
            .update(friendshipRequestTable)
            .set({ rejectedAt: new Date() })
            .where(eq(friendshipRequestTable.id, id));
        }

        return res.code(200).send();
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.delete(
    "/friendship/:id",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          id: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { id } = req.params;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        await db
          .delete(friendshipTable)
          .where(
            and(
              eq(friendshipTable.id, id),
              or(eq(friendshipTable.userId1, user.id), eq(friendshipTable.userId2, user.id)),
            ),
          );

        return res.code(200).send();
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/friendships",
    {
      schema: {
        headers: authSchema,
        querystring: paginationSchema,
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { page, limit } = req.query;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        const friendships = await db
          .select()
          .from(friendshipTable)
          .limit(limit)
          .offset(page * limit)
          .where(or(eq(friendshipTable.userId1, user.id), eq(friendshipTable.userId2, user.id)));

        return res.code(200).send(friendships);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
