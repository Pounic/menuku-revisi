package com.example.menuku.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions

class UserCloudStore(private val onError: (String) -> Unit, private val onPending: (Boolean) -> Unit) {
    private val database by lazy { FirebaseFirestore.getInstance() }
    private val listeners = mutableListOf<ListenerRegistration>()
    private var owner: String? = null
    private val pendingCollections = mutableSetOf<String>()

    fun bind(uid: String) { close(); owner = uid }

    fun listen(collection: String, onData: (List<Pair<String, Map<String, Any>>>) -> Unit) {
        val uid = owner ?: return
        listeners += database.collection("users").document(uid).collection(collection)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (owner != uid) return@addSnapshotListener
                if (error != null) {
                    onError("Tidak dapat membaca $collection: ${error.localizedMessage}")
                } else if (snapshot != null) {
                    if (snapshot.metadata.hasPendingWrites()) pendingCollections.add(collection)
                    else pendingCollections.remove(collection)
                    onPending(pendingCollections.isNotEmpty())
                    try { onData(snapshot.documents.mapNotNull { doc -> doc.data?.let { doc.id to it } }) }
                    catch (e: Exception) { onError(e.localizedMessage ?: "Format data tidak sesuai") }
                }
            }
    }

    fun listenProfile(onData: (Map<String, Any>) -> Unit) {
        val uid = owner ?: return
        listeners += database.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (owner != uid) return@addSnapshotListener
            if (error != null) onError("Profil belum tersinkron: ${error.localizedMessage}")
            else snapshot?.data?.let(onData)
        }
    }

    fun write(collection: String, id: String, data: Map<String, Any>?) {
        val uid = owner ?: return onError("Masuk ke akun terlebih dahulu agar data dapat disimpan.")
        val reference = database.collection("users").document(uid).collection(collection).document(id)
        val task = if (data == null) reference.delete() else reference.set(data)
        task.addOnFailureListener { error ->
            if (owner == uid) onError("Perubahan $collection gagal disimpan. Periksa koneksi/izin, lalu ulangi perubahan: ${error.localizedMessage}")
        }
    }

    fun profile(data: Map<String, Any>) {
        val uid = owner ?: return
        database.collection("users").document(uid).set(data, SetOptions.merge())
            .addOnFailureListener { if (owner == uid) onError("Profil gagal disimpan: ${it.localizedMessage}") }
    }

    fun close() {
        listeners.forEach { it.remove() }; listeners.clear()
        owner = null; pendingCollections.clear(); onPending(false)
    }
}
