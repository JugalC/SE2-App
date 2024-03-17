import { initializeApp, applicationDefault } from "firebase-admin/app";
import { getMessaging } from "firebase-admin/messaging";
import { User } from "../db/schema";
import admin from "firebase-admin";

const serviceAccount = {
  clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
  projectId: process.env.FIREBASE_PROJECT_ID,
  privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, "\n"), // Replace escaped newline characters
};

const app = initializeApp({
  credential: admin.credential.cert(serviceAccount),
});
const messaging = getMessaging(app);

export async function sendToDevice(user: User, title: string, body: string): Promise<string | null> {
  const message = {
    notification: {
      title: title,
      body: body,
    },
    token: user.androidRegistrationToken,
  };

  // Send a message to the device corresponding to the provided
  // registration token.
  return await messaging.send(message);
}
