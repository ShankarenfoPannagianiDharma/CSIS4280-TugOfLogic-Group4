package com.example.project1

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/*  This file contains all the classes that is relevant to local SQLlite database with Room.
 */
class DatabaseClasses {

    //ENTITY CLASSES
    @Entity //a debator: username, if the debator has ever switched.
    data class Debator(
        @PrimaryKey(autoGenerate = true) val id : Int = 0,
        @ColumnInfo(name="username") val username : String,
        @ColumnInfo(name="position") val position : Boolean,
        @ColumnInfo(name="switched") val switched : Boolean
    )
    @Entity //a statement: the text, position re. MC, status in bout
    data class Statement(
        @PrimaryKey(autoGenerate = true) val id : Int = 0,
        @ColumnInfo(name="content") val content : String,
        @ColumnInfo(name="position") val position : Boolean,
        @ColumnInfo(name="status") val status : Int
    )
    @Entity //a game room session: id & mainclaim
    data class Session(
        @PrimaryKey(autoGenerate = true) val id : Int = 0,
        @ColumnInfo(name="mainclaim") val mainclaim : String
    )
    @Entity //a citation: id & content
    data class Cite(
        @PrimaryKey(autoGenerate = true) val id : Int = 0,
        @ColumnInfo(name="content") val content : String
    )

    //DAOs
    @Dao
    interface debatorDAO{
        @Query("SELECT * FROM Debator")
        fun getAll(): List<Debator>

        @Query("SELECT username FROM Debator")
        fun getAllNames(): Flow<List<String>>

        @Query("SELECT * FROM Debator WHERE username LIKE :target LIMIT 1")
        fun findByName(target: String): Debator

        @Query("SELECT * FROM Debator WHERE id == :target LIMIT 1")
        fun findById(target: Int): Debator

        @Query("SELECT position FROM Debator WHERE username LIKE :target LIMIT 1")
        fun getDebatorSide(target: String) : Boolean

        @Query("SELECT COUNT(*) FROM Debator WHERE position == :side")
        fun getNumSide(side: Boolean) : Int

        @Update
        fun updateDebator(vararg debator:Debator)

        @Query("SELECT switched FROM Debator WHERE username LIKE :target LIMIT 1")
        fun isDebatorSwitched(target: String): Boolean

        @Insert
        fun insertDebator(debators: Debator)

        @Delete
        fun delete(debator: Debator)

        @Query("SELECT COUNT(*) FROM Debator")
        fun counts(): Int
    }
    @Dao
    interface StatementDAO{
        @Query("SELECT * FROM Statement")
        fun getAll(): List<Statement>

        @Query("SELECT content FROM Statement WHERE position == :side")
        fun getSideStatements(side: Boolean) : Flow<List<String>>

        @Query("SELECT content FROM Statement")
        fun getAllStatements() : List<String>

        @Query("SELECT * FROM Statement WHERE content LIKE :target LIMIT 1")
        fun findByText(target: String): Statement

        @Query("SELECT * FROM Statement WHERE id == :target LIMIT 1")
        fun findById(target: Int): Statement

        @Insert
        fun insertStatement(statement: Statement)

        @Update
        fun updateStatement(vararg statement:Statement)

        @Delete
        fun delete(statements: Statement)

        @Query("SELECT COUNT(*) FROM Statement")
        fun counts(): Int
    }
    @Dao
    interface SessionDAO{
        @Query("SELECT * FROM Session")
        fun getAll(): List<Session>

        @Query("SELECT * FROM Session WHERE mainclaim LIKE :target LIMIT 1")
        fun findByText(target: String): Session

        @Query("SELECT * FROM Session WHERE id == :target LIMIT 1")
        fun findById(target: Int): Session

        @Query("SELECT mainclaim FROM Session")
        fun getRooms(): List<String>

        @Insert
        fun insertSession(vararg session: Session)

        @Delete
        fun delete(sessions: Session)

        @Query("SELECT COUNT(*) FROM Session")
        fun counts(): Int
    }
    @Dao
    interface CiteDAO{
        @Query("SELECT content FROM Cite")
        fun getAllCites() : Flow<List<String>>
    }

    //Database
    @Database(entities = arrayOf(Debator::class,Statement::class,Session::class,Cite::class), version = 1, exportSchema = false)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun debatorDAO(): debatorDAO
        abstract fun statementDAO(): StatementDAO
        abstract fun sessionDAO(): SessionDAO
        abstract fun citeDAO(): CiteDAO

        companion object {
            @Volatile   //static singleton value for database.
            private var INSTANCE : AppDatabase? = null

            //gets a database for use
            fun getDB(context : Context) : AppDatabase {
                //if already exist, return the db
                val db = INSTANCE
                if (db != null) {
                    return db
                }

                synchronized(this) {
                    val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "baseDB").build()
                    //populate with hardcoded data
                    //determines if data is empty ( and thus may have data entered to it )
                    if(instance.debatorDAO().counts() == 0 && instance.statementDAO().counts() == 0 && instance.sessionDAO().counts() == 0){
                        Log.i("APPGAME:DATABASE","POPULATING ONCE!")
                        //insert debators
                        var dbtr = Debator(0,"Ethan", position = true, switched = false)
                        instance.debatorDAO().insertDebator(dbtr)
                        dbtr = Debator(0,"Maya", position = false, switched = false)
                        instance.debatorDAO().insertDebator(dbtr)
                        dbtr = Debator(0,"Eloise", position = false, switched = false)
                        instance.debatorDAO().insertDebator(dbtr)
                        dbtr = Debator(0,"Tredek", position = true, switched = false)
                        instance.debatorDAO().insertDebator(dbtr)
                        //insert statements
                        var stmnts = Statement(0,"Schools provide a safe simulation of the real world", false, 0)
                        instance.statementDAO().insertStatement(stmnts)
                        stmnts = Statement(0,"Schools put immense pressure on growing children.", true, 0)
                        instance.statementDAO().insertStatement(stmnts)
                        stmnts = Statement(0,"Managing hundreds of children force generalizations on students.", true, 0)
                        instance.statementDAO().insertStatement(stmnts)
                        stmnts = Statement(0,"Soft skills are learnt from a school environment", false, 0)
                        instance.statementDAO().insertStatement(stmnts)
                        stmnts = Statement(0,"Schools are incentivised to only have good grades, regardless of actual status.", true, 0)
                        instance.statementDAO().insertStatement(stmnts)
                        //put dud session
                        var sessn = Session(0,"Schools are detrimental to society.")
                        instance.sessionDAO().insertSession(sessn)
                    }
                    INSTANCE = instance
                    return instance
                }
            }


        }

    }


}
