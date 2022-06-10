package com.example.chap17mp3player

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

//DBHelper의 객체를 생성하는 순간 DB명이 결정됨
//중요!! DB파일명이 있으면 DBHelper객체를 계속 만들어도 onCreate()는 안 불러짐
class DBHelper(context: Context, dbName: String, version: Int): SQLiteOpenHelper(context,dbName,null,version){

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
        val createQuery = "create table ${TABLE_NAME}(id TEXT primary key, title TEXT, artist TEXT, albumID TEXT, duration INTEGER)"
        db?.execSQL(createQuery)



    }

    //기존버전보다 업그레이드가 되었을 때 onCreate()를 부르지 않고 onUpgrade()를 부른다
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //기존 테이블 제거
        val dropQuery = "drop table musicTBL"
//        val dropQuery = "drop table ${TABLE_NAME}"
        db?.execSQL(dropQuery)
        //onCreate() 다시 호출
        this.onCreate(db)


    }

    //음악 넣기
    fun insertMusic(music: Music):Boolean{

        var insertFlag = false
        val insertQuery = "insert into musicTBL(id, title, artist, albumID, duration) " +
                "values('${music.id}', '${music.title}', '${music.artist}', '${music.albumID}', '${music.duration}')"

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

    //모든 음악을 선택
    fun selectMusicAll(): MutableList<Music>?{
        var musicList : MutableList<Music>? = mutableListOf<Music>()

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
                    val music = Music(id,title,artist,albumID,duration)
                    musicList?.add(music)
                }
            }else{
                musicList = null
            }
        }catch (e: Exception){
            Log.d("kim",e.toString())
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }

        return musicList

    }

    //조건에 맞는 음악 선택
    fun selectMusic(id: String):Music?{
        var music : Music? = null

        val selectQuery = "select * from musicTBL where id = '${id}'"
        val db = this.readableDatabase

        var cursor : Cursor? = null

        try{
            cursor = db.rawQuery(selectQuery,null)

            if(cursor.count > 0) {
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getLong(4)
                    music = Music(id, title, artist, albumID, duration)
                }
            }
        }catch(e: Exception){
            Log.d("kim",e.toString())
            music = null
        }finally {
            cursor?.close()
            db.close()
        }
        return music
    }


    //내가 만드는 부분



















}