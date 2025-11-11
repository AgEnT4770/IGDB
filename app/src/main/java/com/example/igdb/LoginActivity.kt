package com.example.igdb

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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igdb.ui.theme.IGDBTheme
import com.example.igdb.ui.theme.Orange


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
                contentDescription = "IGDB Logo",
                modifier = Modifier
                    .padding(top = 28.dp , bottom = 16.dp)
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), CircleShape)
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
                        text = "LOGIN",
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
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textStyle = TextStyle(
                brush = gradColors,
                fontSize = 16.sp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
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
                        contentDescription = if (passwordVisible) "Hide password"
                        else "Show password",
                        tint = if (passwordVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            textStyle = TextStyle(
                brush = gradColors,
                fontSize = 16.sp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Forgot your Password?",
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier
                .clickable{
                    isLoading = true
                    if (email.isBlank()) {
                        Toast.makeText(context, "Missing Email", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Missing Email", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                else if (password.isBlank()){
                    Toast.makeText(context, "Missing Password", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                else {
                    authManager?.signIn(email, password) { success ->
                        isLoading = false
                        if (!success) Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
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
                contentDescription = "Login Button",
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "OR",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                isLoading = true
                val intent = Intent(context, SignupActivity::class.java)
                context.startActivity(intent)
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
                contentDescription = "Signup Button",
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