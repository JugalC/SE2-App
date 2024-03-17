package ca.uwaterloo.tunein.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.MainActivity
import ca.uwaterloo.tunein.R
import ca.uwaterloo.tunein.auth.AuthManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class Firebase : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            it.body?.let { body -> sendNotification(body) }
        }
    }


    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer( this, token)
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FCM Message")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FirebaseMsg"

        fun sendRegistrationToServer(context: Context, token: String) {
            val uId = AuthManager.getUser(context).id
            Log.d(TAG, "sendRegistrationTokenToServer($token) for $uId")

            val queue = Volley.newRequestQueue(context)
            val notificationTokenUrl = "${BuildConfig.BASE_URL}/user/notificationToken/$uId"

            val req = JSONObject()
            req.put("androidRegistrationToken", token)

            val notificationReq = JsonObjectRequest(
                Request.Method.PATCH, notificationTokenUrl, req,
                { res ->
                    Log.d(TAG, "sendRegistrationTokenToServer Success")
                },
                { error ->
                    val statusCode: Int = error.networkResponse.statusCode
                    Log.d(TAG, "sendRegistrationTokenToServer Failed ($statusCode)")
                }
            )
            queue.add(notificationReq)
        }

        fun clearRegistrationToken(context: Context) {
            sendRegistrationToServer(context,"")
        }
    }
}