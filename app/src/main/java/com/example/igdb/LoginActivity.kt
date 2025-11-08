package com.example.igdb

import android.content.Context
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
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
import androidx.core.view.isVisible
import com.example.igdb.ui.theme.DarkBlue
import com.example.igdb.ui.theme.IGDBTheme
import com.example.igdb.ui.theme.LightBlue
import com.example.igdb.ui.theme.Orange
import com.example.igdb.ui.theme.White
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = Firebase.auth
        setContent {
            IGDBTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginDesign(modifier = Modifier.padding(innerPadding) , auth )
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

private fun singIn(auth: FirebaseAuth,context: Context, email: String, pass: String) {
    auth.signInWithEmailAndPassword(email, pass)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (auth.currentUser!!.isEmailVerified){
                    Toast.makeText(context, "Login Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    if (context is LoginActivity) {
                        context.finish()
                    }
                }
                else
                    Toast.makeText(context, "Check your Email!!", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
}


@Composable
fun LoginDesign(modifier: Modifier = Modifier , auth: FirebaseAuth? = null) {
    val context = LocalContext.current
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
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            )
            Card(

                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
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
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LoginCredentials(auth)
                }
            }
        }
    }
}


@Composable
fun LoginCredentials(auth: FirebaseAuth? = null) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val gradColors = remember {
        Brush.linearGradient(listOf(Orange , Color.Magenta , White))
    }
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color.LightGray
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
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color.LightGray
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
                        tint = if (passwordVisible) Color.White else Color.LightGray
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
            color = Color.Magenta,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier
                .clickable{
                    if (email.isBlank())
                        Toast.makeText(context, "Missing Email", Toast.LENGTH_SHORT).show()
                    else
                        Firebase.auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Email Sent!", Toast.LENGTH_SHORT).show()
                                }
                            }
                }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "Login Button",
            modifier = Modifier
                .width(140.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    if (email.isBlank())
                        Toast.makeText(context, "Missing Email", Toast.LENGTH_SHORT).show()
                    else if (password.isBlank())
                        Toast.makeText(context, "Missing Password", Toast.LENGTH_SHORT).show()
                    else {
                        isLoading = true
                        singIn(auth!!, context, email, password)
                    }
                }
        )


        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "OR",
            color = Color.Magenta,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(id = R.drawable.signup1),
            contentDescription = "Sign Up Button",
            modifier = Modifier
                .width(140.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    val intent = Intent(context, SignupActivity::class.java)
                    context.startActivity(intent)
                }
        )
        if (isLoading) {
            androidx.compose.material3.LinearProgressIndicator(
                color = Color.Cyan,
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