import { db } from "../db/db";
import { friendshipRequestTable, insertFriendshipRequestSchema } from "../db/schema";
import { randomUUID } from "crypto";
import { z } from "zod";
import { and, eq, or } from "drizzle-orm";
import { Plugin } from "../types";

export const friendships: Plugin = (server, _, done) => {
	// create pending friend request
  server.post(
    "/friendships",
    {
      schema: {
        body: insertFriendshipRequestSchema,
      },
    },
    async (req, res) => {
      try {
        const friendshipRequest = await db.query.friendshipRequestTable.findFirst({
					where: and(
						eq(friendshipRequestTable.userIdRequesting, req.body.userIdRequesting),
						eq(friendshipRequestTable.userIdReceiving, req.body.userIdReceiving),
					),
        });

        if (friendshipRequest) {
          return res.code(404).send({ error: "Request already exists." });
        }
        
				await db.insert(friendshipRequestTable).values({
          id: randomUUID(),
          ...req.body,
					rejectedAt: null,
        });

        return res.code(200).send({});
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

	// view friend requests
  server.get(
    "/friendships/:identifier",
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

				const friendshipRequest = await db.query.friendshipRequestTable.findFirst({
					where: or(
						eq(friendshipRequestTable.id, identifier),
						eq(friendshipRequestTable.userIdRequesting, identifier), // want to keep this in so that a user can see their outgoing requests
						eq(friendshipRequestTable.userIdReceiving, identifier),
					),
        });

        if (!friendshipRequest) {
          return res.code(404).send({ error: "Request not found with given parameters." });
        }

        return res.code(200).send({ ...friendshipRequest });
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

	// accept / deny
  server.put(
    "/friendship/:requestId",
    {
      schema: {
      },
    },
    async (req, res) => {
      try {
        
      } catch (e) {
        console.error(e);
        return res.code(500).send({ error: "Internal server error." });
      }
    },
  );

  done();
};
