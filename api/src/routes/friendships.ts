import { db } from "../db/db";
import { friendshipRequestTable, friendshipTable, userTable } from "../db/schema";
import { Plugin, paginationSchema } from "../types";
import { z } from "zod";
import { randomUUID } from "crypto";
import { and, eq, ne, or } from "drizzle-orm";

export const friendships: Plugin = (server, _, done) => {
  server.post(
    "/friendship-request/:userIdReceiving/:userIdRequesting",
    {
      schema: {
        // headers: authSchema,
        params: z.object({
          userIdReceiving: z.string(),
          userIdRequesting: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        // const {
        //   authorization: { user },
        // } = req.headers;
        const { userIdReceiving, userIdRequesting } = req.params;

        const friendshipRequest = await db.query.friendshipRequestTable.findFirst({
					where: and(
						eq(friendshipRequestTable.userIdRequesting, userIdRequesting),
						eq(friendshipRequestTable.userIdReceiving, userIdReceiving),
					),
        });

        if (friendshipRequest) {
          return res.code(404).send({ error: "Request already exists." });
        }

        await db
          .insert(friendshipRequestTable)
          .values({ id: randomUUID(), userIdRequesting, userIdReceiving });

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.put(
    "/friendship-request/:requestingUserId/:receivingUserId",
    {
      schema: {
        // headers: authSchema,
        params: z.object({
          // id: z.string(),
          requestingUserId: z.string(),
          receivingUserId: z.string(),
        }),
        body: z.object({
          action: z.enum(["accept", "reject"]),
        }),
      },
    },
    async (req, res) => {
      try {
        // const {
        //   authorization: { user },
        // } = req.headers;
        const { requestingUserId, receivingUserId } = req.params;
        const { action } = req.body;

        const friendshipRequest = (
          await db
            .select()
            .from(friendshipRequestTable)
            .where(
              and(
                // eq(friendshipRequestTable.id, id),
                eq(friendshipRequestTable.userIdRequesting, requestingUserId),
                eq(friendshipRequestTable.userIdReceiving, receivingUserId),
                // or(
                // ),
              ),
            )
        )[0];

        if (!friendshipRequest) {
          return res.code(404).send({});
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

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.get(
    "/friendships/:uId",
    {
      schema: {
        // headers: authSchema,
        querystring: paginationSchema,
        params: z.object({
          uId: z.string(),
        })
      },
    },
    async (req, res) => {
      try {
        // const {
        //   authorization: { user },
        // } = req.headers;
        const { uId } = req.params;
        const { page, limit } = req.query;

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
          .where(or(eq(friendshipTable.userId1, uId), eq(friendshipTable.userId2, uId)))
          .innerJoin(userTable, and(ne(userTable.id, uId), or(eq(friendshipTable.userId1, userTable.id), eq(friendshipTable.userId2, userTable.id))));

        return res.code(200).send(friendships);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );


  server.get(
    "/friendship-requests/:userId",
    {
      schema: {
        // headers: authSchema,
        querystring: paginationSchema,
        params: z.object({
          userId: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        // const {
        //   authorization: { user },
        // } = req.headers;
        const { page, limit } = req.query;
        const {userId} = req.params;

        const friendships = await db
          .select({
            id: userTable.id,
            firstName: userTable.firstName,
            lastName: userTable.lastName,
            username: userTable.username,
          })
          .from(friendshipRequestTable)
          .limit(limit)
          .offset(page * limit)
          .where(eq(friendshipRequestTable.userIdReceiving, userId))
          .innerJoin(userTable, eq(friendshipRequestTable.userIdRequesting, userTable.id));

        return res.code(200).send(friendships);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
