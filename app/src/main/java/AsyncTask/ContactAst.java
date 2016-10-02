package asyncTask;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

import model.Destination;

/**
 * Created by lequan on 5/14/2016.
 */
public class ContactAst extends DestinationAst
{
    ContentResolver contentResolver;

    public ContactAst(ContentResolver contentResolver)
    {
        this.contentResolver = contentResolver;
    }

    @Override
    ArrayList<Destination> loadDestination()
    {
        ArrayList<Destination> list = new ArrayList<>();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = contactName(contactID);
                String address = contactAddress(contactID);
                if (name != null && address != null && name.length() > 0 && address.length() > 0)
                {
                    list.add(new Destination(name, address));
                }
            }
            cursor.close();
        }
        return list;
    }

    String contactName(String contact_id)
    {
        Cursor nameCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts._ID + " = ?",
                new String[]{contact_id}, null);
        nameCursor.moveToNext();
        String name = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        nameCursor.close();
        return name;
    }

    // return null if the contact_id don't have any phone number
    // return the first number of contact_id
    public String contactAddress(String contact_id)
    {
        Cursor addressCursor = contentResolver.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + "=?", new String[]{contact_id}, null);
        String address = "";
        if (addressCursor.moveToNext())
        {
            String street = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            if (street != null)
            {
                address += street + ", ";
            }

            String city = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            if (city != null)
            {
                address += city + ", ";
            }

            String country = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            if (country != null)
            {
                address += country + ", ";
            }

            if (address != "")
            {
                address = address.substring(0, address.length() - 2);
            }
        }
        addressCursor.close();
        if (address == "")
        {
            return null;
        }
        return address;
    }

}
