package com.example.chap17mp3player

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

//DBHelper의 객체를 생성하는 순간 DB명이 결정됨
//중요!! DB파일명이 있으면 DBHelper객체를 계속 만들어도 onCreate()는 안 불러짐
class DBHelper(context: Context?, dbName: String, version: Int): SQLiteOpenHelper(context, dbName, null, version){

    companion object{
        val TABLE_NAME = "musicTBL"
    }

    //DB명이 만들어질 때만 호출됨: 한번만 불러짐
    override fun onCreate(db: SQLiteDatabase?) {
        //테이블 설계
//        db.execSQL() -> 테이블에 변화를 줄 때 사용: insert,update,delete,drop,create
//        db.rawQuery() -> 테이블 속 데이터를 가져갈 때 사용: select -> Cursor로 선택해서 담아오기

        //id 중복되면 안되니까 primary key 설정
//        val createQuery = "create table musicTBL(id TEXT primary key, title TEXT, artist TEXT, albumID TEXT, duration INTEGER)"
        val createQuery = "create table ${TABLE_NAME}(id TEXT primary key, title TEXT, artist TEXT, albumID TEXT, duration INTEGER, favorite INTEGER)"

        db?.execSQL(createQuery)
    }

    //기존버전보다 업그레이드가 되었을 때 onCreate()를 부르지 않고 onUpgrade()를 부른다
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //기존 테이블 제거
//        val dropQuery = "drop table musicTBL"
        val dropQuery = "drop table ${TABLE_NAME}"
        db?.execSQL(dropQuery)
        //onCreate() 다시 호출
        this.onCreate(db)


    }

    //새로운 음악 추가
    fun insertMusic(music: Music):Boolean{

        var insertFlag = false
        val insertQuery = "insert into musicTBL(id, title, artist, albumID, duration, favorite)" +
                "values('${music.id}', '${music.title}', '${music.artist}', '${music.albumID}', '${music.duration}', '${music.favorite}')"
//        val insertQuery = "insert into musicTBL(id, title, artist, albumID, duration)" +
//                "values('${music.id}', '${music.title}', '${music.artist}', '${music.albumID}', '${music.duration}')"

        //db: SQLiteDatabase를 가져오는 방법: writableDatebase, readableDatabase
        val db = this.writableDatabase

        try {
            db.execSQL(insertQuery)
            //잘되면 true 안되면 catch로 넘어감
            insertFlag = true
        }catch (e: SQLException){
            Log.d("kim",e.toString())
        }finally {
            db.close()
        }

        return insertFlag
    }

    //DB에 저장되있는 모든 음악을 선택해서 가져오기
    fun selectMusicAll(): MutableList<Music>{
        var musicList : MutableList<Music> = mutableListOf<Music>()

        val selectQuery = "select * from musicTBL"

        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery,null)

            if(cursor.count > 0){
                //커서를 다음 행으로 이동
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getLong(4)
                    val favorite = cursor.getInt(5)
                    val music = Music(id,title,artist,albumID,duration,favorite)
                    musicList.add(music)
                }
            }
        }catch (e: Exception){
            Log.d("kim",e.toString())
        }finally {
            cursor?.close()
            db.close()
        }

        return musicList

    }


    //검색
    fun searchMusic(query: String): MutableList<Music>{
        var musicList: MutableList<Music> = mutableListOf()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val selectQuery = "select * from musicTBL where artist like '%${query}%' or title like '%${query}%'"

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getLong(4)
                    val favorite = cursor.getInt(5)
                    musicList.add(Music(id, title, artist, albumId, duration, favorite))
                }
            }
        } catch (e: Exception){
            Log.d("kim", "${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }

        return musicList
    }


    //좋아요 눌렀을 때 favorite: 100 대입
    fun pressFavorite(music: Music): Boolean{
        var fav_flag = false
        val db = this.writableDatabase

        var updateQuery: String = "update musicTBL set favorite = 100 where id = '${music.id}'"

        try {
            db.execSQL(updateQuery)
            fav_flag = true
        } catch (e: SQLException){
            Log.d("kim", "${e.printStackTrace()}")
        }

        return fav_flag
    }

    //좋아요 취소했을 때 favorite: 0 대입
    fun pressCancel(music: Music): Boolean{
        var cancel_flag = false
        val db = this.writableDatabase

        var updateQuery: String = "update musicTBL set favorite = 0 where id = '${music.id}'"

        try {
            db.execSQL(updateQuery)
            cancel_flag = true
        } catch (e: SQLException){
            Log.d("kim", "${e.printStackTrace()}")
        }

        return cancel_flag
    }

    //좋아요 누른 음악만 가져오기
    fun getFavorite(): MutableList<Music>{
        var musicList: MutableList<Music> = mutableListOf()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val selectQuery = "select * from musicTBL where favorite = 100"

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getLong(4)
                    val favorite = cursor.getInt(5)
                    musicList.add(Music(id, title, artist, albumId, duration, favorite))
                }
            }
        } catch (e: Exception){
            Log.d("kim", "${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }

        return musicList
    }

    fun setFavoriteData(music: Music): Boolean {

        var flag = false
        val updateQuery = "update musicTBL set likes = ${music.favorite} where id = '${music.id}'"
        val db = this.writableDatabase

        try {
            db.execSQL(updateQuery)
            flag = true
        }catch (e:Exception){
            Log.d("kim", "${e.printStackTrace()}")
        }finally {
            db.close()
        }
        return flag
    }


}
