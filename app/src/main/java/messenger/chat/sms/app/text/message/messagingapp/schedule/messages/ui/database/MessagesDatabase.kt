package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Converter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ScheduleMessage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SyncDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room.ContactsDao
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room.ConversationsDao
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room.MessagesDao
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room.ScheduleMessagesDao
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room.StarredMessagesDao
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room.SyncDbDao
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.DB_NAME


@Database(
    entities = [Conversation::class, Message::class, SyncDb::class, SimpleContact::class, ScheduleMessage::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class MessagesDatabase : RoomDatabase() {

    abstract fun getConversationsDao(): ConversationsDao

    abstract fun getMessagesDao(): MessagesDao

    abstract fun getSyncDbDao(): SyncDbDao

    abstract fun getContactsDao(): ContactsDao

    abstract fun getScheduleMessageDao(): ScheduleMessagesDao

    abstract fun getStarredMessageDao(): StarredMessagesDao

    companion object {
        var db: MessagesDatabase? = null

        fun getInstance(context: Context): MessagesDatabase {
            synchronized(MessagesDatabase::class) {
                if (db == null) {
                    db = databaseBuilder(
                        context.applicationContext,
                        MessagesDatabase::class.java,
                        DB_NAME
                    )
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_4_5)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return db!!
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE conversations ADD COLUMN isBlocked INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE messages ADD COLUMN is_scheduled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE conversations ADD COLUMN is_scheduled INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE messages ADD COLUMN is_starred INTEGER NOT NULL DEFAULT 0")
            }
        }

    }
}
