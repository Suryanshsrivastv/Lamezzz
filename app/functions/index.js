/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendNotificationOnNewItem = functions.firestore
    .document("items/{itemId}")
    .onCreate((snapshot, context) => {
      const newItem = snapshot.data();

      const payload = {
        notification: {
          title: "New Item Added",
          body: `A new item ${newItem.name} has been added.`,
          sound: "default",
        },
      };

      return admin.messaging().sendToTopic("new_items", payload)
          .then((response) => {
            console.log("Notification sent successfully:", response);
          })
          .catch((error) => {
            console.log("Error sending notification:", error);
          });
    });
