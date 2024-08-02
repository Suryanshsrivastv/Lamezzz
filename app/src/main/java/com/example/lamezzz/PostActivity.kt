package com.example.lamezzz

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lamezzz.daos.PostDao

class PostActivity : AppCompatActivity() {
    private lateinit var dao : PostDao

    private lateinit var postt: Button
    private lateinit var txtx: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post)
        dao = PostDao()
        postt = findViewById(R.id.postButton)
        txtx = findViewById(R.id.postInput)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        postt.setOnClickListener {
           val inp =  txtx.text.toString().trim()
            if(inp.isNotEmpty()){
                dao.addPost(inp)
                finish()
            }
            else{
                Toast.makeText(this,"Type Something to Post",LENGTH_SHORT).show()
            }
        }
    }
}