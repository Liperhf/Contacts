package com.example.contactsapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Parcelable
import android.provider.ContactsContract
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactInfo(
    val name: String,
    val number: String,
    val photoUri: String?
) : Parcelable


class ContactService : Service() {

    private var duplicatesFound: Boolean = false

    private val binder = object : IContactService.Stub() {
        override fun searchContacts(name: String?): List<ContactInfo> {
            val contacts = mutableListOf<ContactInfo>()
            val uri = ContactsContract.RawContacts.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.RawContacts._ID
            )
            val selection = if (!name.isNullOrEmpty()) {
                "${ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY} LIKE ?"
            } else null
            val selectionArgs = if (!name.isNullOrEmpty()) {
                arrayOf("%$name%")
            } else null
            val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            cursor?.use {
                val nameIndex =
                    it.getColumnIndexOrThrow(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY)
                val idIndex = it.getColumnIndexOrThrow(ContactsContract.RawContacts._ID)
                while (it.moveToNext()) {
                    val contactName = it.getString(nameIndex)
                    val rawId = it.getLong(idIndex)
                    var phoneNumber: String? = null
                    var photoUri: String? = null

                    val phoneCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(
                            rawId.toString(),
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        ),
                        null
                    )
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            phoneNumber =
                                pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                    }

                    val photoCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Photo.PHOTO_URI),
                        "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(
                            rawId.toString(),
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                        ),
                        null
                    )
                    photoCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            photoUri =
                                pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                        }
                    }

                    if (!contactName.isNullOrEmpty() && !phoneNumber.isNullOrEmpty()) {
                        contacts.add(ContactInfo(contactName, phoneNumber!!, photoUri))
                    }
                }
            }
            return contacts
        }

        override fun deleteDuplicatesContacts() {
            val uri = ContactsContract.RawContacts.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.RawContacts._ID
            )
            val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            val cursor = contentResolver.query(uri, projection, null, null, sortOrder)
            val contactsMap = mutableMapOf<Pair<String, String>, MutableList<Long>>()
            cursor?.use {
                val nameIndex =
                    it.getColumnIndexOrThrow(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY)
                val idIndex = it.getColumnIndexOrThrow(ContactsContract.RawContacts._ID)
                while (it.moveToNext()) {
                    val contactName = it.getString(nameIndex)
                    val rawId = it.getLong(idIndex)
                    var phoneNumber: String? = null
                    val phoneCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(
                            rawId.toString(),
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        ),
                        null
                    )
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            phoneNumber =
                                pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                    }
                    if (!contactName.isNullOrEmpty() && !phoneNumber.isNullOrEmpty()) {
                        val key = contactName to phoneNumber!!
                        contactsMap.getOrPut(key) { mutableListOf() }.add(rawId)
                    }
                }
            }
            val ops = ArrayList<android.content.ContentProviderOperation>()
            duplicatesFound = false
            for ((key, ids) in contactsMap) {
                if (ids.size > 1) {
                    duplicatesFound = true
                    ids.drop(1).forEach { id ->
                        ops.add(
                            android.content.ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                                .withSelection(
                                    "${ContactsContract.RawContacts._ID} = ?",
                                    arrayOf(id.toString())
                                )
                                .build()
                        )
                    }
                }
            }
            try {
                if (ops.isNotEmpty()) {
                    contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                }
            } catch (e: Exception) {
            }
        }

        override fun hasDuplicates(): Boolean {
            return duplicatesFound
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}