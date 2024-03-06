import { eq } from "drizzle-orm";
import { db } from "../db/db";
import { User, userTable } from "../db/schema";
import { hash } from "./hashing";
import { timingSafeEqual } from "crypto";

export async function authenticateUser({
  username,
  password,
}: {
  username: string;
  password: string;
}): Promise<User | null> {
  const user = await db.query.userTable.findFirst({
    where: eq(userTable.username, username),
  });

  if (!user) {
    return null;
  }

  const passwordHash = await hash(password, user.salt);

  if (!timingSafeEqual(passwordHash, Buffer.from(user.passwordHash, "hex"))) {
    return null;
  }

  return user;
}
