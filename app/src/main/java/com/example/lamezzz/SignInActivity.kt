package com.example.lamezzz

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lamezzz.daos.UserDao
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {
    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var signInButton: SignInButton
    private lateinit var progressBar: ProgressBar
    private lateinit var titleview: TextView
    private lateinit var tagl: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_sign_in)
        signInButton = findViewById(R.id.signInButton)
        progressBar = findViewById(R.id.progressBar)
        titleview  = findViewById(R.id.tiile)
        tagl = findViewById(R.id.textView2)

        val textShader = LinearGradient(0f, 0f, 0f, titleview.lineHeight.toFloat(),
            intArrayOf(Color.BLUE, Color.parseColor("#FFD700"), Color.CYAN),
            null, Shader.TileMode.CLAMP)

        val tex = LinearGradient(0f, 0f, tagl.width.toFloat(), 0f,
            intArrayOf(Color.BLUE, Color.parseColor("#FFD700"), Color.CYAN),
            null, Shader.TileMode.CLAMP)
        tagl.paint.shader = textShader

        titleview.paint.shader = textShader
        tagl.paint.shader = tex

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            Log.w(TAG, "signInResult:failed code=$statusCode")
            val errorMessage = when (statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign-in was cancelled by the user."
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign-in failed. Please try again."
                GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error occurred. Please check your connection."
                else -> "Sign-in failed with unknown error code: $statusCode"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                withContext(Dispatchers.Main) {
                    updateUI(firebaseUser)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    signInButton.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SignInActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {

        if (firebaseUser != null) {
            val user = com.example.lamezzz.models.User(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                imageUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            val usersDao = UserDao()
            usersDao.addUser(user)

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            signInButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

}