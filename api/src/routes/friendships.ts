import { db } from "../db/db";
import { friendshipRequestTable, getUserSchema, insertFriendshipRequestSchema, insertUserSchema, userTable } from "../db/schema";
import { randomUUID, timingSafeEqual } from "crypto";
import { generateSalt, hash } from "../lib/hashing";
import { z } from "zod";
import { eq, or } from "drizzle-orm";
import { Plugin, paginationSchema, searchSchema } from "../types";
import { generateLikeFilters } from "../lib/generateLikeFilters";

export const friendships: Plugin = (server, _, done) => {
	// create pending friend request
  server.post(
    "/friendship",
    {
      schema: {
        body: insertFriendshipRequestSchema,
      },
    },
    async (req, res) => {
      try {
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
    "/friendship/:identifier",
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
