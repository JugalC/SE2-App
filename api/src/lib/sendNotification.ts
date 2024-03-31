import { initializeApp } from "firebase-admin/app";
import { getMessaging } from "firebase-admin/messaging";
import admin from "firebase-admin";
import { db } from "../db/db";
import { userTable } from "../db/schema";
import { ne } from "drizzle-orm";
import { Params } from "fastify-cron";
import fetch from 'node-fetch';

const serviceAccount = {
  clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
  projectId: process.env.FIREBASE_PROJECT_ID,
  privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, "\n"), // Replace escaped newline characters
};

const app = initializeApp({
  credential: admin.credential.cert(serviceAccount),
});
const messaging = getMessaging(app);

export async function sendToDevice(token: string, title: string, body: string): Promise<string | null> {
  const message = {
    notification: {
      title: title,
      body: body,
    },
    token: token,
  };

  // Send a message to the device corresponding to the provided
  // registration token.
  return await messaging.send(message);
}

export const notificationJob: Params = {
  cronTime: "0 0 * * *", // Every day at midnight UTC

  onTick: async (server) => {
    interface userReturned {
        id: string;
        firstName: string;
        lastName: string;
        username: string;
        spotifyAccessToken: string;
        spotifyRefreshToken: string;
        androidRegistrationToken: string;
        passwordHash: string;
        salt: string;
        profilePicture: string;
        displayName: string;
        createdAt: string;  
    }

    const apiUrl = "http://[::1]:8080/"
    const allUsersEndpoint = "users"
    const getRecentSongEndpoint = "most_recent_song"

    const response = await fetch(apiUrl + allUsersEndpoint);
    const allUsersData = (await response.json()) as userReturned[]





    const validUserIds = []
    for (const user_account of allUsersData) {
      if ((user_account['spotifyAccessToken'] != null) && (user_account['spotifyRefreshToken'] != null)) {
        validUserIds.push(user_account['id'])
      }
    }

    for (const validUser of validUserIds) {
      const response = await fetch(apiUrl + getRecentSongEndpoint + "/" + validUser)
    }




    const result = await db
      .select({
        token: userTable.androidRegistrationToken,
      })
      .from(userTable)
      .where(ne(userTable.androidRegistrationToken, ""));
    for (const { token } of result) {
      await sendToDevice(
        token,
        "Your daily TuneIn posts are here!",
        "Open the app to see what your friends have been listening to",
      );
    }
  },
};
