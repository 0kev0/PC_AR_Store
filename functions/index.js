const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendGiftCardNotification = functions.https.onCall((data, context) => {
    const { token, notificationData } = data;

    const message = {
        token: token,
        notification: {
            title: notificationData.title,
            body: notificationData.body
        },
        data: {
            giftCardId: notificationData.giftCardId,
            type: notificationData.type
        }
    };

    return admin.messaging().send(message)
        .then(response => {
            console.log('Notificación enviada:', response);
            return { success: true };
        })
        .catch(error => {
            console.error('Error enviando notificación:', error);
            throw new functions.https.HttpsError('internal', 'Error al enviar notificación');
        });
});