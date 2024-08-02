package com.example.lamezzz

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import com.example.lamezzz.models.Post
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lamezzz.daos.PostDao
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity(),IPostAdapter {

    private lateinit var postDao: PostDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PostAdapter
    private lateinit var tobatitle: TextView
    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    private lateinit var logout: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        logout = findViewById(R.id.logout_button)
        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)
        tobatitle = findViewById(R.id.tootitle)
        auth = FirebaseAuth.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tex = LinearGradient(0f, 0f, tobatitle.width.toFloat(), 0f,
            intArrayOf(Color.BLUE, Color.parseColor("#FFD700"), Color.CYAN),
            null, Shader.TileMode.CLAMP)

        tobatitle.paint.shader = tex

        logout.setOnClickListener(){
            showLogoutConfirmationDialog()
        }
        fab.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }

        postDao = PostDao()  // Ensure that postDao is initialized
        setupRecyclerView()


    }

    private fun setupRecyclerView() {
        val postcollection = postDao.postCollections
        val query = postcollection.orderBy("createdAt",Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions,this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            logoutUser()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun logoutUser() {
        // Sign out the user
        auth.signOut()
        Toast.makeText(this@MainActivity, "Logged out", Toast.LENGTH_SHORT).show()

        // Redirect to LoginActivity or any other activity
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }


    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

}