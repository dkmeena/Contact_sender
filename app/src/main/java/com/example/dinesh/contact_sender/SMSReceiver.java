package com.example.dinesh.contact_sender;

/**
 * Created by dinesh on 18/8/16.
 */
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

    private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Uri uriContact;
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sData;
        sData = context.getSharedPreferences("codes",0);
        String codereturned = sData.getString("code","couldn't load data");
        //Toast.makeText(context, codereturned, Toast.LENGTH_SHORT).show();
        String phoneNumber="";
        String z="";
        String ans="";
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle myBundle = intent.getExtras();
            SmsMessage[] messages = null;
            String strMessage = "";

            if (myBundle != null) {
                //get message in pdus format(protocol discription unit)
                Object[] pdus = (Object[]) myBundle.get("pdus");
                //create an array of messages
                messages = new SmsMessage[pdus.length];

                for (int i = 0; i < messages.length; i++) {
                    //Create an SmsMessage from a raw PDU.
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                   phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    //get the originating address (sender) of this SMS message in String form or null if unavailable
                    //strMessage += "SMS From: " + messages[i].getOriginatingAddress();
                    //strMessage += " : ";
                    //get the message body as a String, if it exists and is text based.
                    strMessage += messages[i].getMessageBody();
                    strMessage += "\n";
                }
                // Intent i = new Intent(context.getApplicationContext(), MainActivity.class);
                //i.putExtra("new_variable_name",strMessage);
                // context.startActivity(i);
                if(strMessage.contains(codereturned))
                {

                    String[] a = strMessage.split(codereturned);
                    z = a[1].trim();

                    ContentResolver cr = context.getContentResolver();
                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);

                    if (cur.getCount() > 0) {
                        while (cur.moveToNext()) {
                            String id = cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(cur.getColumnIndex(
                                    ContactsContract.Contacts.DISPLAY_NAME));

                            if (cur.getInt(cur.getColumnIndex(
                                    ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                Cursor pCur = cr.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                        new String[]{id}, null);
                                while (pCur.moveToNext()) {
                                    String phoneNo = pCur.getString(pCur.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    if(name.contains(z)){
                                    //Toast.makeText(context, z+"::"+name, Toast.LENGTH_SHORT).show();
                                        ans = ans + name+" :: "+phoneNo+'\n';
                                    }

                                }
                                pCur.close();
                            }
                        }
                    }

                    PendingIntent pi = PendingIntent.getActivity(context, 0,
                            new Intent(context, SMSReceiver.class), 0);
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(phoneNumber, null, ans, pi, null);
                    Toast.makeText(context, ans, Toast.LENGTH_SHORT).show();
                }


            }

        }
    }
}
