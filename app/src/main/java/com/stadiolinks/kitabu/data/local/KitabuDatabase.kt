package com.stadiolinks.kitabu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stadiolinks.kitabu.domain.RentalPolicy

@Database(
    entities = [BookEntity::class, BookingEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KitabuDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: KitabuDatabase? = null

        /** Thread-safe singleton. */
        fun getInstance(context: Context): KitabuDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        private fun build(context: Context): KitabuDatabase =
            Room.databaseBuilder(context, KitabuDatabase::class.java, "kitabu.db")
                .fallbackToDestructiveMigration() // development only
                .addCallback(object : RoomDatabase.Callback() {
                    // Runs once, on the very first creation of the database file.
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        prePopulate(db)
                    }
                })
                .build()

        private fun prePopulate(db: SupportSQLiteDatabase) {
            val sampleBooks = listOf(
                Triple("Clean Code", "Robert C. Martin", "Programming"),
                Triple("Kotlin in Action", "Dmitry Jemerov and Svetlana Isakova", "Programming"),
                Triple("Head First Design Patterns", "Eric Freeman and Elisabeth Robson", "Software Engineering"),
                Triple("Introduction to Algorithms", "Thomas H. Cormen et al.", "Computer Science"),
                Triple("Database System Concepts", "Abraham Silberschatz et al.", "Databases"),
                Triple("The Pragmatic Programmer", "Andrew Hunt and David Thomas", "Software Engineering"),
                Triple("Computer Networking: A Top-Down Approach", "James Kurose and Keith Ross", "Networking"),
                Triple("Operating System Concepts", "Abraham Silberschatz et al.", "Computer Science"),
                Triple("Refactoring", "Martin Fowler", "Software Engineering"),
                Triple("Don't Make Me Think", "Steve Krug", "UX Design"),
                Triple("Things Fall Apart", "Chinua Achebe", "Literature"),
                Triple("Long Walk to Freedom", "Nelson Mandela", "Biography")
            )
            sampleBooks.forEach { (title, author, category) ->
                db.execSQL(
                    "INSERT INTO books (title, author, category, isAvailable) VALUES (?, ?, ?, 1)",
                    arrayOf<Any?>(title, author, category)
                )
            }

            // Two demo bookings so the dashboard is not empty on first launch.
            val now = System.currentTimeMillis()
            val day = RentalPolicy.DAY_MILLIS
            db.execSQL(
                "INSERT INTO bookings (bookOwnerId, userName, bookingDate, returnDeadline, status) " +
                    "VALUES (?, ?, ?, ?, ?)",
                arrayOf<Any?>(1L, "Demo Student", now - 3 * day, now + 4 * day, "ACTIVE")
            )
            db.execSQL(
                "INSERT INTO bookings (bookOwnerId, userName, bookingDate, returnDeadline, status) " +
                    "VALUES (?, ?, ?, ?, ?)",
                arrayOf<Any?>(2L, "Demo Student", now, now + 7 * day, "PENDING")
            )
            db.execSQL("UPDATE books SET isAvailable = 0 WHERE bookId IN (1, 2)")
        }
    }
}
