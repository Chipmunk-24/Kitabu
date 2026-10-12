package com.stadiolinks.kitabu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    // ---- Create ----
    @Insert
    suspend fun insertBook(book: BookEntity): Long

    // ---- Read ----
    @Query("SELECT * FROM books ORDER BY title COLLATE NOCASE")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isAvailable = :available ORDER BY title COLLATE NOCASE")
    fun getBooksByAvailability(available: Boolean): Flow<List<BookEntity>>

    /** LIKE search on title or author. The query must already be escaped with '!' (see InputValidator.escapeLike). */
    @Query(
        "SELECT * FROM books " +
            "WHERE title LIKE '%' || :query || '%' ESCAPE '!' " +
            "OR author LIKE '%' || :query || '%' ESCAPE '!' " +
            "ORDER BY title COLLATE NOCASE"
    )
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE bookId = :bookId")
    suspend fun getBookById(bookId: Long): BookEntity?

    // ---- Update ----
    @Query("UPDATE books SET isAvailable = :available WHERE bookId = :bookId")
    suspend fun setAvailability(bookId: Long, available: Boolean)
}
