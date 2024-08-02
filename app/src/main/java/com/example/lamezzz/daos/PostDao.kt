package com.example.lamezzz.daos

import com.example.lamezzz.models.Post
import com.example.lamezzz.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {


    val db = FirebaseFirestore.getInstance()
    val postCollections = db.collection("posts")
    val auth = FirebaseAuth.getInstance()


    fun addPost(text: String) {
        GlobalScope.launch(Dispatchers.Main) { // Switch to Main Dispatcher
            val currentUserId = auth.currentUser!!.uid
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!

            val currentTime = System.currentTimeMillis()
            val post = Post(text, user, currentTime)
            postCollections.document().set(post).await() // Ensure operation completes
        }
    }

    fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollections.document(postId).get()
    }

    fun updateLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)

            if (isLiked) {
                post.likedBy.remove(currentUserId)
            } else {
                post.likedBy.add(currentUserId)
            }
            postCollections.document(postId).set(post)
        }

    }
}