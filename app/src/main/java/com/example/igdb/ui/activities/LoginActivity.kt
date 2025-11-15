package com.example.igdb.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igdb.R
import com.example.igdb.auth.Authentication
import com.example.igdb.ui.theme.IGDBTheme
import com.example.igdb.ui.theme.Orange
import com.example.igdb.ui.theme.OutlinedFieldStyles


class LoginActivity : ComponentActivity() {
    private lateinit var authManager: Authentication


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authManager = Authentication(this)
        setContent {
            IGDBTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginDesign(modifier = Modifier.padding(innerPadding) , authManager )
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        authManager.checkCurrentUser(this)
    }
}

@Composable
fun LoginDesign(modifier: Modifier = Modifier , authManager: Authentication? = null) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ){
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(R.drawable.app_icn),
                contentDescription = stringResource(R.string.igdb_logo),
                modifier = Modifier
                    .padding(top = 28.dp, bottom = 16.dp)
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        CircleShape
                    )
            )
            Card(

                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ),

                ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.login),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LoginCredentials(authManager)
                }
            }
        }
    }
}


@Composable
fun LoginCredentials(authManager: Authentication?) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gradColors = remember(primaryColor, secondaryColor) {
        Brush.linearGradient(listOf(Orange, primaryColor, secondaryColor))
    }
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email),
            colors = OutlinedFieldStyles.colors,
            textStyle = OutlinedFieldStyles.textStyle(gradColors),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedFieldStyles.Spacer

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            colors = OutlinedFieldStyles.colors,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Default.Visibility
                else
                    Icons.Default.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible) stringResource(R.string.hide_password)
                        else stringResource(R.string.show_password),
                        tint = if (passwordVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            textStyle = OutlinedFieldStyles.textStyle(gradColors),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.forgot_password),
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier
                .clickable{
                    isLoading = true
                    if (email.isBlank()) {
                        Toast.makeText(context,
                            context.getString(R.string.missing_email), Toast.LENGTH_SHORT)
                            .show()
                        isLoading = false
                    }
                    else {
                        authManager?.resetPassword(email){isLoading = false}
                    }
                }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true
                if (email.isBlank()){
                    Toast.makeText(context,
                        context.getString(R.string.missing_email), Toast.LENGTH_SHORT)
                        .show()
                    isLoading = false
                }
                else if (password.isBlank()){
                    Toast.makeText(context,
                        context.getString(R.string.missing_password), Toast.LENGTH_SHORT)
                        .show()
                    isLoading = false
                }
                else {
                    authManager?.signIn(email, password) { success ->
                        isLoading = false
                        if (!success) Toast.makeText(context,
                            context.getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .width(108.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.login),
                contentDescription = stringResource(R.string.login_button),
                contentScale = ContentScale.Crop
            )
        }

        OutlinedFieldStyles.Spacer
        Text(
            text = stringResource(R.string.or),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        OutlinedFieldStyles.Spacer
        Button(
            onClick = {
                isLoading = true
                val intent = Intent(context, SignupActivity::class.java)
                context.startActivity(intent)
                if (context is LoginActivity) context.finish()
            },
            modifier = Modifier
                .width(108.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.signup1),
                contentDescription = stringResource(R.string.signup_button),
                contentScale = ContentScale.Crop
            )
        }
        if (isLoading) {
            androidx.compose.material3.LinearProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    IGDBTheme {
        LoginDesign()
    }
}