import { db } from "../db/db";
import { Plugin, authSchema, paginationSchema, searchSchema } from "../types";
import { z } from "zod";
import { and, eq, isNull, ne, or } from "drizzle-orm";
import { authenticateUser } from "../lib/authenticateUser";
import { friendshipRequestTable, friendshipTable, getUserSchema, userTable } from "../db/schema";
import { randomUUID } from "crypto";
import { generateLikeFilters } from "../lib/generateLikeFilters";

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
          .values({ id: randomUUID(), userIdRequesting: user.id, userIdReceiving, rejectedAt: null });

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.put(
    "/friendship-request/:uId",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          uId: z.string(),
        }),
        body: z.object({
          action: z.enum(["accept", "reject"]),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { uId } = req.params;
        const { action } = req.body;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        // There must be a friendship_request in which the user requesting has a relation to the `id` param.
        const friendshipRequest = (
          await db
            .select()
            .from(friendshipRequestTable)
            .where(
              or(
                and(
                  eq(friendshipRequestTable.userIdRequesting, uId),
                  eq(friendshipRequestTable.userIdReceiving, user.id),
                ),
                and(
                  eq(friendshipRequestTable.userIdRequesting, user.id),
                  eq(friendshipRequestTable.userIdReceiving, uId),
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

        return res.code(200).send({});
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
          .where(
            and(
              eq(friendshipRequestTable.userIdReceiving, user.id),
              isNull(friendshipRequestTable.rejectedAt),
            )
          )
          .innerJoin(userTable, eq(friendshipRequestTable.userIdRequesting, userTable.id));

        return res.code(200).send(friendshipRequests);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  // search friends
  server.get(
    "/friendships/search",
    {
      schema: {
        querystring: searchSchema.and(getUserSchema).and(paginationSchema),
      },
    },
    async (req, res) => {
      try {
        const { search, page, limit, id } = req.query;

        const where = generateLikeFilters([
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
        ]);

        // const pendingFriendshipRequests = alias(friendshipRequestTable, "pendingFriendshipRequests");
        // const outgoingFriendshipRequests = alias(friendshipRequestTable, "outgoingFriendshipRequests");
        const users = await db
          .select({
            id: userTable.id,
            firstName: userTable.firstName,
            lastName: userTable.lastName,
            username: userTable.username,
            friendshipRequest: friendshipRequestTable.id,
            friendship: friendshipTable.id,
          })
          .from(userTable)
          .limit(limit)
          .offset(page * limit)
          .where(where)
          .leftJoin(friendshipRequestTable,
            and(
              eq(friendshipRequestTable.userIdReceiving, id!),
              eq(friendshipRequestTable.userIdRequesting, userTable.id)
            )
          )
          // .leftJoin(outgoingFriendshipRequests,
          //   and(
          //     eq(outgoingFriendshipRequests.userIdRequesting, id!),
          //     eq(outgoingFriendshipRequests.userIdReceiving, userTable.id)
          //   )
          // )
          .leftJoin(friendshipTable,
            and(
              or(eq(friendshipTable.userId1, id!), eq(friendshipTable.userId2, id!)),
              or(eq(friendshipTable.userId1, userTable.id), eq(friendshipTable.userId2, userTable.id)))
          );

        return res.code(200).send(users);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  server.post(
    "/friendship/:uId",
    {
      schema: {
        headers: authSchema,
        params: z.object({
          uId: z.string(),
        }),
      },
    },
    async (req, res) => {
      try {
        const { authorization } = req.headers;
        const { uId } = req.params;

        const user = await authenticateUser(authorization);

        if (!user) {
          return res.code(401).send();
        }

        await db
          .delete(friendshipTable)
          .where(
            or(
              and(
                eq(friendshipTable.userId1, user.id),
                eq(friendshipTable.userId2, uId),
              ),
              and(
                eq(friendshipTable.userId1, uId),
                eq(friendshipTable.userId2, user.id),
              ),
            )
          );

        return res.code(200).send({});
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
          .innerJoin(
            userTable,
            and(
              ne(userTable.id, user.id),
              or(eq(friendshipTable.userId1, userTable.id), eq(friendshipTable.userId2, userTable.id)),
            ),
          );

        return res.code(200).send(friendships);
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
