import firebase from "firebase-admin";

firebase.initializeApp({
  credential: firebase.credential.cert({
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
    projectId: process.env.FIREBASE_PROJECT_ID,
    privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/gm, "\n"),
  }),
});

export function sendPushNotification({ token, data }: { token: string; data: Record<string, string> }) {
  return firebase.messaging().send({ token, data });
}
