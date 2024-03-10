import { db } from "../db/db";
import { Plugin, authSchema, paginationSchema } from "../types";
import { z } from "zod";
import { and, eq, ne, or } from "drizzle-orm";
import { authenticateUser } from "../lib/authenticateUser";
import { friendshipRequestTable, friendshipTable, userTable } from "../db/schema";
import { randomUUID } from "crypto";

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

        // check if friendship request already exists
        const friendshipRequest = await db.query.friendshipRequestTable.findFirst({
					where: and(
						eq(friendshipRequestTable.userIdRequesting, user.id),
						eq(friendshipRequestTable.userIdReceiving, userIdReceiving),
					),
        });

        if (friendshipRequest) {
          return res.code(404).send({ error: "Request already exists." });
        }

        await db
          .insert(friendshipRequestTable)
          .values({ id: randomUUID(), userIdRequesting: user.id, userIdReceiving, rejectedAt: null, });
  
        return res.code(200).send({});
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

          await db.delete(friendshipRequestTable).where(eq(friendshipRequestTable.id, friendshipRequest.id));
        } else {
          await db
            .update(friendshipRequestTable)
            .set({ rejectedAt: new Date() })
            .where(eq(friendshipRequestTable.id, friendshipRequest.id));
        }

        return res.code(200).send();
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  // view friend requests
  server.get(
    "/friendship-requests",
    {
      schema: {
        headers: authSchema,
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const user = await authenticateUser(authorization);
        if (!user) {
          return res.code(401).send();
        }

        const friendshipRequests = await db
          .select({
            requestId: friendshipRequestTable.id,
            uId: userTable.id,
            firstName: userTable.firstName,
            lastName: userTable.lastName,
            username: userTable.username,
          })
          .from(friendshipRequestTable)
          .where(eq(friendshipRequestTable.userIdReceiving, user.id))
          .innerJoin(userTable, eq(friendshipRequestTable.userIdRequesting, userTable.id));

				
        return res.code(200).send(friendshipRequests);
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
          .select({
            id: userTable.id,
            firstName: userTable.firstName,
            lastName: userTable.lastName,
            username: userTable.username,
          })
          .from(friendshipTable)
          .limit(limit)
          .offset(page * limit)
          .where(or(eq(friendshipTable.userId1, user.id), eq(friendshipTable.userId2, user.id)))
          .innerJoin(userTable, and(ne(userTable.id, user.id), or(eq(friendshipTable.userId1, userTable.id), eq(friendshipTable.userId2, userTable.id))));

        return res.code(200).send(friendships);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
