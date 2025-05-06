package com.example.contactsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage

@Composable
fun ContactItem(contact: ContactInfo) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            AsyncImage(
                model = contact.photoUri,
                contentDescription = "",
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(42.dp)),
                placeholder = painterResource(R.drawable.user_image),
                error = painterResource(R.drawable.user_image)
            )
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)) {
                Text(contact.name)
                Text(contact.number)
            }
            IconButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${contact.number}"))
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "No permission", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_local_phone_24),
                    contentDescription = "Call"
                )
            }

        }
    }
}